package com.earth2me.essentials.antibuild;


/**
 * <p>AntiBuildConfig class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public enum AntiBuildConfig {
    disable_build("protect.disable.build", true),
    disable_use("protect.disable.use", true),
    alert_on_placement("protect.alert.on-placement"),
    alert_on_use("protect.alert.on-use"),
    alert_on_break("protect.alert.on-break"),
    blacklist_placement("protect.blacklist.placement"),
    blacklist_usage("protect.blacklist.usage"),
    blacklist_break("protect.blacklist.break"),
    blacklist_piston("protect.blacklist.piston"),
    blacklist_dispenser("protect.blacklist.dispenser");
    private final String configName;
    private final String defValueString;
    private final boolean defValueBoolean;
    private final boolean isList;
    private final boolean isString;

    AntiBuildConfig(final String configName) {
        this(configName, null, false, true, false);
    }

    AntiBuildConfig(final String configName, final boolean defValueBoolean) {
        this(configName, null, defValueBoolean, false, false);
    }

    AntiBuildConfig(final String configName, final String defValueString, final boolean defValueBoolean, final boolean isList, final boolean isString) {
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
