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
        SlimefunItemStack fakeicon = new SlimefunItemStack("GCE_"+itemIDType+"_CHICKEN_ICON", ChickenTypes.getResource(typing));
        // Since these will be "Pocket Chickens", they will spawn chickens when cheated into a player's inventory
        // We set the DNA on the icon so that it will spawn a chicken of the correct type
        ItemMeta meta = fakeicon.getItemMeta();
        meta.getPersistentDataContainer().set(dnakey, PersistentDataType.INTEGER_ARRAY, dna.getState());
        fakeicon.setItemMeta(meta);

        // Make the fake chicken variant and return it
        PocketChicken<LivingEntity> newpc = new PocketChicken<LivingEntity>(this.plugin, category, fakeicon, this.mutationRate, this.maxMutation, this.displayResources, this.adapterkey, this.dnakey, rt, 
            new ItemStack[]{
                null, null, null,
                null, fakechicken, null,
                null, null, null
            }
        );
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
            if (e.getItem().getType()!=Material.PLAYER_HEAD) {
                ItemStack real = new ItemStack(e.getItem().getType(), e.getItem().getAmount());
                e.getItem().setItemMeta(real.getItemMeta());
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
        boolean hasLoreItem = itemMeta.hasLore();
        boolean hasLoreSfItem = sfItemMeta.hasLore();

        if(hasLoreItem && hasLoreSfItem && SlimefunUtils.equalsLore(itemMeta.getLore(), sfItemMeta.getLore())) {
            return true;
        }
        return !hasLoreItem && !hasLoreSfItem;
    }
}
