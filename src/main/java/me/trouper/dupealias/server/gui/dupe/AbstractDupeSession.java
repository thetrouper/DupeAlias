package me.trouper.dupealias.server.gui.dupe;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.dupealias.DupeContext;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public abstract class AbstractDupeSession implements DupeContext {
    protected final Player owner;
    protected final QuickGui gui;
    protected BukkitTask replicationTask;
    protected boolean closed;

    public AbstractDupeSession(Player owner, String title, int rows) {
        this.owner = owner;
        this.closed = false;
        this.gui = buildGui(title, rows);
        startTicking();
    }

    protected abstract QuickGui buildGui(String title, int rows);

    protected abstract void tick();

    private void startTicking() {
        if (replicationTask != null && !replicationTask.isCancelled()) {
            replicationTask.cancel();
        }
        replicationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (closed || !owner.isOnline()) {
                    owner.stopSound(Sound.BLOCK_BEACON_AMBIENT);
                    cancel();
                    replicationTask = null;
                    return;
                }
                tick();
            }
        }.runTaskTimer(getPlugin(), 0, 1);
    }

    public void close() {
        this.closed = true;
    }

    public Inventory open() {
        this.closed = false;
        if (replicationTask == null || replicationTask.isCancelled()) {
            startTicking();
        }
        return gui.getInventory();
    }

    public boolean isClosed() {
        return closed;
    }

    public QuickGui getGui() {
        return gui;
    }

    public Player getOwner() {
        return owner;
    }

    public BukkitTask getReplicationTask() {
        return replicationTask;
    }
}