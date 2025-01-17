package net.ess3.api.events;

import com.earth2me.essentials.Trade;
import net.ess3.api.IUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
/**
 * Called when the player use the command /warp
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class UserWarpEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private IUser user;
    private String warp;
    private Trade trade;
    private boolean cancelled = false;


    /**
     * <p>Constructor for UserWarpEvent.</p>
     *
     * @param user a {@link net.ess3.api.IUser} object.
     * @param warp a {@link java.lang.String} object.
     * @param trade a {@link com.earth2me.essentials.Trade} object.
     */
    public UserWarpEvent(IUser user, String warp, Trade trade) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.user = user;
        this.warp = warp;
        this.trade = trade;
    }

    /**
     * <p>Getter for the field <code>user</code>.</p>
     *
     * @return a {@link net.ess3.api.IUser} object.
     */
    public IUser getUser() {
        return user;
    }

    /**
     * <p>Getter for the field <code>warp</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getWarp() {
        return warp;
    }

    /**
     * Getting payment handling information
     *
     * @return The payment handling class
     */
    public Trade getTrade() {
        return trade;
    }

    /**
     * <p>Setter for the field <code>warp</code>.</p>
     *
     * @param warp a {@link java.lang.String} object.
     */
    public void setWarp(String warp) {
        this.warp = warp;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /** {@inheritDoc} */
    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    /** {@inheritDoc} */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * <p>getHandlerList.</p>
     *
     * @return a {@link org.bukkit.event.HandlerList} object.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
