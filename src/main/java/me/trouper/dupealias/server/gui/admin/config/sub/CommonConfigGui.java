package me.trouper.dupealias.server.gui.admin.config.sub;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.files.CommonConfig;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.AdminPanelManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class CommonConfigGui implements DupeContext, CommonItems {

    private final AdminPanelManager manager;

    public CommonConfigGui(AdminPanelManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {
        CommonConfig config = getCommonConfig();

        QuickGui gui = QuickGui.create()
                .titleMini("<dark_aqua><bold>Common Config</bold></dark_aqua>")
                .defaultTimeout(30000)
                .rows(3)
                .clickSound(Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)

                .item(0, BACK(), (g, e) -> manager.openMainGui(player))

                .callback("main_color", (g, p, input, source) -> {
                    try {
                        int color = Integer.parseInt(input.replace("#", ""), 16);
                        config.mainColor = color;
                        config.save();

                        getInstance().getCommon().update(config.generateCommon());
                        successAny(p, "Main color set to <#{0}>#{0}</#{0}>.", Integer.toHexString(color));
                        open(p);
                    } catch (NumberFormatException ex) {
                        errorAny(p, "Invalid hex color format. Use RRGGBB (e.g., AAAAFF).");
                    }
                })
                .item(11, ItemBuilder.create(Material.BLUE_WOOL)
                        .displayName("<blue><bold>Main Color</bold>")
                        .loreMiniMessage(List.of(
                                "<gray>The color for the message border.",
                                "",
                                String.format("<yellow>Current: <#%s>#%s</#%s>", Integer.toHexString(config.mainColor), Integer.toHexString(config.mainColor), Integer.toHexString(config.mainColor)),
                                "",
                                "<yellow>▶ <white>Click to modify"
                        )).build(), (g, e) ->

                        getDupe().getGuiListener().requestChatInput(g, player, "main_color",
                                "<blue>Enter a hex color code for the main color.\n<gray>Example: AAAAFF"))

                .callback("secondary_color", (g, p, input, source) -> {
                    try {
                        int color = Integer.parseInt(input.replace("#", ""), 16);
                        config.secondaryColor = color;
                        config.save();
                        getInstance().getCommon().update(config.generateCommon());
                        successAny(p, "Secondary color set to <#{0}>#{0}</#{0}>.", Integer.toHexString(color));
                        open(p);
                    } catch (NumberFormatException ex) {
                        errorAny(p, "Invalid hex color format. Use RRGGBB (e.g., 00DDFF).");
                    }
                })
                .item(12, ItemBuilder.create(Material.CYAN_WOOL)
                        .displayName("<aqua><bold>Secondary Color</bold>")
                        .loreMiniMessage(List.of(
                                "<gray>The color used for the plugin's name.",
                                "",
                                String.format("<yellow>Current: <#%s>#%s</#%s>", Integer.toHexString(config.secondaryColor), Integer.toHexString(config.secondaryColor), Integer.toHexString(config.secondaryColor)),
                                "",
                                "<yellow>▶ <white>Click to modify"
                        )).build(), (g, e) ->
                        getDupe().getGuiListener().requestChatInput(g, player, "secondary_color",
                                "<aqua>Enter a hex color code for the secondary color.\n<gray>Example: 00DDFF"))

                .callback("plugin_name", (g, p, input, source) -> {
                    config.pluginName = input;
                    config.save();
                    getInstance().getCommon().update(config.generateCommon());
                    successAny(p, "Plugin name set to: {0}", input);
                    open(p);
                })
                .item(13, ItemBuilder.create(Material.NAME_TAG)
                        .displayName("<green><bold>Plugin Name</bold>")
                        .loreMiniMessage(List.of(
                                "<gray>The name of the plugin displayed in messages.",
                                "",
                                "<yellow>Current: <white>" + config.pluginName,
                                "",
                                "<yellow>▶ <white>Click to modify"
                        )).build(), (g, e) ->
                        getDupe().getGuiListener().requestChatInput(g, player, "plugin_name",
                                "<green>Enter the new plugin name."))

                .callback("flat_prefix", (g, p, input, source) -> {
                    config.flatPrefix = input;
                    config.save();
                    getInstance().getCommon().update(config.generateCommon());
                    successAny(p, "Flat prefix set to: {0}", input);
                    open(p);
                })
                .item(14, ItemBuilder.create(Material.PAPER)
                        .displayName("<gray><bold>Flat Prefix</bold>")
                        .loreMiniMessage(List.of(
                                "<gray>The prefix used when 'flat' mode is enabled.",
                                "<gray>Uses legacy '&' color codes.",
                                "",
                                "<yellow>Current: <white>" + config.flatPrefix,
                                "",
                                "<yellow>▶ <white>Click to modify"
                        )).build(), (g, e) ->
                        getDupe().getGuiListener().requestChatInput(g, player, "flat_prefix",
                                "<gray>Enter the new flat prefix."))

                .item(15, ItemBuilder.create(config.flat ? Material.LIME_DYE : Material.GRAY_DYE)
                        .displayName("<white><bold>Flat Mode</bold>")
                        .loreMiniMessage(List.of(
                                "<gray>If true, uses the simple flat message system",
                                "<gray>instead of the complex line wrapping feature.",
                                "",
                                "<yellow>Current: <white>" + config.flat,
                                "",
                                "<yellow>▶ <white>Click to toggle"
                        )).build(), (g, e) -> {
                    config.flat = !config.flat;
                    config.save();
                    getInstance().getCommon().update(config.generateCommon());
                    infoAny(player, "Flat mode set to: {0}", config.flat);
                    open(player);
                })

                .fillEmpty(EMPTY())
                .build();

        gui.open(player);
    }
}
