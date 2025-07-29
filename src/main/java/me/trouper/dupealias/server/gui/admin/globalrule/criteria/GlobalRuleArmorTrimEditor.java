package me.trouper.dupealias.server.gui.admin.globalrule.criteria;

import me.trouper.alias.data.enums.ValidTrimMaterial;
import me.trouper.alias.data.enums.ValidTrimPattern;
import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.FormatUtils;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.GlobalRule;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.AdminPanelManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GlobalRuleArmorTrimEditor implements DupeContext, CommonItems {

    private final AdminPanelManager manager;
    private final GlobalRule rule;

    public GlobalRuleArmorTrimEditor(AdminPanelManager manager, GlobalRule rule) {
        this.manager = manager;
        this.rule = rule;
    }

    public void open(Player player) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#795548:#5d4037><bold>Armor Trim Criteria</bold></gradient>")
                .rows(4)
                .fillBorder(EMPTY(Material.BROWN_STAINED_GLASS_PANE))

                // Back button
                .item(0, BACK(), (g, e) -> manager.openGlobalRuleEditor(player, rule))

                // Pattern section
                .item(11, ItemBuilder.create(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                        .displayName("<gold><bold>Trim Patterns")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Select required armor",
                                "<gray>trim patterns",
                                "",
                                "<white>Selected: <gold>" + rule.trimPatterns.size() + " patterns",
                                !rule.trimPatterns.isEmpty() ? "<gray>" + rule.trimPatterns.stream()
                                        .limit(3)
                                        .map(FormatUtils::formatEnum)
                                        .collect(Collectors.joining(", ")) : "",
                                rule.trimPatterns.size() > 3 ? "<gray>... and " + (rule.trimPatterns.size() - 3) + " more" : "",
                                "",
                                "<yellow>▶ <white>Click to manage patterns"
                        ))
                        .build(), (g, e) -> openPatternSelector(player))

                // Material section
                .item(15, ItemBuilder.create(Material.COPPER_INGOT)
                        .displayName("<aqua><bold>Trim Materials")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Select required armor",
                                "<gray>trim materials",
                                "",
                                "<white>Selected: <aqua>" + rule.trimMaterials.size() + " materials",
                                !rule.trimMaterials.isEmpty() ? "<gray>" + rule.trimMaterials.stream()
                                        .limit(3)
                                        .map(FormatUtils::formatEnum)
                                        .collect(Collectors.joining(", ")) : "",
                                rule.trimMaterials.size() > 3 ? "<gray>... and " + (rule.trimMaterials.size() - 3) + " more" : "",
                                "",
                                "<yellow>▶ <white>Click to manage materials"
                        ))
                        .build(), (g, e) -> openMaterialSelector(player))

                // Clear all button
                .item(22, ItemBuilder.create(Material.BARRIER)
                        .displayName("<red><bold>Clear All Trim Requirements")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Remove all trim criteria",
                                "",
                                "<yellow>▶ <white>Click to clear"
                        ))
                        .build(), (g, e) -> {
                    rule.trimPatterns.clear();
                    rule.trimMaterials.clear();
                    getConfig().save();
                    successAny(player, "Cleared all armor trim requirements");
                    open(player);
                })

                .fillEmpty(EMPTY())
                .clickSound(Sound.UI_BUTTON_CLICK, 0.7f, 1.2f)
                .build();

        gui.open(player);
    }

    private void openPatternSelector(Player player) {
        QuickGui.GuiBuilder builder = QuickGui.create()
                .titleMini("<gradient:#ffc107:#ff6f00><bold>Select Trim Patterns</bold></gradient>")
                .rows(6)
                .fillBorder(EMPTY(Material.ORANGE_STAINED_GLASS_PANE))
                .item(0, BACK(), (g, e) -> open(player));

        List<ValidTrimPattern> patterns = Arrays.asList(ValidTrimPattern.values());
        int[] slots = {
                11, 12, 13, 14, 15,
                20, 21, 22, 23, 24,
                29, 30, 31, 32, 33,
                    39, 40, 41
        };

        for (int i = 0; i < patterns.size(); i++) {
            ValidTrimPattern pattern = patterns.get(i);
            boolean selected = rule.trimPatterns.contains(pattern);
            ItemBuilder item = ItemBuilder.create(pattern.getMaterial())
                    .displayName((selected ? "<gold>" : "<gray>") + "<bold>" + pattern.name())
                    .loreMiniMessage("<gray>Pattern: " + pattern.name(),"", selected ? "<white>Status: <gold>SELECTED" : "<white>Status: <gray>NOT SELECTED","","<yellow>▶ <white>Click to toggle")
                    .hideAllFlags();

            if (selected) item.enchant(Enchantment.MENDING);

            builder.item(slots[i], item.build(), (g, e) -> {
                if (rule.trimPatterns.contains(pattern)) {
                    rule.trimPatterns.remove(pattern);
                    infoAny(player, "Removed {0} pattern requirement", pattern.name());
                } else {
                    rule.trimPatterns.add(pattern);
                    successAny(player, "Added {0} pattern requirement", pattern.name());
                }
                getConfig().save();
                openPatternSelector(player);
            });
        }

        builder.fillEmpty(EMPTY()).build().open(player);
    }

    private void openMaterialSelector(Player player) {
        QuickGui.GuiBuilder builder = QuickGui.create()
                .titleMini("<gradient:#00bcd4:#006064><bold>Select Trim Materials</bold></gradient>")
                .rows(5)
                .fillBorder(EMPTY(Material.CYAN_STAINED_GLASS_PANE))
                .item(0, BACK(), (g, e) -> open(player));

        List<ValidTrimMaterial> materials = Arrays.asList(ValidTrimMaterial.values());

        int[] slots = {
                11, 13, 15,
                20, 21, 22, 23, 24,
                29, 31, 33
        };


        for (int i = 0; i < materials.size(); i++) {
            ValidTrimMaterial material = materials.get(i);
            boolean selected = rule.trimMaterials.contains(material);

            ItemBuilder item = ItemBuilder.create(materials.get(i).getMaterial())
                    .displayName((selected ? "<aqua>" : "<gray>") + "<bold>" + material.name())
                    .loreMiniMessage(
                            "<gray>Material: " + material.name(),
                            "",
                            "<white>Status: " + (selected ? "<aqua>SELECTED" : "<gray>NOT SELECTED"),
                            "",
                            "<yellow>▶ <white>Click to toggle"
                    )
                    .hideAllFlags();

            if (selected) item.enchant(Enchantment.MENDING);

            builder.item(slots[i], item.build(), (g, e) -> {
                if (rule.trimMaterials.contains(material)) {
                    rule.trimMaterials.remove(material);
                    infoAny(player, "Removed {0} material requirement", material.name());
                } else {
                    rule.trimMaterials.add(material);
                    successAny(player, "Added {0} material requirement", material.name());
                }
                getConfig().save();
                openMaterialSelector(player);
            });
        }

        builder.fillEmpty(EMPTY()).build().open(player);
    }

}