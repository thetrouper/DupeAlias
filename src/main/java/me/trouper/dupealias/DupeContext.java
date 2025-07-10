package me.trouper.dupealias;

import me.trouper.alias.server.ContextAware;
import me.trouper.alias.server.events.listeners.GuiInputListener;
import me.trouper.dupealias.data.CommonConfig;
import me.trouper.dupealias.data.DupeConfig;
import me.trouper.dupealias.server.DupeManager;
import org.bukkit.plugin.java.JavaPlugin;

public interface DupeContext extends ContextAware {
    @Override
    default Class<? extends JavaPlugin> getPluginClass() {
        return DupeAlias.class;
    }

    default DupeAlias getInstance() {
        return DupeAlias.getDupeAlias();
    }

    default CommonConfig getCommonConfig() {
        return getDataManager().get(CommonConfig.class);
    }

    default DupeConfig getConfig() {
        return getDataManager().get(DupeConfig.class);
    }
    default GuiInputListener getGuiListener() {
        return getContext().getGuiInputListener();
    }

    default DupeManager getDupe() {
        return getInstance().getDupe();
    }
}
