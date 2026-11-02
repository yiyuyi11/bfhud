package cc.xypp.modernhud.client;

import cc.xypp.modernhud.config.WeaponConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Keybinds. The killstreak/compass/editor keys are dropped with those widgets; only the weapon-HUD toggle
 * remains.
 */
public class ModKeybinds {
   public static final String CATEGORY = "key.categories.modernhud";
   public static final KeyMapping TOGGLE_WEAPON_HUD = new KeyMapping(
      "key.modernhud.toggle_weapon_hud", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(),
      "key.categories.modernhud"
   );

   @EventBusSubscriber(
      modid = "modernhud",
      value = {Dist.CLIENT}
   )
   public static class ForgeBus {
      @SubscribeEvent
      public static void onClientTick(ClientTickEvent.Post event) {
         while (TOGGLE_WEAPON_HUD.consumeClick()) {
            WeaponConfig.toggleEnabled();
         }
      }
   }

   /** Registered on the mod bus by {@code ModernHud}; the annotation would land on the game bus instead. */
   public static class ModBus {
      public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
         event.register(TOGGLE_WEAPON_HUD);
      }
   }
}
