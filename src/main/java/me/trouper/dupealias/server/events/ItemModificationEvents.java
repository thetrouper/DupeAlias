package me.trouper.dupealias.server.events;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import me.trouper.alias.server.events.QuickListener;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.ItemTag;
import net.kyori.adventure.audience.Audience;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.view.MerchantView;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ItemModificationEvents implements QuickListener, DupeContext {


    @EventHandler(priority = EventPriority.HIGHEST)
    public void prepareCraftEvent(PrepareItemCraftEvent event) {
        getVerbose().send("Checking crafting matrix");
        ItemStack result = event.getInventory().getResult();

        if (getDupe().checkEffectiveTag(result, ItemTag.PROTECTED)) {
            event.getInventory().setResult(null);
            warningAny(Audience.audience(event.getViewers()), "You cannot craft protected items!");
            return;
        }

        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ingredient == null || ingredient.isEmpty()) continue;
            if (getDupe().checkEffectiveTag(ingredient, ItemTag.FINAL)) {
                if (isModifyingCraft(ingredient, result)) {
                    event.getInventory().setResult(null);
                    warningAny(Audience.audience(event.getViewers()), "You cannot modify final items!");
                    return;
                }
            }
            if (getDupe().checkEffectiveTag(ingredient, ItemTag.PROTECTED)) {
                event.getInventory().setResult(null);
                warningAny(Audience.audience(event.getViewers()), "You cannot use protected items!");
                return;
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSmithingTableUse(PrepareSmithingEvent event) {
        getVerbose().send("Smithing Event");
        ItemStack result = event.getResult();
        ItemStack base = event.getInventory().getItem(1);
        ItemStack addition = event.getInventory().getItem(2);

        // Prevent creating PROTECTED items
        if (getDupe().checkEffectiveTag(result, ItemTag.PROTECTED)) {
            event.setResult(null);
            return;
        }

        // Prevent modifying FINAL items (base or addition)
        if (getDupe().checkEffectiveTag(base, ItemTag.FINAL) || getDupe().checkEffectiveTag(addition, ItemTag.FINAL)) {
            // A smithing recipe always modifies the base item.
            event.setResult(null);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnchantItem(EnchantItemEvent event) {
        getVerbose().send("Enchant Event");
        ItemStack item = event.getItem();

        if (getDupe().checkEffectiveTag(item, ItemTag.FINAL)) {
            event.setCancelled(true);
            warningAny(event.getEnchanter(), "You cannot modify final items!");
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        getVerbose().send("Enchant Prepare Event");
        ItemStack item = event.getItem();
        if (getDupe().checkEffectiveTag(item, ItemTag.FINAL)) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilUse(PrepareAnvilEvent event) {
        getVerbose().send("Anvil Prepare Event");
        AnvilInventory inventory = event.getInventory();
        ItemStack first = inventory.getItem(0);
        ItemStack second = inventory.getItem(1);
        ItemStack result = event.getResult();

        // Prevent modifying a FINAL item
        if (getDupe().checkEffectiveTag(first, ItemTag.FINAL) || getDupe().checkEffectiveTag(second, ItemTag.FINAL)) {
            if (isModifyingCraft(first, result) || isModifyingCraft(second, result)) {
                event.setResult(null);
                return;
            }
        }

        // Prevent creating PROTECTED items
        if (getDupe().checkEffectiveTag(result, ItemTag.PROTECTED)) {
            event.setResult(null);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void furnaceBurnEvent(FurnaceBurnEvent event) {
        Block block = event.getBlock();
        BlockState state = block.getState();
        BlockInventoryHolder holder = (BlockInventoryHolder) state;
        Furnace furnace = (Furnace) holder;
        if (dropIllegalSlots(furnace)) {
            event.setCancelled(true);
            event.setBurning(false);
        }
    }


    @EventHandler
    public void brewingStandFuel(BrewingStandFuelEvent event) {
        Block block = event.getBlock();
        BlockState state = block.getState();
        BlockInventoryHolder holder = (BlockInventoryHolder) state;
        BrewingStand stand = (BrewingStand) holder;
        if (dropIllegalSlots(stand)) {
            event.setCancelled(true);
            event.setFuelPower(0);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFurnace(InventoryBlockStartEvent event) {
        Block block = event.getBlock();
        BlockState state = block.getState();
        if (state instanceof Campfire campfire) {
            ItemStack source = event.getSource().clone();
            if (getDupe().checkEffectiveTag(source, ItemTag.FINAL) || getDupe().checkEffectiveTag(source, ItemTag.PROTECTED)) {
                block.setType(Material.AIR);
                block.setType(campfire.getType());
                block.setBlockData(campfire.getBlockData());
                source.setAmount(1);
                block.getWorld().dropItem(block.getLocation(), source);

            }
            return;
        }
        BlockInventoryHolder holder = (BlockInventoryHolder) state;
        switch (holder) {
            case Furnace furnace -> dropIllegalSlots(furnace);
            case BrewingStand stand -> dropIllegalSlots(stand);
            default -> {
            }
        }
    }


    private boolean dropIllegalSlots(BlockInventoryHolder holder) {
        int[] dropProtected;
        int[] dropFinal;
        boolean result = false;
        switch (holder) {
            case BrewingStand ignored -> {
                dropProtected = new int[]{4, 3};
                dropFinal = new int[]{0, 1, 2};
            }
            case Furnace ignored -> {
                dropProtected = new int[]{0, 1};
                dropFinal = new int[]{0};
            }
            default -> {
                return true;
            }
        }
        for (int i : dropProtected) {
            if (dropIfTag(holder, i, ItemTag.PROTECTED) && !result) result = true;
        }
        for (int i : dropFinal) {
            if (dropIfTag(holder, i, ItemTag.FINAL) && !result) result = true;
        }
        return result;
    }

    private boolean dropIfTag(BlockInventoryHolder holder, int slot, ItemTag tag) {
        ItemStack item = holder.getInventory().getItem(slot);
        if (item != null && getDupe().checkEffectiveTag(item, tag)) {
            holder.getInventory().setItem(slot, new ItemStack(Material.AIR));
            holder.getBlock().getWorld().dropItem(holder.getBlock().getLocation(), item);
            return true;
        }
        return false;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpecialCraft(PrepareItemCraftEvent event) {
        getVerbose().send("Special Craft");

        CraftingInventory inv = event.getInventory();
        ItemStack result = inv.getResult();

        // Prevent creating PROTECTED items in any of these custom inventories
        if (getDupe().checkEffectiveTag(result, ItemTag.PROTECTED)) {
            inv.setResult(null);
            return;
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void resultEvent(PrepareResultEvent event) {
        getVerbose().send("Result Event");
        ItemStack result = event.getResult();
        if (getDupe().checkEffectiveTag(result, ItemTag.PROTECTED)) {
            event.setResult(null);
            return;
        }

        Inventory inv = event.getInventory();
        getVerbose().send("Inv: {0}", inv.getType());
        // Handle FINAL item modification logic for each type
        switch (inv) {
            case LoomInventory loom -> {
                if (getDupe().checkEffectiveTag(loom.getItem(1), ItemTag.PROTECTED) || getDupe().checkEffectiveTag(loom.getItem(2), ItemTag.PROTECTED)) {
                    warningAny(Audience.audience(event.getViewers()), "You cannot use a protected item!");
                    event.getInventory().close();
                }
                if (getDupe().checkEffectiveTag(loom.getItem(0), ItemTag.FINAL)) {
                    warningAny(Audience.audience(event.getViewers()), "You cannot modify a final item!");
                    event.getInventory().close();
                }
            }
            case CartographyInventory carto -> {
                if (getDupe().checkEffectiveTag(carto.getResult(), ItemTag.PROTECTED)) {
                    warningAny(Audience.audience(event.getViewers()), "You cannot use a protected item!");
                    event.getInventory().close();
                }
                if (getDupe().checkEffectiveTag(carto.getItem(0), ItemTag.FINAL) || getDupe().checkEffectiveTag(carto.getItem(1), ItemTag.FINAL)) {
                    warningAny(Audience.audience(event.getViewers()), "You cannot modify a final item!");
                    event.getInventory().close();
                }
            }
            case GrindstoneInventory grind -> {
                if (getDupe().checkEffectiveTag(grind.getResult(), ItemTag.PROTECTED)) {
                    warningAny(Audience.audience(event.getViewers()), "You cannot use a protected item!");
                    event.getInventory().close();
                }
                if (getDupe().checkEffectiveTag(grind.getItem(0), ItemTag.FINAL) || getDupe().checkEffectiveTag(grind.getItem(1), ItemTag.FINAL)) {
                    warningAny(Audience.audience(event.getViewers()), "You cannot modify a final item!");
                    event.getInventory().close();
                }
            }
            case StonecutterInventory stone -> {
                if (getDupe().checkEffectiveTag(stone.getResult(), ItemTag.PROTECTED)) {
                    warningAny(Audience.audience(event.getViewers()), "You cannot use a protected item!");
                    event.getInventory().close();
                }
                if (getDupe().checkEffectiveTag(stone.getItem(0), ItemTag.FINAL)) {
                    warningAny(Audience.audience(event.getViewers()), "You cannot modify a final item!");
                    event.getInventory().close();
                }
            }
            default -> {
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGrindStone(PrepareGrindstoneEvent event) {
        getVerbose().send("Grindstone Event");
        GrindstoneInventory grind = event.getInventory();
        if (getDupe().checkEffectiveTag(grind.getResult(), ItemTag.PROTECTED)) {
            grind.setResult(null);
        }
        if (getDupe().checkEffectiveTag(grind.getItem(0), ItemTag.FINAL) || getDupe().checkEffectiveTag(grind.getItem(1), ItemTag.FINAL)) {
            grind.setResult(null);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCauldron(CauldronLevelChangeEvent event) {
        getVerbose().send("Cauldron Event");
        if (event.getEntity() instanceof Player player) {
            ItemStack main = player.getInventory().getItemInMainHand();
            ItemStack off = player.getInventory().getItemInOffHand();
            if (main.getType().equals(Material.BUCKET) || main.getType().equals(Material.GLASS_BOTTLE) || main.getType().name().contains("BANNER") || main.getType().name().contains("ARMOR")) {
                if (getDupe().checkEffectiveTag(main, ItemTag.FINAL) || getDupe().checkEffectiveTag(main, ItemTag.FINAL)) {
                    event.setCancelled(true);
                    warningAny(player, "That item is final and cannot be modified!");
                }
            } else if (off.getType().equals(Material.BUCKET) || off.getType().equals(Material.GLASS_BOTTLE) || off.getType().name().contains("BANNER") || off.getType().name().contains("ARMOR")) {
                if (getDupe().checkEffectiveTag(off, ItemTag.FINAL) || getDupe().checkEffectiveTag(off, ItemTag.FINAL)) {
                    event.setCancelled(true);
                    warningAny(player, "That item is final and cannot be modified!");
                }
            }

        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        ItemStack main = e.getPlayer().getInventory().getItemInMainHand();
        ItemStack off = e.getPlayer().getInventory().getItemInOffHand();
        if (!getDupe().checkEffectiveTag(main, ItemTag.FINAL) && !getDupe().checkEffectiveTag(off, ItemTag.FINAL))
            return;

        String command = e.getMessage();
        for (String finalCommandRegex : getConfig().finalCommandRegex) {
            if (command.matches(finalCommandRegex)) {
                e.setCancelled(true);
                warningAny(e.getPlayer(), "That item is final and cannot be modified!");
                return;
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickUp(EntityPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();

        if (!(event.getEntity() instanceof Player)) {
            if (getDupe().checkEffectiveTag(item, ItemTag.PROTECTED)) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (getDupe().checkEffectiveTag(item, ItemTag.PROTECTED)) {
            event.setCancelled(true);
            warningAny(event.getPlayer(), "You cannot place protected items!");
        }
        if (item.getItemMeta() instanceof BannerMeta && getDupe().checkEffectiveTag(item, ItemTag.FINAL)) {
            event.setCancelled(true);
            warningAny(event.getPlayer(), "You cannot place final banners!");
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (getDupe().checkEffectiveTag(item, ItemTag.PROTECTED)) {
            event.setCancelled(true);
            warningAny(event.getPlayer(), "You cannot use protected items!");
        }

        Block targetBlock = event.getPlayer().getTargetBlockExact(4, FluidCollisionMode.ALWAYS);
        if (targetBlock != null && targetBlock.getType() == Material.WATER) {
            if (getDupe().checkEffectiveTag(item, ItemTag.FINAL) && item != null && (item.getType().equals(Material.GLASS_BOTTLE) || item.getType().equals(Material.BUCKET))) {
                event.setCancelled(true);
                warningAny(event.getPlayer(), "You cannot fill final items!");
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        boolean isFinal = getDupe().checkEffectiveTag(item, ItemTag.FINAL);
        boolean isProtected = getDupe().checkEffectiveTag(item, ItemTag.PROTECTED);
        if (isProtected || isFinal) {
            String message = isFinal ? "You cannot drain final buckets!" : "You cannot drain protected buckets!";
            warningAny(event.getPlayer(), message);
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketFill(PlayerBucketFillEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        boolean isFinal = getDupe().checkEffectiveTag(item, ItemTag.FINAL);
        boolean isProtected = getDupe().checkEffectiveTag(item, ItemTag.PROTECTED);
        if (isProtected || isFinal) {
            String message = isFinal ? "You cannot fill final buckets!" : "You cannot fill protected buckets!";
            warningAny(event.getPlayer(), message);
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onBucketFish(PlayerBucketEntityEvent event) {
        ItemStack item = event.getOriginalBucket();
        boolean isFinal = getDupe().checkEffectiveTag(item, ItemTag.FINAL);
        boolean isProtected = getDupe().checkEffectiveTag(item, ItemTag.PROTECTED);
        if (isProtected || isFinal) {
            String message = isFinal ? "You cannot fish with final buckets!" : "You cannot fish with protected buckets!";
            warningAny(event.getPlayer(), message);
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if (getDupe().checkEffectiveTag(item, ItemTag.PROTECTED)) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        if (getDupe().checkEffectiveTag(item, ItemTag.PROTECTED)) {
            event.setCancelled(true);
            warningAny(event.getPlayer(), "You cannot consume protected items!");
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBowShoot(EntityShootBowEvent event) {
        ItemStack bow = event.getBow();
        ItemStack consumable = event.getConsumable();
        boolean isFinal = getDupe().checkEffectiveTag(bow, ItemTag.FINAL);
        boolean isProtected = getDupe().checkEffectiveTag(bow, ItemTag.PROTECTED) || getDupe().checkEffectiveTag(consumable, ItemTag.PROTECTED);

        if (isFinal || isProtected) {
            event.getProjectile().remove();
            event.setCancelled(true);
            if (event.getEntity() instanceof Player player) {
                if (consumable != null) player.getInventory().addItem(consumable);
                String message = isFinal ? "You cannot use final bows!" : "You cannot shoot protected items!";
                warningAny(player, message);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player) {
            // This covers tridents, etc. held in hand
            ItemStack item = player.getInventory().getItemInMainHand();
            if (event.getEntity() instanceof ThrowableProjectile t) {
                item = t.getItem();
            }

            if (getDupe().checkEffectiveTag(item, ItemTag.PROTECTED)) {
                event.setCancelled(true);
                warningAny(player, "You cannot use protected items!");
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            ItemStack item = player.getInventory().getItemInMainHand();

            if (getDupe().checkEffectiveTag(item, ItemTag.PROTECTED)) {
                event.setCancelled(true);
                warningAny(player, "You cannot use protected items!");
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (getDupe().checkEffectiveTag(item, ItemTag.PROTECTED)) {
            event.setCancelled(true);
            warningAny(player, "You cannot use protected items!");
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDispense(BlockDispenseEvent event) {
        ItemStack item = event.getItem();

        if (getDupe().checkEffectiveTag(item, ItemTag.PROTECTED)) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (getDupe().checkEffectiveTag(item, ItemTag.FINAL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemMend(PlayerItemMendEvent event) {
        ItemStack item = event.getItem();
        if (getDupe().checkEffectiveTag(item, ItemTag.FINAL)) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onVillagerClick(TradeSelectEvent event) {
        MerchantView view = event.getView();
        ItemStack trade1 = view.getItem(0);
        ItemStack trade2 = view.getItem(1);
        ItemStack result = view.getItem(2);

        if (getDupe().checkEffectiveTag(trade1, ItemTag.PROTECTED) || getDupe().checkEffectiveTag(trade2, ItemTag.PROTECTED) || getDupe().checkEffectiveTag(result, ItemTag.PROTECTED)) {
            event.setCancelled(true);
            event.getInventory().close();
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();
        if (getDupe().checkEffectiveTag(item, ItemTag.PROTECTED)) {
            event.setCancelled(true);
        }
    }


    private boolean isModifyingCraft(ItemStack ingredient, ItemStack result) {
        if (ingredient == null || result == null) return false;
        getVerbose().send("Performing Heuristic check on {0} and {1}", ingredient.getType(), result.getType());

        if (ingredient.getType() != result.getType()) {
            return false;
        }

        if (ingredient.isSimilar(result) && ingredient.getAmount() == result.getAmount()) {
            return false;
        }

        if (result.getAmount() < ingredient.getAmount()) {
            return true;
        }

        ItemMeta inMeta = ingredient.getItemMeta();
        ItemMeta outMeta = result.getItemMeta();
        if (inMeta == null && outMeta == null) {
            return false;
        }
        if (inMeta == null || outMeta == null) {
            return true;
        }

        Map<String, Object> inMap = new HashMap<>(inMeta.serialize());
        Map<String, Object> outMap = new HashMap<>(outMeta.serialize());
        inMap.remove("material");
        inMap.remove("amount");
        outMap.remove("material");
        outMap.remove("amount");
        if (!inMap.equals(outMap)) {
            return true;
        }

        if (!Objects.equals(inMeta.getCustomModelData(), outMeta.getCustomModelData())) {
            return true;
        }
        if (!inMeta.getItemFlags().equals(outMeta.getItemFlags())) {
            return true;
        }

        if (inMeta instanceof Damageable inD && outMeta instanceof Damageable outD) {
            if (inD.getDamage() != outD.getDamage()) {
                return true;
            }
        }

        if (!inMeta.getEnchants().equals(outMeta.getEnchants())) {
            return true;
        }

        if (!Objects.equals(inMeta.getDisplayName(), outMeta.getDisplayName()) ||
                !Objects.equals(inMeta.getLore(), outMeta.getLore())) {
            return true;
        }

        Set<NamespacedKey> keysIn = inMeta.getPersistentDataContainer().getKeys();
        Set<NamespacedKey> keysOut = outMeta.getPersistentDataContainer().getKeys();
        if (!keysIn.equals(keysOut)) {
            return true;
        }
        for (NamespacedKey key : keysIn) {
            Object valIn = inMeta.getPersistentDataContainer().get(key, PersistentDataType.TAG_CONTAINER);
            Object valOut = outMeta.getPersistentDataContainer().get(key, PersistentDataType.TAG_CONTAINER);
            if (!Objects.equals(valIn, valOut)) {
                return true;
            }
        }

        if (inMeta instanceof BannerMeta inB && outMeta instanceof BannerMeta outB) {
            if (inB.getPatterns().size() != outB.getPatterns().size()) {
                return true;
            }
        }
        if (inMeta instanceof MapMeta inM && outMeta instanceof MapMeta outM) {
            boolean iLocked = inM.hasMapView() && inM.getMapView().isLocked();
            boolean oLocked = outM.hasMapView() && outM.getMapView().isLocked();
            if (iLocked != oLocked) return true;
            byte iScale = inM.hasMapView() ? inM.getMapView().getScale().getValue() : -1;
            byte oScale = outM.hasMapView() ? outM.getMapView().getScale().getValue() : -1;
            if (iScale != oScale) return true;
        }
        if (inMeta instanceof LeatherArmorMeta inL && outMeta instanceof LeatherArmorMeta outL) {
            if (!inL.getColor().equals(outL.getColor())) {
                return true;
            }
        }
        if (inMeta instanceof PotionMeta inP && outMeta instanceof PotionMeta outP) {
            if (!Objects.equals(inP.getBasePotionType(), outP.getBasePotionType())
                    || !inP.getCustomEffects().equals(outP.getCustomEffects())
                    || !Objects.equals(inP.getColor(), outP.getColor())) {
                return true;
            }
        }
        if (inMeta instanceof FireworkMeta inF && outMeta instanceof FireworkMeta outF) {
            if (inF.getEffects().size() != outF.getEffects().size()) {
                return true;
            }
        }
        if (inMeta instanceof BookMeta inBk && outMeta instanceof BookMeta outBk) {
            if (!Objects.equals(inBk.getTitle(), outBk.getTitle())
                    || !Objects.equals(inBk.getAuthor(), outBk.getAuthor())
                    || !inBk.pages().equals(outBk.pages())) {
                return true;
            }
        }
        if (inMeta instanceof CrossbowMeta inC && outMeta instanceof CrossbowMeta outC) {
            if (!inC.getChargedProjectiles().equals(outC.getChargedProjectiles())
                    || inC.hasChargedProjectiles() != outC.hasChargedProjectiles()) {
                return true;
            }
        }

        return false;
    }
}
