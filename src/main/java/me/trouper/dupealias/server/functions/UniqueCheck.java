package me.trouper.dupealias.server.functions;

import me.trouper.dupealias.server.ItemTag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class UniqueCheck implements Check<ItemStack> {
    @Override
    public boolean passes(ItemStack input) {
        boolean globallyUnique = getDupe().checkGlobalRuleTag(input,ItemTag.UNIQUE);
        boolean set = input.hasItemMeta() && input.getPersistentDataContainer().has(ItemTag.UNIQUE.getKey());
        boolean individuallyUnique = Boolean.TRUE.equals(input.getPersistentDataContainer().get(ItemTag.UNIQUE.getKey(), PersistentDataType.BOOLEAN));

        if (set && individuallyUnique) return false;
        if (!set && globallyUnique) return false;

        return new ItemInventoryCheck(this).passes(input);
    }
}
