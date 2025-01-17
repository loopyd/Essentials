package com.earth2me.essentials.register.payment;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.HashSet;
import java.util.Set;


/**
 * The <code>Methods</code> initializes Methods that utilize the Method interface based on a "first come, first served"
 * basis.
 *
 * Allowing you to check whether a payment method exists or not.
 *
 * Methods also allows you to set a preferred method of payment before it captures payment plugins in the initialization
 * process.
 *
 * in
 * <code>bukkit.yml</code>: <blockquote><pre>
 *  economy:
 *      preferred: "iConomy"
 * </pre></blockquote>
 *
 * @author Nijikokun &lt;<a href="nijikokun@shortmail.com">nijikokun@shortmail.com</a>&gt; (@nijikokun) @copyright: Copyright (C) 2011 @license: AOL license
 * &lt;<a href="http://aol.nexua.org">http://aol.nexua.org</a>&gt;
 * @version $Id: $Id
 */
public class Methods {
    private static String version = null;
    private static boolean self = false;
    private static Method Method = null;
    private static String preferred = "";
    private static final Set<Method> Methods = new HashSet<>();
    private static final Set<String> Dependencies = new HashSet<>();
    private static final Set<Method> Attachables = new HashSet<>();

    static {
        _init();
    }

    /**
     * Implement all methods along with their respective name and class.
     */
    private static void _init() {
        addMethod("Vault", new com.earth2me.essentials.register.payment.methods.VaultEco());
    }

    /**
     * Used by the plugin to setup version
     *
     * @param v version
     */
    public static void setVersion(String v) {
        version = v;
    }

    /**
     * Use to reset methods during disable
     */
    public static void reset() {
        version = null;
        self = false;
        Method = null;
        preferred = "";
        Attachables.clear();
    }

    /**
     * Use to get version of Register plugin
     *
     * @return version
     */
    public static String getVersion() {
        return version;
    }

    /**
     * Returns an array of payment method names that have been loaded through the <code>_init</code> method.
     *
     * @return <code>Set&lt;String&gt;</code> - Array of payment methods that are loaded.
     * @see #setMethod(PluginManager)
     */
    public static Set<String> getDependencies() {
        return Dependencies;
    }

    /**
     * Interprets Plugin class data to verify whether it is compatible with an existing payment method to use for
     * payments and other various economic activity.
     *
     * @param plugin Plugin data from bukkit, Internal Class file.
     * @return Method <em>or</em> Null
     */
    public static Method createMethod(Plugin plugin) {
        for (Method method : Methods) {
            if (method.isCompatible(plugin)) {
                method.setPlugin(plugin);
                return method;
            }
        }

        return null;
    }

    private static void addMethod(String name, Method method) {
        Dependencies.add(name);
        Methods.add(method);
    }

    /**
     * Verifies if Register has set a payment method for usage yet.
     *
     * @return <code>boolean</code>
     * @see #setMethod(PluginManager)
     * @see #checkDisabled(Plugin)
     * @see #setMethod(PluginManager)
     * @see #checkDisabled(Plugin)
     */
    public static boolean hasMethod() {
        return (Method != null);
    }

    /**
     * Checks Plugin Class against a multitude of checks to verify it's usability as a payment method.
     *
     * @param manager the plugin manager for the server
     * @return <code>boolean</code> True on success, False on failure.
     */
    public static boolean setMethod(PluginManager manager) {
        if (hasMethod()) {
            return true;
        }

        if (self) {
            self = false;
            return false;
        }

        int count = 0;
        boolean match = false;
        Plugin plugin;

        for (String name : getDependencies()) {
            if (hasMethod()) {
                break;
            }

            plugin = manager.getPlugin(name);
            if (plugin == null || !plugin.isEnabled()) {
                continue;
            }

            Method current = createMethod(plugin);
            if (current == null) {
                continue;
            }

            if (preferred.isEmpty()) {
                Method = current;
            } else {
                Attachables.add(current);
            }
        }

        if (!preferred.isEmpty()) {
            do {
                if (hasMethod()) {
                    match = true;
                } else {
                    for (Method attached : Attachables) {
                        if (attached == null) {
                            continue;
                        }

                        if (hasMethod()) {
                            match = true;
                            break;
                        }

                        if (preferred.isEmpty()) {
                            Method = attached;
                        }

                        if (count == 0) {
                            if (preferred.equalsIgnoreCase(attached.getName())) {
                                Method = attached;
                            } else {
                                Method = attached;
                            }
                        }
                    }

                    count++;
                }
            } while (!match);
        }

        return hasMethod();
    }

    /**
     * Sets the preferred economy
     *
     * @return <code>boolean</code>
     * @param check a {@link java.lang.String} object.
     */
    public static boolean setPreferred(String check) {
        if (getDependencies().contains(check)) {
            preferred = check;
            return true;
        }

        return false;
    }

    /**
     * Grab the existing and initialized (hopefully) Method Class.
     *
     * @return <code>Method</code> <em>or</em> <code>Null</code>
     */
    public static Method getMethod() {
        return Method;
    }

    /**
     * Verify is a plugin is disabled, only does this if we there is an existing payment method initialized in
     * Register.
     *
     * @param method Plugin data from bukkit, Internal Class file.
     * @return <code>boolean</code>
     */
    public static boolean checkDisabled(Plugin method) {
        if (!hasMethod()) {
            return true;
        }

        if (Method.isCompatible(method)) {
            Method = null;
        }

        return (Method == null);
    }
}
