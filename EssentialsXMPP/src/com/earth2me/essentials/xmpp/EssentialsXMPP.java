package com.earth2me.essentials.xmpp;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.metrics.Metrics;
import net.ess3.api.IUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>EssentialsXMPP class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class EssentialsXMPP extends JavaPlugin implements IEssentialsXMPP {
    private static EssentialsXMPP instance = null;
    private transient UserManager users;
    private transient XMPPManager xmpp;
    private transient IEssentials ess;
    private transient Metrics metrics = null;

    static IEssentialsXMPP getInstance() {
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public void onEnable() {
        instance = this;

        final PluginManager pluginManager = getServer().getPluginManager();
        ess = (IEssentials) pluginManager.getPlugin("Essentials");
        if (!this.getDescription().getVersion().equals(ess.getDescription().getVersion())) {
            getLogger().log(Level.WARNING, tl("versionMismatchAll"));
        }
        if (!ess.isEnabled()) {
            this.setEnabled(false);
            return;
        }

        final EssentialsXMPPPlayerListener playerListener = new EssentialsXMPPPlayerListener(ess);
        pluginManager.registerEvents(playerListener, this);

        users = new UserManager(this.getDataFolder());
        xmpp = new XMPPManager(this);

        ess.addReloadListener(users);
        ess.addReloadListener(xmpp);

        if (metrics == null) {
            metrics = new Metrics(this);
            metrics.addCustomChart(new Metrics.SimplePie("config-valid", () -> xmpp.isConfigValid() ? "yes" : "no"));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onDisable() {
        if (xmpp != null) {
            xmpp.disconnect();
        }
        instance = null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        return ess.onCommandEssentials(sender, command, commandLabel, args, EssentialsXMPP.class.getClassLoader(), "com.earth2me.essentials.xmpp.Command", "essentials.", null);
    }

    /** {@inheritDoc} */
    @Override
    public void setAddress(final Player user, final String address) {
        final String username = user.getName().toLowerCase(Locale.ENGLISH);
        instance.users.setAddress(username, address);
    }

    /** {@inheritDoc} */
    @Override
    public String getAddress(final String name) {
        return instance.users.getAddress(name);
    }

    /** {@inheritDoc} */
    @Override
    public IUser getUserByAddress(final String address) {
        String username = instance.users.getUserByAddress(address);
        return username == null ? null : ess.getUser(username);
    }

    /** {@inheritDoc} */
    @Override
    public boolean toggleSpy(final Player user) {
        final String username = user.getName().toLowerCase(Locale.ENGLISH);
        final boolean spy = !instance.users.isSpy(username);
        instance.users.setSpy(username, spy);
        return spy;
    }

    /** {@inheritDoc} */
    @Override
    public String getAddress(final Player user) {
        return instance.users.getAddress(user.getName());
    }

    /** {@inheritDoc} */
    @Override
    public boolean sendMessage(final Player user, final String message) {
        return instance.xmpp.sendMessage(instance.users.getAddress(user.getName()), message);
    }

    /** {@inheritDoc} */
    @Override
    public boolean sendMessage(final String address, final String message) {
        return instance.xmpp.sendMessage(address, message);
    }

    static void updatePresence() {
        instance.xmpp.updatePresence();
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getSpyUsers() {
        return instance.users.getSpyUsers();
    }

    /** {@inheritDoc} */
    @Override
    public void broadcastMessage(final IUser sender, final String message, final String xmppAddress) {
        ess.broadcastMessage(sender, message);
        try {
            for (String address : getSpyUsers()) {
                if (!address.equalsIgnoreCase(xmppAddress)) {
                    sendMessage(address, message);
                }
            }
        } catch (Exception ignored) {}
    }

    /** {@inheritDoc} */
    @Override
    public IEssentials getEss() {
        return ess;
    }
}
