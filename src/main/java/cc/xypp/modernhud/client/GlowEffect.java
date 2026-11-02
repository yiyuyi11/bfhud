package cc.xypp.modernhud.client;

import cc.xypp.modernhud.config.HudConfig;
import net.minecraft.client.gui.GuiGraphics;

public final class GlowEffect {
   private GlowEffect() {
   }

   public static void drawGlowRect(GuiGraphics g, int x, int y, int w, int h, int colorRgb, float alpha, float spread) {
      float k = (float)HudConfig.glowIntensity / 100.0F;
      alpha *= k;
      if (!(alpha <= 0.001F) && !(spread <= 0.01F) && w > 0 && h > 0) {
         SoftDraw.soft(g, (float)x, (float)y, (float)w, (float)h, colorRgb, 0.22F * alpha, 0.58F * alpha, Math.max(1.5F, spread * 2.4F), true);
      }
   }

   public static void drawBloom(GuiGraphics g, float cx, float cy, float radius, int colorRgb, float alpha) {
      alpha *= (float)HudConfig.glowIntensity / 100.0F;
      if (!(alpha <= 0.001F) && !(radius <= 0.5F)) {
         SoftDraw.radial(g, cx, cy, radius, colorRgb, 0.5F * alpha, true);
      }
   }
}
