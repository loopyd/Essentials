package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.Commandrepair;
import com.earth2me.essentials.commands.NotEnoughArgumentsException;
import net.ess3.api.IEssentials;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>SignRepair class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class SignRepair extends EssentialsSign {
    /**
     * <p>Constructor for SignRepair.</p>
     */
    public SignRepair() {
        super("Repair");
    }

    /** {@inheritDoc} */
    @Override
    protected boolean onSignCreate(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
        final String repairTarget = sign.getLine(1);
        if (repairTarget.isEmpty()) {
            sign.setLine(1, "Hand");
        } else if (!repairTarget.equalsIgnoreCase("all") && !repairTarget.equalsIgnoreCase("hand")) {
            sign.setLine(1, "§c<hand|all>");
            throw new SignException(tl("invalidSignLine", 2));
        }
        validateTrade(sign, 2, ess);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean onSignInteract(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
        final Trade charge = getTrade(sign, 2, ess);
        charge.isAffordableFor(player);

        Commandrepair command = new Commandrepair();
        command.setEssentials(ess);

        try {
            if (sign.getLine(1).equalsIgnoreCase("hand")) {
                command.repairHand(player);
            } else if (sign.getLine(1).equalsIgnoreCase("all")) {
                command.repairAll(player);
            } else {
                throw new NotEnoughArgumentsException();
            }

        } catch (Exception ex) {
            throw new SignException(ex.getMessage(), ex);
        }

        charge.charge(player);
        Trade.log("Sign", "Repair", "Interact", username, null, username, charge, sign.getBlock().getLocation(), ess);
        return true;
    }
}
