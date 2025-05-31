package net.kaupenjoe.tutorialmod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.util.List;
import java.util.function.Supplier;

public class ModFoodProperties {
        public static final FoodProperties KOHLRABI = new FoodProperties.Builder().nutrition(3)
                        .saturationModifier(0.25f).build();

        public static final Consumable KOHLRABI_EFFECT = Consumables.defaultFood().onConsume(
                        new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.GLOWING, 200), 0.25f))
                        .build();

        public static final FoodProperties HONEY_BERRY = new FoodProperties.Builder().nutrition(2)
                        .saturationModifier(0.15f).build();

        public static final FoodProperties SWIFTNESS_BERRY = new FoodProperties.Builder().nutrition(2)
                        .saturationModifier(0.2f)
                        .alwaysEdible()
                        .build();

        public static final Consumable SWIFTNESS_BERRY_EFFECT = Consumables.defaultFood().onConsume(
                        new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 9),
                                        1.0f))
                        .build();

        public static final FoodProperties ENCHANTED_SWIFTNESS_BERRY_PROPERTIES = new FoodProperties.Builder()
                        .nutrition(4)
                        .saturationModifier(1.2f)
                        .alwaysEdible()
                        .build();

        public static final Consumable ENCHANTED_SWIFTNESS_BERRY_EFFECT = Consumables.defaultFood().onConsume(
                        new ApplyStatusEffectsConsumeEffect(List.of(
                                        new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 4),
                                        new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3),
                                        new MobEffectInstance(MobEffects.REGENERATION, 600, 3),
                                        new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 1),
                                        new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0)), 1.0f))
                        .build();
}
