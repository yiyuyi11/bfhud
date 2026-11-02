package cc.xypp.modernhud.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

public final class PixelDraw {
   private static int pxDepth;

   private PixelDraw() {
   }

   public static float physPerUnit(GuiGraphics g) {
      return Math.max(0.01F, g.pose().last().pose().m00() * gui());
   }

   private static float gui() {
      return (float)Minecraft.getInstance().getWindow().getGuiScale();
   }

   public static void fillPx(GuiGraphics g, int x0, int y0, int x1, int y1, int argb) {
      if (x1 > x0 && y1 > y0 && argb >>> 24 != 0) {
         if (pxDepth > 0) {
            g.fill(x0, y0, x1, y1, argb);
         } else {
            PoseStack ps = g.pose();
            ps.pushPose();
            setPixelPose(ps);
            g.fill(x0, y0, x1, y1, argb);
            ps.popPose();
         }
      }
   }

   private static void setPixelPose(PoseStack ps) {
      float gs = gui();
      Matrix4f pm = ps.last().pose();
      float z = pm.m32();
      pm.identity();
      pm.translate(0.0F, 0.0F, z);
      pm.scale(1.0F / gs, 1.0F / gs, 1.0F);
   }

   private static void scoped(GuiGraphics g, Runnable body) {
      HudBatch.run(g, () -> {
         PoseStack ps = g.pose();
         ps.pushPose();
         setPixelPose(ps);
         pxDepth++;

         try {
            body.run();
         } finally {
            pxDepth--;
            ps.popPose();
         }
      });
   }

   public static void fillUnits(GuiGraphics g, float x, float y, float w, float h, int argb) {
      Matrix4f m = g.pose().last().pose();
      float gs = gui();
      int x0 = Math.round((m.m00() * x + m.m30()) * gs);
      int y0 = Math.round((m.m11() * y + m.m31()) * gs);
      int x1 = Math.max(x0 + 1, Math.round((m.m00() * (x + w) + m.m30()) * gs));
      int y1 = Math.max(y0 + 1, Math.round((m.m11() * (y + h) + m.m31()) * gs));
      fillPx(g, x0, y0, x1, y1, argb);
   }

   public static boolean isFireIcon(String mode) {
      return "AUTO".equals(mode) || "SEMI".equals(mode) || "BURST".equals(mode);
   }

   public static void fireIcon(GuiGraphics g, String mode, float x, float y, float size, int argb) {
      fireIcon(g, mode, x, y, size, size, argb);
   }

   public static void fireIcon(GuiGraphics g, String mode, float x, float y, float w, float hgt, int argb) {
      if (argb >>> 24 != 0) {
         Matrix4f m = g.pose().last().pose();
         float gs = gui();
         int nw = Math.max(5, Math.round(m.m00() * w * gs));
         int nh = Math.max(5, Math.round(m.m00() * hgt * gs));
         int bx = Math.round((m.m00() * x + m.m30()) * gs);
         int by = Math.round((m.m11() * y + m.m31()) * gs);
         boolean reserve = "RESERVE".equals(mode);
         int rows = reserve ? 4 : ("AUTO".equals(mode) ? 3 : ("BURST".equals(mode) ? 3 : 1));
         int h = Math.max(2, Math.round((float)nh * (reserve ? 0.17F : 0.2F)));
         int gap = Math.max(1, Math.round((float)nh * (reserve ? 0.08F : 0.1F)));

         while (rows > 1 && rows * h + (rows - 1) * gap > nh) {
            if (gap > 1) {
               gap--;
            } else {
               if (h <= 2) {
                  break;
               }

               h--;
            }
         }

         int[] fit = fitParity(rows, h, gap, nh);
         h = fit[0];
         gap = fit[1];
         int total = rows * h + (rows - 1) * gap;
         int top = by + (nh - total) / 2;
         final int fh = h;
         final int fgap = gap;
         boolean burst = "BURST".equals(mode);
         int bw = burst ? Math.max(5, Math.round((float)nw * 0.72F)) : nw;
         scoped(g, () -> {
            for (int i = 0; i < rows; i++) {
               int ry = top + i * (fh + fgap);
               if (reserve) {
                  roundBullet(g, bx, ry, nw, fh, argb);
               } else if (burst) {
                  int off = rows == 1 ? 0 : Math.round((float)((nw - bw) * i) / (float)(rows - 1));
                  fireBullet(g, bx + off, ry, bw, fh, argb);
               } else {
                  fireBullet(g, bx, ry, nw, fh, argb);
               }
            }
         });
      }
   }

