package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import org.bukkit.Server;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commanddeljail class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commanddeljail extends EssentialsCommand {
    /**
     * <p>Constructor for Commanddeljail.</p>
     */
    public Commanddeljail() {
        super("deljail");
    }

    /** {@inheritDoc} */
    @Override
    protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }

        if (ess.getJails().getJail(args[0]) == null) {
            throw new Exception(tl("jailNotExist"));
        }

        ess.getJails().removeJail(args[0]);
        sender.sendMessage(tl("deleteJail", args[0]));
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
        if (args.length == 1) {
            try {
                return new ArrayList<>(ess.getJails().getList());
            } catch (Exception e) {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }
}
