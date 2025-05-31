# Project: Sword Flight & Custom Food Items for Kaupenjoe's Forge Tutorial Mod (Minecraft 1.21.X)

## Overview

This project enhanced Kaupenjoe's Forge 1.21.X tutorial mod by implementing two primary features:

1.  **Sword Flight:** A client-side mechanic enabling players to toggle flight by pressing a designated key ('R') while holding any sword.
2.  **Custom Food Items:** Two new berries, the "Swiftness Berry" and the "Enchanted Swiftness Berry," with custom potion effects, textures, and specific eating behaviors.

The development process focused on understanding and utilizing core Forge APIs for key bindings, event handling, player abilities, item and food property registration, model and texture management, and creative tab integration.

## New Features Detailed

### 1. Sword Flight

This feature allows players to fly when holding any item that is an instance of `net.minecraft.world.item.SwordItem` and pressing the 'R' key.

**Implementation Details:**

- **Key Binding (`src/main/java/net/kaupenjoe/tutorialmod/client/ModKeyBindings.java`):**

  - A `KeyMapping` instance named `FLIGHT_KEY` was created. This object represents the actual keybind in the game.
  - Constructor arguments for `KeyMapping`:
    - `"key." + TutorialMod.MOD_ID + ".flight"`: This is the translation key for the name of the keybinding that appears in the controls menu (e.g., "key.tutorialmod.flight").
    - `InputConstants.Type.KEYSYM`: Specifies that this is a standard keyboard key.
    - `GLFW.GLFW_KEY_R`: Sets the default physical key to 'R'. `GLFW` is the library Minecraft uses for input handling.
    - `"key.category." + TutorialMod.MOD_ID + ".sword_soaring"`: Assigns the keybinding to a custom category in the controls menu (e.g., "key.category.tutorialmod.sword_soaring").
  - **Registration:** The `FLIGHT_KEY` is registered with Forge during the `RegisterKeyMappingsEvent`. This event is fired on the client-side MOD event bus. The `ModKeyBindings` class is annotated with `@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)` to automatically subscribe its static `@SubscribeEvent` methods for client-side MOD bus events.

- **Event Handling (`src/main/java/net/kaupenjoe/tutorialmod/event/ClientForgeEvents.java`):**

  - This class handles the logic when the flight key is pressed. It subscribes to the `TickEvent.ClientTickEvent`.
  - The class is annotated with `@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)` to listen to client-side FORGE bus events.
  - **`onClientTick(TickEvent.ClientTickEvent event)` method:**

    - Checks `if (event.phase != TickEvent.Phase.END)` to ensure logic runs only once at the end of a tick.
    - Gets the `LocalPlayer` instance: `Minecraft.getInstance().player`.
    - **Activation Logic:**

      - `if (ModKeyBindings.FLIGHT_KEY.isDown() && player != null && player.getMainHandItem().getItem() instanceof SwordItem)`: Checks if the flight key is held, the player exists, and the player is holding a sword.
      - `player.getAbilities().mayfly = true;`: Sets the player's ability to be _allowed_ to fly.
      - A static boolean `modEnabledFlight` was used to track if the flight was initiated by this mod, to distinguish from creative mode flight. If conditions are met, `modEnabledFlight` is set to true.
      - `player.getAbilities().flying = true;` is set if `modEnabledFlight` is true.

    - **Deactivation Logic:**
      - `else if (player != null && modEnabledFlight)`: If the conditions for flight are no longer met (key released, not holding a sword, or player is null) AND the flight was previously enabled by the mod:
      - `player.getAbilities().mayfly = false;`
      - `player.getAbilities().flying = false;`
      - `modEnabledFlight = false;`
    - `player.onUpdateAbilities();`: This is crucial. It sends the updated abilities to the server (if applicable for other abilities, though `mayfly` for movement is largely client-authoritative for the local player but needs to be known for server-side checks like anti-cheat or fall damage). It ensures the game correctly processes the ability changes.
    - `player.fallDistance = 0.0F;`: Resets fall distance when disabling flight to prevent the player from taking fall damage.

- **Language Entries (`src/main/resources/assets/tutorialmod/lang/en_us.json`):**
  - `"key.category.tutorialmod.sword_soaring": "Sword Soaring"`
  - `"key.tutorialmod.flight": "Toggle Sword Flight"`

### 2. Swiftness Berry

A custom food item that grants a significant speed boost.

**Implementation Details:**

