package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.LocationUtil;
import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.TreeType;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.earth2me.essentials.I18n.tl;

/**
 * <p>Commandtree class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandtree extends EssentialsCommand {
    /**
     * <p>Constructor for Commandtree.</p>
     */
    public Commandtree() {
        super("tree");
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        TreeType tree = null;
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        } else {
            for (TreeType type : TreeType.values()) {
                if (type.name().replace("_", "").equalsIgnoreCase(args[0])) {
                    tree = type;
                    break;
                }
            }
            if (args[0].equalsIgnoreCase("jungle")) {
                tree = TreeType.SMALL_JUNGLE;
            } else if (args[0].equalsIgnoreCase("acacia")) {
                tree = TreeType.ACACIA;
            } else if (args[0].equalsIgnoreCase("birch")) {
                tree = TreeType.BIRCH;
            }
            if (tree == null) {
                throw new NotEnoughArgumentsException();
            }
        }

        final Location loc = LocationUtil.getTarget(user.getBase());
        final Location safeLocation = LocationUtil.getSafeDestination(loc);
        final boolean success = user.getWorld().generateTree(safeLocation, tree);
        if (success) {
            user.sendMessage(tl("treeSpawned"));
        } else {
            user.sendMessage(tl("treeFailure"));
        }
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getTabCompleteOptions(Server server, User user, String commandLabel, String[] args) {
        if (args.length == 1) {
            List<String> options = Lists.newArrayList();
            for (TreeType type : TreeType.values()) {
                options.add(type.name().toLowerCase(Locale.ENGLISH).replace("_", ""));
            }
            return options;
        } else {
            return Collections.emptyList();
        }
    }
}
