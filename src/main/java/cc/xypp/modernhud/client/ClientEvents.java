package cc.xypp.modernhud.client;

import cc.xypp.modernhud.config.ConfigRefresh;
import cc.xypp.modernhud.config.ExtraHudConfig;
import cc.xypp.modernhud.config.HudConfig;
import cc.xypp.modernhud.config.WeaponConfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;



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

   private static net.minecraftforge.client.gui.overlay.IGuiOverlay layer(IGuiOverlay inner) {
      return (gui,graphics,partial,w,h)->{ensureConfig();if(!HudConfig.master())return;HudScale.render(inner,graphics,partial,w,h);};
   }
   public static void onRegisterOverlays(net.minecraftforge.client.event.RegisterGuiOverlaysEvent event) {
      event.registerAboveAll("feedback_icon",layer(new FeedbackIconOverlay()));
      event.registerAboveAll("weapon_hud",layer(WEAPON_HUD));
      event.registerAboveAll("ammo_alert",layer(AMMO_ALERT));
      event.registerAboveAll("perk_toast",layer(new PerkToastOverlay()));
      event.registerAboveAll("boss_bar",layer(new BossBarOverlay()));
      event.registerAboveAll("vitals_hud",layer(VitalsHud.create()));
      event.registerAboveAll("hotbar_hud",layer(HotbarHud.create()));
      event.registerAboveAll("remaining_slots",layer(RemainingSlotsHud.create()));
   }

   public static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         ConfigRefresh.refreshAll();
         WeaponConfig.syncHudSource();
      });
   }
}
