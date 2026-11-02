package cc.xypp.modernhud.client;

import cc.xypp.modernhud.client.compat.LrTacticalCompat;
import cc.xypp.modernhud.config.ElementStyle;
import cc.xypp.modernhud.config.PanelBorderStyle;
import cc.xypp.modernhud.config.WeaponConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

final class LoadoutGear {
   static final int GAP = 2;
   private static final int COLUMN_W = 66;
   private static final int V_GAP = 2;
   private static final int H_GAP = 2;
   private static final int BOTTOM = 50;
   private static final int SQUARE = 32;
   private static final int MELEE_H = 16;
   private static final int TL = 1;
   private static final int TR = 2;
   private static final int BL = 4;
   private static final int BR = 8;
   private static final int SCAN_MS = 200;
   private static final int NORMAL = 15265008;
   private static final LoadoutGear.Tracked[] SLOTS = new LoadoutGear.Tracked[]{new LoadoutGear.Tracked(), new LoadoutGear.Tracked()};
   private static ItemStack melee = ItemStack.EMPTY;
   private static ItemStack meleeShown = ItemStack.EMPTY;
   private static final EasedAnimator[] SLOT_VIS = new EasedAnimator[]{
      new EasedAnimator(220.0F, EasedAnimator.Easing.CUBIC_IN_OUT),
      new EasedAnimator(220.0F, EasedAnimator.Easing.CUBIC_IN_OUT),
      new EasedAnimator(220.0F, EasedAnimator.Easing.CUBIC_IN_OUT)
   };
   private static long lastScan = -1000L;
   private static long lastDraw = -1000L;
   private static final EasedAnimator WIDTH = new EasedAnimator(220.0F, EasedAnimator.Easing.CUBIC_IN_OUT);
   private static long lastLayout = -1000L;
   private static final EasedAnimator VIS = new EasedAnimator(220.0F, EasedAnimator.Easing.CUBIC_IN_OUT);

   private LoadoutGear() {
   }

   static boolean enabled() {
      // Disabled: this block rescans the inventory and shows "the last melee/shield found", which reads as a
      // duplicate slot next to the real hotbar indices. The hotbar widgets cover slots 3-6 now.
      if (true) return false;
      return WeaponConfig.showGear && LrTacticalCompat.loaded();
   }

   static int extraWidth() {
      if (!enabled()) {
         return 0;
      } else if (!WeaponConfig.gearAutoLayout) {
         return 68;
      } else {
         float w = WIDTH.update();
         return w < 0.5F ? 0 : Math.round(w);
      }
   }

   private static int wantedLower(boolean idle) {
      int n = 0;

      for (LoadoutGear.Tracked t : SLOTS) {
         if (isWanted(t, idle)) {
            n++;
         }
      }

      return n;
   }

   private static boolean isWanted(LoadoutGear.Tracked t, boolean idle) {
      return t.key != null && (!idle || t.count > 0);
   }

   private static int contentWidth(boolean idle) {
      if (!melee.isEmpty()) {
         return 66;
      } else {
         int n = wantedLower(idle);
         return n == 0 ? 0 : (n == 1 ? 32 : 66);
      }
   }

   static void update(LocalPlayer player, boolean idle) {
      scan(player);
      long now = Util.getMillis();
      int content = contentWidth(idle);
      float target = content == 0 ? 0.0F : (float)(2 + content);
      if (now - lastLayout > 300L) {
         WIDTH.snapTo(target);
      }

      lastLayout = now;
      WIDTH.setDurationMs((float)WeaponConfig.gearStyle.fadeInMs);
      WIDTH.setTarget(target);
   }

