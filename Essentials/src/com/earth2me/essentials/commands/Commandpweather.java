package com.earth2me.essentials.commands;

import com.google.common.collect.Lists;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;

import java.util.*;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commandpweather class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandpweather extends EssentialsCommand {
    /** Constant <code>getAliases</code> */
    public static final Set<String> getAliases = new HashSet<>();
    /** Constant <code>weatherAliases</code> */
    public static final Map<String, WeatherType> weatherAliases = new HashMap<>();

    static {
        getAliases.add("get");
        getAliases.add("list");
        getAliases.add("show");
        getAliases.add("display");
        weatherAliases.put("sun", WeatherType.CLEAR);
        weatherAliases.put("clear", WeatherType.CLEAR);
        weatherAliases.put("storm", WeatherType.DOWNFALL);
        weatherAliases.put("thunder", WeatherType.DOWNFALL);
    }

    /**
     * <p>Constructor for Commandpweather.</p>
     */
    public Commandpweather() {
        super("pweather");
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        // Which Players(s) / Users(s) are we interested in?
        String userSelector = null;
        if (args.length == 2) {
            userSelector = args[1];
        }
        Set<User> users = getUsers(server, sender, userSelector);

        if (args.length == 0) {
            getUsersWeather(sender, users);
            return;
        }

        if (getAliases.contains(args[0])) {
            getUsersWeather(sender, users);
            return;
        }

        if (sender.isPlayer()) {
            User user = ess.getUser(sender.getPlayer());
            if (user != null && (!users.contains(user) || users.size() > 1) && !user.isAuthorized("essentials.pweather.others")) {
                user.sendMessage(tl("pWeatherOthersPermission"));
                return;
            }
        }

        setUsersWeather(sender, users, args[0].toLowerCase());
    }

    /**
     * Used to get the time and inform
     * @param sender the CommandSource object where the call was made from.
     * @param users A Collection of Users to inform.
     */
    private void getUsersWeather(final CommandSource sender, final Collection<User> users) {
        if (users.size() > 1) {
            sender.sendMessage(tl("pWeatherPlayers"));
        }

        for (User user : users) {
            if (user.getBase().getPlayerWeather() == null) {
                sender.sendMessage(tl("pWeatherNormal", user.getName()));
            } else {
                sender.sendMessage(tl("pWeatherCurrent", user.getName(), user.getBase().getPlayerWeather().toString().toLowerCase(Locale.ENGLISH)));
            }
        }
    }

    /**
     * Used to set the time and inform of the change
     * @param sender the CommandSource object where the call was made from.
     * @param users A Collection of Users to inform.
     * @param weatherType Weather type to filter by.
     * @throws Exception if any.
     */
    private void setUsersWeather(final CommandSource sender, final Collection<User> users, final String weatherType) throws Exception {

        final StringBuilder msg = new StringBuilder();
        for (User user : users) {
            if (msg.length() > 0) {
                msg.append(", ");
            }

            msg.append(user.getName());
        }

        if (weatherType.equalsIgnoreCase("reset")) {
            for (User user : users) {
                user.getBase().resetPlayerWeather();
            }

            sender.sendMessage(tl("pWeatherReset", msg));
        } else {
            if (!weatherAliases.containsKey(weatherType)) {
                throw new NotEnoughArgumentsException(tl("pWeatherInvalidAlias"));
            }

            for (User user : users) {
                user.getBase().setPlayerWeather(weatherAliases.get(weatherType));
            }
            sender.sendMessage(tl("pWeatherSet", weatherType, msg.toString()));
        }
    }

    /**
     * Used to parse an argument of the type "users(s) selector"
     * @param server The server object to check from.
     * @param sender The CommandSource object where the call was made from.
     * @param selector A string selector used as a filter.
     * @return A Set of Users.
     * @throws Exception if any.
     */
    private Set<User> getUsers(final Server server, final CommandSource sender, final String selector) throws Exception {
        final Set<User> users = new TreeSet<>(new UserNameComparator());
        // If there is no selector we want the sender itself. Or all users if sender isn't a user.
        if (selector == null) {
            if (sender.isPlayer()) {
                final User user = ess.getUser(sender.getPlayer());
                users.add(user);
            } else {
                for (User user : ess.getOnlineUsers()) {
                    users.add(user);
                }
            }
            return users;
        }

        // Try to find the user with name = selector
        User user = null;
        final List<Player> matchedPlayers = server.matchPlayer(selector);
        if (!matchedPlayers.isEmpty()) {
            user = ess.getUser(matchedPlayers.get(0));
        }

        if (user != null) {
            users.add(user);
        }
        // If that fails, Is the argument something like "*" or "all"?
        else if (selector.equalsIgnoreCase("*") || selector.equalsIgnoreCase("all")) {
            for (User u : ess.getOnlineUsers()) {
                users.add(u);
            }
        }
        // We failed to understand the world target...
        else {
            throw new PlayerNotFoundException();
        }

        return users;
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getTabCompleteOptions(Server server, User user, String commandLabel, String[] args) {
        if (args.length == 1) {
            return Lists.newArrayList("get", "reset", "storm", "sun");
        } else if (args.length == 2 && (getAliases.contains(args[0]) || user == null || user.isAuthorized("essentials.pweather.others"))) {
            return getPlayers(server, user);
        } else {
            return Collections.emptyList();
        }
    }
}
