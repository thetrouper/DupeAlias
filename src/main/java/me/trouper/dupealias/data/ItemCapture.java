package me.trouper.dupealias.data;

import me.trouper.alias.utils.ItemSimilarity;
import me.trouper.dupealias.server.ItemTag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.ByteArrayInputStream;
import java.util.*;

public class ItemCapture {
    
    private String serializedItem;
    private double similarityThreshold;
    private final ItemSimilarity.SimilarityConfiguration configuration;
    private final Map<ItemTag, Boolean> tags;
    
    public ItemCapture() {
        this.similarityThreshold = 1;
        this.configuration = new ItemSimilarity.SimilarityConfiguration();
        this.tags = new HashMap<>();
    }
    
    public ItemCapture(ItemStack stack) {
        this.serializedItem = serialize(stack);
        this.similarityThreshold = 1;
        this.configuration = new ItemSimilarity.SimilarityConfiguration();
        this.tags = new HashMap<>();
    }

    private String serialize(ItemStack itemStack) {
        try {
            return Base64.getEncoder().encodeToString(itemStack.serializeAsBytes());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize ItemStack", e);
        }
    }

    private ItemStack deserialize(String serializedItemStack) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(serializedItemStack));
            byte[] itemBytes = inputStream.readAllBytes();
            return ItemStack.deserializeBytes(itemBytes);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deserialize ItemStack", e);
        }
    }
    
    public Map<ItemTag, Boolean> getTags() {
        return tags;
    }

    public ItemStack getStack() {
        return deserialize(serializedItem);
    }

    public ItemMeta getMeta() {
        return getStack().getItemMeta();
    }
    
    public boolean matches(ItemStack item) {
        if (similarityThreshold >= 1) return item.isSimilar(getStack());
        return similarityThreshold <= similarityTo(item);
    } 
    
    public double similarityTo(ItemStack item) {
        return ItemSimilarity.calculateSimilarity(item,getStack());
    }

    public double getThreshold() {
        return similarityThreshold;
    }

    public ItemSimilarity.SimilarityConfiguration getConfiguration() {
        return configuration;
    }

    public void setThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }
}
