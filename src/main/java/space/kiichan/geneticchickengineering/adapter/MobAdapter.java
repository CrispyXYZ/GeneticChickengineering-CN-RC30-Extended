package space.kiichan.geneticchickengineering.adapter;

/* Wholesale stolen from TheBusyBiscuit's
 * MobCapturer.
 *
 * I am dum and couldn't figure out how to set that as a
 * dependency
 */

import java.util.*;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This is a simple Adapter that allows conversion between a {@link LivingEntity} and
 * a {@link JsonObject}.
 *
 * It also requires the implementation of {@link PersistentDataType}.
 *
 * @author TheBusyBiscuit
 *
 */
public interface MobAdapter<T extends LivingEntity> extends PersistentDataType<String, JsonObject> {

    Class<T> getEntityClass();

    default List<String> getLore(JsonObject json) {
        List<String> lore = new LinkedList<>();

        lore.add("");
        lore.add(ChatColor.GRAY + "血量: " + ChatColor.GREEN + json.get("_health").getAsDouble());

        if (!json.get("_customName").isJsonNull()) {
            lore.add(ChatColor.GRAY + "名称: " + ChatColor.RESET + json.get("_customName").getAsString());
        }

        int fireTicks = json.get("_fireTicks").getAsInt();

        if (fireTicks > 0) {
            lore.add(ChatColor.GRAY + "着火: " + ChatColor.RESET + "true");
        }

        return lore;
    }

    default Class<String> getPrimitiveType() {
        return String.class;
    }

    default Class<JsonObject> getComplexType() {
        return JsonObject.class;
    }

    default String toPrimitive(JsonObject json, PersistentDataAdapterContext context) {
        return json.toString();
    }

    default JsonObject fromPrimitive(String primitive, PersistentDataAdapterContext context) {
        return new JsonParser().parse(primitive).getAsJsonObject();
    }

    Logger getLogger();

    default void apply(T entity, JsonObject json) {
        // We need to apply Attributes before the health.
        JsonObject attributes = json.getAsJsonObject("_attributes");

        for (Map.Entry<String, JsonElement> entry : attributes.entrySet()) {
            NamespacedKey namespacedKey = NamespacedKey.fromString(entry.getKey());
            if (namespacedKey == null) {
                namespacedKey = convertOldAttribute(entry.getKey());
            }
            if (namespacedKey == null) {
                getLogger().warning("Could not convert " + entry.getKey() + " to a namespaced key, skipping it.");
                continue;
            }

            Attribute attribute = Registry.ATTRIBUTE.get(namespacedKey);
            if (attribute == null) {
                getLogger().warning("Unrecognizable attribute " + namespacedKey.asString() + ", skipping it.");
                continue;
            }

            AttributeInstance instance = entity.getAttribute(attribute);

            if (instance != null) {
                for (AttributeModifier modifier : new ArrayList<>(instance.getModifiers())) {
                    instance.removeModifier(modifier);
                }

                JsonObject attributeJSON = entry.getValue().getAsJsonObject();
                instance.setBaseValue(attributeJSON.get("base").getAsDouble());

                JsonArray modifiers = attributeJSON.getAsJsonArray("modifiers");

                for (JsonElement modifier : modifiers) {
                    JsonObject obj = modifier.getAsJsonObject();

                    Map<String, Object> mod = new HashMap<>();
                    obj.entrySet().forEach((en) -> mod.put(en.getKey(), en.getValue().toString().replaceAll("\"","")));

                    instance.addModifier(AttributeModifier.deserialize(mod));
                }
            }
        }

        entity.setHealth(json.get("_health").getAsDouble());
        entity.setAbsorptionAmount(json.get("_absorption").getAsDouble());
        entity.setRemoveWhenFarAway(json.get("_removeWhenFarAway").getAsBoolean());

        if (!json.get("_customName").isJsonNull()) {
            entity.setCustomName(json.get("_customName").getAsString());
        }

        entity.setCustomNameVisible(json.get("_customNameVisible").getAsBoolean());
        entity.setAI(json.get("_ai").getAsBoolean());
        entity.setSilent(json.get("_silent").getAsBoolean());
        entity.setGlowing(json.get("_glowing").getAsBoolean());
        entity.setInvulnerable(json.get("_invulnerable").getAsBoolean());
        entity.setCollidable(json.get("_collidable").getAsBoolean());
        entity.setGravity(json.get("_gravity").getAsBoolean());
        entity.setFireTicks(json.get("_fireTicks").getAsInt());

        JsonObject effects = json.getAsJsonObject("_effects");

        for (Map.Entry<String, JsonElement> entry : effects.entrySet()) {
            PotionEffectType type = PotionEffectType.getByName(entry.getKey());

            if (type != null) {
                JsonObject obj = entry.getValue().getAsJsonObject();

                int duration = obj.get("duration").getAsInt();
                int amplifier = obj.get("amplifier").getAsInt();
                boolean ambient = obj.get("ambient").getAsBoolean();
                boolean particles = obj.get("particles").getAsBoolean();
                boolean icon = obj.get("icon").getAsBoolean();

                entity.addPotionEffect(new PotionEffect(type, duration, amplifier, ambient, particles, icon));
            }
        }

        JsonArray tags = json.getAsJsonArray("_scoreboardTags");

        for (JsonElement tag : tags) {
            entity.addScoreboardTag(tag.getAsString());
        }
    }

