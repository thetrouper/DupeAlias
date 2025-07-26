package me.trouper.dupealias.server.gui.admin;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.GlobalRule;
import me.trouper.dupealias.server.ItemTag;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.config.ConfigGui;
import me.trouper.dupealias.server.gui.admin.globalrule.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class AdminPanelManager implements DupeContext, CommonItems {

    public void openMainGui(Player player) {
        new MainAdminGui(this).open(player);
    }

    public void openHeldItemGui(Player player) {
        new HeldItemGui(this).open(player);
    }

    public void openBulkTagGui(Player player) {
        new BulkTagGui(this).open(player);
    }
    
    public void openHelpGui(Player player) {
        new HelpGui(this).open(player);
    }

    public void openGlobalRuleList(Player player) {
        new GlobalRuleListGui(this).createGUI(player).open(player);
    }

    public void openConfigGui(Player player) {
        new ConfigGui(this).open(player);
    }

    public void openGlobalRuleEditor(Player player, GlobalRule rule) {
        new GlobalRuleEditorGui(this, rule).open(player);
    }

    public void openMaterialSelector(Player player, GlobalRule rule) {
        new GlobalRuleMaterialSelector(this, rule).createGUI(player).open(player);
    }

    public void openNameCriteriaEditor(Player player, GlobalRule rule) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#ffeb3b:#ffc107><bold>Name Contains</bold></gradient>")
                .rows(3)
                .item(13, ItemBuilder.create(Material.NAME_TAG)
                        .displayName("<yellow><bold>Current Pattern")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Set a regex pattern to match",
                                "<gray>against item display names",
                                "",
                                "<white>Current: <yellow>" + (rule.nameContainsRegex.isEmpty() ? "Not set" : rule.nameContainsRegex),
                                "",
                                "<yellow>▶ <white>Click to set pattern",
                                "<yellow>▶ <white>Right-click to clear"
                        ))
                        .build(), (g, e) -> {
                    if (e.isRightClick()) {
                        rule.nameContainsRegex = "";
                        getConfig().save();
                        successAny(player, "Cleared name pattern");
                        openGlobalRuleEditor(player, rule);
                    } else {
                        g.requestInput(player, "modelData");
                    }
                })
                .item(31, BACK(), (g, e) -> openGlobalRuleEditor(player, rule))
                .fillEmpty(EMPTY())
                .callback("modelData", new QuickGui.GuiCallback() {
                    @Override
                    public void onInput(QuickGui gui, Player player, String input, QuickGui.InputSource source) {
                        try {
                            int value = Integer.parseInt(input);
                            if (rule.legacyModelData.contains(value)) {
                                infoAny(player, "Model data value {0} already exists", value);
                            } else {
                                rule.legacyModelData.add(value);
                                getConfig().save();
                                successAny(player, "Added model data value: {0}", value);
                            }
                        } catch (NumberFormatException ex) {
                            errorAny(player, "Invalid number: {0}", input);
                        }
                        openModelDataEditor(player, rule);
                    }
                })
                .build();

        int slot = 19;
        for (Integer value : rule.legacyModelData.stream().limit(5).toList()) {
            gui.updateItem(slot++, ItemBuilder.create(Material.FILLED_MAP)
                    .displayName("<aqua><bold>Value: " + value)
                    .loreMiniMessage(Arrays.asList(
                            "<gray>Model data value",
                            "",
                            "<yellow>▶ <white>Click to remove"
                    ))
                    .build(), (g, e) -> {
                rule.legacyModelData.remove(value);
                getConfig().save();
                successAny(player, "Removed model data value: {0}", value);
                openModelDataEditor(player, rule);
            });
        }

        gui.open(player);
    }

    public void openPotionEffectEditor(Player player, GlobalRule rule) {
        new GlobalRulePotionEffectEditor(this, rule).createGUI(player).open(player);
    }

    public void openArmorTrimEditor(Player player, GlobalRule rule) {
        new GlobalRuleArmorTrimEditor(this, rule).open(player);
    }

    public void openLoreCriteriaEditor(Player player, GlobalRule rule) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#9c27b0:#7b1fa2><bold>Lore Contains</bold></gradient>")
                .rows(3)
                .item(13, ItemBuilder.create(Material.WRITABLE_BOOK)
                        .displayName("<light_purple><bold>Current Pattern")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Set a regex pattern to match",
                                "<gray>against item lore lines",
                                "",
                                "<white>Current: <light_purple>" + (rule.loreContainsRegex.isEmpty() ? "Not set" : rule.loreContainsRegex),
                                "",
                                "<yellow>▶ <white>Click to set pattern",
                                "<yellow>▶ <white>Right-click to clear"
                        ))
                        .build(), (g, e) -> {
                    if (e.isRightClick()) {
                        rule.loreContainsRegex = "";
                        getConfig().save();
                        successAny(player, "Cleared lore pattern");
                        openGlobalRuleEditor(player, rule);
                    } else {
                        g.requestInput(player, "lorePattern");
                    }
                })
                .item(22, BACK(), (g, e) -> openGlobalRuleEditor(player, rule))
                .fillEmpty(EMPTY())
                .callback("lorePattern", new QuickGui.GuiCallback() {
                    @Override
                    public void onInput(QuickGui gui, Player player, String input, QuickGui.InputSource source) {
                        rule.loreContainsRegex = input;
                        getConfig().save();
                        successAny(player, "Set lore pattern to: " + input);
                        openGlobalRuleEditor(player, rule);
                    }
                })
                .build();
        gui.open(player);
    }

    public void openEnchantmentEditor(Player player, GlobalRule rule) {
        new GlobalRuleEnchantmentEditor(this, rule).createGUI(player).open(player);
    }

    public void openAttributeEditor(Player player, GlobalRule rule) {
        new GlobalRuleAttributeEditor(this, rule).createGUI(player).open(player);
    }

    public void openItemFlagEditor(Player player, GlobalRule rule) {
        new GlobalRuleItemFlagEditor(this, rule).open(player);
    }

    public void openModelDataEditor(Player player, GlobalRule rule) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#00bcd4:#0097a7><bold>Model Data Values</bold></gradient>")
                .rows(4)
                .item(13, ItemBuilder.create(Material.COMPASS)
                        .displayName("<aqua><bold>Model Data Values")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Manage custom model data values",
                                "<gray>that items must have",
                                "",
                                "<white>Current values: <aqua>" + rule.legacyModelData.size(),
                                rule.legacyModelData.isEmpty() ? "" : "<gray>" + rule.legacyModelData.stream()
                                        .limit(5)
                                        .map(String::valueOf)
                                        .collect(Collectors.joining(", ")),
                                rule.legacyModelData.size() > 5 ? "<gray>... and " + (rule.legacyModelData.size() - 5) + " more" : "",
                                "",
                                "<yellow>▶ <white>Click to add value",
                                "<yellow>▶ <white>Right-click to clear all"
                        ))
                        .build(), (g, e) -> {
                    if (e.isRightClick()) {
                        rule.legacyModelData.clear();
                        getConfig().save();
                        successAny(player, "Cleared all model data values");
                        openModelDataEditor(player, rule);
                    } else {
                        g.requestInput(player,"namePattern");
                    }
                })
                .item(22, BACK(), (g, e) -> openGlobalRuleEditor(player, rule))
                .fillEmpty(EMPTY())
                .callback("namePattern", new QuickGui.GuiCallback() {
                    @Override
                    public void onInput(QuickGui gui, Player player, String input, QuickGui.InputSource source) {
                        rule.nameContainsRegex = input;
                        getConfig().save();
                        successAny(player, "Set name pattern to: " + input);
                        openGlobalRuleEditor(player, rule);
                    }
                })
                .build();
        gui.open(player);
    }

    public ItemStack createExplainedItem(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE)
                    .displayName("<gray><italic>No item held</italic>")
                    .loreMiniMessage("<aqua>💡 <white>Hold an item to get information on it")
                    .build();
        }

        List<String> lore = new ArrayList<>();
        lore.add("<white><bold>Held Item Explanation:</bold>");

        Set<ItemTag> activeTags = new HashSet<>();

        for (ItemTag tag : ItemTag.values()) {
            boolean global = getDupe().checkGlobalRuleTag(item, tag);
            boolean hasMeta = item.hasItemMeta() &&
                    item.getItemMeta().getPersistentDataContainer().has(tag.getKey());

            if (hasMeta) {
                Boolean individual = item.getItemMeta()
                        .getPersistentDataContainer()
                        .get(tag.getKey(), PersistentDataType.BOOLEAN);
                if (Boolean.TRUE.equals(individual)) {
                    lore.add("<gray>• <green>" + tag.getName() + "</green> (Individual): " + tag.getDesc());
                    activeTags.add(tag);
                } else {
                    lore.add("<gray>• <red>" + tag.getName() + "</red> (Individually false)");
                    if (global) {
                        lore.add("  <gray>- Global is active: " + tag.getDesc());
                        lore.add("  <gray>- Global is overridden by Individual tag.");
                        activeTags.add(tag);
                    }
                }
            } else if (global) {
                lore.add("<gray>• <yellow>" + tag.getName() + "</yellow> (Global): " + tag.getDesc());
                activeTags.add(tag);
            }
        }

        if (getDupe().isUnique(item)) {
            lore.add("<gray>• Detected UNIQUE by UniqueCheck");
            activeTags.add(ItemTag.UNIQUE);
        }

        if (lore.size() == 1) {
            lore.add("<gray>• No DupeAlias tags apply to this item");
        }

        List<String> conflicts = new ArrayList<>();
        if (activeTags.contains(ItemTag.INFINITE) && activeTags.contains(ItemTag.UNIQUE)) {
            conflicts.add("INFINITE ↔ UNIQUE");
        }
        if (activeTags.contains(ItemTag.INFINITE) && activeTags.contains(ItemTag.PROTECTED)) {
            conflicts.add("INFINITE ↔ PROTECTED");
        }

        if (!conflicts.isEmpty()) {
            lore.add("");
            lore.add("<red>⚠ <bold>Conflicts detected:</bold>");
            for (String c : conflicts) {
                lore.add("<gray>• " + c);
            }
            lore.add("<gray>Consider removing one of the above tags.");
        }

        return ItemBuilder.of(item)
                .displayName("<white><bold>Item Details</bold>")
                .loreMiniMessage(lore)
                .build();
    }

    public List<String> getItemTagStatus(ItemStack item) {
        List<String> lore = new ArrayList<>();
        lore.add("<white>Held Item: <yellow>" + item.getType().name());
        lore.add("");

        List<String> individualTags = new ArrayList<>();
        for (ItemTag tag : ItemTag.values()) {
            if (getDupe().hasIndividualTag(item,tag)) {
                individualTags.add("<" + getTagColor(tag) + ">" + (getDupe().checkIndividualTag(item,tag) ? "✔" : "❌") + " " + tag.getName());
            }
        }

        List<String> globalTags = new ArrayList<>();
        for (ItemTag tag : ItemTag.values()) {
            if (getDupe().checkGlobalRuleTag(item, tag)) {
                globalTags.add("<" + getTagColor(tag) + ">🌍 " + tag.getName());
            }
        }

        if (!individualTags.isEmpty()) {
            lore.add("<white>Individual Tags:");
            lore.addAll(individualTags);
        }

        if (!globalTags.isEmpty()) {
            if (!individualTags.isEmpty()) lore.add("");
            lore.add("<white>Global Tags:");
            lore.addAll(globalTags);
        }

        if (individualTags.isEmpty() && globalTags.isEmpty()) {
            lore.add("<gray>No tags applied");
        }

        return lore;
    }

    public String getTagColor(ItemTag tag) {
        return switch (tag) {
            case UNIQUE -> "green";
            case FINAL -> "red";
            case INFINITE -> "blue";
            case PROTECTED -> "dark_purple";
        };
    }

    
}