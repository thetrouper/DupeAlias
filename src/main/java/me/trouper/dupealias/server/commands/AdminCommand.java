package me.trouper.dupealias.server.commands;

import me.trouper.alias.server.commands.Args;
import me.trouper.alias.server.commands.CommandRegistry;
import me.trouper.alias.server.commands.Permission;
import me.trouper.alias.server.commands.QuickCommand;
import me.trouper.alias.server.commands.completions.CompletionBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.GlobalRule;
import me.trouper.dupealias.server.ItemTag;
import me.trouper.dupealias.server.gui.admin.AdminPanelManager;
import me.trouper.dupealias.server.gui.admin.MainAdminGui;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandRegistry(
        value = "dupealias",
        permission = @Permission(value = "dupealias.admin", message = "Only server administrators can use this command."),
        usage = "/da <gui|tag|rule> [unique|final|infinite|useless] [<material>|global|remove] [remove]",
        blocksAllowed = false,
        printStackTrace = true
)
public class AdminCommand implements QuickCommand, DupeContext {

    @Override
    public void handleCommand(CommandSender sender, Command command, String label, Args args) {
        if (args.isEmpty()) {
            openBaseGui(sender);
            return;
        }

        switch (args.get(0).toString()) {
            case "debug" -> {
                handleDebug(sender,args);
            }

            case "gui" -> {
                openBaseGui(sender);
            }

            case "tag" -> {
                handleTag(sender,args);
            }

            case "rule" -> {
                handleRule(sender,args);
            }

            default -> {
                errorAny(sender,"Invalid subcommand!");
            }
        }
    }

    @Override
    public void handleCompletion(CommandSender sender, Command command, String label, Args args, CompletionBuilder b) {
        quickDebugArgs(b,getCommonConfig().debuggerExclusions.stream().toList())
                .then(
                        b.arg("tag")
                                .then(b.argEnum(ItemTag.class)
                                        .then(
                                                b.argEnum(Material.class)
                                                        .then(
                                                                b.arg("remove") // Global Scope
                                                        )
                                        ).then(
                                                b.arg("global")
                                                        .then(
                                                                b.arg("remove") // Global Scope
                                                        )
                                        ).then(
                                                b.arg("remove","false")
                                        )
                                )
                ).then(
                        b.arg("rule")
                                .then(
                                        b.arg("create")
                                                .then(
                                                        b.argEnum(ItemTag.class)
                                                )
                                ).then(
                                        b.arg("list")
                                )
                                .then(
                                        b.arg("remove")
                                                .then(
                                                        b.arg("<rule_index>")
                                                )
                                )
                ).then(
                        b.arg("gui")
                );
    }


    private void handleDebug(CommandSender sender, Args args) {
        if (args.getSize() < 2) {
            errorAny(sender, "Usage: debug <toggle|include|exclude>");
            return;
        }

        final String sub = args.get(1).toString();

        switch (sub) {
            case "toggle" -> {
                boolean result = false;
                getCommonConfig().debugMode = result = !getCommonConfig().debugMode;
                getCommonConfig().save();

                getInstance().updateCommon();

                successAny(sender,"Toggled debug mode {0}.",result ? "on" : "off");
            }
            case "exclude" -> {
                if (args.getSize() < 3) {
                    errorAny(sender, "Usage: debug exclude <method>");
                    return;
                }
                final String exclusion = args.get(2).toString();
                getCommonConfig().debuggerExclusions.add(exclusion);
                getCommonConfig().save();

                getInstance().updateCommon();

                successAny(sender, "Excluded {0} from the debugger.", exclusion);
            }
            case "include" -> {
                if (args.getSize() < 3) {
                    errorAny(sender, "Usage: debug include <method>");
                    return;
                }
                final String exclusion = args.get(2).toString();
                getCommonConfig().debuggerExclusions.remove(exclusion);
                getCommonConfig().save();

                getInstance().updateCommon();

                successAny(sender, "Removed exclusion for {0} on the debugger.", exclusion);
            }
        }
    }

