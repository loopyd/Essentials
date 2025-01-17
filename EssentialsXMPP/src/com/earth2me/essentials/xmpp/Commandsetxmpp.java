package com.earth2me.essentials.xmpp;

import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.EssentialsCommand;
import com.earth2me.essentials.commands.NotEnoughArgumentsException;
import org.bukkit.Server;


/**
 * <p>Commandsetxmpp class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandsetxmpp extends EssentialsCommand {
    /**
     * <p>Constructor for Commandsetxmpp.</p>
     */
    public Commandsetxmpp() {
        super("setxmpp");
    }

    /** {@inheritDoc} */
    @Override
    protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws NotEnoughArgumentsException {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }

        EssentialsXMPP.getInstance().setAddress(user.getBase(), args[0]);
        user.sendMessage("XMPP address set to " + args[0]);
    }
}
