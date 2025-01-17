package net.ess3.nms.updatedmeta;

import net.ess3.nms.SpawnerProvider;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

/**
 * <p>BlockMetaSpawnerProvider class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class BlockMetaSpawnerProvider extends SpawnerProvider {
    /** {@inheritDoc} */
    @Override
    public ItemStack setEntityType(ItemStack is, EntityType type) {
        BlockStateMeta bsm = (BlockStateMeta) is.getItemMeta();
        BlockState bs = bsm.getBlockState();
        ((CreatureSpawner) bs).setSpawnedType(type);
        bsm.setBlockState(bs);
        is.setItemMeta(bsm);
        return setDisplayName(is, type);
    }

    /** {@inheritDoc} */
    @Override
    public EntityType getEntityType(ItemStack is) {
        BlockStateMeta bsm = (BlockStateMeta) is.getItemMeta();
        CreatureSpawner bs = (CreatureSpawner) bsm.getBlockState();
        return bs.getSpawnedType();
    }

    /** {@inheritDoc} */
    @Override
    public String getHumanName() {
        return "1.8.3+ BlockStateMeta provider";
    }
}
