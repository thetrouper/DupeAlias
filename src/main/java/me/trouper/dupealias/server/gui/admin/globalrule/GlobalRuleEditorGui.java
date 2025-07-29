package me.trouper.dupealias.server.gui.admin.globalrule;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.GlobalRule;
import me.trouper.dupealias.server.ItemTag;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.AdminPanelManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlobalRuleEditorGui implements DupeContext, CommonItems {

    private final AdminPanelManager manager;
    private final GlobalRule rule;

    public GlobalRuleEditorGui(AdminPanelManager manager, GlobalRule rule) {
        this.manager = manager;
        this.rule = rule;
    }

    public void open(Player player) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#ff6b6b:#ffa726><bold>Rule Editor</bold></gradient>")
                .rows(6)
                .fillBorder(EMPTY(Material.ORANGE_STAINED_GLASS_PANE))

                // Back button top-left
                .item(0, BACK(), (g, e) -> manager.openGlobalRuleList(player))

                // Item Tags
                .item(10, createTagItem(ItemTag.UNIQUE),    (g, e) -> toggleTag(player, ItemTag.UNIQUE))
                .item(11, createTagItem(ItemTag.FINAL),     (g, e) -> toggleTag(player, ItemTag.FINAL))
                .item(12, createTagItem(ItemTag.INFINITE),  (g, e) -> toggleTag(player, ItemTag.INFINITE))
                .item(13, createTagItem(ItemTag.PROTECTED), (g, e) -> toggleTag(player, ItemTag.PROTECTED))

                // Material Matching
                .item(16, createMaterialListItem(), (g, e) -> {
                    if (rule.materialMode != GlobalRule.MaterialMatchMode.IGNORE)
                        manager.openMaterialSelector(player, rule);
                })
                .item(25, createItemsAdderListItem(), (g, e) -> {
                    if (rule.materialMode != GlobalRule.MaterialMatchMode.IGNORE)
                        manager.openItemsAdderParser(player, rule);
                })
                .item(34, createMaterialModeItem(), (g, e) -> cycleMaterialMode(player))

                // Criteria Match Mode
                .item(43, createMatchModeItem(), (g, e) -> cycleMatchMode(player))

                // Criteria Section
                .item(28, createCriteriaItem("Name Regex", Material.NAME_TAG,
                                !rule.nameContainsRegex.isEmpty(), rule.nameContainsRegex),
                        (g, e) -> manager.openNameCriteriaEditor(player, rule))

                .item(29, createCriteriaItem("Lore Regex", Material.WRITABLE_BOOK,
                                !rule.loreContainsRegex.isEmpty(), rule.loreContainsRegex),
                        (g, e) -> manager.openLoreCriteriaEditor(player, rule))

                .item(30, createCriteriaItem("NBT Regex", Material.BOOK,
                                !rule.nbtTagContainsRegex.isEmpty(), rule.nbtTagContainsRegex),
                        (g, e) -> manager.openNbtCriteriaEditor(player, rule))

                .item(31, createCriteriaItem("Compound Regex", Material.BOOKSHELF,
                                !rule.compoundTagContainsRegex.isEmpty(), rule.compoundTagContainsRegex),
                        (g, e) -> manager.openCompoundCriteriaEditor(player, rule))

                .item(32, createCriteriaItem("Model Data", Material.COMPASS,
                                !rule.legacyModelData.isEmpty(), rule.legacyModelData.size() + " values"),

                        (g, e) -> manager.openModelDataEditor(player, rule))
                .item(37, createCriteriaItem("Enchantments", Material.ENCHANTED_BOOK,
                                !rule.enchantments.isEmpty(), rule.enchantments.size() + " enchants"),
                        (g, e) -> manager.openEnchantmentEditor(player, rule))

                .item(38, createCriteriaItem("Attributes", Material.GOLDEN_APPLE,
                                !rule.attributes.isEmpty(), rule.attributes.size() + " attributes"),
                        (g, e) -> manager.openAttributeEditor(player, rule))

                .item(39, createCriteriaItem("Item Flags", Material.WHITE_BANNER,
                                !rule.itemFlags.isEmpty(), rule.itemFlags.size() + " flags"),
                        (g, e) -> manager.openItemFlagEditor(player, rule))

                .item(40, createCriteriaItem("Potion Effects", Material.POTION,
                                !rule.potionEffects.isEmpty(), rule.potionEffects.size() + " effects"),
                        (g, e) -> manager.openPotionEffectEditor(player, rule))

                .item(41, createCriteriaItem("Armor Trim", Material.NETHERITE_CHESTPLATE,
                                !rule.trimPatterns.isEmpty() || !rule.trimMaterials.isEmpty(),
                                (rule.trimPatterns.size() + rule.trimMaterials.size()) + " selected"),
                        (g, e) -> manager.openArmorTrimEditor(player, rule))

                .onClose((g, e) -> {
                    getConfig().save();
                    successAny(player, "Saved global rule");
                })
                .fillEmpty(EMPTY())
                .clickSound(Sound.UI_BUTTON_CLICK, 0.7f, 1.2f)
                .build();

        gui.open(player);
    }



    private ItemStack createTagItem(ItemTag tag) {
        boolean active = rule.appliedTags.contains(tag);
        Material material = switch (tag) {
            case UNIQUE -> Material.EMERALD_BLOCK;
            case FINAL -> Material.REDSTONE_BLOCK;
            case INFINITE -> Material.LAPIS_BLOCK;
            case PROTECTED -> Material.STRUCTURE_BLOCK;
        };

        return ItemBuilder.create(material)
                .displayName("<" + (active ? "green" : "gray") + "><bold>" + tag.getName() + " Tag")
                .loreMiniMessage(Arrays.asList(
                        "<gray>" + tag.getDesc(),
                        "",
                        "<white>Status: " + (active ? "<green>ACTIVE" : "<red>INACTIVE"),
                        "",
                        "<yellow>▶ <white>Click to toggle"
                ))
                .build();
    }

    private ItemStack createMatchModeItem() {
        return ItemBuilder.create(Material.COMPARATOR)
                .displayName("<yellow><bold>Match Mode: " + rule.matchMode.name())
                .loreMiniMessage(Arrays.asList(
                        "<gray>Determines how multiple criteria",
                        "<gray>are evaluated together",
                        "",
                        "<white>Current: <yellow>" + rule.matchMode.name(),
                        "",
                        "<gray>• AND: All criteria must match",
                        "<gray>• OR: Any criteria must match",
                        "<gray>• NAND: Not all criteria match",
                        "<gray>• XOR: Exactly one criteria matches",
                        "",
                        "<yellow>▶ <white>Click to cycle"
                ))
                .build();
    }

    private ItemStack createCriteriaItem(String name, Material material, boolean hasValue, String preview) {
        List<String> lore = new ArrayList<>();
        lore.add("<gray>Configure " + name.toLowerCase() + " criteria");
        lore.add("");

        if (hasValue) {
            lore.add("<white>Current: <green>" + preview);
        } else {
            lore.add("<white>Current: <red>Not set");
        }

        lore.add("");
        lore.add("<yellow>▶ <white>Click to edit");

        return ItemBuilder.create(material)
                .displayName("<white><bold>" + name)
                .loreMiniMessage(lore)
                .build();
    }

    private ItemStack createMaterialModeItem() {
        return ItemBuilder.create(Material.GRASS_BLOCK)
                .displayName("<yellow><bold>Material Mode: " + rule.materialMode.name())
                .loreMiniMessage(Arrays.asList(
                        "<gray>Control which materials this",
                        "<gray>rule applies to",
                        "",
                        "<white>Current: <yellow>" + rule.materialMode.name(),
                        "",
                        "<gray>• IGNORE: Applies to all materials",
                        "<gray>• WHITELIST: Only listed materials",
                        "<gray>• BLACKLIST: Exclude listed materials",
                        "",
                        "<yellow>▶ <white>Click to cycle"
                ))
                .build();
    }

    private ItemStack createMaterialListItem() {
        if (rule.materialMode == GlobalRule.MaterialMatchMode.IGNORE) {
            return ItemBuilder.create(Material.GRAY_DYE)
                    .displayName("<gray><bold>Material List")
                    .loreMiniMessage(Arrays.asList(
                            "<gray>Set material mode to",
                            "<gray>WHITELIST or BLACKLIST",
                            "<gray>to configure materials"
                    ))
                    .build();
        }

        List<String> lore = new ArrayList<>();
        lore.add("<gray>Manage materials for this rule");
        lore.add("");
        lore.add("<white>Selected: <yellow>" + rule.effectedMaterials.size() + " materials");

        if (!rule.effectedMaterials.isEmpty()) {
            lore.add("");
            List<String> materialNames = rule.effectedMaterials.stream()
                    .limit(5)
                    .map(Enum::name)
                    .toList();

            for (String mat : materialNames) {
                lore.add("<gray>• " + mat);
            }

            if (rule.effectedMaterials.size() > 5) {
                lore.add("<gray>... and " + (rule.effectedMaterials.size() - 5) + " more");
            }
        }

        lore.add("");
        lore.add("<yellow>▶ <white>Click to manage");

        return ItemBuilder.create(Material.CHEST)
                .displayName("<white><bold>Material List")
                .loreMiniMessage(lore)
                .build();
    }

    private ItemStack createItemsAdderListItem() {
        if (rule.materialMode == GlobalRule.MaterialMatchMode.IGNORE) {
            return ItemBuilder.create(Material.GRAY_DYE)
                    .displayName("<gray><bold>ItemsAdder List")
                    .loreMiniMessage(Arrays.asList(
                            "<gray>Set material mode to",
                            "<gray>WHITELIST or BLACKLIST",
                            "<gray>to configure materials"
                    ))
                    .build();
        }

        List<String> lore = new ArrayList<>();
        lore.add("<gray>Manage custom materials for this rule");
        lore.add("");
        lore.add("<white>Selected: <yellow>" + rule.effectedItemsAdderMaterials.size() + " Custom Materials");

        if (!rule.effectedMaterials.isEmpty()) {
            lore.add("");
            List<String> items = new ArrayList<>();

            rule.effectedItemsAdderMaterials.stream()
                    .limit(5)
                    .forEach((iai)->{
                        items.add("<dark_green>" + iai.namespace + "<gray>:<green>" + iai.id);
                    });

            for (String mat : items) {
                lore.add("<gray>• " + mat);
            }

            if (rule.effectedMaterials.size() > 5) {
                lore.add("<gray>... and " + (rule.effectedMaterials.size() - 5) + " more");
            }
        }

        lore.add("");
        lore.add("<yellow>▶ <white>Click to manage");

        return ItemBuilder.create(Material.CHEST)
                .displayName("<white><bold>ItemsAdder List")
                .loreMiniMessage(lore)
                .build();
    }

    private void toggleTag(Player player, ItemTag tag) {
        if (rule.appliedTags.contains(tag)) {
            rule.appliedTags.remove(tag);
        } else {
            rule.appliedTags.add(tag);
        }
        open(player);
    }

    private void cycleMatchMode(Player player) {
        GlobalRule.MatchMode[] modes = GlobalRule.MatchMode.values();
        int current = rule.matchMode.ordinal();
        rule.matchMode = modes[(current + 1) % modes.length];
        open(player);
    }

    private void cycleMaterialMode(Player player) {
        GlobalRule.MaterialMatchMode[] modes = GlobalRule.MaterialMatchMode.values();
        int current = rule.materialMode.ordinal();
        rule.materialMode = modes[(current + 1) % modes.length];

        // Clear materials if switching to IGNORE
        if (rule.materialMode == GlobalRule.MaterialMatchMode.IGNORE) {
            rule.effectedMaterials.clear();
        }

        open(player);
    }
}