package me.trouper.dupealias.data;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.Objects;

public class ItemsAdderItem {

    public final String namespace;

    public final String id;

    private static final Gson GSON = new Gson();

    public ItemsAdderItem() {
        this.namespace = null;
        this.id = null;
    }

    @SuppressWarnings("unchecked")
    public ItemsAdderItem(ItemStack itemStack) throws IllegalArgumentException {
        if (itemStack == null) {
            throw new IllegalArgumentException("ItemStack provided cannot be null.");
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            throw new IllegalArgumentException("ItemStack has no ItemMeta. It cannot be an ItemsAdder item.");
        }

        String itemMetaJson = itemMeta.getAsString();
        if (itemMetaJson.trim().isEmpty()) {
            throw new IllegalArgumentException("ItemMeta.getAsString() returned null or an empty string. No NBT data found.");
        }

        Map<String, Object> components;
        try {
            components = GSON.fromJson(itemMetaJson, Map.class);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Failed to parse ItemMeta JSON string: " + e.getMessage());
        }

        Object customDataObj = components.get("minecraft:custom_data");
        if (!(customDataObj instanceof Map)) {
            throw new IllegalArgumentException("Missing or invalid 'minecraft:custom_data' component in ItemStack NBT. Expected a JSON object.");
        }

        Map<String, Object> customData = (Map<String, Object>) customDataObj;

        Object itemsAdderObj = customData.get("itemsadder");
        if (!(itemsAdderObj instanceof Map)) {
            throw new IllegalArgumentException("Missing or invalid 'itemsadder' object within 'minecraft:custom_data'. Expected a JSON object.");
        }
        Map<String, Object> itemsAdderData = (Map<String, Object>) itemsAdderObj;

        Object namespaceObj = itemsAdderData.get("namespace");
        Object idObj = itemsAdderData.get("id");

        if (!(namespaceObj instanceof String) || ((String) namespaceObj).trim().isEmpty()) {
            throw new IllegalArgumentException("Missing, invalid, or empty 'namespace' field in ItemsAdder custom data. Expected a non-empty string.");
        }
        this.namespace = (String) namespaceObj;

        if (!(idObj instanceof String) || ((String) idObj).trim().isEmpty()) {
            throw new IllegalArgumentException("Missing, invalid, or empty 'id' field in ItemsAdder custom data. Expected a non-empty string.");
        }
        this.id = (String) idObj;
    }
    @Override
    public String toString() {
        return "ItemsAdderItem{namespace='" + namespace + "', id='" + id + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemsAdderItem that = (ItemsAdderItem) o;
        return Objects.equals(namespace, that.namespace) &&
                Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(namespace, id);
    }
}
