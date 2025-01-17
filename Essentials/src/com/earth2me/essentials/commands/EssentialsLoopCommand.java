package com.earth2me.essentials.commands;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.UUID;


/**
 * <p>Abstract EssentialsLoopCommand class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public abstract class EssentialsLoopCommand extends EssentialsCommand {
    /**
     * <p>Constructor for EssentialsLoopCommand.</p>
     *
     * @param command a {@link java.lang.String} object.
     */
    public EssentialsLoopCommand(String command) {
        super(command);
    }

    /**
     * <p>loopOfflinePlayers.</p>
     *
     * @param server a {@link org.bukkit.Server} object.
     * @param sender a {@link com.earth2me.essentials.CommandSource} object.
     * @param multipleStringMatches a boolean.
     * @param matchWildcards a boolean.
     * @param searchTerm a {@link java.lang.String} object.
     * @param commandArgs an array of {@link java.lang.String} objects.
     * @throws com.earth2me.essentials.commands.PlayerNotFoundException if any.
     * @throws com.earth2me.essentials.commands.NotEnoughArgumentsException if any.
     * @throws com.earth2me.essentials.commands.PlayerExemptException if any.
     * @throws com.earth2me.essentials.ChargeException if any.
     * @throws net.ess3.api.MaxMoneyException if any.
     */
    protected void loopOfflinePlayers(final Server server, final CommandSource sender, final boolean multipleStringMatches, boolean matchWildcards, final String searchTerm, final String[] commandArgs) throws PlayerNotFoundException, NotEnoughArgumentsException, PlayerExemptException, ChargeException, MaxMoneyException {
        if (searchTerm.isEmpty()) {
            throw new PlayerNotFoundException();
        }

        if (matchWildcards && searchTerm.contentEquals("**")) {
            for (UUID sUser : ess.getUserMap().getAllUniqueUsers()) {
                final User matchedUser = ess.getUser(sUser);
                updatePlayer(server, sender, matchedUser, commandArgs);
            }
        } else if (matchWildcards && searchTerm.contentEquals("*")) {
            boolean skipHidden = sender.isPlayer() && !ess.getUser(sender.getPlayer()).canInteractVanished();
            for (User onlineUser : ess.getOnlineUsers()) {
                if (skipHidden && onlineUser.isHidden(sender.getPlayer()) && !sender.getPlayer().canSee(onlineUser.getBase())) {
                    continue;
                }
                updatePlayer(server, sender, onlineUser, commandArgs);
            }
        } else if (multipleStringMatches) {
            if (searchTerm.trim().length() < 3) {
                throw new PlayerNotFoundException();
            }
            final List<Player> matchedPlayers = server.matchPlayer(searchTerm);
            if (matchedPlayers.isEmpty()) {
                final User matchedUser = getPlayer(server, searchTerm, true, true);
                updatePlayer(server, sender, matchedUser, commandArgs);
            }
            for (Player matchPlayer : matchedPlayers) {
                final User matchedUser = ess.getUser(matchPlayer);
                updatePlayer(server, sender, matchedUser, commandArgs);
            }
        } else {
            final User user = getPlayer(server, searchTerm, true, true);
            updatePlayer(server, sender, user, commandArgs);
        }
    }

    /**
     * <p>loopOnlinePlayers.</p>
     *
     * @param server a {@link org.bukkit.Server} object.
     * @param sender a {@link com.earth2me.essentials.CommandSource} object.
     * @param multipleStringMatches a boolean.
     * @param matchWildcards a boolean.
     * @param searchTerm a {@link java.lang.String} object.
     * @param commandArgs an array of {@link java.lang.String} objects.
     * @throws com.earth2me.essentials.commands.PlayerNotFoundException if any.
     * @throws com.earth2me.essentials.commands.NotEnoughArgumentsException if any.
     * @throws com.earth2me.essentials.commands.PlayerExemptException if any.
     * @throws com.earth2me.essentials.ChargeException if any.
     * @throws net.ess3.api.MaxMoneyException if any.
     */
    protected void loopOnlinePlayers(final Server server, final CommandSource sender, final boolean multipleStringMatches, boolean matchWildcards, final String searchTerm, final String[] commandArgs) throws PlayerNotFoundException, NotEnoughArgumentsException, PlayerExemptException, ChargeException, MaxMoneyException {
        if (searchTerm.isEmpty()) {
            throw new PlayerNotFoundException();
        }

        boolean skipHidden = sender.isPlayer() && !ess.getUser(sender.getPlayer()).canInteractVanished();

        if (matchWildcards && (searchTerm.contentEquals("**") || searchTerm.contentEquals("*"))) {
            for (User onlineUser : ess.getOnlineUsers()) {
                if (skipHidden && onlineUser.isHidden(sender.getPlayer()) && !sender.getPlayer().canSee(onlineUser.getBase())) {
                    continue;
                }
                updatePlayer(server, sender, onlineUser, commandArgs);
            }
        } else if (multipleStringMatches) {
            if (searchTerm.trim().length() < 2) {
                throw new PlayerNotFoundException();
            }
            boolean foundUser = false;
            final List<Player> matchedPlayers = server.matchPlayer(searchTerm);

            if (matchedPlayers.isEmpty()) {
                final String matchText = searchTerm.toLowerCase(Locale.ENGLISH);
                for (User player : ess.getOnlineUsers()) {
                    if (skipHidden && player.isHidden(sender.getPlayer()) && !sender.getPlayer().canSee(player.getBase())) {
                        continue;
                    }
                    final String displayName = FormatUtil.stripFormat(player.getDisplayName()).toLowerCase(Locale.ENGLISH);
                    if (displayName.contains(matchText)) {
                        foundUser = true;
                        updatePlayer(server, sender, player, commandArgs);
                    }
                }
            } else {
                for (Player matchPlayer : matchedPlayers) {
                    final User player = ess.getUser(matchPlayer);
                    if (skipHidden && player.isHidden(sender.getPlayer()) && !sender.getPlayer().canSee(matchPlayer)) {
                        continue;
                    }
                    foundUser = true;
                    updatePlayer(server, sender, player, commandArgs);
                }
            }
            if (!foundUser) {
                throw new PlayerNotFoundException();
            }
        } else {
            final User player = getPlayer(server, sender, searchTerm);
            updatePlayer(server, sender, player, commandArgs);
        }
    }

    /**
     * <p>updatePlayer.</p>
     *
     * @param server a {@link org.bukkit.Server} object.
     * @param sender a {@link com.earth2me.essentials.CommandSource} object.
     * @param user a {@link com.earth2me.essentials.User} object.
     * @param args an array of {@link java.lang.String} objects.
     * @throws com.earth2me.essentials.commands.NotEnoughArgumentsException if any.
     * @throws com.earth2me.essentials.commands.PlayerExemptException if any.
     * @throws com.earth2me.essentials.ChargeException if any.
     * @throws net.ess3.api.MaxMoneyException if any.
     */
    protected abstract void updatePlayer(Server server, CommandSource sender, User user, String[] args) throws NotEnoughArgumentsException, PlayerExemptException, ChargeException, MaxMoneyException;

    /** {@inheritDoc} */
    @Override
    protected List<String> getPlayers(final Server server, final CommandSource interactor) {
        List<String> players = super.getPlayers(server, interactor);
        players.add("**");
        players.add("*");
        return players;
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getPlayers(final Server server, final User interactor) {
        List<String> players = super.getPlayers(server, interactor);
        players.add("**");
        players.add("*");
        return players;
    }
}
