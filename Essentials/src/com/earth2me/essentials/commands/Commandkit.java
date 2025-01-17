package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Kit;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.StringUtil;
import org.bukkit.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commandkit class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandkit extends EssentialsCommand {
    /**
     * <p>Constructor for Commandkit.</p>
     */
    public Commandkit() {
        super("kit");
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        if (args.length < 1) {
            final String kitList = ess.getKits().listKits(ess, user);
            user.sendMessage(kitList.length() > 0 ? tl("kits", kitList) : tl("noKits"));
            throw new NoChargeException();
        } else if (args.length > 1 && user.isAuthorized("essentials.kit.others")) {
            final User userTo = getPlayer(server, user, args, 1);
            final String kitNames = StringUtil.sanitizeString(args[0].toLowerCase(Locale.ENGLISH)).trim();
            giveKits(userTo, user, kitNames);
        } else {
            final String kitNames = StringUtil.sanitizeString(args[0].toLowerCase(Locale.ENGLISH)).trim();
            giveKits(user, user, kitNames);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (args.length < 2) {
            final String kitList = ess.getKits().listKits(ess, null);
            sender.sendMessage(kitList.length() > 0 ? tl("kits", kitList) : tl("noKits"));
            throw new NoChargeException();
        } else {
            final User userTo = getPlayer(server, args, 1, true, false);
            final String[] kits = args[0].toLowerCase(Locale.ENGLISH).split(",");

            for (final String kitName : kits) {
                final Kit kit = new Kit(kitName, ess);
                kit.expandItems(userTo);

                sender.sendMessage(tl("kitGiveTo", kitName, userTo.getDisplayName()));
                userTo.sendMessage(tl("kitReceive", kitName));
            }
        }
    }

    private void giveKits(final User userTo, final User userFrom, final String kitNames) throws Exception {
        if (kitNames.isEmpty()) {
            throw new Exception(tl("kitNotFound"));
        }
        String[] kitList = kitNames.split(",");

        List<Kit> kits = new ArrayList<Kit>();

        for (final String kitName : kitList) {
            if (kitName.isEmpty()) {
                throw new Exception(tl("kitNotFound"));
            }

            Kit kit = new Kit(kitName, ess);
            kit.checkPerms(userFrom);
            kit.checkDelay(userFrom);
            kit.checkAffordable(userFrom);
            kits.add(kit);
        }

        for (final Kit kit : kits) {
            try {

                kit.checkDelay(userFrom);
                kit.checkAffordable(userFrom);
                kit.setTime(userFrom);
                kit.expandItems(userTo);
                kit.chargeUser(userTo);

                if (!userFrom.equals(userTo)) {
                    userFrom.sendMessage(tl("kitGiveTo", kit.getName(), userTo.getDisplayName()));
                }

                userTo.sendMessage(tl("kitReceive", kit.getName()));

            } catch (NoChargeException ex) {
                if (ess.getSettings().isDebug()) {
                    ess.getLogger().log(Level.INFO, "Soft kit error, abort spawning " + kit.getName(), ex);
                }
            } catch (Exception ex) {
                ess.showError(userFrom.getSource(), ex, "\\ kit: " + kit.getName());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            // TODO: Move all of this to its own method
            for (String kitName : ess.getKits().getKits().getKeys(false)) {
                if (!user.isAuthorized("essentials.kits." + kitName)) { // Only check perm, not time or money
                    continue;
                }
                options.add(kitName);
            }
            return options;
        } else if (args.length == 2 && user.isAuthorized("essentials.kit.others")) {
            return getPlayers(server, user);
        } else {
            return Collections.emptyList();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(ess.getKits().getKits().getKeys(false)); // TODO: Move this to its own method
        } else if (args.length == 2) {
            return getPlayers(server, sender);
        } else {
            return Collections.emptyList();
        }
    }
}
