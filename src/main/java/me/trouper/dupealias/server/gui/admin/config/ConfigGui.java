package me.trouper.dupealias.server.gui.admin.config;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.ItemTag;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.AdminPanelManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ConfigGui implements DupeContext, CommonItems {

    private final AdminPanelManager manager;

    public ConfigGui(AdminPanelManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {
        QuickGui gui = QuickGui.create()
                .titleMini("<dark_blue><bold>DupeAlias Config</bold></dark_blue>")
                .defaultTimeout(30000)
                .rows(5)
                .clickSound(Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)

                // Back button
                .item(0, BACK(), (g, e) -> manager.openMainGui(player))

                // Dupe Cooldown
                .callback("dupe_cooldown", new QuickGui.GuiCallback() {
                    @Override
                    public void onInput(QuickGui gui, Player player, String input, QuickGui.InputSource source) {
                        try {
                            long millis = Long.parseLong(input);
                            if (millis < 0) {
                                errorAny(player, "Cooldown cannot be negative!");
                                return;
                            }
                            infoAny(player, "You have set the dupe cooldown to {0}ms.", input);
                            getConfig().dupeCooldownMillis = millis;
                            getConfig().save();
                            open(player);
                        } catch (NumberFormatException ex) {
                            errorAny(player, "Please input a valid number of milliseconds.");
                            getDupe().getGuiListener().requestChatInput(gui, player, "dupe_cooldown", "Number format error, please input a valid value.");
                        }
                    }
                })
                .item(11, ItemBuilder.integerItem(Material.DIAMOND, "<aqua><bold>Dupe Command Cooldown</bold>", List.of(
                        "<gray>How long players have to wait",
                        "<gray>before running the /dupe command again.",
                        "",
                        "<yellow>Current: <white>" + getConfig().dupeCooldownMillis + "ms",
                        "",
                        "<yellow>▶ <white>Click to modify"
                ), (int) getConfig().dupeCooldownMillis), (g, e) ->
                        getDupe().getGuiListener().requestChatInput(g, player, "dupe_cooldown",
                                "<aqua>Insert a long value of Milliseconds.\n<gray>     1000ms = 1 Second\n\n<yellow>Current value: <white>" + getConfig().dupeCooldownMillis + "ms"))

                // Default Dupe GUI
                .item(12, ItemBuilder.create(Material.CHEST)
                        .displayName("<green><bold>Default Dupe GUI</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>The GUI type that opens when",
                                "<gray>players use the /dupe command",
                                "",
                                "<white>Options: <gray>REPLICATOR, INVENTORY, CHEST, MENU",
                                "",
                                "<yellow>Current: <white>" + getConfig().defaultDupeGui,
                                "",
                                "<yellow>▶ <white>Click to cycle options"
                        ))
                        .build(), (g, e) -> cycleDefaultGui(player))

                // Final Command Regex
                .item(13, ItemBuilder.create(Material.BARRIER)
                        .displayName("<red><bold>Final Command Regex</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Regex patterns for commands that",
                                "<gray>are blocked when holding FINAL items",
                                "",
                                "<yellow>Patterns: <white>" + getConfig().finalCommandRegex.size(),
                                "",
                                "<yellow>▶ <white>Click to manage patterns"
                        ))
                        .build(), (g, e) -> openFinalCommandRegexGui(player))

                // Global Rules Editor
                .item(14, ItemBuilder.create(Material.COMMAND_BLOCK)
                        .displayName("<light_purple><bold>Global Rules Editor</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Advanced rule system for applying",
                                "<gray>tags based on item properties",
                                "",
                                "<yellow>Rules: <white>" + getConfig().globalRules.size(),
                                "",
                                "<yellow>▶ <white>Click to manage rules"
                        ))
                        .build(), (g, e) -> openGlobalRulesGui(player))

                // Tag Lore Settings
                .item(15, ItemBuilder.create(Material.NAME_TAG)
                        .displayName("<yellow><bold>Tag Lore Settings</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Configure the lore text that",
                                "<gray>appears on tagged items",
                                "",
                                "<yellow>▶ <white>Click to configure lore"
                        ))
                        .build(), (g, e) -> openTagLoreGui(player, g))

                // Common Settings
                .item(22,ItemBuilder.create(Material.LIGHT)
                        .displayName("<gold><bold>Common Config</bold>")
                        .loreMiniMessage(
                                "<gray>Generic plugin configuration",
                                "<gray>Like plugin name and colors",
                                "",
                                "<yellow>▶ <white>Click to modify"
                        )
                        .build(),
                        (g,e) -> openCommonGui(player, g))

                // Replicator Settings
                .item(30, ItemBuilder.create(Material.REPEATER)
                        .displayName("<blue><bold>Replicator Settings</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Configure replicator GUI behavior",
                                "",
                                "<yellow>Refresh Delay: <white>" + getConfig().replicator.baseRefreshDelayTicks + " ticks",
                                "<yellow>Input Cooldown: <white>" + getConfig().replicator.baseInputCooldownTicks + " ticks",
                                "",
                                "<yellow>▶ <white>Click to configure"
                        ))
                        .build(), (g, e) -> openReplicatorGui(player, g))

                // Chest Settings
                .item(31, ItemBuilder.create(Material.CHEST)
                        .displayName("<gold><bold>Chest GUI Settings</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Configure chest GUI behavior",
                                "",
                                "<yellow>Refresh Delay: <white>" + getConfig().chest.baseRefreshDelayTicks + " ticks",
                                "",
                                "<yellow>▶ <white>Click to configure"
                        ))
                        .build(), (g, e) -> openChestGui(player, g))

                // Inventory Settings
                .item(32, ItemBuilder.create(Material.ENDER_CHEST)
                        .displayName("<dark_purple><bold>Inventory GUI Settings</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Configure inventory GUI behavior",
                                "",
                                "<yellow>Refresh Delay: <white>" + getConfig().inventory.baseRefreshDelayTicks + " ticks",
                                "",
                                "<yellow>▶ <white>Click to configure"
                        ))
                        .build(), (g, e) -> openInventoryGui(player, g))

                .fillEmpty(EMPTY())
                .build();

        gui.open(player);
    }

    private void openCommonGui(Player player, QuickGui g) {
        new CommonConfigGui(manager).open(player);
    }

    private void cycleDefaultGui(Player player) {
        String[] options = {"REPLICATOR", "INVENTORY", "CHEST", "MENU"};
        String current = getConfig().defaultDupeGui;

        int currentIndex = Arrays.asList(options).indexOf(current);
        int nextIndex = (currentIndex + 1) % options.length;

        getConfig().defaultDupeGui = options[nextIndex];
        getConfig().save();

        infoAny(player, "Default GUI changed to: {0}", options[nextIndex]);
        open(player);
    }

    private void openFinalCommandRegexGui(Player player) {
        new CommandRegexGui(manager).createGUI(player).open(player);
    }

    private void openGlobalRulesGui(Player player) {
        manager.openGlobalRuleList(player);
    }

    private void openTagLoreGui(Player player, QuickGui backGui) {
        QuickGui gui = QuickGui.create()
                .titleMini("<yellow><bold>Tag Lore Settings</bold>")
                .rows(1)
                .item(8, BACK(), (g, e) -> open(player))
                .fillEmpty(EMPTY())
                .build();

        int slot = 0;
        for (ItemTag tag : ItemTag.values()) {
            String trueLore = getConfig().trueTagLore.get(tag);
            String falseLore = getConfig().falseTagLore.get(tag);

            gui.updateItem(slot, ItemBuilder.create(getMaterialForTag(tag))
                    .displayName("<" + getTagColor(tag) + "><bold>" + tag.getName() + " Tag Lore</bold>")
                    .loreMiniMessage(Arrays.asList(
                            "<gray>True Lore: <white>" + (trueLore != null ? trueLore : "Not set"),
                            "<gray>False Lore: <white>" + (falseLore != null ? falseLore : "Not set"),
                            "",
                            "<yellow>▶ <white>Left-click to edit true lore",
                            "<yellow>▶ <white>Right-click to edit false lore"
                    ))
                    .build(), (g, e) -> editTagLore(player, tag, e.isLeftClick(), g, backGui));

            slot++;
        }

        gui.open(player);
    }

    private void editTagLore(Player player, ItemTag tag, boolean isTrue, QuickGui gui, QuickGui backGui) {
        String callbackId = "edit_lore_" + tag.name() + "_" + isTrue;

        gui.updateItem(0, gui.getSlotItems().get(0));

        QuickGui.GuiCallback callback = new QuickGui.GuiCallback() {
            @Override
            public void onInput(QuickGui gui, Player player, String input, QuickGui.InputSource source) {
                if (isTrue) {
                    getConfig().trueTagLore.put(tag, input);
                } else {
                    getConfig().falseTagLore.put(tag, input);
                }
                getConfig().save();
                successAny(player, "Updated {0} {1} lore", tag.getName(), isTrue ? "true" : "false");
                openTagLoreGui(player, backGui);
            }
        };

        // Create a temporary GUI with the callback
        QuickGui tempGui = QuickGui.create()
                .titleMini("<yellow>Editing " + tag.getName() + " Lore")
                .rows(1)
                .callback(callbackId, callback)
                .build();

        getDupe().getGuiListener().requestChatInput(tempGui, player, callbackId,
                "<yellow>Enter the " + (isTrue ? "true" : "false") + " lore for " + tag.getName() + ":\n" +
                        "<gray>Use MiniMessage format (e.g., <green>text</green>)\n" +
                        "<white>Current: " + (isTrue ? getConfig().trueTagLore.get(tag) : getConfig().falseTagLore.get(tag)));
    }

    private void openReplicatorGui(Player player, QuickGui backGui) {
        openTicksConfigGui(player, backGui, "Replicator", Material.REPEATER,
                getConfig().replicator.baseRefreshDelayTicks,
                getConfig().replicator.baseInputCooldownTicks,
                (refresh, input) -> {
                    getConfig().replicator.baseRefreshDelayTicks = refresh;
                    getConfig().replicator.baseInputCooldownTicks = input;
                });
    }

    private void openChestGui(Player player, QuickGui backGui) {
        openTicksConfigGui(player, backGui, "Chest", Material.CHEST,
                getConfig().chest.baseRefreshDelayTicks,
                null,
                (refresh, input) -> {
                    getConfig().chest.baseRefreshDelayTicks = refresh;
                });
    }

    private void openInventoryGui(Player player, QuickGui backGui) {
        openTicksConfigGui(player, backGui, "Inventory", Material.ENDER_CHEST,
                getConfig().inventory.baseRefreshDelayTicks,
                null,
                (refresh, input) -> {
                    getConfig().inventory.baseRefreshDelayTicks = refresh;
                });
    }

    private void openTicksConfigGui(Player player, QuickGui backGui, String name, Material icon,
                                    int refreshTicks, Integer inputTicks, TicksConfigSetter setter) {
        QuickGui gui = QuickGui.create()
                .titleMini("<blue><bold>" + name + " Settings</bold>")
                .rows(1)
                .item(8, BACK(), (g, e) -> open(player))

                .callback("refresh_ticks", new QuickGui.GuiCallback() {
                    @Override
                    public void onInput(QuickGui gui, Player player, String input, QuickGui.InputSource source) {
                        try {
                            int ticks = Integer.parseInt(input);
                            if (ticks < 1) {
                                errorAny(player, "Ticks must be at least 1!");
                                return;
                            }
                            setter.setRefresh(ticks, inputTicks);
                            getConfig().save();
                            successAny(player, "Set refresh delay to {0} ticks", ticks);
                            openTicksConfigGui(player, backGui, name, icon, ticks, inputTicks, setter);
                        } catch (NumberFormatException ex) {
                            errorAny(player, "Please input a valid number of ticks.");
                        }
                    }
                })

                .callback("input_ticks", new QuickGui.GuiCallback() {
                    @Override
                    public void onInput(QuickGui gui, Player player, String input, QuickGui.InputSource source) {
                        try {
                            int ticks = Integer.parseInt(input);
                            if (ticks < 1) {
                                errorAny(player, "Ticks must be at least 1!");
                                return;
                            }
                            setter.setRefresh(refreshTicks, ticks);
                            getConfig().save();
                            successAny(player, "Set input cooldown to {0} ticks", ticks);
                            openTicksConfigGui(player, backGui, name, icon, refreshTicks, ticks, setter);
                        } catch (NumberFormatException ex) {
                            errorAny(player, "Please input a valid number of ticks.");
                        }
                    }
                })

                .item(0, ItemBuilder.integerItem(icon, "<aqua><bold>Refresh Delay</bold>", List.of(
                        "<gray>How many ticks between item",
                        "<gray>replenishment in the GUI",
                        "",
                        "<yellow>Current: <white>" + refreshTicks + " ticks",
                        "<gray>(" + (refreshTicks / 20.0) + " seconds)",
                        "",
                        "<yellow>▶ <white>Click to modify"
                ), refreshTicks), (g, e) ->
                        getDupe().getGuiListener().requestChatInput(g, player, "refresh_ticks",
                                "<aqua>Enter the refresh delay in ticks:\n<gray>20 ticks = 1 second\n\n<yellow>Current: <white>" + refreshTicks + " ticks"))


                .fillEmpty(EMPTY())
                .build();

        if (inputTicks != null) {
            gui.updateItem(1, ItemBuilder.integerItem(Material.CLOCK, "<green><bold>Input Cooldown</bold>", List.of(
                    "<gray>How many ticks players must wait",
                    "<gray>before changing the input again",
                    "",
                    "<yellow>Current: <white>" + inputTicks + " ticks",
                    "<gray>(" + (inputTicks / 20.0) + " seconds)",
                    "",
                    "<yellow>▶ <white>Click to modify"
            ), inputTicks), (g, e) -> {
                getDupe().getGuiListener().requestChatInput(g, player, "input_ticks",
                        "<green>Enter the input cooldown in ticks:\n<gray>20 ticks = 1 second\n\n<yellow>Current: <white>" + inputTicks + " ticks");
            });
        }

        gui.open(player);
    }

    private Material getMaterialForTag(ItemTag tag) {
        return switch (tag) {
            case UNIQUE -> Material.EMERALD_BLOCK;
            case FINAL -> Material.REDSTONE_BLOCK;
            case INFINITE -> Material.LAPIS_BLOCK;
            case PROTECTED -> Material.STRUCTURE_BLOCK;
        };
    }

    private String getTagColor(ItemTag tag) {
        return switch (tag) {
            case UNIQUE -> "green";
            case FINAL -> "red";
            case INFINITE -> "blue";
            case PROTECTED -> "dark_purple";
        };
    }

    @FunctionalInterface
    private interface TicksConfigSetter {
        void setRefresh(int refreshTicks, Integer inputTicks);
    }
}