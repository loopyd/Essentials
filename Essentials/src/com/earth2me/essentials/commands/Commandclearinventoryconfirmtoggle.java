package com.earth2me.essentials.commands;

import static com.earth2me.essentials.I18n.tl;

import com.earth2me.essentials.User;

import org.bukkit.Server;

/**
 * <p>Commandclearinventoryconfirmtoggle class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandclearinventoryconfirmtoggle extends EssentialsCommand {

    /**
     * <p>Constructor for Commandclearinventoryconfirmtoggle.</p>
     */
    public Commandclearinventoryconfirmtoggle() {
        super("clearinventoryconfirmtoggle");
    }

    /** {@inheritDoc} */
    @Override
    public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
        boolean confirmingClear = !user.isPromptingClearConfirm();
        if (commandLabel.toLowerCase().endsWith("on")) {
            confirmingClear = true;
        } else if (commandLabel.toLowerCase().endsWith("off")) {
            confirmingClear = false;
        }
        user.setPromptingClearConfirm(confirmingClear);
        if (confirmingClear) {
            user.sendMessage(tl("clearInventoryConfirmToggleOn"));
        } else {
            user.sendMessage(tl("clearInventoryConfirmToggleOff"));
        }
        user.setConfirmingClearCommand(null);
    }
}

