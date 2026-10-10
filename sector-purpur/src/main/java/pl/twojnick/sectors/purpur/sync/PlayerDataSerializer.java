package pl.twojnick.sectors.purpur.sync;

import com.google.gson.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class PlayerDataSerializer {
    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(ItemStack[].class, new ItemStackArrayAdapter())
        .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
        .registerTypeAdapter(PotionEffect.class, new PotionEffectAdapter())
        .create();

    public static String serialize(SyncedPlayerData data) {
        return gson.toJson(data);
    }

    public static SyncedPlayerData deserialize(String json) {
        return gson.fromJson(json, SyncedPlayerData.class);
    }

    private static class ItemStackArrayAdapter implements JsonSerializer<ItemStack[]>, JsonDeserializer<ItemStack[]> {
        @Override
        public JsonElement serialize(ItemStack[] src, java.lang.reflect.Type type, JsonSerializationContext ctx) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
                dataOutput.writeInt(src.length);
                for (ItemStack item : src) {
                    dataOutput.writeObject(item);
                }
                dataOutput.close();
                return new JsonPrimitive(Base64.getEncoder().encodeToString(outputStream.toByteArray()));
            } catch (Exception e) {
                throw new JsonParseException("Error serializing ItemStack[]", e);
            }
        }

        @Override
        public ItemStack[] deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext ctx) {
            try {
                byte[] data = Base64.getDecoder().decode(json.getAsString());
                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                ItemStack[] items = new ItemStack[dataInput.readInt()];
                for (int i = 0; i < items.length; i++) {
                    items[i] = (ItemStack) dataInput.readObject();
                }
                dataInput.close();
                return items;
            } catch (Exception e) {
                throw new JsonParseException("Error deserializing ItemStack[]", e);
            }
        }
    }

    private static class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {
        @Override
        public JsonElement serialize(ItemStack src, java.lang.reflect.Type type, JsonSerializationContext ctx) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
                dataOutput.writeObject(src);
                dataOutput.close();
                return new JsonPrimitive(Base64.getEncoder().encodeToString(outputStream.toByteArray()));
            } catch (Exception e) {
                throw new JsonParseException("Error serializing ItemStack", e);
            }
        }

        @Override
        public ItemStack deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext ctx) {
            try {
                byte[] data = Base64.getDecoder().decode(json.getAsString());
                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                ItemStack item = (ItemStack) dataInput.readObject();
                dataInput.close();
                return item;
            } catch (Exception e) {
                throw new JsonParseException("Error deserializing ItemStack", e);
            }
        }
    }

    private static class PotionEffectAdapter implements JsonSerializer<PotionEffect>, JsonDeserializer<PotionEffect> {
        @Override
        public JsonElement serialize(PotionEffect src, java.lang.reflect.Type type, JsonSerializationContext ctx) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", src.getType().getName());
            obj.addProperty("duration", src.getDuration());
            obj.addProperty("amplifier", src.getAmplifier());
            obj.addProperty("ambient", src.isAmbient());
            obj.addProperty("particles", src.hasParticles());
            obj.addProperty("icon", src.hasIcon());
            return obj;
        }

        @Override
        public PotionEffect deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext ctx) {
            JsonObject obj = json.getAsJsonObject();
            return new org.bukkit.potion.PotionEffect(
                org.bukkit.Registry.EFFECT.get(org.bukkit.NamespacedKey.minecraft(obj.get("type").getAsString())),
                obj.get("duration").getAsInt(),
                obj.get("amplifier").getAsInt(),
                obj.get("ambient").getAsBoolean(),
                obj.get("particles").getAsBoolean(),
                obj.get("icon").getAsBoolean()
            );
        }
    }
}
