package me.trouper.dupealias.server.gui.dupe;

import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.gui.CommonItems;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractDupeGui<T extends AbstractDupeSession> implements DupeContext, CommonItems {
    protected final Map<UUID, T> sessions = new HashMap<>();

    protected abstract T createSession(Player player);

    public T getSession(Player player) {
        sessions.entrySet().removeIf(entry -> entry.getValue().isClosed());
        return sessions.computeIfAbsent(player.getUniqueId(), uuid -> createSession(player));
    }

    public void removeSession(Player player) {
        sessions.remove(player.getUniqueId());
    }
}