package me.trouper.dupealias.server.gui.dupe.sub;

import me.trouper.alias.server.systems.gui.QuickGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DupeChestGui extends AbstractDupeGui<DupeChestGui.ChestSession> {

    @Override
    protected ChestSession createSession(Player player) {
        return new ChestSession(player);
    }

    @Override
    public ChestSession getSession(Player player) {
        ChestSession session = super.getSession(player);
        session.setDelayTicks(getDupe().getPermissionValue(player, "dupealias.gui.chest.refresh.", getConfig().chest.baseRefreshDelayTicks));
        session.open();
        return session;
    }

    public class ChestSession extends AbstractDupeSession {
        private Map<Integer, ItemDelayInfo> itemDelays = new HashMap<>();
        private int delayTicks;

        public ChestSession(Player owner) {
            super(owner, "<gradient:#cc22ff:#cc99ff><bold>DUPE CHEST</gradient>", 6);
            this.delayTicks = getDupe().getPermissionValue(owner, "dupealias.gui.chest.refresh.", getConfig().chest.baseRefreshDelayTicks);
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
                    .onCreate((g, i) -> populateInventory(i))
                    .onClose((g, e) -> close())
                    .build();
        }

        @Override
        protected void tick() {
            populateInventory(getGui().getInventory());
        }

        public List<ItemStack> getInputItems() {
            List<ItemStack> items = new ArrayList<>();
            Inventory inv = getGui().getInventory();

            for (int row = 0; row < 6; row++) {
                int rowStart = row * 9;
                for (int col = 0; col < 4; col++) {
                    int index = rowStart + col;
                    ItemStack item =  inv.getItem(index);
                    if (item == null || item.isEmpty()) continue;
                    items.add(item);
                }
            }

            return items;
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

        private void populateInventory(Inventory inv) {
            for (int row = 0; row < 6; row++) {
                int rowStart = row * 9;
                inv.setItem(rowStart + 4, EMPTY(Material.WHITE_STAINED_GLASS_PANE));
                for (int col = 0; col < 4; col++) {
                    int leftIndex = rowStart + col;
                    int rightIndex = rowStart + 8 - col;
                    ItemStack leftItem = inv.getItem(leftIndex);
                    inv.setItem(rightIndex, getDelayedItem(rightIndex, leftItem));
                }
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