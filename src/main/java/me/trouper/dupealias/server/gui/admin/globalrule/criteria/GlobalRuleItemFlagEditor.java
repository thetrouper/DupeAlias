package me.trouper.dupealias.server.gui.admin.globalrule.criteria;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.GlobalRule;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.AdminPanelManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class GlobalRuleItemFlagEditor implements DupeContext, CommonItems {

    private final AdminPanelManager manager;
    private final GlobalRule rule;

    public GlobalRuleItemFlagEditor(AdminPanelManager manager, GlobalRule rule) {
        this.manager = manager;
        this.rule = rule;
    }

    @SuppressWarnings("deprecation")
    public void open(Player player) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#ff9800:#f57c00><bold>Item Flag Criteria</bold></gradient>")
                .rows(4)
                .fillBorder(EMPTY(Material.ORANGE_STAINED_GLASS_PANE))

                // Back button
                .item(0, BACK(), (g, e) -> manager.openGlobalRuleEditor(player, rule))

                // Item flags
                .item(10, createFlagItem(ItemFlag.HIDE_ENCHANTS),
                        (g, e) -> toggleFlag(player, ItemFlag.HIDE_ENCHANTS))
                .item(11, createFlagItem(ItemFlag.HIDE_ATTRIBUTES),
                        (g, e) -> toggleFlag(player, ItemFlag.HIDE_ATTRIBUTES))
                .item(12, createFlagItem(ItemFlag.HIDE_UNBREAKABLE),
                        (g, e) -> toggleFlag(player, ItemFlag.HIDE_UNBREAKABLE))
                .item(13, createFlagItem(ItemFlag.HIDE_DESTROYS),
                        (g, e) -> toggleFlag(player, ItemFlag.HIDE_DESTROYS))
                .item(14, createFlagItem(ItemFlag.HIDE_PLACED_ON),
                        (g, e) -> toggleFlag(player, ItemFlag.HIDE_PLACED_ON))
                .item(15, createFlagItem(ItemFlag.HIDE_ADDITIONAL_TOOLTIP),
                        (g, e) -> toggleFlag(player, ItemFlag.HIDE_ADDITIONAL_TOOLTIP))
                .item(16, createFlagItem(ItemFlag.HIDE_DYE),
                        (g, e) -> toggleFlag(player, ItemFlag.HIDE_DYE))
                .item(19, createFlagItem(ItemFlag.HIDE_ARMOR_TRIM),
                        (g, e) -> toggleFlag(player, ItemFlag.HIDE_ARMOR_TRIM))
                .item(20, createFlagItem(ItemFlag.HIDE_STORED_ENCHANTS),
                        (g, e) -> toggleFlag(player, ItemFlag.HIDE_STORED_ENCHANTS))

                // Clear all button
                .item(22, ItemBuilder.create(Material.BARRIER)
                        .displayName("<red><bold>Clear All Flags")
                        .loreMiniMessage(Arrays.asList(
                                "<gray>Remove all flag requirements",
                                "",
                                "<yellow>▶ <white>Click to clear"
                        ))
                        .build(), (g, e) -> {
                    rule.itemFlags.clear();
                    getConfig().save();
                    successAny(player, "Cleared all item flag requirements");
                    open(player);
                })

                .fillEmpty(EMPTY())
                .clickSound(Sound.UI_BUTTON_CLICK, 0.7f, 1.2f)
                .build();

        gui.open(player);
    }

    private ItemStack createFlagItem(ItemFlag flag) {
        boolean required = rule.itemFlags.contains(flag);

        return ItemBuilder.create(required ? Material.WHITE_BANNER : Material.LIME_BANNER)
                .displayName((required ? "<yellow>" : "<gray>") + "<bold>" + flag.name())
                .loreMiniMessage(Arrays.asList(
                        "<gray>" + getFlagDescription(flag),
                        "",
                        "<white>Status: " + (required ? "<yellow>REQUIRED" : "<gray>NOT REQUIRED"),
                        "",
                        "<yellow>▶ <white>Click to toggle"
                ))
                .build();
    }

    private String getFlagDescription(ItemFlag flag) {
        return switch (flag) {
            case HIDE_ENCHANTS -> "Hides enchantments from item tooltip";
            case HIDE_ATTRIBUTES -> "Hides attribute modifiers";
            case HIDE_UNBREAKABLE -> "Hides the unbreakable tag";
            case HIDE_DESTROYS -> "Hides what blocks can be destroyed";
            case HIDE_PLACED_ON -> "Hides what blocks can be placed on";
            case HIDE_ADDITIONAL_TOOLTIP -> "Hides additional tooltip info";
            case HIDE_DYE -> "Hides dye color from items";
            case HIDE_ARMOR_TRIM -> "Hides armor trim information";
            case HIDE_STORED_ENCHANTS -> "Hides stored enchantments (books)";
        };
    }

    private void toggleFlag(Player player, ItemFlag flag) {
        if (rule.itemFlags.contains(flag)) {
            rule.itemFlags.remove(flag);
            infoAny(player, "Removed {0} requirement", flag.name());
        } else {
            rule.itemFlags.add(flag);
            successAny(player, "Added {0} requirement", flag.name());
        }
        getConfig().save();
        open(player);
    }
}