package me.trouper.dupealias.server.events;

import me.trouper.alias.server.events.QuickListener;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.commands.DupeCommand;
import me.trouper.dupealias.server.gui.dupe.DupeGui;
import me.trouper.dupealias.server.gui.dupe.sub.DupeChestGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DeathEvent implements QuickListener, DupeContext {

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("dupealias.gui.chest.keepondeath")) return;

        DupeGui gui = getContext().getAutoRegistrar().getQuickCommand(DupeCommand.class).dupeGui;
        DupeChestGui.ChestSession session = gui.chestGui.getSession(p);
        if (session == null) return;

        List<ItemStack> items = session.getInputItems();
        e.getDrops().addAll(items);
    }
}
