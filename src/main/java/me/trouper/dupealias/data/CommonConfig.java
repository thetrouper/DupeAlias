package me.trouper.dupealias.data;

import me.trouper.alias.data.Common;
import me.trouper.alias.data.JsonSerializable;
import me.trouper.dupealias.DupeContext;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class CommonConfig implements JsonSerializable<CommonConfig>, DupeContext {

    @Override
    public File getFile() {
        return new File(getInstance().getDataFolder(), "common.json");
    }

    public int mainColor = 0xAAAAFF;
    public int secondaryColor = 0x00DDFF;
    public String pluginName = "DupeAlias";
    public String flatPrefix = "&9DupeAlias> &7";
    public boolean flat = false;
    public boolean debugMode = false;
    public Set<String> debuggerExclusions = new HashSet<>();

    public Common generateCommon() {
        Common common = new Common(
                "me.trouper.dupealias",
                mainColor,
                secondaryColor,
                pluginName,
                flatPrefix,
                flat,
                "http://api.trouper.me:9090/download/plugins/DupeAlias/DupeAlias-LATEST.jar"
        );
        common.setDebugMode(debugMode);
        common.setDebuggerExclusions(debuggerExclusions);
        return common;
    }
}
