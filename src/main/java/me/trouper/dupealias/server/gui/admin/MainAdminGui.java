package me.trouper.dupealias.server.gui.admin;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.config.ConfigGui;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class MainAdminGui implements CommonItems {

    private final AdminPanelManager manager;

    public MainAdminGui(AdminPanelManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#6b6bff:#9999ff><bold>DupeAlias Admin Panel</gradient>")
                .rows(5)

                .item(11, ItemBuilder.create(player.getInventory().getItemInMainHand().isEmpty() ? Material.BARRIER : Material.DIAMOND_SWORD)
                                .displayName("<gradient:#4ecdc4:#45b7d1><bold>Held Item Actions</bold></gradient>")
                                .loreMiniMessage(
                                        "<gray>Manage tags for the item",
                                        "<gray>you're currently holding",
                                        "",
                                        "<yellow>▶ <white>Click to open menu"
                                )
                                .hideAllFlags()
                                .build(),
                        (q, event) -> manager.openHeldItemGui(player))

                .item(13, ItemBuilder.create(Material.BOOKSHELF)
                                .displayName("<gradient:#ff6b6b:#ffa726><bold>Global Rules</bold></gradient>")
                                .loreMiniMessage(
                                        "<gray>Configure global rules to apply",
                                        "<gray>tags based on item properties",
                                        "",
                                        "<white>Rules: <yellow>" + getConfig().globalRules.size(),
                                        "",
                                        "<yellow>▶ <white>Click to manage rules"
                                )
                                .build(),
                        (q, event) -> manager.openGlobalRuleList(player))

                .item(15, ItemBuilder.create(Material.KNOWLEDGE_BOOK)
                                .displayName("<gradient:#cb59b6:#8e44ad><bold>Information & Help</bold></gradient>")
                                .loreMiniMessage(
                                        "<gray>Learn about item tags and",
                                        "<gray>how to use this system",
                                        "",
                                        "<yellow>▶ <white>Click to view help"
                                )
                                .build(),
                        (q, event) -> manager.openHelpGui(player))

                .item(29, ItemBuilder.create(Material.COMPARATOR)
                        .displayName("<gradient:#ff6bff:#ffa7ff><bold>Configuration</bold></gradient>")
                        .loreMiniMessage(
                                "<gray>Modify plugin parameters",
                                "<gray>name and colors",
                                "",
                                "<yellow>▶ <white>Click to open config"
                        )
                        .build(), (q,event) -> manager.openConfigGui(player))

                .item(31, manager.createPreviewItem(player.getInventory().getItemInMainHand()))

                .item(33, ItemBuilder.create(Material.DIAMOND)
                        .displayName("<#AAAAFF><bold>Dupe<#00DDFF>Alias</bold> <white>Credits")
                        .loreMiniMessage(
                                "<dark_gray><bold>|</bold><gray> Built with Alias Development Kit",
                                "<dark_gray><bold>|</bold><gray>",
                                "<dark_gray><bold>|</bold> <gradient:#e38c01:#eccd00:#FFFFFF:#62afdd:#1f3857>Written by obvWolf</gradient>",
                                " ",
                                "<dark_gray>Copyright © 2025 DupeAlias",
                                "<dark_gray>Do Not Redistribute"
                        )
                        .build())

                .fillEmpty(EMPTY())
                .clickSound(Sound.UI_BUTTON_CLICK, 0.7f, 1.2f)
                .build();

        gui.open(player);
    }
}