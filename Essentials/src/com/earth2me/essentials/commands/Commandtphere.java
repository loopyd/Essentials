package com.earth2me.essentials.commands;

import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.Collections;
import java.util.List;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commandtphere class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandtphere extends EssentialsCommand {
    /**
     * <p>Constructor for Commandtphere.</p>
     */
    public Commandtphere() {
        super("tphere");
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        final User player = getPlayer(server, user, args, 0);
        if (!player.isTeleportEnabled()) {
            throw new Exception(tl("teleportDisabled", player.getDisplayName()));
        }
        if (user.getWorld() != player.getWorld() && ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + user.getWorld().getName())) {
            throw new Exception(tl("noPerm", "essentials.worlds." + user.getWorld().getName()));
        }
        user.getTeleport().teleportPlayer(player, user.getBase(), new Trade(this.getName(), ess), TeleportCause.COMMAND);
        throw new NoChargeException();
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getTabCompleteOptions(Server server, User user, String commandLabel, String[] args) {
        if (args.length == 1) {
            return getPlayers(server, user);
        } else {
            return Collections.emptyList();
        }
    }
}
