package space.kiichan.geneticchickengineering.machines;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import space.kiichan.geneticchickengineering.GeneticChickengineering;
import space.kiichan.geneticchickengineering.chickens.PocketChicken;

import java.util.HashMap;
import java.util.Map;

public class ExcitationChamber extends AContainer {
    private GeneticChickengineering plugin;
    private final PocketChicken pc;
    private ItemStack currentResource;
    private static final Map<BlockMenu, ItemStack> resources = new HashMap<>();
    private static final ItemStack blackPane = createSimpleItem(Material.BLACK_STAINED_GLASS_PANE, "&r&0 ");
    private int failRate;
    private int baseTime;

    /**
     * 清理进度条图标缓存。该Map 以 BlockMenu 为键且跨所有鼓舞室实例共享，
     * 插件卸载时必须清空，否则热重载会残留旧的 BlockMenu 引用（内存泄漏），
     * 且其中缓存的 ItemStack 可能把已失效的 SF 元数据带入新一次运行。
     */
    public static void clearResourceCache() {
        resources.clear();
    }

    public ExcitationChamber(GeneticChickengineering plugin, ItemGroup category, SlimefunItemStack item, int failRate, int baseTime, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
        this.plugin = plugin;
        this.pc = plugin.pocketChicken;
        this.currentResource = this.blackPane.clone();
        this.failRate = failRate;
        this.baseTime = baseTime;
    }

    public static ItemStack createSimpleItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name.replace("&", "§"));
        }
        item.setItemMeta(meta);
        return item;
    }

    private Block initBlock(Block b) {
        // Hacky way to get the progress bar to be the resource without sharing
        // the progress bar amongst all the excitation chambers
        BlockMenu inv = StorageCacheUtils.getMenu(b.getLocation());
        this.currentResource = this.resources.getOrDefault(inv, this.blackPane.clone());
        return b;
    }

    @Override
    public ItemStack getProgressBar() {
        /* 返回副本：Slimefun 在渲染进度条时可能改写 ItemMeta，
         * 直接把缓存里的资源图标交出去会把它污染成 SF 物品。
         *
         * 重要：AContainer 的构造器（AContainer.java:79）会在本类字段初始化之前
         * 就回调本方法，此时 this.currentResource 仍是 null。若直接 clone()
         * 会抛 NullPointerException，导致插件在 onEnable 构造本机器时加载失败。
         *
         * 因此做惰性兜底：构造期返回 blackPane 的副本。blackPane 是 static final，
         * 类加载时即已就绪（static 初始化先于任何实例构造），构造期内安全可用；
         * 且与构造器体执行后「this.currentResource = blackPane.clone()」的初始状态
         * 完全一致，不会改变「无资源时进度条显示为黑玻璃」的原有表现。
         */
        ItemStack resource = this.currentResource;
        if (resource == null) {
            resource = this.blackPane;
        }
        return resource != null ? resource.clone() : null;
    }

    @Override
    public String getMachineIdentifier() {
        return "GCE_EXCITATION_CHAMBER";
    }

    @Override
    public int[] getInputSlots() {
        return new int[] { 4 };
    }

    @Override
    public int[] getOutputSlots() {
        return new int[] { 37, 38, 39, 40, 41, 42, 43 };
    }

    protected void constructMenu(BlockMenuPreset preset) {
        for (int i : new int[] { 0, 1, 2, 6, 7, 8, 9, 10, 11, 15, 16, 17, 18, 19, 20, 21, 23, 24, 25, 26 }) {
            preset.addItem(i, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i : new int[] { 3, 5, 12, 13, 14 }) {
            preset.addItem(i, ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i : new int[] { 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 44 }) {
            preset.addItem(i, ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }

        preset.addItem(22, this.blackPane.clone(), ChestMenuUtils.getEmptyClickHandler());

        for (int i : getOutputSlots()) {
            preset.addMenuClickHandler(i, (player, slot, cursor, action) -> {
                return cursor != null && cursor.getType() != null && cursor.getType() != Material.AIR;
            });
        }
    }

    @Override
    protected void tick(Block b) {
        super.tick(this.initBlock(b));
        BlockMenu inv = StorageCacheUtils.getMenu(b.getLocation());
        MachineProcessor<CraftingOperation> processor = getMachineProcessor();
        if (processor.getOperation(b) != null) {
            if (this.findNextRecipe(inv) == null) {
            	processor.endOperation(b);
                inv.replaceExistingItem(22, this.blackPane.clone());
                this.resources.remove(inv);
            }
        } else if (this.resources.containsKey(inv)) {
            this.resources.remove(inv);
        }
    }


	@Override
    protected MachineRecipe findNextRecipe(BlockMenu inv) {
        for (int slot : getInputSlots()) {
            ItemStack chick = inv.getItemInSlot(slot);

            if (chick == null) {
                continue;
            }

            if (this.pc.isPocketChicken(chick)) {
                if (!this.pc.isAdult(chick)) {
                    continue;
                }
                // Set the progress bar to always be the resource, since players
                // can abort the recipe if they know the egg is coming
                ItemStack resourceIcon = this.pc.getResource(chick);

                ItemStack chickResource;
                if (Math.random()*100 < this.failRate) {
                    chickResource = new ItemStack(Material.EGG);
                } else {
                    chickResource = this.pc.getResource(chick);
                }
                /* Speed calculation
                 * All recipes have a base speed of 14 (by default)
                 * All recipes add 1 second/DNA tier
                 * All recipes subtract 2 seconds/DNA strength (dominant pairs)
                 *         | normal    | boosted
                 *  Tier 0 | 2-14 sec  | 1-7 sec
                 *  Tier 1 | 5-15 sec  | 2-7 sec
                 *  Tier 2 | 8-16 sec  | 4-8 sec
                 *  Tier 3 | 11-17 sec | 5-8 sec
                 *  Tier 4 | 14-18 sec | 7-9 sec
                 *  Tier 5 | 17-19 sec | 8-9 sec
                 *  Tier 6 | 20 sec    | 10 sec
                 */
                int speed = (this.baseTime + this.pc.getResourceTier(chick) - 2*this.pc.getDNAStrength(chick)) / getSpeed();
                MachineRecipe recipe = new MachineRecipe(speed, new ItemStack[] { chick }, new ItemStack[] { chickResource });
                if (!InvUtils.fitAll(inv.toInventory(), recipe.getOutput(), getOutputSlots())) {
                    continue;
                }
                
                if (this.plugin.painEnabled()) {
                    if (!this.plugin.survivesPain(chick) && !this.plugin.deathEnabled()) {
                        continue;
                    }
                    this.plugin.possiblyHarm(chick);
                    if (this.pc.getHealth(chick) == 0d) {
                        ItemUtils.consumeItem(chick, false);
                        inv.getBlock().getWorld().playSound(inv.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 1f, 1f);
                        continue;
                    }
                }

                this.resources.put(inv, resourceIcon);
                return recipe;
            }
        }

        return null;
    }

}