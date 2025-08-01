package me.trouper.dupealias.server.events;

import me.trouper.alias.server.events.QuickListener;
import me.trouper.alias.utils.FormatUtils;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.ItemTag;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InfiniteItemEvents implements QuickListener, DupeContext {
    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        resetBothHands(e.getPlayer());
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player player)) return;
        resetBothHands(player);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        ItemStack stack = e.getItemDrop().getItemStack();
        if (!getDupe().checkEffectiveTag(stack,ItemTag.INFINITE)) return;
        if (getDupe().checkEffectiveTag(stack,ItemTag.PROTECTED)) {
            e.setCancelled(true);
            return;
        }
        if (stack.getAmount() < 99) {
            resetBothHands(player);

            getDupe().removeTag(stack,ItemTag.INFINITE);
            ItemMeta stackMeta = stack.getItemMeta();
            stackMeta.setMaxStackSize(stack.getType().getMaxStackSize());
            stack.setItemMeta(stackMeta);
            e.getItemDrop().setItemStack(stack);
        } else {
            infoAny(player,dict().itemModificationEvents.dropInfiniteItem, FormatUtils.formatEnum(stack.getType()));
        }

    }

    @EventHandler
    public void onDispense(BlockDispenseEvent e) {
        ItemStack stack = e.getItem();

        if (!getDupe().checkEffectiveTag(stack, ItemTag.INFINITE)) return;
        if (getDupe().checkEffectiveTag(stack, ItemTag.PROTECTED)) {
            e.setCancelled(true);
            return;
        }

        if (stack.getAmount() < 99) {
            getDupe().removeTag(stack,ItemTag.INFINITE);
            ItemMeta stackMeta = stack.getItemMeta();
            stackMeta.setMaxStackSize(stack.getType().getMaxStackSize());
            stack.setItemMeta(stackMeta);
            e.setItem(stack);
        }

        Container container = (Container) e.getBlock().getState();

        for (ItemStack itemStack : container.getInventory()) {
            if (!getDupe().checkEffectiveTag(itemStack, ItemTag.INFINITE)) continue;
            itemStack.setAmount(99);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        resetBothHands(e.getPlayer());
    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent e) {
        resetBothHands(e.getPlayer());
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        resetBothHands(e.getPlayer());
    }

    @EventHandler
    public void onItemChange(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        ItemStack held = player.getInventory().getItem(e.getNewSlot());
        if (held == null || held.isEmpty()) return;
        if (!getDupe().checkEffectiveTag(held,ItemTag.INFINITE)) return;
        ItemMeta meta = held.getItemMeta();
        meta.setMaxStackSize(99);
        held.setItemMeta(meta);
        held.setAmount(meta.getMaxStackSize());
    }

    private void resetBothHands(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        if (!main.isEmpty() && getDupe().checkEffectiveTag(main,ItemTag.INFINITE)) main.setAmount(main.getMaxStackSize());
        if (!off.isEmpty() && getDupe().checkEffectiveTag(off,ItemTag.INFINITE)) off.setAmount(off.getMaxStackSize());
    }
}
