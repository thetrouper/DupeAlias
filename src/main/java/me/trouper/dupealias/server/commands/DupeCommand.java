package me.trouper.dupealias.server.commands;

import me.trouper.alias.server.commands.Args;
import me.trouper.alias.server.commands.CommandRegistry;
import me.trouper.alias.server.commands.Permission;
import me.trouper.alias.server.commands.QuickCommand;
import me.trouper.alias.server.commands.completions.CompletionBuilder;
import me.trouper.alias.utils.misc.Cooldown;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.gui.dupe.DupeGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@CommandRegistry(
        value = "dupe",
        permission = @Permission(value = "dupealias.dupe", message = "You do not have permission to duplicate items."),
        usage = "/dupe [integer|gui]",
        printStackTrace = true,
        blocksAllowed = false,
        consoleAllowed = false
)
public class DupeCommand implements QuickCommand, DupeContext {

    private final DupeGui dupeGui = new DupeGui();
    private final Cooldown<UUID> dupeCooldown = new Cooldown<>();

    @Override
    public void handleCommand(CommandSender sender, Command command, String label, Args args) {
        Player player = (Player) sender;
        if (!player.hasPermission("dupealias.dupe.cooldownbypass") && dupeCooldown.isOnCooldown(player.getUniqueId())) {
            warningAny(player,"You can run /dupe again in {0}.", dupeCooldown.formatLong(player.getUniqueId()));
            return;
        }

        if (args.isEmpty()) {
            if (dupeHeld(player,0)) {
                dupeCooldown.setCooldown(player.getUniqueId(), getConfig().dupeCooldownMillis);
            } else {
                dupeGui.openDefaultGui(player);
            }
            return;
        }

        switch (args.get(0).toString()) {
            case "gui" -> {
                dupeGui.openMainGui(player);
                return;
            }
            case "replicator" -> {
                dupeGui.openIfPermission(player,dupeGui.replicatorGui,"dupealias.gui.replicator");
                return;
            }
            case "inventory" -> {
                dupeGui.openIfPermission(player,dupeGui.inventoryGui,"dupealias.gui.inventory");
                return;
            }
            case "chest" -> {
                dupeGui.openIfPermission(player,dupeGui.chestGui,"dupealias.gui.chest");
                return;
            }
        }

        try {
            int amount = args.get(0).toInt();
            if (dupeHeld(player,amount)) {
                dupeCooldown.setCooldown(player.getUniqueId(), getConfig().dupeCooldownMillis);
            } else {
                dupeGui.openDefaultGui(player);
            }
        } catch (NumberFormatException e) {
            warningAny(player,"{0} is not a valid number.", args.get(0).toString());
        }
    }

    @Override
    public void handleCompletion(CommandSender sender, Command command, String label, Args args, CompletionBuilder b) {
        b.then(
                b.arg("gui","number","replicator","inventory","chest")
        );
    }

    private boolean dupeHeld(Player player, int amount) {
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (getDupe().isUnique(inHand)) {
            warningAny(player,"Your {0} is or contains a unique item that cannot be duped!", inHand.getType());
            return false;
        }
        if (inHand.isEmpty()) return false;

        int baseCount = inHand.getAmount();
        int maxPerStack = inHand.getMaxStackSize();

        for (int i = 0; i <= amount; i++) {
            int remaining = baseCount * (1 << i);

            while (remaining > 0) {
                int stackAmt = Math.min(remaining, maxPerStack);
                remaining -= stackAmt;

                ItemStack batch = inHand.clone();
                batch.setAmount(stackAmt);

                if (!player.getInventory().addItem(batch).isEmpty()) {
                    infoAny(player,"Your inventory is now full.");
                    return true;
                }
            }
        }

        int totalGiven = baseCount * ((1 << (amount + 1)) - 1);
        successAny(player,"You have duplicated {0} items!", totalGiven);
        return true;
    }
}
