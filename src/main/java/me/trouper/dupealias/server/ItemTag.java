package me.trouper.dupealias.server;

import me.trouper.dupealias.DupeAlias;
import org.bukkit.NamespacedKey;

public enum ItemTag {
    UNIQUE(new NamespacedKey(DupeAlias.getDupeAlias(),"unique"), "Unique", "Cannot be duplicated"),
    FINAL(new NamespacedKey(DupeAlias.getDupeAlias(),"final"), "Final", "Cannot be modified"),
    PROTECTED(new NamespacedKey(DupeAlias.getDupeAlias(),"protected"), "Protected", "Cannot be used or crafted with"),
    INFINITE(new NamespacedKey(DupeAlias.getDupeAlias(),"infinite"), "Infinite", "Will always have max stack size");


    private final NamespacedKey key;
    private final String name;
    private final String desc;

    ItemTag(NamespacedKey key, String name, String desc) {
        this.key = key;
        this.name = name;
        this.desc = desc;
    }

    public NamespacedKey getKey() {
        return key;
    }
    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }

}
