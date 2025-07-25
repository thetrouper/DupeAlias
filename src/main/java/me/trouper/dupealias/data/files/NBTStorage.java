package me.trouper.dupealias.data.files;

import me.trouper.alias.data.JsonSerializable;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.ItemCapture;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class NBTStorage implements JsonSerializable<NBTStorage>, DupeContext {
    @Override
    public File getFile() {
        return new File(getInstance().getDataFolder(), ".nbtstorage.json");
    }
    
    public List<ItemCapture> captures = new ArrayList<>();

    public ItemCapture getCapture(ItemStack input) {
        if (getNbtStorage().captures.isEmpty()) return null;
        ItemCapture match = null;
        double closest = -1;

        for (ItemCapture capture : getNbtStorage().captures) {
            boolean isSimilar = capture.getStack().isSimilar(input);
            if (isSimilar) return capture;
            double threshold = capture.getThreshold();
            
            if (threshold >= 1) continue; // Don't bother calculating similarity if the item isn't similar.
            double sim = capture.similarityTo(input);
            if (sim >= threshold && sim >= closest) {
                closest = sim;
                match = capture;
            }
        }

        if (closest == -1) return null;

        return match;
    }
}
