package me.trouper.dupealias.server.gui.dupe;

import me.trouper.alias.server.systems.gui.QuickGui;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;

public class DupeInventoryGui extends AbstractDupeGui<DupeInventoryGui.InventorySession> {

    @Override
    protected InventorySession createSession(Player player) {
        return new InventorySession(player);
    }

    public class InventorySession extends AbstractDupeSession {

        public InventorySession(Player owner) {
            super(owner, "<gradient:#cc22ff:#cc99ff><bold>YOUR INVENTORY</gradient>", 6);
        }

        @Override
        protected QuickGui buildGui(String title, int rows) {
            return QuickGui.create()
                    .titleMini(title)
                    .rows(rows)
                    .onGlobalClick((g, e) -> e.setCancelled(
                            shouldBlockClick(e.getCursor()) ||
                                    shouldBlockClick(e.getCurrentItem()) ||
                                    (e.getClickedInventory() != null && shouldBlockClick(e.getClickedInventory().getItem(e.getSlot())))
                    ))
                    .allowDrag()
                    .onCreate((g, i) -> populateInventory(getOwner(), i))
                    .onClose((g, e) -> close())
                    .build();
        }

        @Override
        protected void tick() {
            populateInventory(getOwner(), getGui().getInventory());
        }

        @Override
        protected long getTickDelay(Player player) {
            return 1;
        }
    }

    private void populateInventory(Player player, Inventory inv) {
        EntityEquipment equipment = player.getEquipment();
        for (int i = 0; i < 18; i++) {
            inv.setItem(i, EMPTY());
        }

        inv.setItem(0, createPopulatedItem(equipment.getHelmet()));
        inv.setItem(1, createPopulatedItem(equipment.getChestplate()));
        inv.setItem(2, createPopulatedItem(equipment.getLeggings()));
        inv.setItem(3, createPopulatedItem(equipment.getBoots()));
        inv.setItem(6, createPopulatedItem(equipment.getItemInOffHand()));

        for (int i = 0; i < 9; i++) {
            inv.setItem(i + 18, createPopulatedItem(player.getInventory().getItem(i)));
        }
        for (int i = 27; i < 36; i++) {
            inv.setItem(i, createPopulatedItem(player.getInventory().getItem(i)));
        }
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, createPopulatedItem(player.getInventory().getItem(i - 18)));
        }
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, createPopulatedItem(player.getInventory().getItem(i - 36)));
        }
    }
}