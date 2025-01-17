package com.earth2me.essentials;

import com.earth2me.essentials.commands.IEssentialsCommand;
import com.earth2me.essentials.messaging.IMessageRecipient;
import com.earth2me.essentials.messaging.SimpleMessageRecipient;
import com.earth2me.essentials.register.payment.Method;
import com.earth2me.essentials.register.payment.Methods;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.VersionUtil;
import net.ess3.api.IEssentials;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.events.AfkStatusChangeEvent;
import net.ess3.api.events.JailStatusChangeEvent;
import net.ess3.api.events.MuteStatusChangeEvent;
import net.ess3.api.events.UserBalanceUpdateEvent;
import net.ess3.nms.refl.ReflUtil;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>User class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class User extends UserData implements Comparable<User>, IMessageRecipient, net.ess3.api.IUser {
    private static final Logger logger = Logger.getLogger("Essentials");
    private IMessageRecipient messageRecipient;
    private transient UUID teleportRequester;
    private transient boolean teleportRequestHere;
    private transient Location teleportLocation;
    private transient boolean vanished;
    private transient final Teleport teleport;
    private transient long teleportRequestTime;
    private transient long lastOnlineActivity;
    private transient long lastThrottledAction;
    private transient long lastActivity = System.currentTimeMillis();
    private boolean hidden = false;
    private boolean rightClickJump = false;
    private transient Location afkPosition = null;
    private boolean invSee = false;
    private boolean recipeSee = false;
    private boolean enderSee = false;
    private transient long teleportInvulnerabilityTimestamp = 0;
    private boolean ignoreMsg = false;
    private String afkMessage;
    private long afkSince;
    private Map<User, BigDecimal> confirmingPayments = new WeakHashMap<>();
    private String confirmingClearCommand;
    private long lastNotifiedAboutMailsMs;

    /**
     * <p>Constructor for User.</p>
     *
     * @param base a {@link org.bukkit.entity.Player} object.
     * @param ess a {@link net.ess3.api.IEssentials} object.
     */
    public User(final Player base, final IEssentials ess) {
        super(base, ess);
        teleport = new Teleport(this, ess);
        if (isAfk()) {
            afkPosition = this.getLocation();
        }
        if (this.getBase().isOnline()) {
            lastOnlineActivity = System.currentTimeMillis();
        }
        this.messageRecipient = new SimpleMessageRecipient(ess, this);
    }

    User update(final Player base) {
        setBase(base);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAuthorized(final IEssentialsCommand cmd) {
        return isAuthorized(cmd, "essentials.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAuthorized(final IEssentialsCommand cmd, final String permissionPrefix) {
        return isAuthorized(permissionPrefix + (cmd.getName().equals("r") ? "msg" : cmd.getName()));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAuthorized(final String node) {
        final boolean result = isAuthorizedCheck(node);
        if (ess.getSettings().isDebug()) {
            ess.getLogger().log(Level.INFO, "checking if " + base.getName() + " has " + node + " - " + result);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPermissionSet(final String node) {
        return isPermSetCheck(node);
    }

    private boolean isAuthorizedCheck(final String node) {

        if (base instanceof OfflinePlayer) {
            return false;
        }

        try {
            return ess.getPermissionsHandler().hasPermission(base, node);
        } catch (Exception ex) {
            if (ess.getSettings().isDebug()) {
                ess.getLogger().log(Level.SEVERE, "Permission System Error: " + ess.getPermissionsHandler().getName() + " returned: " + ex.getMessage(), ex);
            } else {
                ess.getLogger().log(Level.SEVERE, "Permission System Error: " + ess.getPermissionsHandler().getName() + " returned: " + ex.getMessage());
            }

            return false;
        }
    }

    private boolean isPermSetCheck(final String node) {
        if (base instanceof OfflinePlayer) {
            return false;
        }

        try {
            return ess.getPermissionsHandler().isPermissionSet(base, node);
        } catch (Exception ex) {
            if (ess.getSettings().isDebug()) {
                ess.getLogger().log(Level.SEVERE, "Permission System Error: " + ess.getPermissionsHandler().getName() + " returned: " + ex.getMessage(), ex);
            } else {
                ess.getLogger().log(Level.SEVERE, "Permission System Error: " + ess.getPermissionsHandler().getName() + " returned: " + ex.getMessage());
            }

            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void healCooldown() throws Exception {
        final Calendar now = new GregorianCalendar();
        if (getLastHealTimestamp() > 0) {
            final double cooldown = ess.getSettings().getHealCooldown();
            final Calendar cooldownTime = new GregorianCalendar();
            cooldownTime.setTimeInMillis(getLastHealTimestamp());
            cooldownTime.add(Calendar.SECOND, (int) cooldown);
            cooldownTime.add(Calendar.MILLISECOND, (int) ((cooldown * 1000.0) % 1000.0));
            if (cooldownTime.after(now) && !isAuthorized("essentials.heal.cooldown.bypass")) {
                throw new Exception(tl("timeBeforeHeal", DateUtil.formatDateDiff(cooldownTime.getTimeInMillis())));
            }
        }
        setLastHealTimestamp(now.getTimeInMillis());
    }

    /** {@inheritDoc} */
    @Override
    public void giveMoney(final BigDecimal value) throws MaxMoneyException {
        giveMoney(value, null);
    }

    /** {@inheritDoc} */
    @Override
    public void giveMoney(final BigDecimal value, final CommandSource initiator) throws MaxMoneyException {
        if (value.signum() == 0) {
            return;
        }
        setMoney(getMoney().add(value));
        sendMessage(tl("addedToAccount", NumberUtil.displayCurrency(value, ess)));
        if (initiator != null) {
            initiator.sendMessage(tl("addedToOthersAccount", NumberUtil.displayCurrency(value, ess), this.getDisplayName(), NumberUtil.displayCurrency(getMoney(), ess)));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void payUser(final User reciever, final BigDecimal value) throws Exception {
        if (value.compareTo(BigDecimal.ZERO) < 1) {
            throw new Exception(tl("payMustBePositive"));
        }

        if (canAfford(value)) {
            setMoney(getMoney().subtract(value));
            reciever.setMoney(reciever.getMoney().add(value));
            sendMessage(tl("moneySentTo", NumberUtil.displayCurrency(value, ess), reciever.getDisplayName()));
            reciever.sendMessage(tl("moneyRecievedFrom", NumberUtil.displayCurrency(value, ess), getDisplayName()));
        } else {
            throw new ChargeException(tl("notEnoughMoney", NumberUtil.displayCurrency(value, ess)));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void takeMoney(final BigDecimal value) {
        takeMoney(value, null);
    }

    /** {@inheritDoc} */
    @Override
    public void takeMoney(final BigDecimal value, final CommandSource initiator) {
        if (value.signum() == 0) {
            return;
        }
        try {
            setMoney(getMoney().subtract(value));
        } catch (MaxMoneyException ex) {
            ess.getLogger().log(Level.WARNING, "Invalid call to takeMoney, total balance can't be more than the max-money limit.", ex);
        }
        sendMessage(tl("takenFromAccount", NumberUtil.displayCurrency(value, ess)));
        if (initiator != null) {
            initiator.sendMessage(tl("takenFromOthersAccount", NumberUtil.displayCurrency(value, ess), this.getDisplayName(), NumberUtil.displayCurrency(getMoney(), ess)));
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean canAfford(final BigDecimal cost) {
        return canAfford(cost, true);
    }

    /**
     * <p>canAfford.</p>
     *
     * @param cost a {@link java.math.BigDecimal} object.
     * @param permcheck a boolean.
     * @return a boolean.
     */
    public boolean canAfford(final BigDecimal cost, final boolean permcheck) {
        if (cost.signum() <= 0) {
            return true;
        }
        final BigDecimal remainingBalance = getMoney().subtract(cost);
        if (!permcheck || isAuthorized("essentials.eco.loan")) {
            return (remainingBalance.compareTo(ess.getSettings().getMinMoney()) >= 0);
        }
        return (remainingBalance.signum() >= 0);
    }

    /**
     * <p>dispose.</p>
     */
    public void dispose() {
        ess.runTaskAsynchronously(new Runnable() {
            @Override
            public void run() {
                _dispose();
            }
        });
    }

    private void _dispose() {
        if (!base.isOnline()) {
            this.base = new OfflinePlayer(getConfigUUID(), ess.getServer());
        }
        cleanup();
    }

    /** {@inheritDoc} */
    @Override
    public Boolean canSpawnItem(final Material material) {
        if (ess.getSettings().permissionBasedItemSpawn()) {
            final String name = material.toString().toLowerCase(Locale.ENGLISH).replace("_", "");

            if (isAuthorized("essentials.itemspawn.item-all") || isAuthorized("essentials.itemspawn.item-" + name)) return true;

            if (VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_13_0_R01)) {
                final int id = material.getId();
                if (isAuthorized("essentials.itemspawn.item-" + id)) return true;
            }
        }

        return isAuthorized("essentials.itemspawn.exempt") || !ess.getSettings().itemSpawnBlacklist().contains(material);
    }

    /** {@inheritDoc} */
    @Override
    public void setLastLocation() {
        setLastLocation(this.getLocation());
    }

    /** {@inheritDoc} */
    @Override
    public void setLogoutLocation() {
        setLogoutLocation(this.getLocation());
    }

    /** {@inheritDoc} */
    @Override
    public void requestTeleport(final User player, final boolean here) {
        teleportRequestTime = System.currentTimeMillis();
        teleportRequester = player == null ? null : player.getBase().getUniqueId();
        teleportRequestHere = here;
        if (player == null) {
            teleportLocation = null;
        } else {
            teleportLocation = here ? player.getLocation() : this.getLocation();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasOutstandingTeleportRequest() {
        if (getTeleportRequest() != null) { // Player has outstanding teleport request.
            long timeout = ess.getSettings().getTpaAcceptCancellation();
            if (timeout != 0) {
                if ((System.currentTimeMillis() - getTeleportRequestTime()) / 1000 <= timeout) { // Player has outstanding request
                    return true;
                } else { // outstanding request expired.
                    requestTeleport(null, false);
                    return false;
                }
            } else { // outstanding request does not expire
                return true;
            }
        }
        return false;
    }

    /**
     * <p>getTeleportRequest.</p>
     *
     * @return a {@link java.util.UUID} object.
     */
    public UUID getTeleportRequest() {
        return teleportRequester;
    }

    /**
     * <p>isTpRequestHere.</p>
     *
     * @return a boolean.
     */
    public boolean isTpRequestHere() {
        return teleportRequestHere;
    }

    /**
     * <p>getTpRequestLocation.</p>
     *
     * @return a {@link org.bukkit.Location} object.
     */
    public Location getTpRequestLocation() {
        return teleportLocation;
    }

    /**
     * <p>getNick.</p>
     *
     * @param longnick a boolean.
     * @return a {@link java.lang.String} object.
     */
    public String getNick(final boolean longnick) {
        return getNick(longnick, true, true);
    }

    /**
     * <p>getNick.</p>
     *
     * @param longnick a boolean.
     * @param withPrefix a boolean.
     * @param withSuffix a boolean.
     * @return a {@link java.lang.String} object.
     */
    public String getNick(final boolean longnick, final boolean withPrefix, final boolean withSuffix) {
        final StringBuilder prefix = new StringBuilder();
        String nickname;
        String suffix = "";
        final String nick = getNickname();
        if (ess.getSettings().isCommandDisabled("nick") || nick == null || nick.isEmpty() || nick.equals(getName())) {
            nickname = getName();
        } else if (nick.equalsIgnoreCase(getName())) {
            nickname = nick;
        } else {
            nickname = FormatUtil.replaceFormat(ess.getSettings().getNicknamePrefix()) + nick;
            suffix = "§r";
        }

        if (this.getBase().isOp()) {
            try {
                final ChatColor opPrefix = ess.getSettings().getOperatorColor();
                if (opPrefix != null && opPrefix.toString().length() > 0) {
                    prefix.insert(0, opPrefix.toString());
                    suffix = "§r";
                }
            } catch (Exception e) {
            }
        }

        if (ess.getSettings().addPrefixSuffix()) {
            //These two extra toggles are not documented, because they are mostly redundant #EasterEgg
            if (withPrefix || !ess.getSettings().disablePrefix()) {
                final String ptext = ess.getPermissionsHandler().getPrefix(base).replace('&', '§');
                prefix.insert(0, ptext);
                suffix = "§r";
            }
            if (withSuffix || !ess.getSettings().disableSuffix()) {
                final String stext = ess.getPermissionsHandler().getSuffix(base).replace('&', '§');
                suffix = stext + "§r";
                suffix = suffix.replace("§f§f", "§f").replace("§f§r", "§r").replace("§r§r", "§r");
            }
        }
        final String strPrefix = prefix.toString();
        String output = strPrefix + nickname + suffix;
        if (!longnick && output.length() > 16) {
            output = strPrefix + nickname;
        }
        if (!longnick && output.length() > 16) {
            output = FormatUtil.lastCode(strPrefix) + nickname;
        }
        if (!longnick && output.length() > 16) {
            output = FormatUtil.lastCode(strPrefix) + nickname.substring(0, 14);
        }
        if (output.charAt(output.length() - 1) == '§') {
            output = output.substring(0, output.length() - 1);
        }
        return output;
    }

    /**
     * <p>setDisplayNick.</p>
     */
    public void setDisplayNick() {
        if (base.isOnline() && ess.getSettings().changeDisplayName()) {
            this.getBase().setDisplayName(getNick(true));
            if (isAfk()) {
                updateAfkListName();
            } else if (ess.getSettings().changePlayerListName()) {
                // 1.8 enabled player list-names longer than 16 characters.
                // If the server is on 1.8 or higher, provide that functionality. Otherwise, keep prior functionality.
                boolean higherOrEqualTo1_8 = ReflUtil.getNmsVersionObject().isHigherThanOrEqualTo(ReflUtil.V1_8_R1);
                String name = getNick(higherOrEqualTo1_8, ess.getSettings().isAddingPrefixInPlayerlist(), ess.getSettings().isAddingSuffixInPlayerlist());
                try {
                    this.getBase().setPlayerListName(name);
                } catch (IllegalArgumentException e) {
                    if (ess.getSettings().isDebug()) {
                        logger.log(Level.INFO, "Playerlist for " + name + " was not updated. Name clashed with another online player.");
                    }
                }
            }
        }
    }

    /**
     * <p>getDisplayName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDisplayName() {
        return super.getBase().getDisplayName() == null || (ess.getSettings().hideDisplayNameInVanish() && isHidden()) ? super.getBase().getName() : super.getBase().getDisplayName();
    }

    /** {@inheritDoc} */
    @Override
    public Teleport getTeleport() {
        return teleport;
    }

    /**
     * <p>Getter for the field <code>lastOnlineActivity</code>.</p>
     *
     * @return a long.
     */
    public long getLastOnlineActivity() {
        return lastOnlineActivity;
    }

    /**
     * <p>Setter for the field <code>lastOnlineActivity</code>.</p>
     *
     * @param timestamp a long.
     */
    public void setLastOnlineActivity(final long timestamp) {
        lastOnlineActivity = timestamp;
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal getMoney() {
        final long start = System.nanoTime();
        final BigDecimal value = _getMoney();
        final long elapsed = System.nanoTime() - start;
        if (elapsed > ess.getSettings().getEconomyLagWarning()) {
            ess.getLogger().log(Level.INFO, "Lag Notice - Slow Economy Response - Request took over {0}ms!", elapsed / 1000000.0);
        }
        return value;
    }

    private BigDecimal _getMoney() {
        if (ess.getSettings().isEcoDisabled()) {
            if (ess.getSettings().isDebug()) {
                ess.getLogger().info("Internal economy functions disabled, aborting balance check.");
            }
            return BigDecimal.ZERO;
        }
        if (Methods.hasMethod()) {
            try {
                final Method method = Methods.getMethod();
                if (!method.hasAccount(this.getName())) {
                    throw new Exception();
                }
                final Method.MethodAccount account = Methods.getMethod().getAccount(this.getName());
                return BigDecimal.valueOf(account.balance());
            } catch (Exception ex) {
            }
        }
        return super.getMoney();
    }

    /** {@inheritDoc} */
    @Override
    public void setMoney(final BigDecimal value) throws MaxMoneyException {
        if (ess.getSettings().isEcoDisabled()) {
            if (ess.getSettings().isDebug()) {
                ess.getLogger().info("Internal economy functions disabled, aborting balance change.");
            }
            return;
        }
        final BigDecimal oldBalance = _getMoney();
        
        UserBalanceUpdateEvent updateEvent = new UserBalanceUpdateEvent(this.getBase(), oldBalance, value);
        ess.getServer().getPluginManager().callEvent(updateEvent);
        BigDecimal newBalance = updateEvent.getNewBalance();
        
        if (Methods.hasMethod()) {
            try {
                final Method method = Methods.getMethod();
                if (!method.hasAccount(this.getName())) {
                    throw new Exception();
                }
                final Method.MethodAccount account = Methods.getMethod().getAccount(this.getName());
                account.set(newBalance.doubleValue());
            } catch (Exception ex) {
            }
        }
        super.setMoney(newBalance, true);
        Trade.log("Update", "Set", "API", getName(), new Trade(newBalance, ess), null, null, null, ess);
    }

    /**
     * <p>updateMoneyCache.</p>
     *
     * @param value a {@link java.math.BigDecimal} object.
     */
    public void updateMoneyCache(final BigDecimal value) {
        if (ess.getSettings().isEcoDisabled()) {
            return;
        }
        if (Methods.hasMethod() && super.getMoney() != value) {
            try {
                super.setMoney(value, false);
            } catch (MaxMoneyException ex) {
                // We don't want to throw any errors here, just updating a cache
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setAfk(final boolean set) {
        final AfkStatusChangeEvent afkEvent = new AfkStatusChangeEvent(this, set);
        ess.getServer().getPluginManager().callEvent(afkEvent);
        if (afkEvent.isCancelled()) {
            return;
        }

        this.getBase().setSleepingIgnored(this.isAuthorized("essentials.sleepingignored") || set && ess.getSettings().sleepIgnoresAfkPlayers());
        if (set && !isAfk()) {
            afkPosition = this.getLocation();
            this.afkSince = System.currentTimeMillis();
        } else if (!set && isAfk()) {
            afkPosition = null;
            this.afkMessage = null;
            this.afkSince = 0;
        }
        _setAfk(set);
        updateAfkListName();
    }
    
    private void updateAfkListName() {
        if (ess.getSettings().isAfkListName()) {
            if(isAfk()) {
                String afkName = ess.getSettings().getAfkListName().replace("{PLAYER}", getDisplayName()).replace("{USERNAME}", getName());
                getBase().setPlayerListName(afkName);
            } else {
                getBase().setPlayerListName(null);
            }
        }
    }

    /**
     * <p>toggleAfk.</p>
     *
     * @return a boolean.
     */
    public boolean toggleAfk() {
        setAfk(!isAfk());
        return isAfk();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isHidden() {
        return hidden;
    }

    /**
     * <p>isHidden.</p>
     *
     * @param player a {@link org.bukkit.entity.Player} object.
     * @return a boolean.
     */
    public boolean isHidden(final Player player) {
        return hidden || !player.canSee(getBase());
    }

    /** {@inheritDoc} */
    @Override
    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
        if (hidden == true) {
            setLastLogout(getLastOnlineActivity());
        }
    }

    //Returns true if status expired during this check
    /**
     * <p>checkJailTimeout.</p>
     *
     * @param currentTime a long.
     * @return a boolean.
     */
    public boolean checkJailTimeout(final long currentTime) {
        if (getJailTimeout() > 0 && getJailTimeout() < currentTime && isJailed()) {
            final JailStatusChangeEvent event = new JailStatusChangeEvent(this, null, false);
            ess.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                setJailTimeout(0);
                setJailed(false);
                sendMessage(tl("haveBeenReleased"));
                setJail(null);
                if (ess.getSettings().isTeleportBackWhenFreedFromJail()) {
                    try {
                        getTeleport().back();
                    } catch (Exception ex) {
                        try {
                            getTeleport().respawn(null, TeleportCause.PLUGIN);
                        } catch (Exception ex1) {
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    //Returns true if status expired during this check
    /**
     * <p>checkMuteTimeout.</p>
     *
     * @param currentTime a long.
     * @return a boolean.
     */
    public boolean checkMuteTimeout(final long currentTime) {
        if (getMuteTimeout() > 0 && getMuteTimeout() < currentTime && isMuted()) {
            final MuteStatusChangeEvent event = new MuteStatusChangeEvent(this, null, false);
            ess.getServer().getPluginManager().callEvent(event);
            
            if (!event.isCancelled()) {
                setMuteTimeout(0);
                sendMessage(tl("canTalkAgain"));
                setMuted(false);
                setMuteReason(null);
                return true;
            }
        }
        return false;
    }

    /**
     * <p>updateActivity.</p>
     *
     * @param broadcast a boolean.
     */
    public void updateActivity(final boolean broadcast) {
        if (isAfk()) {
            setAfk(false);
            if (broadcast && !isHidden()) {
                setDisplayNick();
                final String msg = tl("userIsNotAway", getDisplayName());
                if (!msg.isEmpty()) {
                    ess.broadcastMessage(this, msg);
                }
            }
        }
        lastActivity = System.currentTimeMillis();
    }

    /**
     * <p>updateActivityOnMove.</p>
     *
     * @param broadcast a boolean.
     */
    public void updateActivityOnMove(final boolean broadcast) {
        if(ess.getSettings().cancelAfkOnMove()) {
            updateActivity(broadcast);
        }
    }

    /**
     * <p>updateActivityOnInteract.</p>
     *
     * @param broadcast a boolean.
     */
    public void updateActivityOnInteract(final boolean broadcast) {
        if(ess.getSettings().cancelAfkOnInteract()) {
            updateActivity(broadcast);
        }
    }

    /**
     * <p>checkActivity.</p>
     */
    public void checkActivity() {
        // Graceful time before the first afk check call. 
        if (System.currentTimeMillis() - lastActivity <= 10000) {
            return;
        }

        final long autoafkkick = ess.getSettings().getAutoAfkKick();
        if (autoafkkick > 0
            && lastActivity > 0 && (lastActivity + (autoafkkick * 1000)) < System.currentTimeMillis()
            && !isAuthorized("essentials.kick.exempt")
            && !isAuthorized("essentials.afk.kickexempt")) {
            final String kickReason = tl("autoAfkKickReason", autoafkkick / 60.0);
            lastActivity = 0;
            this.getBase().kickPlayer(kickReason);


            for (User user : ess.getOnlineUsers()) {
                if (user.isAuthorized("essentials.kick.notify")) {
                    user.sendMessage(tl("playerKicked", Console.NAME, getName(), kickReason));
                }
            }
        }
        final long autoafk = ess.getSettings().getAutoAfk();
        if (!isAfk() && autoafk > 0 && lastActivity + autoafk * 1000 < System.currentTimeMillis() && isAuthorized("essentials.afk.auto")) {
            setAfk(true);
            if (!isHidden()) {
                setDisplayNick();
                final String msg = tl("userIsAway", getDisplayName());
                if (!msg.isEmpty()) {
                    ess.broadcastMessage(this, msg);
                }
            }
        }
    }

    /**
     * <p>Getter for the field <code>afkPosition</code>.</p>
     *
     * @return a {@link org.bukkit.Location} object.
     */
    public Location getAfkPosition() {
        return afkPosition;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isGodModeEnabled() {
        if (super.isGodModeEnabled()) {
            // This enables the no-god-in-worlds functionality where the actual player god mode state is never modified in disabled worlds,
            // but this method gets called every time the player takes damage. In the case that the world has god-mode disabled then this method
            // will return false and the player will take damage, even though they are in god mode (isGodModeEnabledRaw()).
            if (!ess.getSettings().getNoGodWorlds().contains(this.getLocation().getWorld().getName())) {
                return true;
            }
        }
        if (isAfk()) {
            // Protect AFK players by representing them in a god mode state to render them invulnerable to damage.
            if (ess.getSettings().getFreezeAfkPlayers()) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>isGodModeEnabledRaw.</p>
     *
     * @return a boolean.
     */
    public boolean isGodModeEnabledRaw() {
        return super.isGodModeEnabled();
    }

    /** {@inheritDoc} */
    @Override
    public String getGroup() {
        final String result = ess.getPermissionsHandler().getGroup(base);
        if (ess.getSettings().isDebug()) {
            ess.getLogger().log(Level.INFO, "looking up groupname of " + base.getName() + " - " + result);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean inGroup(final String group) {
        final boolean result = ess.getPermissionsHandler().inGroup(base, group);
        if (ess.getSettings().isDebug()) {
            ess.getLogger().log(Level.INFO, "checking if " + base.getName() + " is in group " + group + " - " + result);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canBuild() {
        if (this.getBase().isOp()) {
            return true;
        }
        return ess.getPermissionsHandler().canBuild(base, getGroup());
    }

    /**
     * <p>Getter for the field <code>teleportRequestTime</code>.</p>
     *
     * @return a long.
     */
    public long getTeleportRequestTime() {
        return teleportRequestTime;
    }

    /**
     * <p>isInvSee.</p>
     *
     * @return a boolean.
     */
    public boolean isInvSee() {
        return invSee;
    }

    /**
     * <p>Setter for the field <code>invSee</code>.</p>
     *
     * @param set a boolean.
     */
    public void setInvSee(final boolean set) {
        invSee = set;
    }

    /**
     * <p>isEnderSee.</p>
     *
     * @return a boolean.
     */
    public boolean isEnderSee() {
        return enderSee;
    }

    /**
     * <p>Setter for the field <code>enderSee</code>.</p>
     *
     * @param set a boolean.
     */
    public void setEnderSee(final boolean set) {
        enderSee = set;
    }

    /** {@inheritDoc} */
    @Override
    public void enableInvulnerabilityAfterTeleport() {
        final long time = ess.getSettings().getTeleportInvulnerability();
        if (time > 0) {
            teleportInvulnerabilityTimestamp = System.currentTimeMillis() + time;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void resetInvulnerabilityAfterTeleport() {
        if (teleportInvulnerabilityTimestamp != 0 && teleportInvulnerabilityTimestamp < System.currentTimeMillis()) {
            teleportInvulnerabilityTimestamp = 0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasInvulnerabilityAfterTeleport() {
        return teleportInvulnerabilityTimestamp != 0 && teleportInvulnerabilityTimestamp >= System.currentTimeMillis();
    }

    /**
     * <p>canInteractVanished.</p>
     *
     * @return a boolean.
     */
    public boolean canInteractVanished() {
        return isAuthorized("essentials.vanish.interact");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isIgnoreMsg() {
        return ignoreMsg;
    }

    /** {@inheritDoc} */
    @Override
    public void setIgnoreMsg(boolean ignoreMsg) {
        this.ignoreMsg = ignoreMsg;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isVanished() {
        return vanished;
    }

    /** {@inheritDoc} */
    @Override
    public void setVanished(final boolean set) {
        vanished = set;
        if (set) {
            for (User user : ess.getOnlineUsers()) {
                if (!user.isAuthorized("essentials.vanish.see")) {
                    user.getBase().hidePlayer(getBase());
                }
            }
            setHidden(true);
            ess.getVanishedPlayersNew().add(getName());
            if (isAuthorized("essentials.vanish.effect")) {
                this.getBase().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false));
            }
        } else {
            for (Player p : ess.getOnlinePlayers()) {
                p.showPlayer(getBase());
            }
            setHidden(false);
            ess.getVanishedPlayersNew().remove(getName());
            if (isAuthorized("essentials.vanish.effect")) {
                this.getBase().removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }
    }

    /**
     * <p>checkSignThrottle.</p>
     *
     * @return a boolean.
     */
    public boolean checkSignThrottle() {
        if (isSignThrottled()) {
            return true;
        }
        updateThrottle();
        return false;
    }

    /**
     * <p>isSignThrottled.</p>
     *
     * @return a boolean.
     */
    public boolean isSignThrottled() {
        final long minTime = lastThrottledAction + (1000 / ess.getSettings().getSignUsePerSecond());
        return (System.currentTimeMillis() < minTime);
    }

    /**
     * <p>updateThrottle.</p>
     */
    public void updateThrottle() {
        lastThrottledAction = System.currentTimeMillis();
    }

    /**
     * <p>isFlyClickJump.</p>
     *
     * @return a boolean.
     */
    public boolean isFlyClickJump() {
        return rightClickJump;
    }

    /**
     * <p>Setter for the field <code>rightClickJump</code>.</p>
     *
     * @param rightClickJump a boolean.
     */
    public void setRightClickJump(boolean rightClickJump) {
        this.rightClickJump = rightClickJump;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isIgnoreExempt() {
        return this.isAuthorized("essentials.chat.ignoreexempt");
    }

    /**
     * <p>isRecipeSee.</p>
     *
     * @return a boolean.
     */
    public boolean isRecipeSee() {
        return recipeSee;
    }

    /**
     * <p>Setter for the field <code>recipeSee</code>.</p>
     *
     * @param recipeSee a boolean.
     */
    public void setRecipeSee(boolean recipeSee) {
        this.recipeSee = recipeSee;
    }

    /** {@inheritDoc} */
    @Override
    public void sendMessage(String message) {
        if (!message.isEmpty()) {
            base.sendMessage(message);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(final User other) {
        return FormatUtil.stripFormat(getDisplayName()).compareToIgnoreCase(FormatUtil.stripFormat(other.getDisplayName()));
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof User)) {
            return false;
        }
        return this.getName().equalsIgnoreCase(((User) object).getName());

    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public CommandSource getSource() {
        return new CommandSource(getBase());
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return this.getBase().getName();
    }
    
    /** {@inheritDoc} */
    @Override public boolean isReachable() {
        return getBase().isOnline();
    }

    /** {@inheritDoc} */
    @Override public MessageResponse sendMessage(IMessageRecipient recipient, String message) {
        return this.messageRecipient.sendMessage(recipient, message);
    }

    /** {@inheritDoc} */
    @Override public MessageResponse onReceiveMessage(IMessageRecipient sender, String message) {
        return this.messageRecipient.onReceiveMessage(sender, message);
    }

    /** {@inheritDoc} */
    @Override public IMessageRecipient getReplyRecipient() {
        return this.messageRecipient.getReplyRecipient();
    }

    /** {@inheritDoc} */
    @Override public void setReplyRecipient(IMessageRecipient recipient) {
        this.messageRecipient.setReplyRecipient(recipient);
    }

    /** {@inheritDoc} */
    @Override
    public String getAfkMessage() {
        return this.afkMessage;
    }

    /** {@inheritDoc} */
    @Override
    public void setAfkMessage(String message) {
        if (isAfk()) {
            this.afkMessage = message;
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getAfkSince() {
        return afkSince;
    }

    /** {@inheritDoc} */
    @Override
    public Map<User, BigDecimal> getConfirmingPayments() {
        return confirmingPayments;
    }

    /**
     * <p>Getter for the field <code>confirmingClearCommand</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getConfirmingClearCommand() {
        return confirmingClearCommand;
    }
    
    /**
     * <p>Setter for the field <code>confirmingClearCommand</code>.</p>
     *
     * @param command a {@link java.lang.String} object.
     */
    public void setConfirmingClearCommand(String command) {
        this.confirmingClearCommand = command;
    }

    /**
     * Returns the {@link org.bukkit.inventory.ItemStack} in the main hand or off-hand. If the main hand is empty then the offhand item is returned - also nullable.
     *
     * @return a {@link org.bukkit.inventory.ItemStack} object.
     */
    public ItemStack getItemInHand() {
        if (VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_9_R01)) {
            return getBase().getInventory().getItemInHand();
        } else {
            PlayerInventory inventory = getBase().getInventory();
            return inventory.getItemInMainHand() != null ? inventory.getItemInMainHand() : inventory.getItemInOffHand();
        }
    }
    
    /**
     * <p>notifyOfMail.</p>
     */
    public void notifyOfMail() {
        List<String> mails = getMails();
        if (mails != null && !mails.isEmpty()) {
            int notifyPlayerOfMailCooldown = ess.getSettings().getNotifyPlayerOfMailCooldown() * 1000;
            if (System.currentTimeMillis() - lastNotifiedAboutMailsMs >= notifyPlayerOfMailCooldown) {
                sendMessage(tl("youHaveNewMail", mails.size()));
                lastNotifiedAboutMailsMs = System.currentTimeMillis();
            }
        }
    }
}
