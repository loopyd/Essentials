package com.earth2me.essentials;

import com.earth2me.essentials.utils.FormatUtil;
import org.bukkit.Server;

import java.util.*;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>PlayerList class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class PlayerList {
    // Cosmetic list formatting
    /**
     * <p>listUsers.</p>
     *
     * @param ess a {@link com.earth2me.essentials.IEssentials} object.
     * @param users a {@link java.util.List} object.
     * @param seperator a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String listUsers(final IEssentials ess, final List<User> users, final String seperator) {
        final StringBuilder groupString = new StringBuilder();
        Collections.sort(users);
        boolean needComma = false;
        for (User user : users) {
            if (needComma) {
                groupString.append(seperator);
            }
            needComma = true;
            if (user.isAfk()) {
                groupString.append(tl("listAfkTag"));
            }
            if (user.isHidden()) {
                groupString.append(tl("listHiddenTag"));
            }
            user.setDisplayNick();
            groupString.append(user.getDisplayName());
            groupString.append("\u00a7f");
        }
        return groupString.toString();
    }

    // Produce a user summary: There are 5 out of maximum 10 players online.
    /**
     * <p>listSummary.</p>
     *
     * @param ess a {@link com.earth2me.essentials.IEssentials} object.
     * @param user a {@link com.earth2me.essentials.User} object.
     * @param showHidden a boolean.
     * @return a {@link java.lang.String} object.
     */
    public static String listSummary(final IEssentials ess, final User user, final boolean showHidden) {
        Server server = ess.getServer();
        int playerHidden = 0;
        int hiddenCount = 0;
        for (User onlinePlayer : ess.getOnlineUsers()) {
            if (onlinePlayer.isHidden() || (user != null && !user.getBase().canSee(onlinePlayer.getBase()))) {
                playerHidden++;
                if (showHidden || user.getBase().canSee(onlinePlayer.getBase())) {
                    hiddenCount++;
                }
            }
        }
        String online;
        if (hiddenCount > 0) {
            online = tl("listAmountHidden", ess.getOnlinePlayers().size() - playerHidden, hiddenCount, server.getMaxPlayers());
        } else {
            online = tl("listAmount", ess.getOnlinePlayers().size() - playerHidden, server.getMaxPlayers());
        }
        return online;
    }

    // Build the basic player list, divided by groups.
    /**
     * <p>getPlayerLists.</p>
     *
     * @param ess a {@link com.earth2me.essentials.IEssentials} object.
     * @param sender a {@link com.earth2me.essentials.User} object.
     * @param showHidden a boolean.
     * @return a {@link java.util.Map} object.
     */
    public static Map<String, List<User>> getPlayerLists(final IEssentials ess, final User sender, final boolean showHidden) {
        Server server = ess.getServer();
        final Map<String, List<User>> playerList = new HashMap<String, List<User>>();
        for (User onlineUser : ess.getOnlineUsers()) {
            if ((sender == null && !showHidden && onlineUser.isHidden()) || (sender != null && !showHidden && !sender.getBase().canSee(onlineUser.getBase()))) {
                continue;
            }
            final String group = FormatUtil.stripFormat(FormatUtil.stripEssentialsFormat(onlineUser.getGroup().toLowerCase()));
            List<User> list = playerList.get(group);
            if (list == null) {
                list = new ArrayList<User>();
                playerList.put(group, list);
            }
            list.add(onlineUser);
        }
        return playerList;
    }

    // Handle the merging of groups
    /**
     * <p>getMergedList.</p>
     *
     * @param ess a {@link com.earth2me.essentials.IEssentials} object.
     * @param playerList a {@link java.util.Map} object.
     * @param groupName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public static List<User> getMergedList(final IEssentials ess, final Map<String, List<User>> playerList, final String groupName) {
        final Set<String> configGroups = ess.getSettings().getListGroupConfig().keySet();
        final List<User> users = new ArrayList<User>();
        for (String configGroup : configGroups) {
            if (configGroup.equalsIgnoreCase(groupName)) {
                String[] groupValues = ess.getSettings().getListGroupConfig().get(configGroup).toString().trim().split(" ");
                for (String groupValue : groupValues) {
                    groupValue = groupValue.toLowerCase(Locale.ENGLISH);
                    if (groupValue == null || groupValue.isEmpty()) {
                        continue;
                    }
                    List<User> u = playerList.get(groupValue.trim());
                    if (u == null || u.isEmpty()) {
                        continue;
                    }
                    playerList.remove(groupValue);
                    users.addAll(u);
                }
            }
        }
        return users;
    }

    // Output a playerlist of just a single group, /list <groupname>
    /**
     * <p>listGroupUsers.</p>
     *
     * @param ess a {@link com.earth2me.essentials.IEssentials} object.
     * @param playerList a {@link java.util.Map} object.
     * @param groupName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public static String listGroupUsers(final IEssentials ess, final Map<String, List<User>> playerList, final String groupName) throws Exception {
        final List<User> users = getMergedList(ess, playerList, groupName);
        final List<User> groupUsers = playerList.get(groupName);
        if (groupUsers != null && !groupUsers.isEmpty()) {
            users.addAll(groupUsers);
        }
        if (users == null || users.isEmpty()) {
            throw new Exception(tl("groupDoesNotExist"));
        }
        final StringBuilder displayGroupName = new StringBuilder();
        displayGroupName.append(Character.toTitleCase(groupName.charAt(0)));
        displayGroupName.append(groupName.substring(1));
        return outputFormat(displayGroupName.toString(), listUsers(ess, users, ", "));
    }

    // Build the output string
    /**
     * <p>outputFormat.</p>
     *
     * @param group a {@link java.lang.String} object.
     * @param message a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String outputFormat(final String group, final String message) {
        final StringBuilder outputString = new StringBuilder();
        outputString.append(tl("listGroupTag", FormatUtil.replaceFormat(group)));
        outputString.append(message);
        return outputString.toString();
    }
}
