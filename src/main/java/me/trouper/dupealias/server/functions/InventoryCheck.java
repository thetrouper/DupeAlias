package me.trouper.dupealias.server.functions;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryCheck implements Check<Inventory> {

    private final Check<ItemStack> nestedCheck;

    public InventoryCheck(Check<ItemStack> nestedCheck) {
        this.nestedCheck = nestedCheck;
    }

    @Override
    public boolean passes(Inventory inventory) {
        if (inventory == null) return true;

        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;
            if (!nestedCheck.passes(item)) {
                return false;
            }
        }

        return true;
    }
}
