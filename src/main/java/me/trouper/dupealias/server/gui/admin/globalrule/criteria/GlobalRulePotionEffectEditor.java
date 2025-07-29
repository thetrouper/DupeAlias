package me.trouper.dupealias.server.gui.admin.globalrule.criteria;

import me.trouper.alias.data.enums.ValidPotionEffectType;
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

public class GlobalRulePotionEffectEditor extends QuickPaginatedGUI<ValidPotionEffectType> implements DupeContext, CommonItems {

    private final AdminPanelManager manager;
    private final GlobalRule rule;

    public GlobalRulePotionEffectEditor(AdminPanelManager manager, GlobalRule rule) {
        this.manager = manager;
        this.rule = rule;
    }

    @Override
    protected String getTitle(Player player) {
        return "<gradient:#9c27b0:#6a1b9a><bold>Potion Effect Criteria</bold></gradient>";
    }

    @Override
    protected List<ValidPotionEffectType> getAllItems(Player player) {
        return Arrays.asList(ValidPotionEffectType.values());
    }

    @Override
    protected ItemStack createDisplayItem(ValidPotionEffectType effect) {
        Integer amplifier = rule.potionEffects.get(effect);
        boolean hasEffect = amplifier != null;

        Material icon = effect.name().contains("INSTANT") || effect.name().contains("HARM") ?
                Material.SPLASH_POTION : Material.POTION;

        ItemBuilder builder = ItemBuilder.create(icon)
                .displayName((hasEffect ? "<light_purple>" : "<gray>") + "<bold>" + effect.name())
                .loreMiniMessage("<gray>Effect: " + effect.name());

        if (hasEffect) {
            builder.loreMiniMessage(
                    "",
                    "<white>Required Amplifier: <light_purple>" + amplifier,
                    "",
                    "<yellow>▶ <white>Left-click to change amplifier",
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
    protected void handleItemClick(Player player, ValidPotionEffectType effect, InventoryClickEvent event) {
        if (event.isRightClick() && rule.potionEffects.containsKey(effect)) {
            rule.potionEffects.remove(effect);
            getConfig().save();
            successAny(player, "Removed {0} requirement", effect.name());
            createGUI(player).open(player);
        } else {
            // Request amplifier input
            QuickGui inputGui = QuickGui.create()
                    .titleMini("<gradient:#9c27b0:#6a1b9a><bold>Set Effect Amplifier</bold></gradient>")
                    .rows(3)
                    .item(13, ItemBuilder.create(Material.BREWING_STAND)
                            .displayName("<light_purple><bold>" + effect.name())
                            .loreMiniMessage(Arrays.asList(
                                    "<gray>Enter the minimum amplifier",
                                    "<gray>required for this effect",
                                    "<gray>(0 = Level I, 1 = Level II, etc.)",
                                    "",
                                    "<white>Current: <light_purple>" +
                                            (rule.potionEffects.containsKey(effect) ? rule.potionEffects.get(effect) : "Not set"),
                                    "",
                                    "<yellow>▶ <white>Click to set amplifier"
                            ))
                            .build(), (g, e) -> getDupe().getGuiListener().requestChatInput(g,player, "effectAmplifier","Enter an integer to set the minimum effect amplifier. (Starting at 0)"))
                    .item(22, BACK(), (g, e) -> createGUI(player).open(player))
                    .fillEmpty(EMPTY())
                    .callback("effectAmplifier", new QuickGui.GuiCallback() {
                        @Override
                        public void onInput(QuickGui gui, Player player, String input, QuickGui.InputSource source) {
                            try {
                                int amplifier = Integer.parseInt(input);
                                if (amplifier < 0) {
                                    errorAny(player, "Amplifier must be 0 or higher");
                                } else {
                                    rule.potionEffects.put(effect, amplifier);
                                    getConfig().save();
                                    successAny(player, "Set {0} requirement to amplifier {1}", effect.name(), amplifier);
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
        filterGui.item(1, createFilterToggleItem("Beneficial", Material.GOLDEN_APPLE, filters.contains("B")),
                (gui, event) -> toggleFilter(player, "B"));
        filterGui.item(2, createFilterToggleItem("Harmful", Material.POISONOUS_POTATO, filters.contains("H")),
                (gui, event) -> toggleFilter(player, "H"));
        filterGui.item(3, createFilterToggleItem("Neutral", Material.MILK_BUCKET, filters.contains("N")),
                (gui, event) -> toggleFilter(player, "N"));
    }

    @Override
    protected boolean testFilter(Player player, ValidPotionEffectType effect, String filterKey) {
        return switch (filterKey) {
            case "S" -> rule.potionEffects.containsKey(effect);
            case "B" -> effect.name().contains("SPEED") || effect.name().contains("HASTE") ||
                    effect.name().contains("STRENGTH") || effect.name().contains("INSTANT_HEALTH") ||
                    effect.name().contains("JUMP") || effect.name().contains("REGENERATION") ||
                    effect.name().contains("RESISTANCE") || effect.name().contains("FIRE_RESISTANCE") ||
                    effect.name().contains("WATER_BREATHING") || effect.name().contains("INVISIBILITY") ||
                    effect.name().contains("NIGHT_VISION") || effect.name().contains("HEALTH_BOOST") ||
                    effect.name().contains("ABSORPTION") || effect.name().contains("SATURATION") ||
                    effect.name().contains("LUCK") || effect.name().contains("CONDUIT") ||
                    effect.name().contains("DOLPHINS") || effect.name().contains("HERO");
            case "H" -> effect.name().contains("SLOWNESS") || effect.name().contains("MINING_FATIGUE") ||
                    effect.name().contains("INSTANT_DAMAGE") || effect.name().contains("NAUSEA") ||
                    effect.name().contains("BLINDNESS") || effect.name().contains("HUNGER") ||
                    effect.name().contains("WEAKNESS") || effect.name().contains("POISON") ||
                    effect.name().contains("WITHER") || effect.name().contains("UNLUCK") ||
                    effect.name().contains("BAD_OMEN") || effect.name().contains("DARKNESS");
            case "N" -> !testFilter(player, effect, "B") && !testFilter(player, effect, "H");
            default -> false;
        };
    }

    @Override
    protected void openBackGUI(Player player) {
        manager.openGlobalRuleEditor(player, rule);
    }
}