    private NamespacedKey convertOldAttribute(String oldAttribute) {
        String inner = oldAttribute.substring(oldAttribute.indexOf('[')+1, oldAttribute.lastIndexOf(']'));
        if (inner.contains("]")) {
            getLogger().warning("Conversion failed. Old attribute: " + oldAttribute);
            return null;
        }
        String newAttribute = inner.substring(inner.indexOf('/')+1).trim();
        return NamespacedKey.fromString(newAttribute);
    }

    default JsonObject saveData(T entity) {
        JsonObject json = new JsonObject();

        json.addProperty("_type", entity.getType().toString());
        json.addProperty("_health", entity.getHealth());
        json.addProperty("_absorption", entity.getAbsorptionAmount());
        json.addProperty("_removeWhenFarAway", entity.getRemoveWhenFarAway());
        json.addProperty("_customName", entity.getCustomName());
        json.addProperty("_customNameVisible", entity.isCustomNameVisible());
        json.addProperty("_ai", entity.hasAI());
        json.addProperty("_silent", entity.isSilent());
        json.addProperty("_glowing", entity.isGlowing());
        json.addProperty("_invulnerable", entity.isInvulnerable());
        json.addProperty("_collidable", entity.isCollidable());
        json.addProperty("_gravity", entity.hasGravity());
        json.addProperty("_fireTicks", entity.getFireTicks());

        JsonObject attributes = new JsonObject();

        for (Attribute attribute : Registry.ATTRIBUTE) {
            AttributeInstance instance = entity.getAttribute(attribute);

            if (instance != null) {
                JsonObject obj = new JsonObject();
                obj.addProperty("base", instance.getBaseValue());

                JsonArray modifiers = new JsonArray();

                for (AttributeModifier modifier : instance.getModifiers()) {
                    JsonObject mod = new JsonObject();

                    modifier.serialize().forEach((key, value) -> mod.addProperty(key, value.toString()));

                    modifiers.add(mod);
                }

                obj.add("modifiers", modifiers);
                attributes.add(attribute.getKey().asString(), obj);
            }
        }

        json.add("_attributes", attributes);

        JsonObject effects = new JsonObject();

        for (PotionEffect effect : entity.getActivePotionEffects()) {
            JsonObject obj = new JsonObject();

            obj.addProperty("duration", effect.getDuration());
            obj.addProperty("amplifier", effect.getAmplifier());
            obj.addProperty("ambient", effect.isAmbient());
            obj.addProperty("particles", effect.hasParticles());
            obj.addProperty("icon", effect.hasIcon());

            effects.add(effect.getType().getName(), obj);
        }

        json.add("_effects", effects);

        JsonArray tags = new JsonArray();

        for (String tag : entity.getScoreboardTags()) {
            tags.add(tag);
        }

        json.add("_scoreboardTags", tags);

        return json;
    }

}
