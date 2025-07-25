package me.trouper.dupealias.server.gui.admin;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.server.gui.CommonItems;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class HelpGui implements CommonItems {

    private final AdminPanelManager manager;

    public HelpGui(AdminPanelManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#9b59b6:#8e44ad><bold>DupeAlias Help</bold></gradient>")
                .rows(6)
                .fillBorder(EMPTY(Material.PURPLE_STAINED_GLASS_PANE))

                .item(0, BACK(),
                        (g, e) -> manager.openMainGui(player))

                .item(20, ItemBuilder.create(Material.EMERALD)
                        .displayName("<green><bold>UNIQUE Tag</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>What it does:",
                                "<gray>• Prevents item duplication",
                                "<gray>• Works globally or per item",
                                "",
                                "<white>Use cases:",
                                "<gray>• Crate Keys",
                                "<gray>• Special or rare items",
                                "<gray>• Admin-only gear",
                                "",
                                "<red>⚠ Conflict:",
                                "<gray>• Avoid combining with <red>INFINITE"
                        ))
                        .build())

                .item(21, ItemBuilder.create(Material.BARRIER)
                        .displayName("<red><bold>FINAL Tag</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>What it does:",
                                "<gray>• Blocks all item modifications",
                                "<gray>• Prevents renaming, enchanting, etc.",
                                "",
                                "<white>Use cases:",
                                "<gray>• Name-dependent items",
                                "<gray>• Rank kits or prizes",
                                "<gray>• Event rewards",
                                "",
                                "<gray>✔ Can be combined safely with all tags"
                        ))
                        .build())

                .item(22, ItemBuilder.create(Material.WATER_BUCKET)
                        .displayName("<blue><bold>INFINITE Tag</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>What it does:",
                                "<gray>• Enforces max stack size (99)",
                                "<gray>• Item instantly refills when used",
                                "",
                                "<white>Use cases:",
                                "<gray>• Infinite building blocks",
                                "<gray>• 'Infinity' for tipped arrows",
                                "<gray>• Creative-like resource flow",
                                "",
                                "<red>⚠ Conflicts:",
                                "<gray>• Avoid combining with <red>UNIQUE",
                                "<gray>• Avoid combining with <red>PROTECTED"
                        ))
                        .build())

                .item(23, ItemBuilder.create(Material.STRUCTURE_VOID)
                        .displayName("<dark_purple><bold>PROTECTED Tag</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>What it does:",
                                "<gray>• Blocks all use: crafting, consuming, enchanting",
                                "<gray>• Makes item functionally inert",
                                "<gray>• This does <red>NOT</red> prevent duping",
                                "",
                                "<white>Use cases:",
                                "<gray>• Crate keys or Coupons",
                                "<gray>• Decorative/admin-only items",
                                "",
                                "<red>⚠ Conflict:",
                                "<gray>• Avoid combining with <red>INFINITE"
                        ))
                        .build())

                .item(24, ItemBuilder.create(Material.REDSTONE_TORCH)
                        .displayName("<red><bold>Important Notes</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>Things to remember:",
                                "<gray>• Individual tags <i>override</i> global rules",
                                "<gray>• Global rules can match complex criteria",
                                "<gray>• PROTECTED items are <red>not</red> UNIQUE by default",
                                "<gray>• UNIQUE items can <i>still</i> be duped with <u>external</u> exploits",
                                "",
                                "<white>Tag Combinations:",
                                "<gray>✔ <green>FINAL + PROTECTED</green> = Immutable Inert item",
                                "<gray>✔ <green>PROTECTED + UNIQUE</green> = Inert Dupe-Proof Item",
                                "<gray>❌ <red>INFINITE + UNIQUE</red> = Paradox",
                                "<gray>❌ <red>INFINITE + PROTECTED</red> = Contradiction"
                        ))
                        .build())

                .item(30, ItemBuilder.create(Material.WRITABLE_BOOK)
                        .displayName("<yellow><bold>Individual vs Global Tags</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>Individual Tags:",
                                "<gray>• Stored directly on the item",
                                "<gray>• Apply only to that one instance",
                                "<gray>• Use 'Held Item Actions' menu",
                                "",
                                "<white>Global Rules:",
                                "<gray>• Apply tags based on item properties",
                                "<gray>• Match by material, name, enchants, etc.",
                                "<gray>• Use 'Global Rules' menu",
                                "",
                                "<red>⚠ Individual tags override global rules"
                        ))
                        .build())

                .item(31, ItemBuilder.create(Material.COMPARATOR)
                        .displayName("<gold><bold>Global Rules System</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>Match items by:",
                                "<gray>• <white>Materials<gray> (whitelist/blacklist)",
                                "<gray>• <white>Name/Lore<gray> (regex patterns)",
                                "<gray>• <white>Enchantments<gray> (type & level)",
                                "<gray>• <white>Attributes<gray> (exact values)",
                                "<gray>• <white>Model Data<gray> (custom values)",
                                "<gray>• <white>Potion Effects<gray> (type & amp)",
                                "<gray>• <white>Armor Trim<gray> (pattern & material)",
                                "<gray>• <white>Item Flags<gray> (hide tooltips)",
                                "",
                                "<white>Match Modes:",
                                "<gray>• <green>AND<gray>: All criteria must match",
                                "<gray>• <yellow>OR<gray>: Any criteria matches",
                                "<gray>• <red>NAND<gray>: Not all match",
                                "<gray>• <aqua>XOR<gray>: Exactly one matches"
                        ))
                        .build())

                .item(32, ItemBuilder.create(Material.COMMAND_BLOCK)
                        .displayName("<aqua><bold>Rule Examples</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white><bold>Example Criteria:",
                                "",
                                "<white>1. Prevent Duping Netherite:",
                                "<gray>• Material: [NETHERITE_INGOT, ANCIENT_DEBRIS, ...]",
                                "<gray>• Tag: UNIQUE",
                                "",
                                "<white>2. Protect Crate Keys by Name",
                                "<gray>• Name Regex: 'key'",
                                "<gray>• Tags: PROTECTED, UNIQUE",
                                "",
                                "<white>3. Lock Silence Trim Armor:",
                                "<gray>• Material: Ignore",
                                "<gray>• Trim: [Silence]",
                                "<gray>• Match Mode: AND",
                                "<gray>• Tag: FINAL"
                        ))
                        .build())

                .item(37, ItemBuilder.create(Material.NAME_TAG)
                        .displayName("<aqua><bold>Tag Glossary</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white><bold>UNIQUE</bold><gray>: Prevents <i>intended</i> duplication",
                                "<white><bold>FINAL</bold><gray>: Cancels editing/modification",
                                "<white><bold>PROTECTED</bold><gray>: Blocks all use (crafting, consuming)",
                                "<white><bold>INFINITE</bold><gray>: Always max stack size (99)",
                                "",
                                "<gray>Tags can be combined, but some conflict!"
                        ))
                        .build())

                .item(43, ItemBuilder.create(Material.TRIAL_KEY)
                        .displayName("<white><bold>Permissions Guide</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>Access Permissions:",
                                "<green>The root permission node is <bold>dupealias",
                                "<gray>• <white>.dupe<gray> - Use /dupe command",
                                "<gray>• <white>.gui<gray> - Access duplication GUI",
                                "",
                                "<white>Dupe GUI & Sessions:",
                                "<gray>• <white>.gui.<type>.refresh.<n><gray> - GUI refill time",
                                "<gray>• <white>.gui.<type>.keep<gray> - Retain items in GUI session",
                                "<gray>• <white>.gui.replicator<gray> - Shift-click duplicate",
                                "<gray>• <white>.gui.replicator.cooldown<gray> - Item input cooldown",
                                "<gray>• <white>.gui.inventory<gray> - View personal inventory",
                                "<gray>• <white>.gui.chest<gray> - Dupe via container",
                                "",
                                "<white>Bypass Permissions:",
                                "<gray>• <white>.unique.bypass<gray> - Dupe unique items",
                                "<gray>• <white>.final.bypass<gray> - Modify final items",
                                "<gray>• <white>.protected.bypass<gray> - Use protected items",
                                "<gray>• <white>.dupe.cooldownbypass<gray> - Skip /dupe cooldown",
                                "",
                                "<white>Other:",
                                "<gray>• <white>.infinite<gray> - Use infinite-tagged items",
                                "",
                                "<red>⚠ Misuse Warning:",
                                "<gray>Bypass perms override protections!",
                                "<gray>Use caution when assigning them."
                        ))
                        .build())

                .item(49, manager.createExplainedItem(player.getInventory().getItemInMainHand()))

                .fillEmpty(EMPTY())
                .clickSound(Sound.UI_BUTTON_CLICK, 0.7f, 1.2f)
                .build();

        gui.open(player);
    }
}