package com.earth2me.essentials.commands;

import com.earth2me.essentials.Backup;
import com.earth2me.essentials.CommandSource;
import org.bukkit.Server;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commandbackup class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandbackup extends EssentialsCommand {
    /**
     * <p>Constructor for Commandbackup.</p>
     */
    public Commandbackup() {
        super("backup");
    }

    /** {@inheritDoc} */
    @Override
    protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        final Backup backup = ess.getBackup();
        if (backup == null) {
            throw new Exception(tl("backupDisabled"));
        }
        final String command = ess.getSettings().getBackupCommand();
        if (command == null || "".equals(command) || "save-all".equalsIgnoreCase(command)) {
            throw new Exception(tl("backupDisabled"));
        }
        backup.run();
        sender.sendMessage(tl("backupStarted"));
    }
}
