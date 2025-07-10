package me.trouper.dupealias.server.gui.dupe;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.gui.CommonItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DupeGui implements DupeContext, CommonItems {

    public final ReplicatorGui replicatorGui = new ReplicatorGui();
    public final DupeInventoryGui inventoryGui  = new DupeInventoryGui();
    public final DupeChestGui chestGui = new DupeChestGui();

    public void openMainGui(Player player) {
        QuickGui gui = QuickGui.create()
                .rows(5)
                .titleMini("<aqua><bold>Available GUIs")
                .item(20,
                        permissionItem(
                                player,
                                "dupealias.gui.replicator",
                                ItemBuilder.of(Material.DISPENSER)
                                .displayName("<blue>Replicator GUI")
                                .loreMiniMessage("<gray>Open the single-item dupe GUI.")
                        ),
                        openSession(replicatorGui,"dupealias.gui.replicator"))

                .item(22,permissionItem(
                        player,
                                "dupealias.gui.inventory",
                                ItemBuilder.of(Material.NETHERITE_CHESTPLATE)
                                .displayName("<yellow>Inventory GUI")
                                .loreMiniMessage("<gray>Open a mirror of your own inventory.")
                        ),
                        openSession(inventoryGui,"dupealias.gui.inventory"))

                .item(24,permissionItem(
                        player,
                        "dupealias.gui.chest",
                                ItemBuilder.of(Material.ENDER_CHEST)
                                        .displayName("<green>Chest GUI")
                                        .loreMiniMessage("<gray>Open the multi-item dupe GUI.")
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
            Component name = builder.build().effectiveName();
            return builder.displayName("<dark_red>Unavailable GUI")
                    .loreMiniMessage()
                    .loreComponent(Component.text("You lack the permission to",NamedTextColor.RED),Component.text("use the ", NamedTextColor.RED).decoration(TextDecoration.ITALIC,false).append(name).append(Component.text(".")))
                    .build().withType(Material.BARRIER);
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
                player.openInventory(abstractDupeGui.createSession(player).open());
            }
        } else {
            player.closeInventory();
            warningAny(player,"You do not have permission to use that GUI!");
        }
    }

    public void openDefaultGui(Player player) {
        switch (getConfig().defaultDupeGui) {
            case "REPLICATOR" -> openIfPermission(player,replicatorGui,"dupealias.gui.replicator");
            case "INVENTORY" -> openIfPermission(player,inventoryGui,"dupealias.gui.inventory");
            case "CHEST" -> openIfPermission(player,chestGui,"dupealias.gui.chest");
            case "MENU" -> openMainGui(player);
            default -> {
                infoAny(player,"There is currently no default Dupe GUI.");
            }
        }
    }
}
