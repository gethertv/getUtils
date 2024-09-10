# GetUtils

GetUtils is a comprehensive utility library designed to simplify and enhance the process of creating Minecraft plugins for Bukkit/Spigot servers. It provides a collection of tools, helpers, and utilities that address common challenges in plugin development, allowing developers to focus on creating unique features for their servers.

## Features

- Simplifies common Minecraft plugin development tasks
- Provides utilities for handling items, inventories, players, and more
- Offers serialization and deserialization support for Bukkit objects
- Includes custom models for extended functionality

## Serialization Support

GetUtils provides serialization and deserialization support for various Bukkit objects, making it easier to save and load data. The following objects are supported:

1. ItemStack
2. Location
3. Cuboid (custom implementation)
4. AttributeModifier

These serializers and deserializers can be used with Jackson for easy conversion between objects and YAML or JSON formats.

## Models

GetUtils includes several custom models to extend Bukkit's functionality:

1. **BlockData**: Represents block data, including its material and location.

2. **Cuboid**: A custom implementation for defining and manipulating 3D rectangular regions in the Minecraft world.

3. **PersistentData**: Utility class for handling persistent data types.

4. **TitleMessage**: Represents a title message that can be displayed to players, including title, subtitle, and timing information.

5. **DynamicItem**: Represents an item with dynamic properties that can be updated based on placeholders.

6. **InventoryConfig**: Configuration class for custom inventories.

7. **AbstractInventoryHolder**: Base class for creating custom inventory GUIs.

These models provide additional functionality and abstraction layers to simplify plugin development and enhance the capabilities of your Minecraft server.

## Installation

Add the following repository and dependency to your `pom.xml`:

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.gethertv</groupId>
    <artifactId>GetUtils</artifactId>
    <version>1.0.0</version>
</dependency>
```

# GetUtils

## Table of Contents
- [Key Features](#key-features)
- [Serialization Support](#serialization-support)
- [Models](#models)
- [Installation](#installation)
- [Usage](#usage)
  - [Custom Config](#custom-config)
  - [Creating Custom GUIs](#creating-custom-guis)
- [Utility Classes](#utility-classes)
  - [TimeUtil](#timeutil)
  - [MessageUtil](#messageutil)
  - [InventoryUtils](#inventoryutils)
  - [EntityUtil](#entityutil)
- [Contributing](#contributing)


## Usage

### Custom Config

GetUtils provides a powerful and flexible configuration system that allows you to easily manage your plugin's settings. Here's a comprehensive guide on how to use it:

#### 1. Create a Configuration Class

First, create a class that extends `GetConfig`. This class will represent your plugin's configuration:

```java
@Getter
@Setter
public class MyConfig extends GetConfig {
    @Comment("The name of the server")
    private String serverName = "My Awesome Server";

    @Comment("Maximum number of players")
    private int maxPlayers = 100;

    @Comment("Welcome message")
    private String welcomeMessage = "Welcome to {server}!";

    @Comment("List of VIP players")
    private List<String> vipPlayers = new ArrayList<>();

    @Comment("Special item given to new players")
    private ItemStack welcomeItem = new ItemStack(Material.DIAMOND);

    @Comment("Spawn location")
    private Location spawnLocation;

}
```

#### 2. Initialize the Configuration

In your plugin's `onEnable` method, initialize the configuration:

```java
public class MyPlugin extends JavaPlugin {
    private MyConfig config;

    @Override
    public void onEnable() {
        this.config = ConfigManager.create(MyConfig.class, cfg -> {
            cfg.setFile(new File(getDataFolder(), "config.yml"));
            cfg.load();
        });
    }

