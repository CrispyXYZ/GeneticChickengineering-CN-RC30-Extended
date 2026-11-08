package space.kiichan.geneticchickengineering.chickens;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.DistinctiveItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import space.kiichan.geneticchickengineering.GeneticChickengineering;
import space.kiichan.geneticchickengineering.adapter.AnimalsAdapter;
import space.kiichan.geneticchickengineering.genetics.DNA;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class PocketChicken<T extends LivingEntity> extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable, DistinctiveItem {

    private final AnimalsAdapter<Chicken> adapter;
    private final NamespacedKey adapterkey;
    private final NamespacedKey dnakey;
    public GeneticChickengineering plugin;
    private int mutationRate;
    private int maxMutation;
    private boolean displayResources;
    /**
     * 64 种图鉴鸡的基底材质。null表示沿用「与资源同材质」的传统做法
     * （此时图标与原版方块完全不可区分）；非 null 时改用该材质做基底，
     * 并在 lore 中标注真实产物，使图标永远不会被误认为原版方块。
     */
    private Material iconBaseMaterial;
    /**
     * 是否为图鉴用的假鸡图标（由 {@link #fakeVariant} 创建）。
     * <p>
     * 这类物品<strong>仅用于合成图鉴展示</strong>，右键不应召唤真鸡。
     * 早期版本靠「材质是否为 PLAYER_HEAD」来区分主物品与图标，这很脆弱：
     * 一旦图标材质被改成 PLAYER_HEAD（见 isolate-guide-icons 隔离模式），
     * 就会错误地走进生鸡分支。此处改为显式标记，与材质彻底解耦。
     */
    private boolean guideIcon;

    public PocketChicken(GeneticChickengineering plugin, ItemGroup category, SlimefunItemStack item, int mutationRate, int maxMutation, boolean displayResources, NamespacedKey dnakey, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
        this.plugin = plugin;
        this.adapter = new AnimalsAdapter<>(Chicken.class, plugin.getLogger());
        this.adapterkey = new NamespacedKey(plugin, "gce_pocket_chicken_adapter");
        this.dnakey = dnakey;
        this.mutationRate = mutationRate;
        this.maxMutation = maxMutation;
        this.displayResources = displayResources;
    }
    public PocketChicken(GeneticChickengineering plugin, ItemGroup category, SlimefunItemStack item, int mutationRate, int maxMutation, boolean displayResources, NamespacedKey adapterkey, NamespacedKey dnakey, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
        this.plugin = plugin;
        this.adapter = new AnimalsAdapter<>(Chicken.class, plugin.getLogger());
        this.adapterkey = adapterkey;
        this.dnakey = dnakey;
        this.mutationRate = mutationRate;
        this.maxMutation = maxMutation;
        this.displayResources = displayResources;
        // 图鉴鸡沿用主物品的隔离设置
        this.iconBaseMaterial = plugin.pocketChicken != null ? plugin.pocketChicken.iconBaseMaterial : null;
    }

    public void setIconBaseMaterial(Material material) {
        this.iconBaseMaterial = material;
    }

    public void setGuideIcon(boolean guideIcon) {
        this.guideIcon = guideIcon;
    }

    public JsonObject babyJson() {
        // Returns the json for a new baby chicken
        JsonObject json = new JsonObject();
        json.addProperty("_type", "CHICKEN");
        json.addProperty("_health", 4.0);
        json.addProperty("_absorption", 0.0);
        json.addProperty("_removeWhenFarAway", false);
        json.addProperty("_customName", (String) null);
        json.addProperty("_customNameVisible", false);
        json.addProperty("_ai", true);
        json.addProperty("_silent", false);
        json.addProperty("_glowing", false);
        json.addProperty("_invulnerable", false);
        json.addProperty("_collidable", true);
        json.addProperty("_gravity", true);
        json.addProperty("_fireTicks", 0);
        json.addProperty("baby", true);
        json.addProperty("_age", -24000);
        json.addProperty("_ageLock", false);
        json.addProperty("_breedable", false);
        json.addProperty("_loveModeTicks", 0);
        JsonObject attributes = new JsonObject();
        json.add("_attributes", attributes);
        JsonObject effects = new JsonObject();
        json.add("_effects", effects);
        JsonArray tags = new JsonArray();
        json.add("_scoreboardTags", tags);
        return json;
    }

    public ItemStack breed(ItemStack chick1, ItemStack chick2) {
        ItemMeta c1m = chick1.getItemMeta();
        ItemMeta c2m = chick2.getItemMeta();
        PersistentDataContainer c1c = c1m.getPersistentDataContainer();
        PersistentDataContainer c2c = c2m.getPersistentDataContainer();
        if (c1c.has(dnakey, PersistentDataType.INTEGER_ARRAY) && c2c.has(dnakey, PersistentDataType.INTEGER_ARRAY)) {
            DNA c1d = new DNA(c1c.get(dnakey, PersistentDataType.INTEGER_ARRAY));
            DNA c2d = new DNA(c2c.get(dnakey, PersistentDataType.INTEGER_ARRAY));
            return this.fromDNA(new DNA(c1d.split(), c2d.split()));
        }
        return null;
    }

    public ItemStack convert(Chicken entity) {
        JsonObject json = adapter.saveData(entity);
        ItemStack item = getItem().clone();
        DNA dna;
        String uuid = entity.getUniqueId().toString();

        if (entity.hasMetadata("gce_pocket_chicken_dna")) {
            dna = new DNA(entity.getMetadata("gce_pocket_chicken_dna").get(0).asString());
            this.plugin.db.delete(uuid);
        } else if (this.plugin.db.has(uuid)) {
            // Checked if the UUID existed first, so null won't be returned
            dna = new DNA(this.plugin.db.getDNAOrNull(uuid));
        } else {
            dna = new DNA(mutationRate, maxMutation);
        }

        if (this.displayResources && json.get("_customNameVisible").getAsBoolean() && dna.isKnown()) {
            String name;
            if (!json.get("_customName").isJsonNull()) {
                name = json.get("_customName").getAsString();
            } else {
                name = "";
            }
            name = name.replace(" ("+ChickenTypes.getName(dna.getTyping())+")","")
                       .replace("("+ChickenTypes.getName(dna.getTyping())+")","");
            if (name.isEmpty()) {
                json.addProperty("_customName", (String) null);
                json.addProperty("_customNameVisible", false);
            } else {
                json.addProperty("_customName", name);
            }
        }

        this.setLore(item, json, dna);
        return item;
    }

    public void fakeVariant(int typing, String name, ItemGroup category, RecipeType rt) {
        // Returns a chicken variant of the typing
        // Just used for adding the variants to the guide

        // Make a Pocket Chicken for the "recipe" 
        ItemStack fakechicken = getItem().clone();
        DNA dna = new DNA(typing);
        String chickType = ChickenTypes.getName(typing);
        this.setLore(fakechicken, null, dna);

        // Use the chicken's resource as the icon
        String itemIDType = chickType.replace(" ","_").toUpperCase();
        ItemStack resource = ChickenTypes.getResource(typing);
        ItemStack iconBase;
        if (this.iconBaseMaterial != null) {
            /* 隔离模式：基底换成玩家头颅，并在lore 中写明真实产物。
             * 这样图标永远不会被误当成原版方块，也就不会与原版物品混淆。
             */
            iconBase = new ItemStack(this.iconBaseMaterial);
        } else {
            iconBase = resource;
        }
        SlimefunItemStack fakeicon = new SlimefunItemStack("GCE_"+itemIDType+"_CHICKEN_ICON", iconBase);
        // Since these will be "Pocket Chickens", they will spawn chickens when cheated into a player's inventory
        // We set the DNA on the icon so that it will spawn a chicken of the correct type
        ItemMeta meta = fakeicon.getItemMeta();
        meta.getPersistentDataContainer().set(dnakey, PersistentDataType.INTEGER_ARRAY, dna.getState());
        if (this.iconBaseMaterial != null) {
            // 标注这是图鉴图标而非真正的资源，避免玩家误以为能直接放置
            meta.setDisplayName("§b" + chickType + "鸡 §7(基因工程产品)");
            List<String> iconLore = new LinkedList<String>();
            // 实际产物直接取 typemap 中的中文名（getName 返回条目 [0]，已含原版材料 / 蛋的中文）
            iconLore.add(ChatColor.GRAY + "实际产物: " + ChatColor.RESET + chickType);
            meta.setLore(iconLore);
        }
        fakeicon.setItemMeta(meta);

        // Make the fake chicken variant and return it
        PocketChicken<LivingEntity> newpc = new PocketChicken<LivingEntity>(this.plugin, category, fakeicon, this.mutationRate, this.maxMutation, this.displayResources, this.adapterkey, this.dnakey, rt,
            new ItemStack[]{
                null, null, null,
                null, fakechicken, null,
                null, null, null
            }
        );
        // 显式标记为图鉴图标：右键只取消事件，不召唤真鸡
        newpc.setGuideIcon(true);
        newpc.register(this.plugin);
    }

    public ItemStack fromDNA(DNA dna) {
        /* Reverse the adapter's saveData function
         * to create fresh baby chicken data
         */
        JsonObject json = this.babyJson();

        ItemStack item = getItem().clone();
        this.setLore(item, json, dna);
        return item;
    }

    public DNA getDNA(ItemStack chick) {
        PersistentDataContainer container = chick.getItemMeta().getPersistentDataContainer();
        DNA dna = new DNA(container.get(dnakey, PersistentDataType.INTEGER_ARRAY));
        return dna;
    }

    public int getDNAStrength(ItemStack chick) {
        // Returns a number which reflects the number of homozygous dominant
        // alleles in a chicken. This is used to give a boosted rate to resource
        // production from chickens which are "pure"
        DNA dna = this.getDNA(chick);
        int[] state = dna.getState();
        int str = 6 - dna.getTier();
        for (int i=0; i<6; i++) {
            if (state[i] == 1) {
                str--;
            }
        }
        return str;
    }

    public double getHealth(ItemStack chick) {
        if (chick == null) {
            return 0d;
        }
        PersistentDataContainer container = chick.getItemMeta().getPersistentDataContainer();
        JsonObject json = container.get(adapterkey, (PersistentDataType<String, JsonObject>) adapter);
        if (json != null) {
            return json.get("_health").getAsDouble();
        }
        return 0d;
    }

    @Override
    public ItemUseHandler getItemHandler() {
        return e -> {
            if (this.guideIcon) {
                /* 图鉴用的假鸡图标：仅用于合成图鉴展示，右键不应召唤真鸡。
                 *
                 * 这里必须取消事件：否则原版的放置/使用流程会继续执行，
                 * 把图标当成真正的方块或物品丢进世界里。
                 *
                 * 同时绝不能改写 e.getItem() 的 ItemMeta：手持物品往往与背包/机器
                 * 里的缓存栈共享引用，就地 setItemMeta 会把 PDC 弄脏。
                 *
                 * 注意：这里用显式的 guideIcon 标记判断，而不是「材质是否为
                 * PLAYER_HEAD」——隔离模式下图标材质恰好也是 PLAYER_HEAD，
                 * 用材质判断会导致图标错误地走进下面的生鸡分支。
                 */
                e.cancel();
                return;
            }
            if (e.getItem().getType()!=Material.PLAYER_HEAD) {
                // 兜底：非头颅材质的异常物品，同样只取消事件
                e.cancel();
                return;
            }
            e.cancel();

            Optional<Block> block = e.getClickedBlock();

            if (block.isPresent()) {
                Block b = block.get();
                Location l = b.getRelative(e.getClickedFace()).getLocation();
                Chicken entity = b.getWorld().spawn(l.toCenterLocation(), Chicken.class);

                PersistentDataContainer container = e.getItem().getItemMeta().getPersistentDataContainer();
                JsonObject json = container.get(adapterkey, (PersistentDataType<String, JsonObject>) adapter);
                int[] dnaState = container.get(dnakey, PersistentDataType.INTEGER_ARRAY);
                DNA dna;
                if (dnaState != null) {
                    dna = new DNA(dnaState);
                } else {
                    dna = new DNA(mutationRate, maxMutation);
                }

                String dss = dna.getStateString();
                entity.setMetadata("gce_pocket_chicken_dna", new FixedMetadataValue(plugin, dss));
                this.plugin.db.insert(entity.getUniqueId().toString(), dss);

                if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    ItemUtils.consumeItem(e.getItem(), false);
                }
                String name = "("+ChickenTypes.getName(dna.getTyping())+")";
                if (json != null) {
                    adapter.apply(entity, json);
                    if (this.displayResources && dna.isKnown()) {
                        if (!json.get("_customName").isJsonNull()) {
                            name = json.get("_customName").getAsString() + " " + name;
                        }
                        json.addProperty("_customNameVisible", true);
                        json.addProperty("_customName",name);
                    }
                } else if (this.displayResources && dna.isKnown()) {
                    entity.setCustomName(name);
                    entity.setCustomNameVisible(true);
                }
            }
        };
    }

    private List<String> getLore(JsonObject json, DNA dna) {
        List<String> lore = new LinkedList<>();
        if (json != null) {
            lore = adapter.getLore(json);
            if (this.plugin.painEnabled()) {
                double health = json.get("_health").getAsDouble();
                String status = ChatColor.GOLD + "状态: ";
                if (health > 2.0) {
                    status = status + ChatColor.GREEN + "健康";
                } else if (health <= 0.50) {
                    status = status + ChatColor.RED + "奄奄一息";
                } else {
                    status = status + ChatColor.YELLOW + "疲惫";
                }
                lore.add(status);
            }
        }
        if (dna.isKnown()) {
            String chicktype = ChickenTypes.getName(dna.getTyping());
            lore.add(ChatColor.GOLD + "基因: " + ChatColor.RESET + dna.toString());
            lore.add(ChatColor.GOLD + "类型: " + ChatColor.RESET + chicktype + "鸡");
        }
        return lore;
    }

    public ItemStack getResource(ItemStack chick) {
        DNA dna = this.getDNA(chick);
        return ChickenTypes.getResource(dna.getTyping());
    }

    public int getResourceTier(ItemStack chick) {
        // Returns the number of homozygous recessive genes in the chicken
        // which represents the difficulty of obtaining this chicken
        DNA dna = this.getDNA(chick);
        return dna.getTier();
    }

    public boolean harm(ItemStack chick, double amount) {
        if (chick == null) {
            return false;
        }
        PersistentDataContainer container = chick.getItemMeta().getPersistentDataContainer();
        JsonObject json = container.get(adapterkey, (PersistentDataType<String, JsonObject>) adapter);
        if (json != null) {
            double oldhealth = json.get("_health").getAsDouble();
            double newhealth = Math.max(0d, Math.min(oldhealth - amount, 4d));
            // Adding existing properties overwrites them
            json.addProperty("_health", newhealth);
            this.setLore(chick, json, this.getDNA(chick));
            return true;
        }
        return false;
    }

    public boolean isAdult(ItemStack chick) {
        PersistentDataContainer container = chick.getItemMeta().getPersistentDataContainer();
        JsonObject json = container.get(adapterkey, (PersistentDataType<String, JsonObject>) adapter);
        if (json != null) {
            return !json.get("baby").getAsBoolean();
        }
        return false;
    }

    public boolean isLearned(ItemStack chick) {
        DNA dna = this.getDNA(chick);
        return dna.isKnown();
    }

    public boolean isPocketChicken(ItemStack chick) {
        return chick.getItemMeta().getPersistentDataContainer().has(dnakey, PersistentDataType.INTEGER_ARRAY);
    }

    public ItemStack learnDNA(ItemStack chick) {
        ItemStack item = chick.clone();
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(dnakey, PersistentDataType.INTEGER_ARRAY)) {
            DNA dna = new DNA(container.get(dnakey, PersistentDataType.INTEGER_ARRAY));

            dna.learn();
            JsonObject json = container.get(adapterkey, (PersistentDataType<String, JsonObject>) adapter);
            this.setLore(item, json, dna);
        }

        return item;
    }

    @SuppressWarnings("deprecation")
	public void setLore(ItemStack item, JsonObject json, DNA dna) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(dnakey, PersistentDataType.INTEGER_ARRAY, dna.getState());
        if (json != null) {
            meta.getPersistentDataContainer().set(adapterkey, adapter, json);
        }
        meta.setLore(getLore(json, dna));

        item.setItemMeta(meta);
    }

    @Override
    public boolean canStack(@NotNull ItemMeta sfItemMeta, @NotNull ItemMeta itemMeta) {
        /* 该方法经SlimefunUtils.isItemSimilar 被货运网络/分类过滤/自动合成器调用，
         * 其中一个参数来自「已注册的模板物品」。
         *
         * 注意：模板 GCE_POCKET_CHICKEN 自身并没有 dnakey（dnakey 只在 setLore()
         * 写入实际鸡物品时才产生），所以不能简单地「没有 dna 就拒绝」，
         * 否则货运网络与自动合成器将无法再识别鸡物品。
         *
         * 分两种情况：
         * 1. 双方都带 dna -> 用基因型精确判定，同一基因型才算同一种鸡；
         * 2. 任一方没有 dna -> 退回原有的 lore 比较。
         *
         * 相比旧实现，唯一的行为变化是堵住了最后的兜底分支：
         * 旧代码在「双方都没有lore」时返回 true，这会让没有 lore 的原版物品
         * 与本物品被判定为可合并。由于 64 种鸡图标的材质就是原版方块/物品，
         * 背包整理类插件据此合并就会把 PDC 沾到原版物品上。
         */
        int[] sfDna = sfItemMeta.getPersistentDataContainer().get(dnakey, PersistentDataType.INTEGER_ARRAY);
        int[] itemDna = itemMeta.getPersistentDataContainer().get(dnakey, PersistentDataType.INTEGER_ARRAY);
        if (sfDna != null && itemDna != null) {
            return Arrays.equals(sfDna, itemDna);
        }

        boolean hasLoreItem = itemMeta.hasLore();
        boolean hasLoreSfItem = sfItemMeta.hasLore();
        if (hasLoreItem && hasLoreSfItem) {
            return SlimefunUtils.equalsLore(itemMeta.getLore(), sfItemMeta.getLore());
        }
        return false;
    }
}
