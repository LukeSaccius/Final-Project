package net.kaupenjoe.tutorialmod.event;

import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.client.ModKeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    private static boolean modEnabledFlight = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return; // Only process at the end of the tick
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            if (modEnabledFlight) {
                modEnabledFlight = false;
            }
            return;
        }

        boolean flightKeyPressed = ModKeyBindings.FLIGHT_KEY.isDown();
        boolean holdingSword = player.getMainHandItem().getItem() instanceof SwordItem;

        if (flightKeyPressed && holdingSword) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            if (!player.getAbilities().flying) {
                player.getAbilities().flying = true;
            }
            modEnabledFlight = true;
        } else {
            if (modEnabledFlight) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
                player.fallDistance = 0;
                modEnabledFlight = false;
            }
        }
    }
}