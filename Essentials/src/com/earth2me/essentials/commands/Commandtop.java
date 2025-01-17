package com.earth2me.essentials.commands;

import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.LocationUtil;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commandtop class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandtop extends EssentialsCommand {
    /**
     * <p>Constructor for Commandtop.</p>
     */
    public Commandtop() {
        super("top");
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        final int topX = user.getLocation().getBlockX();
        final int topZ = user.getLocation().getBlockZ();
        final float pitch = user.getLocation().getPitch();
        final float yaw = user.getLocation().getYaw();
        final Location loc = LocationUtil.getSafeDestination(new Location(user.getWorld(), topX, user.getWorld().getMaxHeight(), topZ, yaw, pitch));
        user.getTeleport().teleport(loc, new Trade(this.getName(), ess), TeleportCause.COMMAND);
        user.sendMessage(tl("teleportTop", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));

    }
}
