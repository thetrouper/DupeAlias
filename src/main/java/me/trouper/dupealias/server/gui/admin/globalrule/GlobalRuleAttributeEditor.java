package me.trouper.dupealias.server.gui.admin.globalrule;

import me.trouper.alias.data.enums.ValidAttribute;
import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.server.systems.gui.QuickPaginatedGUI;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.GlobalRule;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.AdminPanelManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class GlobalRuleAttributeEditor extends QuickPaginatedGUI<ValidAttribute> implements DupeContext, CommonItems {

    private final AdminPanelManager manager;
    private final GlobalRule rule;

    public GlobalRuleAttributeEditor(AdminPanelManager manager, GlobalRule rule) {
        this.manager = manager;
        this.rule = rule;
    }

    @Override
    protected String getTitle(Player player) {
        return "<gradient:#4caf50:#2e7d32><bold>Attribute Criteria</bold></gradient>";
    }

    @Override
    protected List<ValidAttribute> getAllItems(Player player) {
        return Arrays.asList(ValidAttribute.values());
    }

    @Override
    protected ItemStack createDisplayItem(ValidAttribute attribute) {
        Double value = rule.attributes.get(attribute);
        boolean hasAttribute = value != null;

        ItemBuilder builder = ItemBuilder.create(hasAttribute ? Material.ENCHANTED_BOOK : Material.BOOK)
                .displayName((hasAttribute ? "<green>" : "<gray>") + "<bold>" + attribute.name())
                .loreMiniMessage("<gray>Attribute: " + attribute.name());

        if (hasAttribute) {
            builder.loreMiniMessage(
                    "",
                    "<white>Required Value: <green>" + String.format("%.2f", value),
                    "",
                    "<yellow>▶ <white>Left-click to change value",
                    "<yellow>▶ <white>Right-click to remove"
            );
        } else {
            builder.loreMiniMessage(
                    "",
                    "<gray>Not required",
                    "",
                    "<yellow>▶ <white>Click to add requirement"
            );
        }

        return builder.build();
    }

    @Override
    protected void handleItemClick(Player player, ValidAttribute attribute, InventoryClickEvent event) {
        if (event.isRightClick() && rule.attributes.containsKey(attribute)) {
            rule.attributes.remove(attribute);
            getConfig().save();
            successAny(player, "Removed {0} requirement", attribute.name());
            createGUI(player).open(player);
            return;
        }
        
        QuickGui inputGui = QuickGui.create()
                .titleMini("<gradient:#4caf50:#2e7d32><bold>Set Attribute Value</bold></gradient>")
                .rows(3)
                .item(13, ItemBuilder.create(Material.EXPERIENCE_BOTTLE)
                        .displayName("<green><bold>" + attribute.name())
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Enter the minimum value",
                                "<gray>required for this attribute.",
                                "",
                                "<white>Current: <green>" +
                                        (rule.attributes.containsKey(attribute) ? String.format("%.2f", rule.attributes.get(attribute)) : "Not set"),
                                "",
                                "<yellow>▶ <white>Click to set value"
                        ))
                        .build(), (g, e) -> getDupe().getGuiListener().requestChatInput(g, player, "attributeValue", "Enter minimum attribute value."))
                .item(22, BACK(), (g, e) -> createGUI(player).open(player)) // Back button.
                .fillEmpty(EMPTY())
                .callback("attributeValue", (gui, p, input, source) -> {
                    try {
                        double value = Double.parseDouble(input);
                        rule.attributes.put(attribute, value);
                        getConfig().save();
                        successAny(player, "Set {0} requirement to {1}", attribute.name(), String.format("%.2f", value));
                    } catch (NumberFormatException ex) {
                        if (input.contains("cancel")) {
                            successAny(player,"Canceled.");
                        } else {
                            errorAny(player, "Invalid number: {0}", input);
                            getDupe().getGuiListener().requestChatInput(gui, player, "attributeValue", "Enter minimum attribute value.");
                            return;
                        }
                    }
                    createGUI(player).open(player);
                })
                .build();
        inputGui.open(player);
    }

    @Override
    protected void addFilterItems(QuickGui.GuiBuilder filterGui, Player player, Set<String> filters) {
        filterGui.item(0, createFilterToggleItem("Selected Only", Material.LIME_DYE, filters.contains("S")),
                (gui, event) -> toggleFilter(player, "S"));
    }

    @Override
    protected boolean testFilter(Player player, ValidAttribute attribute, String filterKey) {
        return switch (filterKey) {
            case "S" -> rule.attributes.containsKey(attribute);
            default -> false;
        };
    }

    @Override
    protected void openBackGUI(Player player) {
        manager.openGlobalRuleEditor(player, rule);
    }
}
