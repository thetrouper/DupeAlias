package me.trouper.dupealias.server.functions;

import me.trouper.alias.utils.InventoryUtils;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

public class ItemInventoryCheck implements Check<ItemStack> {

    private final Check<ItemStack> nestedCheck;

    public ItemInventoryCheck(Check<ItemStack> nestedCheck) {
        this.nestedCheck = nestedCheck;
    }

    @Override
    public boolean passes(ItemStack input) {
        if (input == null || !input.hasItemMeta()) return true;

        if (input.getItemMeta() instanceof BundleMeta bundle) {
            for (ItemStack item : bundle.getItems()) {
                if (!nestedCheck.passes(item)) return false;
            }
        }

        Inventory subInventory = InventoryUtils.getInventory(input);
        if (subInventory == null) return true;

        return new InventoryCheck(nestedCheck).passes(subInventory);
    }
}
