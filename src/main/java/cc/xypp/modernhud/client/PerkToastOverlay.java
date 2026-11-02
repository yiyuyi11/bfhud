package cc.xypp.modernhud.client;

import cc.xypp.modernhud.client.compat.ModernLoadoutCompat;
import cc.xypp.modernhud.config.ElementStyle;
import cc.xypp.modernhud.config.HudConfig;
import cc.xypp.modernhud.config.HudPosition;
import cc.xypp.modernhud.config.KillstreakConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class PerkToastOverlay implements IGuiOverlay {
   private static final int PAD = 10;
   private static final int ROW_GAP = 4;
   private static final int ROW_PAD = 6;
   private static final int ICON_GAP = 6;

   public void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
      if (ModernLoadoutCompat.loaded() && KillstreakConfig.perkToast) {
         if (Minecraft.getInstance().player != null && ModernLoadoutCompat.refreshPerks()) {
            int n = ModernLoadoutCompat.perkCount();
            long now = System.currentTimeMillis();
            long elapsed = now - ModernLoadoutCompat.perkSelectionStartMs();
            long hold = (long)KillstreakConfig.perkHoldSeconds * 1000L;
            float spd = HudConfig.animations ? (float)Math.max(0.1, HudConfig.animationSpeed) : 1000.0F;
            long slideIn = (long)Math.max(1, Math.round((float)KillstreakConfig.perkStyle.fadeInMs / spd));
            long slideOut = (long)Math.max(1, Math.round((float)KillstreakConfig.perkStyle.fadeOutMs / spd));
            long stagger = (long)Math.round((float)KillstreakConfig.perkStaggerMs / spd);
            float op = (float)KillstreakConfig.perkStyle.opacity / 100.0F;
            long enterTotal = slideIn + (long)(n - 1) * stagger;
            long outStart = Math.max(hold, enterTotal);
            long outTotal = slideOut + (long)(n - 1) * stagger;
            if (elapsed >= 0L && elapsed <= outStart + outTotal) {
               Font font = Minecraft.getInstance().font;
               int icon = KillstreakConfig.perkIconSize;
               int rowH = Math.max(icon, 9 + 4);
               int nameW = 0;
               String[] names = new String[n];

               for (int i = 0; i < n; i++) {
                  names[i] = ModernLoadoutCompat.perkLabel(i).toUpperCase(Locale.ROOT);
                  nameW = Math.max(nameW, font.width(names[i]));
               }

               int contentW = nameW + 6 + icon;
               int boxW = contentW + 20;
               int boxH = n * rowH + (n - 1) * 4 + 20;
               float[] vis = new float[n];
               float overall = 0.0F;

               for (int i = 0; i < n; i++) {
                  float in = Ease.inOutCubic((float)(elapsed - (long)i * stagger) / (float)slideIn);
                  float out = Ease.inOutCubic((float)(elapsed - outStart - (long)i * stagger) / (float)slideOut);
                  vis[i] = in * (1.0F - out);
                  overall = Math.max(overall, vis[i]);
               }

               if (!(overall < 0.01F)) {
                  float scale = HudText.snapElement(graphics, KillstreakConfig.perkScale);
                  int w = Math.round((float)boxW * scale);
                  int h = Math.round((float)boxH * scale);
                  int ox = KillstreakConfig.perkOffsetX;
                  int oy = KillstreakConfig.perkOffsetY;
                  HudPosition pos = KillstreakConfig.perkPosition;
                  int left;
                  int top;
                  switch (pos) {
                     case TOP_LEFT:
                        left = ox;
                        top = oy;
                        break;
                     case TOP_RIGHT:
                        left = screenWidth - w - ox;
                        top = oy;
                        break;
                     case BOTTOM_LEFT:
                        left = ox;
                        top = screenHeight - h - oy;
                        break;
                     default:
                        left = screenWidth - w - ox;
                        top = screenHeight - h - oy;
                  }

                  PoseStack pose = graphics.pose();
                  pose.pushPose();

                  try {
                     pose.translate((float)left, (float)top, 0.0F);
                     pose.scale(scale, scale, 1.0F);
                     if (KillstreakConfig.perkFrame) {
                        HudPanel.drawBorder(
                           graphics,
                           0,
                           0,
                           boxW,
                           boxH,
                           KillstreakConfig.perkStyle.borderColor,
                           (double)((float)KillstreakConfig.perkStyle.borderOpacity * overall * op),
                           KillstreakConfig.perkStyle.borderStyle,
                           KillstreakConfig.perkStyle.borderLength,
                           KillstreakConfig.perkStyle.borderOutset
                        );
                     }

                     for (int i = 0; i < n; i++) {
                        float a = vis[i];
                        if (!(a < 0.02F)) {
                           int rowY = 10 + i * (rowH + 4);
                           pose.pushPose();

                           try {
                              pose.translate(0.0F, (float)rowY, 0.0F);
                              drawRowBox(graphics, boxW, rowH, contentW, a * op);
                              drawRow(graphics, font, i, names[i], boxW, rowH, icon, a * op);
                           } finally {
                              pose.popPose();
                           }
                        }
                     }
                  } finally {
                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                     pose.popPose();
                  }
               }
            }
         }
      }
   }

   private static void drawRowBox(GuiGraphics g, int boxW, int rowH, int contentW, float a) {
      ElementStyle st = KillstreakConfig.perkStyle;
      if (st.panelEnabled && !(a <= 0.01F)) {
         int bx = boxW - 10 - contentW - 6;
         int bw = contentW + 12;
         float strength = (float)st.panelOpacity / 100.0F;
         SoftDraw.soft(g, (float)bx, 0.0F, (float)bw, (float)rowH, st.panelColor, 0.55F * strength * a, 4.0F, false);
         if (KillstreakConfig.perkGlow) {
            GlowEffect.drawGlowRect(g, bx, 0, bw, rowH, 16777215, 0.22F * a, 3.0F);
         }

         HudPanel.draw(g, bx, 0, bw, rowH, st.panelColor, (double)((float)st.panelOpacity * a));
      }
   }

   private static void drawRow(GuiGraphics g, Font font, int idx, String name, int boxW, int rowH, int icon, float a) {
      int right = boxW - 10;
      int iconX = right - icon;
      int iconY = (rowH - icon) / 2;
      int nameW = font.width(name);
      int textX = iconX - 6 - nameW;
      int textY = (rowH - 9) / 2;
      if (KillstreakConfig.perkHighlight) {
         float hx0 = (float)textX - 14.0F;
         float hx1 = (float)iconX - 3.0F;
         float hy0 = (float)textY - 1.5F;
         float hy1 = (float)(textY + 9) + 0.5F;
         SoftDraw.gradientH(g, hx0, hy0, hx1, hy1, argb(16777215, 0.0F), argb(16777215, 0.13F * a));
         SoftDraw.rect(g, hx0 + 10.0F, hy1 - 0.6F, hx1, hy1, argb(16777215, 0.1F * a));
      }

      if ((int)(a * 255.0F) >= 16) {
         HudText.text(g, font, name, (float)textX, (float)textY, argb(16777215, a), true);
      }

      if (KillstreakConfig.perkGlow) {
         GlowEffect.drawBloom(g, (float)iconX + (float)icon / 2.0F, (float)iconY + (float)icon / 2.0F, (float)icon * 1.0F, 16777215, 0.4F * a);
      }

      ResourceLocation texture = ModernLoadoutCompat.perkTexture(idx);
      if (texture != null) {
         float[] prev = RenderSystem.getShaderColor();
         float pr = prev[0];
         float pg = prev[1];
         float pb = prev[2];
         float pa = prev[3];

         try {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, Math.min(1.0F, a));
            g.blit(texture, iconX, iconY, icon, icon, 0.0F, 0.0F, 24, 24, 24, 24);
         } finally {
            RenderSystem.setShaderColor(pr, pg, pb, pa);
         }
      } else {
         SoftDraw.rect(
            g,
            (float)iconX + (float)icon * 0.3F,
            (float)iconY + (float)icon * 0.3F,
            (float)iconX + (float)icon * 0.7F,
            (float)iconY + (float)icon * 0.7F,
            argb(16777215, 0.8F * a)
         );
      }
   }

   private static int argb(int rgb, float alpha) {
      int a = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
      return a << 24 | rgb & 16777215;
   }
}
