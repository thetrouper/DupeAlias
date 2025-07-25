package me.trouper.dupealias;

import me.trouper.alias.AliasContext;
import me.trouper.alias.AliasContextProvider;
import me.trouper.alias.data.Common;
import me.trouper.dupealias.data.files.CommonConfig;
import me.trouper.dupealias.data.files.DupeConfig;
import me.trouper.dupealias.data.files.NBTStorage;
import me.trouper.dupealias.server.DupeManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class DupeAlias extends JavaPlugin {

    private static DupeAlias instance;
    private AliasContext alias;
    private Common common;
    private DupeManager dupe;

    @Override
    public void onLoad() {
        instance = this;
        common = new CommonConfig().generateCommon();
        alias = new AliasContext(this, common);
        AliasContextProvider.registerContext(this,alias);
    }

    @Override
    public void onEnable() {
        alias.initialize();
        alias.getDataManager().load(CommonConfig.class).save();
        alias.getDataManager().load(DupeConfig.class).save();
        alias.getDataManager().load(NBTStorage.class).save();
        updateCommon();
        
        dupe = new DupeManager();
    }

    @Override
    public void onDisable() {
        alias.getDataManager().save(CommonConfig.class);
        alias.getDataManager().save(DupeConfig.class);
        alias.getDataManager().save(NBTStorage.class);
        alias.shutdown();
    }

    public static DupeAlias getDupeAlias() {
        return instance;
    }

    public void updateCommon() {
        common.update(alias.getDataManager().get(CommonConfig.class).generateCommon());
    }

    public Common getCommon() {
        return common;
    }

    public CommonConfig getCommonConfig() {
        return alias.getDataManager().get(CommonConfig.class);
    }

    public DupeConfig getDupeConfig() {
        return alias.getDataManager().get(DupeConfig.class);
    }

    public DupeManager getDupe() {
        return dupe;
    }
}
