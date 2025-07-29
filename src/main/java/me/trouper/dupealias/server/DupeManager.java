package me.trouper.dupealias.server;

import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.GlobalRule;
import me.trouper.dupealias.server.functions.UniqueCheck;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DupeManager implements DupeContext {

    public boolean isUnique(ItemStack item) {
        if (item == null || item.isEmpty()) return false;
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

    public boolean checkEffectiveTag(ItemStack input, ItemTag tag) {
        if (tag == null || input == null) return false;
        if (input.isEmpty()) return false;

        boolean set = hasIndividualTag(input,tag);
        boolean individual = set && Boolean.TRUE.equals(input.getPersistentDataContainer().get(tag.getKey(), PersistentDataType.BOOLEAN));

        // Check individual tag first
        if (set) return individual;

        // Check global rules
        return checkGlobalRuleTag(input, tag);
    }

    /**
     * Gets all global rules that apply to a given material
     */
    public List<GlobalRule> getApplicableRules(Material material) {
        return getConfig().globalRules.stream()
                .filter(rule -> {
                    return switch (rule.materialMode) {
                        case WHITELIST -> rule.effectedMaterials.contains(material);
                        case BLACKLIST -> !rule.effectedMaterials.contains(material);
                        case IGNORE -> true;
                        default -> false;
                    };
                })
                .toList();
    }


    /**
     * Gets all global rules that apply to a specific item
     */
    public List<GlobalRule> getMatchingRules(ItemStack item) {
        return getConfig().globalRules.stream()
                .filter(rule -> rule.doesMatch(item))
                .toList();
    }

    /**
     * Checks if any global rule applies this tag to the given item
     */
    public boolean checkGlobalRuleTag(ItemStack input, ItemTag tag) {
        getVerbose().send("Checking tag {0} on item {1}",tag,input.getType());
        for (GlobalRule rule : getConfig().globalRules) {
            getVerbose().send("Scanning rule with tags {0}",rule.appliedTags.toString());
            if (rule.appliedTags.contains(tag) && rule.doesMatch(input)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new global rule that applies the specified tag to items matching the criteria
     */
    public GlobalRule createGlobalRule(ItemTag tag) {
        GlobalRule rule = new GlobalRule();
        rule.appliedTags.add(tag);
        getConfig().globalRules.add(rule);
        getConfig().save();
        return rule;
    }

    /**
     * Removes all global rules that apply the specified tag to the specified material
     */
    public boolean removeGlobalRulesForMaterial(Material material, ItemTag tag) {
        boolean removed = false;
        Iterator<GlobalRule> iterator = getConfig().globalRules.iterator();

        while (iterator.hasNext()) {
            GlobalRule rule = iterator.next();
            if (rule.appliedTags.contains(tag) &&
                    (rule.materialMode == GlobalRule.MaterialMatchMode.WHITELIST && rule.effectedMaterials.contains(material))) {
                iterator.remove();
                removed = true;
            }
        }

        if (removed) {
            getConfig().save();
        }
        return removed;
    }

    /**
     * Adds a global rule for a specific material and tag
     */
    public boolean addGlobalRuleForMaterial(Material material, ItemTag tag) {
        for (GlobalRule rule : getConfig().globalRules) {
            if (rule.appliedTags.contains(tag) &&
                    rule.materialMode == GlobalRule.MaterialMatchMode.WHITELIST &&
                    rule.effectedMaterials.contains(material)) {
                return false;
            }
        }

        GlobalRule rule = new GlobalRule();
        rule.materialMode = GlobalRule.MaterialMatchMode.WHITELIST;
        rule.effectedMaterials.add(material);
        rule.appliedTags.add(tag);
        getConfig().globalRules.add(rule);
        getConfig().save();
        return true;
    }

    public boolean addTag(ItemStack item, ItemTag tag) {
        if (hasIndividualTag(item, tag) && getDupe().checkIndividualTag(item, tag)) return false;

        ItemBuilder builder = ItemBuilder.of(item);
        builder.loreMiniMessage(getConfig().trueTagLore.get(tag));
        builder.modifyMeta(itemMeta -> {
            itemMeta.getPersistentDataContainer().set(tag.getKey(), PersistentDataType.BOOLEAN, true);
            return itemMeta;
        });

        ItemStack result = builder.buildAndGet();
        item.setItemMeta(result.getItemMeta());
        return true;
    }

    public boolean removeTag(ItemStack item, ItemTag tag) {
        ItemBuilder builder = ItemBuilder.of(item);

        if (!hasIndividualTag(item, tag)) return false;

        builder.modifyMeta(itemMeta->{
            if (itemMeta.hasLore()) {
                removeTagLore(itemMeta,tag);
            }

            return itemMeta;
        });

        try {
            builder.modifyMeta(itemMeta -> {
                itemMeta.getPersistentDataContainer().remove(tag.getKey());
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

        builder.modifyMeta(itemMeta -> {
            if (itemMeta.hasLore()) {
                removeTagLore(itemMeta,tag);
            }
            return itemMeta;
        });

        if (value && getConfig().trueTagLore.containsKey(tag)) {
            builder.loreMiniMessage(getConfig().trueTagLore.get(tag));
        } else if (!value && getConfig().falseTagLore.containsKey(tag)) {
            builder.loreMiniMessage(getConfig().falseTagLore.get(tag));
        }

        builder.modifyMeta(itemMeta -> {
            itemMeta.getPersistentDataContainer().set(tag.getKey(), PersistentDataType.BOOLEAN, value);
            return itemMeta;
        });

        ItemStack result = builder.buildAndGet();
        item.setItemMeta(result.getItemMeta());
    }

    public void removeTagLore(ItemMeta meta, ItemTag tag) {
        List<Component> lore = meta.lore();
        if (lore != null) {
            List<String> removeLores = new ArrayList<>();
            if (getConfig().trueTagLore.containsKey(tag)) {
                removeLores.add(getConfig().trueTagLore.get(tag));
            }
            if (getConfig().falseTagLore.containsKey(tag)) {
                removeLores.add(getConfig().falseTagLore.get(tag));
            }

            lore.removeIf(component -> {
                String componentPlain = PlainTextComponentSerializer.plainText().serialize(component);
                return removeLores.stream().anyMatch(loreStr ->
                        componentPlain.equals(loreStr.replaceAll("<[^>]+>", ""))
                );
            });

            meta.lore(lore);
        }
    }

    public ItemTag getTag(NamespacedKey key) {
        for (ItemTag value : ItemTag.values()) {
            if (value.getKey().equals(key)) return value;
        }
        throw new IllegalArgumentException("Invalid NameSpacedKey '%s'".formatted(key.value()));
    }

    public int getPermissionValue(Player player, String rootPermission, int fallback, boolean takeHighest) {
        int result = takeHighest ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
            String perm = permInfo.getPermission();

            if (perm.startsWith(rootPermission)) {
                String valueStr = perm.substring(rootPermission.length());
                try {
                    int value = Integer.parseInt(valueStr);
                    if (takeHighest) {
                        if (value > result) {
                            result = value;
                        }
                    } else {
                        if (value < result) {
                            result = value;
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        if (takeHighest) {
            return (result == Integer.MIN_VALUE) ? fallback : result;
        } else {
            return (result == Integer.MAX_VALUE) ? fallback : result;
        }
    }

}