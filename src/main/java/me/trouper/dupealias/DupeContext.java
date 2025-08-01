package me.trouper.dupealias;

import me.trouper.alias.server.ContextAware;
import me.trouper.alias.server.events.listeners.GuiInputListener;
import me.trouper.dupealias.data.files.CommonConfig;
import me.trouper.dupealias.data.files.Dictionary;
import me.trouper.dupealias.data.files.DupeConfig;
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
        return getInstance().getCommonConfig();
    }

    default DupeConfig getConfig() {
        return getInstance().getDupeConfig();
    }

    default Dictionary dict() {
        return getInstance().getDictionary();
    }

    default GuiInputListener getGuiListener() {
        return getContext().getGuiInputListener();
    }

    default DupeManager getDupe() {
        return getInstance().getDupe();
    }
}
