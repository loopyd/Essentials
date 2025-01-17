package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import org.bukkit.Server;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commandtpdeny class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandtpdeny extends EssentialsCommand {
    /**
     * <p>Constructor for Commandtpdeny.</p>
     */
    public Commandtpdeny() {
        super("tpdeny");
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        if (user.getTeleportRequest() == null) {
            throw new Exception(tl("noPendingRequest"));
        }
        final User player = ess.getUser(user.getTeleportRequest());
        if (player == null) {
            throw new Exception(tl("noPendingRequest"));
        }

        user.sendMessage(tl("requestDenied"));
        player.sendMessage(tl("requestDeniedFrom", user.getDisplayName()));
        user.requestTeleport(null, false);
    }
}
