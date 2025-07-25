package me.trouper.dupealias.data;

import me.trouper.alias.data.enums.*;
import me.trouper.alias.utils.misc.MapUtils;
import me.trouper.dupealias.server.ItemTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GlobalRule {

    public enum MatchMode {
        AND, OR, NAND, XOR
    }

    public enum MaterialMatchMode {
        WHITELIST,
        BLACKLIST,
        IGNORE
    }

    public MatchMode matchMode = MatchMode.AND;
    public MaterialMatchMode materialMode = MaterialMatchMode.IGNORE;
    public Set<Material> effectedMaterials = EnumSet.noneOf(Material.class);

    public String nameContainsRegex = "";
    public String loreContainsRegex = "";
    public Set<Integer> legacyModelData = new HashSet<>();
    public Set<ItemFlag> itemFlags = EnumSet.noneOf(ItemFlag.class);
    public Map<ValidEnchantment, Integer> enchantments = new HashMap<>();
    public Map<ValidPotionEffectType, Integer> potionEffects = new HashMap<>();
    public Map<ValidAttribute, Double> attributes = new HashMap<>();
    public Set<ValidTrimPattern> trimPatterns = EnumSet.noneOf(ValidTrimPattern.class);
    public Set<ValidTrimMaterial> trimMaterials = EnumSet.noneOf(ValidTrimMaterial.class);

    public Set<ItemTag> appliedTags = EnumSet.noneOf(ItemTag.class);

    @SuppressWarnings("deprecation")
    public boolean doesMatch(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        switch (materialMode) {
            case WHITELIST -> {
                if (!effectedMaterials.contains(item.getType())) return false;
            }
            case BLACKLIST -> {
                if (effectedMaterials.contains(item.getType())) return false;
            }
            case IGNORE -> {}
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        List<Boolean> results = new ArrayList<>();

        if (!nameContainsRegex.isEmpty()) {
            Component nameComponent = meta.displayName();
            Pattern namePattern = safeCompileRegex(nameContainsRegex);
            String name = nameComponent != null ? LegacyComponentSerializer.legacyAmpersand().serialize(nameComponent) : "";
            results.add(namePattern.matcher(name).find());
        }

        if (!loreContainsRegex.isEmpty() && meta.hasLore()) {
            List<String> lore = meta.lore().stream().map(line-> LegacyComponentSerializer.legacyAmpersand().serialize(line)).toList();
            Pattern lorePattern = safeCompileRegex(loreContainsRegex);
            boolean found = !lore.isEmpty() && lore.stream().anyMatch(line -> lorePattern.matcher(line).find());
            results.add(found);
        }

        if (!enchantments.isEmpty()) {
            Map<Enchantment, Integer> itemEnchants = item.getEnchantments();
            results.add(MapUtils.allValuesMatch(itemEnchants, enchantments.entrySet().stream().collect(Collectors.toMap(
                    entry -> entry.getKey().getCanonical(),
                    Map.Entry::getValue
            ))));
        }

        if (!potionEffects.isEmpty() && meta instanceof PotionMeta potionMeta) {
            Map<PotionEffectType, Integer> itemPotions = potionMeta.getAllEffects().stream()
                    .collect(Collectors.toMap(
                            PotionEffect::getType,
                            PotionEffect::getAmplifier
                    ));
            results.add(MapUtils.allValuesMatch(itemPotions, potionEffects.entrySet().stream().collect(Collectors.toMap(
                    entry -> entry.getKey().getCanonical(),
                    Map.Entry::getValue
            ))));
        }

        if (!attributes.isEmpty() && meta.hasAttributeModifiers() && meta.getAttributeModifiers() != null) {
            Map<Attribute, Double> itemModifiers = meta.getAttributeModifiers().entries().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().getAmount()
                    ));
            results.add(MapUtils.allValuesMatch(itemModifiers, attributes.entrySet().stream().collect(Collectors.toMap(
                    entry -> entry.getKey().getCanonical(),
                    Map.Entry::getValue
            ))));
        }

        if (!legacyModelData.isEmpty()) {
            results.add(meta.hasCustomModelData() && legacyModelData.contains(meta.getCustomModelData()));
        }

        if (!itemFlags.isEmpty()) {
            results.add(meta.getItemFlags().containsAll(itemFlags));
        }

        if (!trimMaterials.isEmpty() && meta instanceof ArmorMeta armorMeta) {
            ArmorTrim actualTrim = armorMeta.hasTrim() ? armorMeta.getTrim() : null;
            results.add(actualTrim != null && trimMaterials.stream().anyMatch(material-> material.getCanonical().equals(actualTrim.getMaterial())));
        }
        
        if (!trimPatterns.isEmpty() && meta instanceof ArmorMeta armorMeta) {
            ArmorTrim actualTrim = armorMeta.hasTrim() ? armorMeta.getTrim() : null;
            results.add(actualTrim != null && trimPatterns.stream().anyMatch(pattern-> pattern.getCanonical().equals(actualTrim.getPattern())));
        }
        
        int trueCount = (int) results.stream().filter(Boolean::booleanValue).count();
        int total = results.size();

        return switch (matchMode) {
            case AND -> trueCount == total;
            case OR -> trueCount > 0;
            case NAND -> trueCount != total;
            case XOR -> trueCount == 1;
        };
    }

    private Pattern safeCompileRegex(String input) {
        try {
            return Pattern.compile(input);
        } catch (Exception e) {
            return Pattern.compile(Pattern.quote(input));
        }
    }


    public int getCriteriaCount() {
        int criteriaCount = 0;
        if (!nameContainsRegex.isEmpty()) criteriaCount++;
        if (!loreContainsRegex.isEmpty()) criteriaCount++;
        if (!enchantments.isEmpty()) criteriaCount++;
        if (!potionEffects.isEmpty()) criteriaCount++;
        if (!attributes.isEmpty()) criteriaCount++;
        if (!itemFlags.isEmpty()) criteriaCount++;
        if (!legacyModelData.isEmpty()) criteriaCount++;
        if (!trimPatterns.isEmpty()) criteriaCount++;
        if (!trimMaterials.isEmpty()) criteriaCount++;
        return criteriaCount;
    }
}
