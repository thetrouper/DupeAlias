package me.trouper.dupealias.server;

import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.functions.UniqueCheck;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DupeManager implements DupeContext {

    public boolean isUnique(ItemStack item) {
        return !new UniqueCheck().passes(item);
    }

    public boolean hasIndividualTag(ItemStack input, ItemTag tag) {
        return input.hasItemMeta() && input.getPersistentDataContainer().has(tag.getKey());
    }

    public boolean checkIndividualTag(ItemStack input, ItemTag tag) {
        boolean set = hasIndividualTag(input,tag);
        if (!set) throw new IllegalArgumentException("Tried to check a tag which was not set, this may produce unexpected behavior!");
        return Boolean.TRUE.equals(input.getPersistentDataContainer().get(tag.getKey(), PersistentDataType.BOOLEAN));
    }

    public boolean checkGlobalTag(Material material, ItemTag tag) {
        Set<ItemTag> tags = getConfig().globalMaterials.getOrDefault(material,new HashSet<>());
        return tags.contains(tag);
    }

    public boolean checkEffectiveTag(ItemStack input, ItemTag tag) {
        if (tag == null || input == null) return false;
        if (input.isEmpty()) return false;
        boolean set = hasIndividualTag(input,tag);
        boolean global = getDupe().checkGlobalTag(input.getType(),tag);
        boolean individual = Boolean.TRUE.equals(input.getPersistentDataContainer().get(tag.getKey(), PersistentDataType.BOOLEAN));

        if (set) return individual;
        return global;
    }

    public boolean addGlobalTag(Material material, ItemTag tag) {
        Set<ItemTag> tags = getConfig().globalMaterials.getOrDefault(material,new HashSet<>());
        boolean result = tags.add(tag);
        getConfig().globalMaterials.put(material,tags);
        getConfig().save();
        return result;
    }

    public boolean removeGlobalTag(Material material, ItemTag tag) {
        Set<ItemTag> tags = getConfig().globalMaterials.getOrDefault(material,new HashSet<>());
        boolean result = tags.remove(tag);
        getConfig().globalMaterials.put(material,tags);
        getConfig().save();
        return result;
    }

    public boolean addTag(ItemStack item, ItemTag tag) {
        if (hasIndividualTag(item,tag) && getDupe().checkIndividualTag(item,tag)) return false;
        ItemBuilder builder = ItemBuilder.of(item);
        builder.loreMiniMessage(getConfig().tagLore.get(tag));
        builder.modifyMeta(itemMeta -> {
            itemMeta.getPersistentDataContainer().set(tag.getKey(), PersistentDataType.BOOLEAN,true);
            return itemMeta;
        });

        ItemStack result = builder.buildAndGet();

        item.setItemMeta(result.getItemMeta());
        return true;
    }

    public boolean removeTag(ItemStack item, ItemTag tag) {
        ItemBuilder builder = ItemBuilder.of(item);
        if (hasIndividualTag(item,tag) && !checkIndividualTag(item,tag)) return false;
        try {
            builder.modifyMeta(itemMeta -> {
                itemMeta.getPersistentDataContainer().remove(tag.getKey());
                if (itemMeta.hasLore()) {
                    List<Component> lore = item.lore();
                    if (lore == null) return itemMeta;
                    int lines = lore.size();
                    for (int i = 0; i < lines - 1; i++) {
                        for (Map.Entry<ItemTag, String> entry : getConfig().tagLore.entrySet()) {
                            if (tag.equals(entry.getKey())) continue;
                            String search = entry.getValue();
                            String searchPlain = search.replaceAll("<[^>]+>", "");
                            String componentPlain = PlainTextComponentSerializer.plainText().serialize(lore.get(i));
                            if (componentPlain.equals(searchPlain)) {
                                lore.remove(i);
                                break;
                            }
                        }
                    }
                    itemMeta.lore(lore);
                }
                return itemMeta;
            });
        } catch (IllegalArgumentException ex) {
            return false;
        }
        ItemStack result = builder.buildAndGet();

        item.setItemMeta(result.getItemMeta());
        return true;
    }

    public void setTag(ItemStack item, ItemTag tag, boolean value) {
        ItemBuilder builder = ItemBuilder.of(item);
        if (value) builder.loreMiniMessage(getConfig().tagLore.get(tag));
        builder.modifyMeta(itemMeta -> {
            itemMeta.getPersistentDataContainer().set(tag.getKey(), PersistentDataType.BOOLEAN,value);
            return itemMeta;
        });

        ItemStack result = builder.buildAndGet();

        item.setItemMeta(result.getItemMeta());
    }

    public ItemTag getTag(NamespacedKey key) {
        for (ItemTag value : ItemTag.values()) {
            if (value.getKey().equals(key)) return value;
        }
        throw new IllegalArgumentException("Invalid NameSpacedKey '%s'".formatted(key.value()));
    }

}
