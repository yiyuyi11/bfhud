package cc.xypp.modernhud.client;

import net.minecraft.client.gui.GuiGraphics;

public final class BarRender {
   public static final int TRACK = 1711276032;
   public static final int EDGE = 872415231;
   public static final int TIER_GRAY = -4669498;
   public static final int TIER_BLUE = -11228184;
   public static final int TIER_PURPLE = -6393886;
   public static final int TIER_RED = -1552072;
   public static final int HEALTH = -723208;
   public static final int HEALTH_LOW = -1552072;
   public static final int OCEAN_BLUE = -14909738;

   private BarRender() {
   }

   public static int tierColor(int filledSegments) {
      if (filledSegments >= 4) {
         return -1552072;
      } else if (filledSegments == 3) {
         return -6393886;
      } else {
         return filledSegments == 2 ? -11228184 : -4669498;
      }
   }

   public static void track(GuiGraphics g, int x, int y, int w, int h) {
      g.fill(x, y, x + w, y + h, 1711276032);
      g.fill(x, y, x + w, y + 1, 872415231);
   }

   public static void fill(GuiGraphics g, int x, int y, int w, int h, float fraction, int color) {
      int filled = Math.round((float)w * clamp01(fraction));
      if (filled > 0) {
         g.fill(x, y, x + filled, y + h, color);
         if (h >= 3) {
            g.fill(x, y, x + filled, y + 1, lighten(color, 0.3F));
         }
      }
   }

   public static void bar(GuiGraphics g, int x, int y, int w, int h, float fraction, int color) {
      track(g, x, y, w, h);
      fill(g, x, y, w, h, fraction, color);
   }

   public static void barRTL(GuiGraphics g, int x, int y, int w, int h, float fraction, int color) {
      track(g, x, y, w, h);
      int filled = Math.round((float)w * clamp01(fraction));
      if (filled > 0) {
         int fx = x + w - filled;
         g.fill(fx, y, x + w, y + h, color);
         if (h >= 3) {
            g.fill(fx, y, x + w, y + 1, lighten(color, 0.3F));
         }
      }
   }

   public static void plates(GuiGraphics g, int x, int y, int w, int h, float value, int segments, double perSegment, boolean useCustomColor, int customColor) {
      if (segments > 0) {
         double barMax = (double)segments * perSegment;
         int filledWhole = (int)Math.floor(Math.min((double)value, barMax) / perSegment);
         int color = useCustomColor ? customColor | 0xFF000000 : tierColor(filledWhole);
         int gap = Math.max(2, w / 40);
         int cellW = Math.max(1, (w - gap * (segments - 1)) / segments);
         HudBatch.run(g, () -> {
            for (int i = 0; i < segments; i++) {
               int cx = x + i * (cellW + gap);
               g.fill(cx, y, cx + cellW, y + h, 1711276032);
               double segValue = (double)value - (double)i * perSegment;
               if (segValue > 0.0) {
                  float frac = (float)Math.min(1.0, segValue / perSegment);
                  int fw = Math.round((float)cellW * frac);
                  g.fill(cx, y, cx + fw, y + h, color);
                  if (h >= 3) {
                     g.fill(cx, y, cx + fw, y + 1, lighten(color, 0.3F));
                  }
               }
            }
         });
      }
   }

   public static float overflow(float value, int segments, double perSegment) {
      double barMax = (double)segments * perSegment;
      return (float)Math.max(0.0, (double)value - barMax);
   }

   private static float clamp01(float v) {
      return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
   }

   public static int lighten(int argb, float amount) {
      int a = argb >>> 24 & 0xFF;
      int r = argb >>> 16 & 0xFF;
      int g = argb >>> 8 & 0xFF;
      int b = argb & 0xFF;
      r = (int)((float)r + (float)(255 - r) * amount);
      g = (int)((float)g + (float)(255 - g) * amount);
      b = (int)((float)b + (float)(255 - b) * amount);
      return a << 24 | r << 16 | g << 8 | b;
   }
}
