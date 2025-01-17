package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.potion.PotionEffect;

import java.util.Collections;
import java.util.List;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commandheal class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandheal extends EssentialsLoopCommand {
    /**
     * <p>Constructor for Commandheal.</p>
     */
    public Commandheal() {
        super("heal");
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        if (!user.isAuthorized("essentials.heal.cooldown.bypass")) {
            user.healCooldown();
        }

        if (args.length > 0 && user.isAuthorized("essentials.heal.others")) {
            loopOnlinePlayers(server, user.getSource(), true, true, args[0], null);
            return;
        }

        healPlayer(user);
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }

        loopOnlinePlayers(server, sender, true, true, args[0], null);
    }

    /** {@inheritDoc} */
    @Override
    protected void updatePlayer(final Server server, final CommandSource sender, final User player, final String[] args) throws PlayerExemptException {
        try {
            healPlayer(player);
            sender.sendMessage(tl("healOther", player.getDisplayName()));
        } catch (QuietAbortException e) {
            //Handle Quietly
        }
    }

    private void healPlayer(final User user) throws PlayerExemptException, QuietAbortException {
        final Player player = user.getBase();

        if (player.getHealth() == 0) {
            throw new PlayerExemptException(tl("healDead"));
        }

        final double amount = player.getMaxHealth() - player.getHealth();
        final EntityRegainHealthEvent erhe = new EntityRegainHealthEvent(player, amount, RegainReason.CUSTOM);
        ess.getServer().getPluginManager().callEvent(erhe);
        if (erhe.isCancelled()) {
            throw new QuietAbortException();
        }

        double newAmount = player.getHealth() + erhe.getAmount();
        if (newAmount > player.getMaxHealth()) {
            newAmount = player.getMaxHealth();
        }

        player.setHealth(newAmount);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        user.sendMessage(tl("heal"));
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getTabCompleteOptions(Server server, User user, String commandLabel, String[] args) {
        if (args.length == 1 && user.isAuthorized("essentials.heal.others")) {
            return getPlayers(server, user);
        } else {
            return Collections.emptyList();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getTabCompleteOptions(Server server, CommandSource sender, String commandLabel, String[] args) {
        if (args.length == 1) {
            return getPlayers(server, sender);
        } else {
            return Collections.emptyList();
        }
    }
}
