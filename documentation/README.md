# Project: Sword Flight & Custom Food Items for Kaupenjoe's Forge Tutorial Mod (Minecraft 1.21.X)

## Overview

This project enhanced Kaupenjoe's Forge 1.21.X tutorial mod by implementing two primary features:

1.  **Sword Flight:** A client-side mechanic enabling players to toggle flight by pressing a designated key ('R') while holding any sword. This involved understanding client-side input, event handling, and player ability modification.
2.  **Custom Food Items:** Two new berries, the "Swiftness Berry" and the "Enchanted Swiftness Berry," with custom potion effects, unique textures, and specific eating behaviors (e.g., being edible even when not hungry). This explored item registration, data-driven item properties (FoodProperties and Consumables), asset management (models, textures, language files), and creative tab integration.

The development process focused on understanding and utilizing core Forge APIs, iterative testing, debugging common modding issues, and leveraging AI assistance for code generation and problem-solving.

## New Features Detailed

### 1. Sword Flight

This feature allows players to fly when holding any item that is an instance of `net.minecraft.world.item.SwordItem` and pressing the 'R' key. The flight is continuous while the key is held and the conditions are met.

**Implementation Details:**

- **Key Binding (`src/main/java/net/kaupenjoe/tutorialmod/client/ModKeyBindings.java`):**

  - **Purpose:** To define a new, configurable in-game control that players can use to activate the sword flight. Minecraft uses `KeyMapping` objects to manage these controls.
  - **Implementation:**
    - A `public static final KeyMapping FLIGHT_KEY` was declared. This makes the `KeyMapping` a single, easily accessible instance throughout the mod's client-side code.
    - **Instantiation:** `new KeyMapping("key.tutorialmod.flight", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.category.tutorialmod.sword_soaring")`
      - **`descriptionKey`**: `"key.tutorialmod.flight"` is a translation key. The actual text seen in the controls menu (e.g., "Toggle Sword Flight") is looked up from language files (like `en_us.json`) using this key. This system enables localization.
      - **`inputType`**: `InputConstants.Type.KEYSYM` indicates that this keybinding is for a standard keyboard key. Mouse buttons would use `InputConstants.Type.MOUSE`.
      - **`keyCode`**: `GLFW.GLFW_KEY_R` sets the _default_ assignment to the 'R' key on the keyboard. `GLFW` is the low-level library Minecraft uses for windowing and input. Players can rebind this in the game's controls menu.
      - **`categoryKey`**: `"key.category.tutorialmod.sword_soaring"` is another translation key used to group this keybinding under a specific category (e.g., "Sword Soaring") in the controls menu, improving organization.
  - **Registration (`RegisterKeyMappingsEvent`):**
    - **Why:** A `KeyMapping` object must be explicitly registered with Forge so the game knows about it and includes it in the controls screen. This happens during a specific mod loading lifecycle event.
    - **How:** The `ModKeyBindings` class is annotated: `@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)`.
      - `@Mod.EventBusSubscriber`: This Forge annotation tells Forge to scan this class for static methods annotated with `@SubscribeEvent` and automatically register them to listen for events on the specified event bus.
      - `bus = Mod.EventBusSubscriber.Bus.MOD`: This specifies the MOD event bus, which is used for events related to mod setup and initialization (like registering blocks, items, or key mappings).
      - `value = Dist.CLIENT`: This is critical, as key bindings are a client-only concept. This ensures the registration code only runs on the game client, not on a dedicated server.
    - A static method, typically `public static void register(final RegisterKeyMappingsEvent event)`, is defined within `ModKeyBindings`.
      - `@SubscribeEvent`: This annotation marks the method as a handler for the `RegisterKeyMappingsEvent`.
      - `event.register(FLIGHT_KEY);`: This line within the handler method performs the actual registration of our `FLIGHT_KEY`.

