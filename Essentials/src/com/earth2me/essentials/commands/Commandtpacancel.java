package com.earth2me.essentials.commands;

import static com.earth2me.essentials.I18n.tl;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;

import net.ess3.api.IEssentials;

import org.bukkit.Server;


/**
 * <p>Commandtpacancel class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandtpacancel extends EssentialsCommand {

    /**
     * <p>Constructor for Commandtpacancel.</p>
     */
    public Commandtpacancel() {
        super("tpacancel");
    }

    /**
     * Cancel {@link com.earth2me.essentials.User}'s tp request if its {@code requester} is equal to the given {@code requester}.
     *
     * @param ess ess instance
     * @param user user holding tp request
     * @param requester tp requester
     * @return whether tp was cancelled
     * @throws java.lang.Exception if any.
     */
    public static boolean cancelTeleportRequest(IEssentials ess, User user, User requester) throws Exception {
        if (user.getTeleportRequest() != null) {
            User userRequester = ess.getUser(user.getTeleportRequest());
            if (requester.equals(userRequester)) {
                user.requestTeleport(null, false);
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        if (args.length == 0) {
            int cancellations = 0;
            for (User onlineUser : ess.getOnlineUsers()) {
                if (onlineUser == user) continue;
                if (cancelTeleportRequest(ess, onlineUser, user)) {
                    cancellations++;
                }
            }
            if (cancellations > 0) {
                user.sendMessage(tl("teleportRequestAllCancelled", cancellations));
            } else {
                throw new Exception(tl("noPendingRequest"));
            }
        } else {
            User targetPlayer = getPlayer(server, user, args, 0);
            if (cancelTeleportRequest(ess, targetPlayer, user)) {
                user.sendMessage(tl("teleportRequestSpecificCancelled", targetPlayer.getName()));
            }
        }
    }
}