    // ... rest of your plugin class ...
}
```

#### 3. Use the Configuration

Now you can use your configuration throughout your plugin:

```java
public void someMethod() {
    String serverName = config.getServerName();
    player.sendMessage("Welcome to " + serverName);

    Location spawn = config.getSpawnLocation();
    player.teleport(spawn);

    ItemStack welcomeItem = config.getWelcomeItem();
    player.getInventory().addItem(welcomeItem);
}
```

#### 4. Modify and Save the Configuration

You can modify the configuration at runtime and save the changes:

```java
public void updateConfig() {
    config.setMaxPlayers(150);
    config.getVipPlayers().add("NewVIPPlayer");
    config.setSpawnLocation(player.getLocation());

    config.save();
}
```

#### 5. Reload the Configuration

To reload the configuration from the file:

```java
public void reloadConfig() {
    config.load();
}
```

#### 6. Advanced Usage: Comments and Placeholders

- The `@Comment` annotation allows you to add comments to your configuration file.
- You can use placeholders in your configuration strings, which can be replaced at runtime:

```java
String message = config.getWelcomeMessage().replace("{server}", config.getServerName());
player.sendMessage(message);
```

#### 7. Handling Complex Objects

GetUtils can handle complex objects like `ItemStack` and `Location`. These are automatically serialized and deserialized:

```java
ItemStack specialItem = new ItemStack(Material.NETHER_STAR);
// ... customize the item ...
config.setWelcomeItem(specialItem);
config.save();

// Later, when loading:
ItemStack loadedItem = config.getWelcomeItem();
// The loaded item will have all the properties of the saved item
```

#### 8. Serializing Custom Classes

If you want to serialize your own custom classes in the configuration, you need to ensure that these classes have a default no-args constructor. This is required for the deserialization process. You have two options:

1. Add a default constructor manually:

```java
public class MyCustomClass {
    private String someField;

    // This no-args constructor is required for deserialization
    public MyCustomClass() {}

    public MyCustomClass(String someField) {
        this.someField = someField;
    }

    // Getters and setters...
}
```

2. Use Lombok's `@NoArgsConstructor` annotation:

```java
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MyCustomClass {
    private String someField;
}
```

Using Lombok can help reduce boilerplate code. If you're using Lombok, make sure to include it in your project dependencies.

You can then use your custom class in your configuration:

```java
public class MyConfig extends GetConfig {
    @Comment("A custom object in our configuration")
    private MyCustomClass customObject = new MyCustomClass("default value");
}
```

### Creating Custom GUIs

GetUtils provides a powerful system for creating custom GUIs using Minecraft inventories. Here's a step-by-step guide on how to create a custom GUI:

#### 1. Create a Custom Inventory Class

First, create a class that extends `AbstractInventoryHolder`:

```java
public class MyCustomGUI extends AbstractInventoryHolder {

    public MyCustomGUI(Plugin plugin, Player player, InventoryConfig config) {
        super(plugin, player, config);
        initializeItems();
    }

    @Override
    protected void initializeItems() {
        // This is where you'll add items to your GUI
    }
}
```

#### 2. Create an Inventory Configuration

Create an `InventoryConfig` object to define the basic properties of your GUI:

```java
InventoryConfig config = InventoryConfig.builder()
    .title("My Custom GUI")
    .size(27)  // Size must be a multiple of 9
    .refreshInterval(20)  // Optional: refresh GUI every 20 ticks (1 second)
    .build();