    private void handleRule(CommandSender sender, Args args) {
        if (args.getSize() < 2) {
            errorAny(sender, "Usage: /da rule <create|list|remove|info> ...");
            return;
        }

        String subCommand = args.get(1).toString().toLowerCase();

        switch (subCommand) {
            case "create" -> {
                if (args.getSize() < 3) {
                    errorAny(sender, "Usage: /da rule create <tag>");
                    return;
                }

                final ItemTag tag;
                try {
                    tag = args.get(2).toEnum(ItemTag.class);
                } catch (IllegalArgumentException e) {
                    errorAny(sender, "Argument '{0}' is not a valid item tag.", args.get(2).toString());
                    return;
                }

                GlobalRule rule = getDupe().createGlobalRule(tag);
                successAny(sender, "Created new global rule (#{0}) that applies {1} tag. Use /da gui to configure matching criteria.",
                        getConfig().globalRules.indexOf(rule), tag.getName());
            }

            case "list" -> {
                if (getConfig().globalRules.isEmpty()) {
                    infoAny(sender, "No global rules are currently configured.");
                    return;
                }

                infoAny(sender, "Global Rules ({0}):", getConfig().globalRules.size());
                for (int i = 0; i < getConfig().globalRules.size(); i++) {
                    GlobalRule rule = getConfig().globalRules.get(i);
                    StringBuilder tagList = new StringBuilder();
                    for (ItemTag tag : rule.appliedTags) {
                        if (tagList.length() > 0) tagList.append(", ");
                        tagList.append(tag.getName());
                    }
                    infoAny(sender, "  #{0}: Tags: {1}, Match Mode: {2}, Material Mode: {3}",
                            i, tagList.toString(), rule.matchMode, rule.materialMode);
                }
            }

            case "remove" -> {
                if (args.getSize() < 3) {
                    errorAny(sender, "Usage: /da rule remove <rule_index>");
                    return;
                }

                try {
                    int index = Integer.parseInt(args.get(2).toString());
                    if (index < 0 || index >= getConfig().globalRules.size()) {
                        errorAny(sender, "Invalid rule index. Use '/da rule list' to see available rules.");
                        return;
                    }

                    GlobalRule removedRule = getConfig().globalRules.remove(index);
                    getConfig().save();

                    StringBuilder tagList = new StringBuilder();
                    for (ItemTag tag : removedRule.appliedTags) {
                        if (tagList.length() > 0) tagList.append(", ");
                        tagList.append(tag.getName());
                    }

                    successAny(sender, "Removed global rule #{0} (Tags: {1}).", index, tagList.toString());
                } catch (NumberFormatException e) {
                    errorAny(sender, "'{0}' is not a valid number.", args.get(2).toString());
                }
            }

            case "info" -> {
                if (args.getSize() < 3) {
                    errorAny(sender, "Usage: /da rule info <rule_index>");
                    return;
                }

                try {
                    int index = Integer.parseInt(args.get(2).toString());
                    if (index < 0 || index >= getConfig().globalRules.size()) {
                        errorAny(sender, "Invalid rule index. Use '/da rule list' to see available rules.");
                        return;
                    }

                    GlobalRule rule = getConfig().globalRules.get(index);
                    infoAny(sender, "Global Rule #{0}:", index);
                    infoAny(sender, "  Applied Tags: {0}", rule.appliedTags.stream().map(ItemTag::getName).reduce((a,b) -> a + ", " + b).orElse("None"));
                    infoAny(sender, "  Match Mode: {0}", rule.matchMode);
                    infoAny(sender, "  Material Mode: {0}", rule.materialMode);
                    infoAny(sender, "  Affected Materials: {0}", rule.effectedMaterials.size());
                    infoAny(sender, "  Name Regex: {0}", rule.nameContainsRegex.isEmpty() ? "None" : rule.nameContainsRegex);
                    infoAny(sender, "  Lore Regex: {0}", rule.loreContainsRegex.isEmpty() ? "None" : rule.loreContainsRegex);
                    infoAny(sender, "  Enchantments: {0}", rule.enchantments.size());
                    infoAny(sender, "  Potion Effects: {0}", rule.potionEffects.size());
                    infoAny(sender, "  Attributes: {0}", rule.attributes.size());
                    infoAny(sender, "Use the GUI for detailed configuration.");
                } catch (NumberFormatException e) {
                    errorAny(sender, "'{0}' is not a valid number.", args.get(2).toString());
                }
            }

            default -> {
                errorAny(sender, "Invalid subcommand '{0}'. Valid options: create, list, remove, info", subCommand);
            }
        }
    }

    private void handleTag(CommandSender sender, Args args) {
        if (args.getSize() < 2) {
            errorAny(sender, "You must specify an item tag. Usage: /da tag <tag> ...");
            return;
        }

        // Argument 1
        final ItemTag tag;
        try {
            tag = args.get(1).toEnum(ItemTag.class);
        } catch (IllegalArgumentException e) {
            errorAny(sender, "Argument '{0}' is not a valid item tag.", args.get(1).toString());
            return;
        }

        // da tag <tag>
        if (args.getSize() == 2) {
            if (!(sender instanceof Player player)) {
                errorAny(sender, "This command can only be run by a player to tag a held item. To manage material tags, specify a material or 'global'.");
                return;
            }
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem.getType().isAir()) {
                errorAny(sender, "You must be holding an item to tag it.");
                return;
            }
            if (getDupe().addTag(heldItem, tag)) {
                successAny(sender, "Your {0} is now tagged as {1} and {2}.", heldItem.getType(), tag.getName(), tag.getDesc());
                return;
            } else {
                infoAny(sender,"Your {0} already has the {1} tag.",heldItem.getType(),tag.getName());
            }

        }

