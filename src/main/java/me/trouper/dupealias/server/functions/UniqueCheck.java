package me.trouper.dupealias.server.functions;

import me.trouper.dupealias.server.ItemTag;
import org.bukkit.inventory.ItemStack;

public class UniqueCheck implements Check<ItemStack> {
    @Override
    public boolean passes(ItemStack input) {
        boolean isUnique = getDupe().checkEffectiveTag(input,ItemTag.UNIQUE);
        if (isUnique) return false;

        return new ItemInventoryCheck(this).passes(input);
    }
}
