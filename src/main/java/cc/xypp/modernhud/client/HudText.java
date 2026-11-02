package cc.xypp.modernhud.client;

import cc.xypp.modernhud.config.HudConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

public final class HudText {
   private HudText() {
   }

   public static float physPerUnit(GuiGraphics g) {
      Matrix4f m = g.pose().last().pose();
      float sx = (float)Math.sqrt((double)(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02()));
      return sx * (float)Minecraft.getInstance().getWindow().getGuiScale();
   }

   public static float snapScale(GuiGraphics g, double requested) {
      return snapScale(g, requested, 1.0F);
   }

   public static float snapScale(GuiGraphics g, double requested, float parentScale) {
      float r = (float)requested;
      if (HudConfig.pixelPerfectText && !(r <= 0.0F)) {
         float base = physPerUnit(g) * parentScale;
         if (base <= 0.01F) {
            return r;
         } else {
            float p = Math.max(1.0F, (float)Math.round(r * base));
            return p / base;
         }
      } else {
         return r;
      }
   }

   public static float snapElement(GuiGraphics g, double requested) {
      return HudConfig.pixelPerfectElements ? snapScale(g, requested) : (float)requested;
   }

   public static float[] snapPair(GuiGraphics g, double big, double small) {
      float b = snapScale(g, big);
      float s = snapScale(g, small);
      if (HudConfig.pixelPerfectText && small < big) {
         float base = physPerUnit(g);
         if (base > 0.01F && s >= b - 1.0E-4F && Math.round(b * base) > 1) {
            s = (float)(Math.round(b * base) - 1) / base;
         }
      }

      return new float[]{b, s};
   }

   public static void drawSnapped(GuiGraphics g, Font font, String text, float x, float y, float scale, int color, boolean shadow) {
      PoseStack pose = g.pose();
      pose.pushPose();
      float tx = x;
      float ty = y;
      if (HudConfig.pixelPerfectText) {
         Matrix4f m = g.pose().last().pose();
         float gs = (float)Minecraft.getInstance().getWindow().getGuiScale();
         float sx = (float)Math.sqrt((double)(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02())) * gs;
         if (sx > 0.01F) {
            float dx = (m.m00() * x + m.m10() * y + m.m30()) * gs;
            float dy = (m.m01() * x + m.m11() * y + m.m31()) * gs;
            tx = x + ((float)Math.round(dx) - dx) / sx;
            ty = y + ((float)Math.round(dy) - dy) / sx;
         }
      }

      pose.translate(tx, ty, 0.0F);
      pose.scale(scale, scale, 1.0F);
      g.drawString(font, text, 0, 0, color, shadow);
      pose.popPose();
   }

   public static float layoutRatio(float physPerUnit) {
      return HudConfig.pixelPerfectText && !(physPerUnit <= 0.01F) ? Math.max(1.0F, (float)Math.round(physPerUnit)) / physPerUnit : 1.0F;
   }

   public static float width(GuiGraphics g, Font font, String s) {
      return (float)font.width(s) * layoutRatio(currentPhys(g));
   }

   public static float width(GuiGraphics g, Font font, Component c) {
      return (float)font.width(c) * layoutRatio(currentPhys(g));
   }

   private static float currentPhys(GuiGraphics g) {
      Matrix4f m = g.pose().last().pose();
      float sx = (float)Math.sqrt((double)(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02()));
      return sx * (float)Minecraft.getInstance().getWindow().getGuiScale();
   }

   public static void text(GuiGraphics g, Font font, String s, float x, float y, int color, boolean shadow) {
      draw(g, font, null, s, x, y, color, shadow);
   }

   public static void text(GuiGraphics g, Font font, Component c, float x, float y, int color, boolean shadow) {
      draw(g, font, c, null, x, y, color, shadow);
   }

   private static void draw(GuiGraphics g, Font font, Component c, String s, float x, float y, int color, boolean shadow) {
      PoseStack ps = g.pose();
      Matrix4f m = ps.last().pose();
      float gs = (float)Minecraft.getInstance().getWindow().getGuiScale();
      float sx = (float)Math.sqrt((double)(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02()));
      float phys = sx * gs;
      if (HudConfig.pixelPerfectText && !(phys <= 0.01F)) {
         float p = Math.max(1.0F, (float)Math.round(phys));
         float ox = m.m00() * x + m.m10() * y + m.m30();
         float oy = m.m01() * x + m.m11() * y + m.m31();
         float dx = (float)Math.round(ox * gs) / gs;
         float dy = (float)Math.round(oy * gs) / gs;
         float z = m.m32();
         ps.pushPose();
         Matrix4f pm = ps.last().pose();
         pm.identity();
         pm.translate(dx, dy, z);
         pm.scale(p / gs, p / gs, 1.0F);
         plain(g, font, c, s, 0.0F, 0.0F, color, shadow);
         ps.popPose();
      } else {
         plain(g, font, c, s, x, y, color, shadow);
      }
   }

   private static void plain(GuiGraphics g, Font font, Component c, String s, float x, float y, int color, boolean shadow) {
      if (c != null) {
         g.drawString(font, c, Math.round(x), Math.round(y), color, shadow);
      } else {
         g.drawString(font, s, Math.round(x), Math.round(y), color, shadow);
      }
   }
}
