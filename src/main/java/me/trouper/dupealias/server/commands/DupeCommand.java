package me.trouper.dupealias.server.commands;

import me.trouper.alias.server.commands.Args;
import me.trouper.alias.server.commands.CommandRegistry;
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
        usage = "/dupe [integer|gui]",
        printStackTrace = true,
        blocksAllowed = false,
        consoleAllowed = false
)
public class DupeCommand implements QuickCommand, DupeContext {

    public final DupeGui dupeGui = new DupeGui();
    private final Cooldown<UUID> dupeCooldown = new Cooldown<>();

    @Override
    public void handleCommand(CommandSender sender, Command command, String label, Args args) {
        Player player = (Player) sender;

        if (args.isEmpty()) {
            if (!verifyDupe(player,1)) {
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
            if (!verifyDupe(player,amount)) {
                dupeGui.openDefaultGui(player);
            }
        } catch (NumberFormatException e) {
            warningAny(player, dict().dupeCommand.invalidNumber, args.get(0).toString());
        }
    }

    @Override
    public void handleCompletion(CommandSender sender, Command command, String label, Args args, CompletionBuilder b) {
        b.then(
                b.arg("gui","number","replicator","inventory","chest")
        );
    }

    private boolean verifyDupe(Player player, int amount) {
        if (!player.hasPermission("dupealias.dupe")) {
            warningAny(player, dict().dupeCommand.noPermission);
            return false;
        }
        if (dupeCooldown.isOnCooldown(player.getUniqueId())) {
            warningAny(player, dict().dupeCommand.onCooldown, dupeCooldown.formatLong(player.getUniqueId()));
            return false;
        }

        int playerMax = getDupe().getPermissionValue(player,"dupealias.dupe.limit.",Integer.MAX_VALUE,true);
        if (amount > playerMax) {
            warningAny(player, dict().dupeCommand.dupeLimitExceeded, playerMax);
            return false;
        }

        ItemStack toDupe = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (toDupe.isEmpty() && offHand.isEmpty()) {
            warningAny(player, dict().dupeCommand.noItemHeld);
            return false;
        }

        if (toDupe.isEmpty() || getDupe().isUnique(toDupe)) {
            if (getDupe().isUnique(offHand)) {
                warningAny(player, dict().dupeCommand.uniqueItemWarning, toDupe.getType());
                return false;
            } else {
                toDupe = offHand;
            }
        }

        dupeStack(player,toDupe,amount);

        int playerCooldown = getDupe().getPermissionValue(player,"dupealias.dupe.cooldown.",getConfig().baseDupeCooldownMillis,false);
        dupeCooldown.setCooldown(player.getUniqueId(), playerCooldown);

        return true;
    }

    private void dupeStack(Player player, ItemStack heldStack, int amount) {
        int baseCount = heldStack.getAmount();
        int maxPerStack = heldStack.getMaxStackSize();

        for (int i = 0; i <= amount - 1; i++) {
            int remaining = baseCount * (1 << i);

            while (remaining > 0) {
                int stackAmt = Math.min(remaining, maxPerStack);
                remaining -= stackAmt;

                ItemStack batch = heldStack.clone();
                batch.setAmount(stackAmt);

                if (!player.getInventory().addItem(batch).isEmpty()) {
                    infoAny(player, dict().dupeCommand.inventoryFull);
                    return;
                }
            }
        }

        int totalGiven = baseCount * ((1 << amount) - 1);
        successAny(player, dict().dupeCommand.successMessage, totalGiven);
    }
}
