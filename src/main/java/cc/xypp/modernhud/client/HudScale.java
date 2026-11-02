package cc.xypp.modernhud.client;

import cc.xypp.modernhud.config.HudConfig;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class HudScale {
   private HudScale() {
   }

   public static double wanted(Window w) {
      switch (HudConfig.guiScaleMode) {
         case FIXED:
            return Math.max(0.25, HudConfig.hudGuiScale);
         case RESOLUTION:
            double v = HudConfig.hudGuiScale * (double)w.getHeight() / (double)Math.max(1, HudConfig.guiScaleReferenceHeight);
            if (HudConfig.guiScaleSnap) {
               v = Math.max(1.0, (double)Math.round(v));
            }

            return Math.max(0.25, v);
         default:
            return w.getGuiScale();
      }
   }

   public static IGuiOverlay wrap(IGuiOverlay inner) {
      return (graphics, partialTick, sw, sh) -> render(inner, graphics, partialTick, sw, sh);
   }

   public static void render(IGuiOverlay inner, GuiGraphics graphics, float partialTick, int sw, int sh) {
      Window w = Minecraft.getInstance().getWindow();
      double mc = w.getGuiScale();
      double want = wanted(w);
      if (Math.abs(want - mc) < 0.001) {
         inner.render(graphics, partialTick, sw, sh);
      } else {
         float k = (float)(want / mc);
         int vw = (int)Math.ceil((double)w.getWidth() / want);
         int vh = (int)Math.ceil((double)w.getHeight() / want);
         PoseStack pose = graphics.pose();
         pose.pushPose();
         pose.scale(k, k, 1.0F);

         try {
            inner.render(graphics, partialTick, vw, vh);
         } finally {
            pose.popPose();
         }
      }
   }
}
