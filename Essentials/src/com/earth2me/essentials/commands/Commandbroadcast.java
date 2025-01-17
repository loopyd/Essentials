package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import org.bukkit.Server;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commandbroadcast class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandbroadcast extends EssentialsCommand {
    /**
     * <p>Constructor for Commandbroadcast.</p>
     */
    public Commandbroadcast() {
        super("broadcast");
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        sendBroadcast(user.getDisplayName(), args);
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        sendBroadcast(sender.getSender().getName(), args);
    }

    private void sendBroadcast(final String name, final String[] args) throws NotEnoughArgumentsException {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }

        ess.broadcastMessage(tl("broadcast", FormatUtil.replaceFormat(getFinalArg(args, 0)).replace("\\n", "\n"), name));
    }
}
