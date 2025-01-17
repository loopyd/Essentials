package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Kit;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.NoChargeException;
import net.ess3.api.IEssentials;

import java.util.Locale;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>SignKit class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class SignKit extends EssentialsSign {
    /**
     * <p>Constructor for SignKit.</p>
     */
    public SignKit() {
        super("Kit");
    }

    /** {@inheritDoc} */
    @Override
    protected boolean onSignCreate(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
        validateTrade(sign, 3, ess);

        final String kitName = sign.getLine(1).toLowerCase(Locale.ENGLISH).trim();

        if (kitName.isEmpty()) {
            sign.setLine(1, "§dKit name!");
            return false;
        } else {
            try {
                ess.getKits().getKit(kitName);
            } catch (Exception ex) {
                throw new SignException(ex.getMessage(), ex);
            }
            final String group = sign.getLine(2);
            if ("Everyone".equalsIgnoreCase(group) || "Everybody".equalsIgnoreCase(group)) {
                sign.setLine(2, "§2Everyone");
            }
            return true;
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean onSignInteract(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
        final String kitName = sign.getLine(1).toLowerCase(Locale.ENGLISH).trim();
        final String group = sign.getLine(2).trim();
        if ((!group.isEmpty() && ("§2Everyone".equals(group) || player.inGroup(group))) || (group.isEmpty() && (player.isAuthorized("essentials.kits." + kitName)))) {
            final Trade charge = getTrade(sign, 3, ess);
            charge.isAffordableFor(player);
            try {
                final Kit kit = new Kit(kitName, ess);
                kit.checkDelay(player);
                kit.setTime(player);
                kit.expandItems(player);

                charge.charge(player);
                Trade.log("Sign", "Kit", "Interact", username, null, username, charge, sign.getBlock().getLocation(), ess);
            } catch (NoChargeException ex) {
                return false;
            } catch (Exception ex) {
                throw new SignException(ex.getMessage(), ex);
            }
            return true;
        } else {
            if (group.isEmpty()) {
                throw new SignException(tl("noKitPermission", "essentials.kits." + kitName));
            } else {
                throw new SignException(tl("noKitGroup", group));
            }
        }
    }
}
