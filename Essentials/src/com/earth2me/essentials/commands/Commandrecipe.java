package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.VersionUtil;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.*;

import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.earth2me.essentials.I18n.tl;

import net.ess3.nms.refl.ReflUtil;


/**
 * <p>Commandrecipe class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class Commandrecipe extends EssentialsCommand {

    private static final Material FIREWORK_ROCKET = EnumUtil.getMaterial("FIREWORK_ROCKET", "FIREWORK");
    private static final Material FIREWORK_STAR = EnumUtil.getMaterial("FIREWORK_STAR", "FIREWORK_CHARGE");
    private static final Material GUNPOWDER = EnumUtil.getMaterial("GUNPOWDER", "SULPHUR");

    /**
     * <p>Constructor for Commandrecipe.</p>
     */
    public Commandrecipe() {
        super("recipe");
    }
    
    private void disableCommandForVersion1_12() throws Exception {
        VersionUtil.BukkitVersion version = VersionUtil.getServerBukkitVersion();
        if (version.isHigherThanOrEqualTo(VersionUtil.v1_12_0_R01)
            && !ess.getSettings().isForceEnableRecipe()) {
            throw new Exception("Please use the recipe book in your inventory.");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        disableCommandForVersion1_12();
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }

        final ItemStack itemType = ess.getItemDb().get(args[0]);
        int recipeNo = 0;

        if (args.length > 1) {
            if (NumberUtil.isInt(args[1])) {
                recipeNo = Integer.parseInt(args[1]) - 1;
            } else {
                throw new Exception(tl("invalidNumber"));
            }
        }

        final List<Recipe> recipesOfType = ess.getServer().getRecipesFor(itemType);
        if (recipesOfType.size() < 1) {
            throw new Exception(tl("recipeNone", getMaterialName(itemType)));
        }

        if (recipeNo < 0 || recipeNo >= recipesOfType.size()) {
            throw new Exception(tl("recipeBadIndex"));
        }

        final Recipe selectedRecipe = recipesOfType.get(recipeNo);
        sender.sendMessage(tl("recipe", getMaterialName(itemType), recipeNo + 1, recipesOfType.size()));

        if (selectedRecipe instanceof FurnaceRecipe) {
            furnaceRecipe(sender, (FurnaceRecipe) selectedRecipe);
        } else if (selectedRecipe instanceof ShapedRecipe) {
            shapedRecipe(sender, (ShapedRecipe) selectedRecipe, sender.isPlayer());
        } else if (selectedRecipe instanceof ShapelessRecipe) {
            if (recipesOfType.size() == 1 && (itemType.getType() == FIREWORK_ROCKET)) {
                ShapelessRecipe shapelessRecipe = new ShapelessRecipe(itemType);
                shapelessRecipe.addIngredient(GUNPOWDER);
                shapelessRecipe.addIngredient(Material.PAPER);
                shapelessRecipe.addIngredient(FIREWORK_STAR);
                shapelessRecipe(sender, shapelessRecipe, sender.isPlayer());
            } else {
                shapelessRecipe(sender, (ShapelessRecipe) selectedRecipe, sender.isPlayer());
            }
        }

        if (recipesOfType.size() > 1 && args.length == 1) {
            sender.sendMessage(tl("recipeMore", commandLabel, args[0], getMaterialName(itemType)));
        }
    }

    /**
     * <p>furnaceRecipe.</p>
     *
     * @param sender a {@link com.earth2me.essentials.CommandSource} object.
     * @param recipe a {@link org.bukkit.inventory.FurnaceRecipe} object.
     */
    public void furnaceRecipe(final CommandSource sender, final FurnaceRecipe recipe) {
        sender.sendMessage(tl("recipeFurnace", getMaterialName(recipe.getInput())));
    }

    /**
     * <p>shapedRecipe.</p>
     *
     * @param sender a {@link com.earth2me.essentials.CommandSource} object.
     * @param recipe a {@link org.bukkit.inventory.ShapedRecipe} object.
     * @param showWindow a boolean.
     */
    public void shapedRecipe(final CommandSource sender, final ShapedRecipe recipe, final boolean showWindow) {
        final Map<Character, ItemStack> recipeMap = recipe.getIngredientMap();

        if (showWindow) {
            final User user = ess.getUser(sender.getPlayer());
            user.getBase().closeInventory();
            user.setRecipeSee(true);
            final InventoryView view = user.getBase().openWorkbench(null, true);
            final String[] recipeShape = recipe.getShape();
            final Map<Character, ItemStack> ingredientMap = recipe.getIngredientMap();
            for (int j = 0; j < recipeShape.length; j++) {
                for (int k = 0; k < recipeShape[j].length(); k++) {
                    final ItemStack item = ingredientMap.get(recipeShape[j].toCharArray()[k]);
                    if (item == null) {
                        continue;
                    }
                    if (item.getDurability() == Short.MAX_VALUE) {
                        item.setDurability((short) 0);
                    }
                    view.getTopInventory().setItem(j * 3 + k + 1, item);
                }
            }
        } else {
            final HashMap<Material, String> colorMap = new HashMap<>();
            int i = 1;
            for (Character c : "abcdefghi".toCharArray()) {
                ItemStack item = recipeMap.get(c);
                if (!colorMap.containsKey(item == null ? null : item.getType())) {
                    colorMap.put(item == null ? null : item.getType(), String.valueOf(i++));
                }
            }
            final Material[][] materials = new Material[3][3];
            for (int j = 0; j < recipe.getShape().length; j++) {
                for (int k = 0; k < recipe.getShape()[j].length(); k++) {
                    ItemStack item = recipe.getIngredientMap().get(recipe.getShape()[j].toCharArray()[k]);
                    materials[j][k] = item == null ? null : item.getType();
                }
            }
            sender.sendMessage(tl("recipeGrid", colorMap.get(materials[0][0]), colorMap.get(materials[0][1]), colorMap.get(materials[0][2])));
            sender.sendMessage(tl("recipeGrid", colorMap.get(materials[1][0]), colorMap.get(materials[1][1]), colorMap.get(materials[1][2])));
            sender.sendMessage(tl("recipeGrid", colorMap.get(materials[2][0]), colorMap.get(materials[2][1]), colorMap.get(materials[2][2])));

            StringBuilder s = new StringBuilder();
            for (Material items : colorMap.keySet().toArray(new Material[colorMap.size()])) {
                s.append(tl("recipeGridItem", colorMap.get(items), getMaterialName(items)));
            }
            sender.sendMessage(tl("recipeWhere", s.toString()));
        }
    }

    /**
     * <p>shapelessRecipe.</p>
     *
     * @param sender a {@link com.earth2me.essentials.CommandSource} object.
     * @param recipe a {@link org.bukkit.inventory.ShapelessRecipe} object.
     * @param showWindow a boolean.
     */
    public void shapelessRecipe(final CommandSource sender, final ShapelessRecipe recipe, final boolean showWindow) {
        final List<ItemStack> ingredients = recipe.getIngredientList();
        if (showWindow) {
            final User user = ess.getUser(sender.getPlayer());
            user.setRecipeSee(true);
            final InventoryView view = user.getBase().openWorkbench(null, true);
            for (int i = 0; i < ingredients.size(); i++) {
                final ItemStack item = ingredients.get(i);
                if (item.getDurability() == Short.MAX_VALUE) {
                    item.setDurability((short) 0);
                }
                view.setItem(i + 1, item);
            }

        } else {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < ingredients.size(); i++) {
                s.append(getMaterialName(ingredients.get(i)));
                if (i != ingredients.size() - 1) {
                    s.append(",");
                }
                s.append(" ");
            }
            sender.sendMessage(tl("recipeShapeless", s.toString()));
        }
    }

    /**
     * <p>getMaterialName.</p>
     *
     * @param stack a {@link org.bukkit.inventory.ItemStack} object.
     * @return a {@link java.lang.String} object.
     */
    public String getMaterialName(final ItemStack stack) {
        if (stack == null) {
            return tl("recipeNothing");
        }
        return getMaterialName(stack.getType());
    }

    /**
     * <p>getMaterialName.</p>
     *
     * @param type a {@link org.bukkit.Material} object.
     * @return a {@link java.lang.String} object.
     */
    public String getMaterialName(final Material type) {
        if (type == null) {
            return tl("recipeNothing");
        }
        return type.toString().replace("_", " ").toLowerCase(Locale.ENGLISH);
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
        if (args.length == 1) {
            return getItems();
        } else {
            return Collections.emptyList();
        }
    }
}