   public static void fireCellIcon(GuiGraphics g, String mode, float x, float y, float w, float hgt, int argb) {
      if (argb >>> 24 != 0) {
         Matrix4f m = g.pose().last().pose();
         float gs = gui();
         int cx = Math.round((m.m00() * x + m.m30()) * gs);
         int cy = Math.round((m.m11() * y + m.m31()) * gs);
         int cw = Math.round(m.m00() * w * gs);
         int ch = Math.round(m.m00() * hgt * gs);
         boolean semi = "SEMI".equals(mode);
         boolean burst = "BURST".equals(mode);
         int rows = semi ? 1 : 3;
         int margin = Math.max(2, Math.round((float)cw * (1.0F - (semi ? 0.47F : 0.38F)) / 2.0F));
         int iw = Math.max(8, cw - 2 * margin);
         int t0;
         int gap0;
         if (semi) {
            t0 = Math.max(3, Math.round((float)ch * 0.21F));
            gap0 = 0;
         } else {
            int ih = Math.max(5, even((float)ch * 0.49F));
            t0 = Math.max(2, (int)Math.floor((double)ih / ((double)rows + (double)(rows - 1) * 0.5)));
            gap0 = Math.max(1, t0 / 2);
         }

         int[] fit = fitParity(rows, t0, gap0, ch);
         int t = fit[0];
         int gap = fit[1];
         int total = rows * t + (rows - 1) * gap;
         int ix = cx + margin;
         int top = cy + (ch - total) / 2;
         int bw = burst ? Math.max(6, Math.round((float)iw * 0.72F)) : iw;
         scoped(g, () -> {
            for (int i = 0; i < rows; i++) {
               int off = burst ? Math.round((float)((iw - bw) * i) / (float)(rows - 1)) : 0;
               fireBullet(g, ix + off, top + i * (t + gap), burst ? bw : iw, t, argb);
            }
         });
      }
   }

   private static int even(float v) {
      int r = Math.round(v);
      return (r & 1) == 0 ? r : r + 1;
   }

   private static int taper(int h, int k, int len) {
      return Math.max(1, Math.round((float)h * (1.0F - ((float)k + 0.5F) / (float)len)));
   }

   private static void bulletRow(GuiGraphics g, int x, int y, int w, int h, int argb) {
      int minTh = (h & 1) == 1 ? 1 : 2;
      int gap = Math.max(1, Math.round((float)w * 0.07F));
      if (h <= minTh) {
         int piece = Math.max(2, Math.round((float)w * 0.2F));
         fillPx(g, x, y, x + piece, y + h, argb);
         fillPx(g, x + piece + gap, y, x + w, y + h, argb);
      } else {
         int cols = (h - minTh) / 2 + 1;
         int full = Math.max(1, Math.round((float)w * 0.06F));

         while (cols + full + gap + 2 > w && full > 0) {
            full--;
         }

         for (int k = 0; k < cols; k++) {
            int th = minTh + 2 * k;
            int yy = y + (h - th) / 2;
            fillPx(g, x + k, yy, x + k + 1, yy + th, argb);
         }

         for (int k = cols; k < cols + full; k++) {
            fillPx(g, x + k, y, x + k + 1, y + h, argb);
         }

         fillPx(g, x + cols + full + gap, y, x + w, y + h, argb);
      }
   }

