package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Set;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commandbreak class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandbreak extends EssentialsCommand {
    /**
     * <p>Constructor for Commandbreak.</p>
     */
    public Commandbreak() {
        super("break");
    }

    //TODO: Switch to use util class
    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        final Block block = user.getBase().getTargetBlock((Set<Material>) null, 20);
        if (block == null) {
            throw new NoChargeException();
        }
        if (block.getType() == Material.AIR) {
            throw new NoChargeException();
        }
        if (block.getType() == Material.BEDROCK && !user.isAuthorized("essentials.break.bedrock")) {
            throw new Exception(tl("noBreakBedrock"));
        }
        //final List<ItemStack> list = (List<ItemStack>)block.getDrops();
        //final BlockBreakEvent event = new BlockBreakEvent(block, user.getBase(), list);
        final BlockBreakEvent event = new BlockBreakEvent(block, user.getBase());
        server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            throw new NoChargeException();
        } else {
            block.setType(Material.AIR);
        }
    }
}
