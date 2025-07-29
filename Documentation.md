# DupeAlias Documentation

## Table of Contents

1. [Installation & Setup](#installation--setup)
2. [Core Concepts](#core-concepts)
3. [Item Tags System](#item-tags-system)
4. [Global Rules Engine](#global-rules-engine)
5. [Duplication GUIs](#duplication-guis)
6. [Permissions System](#permissions-system)
7. [Configuration](#configuration)
8. [Commands](#commands)
9. [Troubleshooting](#troubleshooting)

---   
## Installation & Setup

### Prerequisites
- Minecraft 1.21.5
- Paper server software
- Java 21 or higher

### Installation Steps
1. Download the DupeAlias JAR file
2. Place it in your server's `plugins/` directory
3. Restart your server
4. The plugin will generate default configuration files in `plugins/DupeAlias/`

### First-Time Configuration
1. Join your server as an operator
2. Run `/da` to open the admin panel
3. Navigate to Configuration → Common Config to customize colors and branding
4. Set up your first global rules or start tagging items manually

---   
## Core Concepts

### Item Tags
DupeAlias uses four primary tags that can be applied to items:

- **UNIQUE**: Prevents the item from being duplicated
- **FINAL**: Prevents the item from being modified in any way
- **INFINITE**: Keeps the item at maximum stack size (99)
- **PROTECTED**: Prevents the item from being used, consumed, or crafted with

### Tag Priority System
Individual item tags **always override** global rules. This allows for fine-grained control where you can set global rules for item types while making exceptions for specific items.

### Global Rules vs Individual Tags
- **Individual Tags**: Stored directly on the item's metadata, apply only to that specific item instance
- **Global Rules**: Server-wide rules that apply tags based on item properties like material, name, enchantments, etc.

---   
## Item Tags System

### UNIQUE Tag
**Purpose**: Prevents item duplication through intended methods

**Use Cases**:
- Crate keys and special tokens
- Rare items and rewards
- Admin-only equipment
- Currency items

**Conflicts**: Cannot be combined with INFINITE.

**Example**: A crate key that should never be duplicated

### FINAL Tag
**Purpose**: Prevents any modification to the item

**Blocks**:
- Renaming items
- Enchanting items
- Repairing items
- Anvil operations
- Any metadata changes

**Use Cases**:
- Quest items with specific names
- Rank kits that shouldn't be modified
- Event rewards with special formatting

### INFINITE Tag
**Purpose**: Maintains maximum stack size and refills items

**Behavior**:
- Sets item stack to 99 (max stackable amount)
- Refills automatically after use
- Works with blocks, consumables, and projectiles

**Use Cases**:
- Creative-style building materials
- "Infinity" enchantment for tipped arrows
- Unlimited consumables for events

**Conflicts**: Cannot be combined with UNIQUE or PROTECTED

### PROTECTED Tag
**Purpose**: Makes items completely inert and unusable

**Blocks**:
- Using items (right-click)
- Consuming food/potions
- Placing blocks
- Attacking with weapons
- Crafting with the item
- Trading with villagers

**Use Cases**:
- Display items
- Coupons and vouchers
- Decorative rewards
- Placeholder items

**Conflicts**: Cannot be combined with INFINITE

 ---   
## Global Rules Engine

### Creating Global Rules

1. Open admin panel with `/da`
2. Navigate to **Global Rules**
3. Click **+ Create New Rule**
4. Configure criteria and applied tags
5. Save the rule

### Rule Components

#### Applied Tags
Select which tags this rule will apply to matching items. Multiple tags can be selected for a single rule.

#### Match Mode
- **AND**: All criteria must match
- **OR**: Any criteria must match
- **NAND**: Not all criteria match
- **XOR**: Exactly one criteria matches

Note that if no criteria are selected, any material or ItemsAdder item will match.

#### Material Matching
- **IGNORE**: Apply to all materials and ItemsAdder items
- **WHITELIST**: Only apply to selected materials and ItemsAdder items
- **BLACKLIST**: Apply to all except selected materials and ItemsAdder items

### Criteria Types

#### Name Regex
Match items based on display name patterns using regular expressions.  
``` Example: ".*[Kk]ey.*" matches any item with "key" or "Key" in the name ```
#### Lore Regex
Match items based on lore content using regular expressions.  
``` Example: ".*Special.*" matches items with "Special" anywhere in lore ```
#### Compound Tag Regex
Match an item based on its compound tag generated from `ItemStack#getAsComponentString()`
```Example: ".*(nutrition).*|.*(saturation).*" Matches any custom food item```
#### NBT Tag Regex
Match an item based on its NBT tag generated from `ItemStack#getAsString()`
```Example: .*{dupenotallowed: 1b}.* matches items from DupePlus's blacklist```
#### Enchantments
Match items that have specific enchantments at minimum levels.  
``` Example: Sharpness V → matches items with Sharpness 5 or higher ```
#### Attributes
Match items with specific attribute modifiers.  
``` Example: Attack Damage ≥ 10.0 → matches weapons with high damage ```
#### Potion Effects
Match potions/foods with specific effects and amplifiers.  
``` Example: Strength II → matches items giving Strength 2 or higher ```
#### Model Data
Match items with specific custom model data values.  
``` Example: 12345 → matches items with CustomModelData: 12345 ```
#### Item Flags
Match items with specific visibility flags.  
``` Example: HIDE_ENCHANTS → matches items that hide enchantments ```
#### Armor Trim
Match armor pieces with specific trim patterns or materials.  
``` Example: Silence Pattern + Gold Material → matches gold silence trim armor ```
### Example Rules

#### Protect All Crate Keys
``` Applied Tags: UNIQUE, PROTECTED, FINAL Match Mode: OR NBT Tag Regex: ".*excellentcrates.*" Lore Regex: ".*[Cc]rate.*" ```
#### Make Netherite Gear Unmodifiable
``` Applied Tags: FINAL Match Mode: AND Material Mode: WHITELIST Materials: NETHERITE_SWORD, NETHERITE_AXE, NETHERITE_PICKAXE, etc. ```
#### Infinite Creative Blocks
``` Applied Tags: INFINITE Match Mode: AND Material Mode: WHITELIST Materials: STONE, DIRT, WOOD, CONCRETE variants ```
---   
## Duplication GUIs

### Replicator GUI
**Access**: `/dupe replicator` or through main menu  
**Permissions**:
- `dupealias.gui.replicator`: Main Access
- `dupealias.gui.replicator.cooldown.<integer>`: Sets the input swap cooldown time (milliseconds).
- `dupealias.gui.replicator.refresh.<integer>`: Sets the amount of ticks it takes for the output item to restock.
- `dupealias.gui.replicator.keep`: Determines if the player should keep the items in the chest when they close it.


**Features**:
- Single-item focused duplication
- Animated visual feedback
- Input cooldown system
- Progress bars for item refresh

**How to Use**:
1. Open the GUI
2. Drag an item into the left input slot
3. Take copies from the output slot
4. Input refreshes after cooldown period

**Best For**: Quick duplication of single item types

### Chest GUI
**Access**: `/dupe chest` or through main menu  
**Permissions**:
- `dupealias.gui.chest`: Main Access
- `dupealias.gui.chest.cooldown.<integer>`: Sets the input swap cooldown time (milliseconds).
- `dupealias.gui.chest.keep`: Determines if the player should keep the items in the chest when they close it.
- `dupealias.gui.chest.keepondeath`: If set to false, all items in the chest will be dropped on the ground on death.

**Features**:
- Multi-item container interface
- 4 input columns, 4 output columns
- Individual item refresh timers
- Session persistence (doesn't persist over reboots)

**How to Use**:
1. Open the GUI
2. Place items in the left 4 columns
3. Take duplicated copies from the right 4 columns
4. Items refresh based on configured delays

**Best For**: Duplication of items you'll commonly need through session persistence.

### Inventory GUI
**Access**: `/dupe inventory` or through main menu  
**Permissions**:
- `dupealias.gui.chest`: Main Access
- `dupealias.gui.chest.refresh.<integer>`: Sets the amount of ticks for the output items to restock

**Features**:
- Mirror of your actual inventory
- Includes armor slots and offhand
- Real-time synchronization
- Individual slot refresh timers

**How to Use**:
1. Open the GUI
2. Your inventory is mirrored in the interface
3. Take copies of any items you're carrying
4. GUI updates as you change your inventory

**Best For**: Easy access to copies of everything you're carrying

### Menu GUI
**Access**: `/dupe gui` or `/dupe` (if set as default)  
**Permission**: `dupealias.gui` If granted, all other GUIs will be granted too unless specifically set to false.

**Features**:
- Central hub for all GUI types
- Permission-based access control
- Clean navigation interface

**How to Use**:
1. Open the main menu
2. Click on the GUI type you want to use
3. Access is controlled by permissions

---   
## Permissions System

### Core Permissions

#### Admin Access
```yaml 
dupealias.admin: true  # Access to admin panel and configuration 
```   
#### Basic Duplication
```yaml 
dupealias.dupe: true                        # Access to /dupe command 
dupealias.dupe.cooldown.<integer>: false    # Cooldown for command duping (milliseconds)  
```   
#### GUI Access
```yaml 
dupealias.gui: true                     # Access to all GUIs 
dupealias.gui.replicator: true          # Access to replicator GUI 
dupealias.gui.inventory: true           # Access to inventory GUI 
dupealias.gui.chest: true               # Access to chest GUI 
```   
### Advanced Permissions

#### Session Persistence
```yaml 
dupealias.gui.replicator.keep: false    # Keep replicator items on close 
dupealias.gui.chest.keep: false         # Keep chest items on close 
dupealias.gui.chest.keepondeath: false  # Keep chest items on death 
```   
#### Permission Based Refresh Rates
```yaml 
dupealias.gui.replicator.refresh.<integer>: false      # Ticks of refresh cooldown
dupealias.gui.replicator.cooldown.<integer>: false     # Ticks of re-input cooldown
dupealias.gui.inventory.refresh.<integer>: false       # Ticks of refresh cooldown
dupealias.gui.chest.refresh.<integer>: false           # Ticks of refresh cooldown
```   
#### Tag Bypasses (Use Carefully!)
```yaml 
dupealias.unique.bypass: false       # Can dupe UNIQUE items 
dupealias.final.bypass: false        # Can modify FINAL items 
dupealias.protected.bypass: false    # Can use PROTECTED items 
```   
#### Special Permissions
```yaml 
dupealias.infinite: true             # Can use INFINITE items
```   
### Permission Hierarchy

The plugin generally uses a "lowest value wins" system for numeric permissions. If a player has both `refresh.20` and `refresh.5`, they will get the 5-tick refresh cooldown. There is an exception to this rule however. For the permission `dupealias.dupe.limit.<integer>`, it will take the highest value given. This is because I am lazy and did not add a default limit for non ranked players.

### Example Permission Sets

#### VIP Player
```yaml 
groups:    
 vip:   
  permissions:  
	 - dupealias.dupe
	 - dupealias.gui 
	 - dupealias.gui.replicator.refresh.5     # Faster refresh     
	 - dupealias.gui.replicator.cooldown.10   # Shorter cooldown   
	 - dupealias.dupe.cooldown.0              # No command cooldown  
```   
#### Staff Member
```yaml 
groups:    
 staff:   
  permissions: 
     - dupealias.admin                         # Full admin access   
     - dupealias.gui.replicator.refresh.1      # Instant refresh   
     - dupealias.gui.*.keep                    # Session persistence   
     - dupealias.final.bypass                  # Can modify final items  
```    
 ---   
## Configuration

### Main Configuration (`config.json`)

#### Duplication Settings
```json 
{    
 "dupeCooldownMillis": 1000,           // Command cooldown in milliseconds    
 "defaultDupeGui": "REPLICATOR"        // Default GUI (REPLICATOR/INVENTORY/CHEST/MENU) 
 } 
```   
#### GUI Refresh Rates
```json 
{    
   "replicator": {    
		 "baseRefreshDelayTicks": 1,         // Base item refresh delay    
		 "baseInputCooldownTicks": 20        // Base input change cooldown
	 },
	 "chest": {    
		 "baseRefreshDelayTicks": 1          // Base item refresh delay
	 },    
	 "inventory": {    
		 "baseRefreshDelayTicks": 1          // Base item refresh delay
	 }
} 
```   

#### Command Blocking

```json
{
  "finalCommandRegex": [
    "\"(?:itemname|iname)\"gmi",  // Block item naming commands
    "\"(?:itemlore|lore)\"gmi"    // Block lore modification commands
  ]
}

```

#### Tag Lore Customization (MiniMessage)

```json
{
  "trueTagLore": {
    "UNIQUE": "<dark_blue><bold>|</bold><blue> Unique",
    "FINAL": "<dark_red><bold>|</bold><red> Final",
    "INFINITE": "<dark_green><bold>|</bold><green> Infinite",
    "PROTECTED": "<dark_purple><bold>|</bold><light_purple> Protected"
  },
  "falseTagLore": {
    "UNIQUE": "<dark_blue><bold>|</bold><blue> Dupeable",
    "FINAL": "<dark_red><bold>|</bold><red> Mutable",
    "INFINITE": "<dark_green><bold>|</bold><green> Finite",
    "PROTECTED": "<dark_purple><bold>|</bold><light_purple> Unprotected"
  }
}

```

### Common Configuration (`common.json`)

#### Visual Customization

```json
{
  "mainColor": 11184895,           // Primary color (hex: AAAAFF)
  "secondaryColor": 909055,        // Secondary color (hex: 00DDFF)
  "pluginName": "DupeAlias",       // Display name
  "flatPrefix": "&9DupeAlias> &7", // Legacy chat prefix
  "flat": false                    // Use legacy formatting
}

```

#### Debug Settings

```json
{
  "debugMode": false,              // Enable debug output
  "debuggerExclusions": []         // Methods to exclude from debug
}

```
 ---   
## Commands

### Administrative Commands

#### `/dupealias` (Aliases: `/da`)
**Permission**: `dupealias.admin`
**Description**: Main administrative command

**Subcommands**:
- `/da` - Open admin panel GUI
- `/da gui` - Open admin panel GUI
- `/da debug toggle` - Toggle debug mode
- `/da debug exclude <method>` - Exclude method from debugging
- `/da debug include <method>` - Include method in debugging

#### Tag Management
- `/da tag <tag> [material|global] [remove]` - Manage item tags
- `/da tag <tag>` - Tag held item
- `/da tag <tag> remove` - Remove tag from held item
- `/da tag <tag> false` - Set tag to false on held item
- `/da tag <tag> global` - Create global rule for held item material
- `/da tag <tag> <material>` - Create global rule for specific material
- `/da tag <tag> <material> remove` - Remove global rule

#### Rule Management
- `/da rule create <tag>` - Create new global rule
- `/da rule list` - List all global rules
- `/da rule remove <index>` - Remove rule by index
- `/da rule info <index>` - Show detailed rule information

### Player Commands

#### `/dupe`
**Permission**: `dupealias.dupe`
**Description**: Main duplication command

**Usage**:
- `/dupe` - Duplicate held item once OR open default GUI
- `/dupe <amount>` - Duplicate held item multiple times
- `/dupe gui` - Open main GUI menu
- `/dupe replicator` - Open replicator GUI
- `/dupe inventory` - Open inventory GUI
- `/dupe chest` - Open chest GUI


## Troubleshooting

### Common Issues

#### "You cannot dupe unique items"
**Cause**: Item has UNIQUE tag or matches global rule    
**Solution**:
- Check `/da` → Held Item Actions to see current tags
- Review global rules that might be applying UNIQUE tag
- Use `/da tag UNIQUE remove` to remove individual tag

#### Items not refreshing in GUI
**Cause**: Long refresh delay or permission issues    
**Solution**:
- Check player has appropriate GUI permissions
- Verify refresh rate permissions (lower numbers = faster)
- Ensure player isn't hitting cooldown limits

#### "You cannot modify final items"
**Cause**: Item has FINAL tag blocking modifications    
**Solution**:
- Check item tags in admin panel
- Use `/da tag FINAL remove` if needed
- Grant `dupealias.final.bypass` permission for admins

#### Global rules not applying
**Cause**: Rule criteria not matching or individual tags overriding    
**Solution**:
- Test rule criteria with `/da rule info <index>`
- Check match mode (AND vs OR)
- Remember individual tags override global rules

#### GUI won't open
**Cause**: Missing permissions or plugin conflicts    
**Solution**:
- Verify player has `dupealias.gui` permission
- Check for inventory plugin conflicts
- Ensure player inventory isn't full

### Debug Mode

Enable debug mode to troubleshoot issues:  ``` /da debug toggle ```   
This will show detailed information about:
- Tag checking processes
- Global rule matching
- Permission calculations
- GUI state changes

Exclude noisy methods:  ``` /da debug exclude <method_name> ```
### Performance Considerations

#### Large Player Counts
- Use session persistence sparingly
- Don't use GUI refresh or input cooldowns.
- Monitor server TPS with `/tps`

#### Complex Global Rules
- Avoid overly complex regex patterns
- Use material whitelisting instead of complex criteria when possible
- Limit the number of active global rules

#### GUI Optimization
- Consider disabling session persistence for busy servers
- Use cooldowns to prevent spam

---   
### Getting Help

If you encounter issues not covered in this documentation:

1. Enable debug mode and check console logs
2. Verify your global rules are correctly configured
3. Check for conflicts with other plugins
4. Join the [Alias Development](https://trouper.me/alias) discord

Remember that individual item tags always override global rules, and bypass permissions should be used carefully as they can compromise your server's item security system.