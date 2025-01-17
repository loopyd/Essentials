package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.utils.FormatUtil;
import org.bukkit.Server;

import static com.earth2me.essentials.I18n.tl;

// This command can be used to echo messages to the users screen, mostly useless but also an #EasterEgg
/**
 * <p>Commandping class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandping extends EssentialsCommand {
    /**
     * <p>Constructor for Commandping.</p>
     */
    public Commandping() {
        super("ping");
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (args.length < 1) {

            sender.sendMessage(tl("pong"));
        } else {
            sender.sendMessage(FormatUtil.replaceFormat(getFinalArg(args, 0)));
        }
    }
}
