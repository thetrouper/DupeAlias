package me.trouper.dupealias.server.gui;

import me.trouper.alias.utils.FormatUtils;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeAlias;
import me.trouper.dupealias.DupeContext;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Light;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;

public interface CommonItems extends DupeContext {

    default NamespacedKey CANCEL_CLICK() {
        return new NamespacedKey(DupeAlias.getDupeAlias(),"CANCEL_CLICK");
    }
    
    default ItemStack EMPTY() {
        return ItemBuilder.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            .displayName("<reset>")
            .modifyMeta(meta->{
                meta.getPersistentDataContainer().set(CANCEL_CLICK(), PersistentDataType.BOOLEAN,Boolean.TRUE);
                return meta;
            })
            .build();
    }
    
    default ItemStack EMPTY(Material display) {
        return EMPTY().withType(display);
    }

   default ItemStack BACK() {
        return ItemBuilder.of(Material.ARROW)
               .displayName("<yellow><bold>← Back</bold>")
               .modifyMeta(meta->{
                   meta.getPersistentDataContainer().set(CANCEL_CLICK(), PersistentDataType.BOOLEAN,Boolean.TRUE);
                   return meta;
               })
               .build();
   }

    default boolean shouldBlockClick(ItemStack item) {
        if (item == null || item.isEmpty()) return false;
        return  (item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(CANCEL_CLICK(),PersistentDataType.BOOLEAN)
                && Boolean.TRUE.equals(item.getItemMeta().getPersistentDataContainer().get(CANCEL_CLICK(), PersistentDataType.BOOLEAN)));
    }

    default ItemStack createPopulatedItem(ItemStack item, double progress) {
        if (progress < 1) {
            return ItemBuilder.of(EMPTY(Material.RED_STAINED_GLASS_PANE))
                    .displayName("<yellow>Item Refilling...")
                    .loreComponent(getTextSystem().createProgressBar(progress,(char) '|',20, TextColor.color(0x5AFF89),TextColor.color(0x6F6F6F)))
                    .build();
        }
        if (item == null || item.isEmpty()) return EMPTY(Material.GRAY_STAINED_GLASS_PANE);
        ItemStack clone = item.clone();
        if (getDupe().isUnique(clone)) return ItemBuilder.of(EMPTY(Material.BARRIER))
                .displayName("<red><bold>UNIQUE ITEM")
                .loreMiniMessage("<gray>You are unable to dupe <white>" + FormatUtils.formatEnum(clone.getType()))
                .build();
        return clone;
    }
}
