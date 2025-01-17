package com.earth2me.essentials;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;


/**
 * <p>EssentialsUserConf class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class EssentialsUserConf extends EssentialsConf {
    public final String username;
    public final UUID uuid;

    /**
     * <p>Constructor for EssentialsUserConf.</p>
     *
     * @param username a {@link java.lang.String} object.
     * @param uuid a {@link java.util.UUID} object.
     * @param configFile a {@link java.io.File} object.
     */
    public EssentialsUserConf(final String username, final UUID uuid, final File configFile) {
        super(configFile);
        this.username = username;
        this.uuid = uuid;
    }

    /** {@inheritDoc} */
    @Override
    public boolean legacyFileExists() {
        final File file = new File(configFile.getParentFile(), username + ".yml");
        return file.exists();
    }

    /** {@inheritDoc} */
    @Override
    public void convertLegacyFile() {
        final File file = new File(configFile.getParentFile(), username + ".yml");
        try {
            Files.move(file, new File(configFile.getParentFile(), uuid + ".yml"));
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to migrate user: " + username, ex);
        }

        setProperty("lastAccountName", username);
    }

    private File getAltFile() {
        final UUID fn = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username.toLowerCase(Locale.ENGLISH)).getBytes(Charsets.UTF_8));
        return new File(configFile.getParentFile(), fn.toString() + ".yml");
    }

    /** {@inheritDoc} */
    @Override
    public boolean altFileExists() {
        if (username.equals(username.toLowerCase())) {
            return false;
        }
        return getAltFile().exists();
    }

    /** {@inheritDoc} */
    @Override
    public void convertAltFile() {
        try {
            Files.move(getAltFile(), new File(configFile.getParentFile(), uuid + ".yml"));
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to migrate user: " + username, ex);
        }
    }
}
