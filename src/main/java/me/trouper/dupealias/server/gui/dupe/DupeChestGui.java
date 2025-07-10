package me.trouper.dupealias.server.gui.dupe;

import me.trouper.alias.server.systems.gui.QuickGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DupeChestGui extends AbstractDupeGui<DupeChestGui.ChestSession> {

    @Override
    protected ChestSession createSession(Player player) {
        return new ChestSession(player);
    }

    public class ChestSession extends AbstractDupeSession {

        public ChestSession(Player owner) {
            super(owner, "<gradient:#cc22ff:#cc99ff><bold>DUPE CHEST</gradient>", 6);
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
                    .onCreate((g, i) -> populateInventory(i))
                    .onClose((g, e) -> close())
                    .build();
        }

        @Override
        protected void tick() {
            populateInventory(getGui().getInventory());
        }

        @Override
        protected long getTickDelay(Player player) {
            return 1;
        }
    }


    private void populateInventory(Inventory inv) {
        for (int row = 0; row < 6; row++) {
            int rowStart = row * 9;
            inv.setItem(rowStart + 4, EMPTY(Material.WHITE_STAINED_GLASS_PANE));
            for (int col = 0; col < 4; col++) {
                int leftIndex = rowStart + col;
                int rightIndex = rowStart + 8 - col;
                ItemStack leftItem = inv.getItem(leftIndex);
                inv.setItem(rightIndex, createPopulatedItem(leftItem));
            }
        }
    }
}