package net.kaupenjoe.tutorialmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeyBindings {

    public static final String KEY_CATEGORY_SWORD_SOARING = "key.category." + TutorialMod.MOD_ID + ".sword_soaring";
    public static final String KEY_BIND_FLIGHT = "key." + TutorialMod.MOD_ID + ".flight";

    public static final KeyMapping FLIGHT_KEY = new KeyMapping(
            KEY_BIND_FLIGHT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KEY_CATEGORY_SWORD_SOARING);

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(FLIGHT_KEY);
    }
}