```

#### 3. Add Items to Your GUI

In the `initializeItems()` method, add items to your GUI:

```java
@Override
protected void initializeItems() {
    // Add a static item
    ItemStack infoItem = new ItemStack(Material.BOOK);
    ItemMeta meta = infoItem.getItemMeta();
    meta.setDisplayName(ColorFixer.addColors("&6Information"));
    infoItem.setItemMeta(meta);
    setItem(13, infoItem, event -> {
        player.sendMessage("This is an information item!");
        event.setCancelled(true);
    });

    // Add a dynamic item
    DynamicItem dynamicItem = new DynamicItem();
    dynamicItem.setItemStack(new ItemStack(Material.CLOCK));
    dynamicItem.setSlots(Arrays.asList(0, 8, 18, 26));  // Corner slots
    dynamicItem.addPlaceholder("time", () -> {
        // This will update the item name with the current time every refresh
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    });
    addItem(dynamicItem);
}
```

#### 4. Register the GetInventory Listener

In your plugin's `onEnable()` method, register the `GetInventory` listener:

```java
@Override
public void onEnable() {
    new GetInventory(this);
    // ... other initialization code
}
```

#### 5. Open the GUI for a Player

To open the GUI for a player, create an instance of your custom GUI class and call the `open()` method:

```java
public void openGUIForPlayer(Player player) {
    InventoryConfig config = InventoryConfig.builder()
        .title("My Custom GUI")
        .size(27)
        .build();
    MyCustomGUI gui = new MyCustomGUI(this, player, config);
    gui.open();
}
```

#### 6. Handling GUI Updates

If you set a refresh interval in your `InventoryConfig`, the `initializeItems()` method will be called automatically at that interval. You can also manually refresh the GUI by calling the `refresh()` method:

```java
public void manuallyRefreshGUI() {
    refresh();
}
```

This system allows you to create dynamic, interactive GUIs with ease. The `AbstractInventoryHolder` takes care of a lot of the boilerplate code, allowing you to focus on defining the content and behavior of your GUI.


# TimeUtil

The `TimeUtil` class provides utility methods for formatting and converting time durations. It's designed to be used as a static utility class, with all methods being static and the constructor private to prevent instantiation.

## Methods

### `formatTimeShort(long seconds)`

Formats a time duration given in seconds into a short, human-readable string.

- **Parameters:** 
  - `seconds`: Time duration in seconds
- **Returns:** A formatted string representing the time duration (e.g., "5d 2h 30m 15s")
- **Example:** 
  ```java
  String formattedTime = TimeUtil.formatTimeShort(93675); // Returns "1d 2h 1m 15s"
  ```

### `convertSecondsToPolishTime(long totalSeconds)`

Converts a time duration in seconds to a Polish language representation, using the most significant time unit.

- **Parameters:** 
  - `totalSeconds`: Time duration in seconds
- **Returns:** A string representing the time duration in Polish, using the most appropriate unit
- **Example:** 
  ```java
  String polishTime = TimeUtil.convertSecondsToPolishTime(3665); // Returns "1 godzina"
  ```

### `formatTimeColon(long seconds)`

Formats a time duration given in seconds into a colon-separated string representation.

- **Parameters:** 
  - `seconds`: Time duration in seconds
- **Returns:** A formatted string representing the time duration in "HH:MM:SS" format
- **Example:** 
  ```java
  String colonTime = TimeUtil.formatTimeColon(3665); // Returns "01:01:05"
  ```

# MessageUtil

The `MessageUtil` class is a utility class designed to simplify message handling and broadcasting in Bukkit/Spigot plugins. It provides methods for sending colored messages to players, broadcasting messages to all online players, and displaying title messages.

## Methods

### `logMessage(String consoleColor, String message)`

Logs a colored message to the console.

- **Parameters:**
  - `consoleColor`: The color code for the console message.
  - `message`: The message to be logged.
- **Usage:**
  ```java
  MessageUtil.logMessage(ConsoleColor.GREEN, "Plugin enabled successfully!");
  ```

### `sendMessage(Player player, String message)`

Sends a colored message to a specific player.

- **Parameters:**
  - `player`: The player to receive the message.
  - `message`: The message to be sent.
- **Usage:**
  ```java
  MessageUtil.sendMessage(player, "&aWelcome to the server!");
  ```

### `sendMessage(CommandSender sender, String message)`

Sends a colored message to a command sender (can be a player or console).

- **Parameters:**
  - `sender`: The command sender to receive the message.
  - `message`: The message to be sent.
- **Usage:**
  ```java
  MessageUtil.sendMessage(sender, "&eCommand executed successfully!");
  ```

### `sendMessage(CommandSender sender, List<String> messages)`

Sends a list of colored messages to a command sender.

- **Parameters:**
  - `sender`: The command sender to receive the messages.
  - `messages`: The list of messages to be sent.
- **Usage:**
  ```java
  List<String> helpMessages = Arrays.asList("&6/command1 - Does something", "&6/command2 - Does something else");
  MessageUtil.sendMessage(sender, helpMessages);
  ```

### `broadcast(String message)`

Broadcasts a colored message to all online players and the console.

- **Parameters:**
  - `message`: The message to be broadcast.
- **Usage:**
  ```java
  MessageUtil.broadcast("&bA new event has started!");
  ```

### `broadcastNoneColor(String message)`

Broadcasts a message to all online players and the console without color processing.

- **Parameters:**
  - `message`: The message to be broadcast without color processing.
- **Usage:**
  ```java
  MessageUtil.broadcastNoneColor("Server restarting in 5 minutes!");
  ```

### `broadcastTitle(TitleMessage titleMessage)`

Broadcasts a title message to all online players.

- **Parameters:**
  - `titleMessage`: The TitleMessage object containing title information.
- **Usage:**
  ```java
  TitleMessage title = TitleMessage.of("&6Welcome", "&7to the server");
  MessageUtil.broadcastTitle(title);
  ```

### `broadcastTitle(TitleMessage titleMessage, Map<String, String> variables)`

Broadcasts a title message to all online players with variable replacement.

- **Parameters:**
  - `titleMessage`: The TitleMessage object containing title information.
  - `variables`: A map of variables to be replaced in the title and subtitle.
- **Usage:**
  ```java
  TitleMessage title = TitleMessage.of("&6Welcome, {player}", "&7to {server}");
  Map<String, String> variables = new HashMap<>();
  variables.put("{player}", player.getName());
  variables.put("{server}", serverName);
  MessageUtil.broadcastTitle(title, variables);
  ```

# InventoryUtils

The `InventoryUtils` class provides methods for serializing and deserializing Bukkit `ItemStack` objects and arrays to and from Base64 strings. This is useful for storing inventory contents in a compact string format, which can be easily saved in databases or configuration files.

## Key Methods

### Serialization

1. `itemStackArrayToBase64(ItemStack[] items)`
   - Serializes an array of ItemStacks to a Base64 string.
   - Use this to serialize entire inventories.

2. `itemStackToBase64(ItemStack item)`
   - Serializes a single ItemStack to a Base64 string.
   - Useful for storing individual items.

### Deserialization

1. `itemStackArrayFromBase64(String data)`
   - Deserializes a Base64 string back into an array of ItemStacks.
   - Use this to restore entire inventories.

2. `itemStackFromBase64(String data)`
   - Deserializes a Base64 string back into a single ItemStack.
   - Useful for restoring individual items.

## Usage Examples

### Serializing an Inventory

```java
Player player = ...;
ItemStack[] inventoryContents = player.getInventory().getContents();
String serializedInventory = InventoryUtils.itemStackArrayToBase64(inventoryContents);
// Now you can store 'serializedInventory' in a database or config file
```

### Deserializing an Inventory

```java
String serializedInventory = ...; // Retrieve this from your storage
ItemStack[] inventoryContents = InventoryUtils.itemStackArrayFromBase64(serializedInventory);
player.getInventory().setContents(inventoryContents);
```

### Serializing a Single Item

```java
ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
String serializedItem = InventoryUtils.itemStackToBase64(item);
// Store 'serializedItem' as needed
```

### Deserializing a Single Item

```java
String serializedItem = ...; // Retrieve this from your storage
ItemStack item = InventoryUtils.itemStackFromBase64(serializedItem);
// Use the deserialized item
```

## Notes

- These methods use Bukkit's serialization system, so they should work with most custom ItemStack modifications.
- Be aware of potential exceptions:
  - `IllegalStateException` for serialization errors
  - `IOException` for deserialization errors
- Always handle these exceptions in your code to ensure robustness.

# EntityUtil

`EntityUtil` is a utility class for Bukkit/Spigot plugins that provides methods for finding and managing entities in the Minecraft world.

## Key Methods

### `findNearestEntity(Location center, double range, Class<T> entityClass)`

Finds the nearest entity of a specified type within a given range from a central location.

- **Parameters:**
  - `center`: The central Location to search around
  - `range`: The radius of the spherical search area
  - `entityClass`: The type of entity to find
- **Returns:** The nearest entity of the specified type, or null if none are found
- **Example:**
  ```java
  Zombie nearestZombie = EntityUtil.findNearestEntity(player.getLocation(), 50, Zombie.class);
  if (nearestZombie != null) {
      player.sendMessage("Nearest zombie is " + nearestZombie.getLocation().distance(player.getLocation()) + " blocks away!");
  }
  ```

### `getNearbyEntities(Location location, double radius)`

Returns a collection of all entities within a specified radius of a location.

- **Parameters:**
  - `location`: The central location
  - `radius`: The search radius
- **Returns:** A Collection of Entity objects within the specified radius
- **Example:**
  ```java
  Collection<Entity> nearbyEntities = EntityUtil.getNearbyEntities(player.getLocation(), 10);
  player.sendMessage("There are " + nearbyEntities.size() + " entities within 10 blocks of you!");
  ```
  
## Contributing

If you have any suggestions, bug reports, or improvements, please feel free to create a pull request or commit with a bug fix. Your contributions are greatly appreciated and help improve GetUtils for everyone.



