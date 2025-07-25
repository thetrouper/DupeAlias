package me.trouper.dupealias.server.gui.admin;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.ItemTag;
import me.trouper.dupealias.server.gui.CommonItems;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class HeldItemGui implements DupeContext, CommonItems {

    private final AdminPanelManager manager;

    public HeldItemGui(AdminPanelManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.getType().isAir()) {
            errorAny(player, "You must be holding an item to use this menu!");
            return;
        }

        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#4ecdc4:#45b7d1><bold>Held Item: " + heldItem.getType().name() + "</bold></gradient>")
                .rows(4)
                .fillBorder(EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE))

                .item(0, BACK(),
                        (g, e) -> manager.openMainGui(player))

                .item(13, ItemBuilder.create(heldItem.getType())
                        .displayName("<white><bold>" + heldItem.getType().name() + "</bold>")
                        .loreMiniMessage(manager.getItemTagStatus(heldItem))
                        .build())

                .item(11, ItemBuilder.create(Material.EMERALD)
                                .displayName("<green><bold>Add UNIQUE Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Makes this specific item",
                                        "<red>unable to be duplicated",
                                        "",
                                        "<yellow>▶ <white>Left click to apply tag",
                                        "<yellow>▶ <white>Right click to remove tag",
                                        "<yellow>▶ <white>Shift click to set tag to false"
                                ))
                                .build(),
                        (g, e) -> tagHeldItem(player, ItemTag.UNIQUE, e.getClick()))

                .item(20, ItemBuilder.create(Material.BARRIER)
                                .displayName("<red><bold>Add FINAL Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Makes this specific item",
                                        "<red>unable to be modified",
                                        "",
                                        "<yellow>▶ <white>Left click to apply tag",
                                        "<yellow>▶ <white>Right click to remove tag",
                                        "<yellow>▶ <white>Shift click to set tag to false"
                                ))
                                .build(),
                        (g, e) -> tagHeldItem(player, ItemTag.FINAL, e.getClick()))

                .item(15, ItemBuilder.create(Material.WATER_BUCKET)
                                .displayName("<blue><bold>Add INFINITE Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Makes this specific item",
                                        "<green>always have max stack size",
                                        "",
                                        "<yellow>▶ <white>Left click to apply tag",
                                        "<yellow>▶ <white>Right click to remove tag",
                                        "<yellow>▶ <white>Shift click to set tag to false"
                                ))
                                .build(),
                        (g, e) -> tagHeldItem(player, ItemTag.INFINITE, e.getClick()))

                .item(24, ItemBuilder.create(Material.STRUCTURE_VOID)
                                .displayName("<dark_purple><bold>Add PROTECTED Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Makes this specific item",
                                        "<red>not able to be manually created",
                                        "",
                                        "<yellow>▶ <white>Left click to apply tag",
                                        "<yellow>▶ <white>Right click to remove tag",
                                        "<yellow>▶ <white>Shift click to set tag to false"
                                ))
                                .build(),
                        (g, e) -> tagHeldItem(player, ItemTag.PROTECTED, e.getClick()))

                .item(22, ItemBuilder.create(Material.TNT)
                                .displayName("<dark_red><bold>Remove All Tags</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Removes all tags from",
                                        "<gray>this specific item",
                                        "",
                                        "<red>⚠ <white>This cannot be undone!",
                                        "<yellow>▶ <white>Click to remove tags"
                                ))
                                .build(),
                        (g, e) -> removeAllTagsFromHeld(player))

                .fillEmpty(EMPTY())
                .clickSound(Sound.UI_BUTTON_CLICK, 0.7f, 1.2f)
                .build();

        gui.open(player);
    }

    private void tagHeldItem(Player player, ItemTag tag, ClickType click) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.getType().isAir()) {
            errorAny(player, "You must be holding an item to tag it!");
            return;
        }

        switch (click) {
            case LEFT -> {
                getDupe().addTag(heldItem, tag);
                successAny(player, "Added {0} tag to your {1}. {2}", tag.getName(), heldItem.getType(), tag.getDesc());
            }
            case RIGHT -> {
                getDupe().removeTag(heldItem, tag);
                successAny(player, "Removed {0} tag from your {1}.", tag.getName(), heldItem.getType());
            }
            case SHIFT_LEFT, SHIFT_RIGHT -> {
                getDupe().setTag(heldItem, tag, false);
                successAny(player, "Set {0} tag from your {1} to {2}.", tag.getName(), heldItem.getType(), "false");
            }
        }

        player.closeInventory();
        open(player); // Re-open the GUI to update
    }

    private void removeAllTagsFromHeld(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.getType().isAir()) {
            errorAny(player, "You must be holding an item to remove tags from it!");
            return;
        }

        for (ItemTag tag : ItemTag.values()) {
            getDupe().removeTag(heldItem, tag);
        }

        successAny(player, "Removed all tags from your {0}.", heldItem.getType());

        player.closeInventory();
        open(player); // Re-open the GUI to update
    }
}