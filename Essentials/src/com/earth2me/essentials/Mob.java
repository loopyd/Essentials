package com.earth2me.essentials;

import com.earth2me.essentials.utils.EnumUtil;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.earth2me.essentials.I18n.tl;


// Suffixes can be appended on the end of a mob name to make it plural
// Entities without a suffix, will default to 's'
/**
 * <p>Mob class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public enum Mob {
    CHICKEN("Chicken", Enemies.FRIENDLY, EntityType.CHICKEN),
    COW("Cow", Enemies.FRIENDLY, EntityType.COW),
    CREEPER("Creeper", Enemies.ENEMY, EntityType.CREEPER),
    GHAST("Ghast", Enemies.ENEMY, EntityType.GHAST),
    GIANT("Giant", Enemies.ENEMY, EntityType.GIANT),
    HORSE("Horse", Enemies.FRIENDLY, EntityType.HORSE),
    PIG("Pig", Enemies.FRIENDLY, EntityType.PIG),
    PIGZOMB("PigZombie", Enemies.NEUTRAL, EntityType.PIG_ZOMBIE),
    SHEEP("Sheep", Enemies.FRIENDLY, "", EntityType.SHEEP),
    SKELETON("Skeleton", Enemies.ENEMY, EntityType.SKELETON),
    SLIME("Slime", Enemies.ENEMY, EntityType.SLIME),
    SPIDER("Spider", Enemies.ENEMY, EntityType.SPIDER),
    SQUID("Squid", Enemies.FRIENDLY, EntityType.SQUID),
    ZOMBIE("Zombie", Enemies.ENEMY, EntityType.ZOMBIE),
    WOLF("Wolf", Enemies.NEUTRAL, "", EntityType.WOLF),
    CAVESPIDER("CaveSpider", Enemies.ENEMY, EntityType.CAVE_SPIDER),
    ENDERMAN("Enderman", Enemies.ENEMY, "", EntityType.ENDERMAN),
    SILVERFISH("Silverfish", Enemies.ENEMY, "", EntityType.SILVERFISH),
    ENDERDRAGON("EnderDragon", Enemies.ENEMY, EntityType.ENDER_DRAGON),
    VILLAGER("Villager", Enemies.FRIENDLY, EntityType.VILLAGER),
    BLAZE("Blaze", Enemies.ENEMY, EntityType.BLAZE),
    MUSHROOMCOW("MushroomCow", Enemies.FRIENDLY, EntityType.MUSHROOM_COW),
    MAGMACUBE("MagmaCube", Enemies.ENEMY, EntityType.MAGMA_CUBE),
    SNOWMAN("Snowman", Enemies.FRIENDLY, "", EntityType.SNOWMAN),
    OCELOT("Ocelot", Enemies.NEUTRAL, EntityType.OCELOT),
    IRONGOLEM("IronGolem", Enemies.NEUTRAL, EntityType.IRON_GOLEM),
    WITHER("Wither", Enemies.ENEMY, EntityType.WITHER),
    BAT("Bat", Enemies.FRIENDLY, EntityType.BAT),
    WITCH("Witch", Enemies.ENEMY, EntityType.WITCH),
    BOAT("Boat", Enemies.NEUTRAL, EntityType.BOAT),
    MINECART("Minecart", Enemies.NEUTRAL, EntityType.MINECART),
    MINECART_CHEST("ChestMinecart", Enemies.NEUTRAL, EntityType.MINECART_CHEST),
    MINECART_FURNACE("FurnaceMinecart", Enemies.NEUTRAL, EntityType.MINECART_FURNACE),
    MINECART_TNT("TNTMinecart", Enemies.NEUTRAL, EntityType.MINECART_TNT),
    MINECART_HOPPER("HopperMinecart", Enemies.NEUTRAL, EntityType.MINECART_HOPPER),
    MINECART_MOB_SPAWNER("SpawnerMinecart", Enemies.NEUTRAL, EntityType.MINECART_MOB_SPAWNER),
    ENDERCRYSTAL("EnderCrystal", Enemies.NEUTRAL, EntityType.ENDER_CRYSTAL),
    EXPERIENCEORB("ExperienceOrb", Enemies.NEUTRAL, "EXPERIENCE_ORB"),
    ARMOR_STAND("ArmorStand", Enemies.NEUTRAL, "ARMOR_STAND"),
    ENDERMITE("Endermite", Enemies.ENEMY, "ENDERMITE"),
    GUARDIAN("Guardian", Enemies.ENEMY, "GUARDIAN"),
    ELDER_GUARDIAN("ElderGuardian", Enemies.ENEMY, "ELDER_GUARDIAN"),
    RABBIT("Rabbit", Enemies.FRIENDLY, "RABBIT"),
    SHULKER("Shulker", Enemies.ENEMY, "SHULKER"),
    POLAR_BEAR("PolarBear", Enemies.NEUTRAL, "POLAR_BEAR"),
    WITHER_SKELETON("WitherSkeleton", Enemies.ENEMY, "WITHER_SKELETON"),
    STRAY_SKELETON("StraySkeleton", Enemies.ENEMY, "STRAY"),
    ZOMBIE_VILLAGER("ZombieVillager", Enemies.FRIENDLY, "ZOMBIE_VILLAGER"),
    SKELETON_HORSE("SkeletonHorse", Enemies.FRIENDLY, "SKELETON_HORSE"),
    ZOMBIE_HORSE("ZombieHorse", Enemies.FRIENDLY, "ZOMBIE_HORSE"),
    DONKEY("Donkey", Enemies.FRIENDLY, "DONKEY"),
    MULE("Mule", Enemies.FRIENDLY, "MULE"),
    EVOKER("Evoker", Enemies.ENEMY, "EVOKER"),
    VEX("Vex", Enemies.ENEMY, "VEX"),
    VINDICATOR("Vindicator", Enemies.ENEMY, "VINDICATOR"),
    LLAMA("Llama", Enemies.NEUTRAL, "LLAMA"),
    HUSK("Husk", Enemies.ENEMY, "HUSK"),
    ILLUSIONER("Illusioner", Enemies.ENEMY, "ILLUSIONER"),
    PARROT("Parrot", Enemies.NEUTRAL, "PARROT"),
    TURTLE("Turtle", Enemies.NEUTRAL, "TURTLE"),
    PHANTOM("Phantom", Enemies.ENEMY, "PHANTOM"),
    COD("Cod", Enemies.NEUTRAL, "", "COD"),
    SALMON("Salmon", Enemies.NEUTRAL, "", "SALMON"),
    PUFFERFISH("Pufferfish", Enemies.NEUTRAL, "", "PUFFERFISH"),
    TROPICAL_FISH("TropicalFish", Enemies.NEUTRAL, "", "TROPICAL_FISH"),
    DROWNED("Drowned", Enemies.ENEMY, "DROWNED"),
    DOLPHIN("Dolphin", Enemies.NEUTRAL, "DOLPHIN"),
    CAT("Cat", Enemies.FRIENDLY, "CAT"),
    FOX("Fox", Enemies.FRIENDLY, "es", "FOX"),
    PANDA("Panda", Enemies.NEUTRAL, "PANDA"),
    PILLAGER("Pillager", Enemies.ENEMY, "PILLAGER"),
    RAVAGER("Ravager", Enemies.ENEMY, "RAVAGER"),
    TRADER_LLAMA("TraderLlama", Enemies.FRIENDLY, "TRADER_LLAMA"),
    WANDERING_TRADER("WanderingTrader", Enemies.FRIENDLY, "WANDERING_TRADER")
    ;

    /** Constant <code>logger</code> */
    public static final Logger logger = Logger.getLogger("Essentials");

    Mob(String n, Enemies en, String s, EntityType type) {
        this.suffix = s;
        this.name = n;
        this.type = en;
        this.bukkitType = type;
    }

    Mob(String n, Enemies en, EntityType type) {
        this.name = n;
        this.type = en;
        this.bukkitType = type;
    }

    Mob(String n, Enemies en, String s, String typeName) {
        this.suffix = s;
        this.name = n;
        this.type = en;
        bukkitType = EnumUtil.getEntityType(typeName);
    }

    Mob(String n, Enemies en, String typeName) {
        this.name = n;
        this.type = en;
        bukkitType = EnumUtil.getEntityType(typeName);
    }

    public String suffix = "s";
    final public String name;
    final public Enemies type;
    final private EntityType bukkitType;
    private static final Map<String, Mob> hashMap = new HashMap<>();
    private static final Map<EntityType, Mob> bukkitMap = new HashMap<>();

    static {
        for (Mob mob : Mob.values()) {
            hashMap.put(mob.name.toLowerCase(Locale.ENGLISH), mob);
            if (mob.bukkitType != null) {
                bukkitMap.put(mob.bukkitType, mob);
            }
        }
    }

    /**
     * <p>getMobList.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public static Set<String> getMobList() {
        return Collections.unmodifiableSet(hashMap.keySet());
    }

    /**
     * <p>spawn.</p>
     *
     * @param world a {@link org.bukkit.World} object.
     * @param server a {@link org.bukkit.Server} object.
     * @param loc a {@link org.bukkit.Location} object.
     * @return a {@link org.bukkit.entity.Entity} object.
     * @throws com.earth2me.essentials.Mob.MobException if any.
     */
    public Entity spawn(final World world, final Server server, final Location loc) throws MobException {
        final Entity entity = world.spawn(loc, this.bukkitType.getEntityClass());
        if (entity == null) {
            logger.log(Level.WARNING, tl("unableToSpawnMob"));
            throw new MobException();
        }
        return entity;
    }


    public enum Enemies {
        FRIENDLY("friendly"),
        NEUTRAL("neutral"),
        ENEMY("enemy");

        Enemies(final String type) {
            this.type = type;
        }

        /**
         * <p>Getter for the field <code>type</code>.</p>
         */
        final protected String type;
    }

    public EntityType getType() {
        return bukkitType;
    }

    /**
     * <p>fromName.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link com.earth2me.essentials.Mob} object.
     */
    public static Mob fromName(final String name) {
        return hashMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * <p>fromBukkitType.</p>
     *
     * @param type a {@link org.bukkit.entity.EntityType} object.
     * @return a {@link com.earth2me.essentials.Mob} object.
     */
    public static Mob fromBukkitType(final EntityType type) {
        return bukkitMap.get(type);
    }


    public static class MobException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
