package me.trouper.dupealias.server.gui.admin;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.ItemTag;
import me.trouper.dupealias.server.gui.CommonItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class BulkTagGui implements DupeContext, CommonItems {

    private final AdminPanelManager manager;

    public BulkTagGui(AdminPanelManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#9b59b6:#8e44ad><bold>Bulk Tagging Menu</bold></gradient>")
                .allowDrag()
                .onGlobalClick((g,e)->{
                    if (e.getSlot() >= 45) return;
                    e.setCancelled(false);
                })
                .size(54)
                .fillSlots(EMPTY(),null,49,51,52)
                .item(53,BACK(),(g,e)->manager.openMainGui(player))

                .item(45, ItemBuilder.create(Material.EMERALD)
                                .displayName("<green><bold>UNIQUE Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Makes all items in the gui",
                                        "<red>unable to be duplicated",
                                        "",
                                        "<yellow>▶ <white>Left click to apply tag",
                                        "<yellow>▶ <white>Right click to remove tag",
                                        "<yellow>▶ <white>Shift click to set tag to false"
                                ))
                                .build(),
                        (g,e) -> handleTag(g.getInventory(),ItemTag.UNIQUE,e.getClick())
                )

                .item(46, ItemBuilder.create(Material.BARRIER)
                                .displayName("<red><bold>FINAL Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Makes all items in the gui",
                                        "<red>unable to be modified",
                                        "",
                                        "<yellow>▶ <white>Left click to apply tag",
                                        "<yellow>▶ <white>Right click to remove tag",
                                        "<yellow>▶ <white>Shift click to set tag to false"
                                ))
                                .build(),
                        (g,e) -> handleTag(g.getInventory(),ItemTag.FINAL,e.getClick()))



                .item(47, ItemBuilder.create(Material.WATER_BUCKET)
                                .displayName("<blue><bold>INFINITE Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Makes all items in the gui",
                                        "<green>always have max stack size",
                                        "",
                                        "<yellow>▶ <white>Left click to apply tag",
                                        "<yellow>▶ <white>Right click to remove tag",
                                        "<yellow>▶ <white>Shift click to set tag to false"
                                ))
                                .build(),
        (g,e) -> handleTag(g.getInventory(),ItemTag.INFINITE,e.getClick()))

                .item(48, ItemBuilder.create(Material.STRUCTURE_VOID)
                                .displayName("<dark_purple><bold>PROTECTED Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Makes all items in the gui",
                                        "<red>not able to be manually created",
                                        "",
                                        "<yellow>▶ <white>Left click to apply tag",
                                        "<yellow>▶ <white>Right click to remove tag",
                                        "<yellow>▶ <white>Shift click to set tag to false"
                                ))
                                .build(),
        (g,e) -> handleTag(g.getInventory(),ItemTag.PROTECTED,e.getClick()))

                .item(50, ItemBuilder.create(Material.TNT)
                                .displayName("<dark_red><bold>Remove All Tags</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Removes all tags from",
                                        "<gray>all items in the gui",
                                        "",
                                        "<red>⚠ <white>This cannot be undone!",
                                        "<yellow>▶ <white>Click to remove tags"
                                ))
                                .build(),
                        (g, e) -> removeTags(g.getInventory()))

                .build();

        gui.open(player);
    }

    private void handleTag(Inventory bulkGui, ItemTag tag, ClickType click) {
        switch (click) {
            case LEFT -> tagItems(bulkGui,tag,true);
            case RIGHT -> removeTag(bulkGui,tag);
            case SHIFT_LEFT, SHIFT_RIGHT -> tagItems(bulkGui,tag,false);
        }
    }

    private void removeTags(Inventory bulkGui) {
        for (ItemTag value : ItemTag.values()) {
            removeTag(bulkGui,value);
        }
    }

    private void removeTag(Inventory bulkGui, ItemTag tag) {
        for (int i = 0; i < 45; i++) {
            ItemStack toTag = bulkGui.getItem(i);
            if (toTag == null || toTag.isEmpty()) continue;
            getDupe().removeTag(toTag,tag);
        }
    }

    private void tagItems(Inventory bulkGui, ItemTag tag, boolean value) {
        for (int i = 0; i < 45; i++) {
            ItemStack toTag = bulkGui.getItem(i);
            if (toTag == null || toTag.isEmpty()) continue;
            getDupe().setTag(toTag,tag,value);
        }
    }
}
