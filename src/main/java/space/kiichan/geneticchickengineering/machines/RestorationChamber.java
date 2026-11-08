package space.kiichan.geneticchickengineering.machines;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import space.kiichan.geneticchickengineering.GeneticChickengineering;
import space.kiichan.geneticchickengineering.chickens.PocketChicken;
import space.kiichan.geneticchickengineering.items.GCEItems;

public class RestorationChamber extends AContainer  {
    private GeneticChickengineering plugin;
    private final PocketChicken<LivingEntity> pc;
    private int healRate;

    public RestorationChamber(GeneticChickengineering plugin, ItemGroup category, int healRate, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
        this.plugin = plugin;
        this.pc = plugin.pocketChicken;
        this.healRate = healRate;
    }

    @Override
    public ItemStack getProgressBar() {
        // 返回副本：Slimefun 渲染进度条时可能改写 ItemMeta，
        // 直接交出 GCEItems.POCKET_CHICKEN 这个静态常量会污染全局物品模板。
        return GCEItems.POCKET_CHICKEN.clone();
    }

    @Override
    public String getMachineIdentifier() {
        return "GCE_RESTORATION_CHAMBER";
    }

    @Override
    protected MachineRecipe findNextRecipe(BlockMenu inv) {
        ItemStack chick = null;
        ItemStack seeds = null;
        for (int slot : getInputSlots()) {
            ItemStack item = inv.getItemInSlot(slot);

            if (item == null) {
                continue;
            }

            if (this.pc.isPocketChicken(item)) {
                chick = item;
            } else if (item.getType() == Material.WHEAT_SEEDS) {
                seeds = item;
            }
        }

        if (chick == null || seeds == null) {
            return null;
        }

        double health = this.pc.getHealth(chick);
        int seedAmount = seeds.getAmount();
        int toConsume = 0;
        while (seedAmount > 0 && health < 4d) {
            seedAmount--;
            toConsume++;
            health = health + 0.25;
        }
        if (toConsume == 0) {
            return null;
        }
        ItemStack recipeSeeds = seeds.clone();
        recipeSeeds.setAmount(toConsume);
        ItemStack recipeChick = chick.clone();
        this.plugin.heal(recipeChick, toConsume*0.25);
        MachineRecipe recipe = new MachineRecipe(this.healRate*toConsume, new ItemStack[] {recipeSeeds, chick.clone()}, new ItemStack[] {recipeChick});
        if (!InvUtils.fitAll(inv.toInventory(), recipe.getOutput(), getOutputSlots())) {
            return null;
        }
        ItemUtils.consumeItem(chick, false);
        ItemUtils.consumeItem(seeds, toConsume, false);
        return recipe;
    }
}
