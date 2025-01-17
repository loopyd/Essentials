package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import net.ess3.api.events.FlyStatusChangeEvent;

import static com.earth2me.essentials.I18n.tl;

/**
 * <p>Commandfly class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandfly extends EssentialsToggleCommand {
    /**
     * <p>Constructor for Commandfly.</p>
     */
    public Commandfly() {
        super("fly", "essentials.fly.others");
    }

    /** {@inheritDoc} */
    @Override
    protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        toggleOtherPlayers(server, sender, args);
    }

    /** {@inheritDoc} */
    @Override
    protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        handleToggleWithArgs(server, user, args);
    }

    @Override
    void togglePlayer(CommandSource sender, User user, Boolean enabled) {
        if (enabled == null) {
            enabled = !user.getBase().getAllowFlight();
        }
        
        final User controller = sender.isPlayer() ? ess.getUser(sender.getPlayer()) : null;
        FlyStatusChangeEvent event = new FlyStatusChangeEvent(user, controller, enabled);
        ess.getServer().getPluginManager().callEvent(event);
        
        if (!event.isCancelled()) {
            user.getBase().setFallDistance(0f);
            user.getBase().setAllowFlight(enabled);

            if (!user.getBase().getAllowFlight()) {
                user.getBase().setFlying(false);
            }

            user.sendMessage(tl("flyMode", tl(enabled ? "enabled" : "disabled"), user.getDisplayName()));
            if (!sender.isPlayer() || !sender.getPlayer().equals(user.getBase())) {
                sender.sendMessage(tl("flyMode", tl(enabled ? "enabled" : "disabled"), user.getDisplayName()));
            }
        }
    }
}
