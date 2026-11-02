package cc.xypp.modernhud.client;

import cc.xypp.modernhud.config.ElementStyle;
import cc.xypp.modernhud.config.PanelBorderStyle;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

public final class HudPanel {
   private HudPanel() {
   }

   public static void draw(GuiGraphics g, int x, int y, int w, int h, int colorRgb, double opacityPercent) {
      if (w > 0 && h > 0) {
         int alpha = (int)Math.round(clamp(opacityPercent, 0.0, 100.0) * 2.55);
         if (alpha > 0) {
            int color = alpha << 24 | colorRgb & 16777215;
            withSafeShaderColor(() -> g.fill(x, y, x + w, y + h, color));
         }
      }
   }

   public static void drawStyled(GuiGraphics g, int x, int y, int w, int h, ElementStyle st, float alpha) {
      float a = st.alpha(alpha);
      if (!(a <= 0.001F)) {
         if (st.panelEnabled) {
            draw(g, x, y, w, h, st.panelColor, (double)((float)st.panelOpacity * a));
         }

         if (st.borderStyle != PanelBorderStyle.NONE) {
            drawBorder(g, x, y, w, h, st.borderColor, (double)((float)st.borderOpacity * a), st.borderStyle, st.borderLength, st.borderOutset);
         }
      }
   }

   public static void drawBorder(
      GuiGraphics g, int x, int y, int w, int h, int colorRgb, double opacityPercent, PanelBorderStyle style, int armLength, int outset
   ) {
      if (style != PanelBorderStyle.NONE) {
         HudBatch.run(g, () -> {
            switch (style) {
               case CORNERS:
                  drawCornerBrackets(g, x, y, w, h, colorRgb, opacityPercent, armLength, outset);
                  break;
               case FULL:
                  drawFullBorder(g, x, y, w, h, colorRgb, opacityPercent);
                  break;
               case DOTS:
                  drawCornerDots(g, x, y, w, h, colorRgb, opacityPercent, Math.max(2, armLength - 1), outset);
                  break;
               case BOTTOM_DOTS:
                  drawBottomDots(g, x, y, w, h, colorRgb, opacityPercent, Math.max(2, armLength - 1));
                  break;
               case FRAME:
                  drawFullBorder(g, x, y, w, h, colorRgb, opacityPercent * 0.35);
                  drawCornerDots(g, x, y, w, h, colorRgb, opacityPercent, Math.max(2, armLength - 1), 0);
               case NONE:
            }
         });
      }
   }

   public static void drawCornerBrackets(GuiGraphics g, int x, int y, int w, int h, int colorRgb, double opacityPercent, int armLength, int outset) {
      if (w > 0 && h > 0 && armLength > 0) {
         int alpha = (int)Math.round(clamp(opacityPercent, 0.0, 100.0) * 2.55);
         if (alpha > 0) {
            int color = alpha << 24 | colorRgb & 16777215;
            int proportionalCap = Math.max(1, Math.round((float)Math.min(w, h) * 0.3F));
            int arm = Math.min(armLength, proportionalCap);
            int t = 1;
            int ox = x - outset;
            int oy = y - outset;
            int ow = w + outset * 2;
            int oh = h + outset * 2;
            withSafeShaderColor(() -> {
               g.fill(ox, oy, ox + arm, oy + t, color);
               g.fill(ox, oy, ox + t, oy + arm, color);
               g.fill(ox + ow - arm, oy, ox + ow, oy + t, color);
               g.fill(ox + ow - t, oy, ox + ow, oy + arm, color);
               g.fill(ox, oy + oh - t, ox + arm, oy + oh, color);
               g.fill(ox, oy + oh - arm, ox + t, oy + oh, color);
               g.fill(ox + ow - arm, oy + oh - t, ox + ow, oy + oh, color);
               g.fill(ox + ow - t, oy + oh - arm, ox + ow, oy + oh, color);
            });
         }
      }
   }

   public static void drawCornerDots(GuiGraphics g, int x, int y, int w, int h, int colorRgb, double opacityPercent, int dotSize, int outset) {
      if (w > 0 && h > 0 && dotSize > 0) {
         int alpha = (int)Math.round(clamp(opacityPercent, 0.0, 100.0) * 2.55);
         if (alpha > 0) {
            int color = alpha << 24 | colorRgb & 16777215;
            int size = Math.min(dotSize, Math.max(1, Math.min(w, h) / 2));
            int half = size / 2;
            int x0 = x - outset - half;
            int y0 = y - outset - half;
            int x1 = x + w + outset - (size - half);
            int y1 = y + h + outset - (size - half);
            withSafeShaderColor(() -> {
               g.fill(x0, y0, x0 + size, y0 + size, color);
               g.fill(x1, y0, x1 + size, y0 + size, color);
               g.fill(x0, y1, x0 + size, y1 + size, color);
               g.fill(x1, y1, x1 + size, y1 + size, color);
            });
         }
      }
   }

   public static void drawFullBorder(GuiGraphics g, int x, int y, int w, int h, int colorRgb, double opacityPercent) {
      if (w > 0 && h > 0) {
         int alpha = (int)Math.round(clamp(opacityPercent, 0.0, 100.0) * 2.55);
         if (alpha > 0) {
            int color = alpha << 24 | colorRgb & 16777215;
            int t = 1;
            withSafeShaderColor(() -> {
               g.fill(x, y, x + w, y + t, color);
               g.fill(x, y + h - t, x + w, y + h, color);
               g.fill(x, y, x + t, y + h, color);
               g.fill(x + w - t, y, x + w, y + h, color);
            });
         }
      }
   }

   public static void drawBottomDots(GuiGraphics g, int x, int y, int w, int h, int colorRgb, double opacityPercent, int dotSize) {
      if (w > 0 && h > 0 && dotSize > 0) {
         int alpha = (int)Math.round(clamp(opacityPercent, 0.0, 100.0) * 2.55);
         if (alpha > 0) {
            int color = alpha << 24 | colorRgb & 16777215;
            int size = Math.min(dotSize, Math.max(1, Math.min(w, h) / 3));
            withSafeShaderColor(() -> {
               g.fill(x, y + h - size, x + size, y + h, color);
               g.fill(x + w - size, y + h - size, x + w, y + h, color);
            });
         }
      }
   }

   private static void withSafeShaderColor(Runnable action) {
      float[] cur = RenderSystem.getShaderColor();
      float pr = cur[0];
      float pg = cur[1];
      float pb = cur[2];
      float pa = cur[3];

      try {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         action.run();
      } finally {
         RenderSystem.setShaderColor(pr, pg, pb, pa);
      }
   }

   private static double clamp(double v, double min, double max) {
      return Math.max(min, Math.min(max, v));
   }

   public static int padding() {
      return 5;
   }
}