- **Event Handling (`src/main/java/net/kaupenjoe/tutorialmod/event/ClientForgeEvents.java`):**

  - **Purpose:** To execute the flight logic whenever the game is running (each "tick") by checking if the conditions for flight (key pressed, sword held) are met.
  - **Implementation:**
    - The class is annotated: `@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)`.
      - `bus = Mod.EventBusSubscriber.Bus.FORGE`: This time, we subscribe to the FORGE event bus, which handles general game events like ticks, player actions, rendering, etc.
      - `value = Dist.CLIENT`: Again, this logic is client-side.
    - **`onClientTick(TickEvent.ClientTickEvent event)` method:**
      - `@SubscribeEvent`: Marks this as the event handler for `TickEvent.ClientTickEvent`.
      - `if (event.phase != TickEvent.Phase.END) { return; }`: `ClientTickEvent` fires twice per tick (START and END). This check ensures the logic runs only once, typically at the `END` phase to avoid interfering with other tick processing.
      - `Minecraft mc = Minecraft.getInstance(); LocalPlayer player = mc.player;`: Gets the current client-side player.
      - A `private static boolean modEnabledFlight = false;` field was used to track if the current flight state was initiated by this mod. This helps differentiate from, for example, creative mode flight.
      - **Activation Logic:**
        - `if (ModKeyBindings.FLIGHT_KEY.isDown() && player != null && player.getMainHandItem().getItem() instanceof SwordItem)`: This compound condition checks:
          - `ModKeyBindings.FLIGHT_KEY.isDown()`: Is our registered 'R' key currently being held down?
          - `player != null`: Is there a valid player object? (Important check, especially during world load/unload).
          - `player.getMainHandItem().getItem() instanceof SwordItem`: Is the item in the player's main hand an instance of `SwordItem` (or any subclass of it)? This makes the flight work with any sword.
        - If all true:
          - `player.getAbilities().mayfly = true;`: Sets the player's capability to fly. This is the permission.
          - `if (!modEnabledFlight) { modEnabledFlight = true; }`: If flight wasn't already active by the mod, mark it as such.
          - `player.getAbilities().flying = true;`: Sets the player's actual flying state to true.
      - **Deactivation Logic:**
        - `else if (player != null && modEnabledFlight)`: This `else if` ensures deactivation logic runs only if the flight was previously enabled by this mod and the activation conditions are no longer true (e.g., key released, sword unequipped).
        - `player.getAbilities().mayfly = false;`
        - `player.getAbilities().flying = false;`
        - `modEnabledFlight = false;`
      - **Critical Post-Ability Change Calls (if `player != null && modEnabledFlight` was true OR conditions to disable were met):**
        - `player.onUpdateAbilities();`: This method _must_ be called after changing player abilities. It ensures the changes are properly processed and synchronized by the game. Without it, the game might not visually or functionally update the player's flight status correctly.
        - `if (!player.getAbilities().mayfly) { player.fallDistance = 0.0F; }`: If flight permission was just removed (meaning the player will start falling), reset their `fallDistance`. This prevents them from taking fall damage from the height they were flying at.

- **Language Entries (`src/main/resources/assets/tutorialmod/lang/en_us.json`):**
  - **Purpose:** To provide human-readable names for the keybinding and its category in the game's UI.
  - `"key.category.tutorialmod.sword_soaring": "Sword Soaring"`: Defines the name for the custom category in the Controls menu.
  - `"key.tutorialmod.flight": "Toggle Sword Flight"`: Defines the name for the actual keybinding in the Controls menu.

### 2. Swiftness Berry

A custom food item designed to grant a significant speed boost when consumed, edible even when the player is not hungry.

**Implementation Details:**

- **Food Properties & Consumable Effects (`src/main/java/net/kaupenjoe/tutorialmod/item/ModFoodProperties.java`):**

  - **`public static final FoodProperties SWIFTNESS_BERRY`**: This defines the inherent properties of the berry as a food item.
    - `new FoodProperties.Builder()`: Standard builder pattern for creating `FoodProperties`.
    - `.nutrition(2)`: How many hunger points (half-shanks) it restores.
    - `.saturationModifier(0.2f)`: How much saturation it provides (influences how long hunger stays full). A low value means hunger depletes faster after eating.
    - `.alwaysEdible()`: A crucial property for utility foods. It allows the player to consume the item even if their hunger bar is full, solely to gain its effects. Standard foods without this can only be eaten when hungry.
    - `.build()`: Finalizes the `FoodProperties` object.
  - **`public static final Consumable SWIFTNESS_BERRY_EFFECT`**: This defines what happens _when_ the item is consumed, specifically for applying potion-like effects. This is part of Minecraft's newer Item Components system (1.20+).
    - `Consumables.defaultFood()`: Provides a base `Consumable.Builder`.
    - `.onConsume(new ApplyStatusEffectsConsumeEffect(...))`: Specifies an action to perform upon consumption. `ApplyStatusEffectsConsumeEffect` is a built-in effect type for applying status effects.
    - `new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 9), 1.0f)`:
      - The first argument is the effect (or list of effects). Here, it's a single `MobEffectInstance`.
        - `MobEffects.MOVEMENT_SPEED`: The specific vanilla effect for Speed.
        - `1200`: Duration of the effect in game ticks (20 ticks = 1 second, so 1200 ticks = 60 seconds).
        - `9`: Amplifier of the effect. Potion amplifiers are 0-indexed (0 is Level I, 1 is Level II, etc.). So, 9 results in Speed X.
      - The second argument, `1.0f`, is the probability (from 0.0f to 1.0f) that this effect will be applied. `1.0f` means a 100% chance.
    - `.build()`: Finalizes the `Consumable` object.

