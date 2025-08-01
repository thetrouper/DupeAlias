package me.trouper.dupealias.data.files;

import me.trouper.alias.data.JsonSerializable;
import me.trouper.dupealias.DupeContext;

import java.io.File;
import java.util.List;

public class Dictionary implements JsonSerializable<Dictionary>, DupeContext {


    @Override
    public File getFile() {
        return new File(getInstance().getDataFolder(), "dictionary.json");
    }

    public DictGuiDupe guiDupe = new DictGuiDupe();
    public DictItemModificationEvents itemModificationEvents = new DictItemModificationEvents();
    public DictDupeCommand dupeCommand = new DictDupeCommand();

    public class DictGuiDupe {
        public String title = "<aqua><bold>Available GUIs";
        public String noPermission = "You do not have permission to use the main dupe gui.";
        public String noSpecificPermission = "You do not have permission to use that GUI.";
        public String replicatorName = "<blue>Replicator GUI";
        public String replicatorLore = "<gray>Open the single-item dupe GUI.";
        public String inventoryName = "<yellow>Inventory GUI";
        public String inventoryLore = "<gray>Open a mirror of your own inventory.";
        public String chestName = "<green>Chest GUI";
        public String chestLore = "<gray>Open the multi-item dupe GUI.";
        public String noDupeGuiName = "<dark_red>Unavailable GUI";
        public List<String> noDupeGuiLore = List.of(
                "",
                "<red>You lack the permission to",
                "<red>use the <italic>{0}</italic> gui."
        );
        public String noDefaultGui = "There is currently no default Dupe GUI.";

        public DictGuiReplicator guiReplicator = new DictGuiReplicator();
        public DictGuiInventory guiInventory = new DictGuiInventory();
        public DictGuiChest guiChest = new DictGuiChest();
        public DictCommonItems commonItems = new DictCommonItems();

        public class DictGuiReplicator {
            public String title = "<gradient:#cc22ff:#cc99ff><bold>REPLICATOR</gradient>";
            public String uniqueItemWarning = "Your {0} is or contains a unique item!";
            public String inputItemDisplayName = "<gold>Replicator Input";
            public List<String> inputItemLoreCooldown = List.of("Replicator input on cooldown.");
            public List<String> inputItemLoreNoItemSelected = List.of("<gray>No item selected.", "<dark_red>Drag an item into this slot.");
            public List<String> inputItemLoreItemSelected = List.of("<white>Set Item: {0}", "<dark_green>Replication Ready!");
        }

        public class DictGuiInventory {
            public String title = "<gradient:#cc22ff:#cc99ff><bold>YOUR INVENTORY</gradient>";
        }

        public class DictGuiChest {
            public String title = "<gradient:#cc22ff:#cc99ff><bold>DUPE CHEST</gradient>";
        }

        public class DictCommonItems {
            public String refillName = "<yellow>Item Refilling...";
            public String uniqueName = "<red>UNIQUE ITEM";
            public String uniqueLore = "<gray>You are unable to dupe <white>{0}";
        }
    }

    public class DictItemModificationEvents {
        public String dropInfiniteItem = "You have dropped your infinite {0}!";
        public String craftProtectedItem = "You cannot craft protected items!";
        public String modifyFinalItem = "You cannot modify final items!";
        public String useProtectedItem = "You cannot use protected items!";
        public String placeProtectedItem = "You cannot place protected items!";
        public String placeFinalBanner = "You cannot place final banners!";
        public String fillFinalItem = "You cannot fill final items!";
        public String drainFinalBucket = "You cannot drain final buckets!";
        public String drainProtectedBucket = "You cannot drain protected buckets!";
        public String fillFinalBucket = "You cannot fill final buckets!";
        public String fillProtectedBucket = "You cannot fill protected buckets!";
        public String fishFinalBucket = "You cannot fish with final buckets!";
        public String fishProtectedBucket = "You cannot fish with protected buckets!";
        public String consumeProtectedItem = "You cannot consume protected items!";
        public String useFinalBow = "You cannot use final bows!";
        public String shootProtectedItem = "You cannot shoot protected items!";
    }

    public class DictDupeCommand {
        public String noPermission = "You are not allowed to dupe via commands.";
        public String onCooldown = "You can command dupe again in {0}.";
        public String dupeLimitExceeded = "Your maximum permitted dupe amplifier is {0}!";
        public String noItemHeld = "You must hold an item to duplicate it with commands.";
        public String uniqueItemWarning = "Your {0} is or contains a unique item that cannot be duped!";
        public String inventoryFull = "Your inventory is now full.";
        public String successMessage = "You have duplicated {0} items!";
        public String invalidNumber = "{0} is not a valid number.";
    }
}