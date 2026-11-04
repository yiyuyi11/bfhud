package cc.xypp.modernhud.client;

import cc.xypp.modernhud.config.ConfigRefresh;
import cc.xypp.modernhud.config.ExtraHudConfig;
import cc.xypp.modernhud.config.HudConfig;
import cc.xypp.modernhud.config.WeaponConfig;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * HUD layer registration.
 *
 * <p>Not an {@code @EventBusSubscriber}: {@code RegisterGuiLayersEvent} is a MOD-bus event and the
 * annotation's {@code bus} attribute is gone in 1.21.1, so {@link cc.xypp.modernhud.ModernHud} attaches this
 * listener to the mod bus explicitly.
 */
public class ClientEvents {
   private static final WeaponHudOverlay WEAPON_HUD = new WeaponHudOverlay();
   private static final AmmoAlertOverlay AMMO_ALERT = new AmmoAlertOverlay();

   static WeaponHudOverlay weaponHud() {
      return WEAPON_HUD;
   }

   static AmmoAlertOverlay ammoAlert() {
      return AMMO_ALERT;
   }

   /** Every layer passes here, so the config statics are refreshed as soon as the TOML is actually loaded. */
   private static boolean configReady;

   private static void ensureConfig() {
      if (configReady) return;
      try {
         HudConfig.refresh();
         ConfigRefresh.refreshAll();
         configReady = true;
      } catch (Throwable notYetLoaded) {
         // retry on the next frame
      }
   }

   /** Bridges the local overlay interface onto the 1.21.1 GUI layer system. */
   private static LayeredDraw.Layer layer(IGuiOverlay inner) {
      return (graphics, delta) -> {
         ensureConfig();
         if (!HudConfig.master()) return;
         HudScale.render(inner, graphics, delta.getGameTimeDeltaPartialTick(false),
                 graphics.guiWidth(), graphics.guiHeight());
      };
   }

   private static ResourceLocation id(String path) {
      return ResourceLocation.fromNamespaceAndPath("modernhud", path);
   }

   private static void hide(RegisterGuiLayersEvent event, ResourceLocation... layers) {
      for (ResourceLocation layer : layers) {
         // With the master switch off the original layer draws again, so the vanilla HUD is not left blank.
         event.wrapLayer(layer, original -> (graphics, delta) -> {
            if (HudConfig.master()) return;
            original.render(graphics, delta);
         });
      }
   }

   public static void onRegisterOverlays(RegisterGuiLayersEvent event) {
      // Unconditional: this runs during registration, before the TOML is loaded, so reading a config value
      // here silently skipped the hide. The mod draws its own six-slot strip, vitals and gear, so the
      // vanilla rows must always give way.
      // EXPERIENCE_LEVEL is a separate NeoForge layer from EXPERIENCE_BAR: the bar was hidden but the
      // level number kept drawing, which is exactly the leftover that was still on screen.
      hide(event, VanillaGuiLayers.HOTBAR, VanillaGuiLayers.PLAYER_HEALTH,
              VanillaGuiLayers.ARMOR_LEVEL, VanillaGuiLayers.FOOD_LEVEL, VanillaGuiLayers.EXPERIENCE_BAR,
              VanillaGuiLayers.EXPERIENCE_LEVEL, VanillaGuiLayers.JUMP_METER);

      event.registerAbove(VanillaGuiLayers.HOTBAR, id("feedback_icon"), layer(new FeedbackIconOverlay()));
      event.registerAbove(VanillaGuiLayers.HOTBAR, id("weapon_hud"), layer(WEAPON_HUD));
      event.registerAbove(VanillaGuiLayers.HOTBAR, id("ammo_alert"), layer(AMMO_ALERT));
      event.registerAbove(VanillaGuiLayers.HOTBAR, id("perk_toast"), layer(new PerkToastOverlay()));
      event.registerAbove(VanillaGuiLayers.HOTBAR, id("boss_bar"), layer(new BossBarOverlay()));
      event.registerAbove(VanillaGuiLayers.HOTBAR, id("vitals_hud"), layer(VitalsHud.create()));
      event.registerAbove(VanillaGuiLayers.HOTBAR, id("hotbar_hud"), layer(HotbarHud.create()));
      event.registerAbove(VanillaGuiLayers.HOTBAR, id("remaining_slots"), layer(RemainingSlotsHud.create()));
   }

   public static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         ConfigRefresh.refreshAll();
         WeaponConfig.syncHudSource();
      });
   }
}