   private static void scan(LocalPlayer player) {
      long now = Util.getMillis();
      if (now - lastScan >= 200L) {
         lastScan = now;
         Inventory inv = player.getInventory();
         int limit = WeaponConfig.gearCountWholeInventory ? inv.items.size() : 9;
         ItemStack meleeFound = ItemStack.EMPTY;
         ItemStack shieldFound = ItemStack.EMPTY;
         Map<String, ItemStack> found = new LinkedHashMap<>();
         Map<String, Integer> counts = new HashMap<>();

         for (int i = 0; i < limit; i++) {
            ItemStack s = inv.getItem(i);
            LrTacticalCompat.Kind kind = LrTacticalCompat.kind(s);
            if (kind != LrTacticalCompat.Kind.NONE) {
               if (kind == LrTacticalCompat.Kind.MELEE) {
                  if (i < 9) {
                     if (LrTacticalCompat.isShield(s)) {
                        if (shieldFound.isEmpty()) {
                           shieldFound = s;
                        }
                     } else if (meleeFound.isEmpty()) {
                        meleeFound = s;
                     }
                  }
               } else {
                  String key = LrTacticalCompat.key(s);
                  counts.merge(key, s.getCount(), Integer::sum);
                  if (i < 9) {
                     found.putIfAbsent(key, s);
                  }
               }
            }
         }

         ItemStack chosen = meleeFound.isEmpty() ? shieldFound : meleeFound;
         melee = chosen.isEmpty() ? ItemStack.EMPTY : chosen.copy();

         for (LoadoutGear.Tracked t : SLOTS) {
            if (t.key != null) {
               ItemStack f = found.remove(t.key);
               if (f != null) {
                  t.icon = f.copy();
               }

               t.count = counts.getOrDefault(t.key, 0);
            }
         }

         for (Entry<String, ItemStack> e : found.entrySet()) {
            LoadoutGear.Tracked target = null;

            for (LoadoutGear.Tracked tx : SLOTS) {
               if (tx.key == null) {
                  target = tx;
                  break;
               }
            }

            if (target == null) {
               for (LoadoutGear.Tracked txx : SLOTS) {
                  if (txx.count <= 0) {
                     target = txx;
                     break;
                  }
               }
            }

            if (target == null) {
               break;
            }

            target.key = e.getKey();
            target.icon = e.getValue().copy();
            target.count = counts.getOrDefault(e.getKey(), e.getValue().getCount());
         }
      }
   }

   static boolean isGearItem(ItemStack stack) {
      return enabled() && LrTacticalCompat.kind(stack) != LrTacticalCompat.Kind.NONE;
   }

   static void draw(GuiGraphics g, int x0, boolean idle) {
      ElementStyle st = WeaponConfig.gearStyle;
      long now = Util.getMillis();
      if (now - lastDraw > 300L) {
         VIS.snapTo(0.0F);
      }

      lastDraw = now;
      VIS.setDurationMs((float)st.fadeInMs);
      VIS.setTarget(1.0F);
      float vis = VIS.update();
      float a = st.alpha(vis);
      if (!(a <= 0.01F)) {
         Font font = Minecraft.getInstance().font;
         int secH = 16;
         int meleeY = idle ? (32 - secH) / 2 : 0;
         int meleeH = idle ? secH : 16;
         int lowerY = idle ? meleeY + secH + 2 : 18;
         int lowerH = idle ? secH : 32;
         int w0 = 32;
         int w1 = 64 - w0;
         if (!melee.isEmpty()) {
            meleeShown = melee;
         }

         EasedAnimator mv = SLOT_VIS[0];
         mv.setDurationMs(melee.isEmpty() ? (float)st.fadeOutMs : (float)st.fadeInMs);
         mv.setTarget(melee.isEmpty() ? 0.0F : 1.0F);
         float meleeVis = mv.update();
         if (meleeVis > 0.01F && !meleeShown.isEmpty()) {
            float av = vis * meleeVis;
            float aa = st.alpha(av);
            box(g, x0, meleeY, 66, meleeH, 10, st, av);
            int iconPx = 14;
            float ix = (float)(x0 + 66 - iconPx) - 4.0F;
            float iy = (float)meleeY + (float)(meleeH - iconPx) / 2.0F;
            if (WeaponConfig.gearGlow) {
               GlowEffect.drawBloom(g, ix + (float)iconPx / 2.0F, iy + (float)iconPx / 2.0F, 12.0F, 15265008, 0.24F * aa);
            }

            drawItem(g, meleeShown, ix, iy, (float)iconPx / 16.0F, aa);
         }

         int packed = 0;

         for (int i = 0; i < SLOTS.length; i++) {
            LoadoutGear.Tracked t = SLOTS[i];
            boolean spent = t.count <= 0;
            boolean present = t.key != null && (!idle || !spent);
            int pos = WeaponConfig.gearAutoLayout ? packed : i;
            if (present) {
               packed++;
            }

            EasedAnimator sv = SLOT_VIS[i + 1];
            sv.setDurationMs(present ? (float)st.fadeInMs : (float)st.fadeOutMs);
            sv.setTarget(present ? 1.0F : 0.0F);
            float slotVis = sv.update();
            if (t.key != null && !(slotVis <= 0.01F)) {
               float av = vis * slotVis;
               float aa = st.alpha(av);
               int w = pos == 0 ? w0 : w1;
               int x = x0 + (pos == 0 ? 0 : w0 + 2);
               int top = pos == 0 != WeaponConfig.gearTopDotInner ? 1 : 2;
               box(g, x, lowerY, w, lowerH, 12 | top, st, av);
               float scale = idle ? 0.75F : 1.0F;
               int iconPx = Math.round(16.0F * scale);
               float ix;
               float iy;
               if (idle) {
                  ix = (float)x + 2.0F;
                  iy = (float)lowerY + (float)(lowerH - iconPx) / 2.0F;
               } else {
                  ix = (float)(x + w - iconPx) - 3.0F;
                  iy = (float)lowerY + 3.0F;
               }

               if (WeaponConfig.gearGlow && !spent) {
                  GlowEffect.drawBloom(g, ix + (float)iconPx / 2.0F, iy + (float)iconPx / 2.0F, 12.0F, 15265008, 0.24F * aa);
               }

               drawItem(g, t.icon, ix, iy, scale, spent ? aa * 0.28F : aa);
               int shown = Math.min(99, Math.max(0, t.count));
               String text = shown < 10 ? "0" + shown : Integer.toString(shown);
               int ta = Math.max(1, Math.round(Math.min(1.0F, aa) * 255.0F));
               int color = ta << 24 | WeaponConfig.reserveColor & 16777215;
               int tx;
               int ty;
               if (idle) {
                  tx = x + 2 + iconPx + 2;
                  ty = lowerY + (lowerH - 9) / 2;
               } else {
                  tx = x + 4;
                  ty = lowerY + lowerH - 9 - 2;
               }

               HudText.text(g, font, text, (float)tx, (float)ty, color, true);
            }
         }
      }
   }

