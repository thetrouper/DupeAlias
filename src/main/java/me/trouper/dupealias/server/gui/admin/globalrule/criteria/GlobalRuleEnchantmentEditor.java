package me.trouper.dupealias.server.gui.admin.globalrule.criteria;

import me.trouper.alias.data.enums.ValidEnchantment;
import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.server.systems.gui.QuickPaginatedGUI;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.GlobalRule;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.AdminPanelManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class GlobalRuleEnchantmentEditor extends QuickPaginatedGUI<ValidEnchantment> implements DupeContext, CommonItems {

    private final AdminPanelManager manager;
    private final GlobalRule rule;

    public GlobalRuleEnchantmentEditor(AdminPanelManager manager, GlobalRule rule) {
        this.manager = manager;
        this.rule = rule;
    }

    @Override
    protected String getTitle(Player player) {
        return "<gradient:#e91e63:#c2185b><bold>Enchantment Criteria</bold></gradient>";
    }

    @Override
    protected List<ValidEnchantment> getAllItems(Player player) {
        return Arrays.asList(ValidEnchantment.values());
    }

    @Override
    protected ItemStack createDisplayItem(ValidEnchantment enchant) {
        Integer level = rule.enchantments.get(enchant);
        boolean hasEnchant = level != null;

        ItemBuilder builder = ItemBuilder.create(hasEnchant ? Material.ENCHANTED_BOOK : Material.BOOK)
                .displayName((hasEnchant ? "<light_purple>" : "<gray>") + "<bold>" + enchant.name())
                .loreMiniMessage("<gray>Enchantment: " + enchant.name());

        if (hasEnchant) {
            builder.loreMiniMessage(
                    "",
                    "<white>Required Level: <light_purple>" + level,
                    "",
                    "<yellow>▶ <white>Left-click to change level",
                    "<yellow>▶ <white>Right-click to remove"
            );
        } else {
            builder.loreMiniMessage(
                    "",
                    "<gray>Not required",
                    "",
                    "<yellow>▶ <white>Click to add requirement"
            );
        }

        return builder.build();
    }

    @Override
    protected void handleItemClick(Player player, ValidEnchantment enchant, InventoryClickEvent event) {
        if (event.isRightClick() && rule.enchantments.containsKey(enchant)) {
            rule.enchantments.remove(enchant);
            getConfig().save();
            successAny(player, "Removed {0} requirement", enchant.name());
            createGUI(player).open(player);
        } else {
            QuickGui inputGui = QuickGui.create()
                    .titleMini("<gradient:#e91e63:#c2185b><bold>Set Enchantment Level</bold></gradient>")
                    .rows(3)
                    .item(13, ItemBuilder.create(Material.EXPERIENCE_BOTTLE)
                            .displayName("<light_purple><bold>" + enchant.name())
                            .loreMiniMessage(Arrays.asList(
                                    "<gray>Enter the minimum level",
                                    "<gray>required for this enchantment",
                                    "",
                                    "<white>Current: <light_purple>" +
                                            (rule.enchantments.containsKey(enchant) ? rule.enchantments.get(enchant) : "Not set"),
                                    "",
                                    "<yellow>▶ <white>Click to set level"
                            ))
                            .build(), (g, e) -> getDupe().getGuiListener().requestChatInput(g,player, "enchantLevel","Enter minimum enchant level starting at 1."))
                    .item(22, BACK(), (g, e) -> createGUI(player).open(player))
                    .fillEmpty(EMPTY())
                    .callback("enchantLevel", new QuickGui.GuiCallback() {
                        @Override
                        public void onInput(QuickGui gui, Player player, String input, QuickGui.InputSource source) {
                            try {
                                int level = Integer.parseInt(input);
                                if (level < 1) {
                                    errorAny(player, "Level must be at least 1");
                                } else {
                                    rule.enchantments.put(enchant, level);
                                    getConfig().save();
                                    successAny(player, "Set {0} requirement to level {1}", enchant.name(), level);
                                }
                            } catch (NumberFormatException ex) {
                                errorAny(player, "Invalid number: {0}", input);
                            }
                            createGUI(player).open(player);
                        }
                    })
                    .build();
            inputGui.open(player);
        }
    }

    @Override
    protected void addFilterItems(QuickGui.GuiBuilder filterGui, Player player, Set<String> filters) {
        filterGui.item(0, createFilterToggleItem("Selected Only", Material.LIME_DYE, filters.contains("S")),
                (gui, event) -> toggleFilter(player, "S"));
        filterGui.item(1, createFilterToggleItem("Weapon", Material.DIAMOND_SWORD, filters.contains("W")),
                (gui, event) -> toggleFilter(player, "W"));
        filterGui.item(2, createFilterToggleItem("Tool", Material.DIAMOND_PICKAXE, filters.contains("T")),
                (gui, event) -> toggleFilter(player, "T"));
        filterGui.item(3, createFilterToggleItem("Armor", Material.DIAMOND_CHESTPLATE, filters.contains("A")),
                (gui, event) -> toggleFilter(player, "A"));
    }

    @Override
    protected boolean testFilter(Player player, ValidEnchantment enchant, String filterKey) {
        return switch (filterKey) {
            case "S" -> rule.enchantments.containsKey(enchant);
            case "W" -> enchant.name().contains("SHARPNESS") || enchant.name().contains("SMITE") ||
                    enchant.name().contains("BANE") || enchant.name().contains("KNOCKBACK") ||
                    enchant.name().contains("FIRE_ASPECT") || enchant.name().contains("LOOTING") ||
                    enchant.name().contains("SWEEPING");
            case "T" -> enchant.name().contains("EFFICIENCY") || enchant.name().contains("SILK_TOUCH") ||
                    enchant.name().contains("UNBREAKING") || enchant.name().contains("FORTUNE");
            case "A" -> enchant.name().contains("PROTECTION") || enchant.name().contains("THORNS") ||
                    enchant.name().contains("RESPIRATION") || enchant.name().contains("AQUA_AFFINITY") ||
                    enchant.name().contains("FEATHER_FALLING") || enchant.name().contains("DEPTH_STRIDER") ||
                    enchant.name().contains("FROST_WALKER");
            default -> false;
        };
    }

    @Override
    protected void openBackGUI(Player player) {
        manager.openGlobalRuleEditor(player, rule);
    }
}