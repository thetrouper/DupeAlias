package me.trouper.dupealias.data.files;

import me.trouper.alias.data.JsonSerializable;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.GlobalRule;
import me.trouper.dupealias.server.ItemTag;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DupeConfig implements JsonSerializable<DupeConfig>, DupeContext {
    @Override
    public File getFile() {
        return new File(getInstance().getDataFolder(),"config.json");
    }

    public int baseDupeCooldownMillis = 1000;

    public String defaultDupeGui = "REPLICATOR";

    public List<String> finalCommandRegex = new ArrayList<>(List.of(
            "\"(?:itemname|iname|einame|eitemname|itemrename|irename|eitemrename|eirename)\"gmi",
            "\"(?:itemlore|lore|elore|ilore|eilore|eitemlore)\"gmi"
    ));

    public Map<ItemTag,String> trueTagLore = new HashMap<>(Map.of(
            ItemTag.PROTECTED, "<dark_purple><bold>|</bold><light_purple> Protected",
            ItemTag.FINAL, "<dark_red><bold>|</bold><red> Final",
            ItemTag.UNIQUE, "<dark_blue><bold>|</bold><blue> Unique",
            ItemTag.INFINITE, "<dark_green><bold>|</bold><green> Infinite"
    ));
    
    public Map<ItemTag,String> falseTagLore = new HashMap<>(Map.of(
            ItemTag.PROTECTED, "<dark_purple><bold>|</bold><light_purple> Unprotected",
            ItemTag.FINAL, "<dark_red><bold>|</bold><red> Mutable",
            ItemTag.UNIQUE, "<dark_blue><bold>|</bold><blue> Dupeable",
            ItemTag.INFINITE, "<dark_green><bold>|</bold><green> Finite"
    ));

    public List<GlobalRule> globalRules = new ArrayList<>();
    
    public Replicator replicator = new Replicator();
    public Chest chest = new Chest();
    public Inventory inventory = new Inventory();

    public class Replicator {
        public int baseRefreshDelayTicks = 1;
        public int baseInputCooldownTicks = 20;
    }
    
    public class Chest {
        public int baseRefreshDelayTicks = 1;
    }
    
    public class Inventory {
        public int baseRefreshDelayTicks = 1;
    }
}
