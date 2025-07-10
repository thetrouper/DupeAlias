package me.trouper.dupealias.data;

import me.trouper.alias.data.JsonSerializable;
import me.trouper.dupealias.DupeContext;

import java.io.File;

public class PlayerData implements JsonSerializable<PlayerData>, DupeContext {
    @Override
    public File getFile() {
        return new File(getInstance().getDataFolder(), "playerdata.json");
    }
}
