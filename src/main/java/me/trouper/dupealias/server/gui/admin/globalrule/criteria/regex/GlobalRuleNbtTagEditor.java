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

public class GlobalRuleNbtTagEditor implements DupeContext, CommonItems {
    private final AdminPanelManager manager;
    private final GlobalRule rule;

    public GlobalRuleNbtTagEditor(AdminPanelManager manager, GlobalRule rule) {
        this.manager = manager;
        this.rule = rule;
    }

    public void open(Player player) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#9c27b0:#7b1fa2><bold>NBT Tag Contains</bold></gradient>")
                .rows(3)
                .item(0, BACK(), (g, e) -> manager.openGlobalRuleEditor(player, rule))
                .callback("nbtPattern", new QuickGui.GuiCallback() {
                    @Override
                    public void onInput(QuickGui gui, Player player, String input, QuickGui.InputSource source) {
                        rule.nbtTagContainsRegex = input;
                        getConfig().save();
                        successAny(player, "Set NBT pattern to: " + input);
                        manager.openGlobalRuleEditor(player, rule);
                    }
                })
                .item(13, ItemBuilder.create(Material.WRITABLE_BOOK)
                        .displayName("<light_purple><bold>Current Pattern")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Set a regex pattern to match",
                                "<gray>against mojangson NBT",
                                "",
                                "<white>Current: <light_purple>" + (rule.loreContainsRegex.isEmpty() ? "Not set" : rule.loreContainsRegex),
                                "",
                                "<yellow>▶ <white>Click to set pattern",
                                "<yellow>▶ <white>Right-click to clear"
                        ))
                        .build(), (g, e) -> {
                    if (e.isRightClick()) {
                        rule.nbtTagContainsRegex = "";
                        getConfig().save();
                        successAny(player, "Cleared lore pattern");
                        manager.openGlobalRuleEditor(player, rule);
                    } else {
                        getDupe().getGuiListener().requestChatInput(g,player,"nbtPattern","Input a regex pattern for matching nbt (mojangson).");
                    }
                })

                .fillEmpty(EMPTY())

                .build();
        gui.open(player);
    }
}
