package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import net.ess3.api.IEssentials;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>SignHeal class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class SignHeal extends EssentialsSign {
    /**
     * <p>Constructor for SignHeal.</p>
     */
    public SignHeal() {
        super("Heal");
    }

    /** {@inheritDoc} */
    @Override
    protected boolean onSignCreate(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
        validateTrade(sign, 1, ess);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean onSignInteract(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
        if (player.getBase().getHealth() == 0) {
            throw new SignException(tl("healDead"));
        }
        final Trade charge = getTrade(sign, 1, ess);
        charge.isAffordableFor(player);
        player.getBase().setHealth(20);
        player.getBase().setFoodLevel(20);
        player.getBase().setFireTicks(0);
        player.sendMessage(tl("youAreHealed"));
        charge.charge(player);
        Trade.log("Sign", "Heal", "Interact", username, null, username, charge, sign.getBlock().getLocation(), ess);
        return true;
    }
}
