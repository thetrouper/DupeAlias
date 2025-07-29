package me.trouper.dupealias.server.gui.admin.globalrule.criteria;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.GlobalRule;
import me.trouper.dupealias.data.ItemsAdderItem;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.AdminPanelManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GlobalRuleItemsAdderParser implements DupeContext, CommonItems {
    private final AdminPanelManager manager;
    private final GlobalRule rule;

    public GlobalRuleItemsAdderParser(AdminPanelManager manager, GlobalRule rule) {
        this.manager = manager;
        this.rule = rule;
    }

    public void open(Player player) {
        QuickGui gui = QuickGui.create()
                .rows(6)
                .titleMini("<gradient:#9b59b6:#8e44ad><bold>ItemsAdder Parsing Menu</bold></gradient>")
                .allowDrag()
                .onGlobalClick((g,e)->{
                    if (e.getSlot() >= 45) return;
                    e.setCancelled(false);
                })
                .fillSlots(EMPTY(),null,46,48,49,50,52)
                .item(45, BACK(), (g,e) -> manager.openGlobalRuleEditor(player,rule))
                .item(53, ItemBuilder.create(Material.LIGHT)
                        .displayName("<gold>Information")
                        .loreMiniMessage(
                                "<gray>Add custom items to this GUI and",
                                "<gray>click the green button to have",
                                "<gray>their namespaces parsed."
                        )
                        .build())
                .item(51,ItemBuilder.create(Material.LIME_DYE)
                                .displayName("<green>Parse Items")
                                .loreMiniMessage(
                                        "<gray>Click to add all items",
                                        "<gray>to the ItemsAdder material list"
                                )
                                .build(),
                        (g,e) -> {
                            Inventory inv = g.getInventory();
                            for (int row = 0; row < 5; row++) {
                                for (int col = 0; col < 9; col++) {
                                    int index = row * 9 + col;
                                    ItemStack item = inv.getItem(index);
                                    if (item == null || item.isEmpty() || !item.hasItemMeta()) continue;
                                    ItemsAdderItem iai;
                                    try {
                                        iai = new ItemsAdderItem(item);
                                    } catch (IllegalArgumentException ignored) {
                                        continue;
                                    }
                                    if (rule.effectedItemsAdderMaterials.contains(iai)) continue;
                                    rule.effectedItemsAdderMaterials.add(iai);
                                    inv.setItem(index,new ItemStack(Material.AIR));
                                }
                            }
                            getConfig().save();
                        })
                .item(47,generateItemsAdderListItem(rule),(g,e)->{
                    switch (e.getClick()) {
                        case SHIFT_RIGHT, SHIFT_LEFT -> {
                            rule.effectedItemsAdderMaterials.clear();
                            getConfig().save();
                            open(player);
                        }
                    }
                })
                .onClose((g,e)->{
                    getConfig().save();
                })
                .build();

        gui.open(player);
    }

    public ItemStack generateItemsAdderListItem(GlobalRule rule) {
        List<String> lore = new ArrayList<>();
        ItemBuilder builder = ItemBuilder.create(Material.PAPER)
                .displayName("<white><bold>Current Items");

        lore.add("<yellow>Total: <bold>" + rule.effectedItemsAdderMaterials.size());
        if (!rule.effectedMaterials.isEmpty()) {
            lore.add("");
            List<String> items = new ArrayList<>();

            rule.effectedItemsAdderMaterials.stream()
                    .limit(5)
                    .forEach((iai)->{
                        items.add("<dark_green>" + iai.namespace + "<gray>:<green>" + iai.id);
                    });

            for (String mat : items) {
                lore.add("<gray>• " + mat);
            }

            if (rule.effectedMaterials.size() > 5) {
                lore.add("<gray>... and " + (rule.effectedMaterials.size() - 5) + " more");
            }
        }

        if (rule.effectedItemsAdderMaterials.size() > 5) lore.add("<gray>and %s more...".formatted(rule.effectedItemsAdderMaterials.size() - 5));

        lore.add("<yellow>▶ <white>Shift Click to clear");

        return builder.loreMiniMessage(lore).build();
    }
}
