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

public class GlobalRuleLoreEditor implements DupeContext, CommonItems {
    private final AdminPanelManager manager;
    private final GlobalRule rule;

    public GlobalRuleLoreEditor(AdminPanelManager manager, GlobalRule rule) {
        this.manager = manager;
        this.rule = rule;
    }

    public void open(Player player) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#9c27b0:#7b1fa2><bold>Lore Contains</bold></gradient>")
                .rows(3)
                .item(0, BACK(), (g, e) -> manager.openGlobalRuleEditor(player, rule))
                .callback("lorePattern", new QuickGui.GuiCallback() {
                    @Override
                    public void onInput(QuickGui gui, Player player, String input, QuickGui.InputSource source) {
                        rule.loreContainsRegex = input;
                        getConfig().save();
                        successAny(player, "Set lore pattern to: " + input);
                        manager.openGlobalRuleEditor(player, rule);
                    }
                })
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
                        manager.openGlobalRuleEditor(player, rule);
                    } else {
                        getDupe().getGuiListener().requestChatInput(g,player,"lorePattern","Input a regex pattern for matching item lore (by line).");
                    }
                })

                .fillEmpty(EMPTY())

                .build();
        gui.open(player);
    }
}
