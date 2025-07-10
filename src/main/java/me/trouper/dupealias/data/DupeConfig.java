package me.trouper.dupealias.data;

import me.trouper.alias.data.JsonSerializable;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.ItemTag;
import org.bukkit.Material;

import java.io.File;
import java.util.*;

public class DupeConfig implements JsonSerializable<DupeConfig>, DupeContext {
    @Override
    public File getFile() {
        return new File(getInstance().getDataFolder(),"config.json");
    }

    public long dupeCooldownMillis = 1000;

    public String defaultDupeGui = "REPLICATOR";

    public List<String> finalCommandRegex = new ArrayList<>(List.of(
            "\"(?:itemname|iname|einame|eitemname|itemrename|irename|eitemrename|eirename)\"gmi",
            "\"(?:itemlore|lore|elore|ilore|eilore|eitemlore)\"gmi"
    ));

    public Map<ItemTag,String> tagLore = new HashMap<>(Map.of(
            ItemTag.PROTECTED, "<dark_purple><bold>|</bold><light_purple> Protected",
            ItemTag.FINAL, "<dark_red><bold>|</bold><red> Final",
            ItemTag.UNIQUE, "<dark_blue><bold>|</bold><blue> Unique",
            ItemTag.INFINITE, "<dark_green><bold>|</bold><green> Infinite"
    ));

    public Map<Material, Set<ItemTag>> globalMaterials = new HashMap<>();

}
