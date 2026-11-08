package space.kiichan.geneticchickengineering.chickens;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import space.kiichan.geneticchickengineering.items.GCEItems;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ChickenTypes {

    private ChickenTypes() {};

    private static final Map<Integer, Object[]> typemap = new LinkedHashMap<Integer, Object[]>() {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
            /* Key: int[] of dominance as returned by DNA.getTyping()
             * Value[0]: String of chicken type
             * Value[1]: ItemStack of the resource it gives
             */
            put(63, new Object[]{"羽毛",new ItemStack(Material.FEATHER)});
            put(31, new Object[]{"骨头",new ItemStack(Material.BONE)});
            put(47, new Object[]{"圆石",new ItemStack(Material.COBBLESTONE)});
            put(55, new Object[]{"泥土",new ItemStack(Material.DIRT)});
            put(59, new Object[]{"燧石",new ItemStack(Material.FLINT)});
            put(61, new Object[]{"沙子",new ItemStack(Material.SAND)});
            put(62, new Object[]{"水", GCEItems.WATER_EGG});
            put(15, new Object[]{"煤炭",new ItemStack(Material.COAL)});
            put(23, new Object[]{"线",new ItemStack(Material.STRING)});
            put(27, new Object[]{"皮革",new ItemStack(Material.LEATHER)});
            put(29, new Object[]{"糖",new ItemStack(Material.SUGAR)});
            put(30, new Object[]{"海绵",new ItemStack(Material.SPONGE)});
            put(39, new Object[]{"闪长岩",new ItemStack(Material.DIORITE)});
            put(43, new Object[]{"安山岩",new ItemStack(Material.ANDESITE)});
            put(45, new Object[]{"沙砾",new ItemStack(Material.GRAVEL)});
            put(46, new Object[]{"冰",new ItemStack(Material.ICE)});
            put(51, new Object[]{"花岗岩",new ItemStack(Material.GRANITE)});
            put(53, new Object[]{"黏土",new ItemStack(Material.CLAY)});
            put(54, new Object[]{"橡木原木",new ItemStack(Material.OAK_LOG)});
            put(57, new Object[]{"火药",new ItemStack(Material.GUNPOWDER)});
            put(58, new Object[]{"海带",new ItemStack(Material.KELP)});
            put(60, new Object[]{"黏液球",new ItemStack(Material.SLIME_BALL)});
            put(7, new Object[]{"金锭",new ItemStack(Material.GOLD_INGOT)});
            put(11, new Object[]{"下界岩",new ItemStack(Material.NETHERRACK)});
            put(13, new Object[]{"玻璃",new ItemStack(Material.GLASS)});
            put(14, new Object[]{"青金石",new ItemStack(Material.LAPIS_LAZULI)});
            put(19, new Object[]{"铁锭",new ItemStack(Material.IRON_INGOT)});
            put(21, new Object[]{"铁粉", SlimefunItems.IRON_DUST});
            put(22, new Object[]{"金粉", SlimefunItems.GOLD_DUST});
            put(25, new Object[]{"银粉", SlimefunItems.SILVER_DUST});
            put(26, new Object[]{"锌粉", SlimefunItems.ZINC_DUST});
            put(28, new Object[]{"蛋糕",new ItemStack(Material.CAKE)});
            put(35, new Object[]{"黑曜石",new ItemStack(Material.OBSIDIAN)});
            put(37, new Object[]{"铜粉", SlimefunItems.COPPER_DUST});
            put(38, new Object[]{"镁", SlimefunItems.MAGNESIUM_DUST});
            put(41, new Object[]{"岩浆", GCEItems.LAVA_EGG});
            put(42, new Object[]{"锡粉", SlimefunItems.TIN_DUST});
            put(44, new Object[]{"雪球",new ItemStack(Material.SNOWBALL)});
            put(49, new Object[]{"紅石",new ItemStack(Material.REDSTONE)});
            put(50, new Object[]{"仙人掌",new ItemStack(Material.CACTUS)});
            put(52, new Object[]{"铝粉", SlimefunItems.ALUMINUM_DUST});
            put(56, new Object[]{"铅粉", SlimefunItems.LEAD_DUST});
            put(3, new Object[]{"黑石",new ItemStack(Material.BLACKSTONE)});
            put(5, new Object[]{"灵魂土",new ItemStack(Material.SOUL_SOIL)});
            put(9, new Object[]{"烈焰棒",new ItemStack(Material.BLAZE_ROD)});
            put(17, new Object[]{"恶魂之泪",new ItemStack(Material.GHAST_TEAR)});
            put(33, new Object[]{"硫酸盐", SlimefunItems.SULFATE});
            put(6, new Object[]{"菌光体",new ItemStack(Material.SHROOMLIGHT)});
            put(10, new Object[]{"下界石英",new ItemStack(Material.QUARTZ)});
            put(18, new Object[]{"玄武岩",new ItemStack(Material.BASALT)});
            put(34, new Object[]{"哭泣的黑曜石",new ItemStack(Material.CRYING_OBSIDIAN)});
            put(12, new Object[]{"灵魂沙",new ItemStack(Material.SOUL_SAND)});
            put(20, new Object[]{"末影珍珠",new ItemStack(Material.ENDER_PEARL)});
            put(36, new Object[]{"下界疣",new ItemStack(Material.NETHER_WART)});
            put(24, new Object[]{"幻翼膜",new ItemStack(Material.PHANTOM_MEMBRANE)});
            put(40, new Object[]{"岩浆膏",new ItemStack(Material.MAGMA_CREAM)});
            put(48, new Object[]{"萤石粉",new ItemStack(Material.GLOWSTONE_DUST)});
            put(1, new Object[]{"钻石",new ItemStack(Material.DIAMOND)});
            put(2, new Object[]{"末地石",new ItemStack(Material.END_STONE)});
            put(4, new Object[]{"海晶砂砾",new ItemStack(Material.PRISMARINE_CRYSTALS)});
            put(8, new Object[]{"海晶碎片",new ItemStack(Material.PRISMARINE_SHARD)});
            put(16, new Object[]{"经验",new ItemStack(Material.EXPERIENCE_BOTTLE)});
            put(32, new Object[]{"绿宝石",new ItemStack(Material.EMERALD)});
            put(0, new Object[]{"下界合金锭",new ItemStack(Material.NETHERITE_INGOT)});
        }
    };

    /**
     * 返回 typemap 中的原始条目。
     * <p>
     * <strong>危险</strong>：返回值直接指向内部数组，调用方拿到其中的
     * {@code ItemStack}（即 {@link #getResource(int)} 的底层单例）后若加以改写，
     * 会污染全局资源表。请改用 {@link #getName(int)} / {@link #getResource(int)}。
     * <p>
     * 当前全仓无调用者，保留仅为兼容可能存在的外部调用。
     */
    public static final Object[] get(int typing) {
        return typemap.get(typing);
    }

    public static final String getName(int typing) {
        return (String) typemap.get(typing)[0];
    }

    /**
     * 返回该基因型对应资源的<strong>独立副本</strong>。
     * <p>
     * typemap 里保存的是全局唯一的 ItemStack 实例。若直接返回引用，调用方
     * （SlimefunItemStack 构造器、机器进度条缓存、乃至其他插件改写 ItemMeta）
     * 一旦往里写入 PersistentDataContainer，污染就会永久留在单例上：此后所有
     * 「泥土鸡/圆石鸡」产出的原版方块都会带上 GCE 的 SF 元数据，进而被识别成
     * 「鸡因工程产品」分类里的物品。
     */
    public static final ItemStack getResource(int typing) {
        return ((ItemStack) typemap.get(typing)[1]).clone();
    }

    public static final void registerChickens(Research research, PocketChicken<LivingEntity> pc, ItemGroup category, RecipeType rt) {
        for (int i=typemap.size()-1; i>-1; i--) {
            Object[] attrs = typemap.get(i);
            pc.fakeVariant(i, (String) attrs[0], category, rt);
        }
        pc.plugin.log.info("Registered "+typemap.size()+" chickens");
    }

}
