package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.LocationUtil;
import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.TreeType;

import java.util.Collections;
import java.util.List;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commandbigtree class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandbigtree extends EssentialsCommand {
    /**
     * <p>Constructor for Commandbigtree.</p>
     */
    public Commandbigtree() {
        super("bigtree");
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        TreeType tree;
        if (args.length > 0 && args[0].equalsIgnoreCase("redwood")) {
            tree = TreeType.TALL_REDWOOD;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("tree")) {
            tree = TreeType.BIG_TREE;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("jungle")) {
            tree = TreeType.JUNGLE;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("darkoak")) {
            tree = TreeType.DARK_OAK;
        } else {
            throw new NotEnoughArgumentsException();
        }

        final Location loc = LocationUtil.getTarget(user.getBase());
        final Location safeLocation = LocationUtil.getSafeDestination(loc);
        final boolean success = user.getWorld().generateTree(safeLocation, tree);
        if (success) {
            user.sendMessage(tl("bigTreeSuccess"));
        } else {
            throw new Exception(tl("bigTreeFailure"));
        }
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getTabCompleteOptions(Server server, User user, String commandLabel, String[] args) {
        if (args.length == 1) {
            return Lists.newArrayList("redwood", "tree", "jungle", "darkoak");
        } else {
            return Collections.emptyList();
        }
    }
}
