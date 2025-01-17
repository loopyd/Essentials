package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import org.bukkit.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commanddelhome class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commanddelhome extends EssentialsCommand {
    /**
     * <p>Constructor for Commanddelhome.</p>
     */
    public Commanddelhome() {
        super("delhome");
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, String[] args) throws Exception {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }

        User user = ess.getUser(sender.getPlayer());
        String name;
        String[] expandedArg;

        //Allowing both formats /sethome khobbits house | /sethome khobbits:house
        final String[] nameParts = args[0].split(":");
        if (nameParts[0].length() != args[0].length()) {
            expandedArg = nameParts;
        } else {
            expandedArg = args;
        }

        if (expandedArg.length > 1 && (user == null || user.isAuthorized("essentials.delhome.others"))) {
            user = getPlayer(server, expandedArg, 0, true, true);
            name = expandedArg[1];
        } else if (user == null) {
            throw new NotEnoughArgumentsException();
        } else {
            name = expandedArg[0];
        }

        if (name.equalsIgnoreCase("bed")) {
            throw new Exception(tl("invalidHomeName"));
        }

        user.delHome(name.toLowerCase(Locale.ENGLISH));
        sender.sendMessage(tl("deleteHome", name));
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
        User user = ess.getUser(sender.getPlayer());
        boolean canDelOthers = (user == null || user.isAuthorized("essentials.delhome.others"));

        if (args.length == 1) {
            if (canDelOthers) {
                return getPlayers(server, sender);
            } else {
                return user.getHomes();
            }
        } else if (args.length == 2 && canDelOthers) {
            try {
                user = getPlayer(server, args, 0, true, true);
                return user.getHomes();
            } catch (Exception ex) {
                // No such user
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }
}
