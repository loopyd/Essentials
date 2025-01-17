package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FloatUtil;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>Commandsetworth class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandsetworth extends EssentialsCommand {
    /**
     * <p>Constructor for Commandsetworth.</p>
     */
    public Commandsetworth() {
        super("setworth");
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }

        ItemStack stack;
        String price;

        if (args.length == 1) {
            stack = user.getBase().getInventory().getItemInHand();
            price = args[0];
        } else {
            stack = ess.getItemDb().get(args[0]);
            price = args[1];
        }

        ess.getWorth().setPrice(ess, stack, FloatUtil.parseDouble(price));
        user.sendMessage(tl("worthSet"));
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (args.length < 2) {
            throw new NotEnoughArgumentsException();
        }

        ItemStack stack = ess.getItemDb().get(args[0]);
        ess.getWorth().setPrice(ess, stack, FloatUtil.parseDouble(args[1]));
        sender.sendMessage(tl("worthSet"));
    }
}
