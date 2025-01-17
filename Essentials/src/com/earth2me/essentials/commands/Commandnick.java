package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import net.ess3.api.events.NickChangeEvent;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commandnick class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandnick extends EssentialsLoopCommand {
    /**
     * <p>Constructor for Commandnick.</p>
     */
    public Commandnick() {
        super("nick");
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }
        if (!ess.getSettings().changeDisplayName()) {
            throw new Exception(tl("nickDisplayName"));
        }

        if (args.length > 1 && user.isAuthorized("essentials.nick.others")) {
            final String[] nickname = formatNickname(user, args[1]).split(" ");
            loopOfflinePlayers(server, user.getSource(), false, true, args[0], nickname);
            user.sendMessage(tl("nickChanged"));
        } else {
            final String[] nickname = formatNickname(user, args[0]).split(" ");
            updatePlayer(server, user.getSource(), user, nickname);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (args.length < 2) {
            throw new NotEnoughArgumentsException();
        }
        if (!ess.getSettings().changeDisplayName()) {
            throw new Exception(tl("nickDisplayName"));
        }
        final String[] nickname = formatNickname(null, args[1]).split(" ");
        loopOfflinePlayers(server, sender, false, true, args[0], nickname);
        sender.sendMessage(tl("nickChanged"));
    }

    /** {@inheritDoc} */
    @Override
    protected void updatePlayer(final Server server, final CommandSource sender, final User target, final String[] args) throws NotEnoughArgumentsException {
        final String nick = args[0];
        if ("off".equalsIgnoreCase(nick)) {
            setNickname(server, sender, target, null);
            target.sendMessage(tl("nickNoMore"));
        } else if (target.getName().equalsIgnoreCase(nick)) {
            String oldName = target.getDisplayName();
            setNickname(server, sender, target, nick);
            if (!target.getDisplayName().equalsIgnoreCase(oldName)) {
                target.sendMessage(tl("nickNoMore"));
            }
            target.sendMessage(tl("nickSet", target.getDisplayName()));
        } else if (nickInUse(server, target, nick)) {
            throw new NotEnoughArgumentsException(tl("nickInUse"));
        } else {
            setNickname(server, sender, target, nick);
            target.sendMessage(tl("nickSet", target.getDisplayName()));
        }
    }

    private String formatNickname(final User user, final String nick) throws Exception {
        String newNick = user == null ? FormatUtil.replaceFormat(nick) : FormatUtil.formatString(user, "essentials.nick", nick);
        if (!newNick.matches("^[a-zA-Z_0-9\u00a7]+$") && user != null && !user.isAuthorized("essentials.nick.allowunsafe")) {
            throw new Exception(tl("nickNamesAlpha"));
        } else if (getNickLength(newNick) > ess.getSettings().getMaxNickLength()) {
            throw new Exception(tl("nickTooLong"));
        } else if (FormatUtil.stripFormat(newNick).length() < 1) {
            throw new Exception(tl("nickNamesAlpha"));
        } else if (user != null && (user.isAuthorized("essentials.nick.changecolors") && !user.isAuthorized("essentials.nick.changecolors.bypass")) && !FormatUtil.stripFormat(newNick).equals(user.getName())) {
            throw new Exception(tl("nickNamesOnlyColorChanges"));
        }
        return newNick;
    }

    private int getNickLength(final String nick) {
        return ess.getSettings().ignoreColorsInMaxLength() ? ChatColor.stripColor(nick).length() : nick.length();
    }

    private boolean nickInUse(final Server server, final User target, String nick) {
        final String lowerNick = FormatUtil.stripFormat(nick.toLowerCase(Locale.ENGLISH));
        for (final Player onlinePlayer : ess.getOnlinePlayers()) {
            if (target.getBase().getName().equals(onlinePlayer.getName())) {
                continue;
            }
            final String matchNick = FormatUtil.stripFormat(onlinePlayer.getDisplayName().replace(ess.getSettings().getNicknamePrefix(), ""));
            if (lowerNick.equals(matchNick.toLowerCase(Locale.ENGLISH)) || lowerNick.equals(onlinePlayer.getName().toLowerCase(Locale.ENGLISH))) {
                return true;
            }
        }
        return ess.getUser(lowerNick) != null && ess.getUser(lowerNick) != target;
    }

    private void setNickname(final Server server, final CommandSource sender, final User target, final String nickname) {
        final User controller = sender.isPlayer() ? ess.getUser(sender.getPlayer()) : null;
        final NickChangeEvent nickEvent = new NickChangeEvent(controller, target, nickname);
        server.getPluginManager().callEvent(nickEvent);
        if (!nickEvent.isCancelled()) {
            target.setNickname(nickname);
            target.setDisplayNick();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
        if (args.length == 1 && user.isAuthorized("essentials.nick.others")) {
            return getPlayers(server, user);
        } else {
            return Collections.emptyList();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
        if (args.length == 1) {
            return getPlayers(server, sender);
        } else {
            return Collections.emptyList();
        }
    }
}
