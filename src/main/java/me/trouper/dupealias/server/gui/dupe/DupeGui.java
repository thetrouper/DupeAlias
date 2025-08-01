package me.trouper.dupealias.server.gui.dupe;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.dupe.sub.AbstractDupeGui;
import me.trouper.dupealias.server.gui.dupe.sub.DupeChestGui;
import me.trouper.dupealias.server.gui.dupe.sub.DupeInventoryGui;
import me.trouper.dupealias.server.gui.dupe.sub.DupeReplicatorGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DupeGui implements DupeContext, CommonItems {

    public final DupeReplicatorGui replicatorGui = new DupeReplicatorGui();
    public final DupeInventoryGui inventoryGui  = new DupeInventoryGui();
    public final DupeChestGui chestGui = new DupeChestGui();

    public void openMainGui(Player player) {
        if (!player.hasPermission("dupealias.gui")) {
            warningAny(player,dict().guiDupe.noPermission);
            return;
        }

        QuickGui gui = QuickGui.create()
                .rows(5)
                .titleMini(dict().guiDupe.title)
                .item(20,
                        permissionItem(
                                player,
                                "dupealias.gui.replicator",
                                ItemBuilder.of(Material.DISPENSER)
                                .displayName(dict().guiDupe.replicatorName)
                                .loreMiniMessage(dict().guiDupe.replicatorLore)
                        ),
                        openSession(replicatorGui,"dupealias.gui.replicator"))

                .item(22,permissionItem(
                        player,
                                "dupealias.gui.inventory",
                                ItemBuilder.of(Material.NETHERITE_CHESTPLATE)
                                .displayName(dict().guiDupe.inventoryName)
                                .loreMiniMessage(dict().guiDupe.inventoryLore)
                        ),
                        openSession(inventoryGui,"dupealias.gui.inventory"))

                .item(24,permissionItem(
                        player,
                        "dupealias.gui.chest",
                                ItemBuilder.of(Material.ENDER_CHEST)
                                        .displayName(dict().guiDupe.chestName)
                                        .loreMiniMessage(dict().guiDupe.chestLore)
                        ),
                        openSession(chestGui,"dupealias.gui.chest"))

                .fillBorder(EMPTY(Material.PURPLE_STAINED_GLASS_PANE))
                .fillEmpty(EMPTY(Material.WHITE_STAINED_GLASS_PANE))
                .build();

        gui.open(player);
    }

    private ItemStack permissionItem(Player player, String permission, ItemBuilder builder) {
        if (player.hasPermission(permission)) {
            return builder.build();
        } else {
            return builder.displayName(dict().guiDupe.noDupeGuiName)
                    .loreMiniMessage(
                            dict().guiDupe.noDupeGuiLore
                                    .stream()
                                    .map(line->line.replace("{0}",permission.replace("dupealias.gui.","")))
                                    .toList()
                    ).build().withType(Material.BARRIER);
        }
    }

    private QuickGui.GuiAction openSession(AbstractDupeGui<?> abstractDupeGui, String guiPermission) {
        return (gui, event) -> {
            openIfPermission((Player) event.getWhoClicked(),abstractDupeGui,guiPermission);
        };
    }

    public void openIfPermission(Player player, AbstractDupeGui<?> abstractDupeGui, String guiPermission) {
        if (player.hasPermission(guiPermission)) {
            if (player.hasPermission(guiPermission + ".keep")) {
                getVerbose().send("Opening existing session for {0}",player.getName());
                abstractDupeGui.getSession(player).getGui().open(player);
            } else {
                getVerbose().send("Creating new session for {0}",player.getName());
                player.openInventory(abstractDupeGui.getSession(player).open());
            }
        } else {
            player.closeInventory();
            warningAny(player,dict().guiDupe.noSpecificPermission);
        }
    }

    public void openDefaultGui(Player player) {
        switch (getConfig().defaultDupeGui) {
            case "REPLICATOR" -> openIfPermission(player,replicatorGui,"dupealias.gui.replicator");
            case "INVENTORY" -> openIfPermission(player,inventoryGui,"dupealias.gui.inventory");
            case "CHEST" -> openIfPermission(player,chestGui,"dupealias.gui.chest");
            case "MENU" -> openMainGui(player);
            default -> {
                infoAny(player,dict().guiDupe.noDefaultGui);
            }
        }
    }
}
