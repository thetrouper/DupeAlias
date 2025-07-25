package me.trouper.dupealias.server.gui.admin.globalrule;

import me.trouper.alias.data.enums.ValidTrimMaterial;
import me.trouper.alias.data.enums.ValidTrimPattern;
import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.server.systems.gui.QuickPaginatedGUI;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.GlobalRule;
import me.trouper.dupealias.server.ItemTag;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.AdminPanelManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalRuleListGui extends QuickPaginatedGUI<GlobalRule> implements DupeContext, CommonItems {

    private final AdminPanelManager manager;

    public GlobalRuleListGui(AdminPanelManager manager) {
        this.manager = manager;
    }

    @Override
    protected String getTitle(Player player) {
        return "<gradient:#ff6b6b:#ffa726><bold>Global Rules Manager</bold></gradient>";
    }

    @Override
    protected List<GlobalRule> getAllItems(Player player) {
        return new ArrayList<>(getConfig().globalRules);
    }

    @Override
    protected ItemStack createDisplayItem(GlobalRule rule) {
        List<String> lore = new ArrayList<>();

        // Display applied tags
        if (!rule.appliedTags.isEmpty()) {
            lore.add("<white>Applied Tags:");
            for (ItemTag tag : rule.appliedTags) {
                lore.add("<gray>• <" + manager.getTagColor(tag) + ">" + tag.getName());
            }
        } else {
            lore.add("<red>No tags applied");
        }

        lore.add("");
        lore.add("<white>Match Mode: <yellow>" + rule.matchMode.name());
        lore.add("<white>Criteria Count: <yellow>" + rule.getCriteriaCount());

        // Show material mode if applicable
        if (rule.materialMode != GlobalRule.MaterialMatchMode.IGNORE) {
            lore.add("<white>Material Mode: <yellow>" + rule.materialMode.name());
            if (!rule.effectedMaterials.isEmpty()) {
                lore.add("<white>Materials: <yellow>" + rule.effectedMaterials.size() + " selected");
            }
        }

        // Quick preview of criteria
        List<String> criteria = new ArrayList<>();
        if (!rule.nameContainsRegex.isEmpty()) criteria.add("Name");
        if (!rule.loreContainsRegex.isEmpty()) criteria.add("Lore");
        if (!rule.enchantments.isEmpty()) criteria.add("Enchants");
        if (!rule.attributes.isEmpty()) criteria.add("Attributes");
        if (!rule.itemFlags.isEmpty()) criteria.add("Flags");
        if (!rule.legacyModelData.isEmpty()) criteria.add("Model Data");
        if (!rule.potionEffects.isEmpty()) criteria.add("Potion Effects");
        if (!rule.trimPatterns.isEmpty() || !rule.trimMaterials.isEmpty()) criteria.add("Armor Trim");

        if (!criteria.isEmpty()) {
            lore.add("<white>Criteria: <gray>" + String.join(", ", criteria));
        }

        lore.add("");
        lore.add("<yellow>▶ <white>Left-click to edit");
        lore.add("<yellow>▶ <white>Right-click to delete");
        lore.add("<yellow>▶ <white>Shift-click to duplicate");

        // Determine icon based on tags or criteria
        Material icon = Material.WRITTEN_BOOK;
        if (rule.appliedTags.contains(ItemTag.UNIQUE)) icon = Material.EMERALD;
        else if (rule.appliedTags.contains(ItemTag.FINAL)) icon = Material.REDSTONE;
        else if (rule.appliedTags.contains(ItemTag.INFINITE)) icon = Material.LAPIS_LAZULI;
        else if (rule.appliedTags.contains(ItemTag.PROTECTED)) icon = Material.NETHER_STAR;
        else if (!rule.enchantments.isEmpty()) icon = Material.ENCHANTED_BOOK;
        else if (!rule.potionEffects.isEmpty()) icon = Material.POTION;
        else if (!rule.trimPatterns.isEmpty() || !rule.trimMaterials.isEmpty()) icon = Material.NETHERITE_CHESTPLATE;

        String tagNames = rule.appliedTags.stream()
                .map(ItemTag::getName)
                .collect(Collectors.joining(", "));

        return ItemBuilder.create(icon)
                .displayName("<white><bold>Rule #" + (getConfig().globalRules.indexOf(rule) + 1) +
                        (tagNames.isEmpty() ? "" : " <gray>(" + tagNames + ")"))
                .loreMiniMessage(lore)
                .build();
    }

    @Override
    protected void handleItemClick(Player player, GlobalRule rule, InventoryClickEvent event) {
        if (event.isRightClick()) {
            // Confirm deletion
            QuickGui.create()
                    .titleMini("<red><bold>Delete Rule?")
                    .rows(3)
                    .item(12, ItemBuilder.create(Material.RED_CONCRETE)
                            .displayName("<red><bold>Delete Rule")
                            .loreMiniMessage("<gray>This action cannot be undone!")
                            .build(), (gui, e) -> {
                        getConfig().globalRules.remove(rule);
                        getConfig().save();
                        successAny(player, "Deleted global rule");
                        createGUI(player).open(player);
                    })
                    .item(14, ItemBuilder.create(Material.GREEN_CONCRETE)
                            .displayName("<green><bold>Cancel")
                            .build(), (gui, e) -> createGUI(player).open(player))
                    .fillEmpty(EMPTY())
                    .build()
                    .open(player);
        } else if (event.isShiftClick()) {
            // Duplicate rule
            GlobalRule duplicate = new GlobalRule();

            // Copy all fieldsx
            duplicate.matchMode = rule.matchMode;
            duplicate.materialMode = rule.materialMode;
            duplicate.effectedMaterials = EnumSet.copyOf(rule.effectedMaterials);
            duplicate.nameContainsRegex = rule.nameContainsRegex;
            duplicate.loreContainsRegex = rule.loreContainsRegex;
            duplicate.legacyModelData = new HashSet<>(rule.legacyModelData);
            duplicate.itemFlags = EnumSet.copyOf(rule.itemFlags);
            duplicate.enchantments = new HashMap<>(rule.enchantments);
            duplicate.potionEffects = new HashMap<>(rule.potionEffects);
            duplicate.attributes = new HashMap<>(rule.attributes);
            duplicate.trimPatterns = rule.trimPatterns.isEmpty() ? EnumSet.noneOf(ValidTrimPattern.class) : EnumSet.copyOf(rule.trimPatterns);
            duplicate.trimMaterials = rule.trimMaterials.isEmpty() ? EnumSet.noneOf(ValidTrimMaterial.class) : EnumSet.copyOf(rule.trimMaterials);
            duplicate.appliedTags = EnumSet.copyOf(rule.appliedTags);

            getConfig().globalRules.add(duplicate);
            getConfig().save();

            successAny(player, "Duplicated global rule");
            createGUI(player).open(player);
        } else {
            // Edit rule
            manager.openGlobalRuleEditor(player, rule);
        }
    }

    @Override
    protected void addFilterItems(QuickGui.GuiBuilder filterGui, Player player, Set<String> filters) {
        filterGui.item(0, createFilterToggleItem("Has UNIQUE", Material.EMERALD_BLOCK, filters.contains("U")),
                (gui, event) -> toggleFilter(player, "U"));
        filterGui.item(1, createFilterToggleItem("Has FINAL", Material.REDSTONE_BLOCK, filters.contains("F")),
                (gui, event) -> toggleFilter(player, "F"));
        filterGui.item(2, createFilterToggleItem("Has INFINITE", Material.LAPIS_BLOCK, filters.contains("I")),
                (gui, event) -> toggleFilter(player, "I"));
        filterGui.item(3, createFilterToggleItem("Has PROTECTED", Material.COMMAND_BLOCK, filters.contains("P")),
                (gui, event) -> toggleFilter(player, "P"));
        filterGui.item(5, createFilterToggleItem("Uses Materials", Material.GRASS_BLOCK, filters.contains("M")),
                (gui, event) -> toggleFilter(player, "M"));
        filterGui.item(6, createFilterToggleItem("Has Criteria", Material.COMPARATOR, filters.contains("C")),
                (gui, event) -> toggleFilter(player, "C"));
    }

    @Override
    protected boolean testFilter(Player player, GlobalRule rule, String filterKey) {
        return switch (filterKey) {
            case "U" -> rule.appliedTags.contains(ItemTag.UNIQUE);
            case "F" -> rule.appliedTags.contains(ItemTag.FINAL);
            case "I" -> rule.appliedTags.contains(ItemTag.INFINITE);
            case "P" -> rule.appliedTags.contains(ItemTag.PROTECTED);
            case "M" -> rule.materialMode != GlobalRule.MaterialMatchMode.IGNORE && !rule.effectedMaterials.isEmpty();
            case "C" -> rule.getCriteriaCount() > 0;
            default -> false;
        };
    }

    @Override
    protected void openBackGUI(Player player) {
        manager.openMainGui(player);
    }

    @Override
    public QuickGui createGUI(Player player) {
        QuickGui gui = super.createGUI(player);
        gui.updateItem(51, ItemBuilder.create(Material.LIME_DYE)
                .displayName("<green><bold>+ Create New Rule")
                .loreMiniMessage(Arrays.asList(
                        "<gray>Create a new global rule",
                        "<gray>to apply tags to items",
                        "",
                        "<yellow>▶ <white>Click to create"
                ))
                .build(), (q, event) -> {
            GlobalRule newRule = new GlobalRule();
            getConfig().globalRules.add(newRule);
            getConfig().save();
            manager.openGlobalRuleEditor((Player) event.getWhoClicked(), newRule);
        });
        return gui;
    }

}