- **Item Registration (`src/main/java/net/kaupenjoe/tutorialmod/item/ModItems.java`):**

  - **Purpose:** To make the game aware of the new item's existence and its properties.
  - `public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TutorialMod.MOD_ID);`: A `DeferredRegister` is used to queue registrations for items. These are processed by Forge at the correct time during mod loading. `ForgeRegistries.ITEMS` is the registry for all game items.
  - `public static final RegistryObject<Item> SWIFTNESS_BERRY = registerItem("swiftness_berry", (properties) -> new Item(properties.food(ModFoodProperties.SWIFTNESS_BERRY, ModFoodProperties.SWIFTNESS_BERRY_EFFECT)));`:
    - `registerItem` is a helper method in this class that simplifies creating `RegistryObject`s.
    - `new Item(...)`: Creates an instance of the base `Item` class. For simple items with food properties, this is often sufficient.
    - `properties.food(FoodProperties foodProperties, Consumable consumable)`: This is where the previously defined `FoodProperties` (for hunger/saturation/alwaysEdible) and the `Consumable` component (for the on-consume potion effect) are attached to the item. This is a key change in Forge/Minecraft 1.20+ for how food effects are handled.

- **Item Model (`src/main/resources/assets/tutorialmod/models/item/swiftness_berry.json`):**

  - **Purpose:** To tell the game how to render the item in inventories, in hand, and as a dropped entity.
  - `{ "parent": "item/generated", "textures": { "layer0": "tutorialmod:item/swiftness_berry" } }`:
    - `"parent": "item/generated"`: This common parent model tells Minecraft to render the item as a flat 2D sprite, typical for most basic items. The texture specified in `layer0` will be used for this sprite.
    - `"textures": { "layer0": "tutorialmod:item/swiftness_berry" }`: This maps the texture layer `layer0` (the primary texture for `item/generated`) to our specific texture file. The path `tutorialmod:item/swiftness_berry` resolves to `assets/tutorialmod/textures/item/swiftness_berry.png`.

- **Texture (`src/main/resources/assets/tutorialmod/textures/item/swiftness_berry.png`):**

  - A 16x16 pixel PNG image. This is the standard resolution for Minecraft item textures. It should have a transparent background where appropriate.

- **Language Entry (`src/main/resources/assets/tutorialmod/lang/en_us.json`):**

  - `"item.tutorialmod.swiftness_berry": "Swiftness Berry"`: Provides the English display name for the item. Other `xx_yy.json` files could provide names in other languages.

- **Creative Tab Integration (`src/main/java/net/kaupenjoe/tutorialmod/item/ModCreativeModeTabs.java`):**
  - **Purpose:** To make the item easily accessible in Creative Mode.
  - `ModCreativeModeTabs.ALEXANDRITE_ITEMS_TAB`: This references a custom creative tab defined in the mod.
  - Within the `.displayItems((itemDisplayParameters, output) -> { ... })` lambda for this tab:
    - `output.accept(ModItems.SWIFTNESS_BERRY.get());`: This line adds an `ItemStack` of the Swiftness Berry to the items displayed in this specific creative tab. `output` is of type `CreativeModeTab.Output`.

### 3. Enchanted Swiftness Berry

A significantly more potent version of the Swiftness Berry, granting multiple powerful effects and higher nutritional value, intended as a rare or high-tier consumable.

**Implementation Details:**

- **Food Properties & Consumable Effects (`src/main/java/net/kaupenjoe/tutorialmod/item/ModFoodProperties.java`):**

  - `public static final FoodProperties ENCHANTED_SWIFTNESS_BERRY_PROPERTIES`:
    - `.nutrition(4).saturationModifier(1.2f)`: Higher nutritional values, comparable to a Golden Apple, making it a good source of hunger and long-lasting saturation.
    - `.alwaysEdible()`: Retained so it can be used for its effects at any time.
  - `public static final Consumable ENCHANTED_SWIFTNESS_BERRY_EFFECT`:
    - `new ApplyStatusEffectsConsumeEffect(List.of(...), 1.0f)`: The key difference here is `List.of(...)`, which allows multiple `MobEffectInstance` objects to be applied from a single consumption.
      - `new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 4)`: Speed V (Amplifier 4) for 60 seconds. A high but more controllable speed than the regular berry's Speed X.
      - `new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3)`: Absorption IV (Amplifier 3) for 2 minutes. Each level of absorption typically grants 2 extra "golden" hearts, so Amplifier 3 grants 8 absorption hearts.
      - `new MobEffectInstance(MobEffects.REGENERATION, 600, 3)`: Regeneration IV (Amplifier 3) for 30 seconds. Provides rapid health recovery.
      - `new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 1)`: Resistance II (Amplifier 1) for 5 minutes. Reduces incoming damage.
      - `new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0)`: Fire Resistance I (Amplifier 0) for 5 minutes. Prevents fire damage.
    - An `import java.util.List;` was added at the top of the file to support `List.of()`.

