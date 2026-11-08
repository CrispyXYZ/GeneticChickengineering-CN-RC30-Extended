package space.kiichan.geneticchickengineering.machines;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import space.kiichan.geneticchickengineering.GeneticChickengineering;
import space.kiichan.geneticchickengineering.chickens.PocketChicken;
import space.kiichan.geneticchickengineering.items.GCEItems;

public class GeneticSequencer extends AContainer {
    private GeneticChickengineering plugin;
    private final PocketChicken<LivingEntity> pc;

    public GeneticSequencer(GeneticChickengineering plugin, ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
        this.plugin = plugin;
        this.pc = plugin.pocketChicken;
    }

    @Override
    public ItemStack getProgressBar() {
        // 返回副本：Slimefun 渲染进度条时可能改写 ItemMeta，
        // 直接交出 GCEItems.POCKET_CHICKEN 这个静态常量会污染全局物品模板。
        return GCEItems.POCKET_CHICKEN.clone();
    }

    @Override
    public String getMachineIdentifier() {
        return "GCE_GENETIC_SEQUENCER";
    }

    @Override
    protected MachineRecipe findNextRecipe(BlockMenu inv) {
        for (int slot : getInputSlots()) {
            ItemStack item = inv.getItemInSlot(slot);

            if (item == null) {
                continue;
            }
            ItemStack chick = item.clone();
            // Just in case these got stacked somehow
            chick.setAmount(1);

            if (this.pc.isPocketChicken(chick)) {
                if (this.pc.isLearned(chick)) {
                    continue;
                }
                ItemStack learnedChick = this.pc.learnDNA(chick);
                if (this.plugin.painEnabled()) {
                    if (!this.plugin.survivesPain(learnedChick) && !this.plugin.deathEnabled()) {
                        continue;
                    }
                    this.plugin.possiblyHarm(learnedChick);
                }
                MachineRecipe recipe = new MachineRecipe(30, new ItemStack[] { chick }, new ItemStack[] {learnedChick});
                if (!InvUtils.fitAll(inv.toInventory(), recipe.getOutput(), getOutputSlots())) {
                    continue;
                }
                if (this.plugin.painEnabled()) {
                    if (this.pc.getHealth(learnedChick) == 0d) {
                        ItemUtils.consumeItem(chick, false);
                        inv.getBlock().getWorld().playSound(inv.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 1f, 1f);
                        continue;
                    }
                }
                inv.consumeItem(slot, 1);

                return recipe;
            }
        }

        return null;
    }

}
