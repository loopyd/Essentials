package com.earth2me.essentials.signs;

import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.MaterialUtil;
import net.ess3.api.IEssentials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;


/**
 * <p>SignEntityListener class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class SignEntityListener implements Listener {

    private final transient IEssentials ess;

    /**
     * <p>Constructor for SignEntityListener.</p>
     *
     * @param ess a {@link net.ess3.api.IEssentials} object.
     */
    public SignEntityListener(final IEssentials ess) {
        this.ess = ess;
    }

    /**
     * <p>onSignEntityExplode.</p>
     *
     * @param event a {@link org.bukkit.event.entity.EntityExplodeEvent} object.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onSignEntityExplode(final EntityExplodeEvent event) {
        if (ess.getSettings().areSignsDisabled()) {
            event.getHandlers().unregister(this);
            return;
        }

        for (Block block : event.blockList()) {
            if ((MaterialUtil.isSign(block.getType()) && EssentialsSign.isValidSign(ess, new EssentialsSign.BlockSign(block))) || EssentialsSign.checkIfBlockBreaksSigns(block)) {
                event.setCancelled(true);
                return;
            }
            for (EssentialsSign sign : ess.getSettings().enabledSigns()) {
                if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType())) {
                    event.setCancelled(!sign.onBlockExplode(block, ess));
                    return;
                }
            }
        }
    }

    /**
     * <p>onSignEntityChangeBlock.</p>
     *
     * @param event a {@link org.bukkit.event.entity.EntityChangeBlockEvent} object.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignEntityChangeBlock(final EntityChangeBlockEvent event) {
        if (ess.getSettings().areSignsDisabled()) {
            event.getHandlers().unregister(this);
            return;
        }

        final Block block = event.getBlock();
        if ((MaterialUtil.isSign(block.getType()) && EssentialsSign.isValidSign(ess, new EssentialsSign.BlockSign(block))) || EssentialsSign.checkIfBlockBreaksSigns(block)) {
            event.setCancelled(true);
            return;
        }
        for (EssentialsSign sign : ess.getSettings().enabledSigns()) {
            if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockBreak(block, ess)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