- **Item Registration (`src/main/java/net/kaupenjoe/tutorialmod/item/ModItems.java`):**

  - Follows the same pattern as the regular Swiftness Berry, but ensures it uses `ModFoodProperties.ENCHANTED_SWIFTNESS_BERRY_PROPERTIES` and `ModFoodProperties.ENCHANTED_SWIFTNESS_BERRY_EFFECT` when constructing the item with `.food()`.
  - `public static final RegistryObject<Item> ENCHANTED_SWIFTNESS_BERRY = registerItem("enchanted_swiftness_berry", ...)`

- **Item Model (`src/main/resources/assets/tutorialmod/models/item/enchanted_swiftness_berry.json`):**

  - Identical structure to the regular berry's model but points to a different texture: `"layer0": "tutorialmod:item/swiftness_berry2"`. This allows it to have a distinct visual appearance.

- **Texture (`src/main/resources/assets/tutorialmod/textures/item/swiftness_berry2.png`):**

  - A separate 16x16 PNG image file, visually distinct from the regular Swiftness Berry (e.g., different color, added glint/sparkle effect).

- **Language Entry (`src/main/resources/assets/tutorialmod/lang/en_us.json`):**

  - `"item.tutorialmod.enchanted_swiftness_berry": "Enchanted Swiftness Berry"`

- **Creative Tab Integration (`src/main/java/net/kaupenjoe/tutorialmod/item/ModCreativeModeTabs.java`):**
  - `output.accept(ModItems.ENCHANTED_SWIFTNESS_BERRY.get());`: Added to the same custom creative tab.

## Development Process & AI Assistance

The development of these features was an iterative process, greatly facilitated by AI assistance for initial code scaffolding and problem-solving:

1.  **Conceptualization:** Clearly defining the desired behavior and effects for each feature (e.g., "player flies when holding R and a sword," "berry gives Speed X and is always edible").
2.  **Initial Implementation (with AI help):**
    - For Sword Flight: The AI helped generate the initial structure for `ModKeyBindings.java` (including `KeyMapping` instantiation and event subscription) and `ClientForgeEvents.java` (subscribing to `ClientTickEvent` and the basic conditional logic for checking the key and item).
    - For Berries: The AI provided the structure for `ModFoodProperties.java` (showing how to use `FoodProperties.Builder` and `Consumable` with `ApplyStatusEffectsConsumeEffect`) and `ModItems.java` (demonstrating the registration pattern). It also generated the basic JSON structure for item models.
3.  **Code Integration & Review:** The AI-generated code was then reviewed, understood, and integrated into the existing mod structure. This often involved adapting variable names, ensuring correct package imports, and confirming logic flow.
4.  **Building & Testing (`./gradlew build runClient`):** Each small piece of functionality was compiled and tested in-game. This was crucial for catching errors early.
5.  **Debugging & Refinement (Iterative Loop):**
    - **Symptom Identification:** Observing what wasn't working as expected (e.g., flight key has no effect, item has missing texture, food doesn't give effects, game crashes).
    - **Log Analysis:** Checking the Minecraft/Forge logs and Gradle console output for error messages and stack traces.
    - **Troubleshooting Steps (often guided by AI suggestions or modding knowledge):**
      - **Registrations:** Double-checking that all necessary components (key bindings, event handlers, items, creative tabs) were correctly registered with Forge's event buses or registries.
      - **Paths & Naming:** Verifying that file paths in models (`.json`) correctly pointed to textures (`.png`), and that translation keys in code matched those in `en_us.json`. Case sensitivity can be an issue.
      - **API Usage:** Ensuring Forge/Minecraft APIs were used correctly (e.g., calling `player.onUpdateAbilities()`, using the correct parameters for `MobEffectInstance`, understanding the difference between `mayfly` and `flying`).
      - **Client vs. Server Logic:** Confirming that client-only code (like key handling, rendering pre-setup) was in client-specific classes or events (`Dist.CLIENT`).
    - The AI was particularly helpful in suggesting potential causes for common errors (like missing textures or unregistered objects) and explaining the purpose of specific API calls when they were unclear.
6.  **Iteration & Expansion:** Once a base feature worked (e.g., the regular Swiftness Berry), it was expanded upon (e.g., creating the Enchanted Swiftness Berry by modifying and adding to the existing patterns).

The AI served as a significant accelerator and learning tool, handling some of the more boilerplate aspects of Forge modding and providing quick explanations or starting points for complex APIs. However, a foundational understanding of Java and the mod's structure was still necessary to effectively guide the AI, integrate its suggestions, and perform the final debugging.