        // Argument 2
        String subCommand = args.get(2).toString().toLowerCase();

        // da tag <tag> remove|false
        switch (subCommand) {
            case "remove" -> {
                if (args.getSize() != 3) {
                    errorAny(sender, "Invalid arguments. Usage: /da tag <tag> remove");
                    return;
                }
                if (!(sender instanceof Player player)) {
                    errorAny(sender, "This command can only be run by a player to remove a tag from a held item.");
                    return;
                }
                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem.getType().isAir()) {
                    errorAny(sender, "You must be holding an item to remove its tag.");
                    return;
                }
                if (getDupe().removeTag(heldItem, tag)) {
                    successAny(sender, "Removed tag {0} from your {1}.", tag.getName(), heldItem.getType());
                    return;
                } else {
                    infoAny(sender,"Your {0} does not have the {1} tag.",heldItem.getType(),tag.getName());
                }
            }
            case "false" -> {
                if (args.getSize() != 3) {
                    errorAny(sender, "Invalid arguments. Usage: /da tag <tag> false");
                    return;
                }
                if (!(sender instanceof Player player)) {
                    errorAny(sender, "This command can only be run by a player to set a tag on a held item.");
                    return;
                }
                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem.getType().isAir()) {
                    errorAny(sender, "You must be holding an item to set its tag.");
                    return;
                }
                getDupe().setTag(heldItem, tag, false);
                successAny(sender, "Set tag {0} on your {1} to {2}.", tag.getName(), heldItem.getType(), "false");
                return;
            }


            // da tag <tag> global [remove]
            case "global" -> {
                if (!(sender instanceof Player player)) {
                    errorAny(sender, "The 'global' subcommand must be run by a player.");
                    return;
                }
                Material heldMaterial = player.getInventory().getItemInMainHand().getType();
                if (heldMaterial.isAir()) {
                    errorAny(sender, "You must be holding an item to use the 'global' subcommand.");
                    return;
                }

                boolean isRemove = args.getSize() > 3 && "remove".equalsIgnoreCase(args.get(3).toString());
                if (isRemove) {
                    if (args.getSize() != 4) {
                        errorAny(sender, "Invalid arguments. Usage: /da tag <tag> global remove");
                        return;
                    }
                    if (getDupe().removeGlobalRulesForMaterial(heldMaterial, tag)) {
                        successAny(sender, "Removed global rules applying tag {0} to {1} items.", tag.getName(), heldMaterial);
                    } else {
                        infoAny(sender, "No global rules found applying {0} tag to {1}.", tag.getName(), heldMaterial);
                    }
                } else {
                    if (args.getSize() != 3) {
                        errorAny(sender, "Invalid arguments. Usage: /da tag <tag> global");
                        return;
                    }
                    if (getDupe().addGlobalRuleForMaterial(heldMaterial, tag)) {
                        successAny(sender, "Created global rule: all {0} items are now tagged as {1} and {2}.", heldMaterial, tag.getName(), tag.getDesc());
                    } else {
                        infoAny(sender, "A global rule already exists that tags {0} items as {1}.", heldMaterial, tag.getName());
                    }
                }
                return;
            }
        }

        // da tag <tag> <material> [remove]
        try {
            Material material = args.get(2).toEnum(Material.class);
            boolean isRemove = args.getSize() > 3 && "remove".equalsIgnoreCase(args.get(3).toString());

            if (isRemove) {
                if (args.getSize() != 4) {
                    errorAny(sender, "Invalid arguments. Usage: /da tag <tag> <material> remove");
                    return;
                }
                if (getDupe().removeGlobalRulesForMaterial(material, tag)) {
                    successAny(sender, "Removed global rules applying tag {0} to {1}.", tag.getName(), material);
                } else {
                    infoAny(sender, "No global rules found applying {0} tag to {1}.", tag.getName(), material);
                }
            } else {
                if (args.getSize() != 3) {
                    errorAny(sender, "Invalid arguments. Usage: /da tag <tag> <material>");
                    return;
                }
                if (getDupe().addGlobalRuleForMaterial(material, tag)) {
                    successAny(sender, "Created global rule: all {0} items are now tagged as {1} and {2}.", material, tag.getName(), tag.getDesc());
                } else {
                    infoAny(sender, "A global rule already exists that tags {0} items as {1}.", material, tag.getName());
                }
            }
        } catch (IllegalArgumentException e) {
            errorAny(sender, "Invalid subcommand '{0}'. Expected 'remove', 'global', or a valid material name.", subCommand);
        }
    }


    public void openBaseGui(CommandSender sender) {
        if (sender instanceof Player player) {
            new MainAdminGui(new AdminPanelManager()).open(player);
        } else {
            errorAny(sender, "Console may not open a GUI.");
        }
    }

}