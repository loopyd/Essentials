package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.Collections;
import java.util.List;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commandtpall class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandtpall extends EssentialsCommand {
    /**
     * <p>Constructor for Commandtpall.</p>
     */
    public Commandtpall() {
        super("tpall");
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (args.length < 1) {
            if (sender.isPlayer()) {
                teleportAllPlayers(server, sender, ess.getUser(sender.getPlayer()));
                return;
            }
            throw new NotEnoughArgumentsException();
        }

        final User target = getPlayer(server, sender, args, 0);
        teleportAllPlayers(server, sender, target);
    }

    private void teleportAllPlayers(Server server, CommandSource sender, User target) {
        sender.sendMessage(tl("teleportAll"));
        final Location loc = target.getLocation();
        for (User player : ess.getOnlineUsers()) {
            if (target == player) {
                continue;
            }
            if (sender.equals(target.getBase()) && target.getWorld() != player.getWorld() && ess.getSettings().isWorldTeleportPermissions() && !target.isAuthorized("essentials.worlds." + target.getWorld().getName())) {
                continue;
            }
            try {
                player.getTeleport().now(loc, false, TeleportCause.COMMAND);
            } catch (Exception ex) {
                ess.showError(sender, ex, getName());
            }
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