   private static void fireBullet(GuiGraphics g, int x, int y, int w, int h, int argb) {
      bulletRow(g, x, y, w, h, argb);
   }

   private static void roundBullet(GuiGraphics g, int x, int y, int w, int h, int argb) {
      bulletRow(g, x, y, w, h, argb);
   }

   private static int[] fitParity(int rows, int t, int gap, int avail) {
      int total = rows * t + (rows - 1) * gap;
      if ((avail - total & 1) == 0) {
         return new int[]{t, gap};
      } else if ((rows & 1) == 1) {
         int nt = total + rows <= avail ? t + 1 : t - 1;
         return new int[]{Math.max(1, nt), gap};
      } else {
         int ng = total + (rows - 1) <= avail ? gap + 1 : gap - 1;
         return new int[]{t, Math.max(1, ng)};
      }
   }

   private static int[] frame(GuiGraphics g, float cx, float cy, float r) {
      Matrix4f m = g.pose().last().pose();
      float gs = gui();
      return new int[]{Math.round((m.m00() * cx + m.m30()) * gs), Math.round((m.m11() * cy + m.m31()) * gs), Math.max(2, Math.round(r * m.m00() * gs))};
   }

   private static int line(GuiGraphics g) {
      return Math.max(1, Math.round(0.6F * physPerUnit(g)));
   }

   public static void diamond(GuiGraphics g, float cx, float cy, float r, boolean filled, int argb) {
      int[] f = frame(g, cx, cy, r);
      int t = line(g);
      scoped(g, () -> {
         for (int dy = -f[2]; dy <= f[2]; dy++) {
            int half = f[2] - Math.abs(dy);
            if (!filled && half >= t) {
               fillPx(g, f[0] - half, f[1] + dy, f[0] - half + t, f[1] + dy + 1, argb);
               fillPx(g, f[0] + half + 1 - t, f[1] + dy, f[0] + half + 1, f[1] + dy + 1, argb);
            } else {
               fillPx(g, f[0] - half, f[1] + dy, f[0] + half + 1, f[1] + dy + 1, argb);
            }
         }
      });
   }

   public static void square(GuiGraphics g, float cx, float cy, float r, boolean filled, int argb) {
      int[] f = frame(g, cx, cy, r);
      int rp = Math.max(2, f[2] - 1);
      int t = line(g);
      int x0 = f[0] - rp;
      int y0 = f[1] - rp;
      int x1 = f[0] + rp + 1;
      int y1 = f[1] + rp + 1;
      scoped(g, () -> {
         if (filled) {
            fillPx(g, x0, y0, x1, y1, argb);
         } else {
            fillPx(g, x0, y0, x1, y0 + t, argb);
            fillPx(g, x0, y1 - t, x1, y1, argb);
            fillPx(g, x0, y0 + t, x0 + t, y1 - t, argb);
            fillPx(g, x1 - t, y0 + t, x1, y1 - t, argb);
         }
      });
   }

   public static void triangleDown(GuiGraphics g, float cx, float cy, float r, int argb) {
      int[] f = frame(g, cx, cy, r);
      int h = f[2] * 2 + 1;
      scoped(g, () -> {
         for (int i = 0; i < h; i++) {
            int half = Math.round((float)f[2] * (1.0F - (float)i / (float)h));
            fillPx(g, f[0] - half, f[1] - f[2] + i, f[0] + half + 1, f[1] - f[2] + i + 1, argb);
         }
      });
   }

   public static void arrowHead(GuiGraphics g, float cx, float cy, float r, boolean left, int argb) {
      int[] f = frame(g, cx, cy, r);
      int w = f[2] + 1;
      scoped(g, () -> {
         for (int i = 0; i < w; i++) {
            int half = Math.round((float)f[2] * (1.0F - (float)i / (float)w));
            int x = left ? f[0] - i : f[0] + i;
            fillPx(g, x, f[1] - half, x + 1, f[1] + half + 1, argb);
         }
      });
   }
}
