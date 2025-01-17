package com.earth2me.essentials.utils;

import net.ess3.api.IEssentials;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import static com.earth2me.essentials.I18n.tl;

/**
 * <p>NumberUtil class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class NumberUtil {

    private static DecimalFormat twoDPlaces = new DecimalFormat("#,###.##");
    private static DecimalFormat currencyFormat = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
    
    // This field is likely to be modified in com.earth2me.essentials.Settings when loading currency format.
    // This ensures that we can supply a constant formatting.
    private static NumberFormat PRETTY_FORMAT = NumberFormat.getInstance(Locale.US);

    static {
        twoDPlaces.setRoundingMode(RoundingMode.HALF_UP);
        currencyFormat.setRoundingMode(RoundingMode.FLOOR);

        PRETTY_FORMAT.setRoundingMode(RoundingMode.FLOOR);
        PRETTY_FORMAT.setGroupingUsed(true);
        PRETTY_FORMAT.setMinimumFractionDigits(2);
        PRETTY_FORMAT.setMaximumFractionDigits(2);
    }

    // this method should only be called by Essentials
    /**
     * <p>internalSetPrettyFormat.</p>
     *
     * @param prettyFormat a {@link java.text.NumberFormat} object.
     */
    public static void internalSetPrettyFormat(NumberFormat prettyFormat) {
        PRETTY_FORMAT = prettyFormat;
    }

    /**
     * <p>shortCurrency.</p>
     *
     * @param value a {@link java.math.BigDecimal} object.
     * @param ess a {@link net.ess3.api.IEssentials} object.
     * @return a {@link java.lang.String} object.
     */
    public static String shortCurrency(final BigDecimal value, final IEssentials ess) {
        return ess.getSettings().getCurrencySymbol() + formatAsCurrency(value);
    }

    /**
     * <p>formatDouble.</p>
     *
     * @param value a double.
     * @return a {@link java.lang.String} object.
     */
    public static String formatDouble(final double value) {
        return twoDPlaces.format(value);
    }

    /**
     * <p>formatAsCurrency.</p>
     *
     * @param value a {@link java.math.BigDecimal} object.
     * @return a {@link java.lang.String} object.
     */
    public static String formatAsCurrency(final BigDecimal value) {
        String str = currencyFormat.format(value);
        if (str.endsWith(".00")) {
            str = str.substring(0, str.length() - 3);
        }
        return str;
    }

    /**
     * <p>formatAsPrettyCurrency.</p>
     *
     * @param value a {@link java.math.BigDecimal} object.
     * @return a {@link java.lang.String} object.
     */
    public static String formatAsPrettyCurrency(BigDecimal value) {
        String str = PRETTY_FORMAT.format(value);
        if (str.endsWith(".00")) {
            str = str.substring(0, str.length() - 3);
        }
        return str;
    }

    /**
     * <p>displayCurrency.</p>
     *
     * @param value a {@link java.math.BigDecimal} object.
     * @param ess a {@link net.ess3.api.IEssentials} object.
     * @return a {@link java.lang.String} object.
     */
    public static String displayCurrency(final BigDecimal value, final IEssentials ess) {
        String currency = formatAsPrettyCurrency(value);
        String sign = "";
        if (value.signum() < 0) {
            currency = currency.substring(1);
            sign = "-";
        }
        return sign + tl("currency", ess.getSettings().getCurrencySymbol(), currency);
    }

    /**
     * <p>displayCurrencyExactly.</p>
     *
     * @param value a {@link java.math.BigDecimal} object.
     * @param ess a {@link net.ess3.api.IEssentials} object.
     * @return a {@link java.lang.String} object.
     */
    public static String displayCurrencyExactly(final BigDecimal value, final IEssentials ess) {
        String currency = value.toPlainString();
        String sign = "";
        if (value.signum() < 0) {
            currency = currency.substring(1);
            sign = "-";
        }
        return sign + tl("currency", ess.getSettings().getCurrencySymbol(), currency);
    }

    /**
     * <p>isInt.</p>
     *
     * @param sInt a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isInt(final String sInt) {
        try {
            Integer.parseInt(sInt);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