   private static void box(GuiGraphics g, int x, int y, int w, int h, int corners, ElementStyle st, float vis) {
      float a = st.alpha(vis);
      if (st.panelEnabled) {
         HudPanel.draw(g, x, y, w, h, st.panelColor, (double)((float)st.panelOpacity * a));
      }

      PanelBorderStyle bs = st.borderStyle;
      if (bs != PanelBorderStyle.NONE) {
         if (bs != PanelBorderStyle.DOTS && bs != PanelBorderStyle.BOTTOM_DOTS && bs != PanelBorderStyle.FRAME) {
            HudPanel.drawBorder(g, x, y, w, h, st.borderColor, (double)((float)st.borderOpacity * a), bs, st.borderLength, st.borderOutset);
         } else {
            if (bs == PanelBorderStyle.FRAME) {
               HudPanel.drawBorder(g, x, y, w, h, st.borderColor, (double)((float)st.borderOpacity * a) * 0.35, PanelBorderStyle.FULL, 1, 0);
            }

            int size = Math.min(Math.max(1, st.borderLength), Math.max(1, Math.min(w, h) / 3));
            int alpha = (int)Math.round(Math.min(100.0, (double)((float)st.borderOpacity * a)) * 2.55);
            if (alpha <= 0) {
               return;
            }

            int c = alpha << 24 | st.borderColor & 16777215;
            if ((corners & 1) != 0) {
               g.fill(x, y, x + size, y + size, c);
            }

            if ((corners & 2) != 0) {
               g.fill(x + w - size, y, x + w, y + size, c);
            }

            if ((corners & 4) != 0) {
               g.fill(x, y + h - size, x + size, y + h, c);
            }

            if ((corners & 8) != 0) {
               g.fill(x + w - size, y + h - size, x + w, y + h, c);
            }
         }
      }
   }

   private static void drawItem(GuiGraphics g, ItemStack stack, float x, float y, float scale, float alpha) {
      float[] cur = RenderSystem.getShaderColor();
      float[] prev = new float[]{cur[0], cur[1], cur[2], cur[3]};
      PoseStack ps = g.pose();
      ps.pushPose();

      try {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, Math.max(0.0F, Math.min(1.0F, alpha)));
         ps.translate(x, y, 0.0F);
         ps.scale(scale, scale, 1.0F);
         g.renderItem(stack, 0, 0);
      } finally {
         RenderSystem.setShaderColor(prev[0], prev[1], prev[2], prev[3]);
         ps.popPose();
      }
   }

   static {
      VIS.snapTo(0.0F);
      WIDTH.snapTo(0.0F);

      for (EasedAnimator a : SLOT_VIS) {
         a.snapTo(0.0F);
      }
   }

   private static final class Tracked {
      String key;
      ItemStack icon = ItemStack.EMPTY;
      int count;
   }
}
