package me.trouper.dupealias.server.gui.dupe.sub;

import me.trouper.alias.server.systems.gui.QuickGui;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DupeInventoryGui extends AbstractDupeGui<DupeInventoryGui.InventorySession> {
    
    @Override
    protected InventorySession createSession(Player player) {
        return new InventorySession(player);
    }

    @Override
    public InventorySession getSession(Player player) {
        InventorySession session = super.getSession(player);
        session.setDelayTicks(getDupe().getPermissionValue(player, "dupealias.gui.inventory.refresh.", getConfig().inventory.baseRefreshDelayTicks,false));
        session.open();
        return session;
    }

    public class InventorySession extends AbstractDupeSession {
        private Map<Integer, ItemDelayInfo> itemDelays = new HashMap<>();
        private int delayTicks;

        public InventorySession(Player owner) {
            super(owner, "<gradient:#cc22ff:#cc99ff><bold>YOUR INVENTORY</gradient>", 6);
            this.delayTicks = getDupe().getPermissionValue(owner, "dupealias.gui.inventory.refresh.", getConfig().inventory.baseRefreshDelayTicks,false);
        }

        @Override
        protected QuickGui buildGui(String title, int rows) {
            return QuickGui.create()
                    .titleMini(title)
                    .rows(rows)
                    .onGlobalClick((g, e) -> {
                        boolean shouldCancel = shouldBlockClick(e.getCursor()) ||
                                shouldBlockClick(e.getCurrentItem()) ||
                                (e.getClickedInventory() != null && shouldBlockClick(e.getClickedInventory().getItem(e.getSlot())));

                        if (!shouldCancel && e.getClickedInventory() != null) {
                            resetItemDelay(e.getSlot());
                        }

                        e.setCancelled(shouldCancel);
                    })
                    .allowDrag()
                    .clickSound(null,0,0)
                    .onCreate((g, i) -> populateInventory(getOwner(), i))
                    .onClose((g, e) -> close())
                    .build();
        }

        @Override
        protected void tick() {
            populateInventory(getOwner(), getGui().getInventory());
        }

        private void resetItemDelay(int slot) {
            ItemDelayInfo info = itemDelays.get(slot);
            if (info != null) {
                info.currentTicks = 0;
                info.ready = false;
            }
        }

        private ItemStack getDelayedItem(int slot, ItemStack sourceItem) {
            if (sourceItem == null) {
                itemDelays.remove(slot);
                return createPopulatedItem(null,1);
            }

            ItemDelayInfo info = itemDelays.get(slot);
            if (info == null) {
                info = new ItemDelayInfo(sourceItem.clone());
                itemDelays.put(slot, info);
            }

                if (!sourceItem.isSimilar(info.originalItem)) {
                info = new ItemDelayInfo(sourceItem.clone());
                itemDelays.put(slot, info);
            }

            if (!info.ready) {
                info.currentTicks++;
                double progress = Math.min(1.0, (double) info.currentTicks / delayTicks);
                if (info.currentTicks >= delayTicks) {
                    info.ready = true;
                }
                return createPopulatedItem(sourceItem, progress);
            }

            return createPopulatedItem(sourceItem, 1.0);
        }

        private void populateInventory(Player player, Inventory inv) {
            for (int i = 0; i < 18; i++) {
                inv.setItem(i, EMPTY());
            }

            inv.setItem(0, getDelayedItem(0, player.getInventory().getHelmet()));
            inv.setItem(1, getDelayedItem(1, player.getInventory().getChestplate()));
            inv.setItem(2, getDelayedItem(2, player.getInventory().getLeggings()));
            inv.setItem(3, getDelayedItem(3, player.getInventory().getBoots()));
            inv.setItem(6, getDelayedItem(6, player.getInventory().getItemInOffHand()));

            for (int i = 0; i < 9; i++) {
                inv.setItem(i + 18, getDelayedItem(i + 18, player.getInventory().getItem(i)));
            }
            for (int i = 27; i < 36; i++) {
                inv.setItem(i, getDelayedItem(i, player.getInventory().getItem(i)));
            }
            for (int i = 36; i < 45; i++) {
                inv.setItem(i, getDelayedItem(i, player.getInventory().getItem(i - 18)));
            }
            for (int i = 45; i < 54; i++) {
                inv.setItem(i, getDelayedItem(i, player.getInventory().getItem(i - 36)));
            }
        }

        public void setDelayTicks(int delayTicks) {
            this.delayTicks = delayTicks;
        }

        private static class ItemDelayInfo {
            ItemStack originalItem;
            int currentTicks = 0;
            boolean ready = false;

            ItemDelayInfo(ItemStack item) {
                this.originalItem = item;
            }
        }
    }
}