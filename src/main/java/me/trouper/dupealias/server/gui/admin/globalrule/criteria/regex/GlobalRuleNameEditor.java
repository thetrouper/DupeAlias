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

public class GlobalRuleNameEditor implements DupeContext, CommonItems {
    private final AdminPanelManager manager;
    private final GlobalRule rule;

    public GlobalRuleNameEditor(AdminPanelManager manager, GlobalRule rule) {
        this.manager = manager;
        this.rule = rule;
    }

    public void open(Player player) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#ffeb3b:#ffc107><bold>Name Contains</bold></gradient>")
                .rows(3)
                .item(0, BACK(), (g, e) -> manager.openGlobalRuleEditor(player, rule))
                .callback("namePattern", new QuickGui.GuiCallback() {
                    @Override
                    public void onInput(QuickGui gui, Player player, String input, QuickGui.InputSource source) {
                        rule.nameContainsRegex = input;
                        getConfig().save();
                        successAny(player, "Set name pattern to: " + input);
                        manager.openGlobalRuleEditor(player, rule);
                    }
                })
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
                        manager.openGlobalRuleEditor(player, rule);
                    } else {
                        getDupe().getGuiListener().requestChatInput(g,player,"namePattern","Input a regex pattern for matching item names.");
                    }
                })
                .fillEmpty(EMPTY())
                .build();

        gui.open(player);
    }
}
