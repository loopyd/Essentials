package com.earth2me.essentials.protect;


/**
 * <p>ProtectConfig class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public enum ProtectConfig {
    disable_contactdmg("protect.disable.contactdmg", false),
    disable_lavadmg("protect.disable.lavadmg", false),
    disable_pvp("protect.disable.pvp", false),
    disable_projectiles("protect.disable.projectiles", false),
    disable_fall("protect.disable.fall", false),
    disable_suffocate("protect.disable.suffocate", false),
    disable_firedmg("protect.disable.firedmg", false),
    disable_lightning("protect.disable.lightning", false),
    disable_drown("protect.disable.drown", false),
    disable_wither("protect.disable.wither", false),
    disable_weather_storm("protect.disable.weather.storm", false),
    disable_weather_lightning("protect.disable.weather.lightning", false),
    disable_weather_thunder("protect.disable.weather.thunder", false),
    prevent_fire_spread("protect.prevent.fire-spread", true),
    prevent_flint_fire("protect.prevent.flint-fire", false),
    prevent_lava_fire_spread("protect.prevent.lava-fire-spread", true),
    prevent_lightning_fire_spread("protect.prevent.lightning-fire-spread", true),
    prevent_water_flow("protect.prevent.water-flow", false),
    prevent_lava_flow("protect.prevent.lava-flow", false),
    prevent_water_bucket_flow("protect.prevent.water-bucket-flow", false),
    prevent_portal_creation("protect.prevent.portal-creation", false),
    prevent_block_on_rail("protect.protect.prevent-block-on-rails", false),
    prevent_tnt_explosion("protect.prevent.tnt-explosion", false),
    prevent_tnt_playerdmg("protect.prevent.tnt-playerdamage", false),
    prevent_tntminecart_explosion("protect.prevent.tnt-minecart-explosion", false),
    prevent_tntminecart_playerdmg("protect.prevent.tnt-minecart-playerdamage", false),
    prevent_fireball_explosion("protect.prevent.fireball-explosion", false),
    prevent_fireball_fire("protect.prevent.fireball-fire", false),
    prevent_fireball_playerdmg("protect.prevent.fireball-playerdamage", false),
    prevent_witherskull_explosion("protect.prevent.witherskull-explosion", false),
    prevent_witherskull_playerdmg("protect.prevent.witherskull-playerdamage", false),
    prevent_wither_spawnexplosion("protect.prevent.wither-spawnexplosion", false),
    prevent_wither_blockreplace("protect.prevent.wither-blockreplace", false),
    prevent_creeper_explosion("protect.prevent.creeper-explosion", true),
    prevent_creeper_playerdmg("protect.prevent.creeper-playerdamage", false),
    prevent_creeper_blockdmg("protect.prevent.creeper-blockdamage", false),
    prevent_enderman_pickup("protect.prevent.enderman-pickup", false),
    prevent_villager_death("protect.prevent.villager-death", false),
    prevent_enderdragon_blockdmg("protect.prevent.enderdragon-blockdamage", true),
    prevent_entitytarget("protect.prevent.entitytarget", false),
    enderdragon_fakeexplosions("protect.enderdragon-fakeexplosions", false);
    private final String configName;
    private final String defValueString;
    private final boolean defValueBoolean;
    private final boolean isList;
    private final boolean isString;

    ProtectConfig(final String configName, final boolean defValueBoolean) {
        this(configName, null, defValueBoolean, false, false);
    }

    ProtectConfig(final String configName, final String defValueString, final boolean defValueBoolean, final boolean isList, final boolean isString) {
        this.configName = configName;
        this.defValueString = defValueString;
        this.defValueBoolean = defValueBoolean;
        this.isList = isList;
        this.isString = isString;
    }

    /**
     * <p>Getter for the field <code>configName</code>.</p>
     *
     * @return the configName
     */
    public String getConfigName() {
        return configName;
    }

    /**
     * <p>getDefaultValueString.</p>
     *
     * @return the default value String
     */
    public String getDefaultValueString() {
        return defValueString;
    }

    /**
     * <p>getDefaultValueBoolean.</p>
     *
     * @return the default value boolean
     */
    public boolean getDefaultValueBoolean() {
        return defValueBoolean;
    }

    /**
     * <p>isString.</p>
     *
     * @return a boolean.
     */
    public boolean isString() {
        return isString;
    }

    /**
     * <p>isList.</p>
     *
     * @return a boolean.
     */
    public boolean isList() {
        return isList;
    }
}
