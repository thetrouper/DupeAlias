package me.trouper.dupealias.server.gui.admin.globalrule.criteria.regex;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.GlobalRule;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.AdminPanelManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GlobalRuleModelDataEditor implements DupeContext, CommonItems {
    private final AdminPanelManager manager;
    private final GlobalRule rule;

    public GlobalRuleModelDataEditor(AdminPanelManager manager, GlobalRule rule) {
        this.manager = manager;
        this.rule = rule;
    }
    
    public void open(Player player) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#00bcd4:#0097a7><bold>Model Data Values</bold></gradient>")
                .rows(5)
                .item(0, BACK(), (g, e) -> manager.openGlobalRuleEditor(player, rule))
                .fillSlots(EMPTY(Material.GRAY_STAINED_GLASS_PANE),null,28,29,30,31,32,33,34)
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
                        open(player);
                    }
                })
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
                        open(player);
                    } else {
                        getDupe().getGuiListener().requestChatInput(g,player,"modelData","Input the the legacy model data ID number.");
                    }
                })
                .fillEmpty(EMPTY())
                .build();

        int slot = 28;
        for (Integer value : rule.legacyModelData.stream().limit(7).toList()) {
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
                open(player);
            });
        }

        gui.open(player);
    }
}
