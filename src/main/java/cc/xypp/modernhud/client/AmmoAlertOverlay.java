package cc.xypp.modernhud.client;

import cc.xypp.modernhud.client.compat.SuperbWarfareCompat;
import cc.xypp.modernhud.client.compat.TaczCompat;
import cc.xypp.modernhud.config.ElementStyle;
import cc.xypp.modernhud.config.WeaponConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

public class AmmoAlertOverlay implements IGuiOverlay {
   private final EasedAnimator vis = new EasedAnimator(200.0F, EasedAnimator.Easing.CUBIC_IN_OUT);
   private String lastText = "";
   private int lastColor = 16777215;
   private long lastPeriod = 600L;

   public AmmoAlertOverlay() {
      this.vis.snapTo(0.0F);
   }

   public void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
      AmmoAlertOverlay.Alert alert = currentAlert();
      ElementStyle st = WeaponConfig.alertStyle;
      if (alert != null) {
         this.lastText = alert.text();
         this.lastColor = alert.color();
         this.lastPeriod = alert.periodMs();
      }

      this.vis.setDurationMs(alert != null ? (float)st.fadeInMs : (float)st.fadeOutMs);
      this.vis.setTarget(alert != null ? 1.0F : 0.0F);
      float v = this.vis.update();
      if (!(v < 0.01F) && !this.lastText.isEmpty()) {
         float pulse = WeaponConfig.ammoAlertPulse
            ? 0.45F + 0.275F * (1.0F + (float)Math.sin((double)System.currentTimeMillis() / (double)this.lastPeriod))
            : 1.0F;
         float alpha = st.alpha(v * pulse);
         Font font = Minecraft.getInstance().font;
         float scale = HudText.snapElement(graphics, WeaponConfig.ammoAlertScale);
         int width = font.width(this.lastText);
         PoseStack pose = graphics.pose();
         pose.pushPose();
         pose.scale(scale, scale, 1.0F);
         float x = (float)screenWidth / 2.0F / scale - (float)width / 2.0F;
         float y = (float)screenHeight / 2.0F / scale + (float)WeaponConfig.ammoAlertYOffset / scale;
         int px = Math.round(x);
         int py = Math.round(y);
         HudPanel.drawStyled(graphics, px - 6, py - 3, width + 12, 9 + 5, st, v);
         if (WeaponConfig.ammoAlertGlow) {
            GlowEffect.drawGlowRect(graphics, px - 3, py - 2, width + 6, 9 + 3, this.lastColor, 0.45F * alpha, 5.0F);
         }

         if (alpha * 255.0F >= 16.0F) {
            int argb = (int)(alpha * 255.0F) << 24 | this.lastColor & 16777215;
            HudText.text(graphics, font, this.lastText, (float)px, (float)py, argb, true);
         }

         pose.popPose();
      }
   }

   private static AmmoAlertOverlay.Alert currentAlert() {
      if (WeaponConfig.enabled && WeaponConfig.showAmmoAlerts && TaczCompat.loaded()) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null) {
            return null;
         } else if (Minecraft.getInstance().options.hideGui && !WeaponConfig.ignoreHideGui) {
            return null;
         } else if (SuperbWarfareCompat.isInVehicle(player)) {
            return null;
         } else {
            ItemStack held = player.getMainHandItem();
            if (!held.isEmpty() && TaczCompat.isGun(held) && !TaczCompat.usesInventoryAmmo(held)) {
               int mag = TaczCompat.magAmmo(player, held);
               int cap = TaczCompat.magCapacity(held);
               int reserve = TaczCompat.reserveAmmo(player, held);
               if (mag <= 0) {
                  return reserve != -1 && reserve <= 0
                     ? new AmmoAlertOverlay.Alert("NO AMMO", WeaponConfig.noAmmoAlertColor, 400L)
                     : new AmmoAlertOverlay.Alert("RELOAD", WeaponConfig.reloadAlertColor, 400L);
               } else {
                  return cap > 0 && 100.0 * (double)mag / (double)cap <= WeaponConfig.lowAmmoThresholdPercent
                     ? new AmmoAlertOverlay.Alert("LOW AMMO", WeaponConfig.lowAmmoAlertColor, 600L)
                     : null;
               }
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static record Alert(String text, int color, long periodMs) {
   }
}