- **Food Properties (`src/main/java/net/kaupenjoe/tutorialmod/item/ModFoodProperties.java`):**

  - `public static final FoodProperties SWIFTNESS_BERRY`:
    - `new FoodProperties.Builder().nutrition(2).saturationModifier(0.2f)`: Defines basic food values.
    - `.alwaysEdible()`: Allows the berry to be eaten even if the player's hunger bar is full.
  - `public static final Consumable SWIFTNESS_BERRY_EFFECT`:
    - `Consumables.defaultFood().onConsume(...)`: Builds a `Consumable` component.
    - `new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 9), 1.0f)`:
      - `MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 9)`: Speed X (Amplifier 9) for 60 seconds (1200 ticks).
      - `1.0f`: 100% chance to apply.

- **Item Registration (`src/main/java/net/kaupenjoe/tutorialmod/item/ModItems.java`):**

  - `public static final RegistryObject<Item> SWIFTNESS_BERRY = registerItem(...)`: Registers the item.
  - `(properties) -> new Item(properties.food(ModFoodProperties.SWIFTNESS_BERRY, ModFoodProperties.SWIFTNESS_BERRY_EFFECT))`: Attaches `FoodProperties` and `Consumable` components.

- **Item Model (`src/main/resources/assets/tutorialmod/models/item/swiftness_berry.json`):**

  - `{ "parent": "item/generated", "textures": { "layer0": "tutorialmod:item/swiftness_berry" } }`: Standard 2D item sprite pointing to its texture.

- **Texture (`src/main/resources/assets/tutorialmod/textures/item/swiftness_berry.png`):**

  - 16x16 PNG image.

- **Language Entry (`src/main/resources/assets/tutorialmod/lang/en_us.json`):**

  - `"item.tutorialmod.swiftness_berry": "Swiftness Berry"`

- **Creative Tab (`src/main/java/net/kaupenjoe/tutorialmod/item/ModCreativeModeTabs.java`):**
  - `output.accept(ModItems.SWIFTNESS_BERRY.get());`: Adds item to creative tab.

### 3. Enchanted Swiftness Berry

A powerful version with multiple effects and higher food values.

**Implementation Details:**

- **Food Properties (`src/main/java/net/kaupenjoe/tutorialmod/item/ModFoodProperties.java`):**

  - `public static final FoodProperties ENCHANTED_SWIFTNESS_BERRY_PROPERTIES`:
    - `.nutrition(4).saturationModifier(1.2f).alwaysEdible()`
  - `public static final Consumable ENCHANTED_SWIFTNESS_BERRY_EFFECT`:
    - `new ApplyStatusEffectsConsumeEffect(List.of(...), 1.0f)`: Applies a list:
      - `new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 4)`: Speed V.
      - `new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3)`: Absorption IV.
      - `new MobEffectInstance(MobEffects.REGENERATION, 600, 3)`: Regeneration IV.
      - `new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 1)`: Resistance II.
      - `new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0)`: Fire Resistance I.

- **Item Registration (`src/main/java/net/kaupenjoe/tutorialmod/item/ModItems.java`):**

  - Registers `ENCHANTED_SWIFTNESS_BERRY` using its specific properties and effects.

- **Item Model (`src/main/resources/assets/tutorialmod/models/item/enchanted_swiftness_berry.json`):**

  - Points to `"layer0": "tutorialmod:item/swiftness_berry2"`.

- **Texture (`src/main/resources/assets/tutorialmod/textures/item/swiftness_berry2.png`):**

  - Distinct 16x16 PNG.

- **Language Entry (`src/main/resources/assets/tutorialmod/lang/en_us.json`):**

  - `"item.tutorialmod.enchanted_swiftness_berry": "Enchanted Swiftness Berry"`

- **Creative Tab (`src/main/java/net/kaupenjoe/tutorialmod/item/ModCreativeModeTabs.java`):**
  - `output.accept(ModItems.ENCHANTED_SWIFTNESS_BERRY.get());`

## Development Process & AI Assistance

The development of these features was an iterative process:

1.  **Conceptualization:** Defining the desired behavior.
2.  **Initial Implementation (with AI help):** The AI (Cursor) generated boilerplate for classes, event subscriptions, and initial structures for item/food definitions.
3.  **Code Integration:** Reviewing and integrating AI-generated code.
4.  **Building & Testing:** Using `./gradlew build runClient`.
5.  **Debugging & Refinement:** Analyzing symptoms (e.g., flight not working, missing textures, incorrect food behavior) and troubleshooting by checking registrations, paths, logic, and API usage, often with AI suggestions for checkpoints.
6.  **Iteration:** Refining code and adding sub-features based on tests.

The AI served as a knowledgeable assistant, accelerating the process by handling verbose coding tasks and providing quick answers or suggestions for API usage and common Forge patterns.
