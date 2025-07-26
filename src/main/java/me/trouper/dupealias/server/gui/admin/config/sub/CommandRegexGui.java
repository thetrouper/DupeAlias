package me.trouper.dupealias.server.gui.admin.config.sub;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.server.systems.gui.QuickPaginatedGUI;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.AdminPanelManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandRegexGui extends QuickPaginatedGUI<String> implements DupeContext, CommonItems {

    private final AdminPanelManager manager;

    public CommandRegexGui(AdminPanelManager manager) {
        this.manager = manager;
    }
    
    @Override
    protected String getTitle(Player player) {
        return "<red><bold>Final Command Regex</bold>";
    }

    @Override
    protected List<String> getAllItems(Player player) {
        return getConfig().finalCommandRegex;
    }

    @Override
    protected ItemStack createDisplayItem(String pattern) {
        return ItemBuilder.create(Material.PAPER)
                .displayName("<yellow>Blocked Pattern")
                .loreMiniMessage(Arrays.asList(
                        "<white>" + pattern,
                        "",
                        "<red>▶ <white>Right-click to remove"
                ))
                .build();
    }

    @Override
    public QuickGui createGUI(Player player) {
        QuickGui gui = super.createGUI(player);
        gui.updateItem(51,ItemBuilder.create(Material.LIME_DYE)
                .displayName("<green><bold>+ Add Regex Pattern</bold>")
                .loreMiniMessage(Arrays.asList(
                        "<gray>Add a new regex pattern for",
                        "<gray>commands to block with FINAL items",
                        "",
                        "<yellow>▶ <white>Click to add pattern"
                ))
                .build(),(g,e)->{
                QuickGui inputGui = QuickGui.create()
                        .titleMini("<gradient:#e91e63:#c2185b><bold>Set Regex Pattern</bold></gradient>")
                        .rows(3)
                        .callback("add_regex", new QuickGui.GuiCallback() {
                            @Override
                            public void onInput(QuickGui gui, Player player, String input, QuickGui.InputSource source) {
                                getConfig().finalCommandRegex.add(input);
                                getConfig().save();
                                successAny(player, "Added {0} to the final command regex.");
                                createGUI(player).open(player);
                            }
                        })
                        .item(13, ItemBuilder.create(Material.EXPERIENCE_BOTTLE)
                                .displayName("<light_purple><bold>Click me!")
                                .loreMiniMessage(Arrays.asList(
                                "<gray>Enter the regex of",
                                "<gray>the command to block"
                        ))
                                .build(), (q, ev) -> getDupe().getGuiListener().requestChatInput(q, player, "add_regex",
                                "<red>Enter a regex pattern for commands to block:\n<gray>Example: \"(?:itemname|iname)\"gmi"))
                        .item(22, BACK(), (q, ev) -> createGUI(player).open(player))
                        .fillEmpty(EMPTY())
                        .build();
                
                inputGui.open(player);
            });
        return gui;
    }

    @Override
    protected void handleItemClick(Player player, String regex, InventoryClickEvent e) {
        if (e.isRightClick()) {
            getConfig().finalCommandRegex.remove(regex);
            getConfig().save();
            successAny(player, "Removed regex pattern");
            createGUI(player).open(player);
        }
    }

    @Override
    protected void addFilterItems(QuickGui.GuiBuilder guiBuilder, Player player, Set<String> set) {

    }

    @Override
    protected void openBackGUI(Player player) {
        manager.openConfigGui(player);
    }
}
