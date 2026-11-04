package cc.xypp.modernhud.client;

import cc.xypp.modernhud.config.BossBackdrop;
import cc.xypp.modernhud.config.BossBarConfig;
import cc.xypp.modernhud.config.BossNameMode;
import cc.xypp.modernhud.config.BossNumberLayout;
import cc.xypp.modernhud.config.BossNumberMode;
import cc.xypp.modernhud.config.ElementStyle;
import cc.xypp.modernhud.config.HudConfig;
import cc.xypp.modernhud.config.HudPosition;
import cc.xypp.modernhud.config.PanelBorderStyle;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent.BossEventProgress;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

public class BossBarOverlay implements IGuiOverlay {
   private static final Map<UUID, LerpingBossEvent> SEEN = new LinkedHashMap<>();
   private static final Map<UUID, Long> SEEN_AT = new HashMap<>();
   private static final long GRACE_MS = 700L;
   private static final int NUMBER_GAP = 5;
   private static final int CAPTION_GAP = 2;
   private static final long RESOLVE_INTERVAL_MS = 1000L;
   private final Map<UUID, BossBarOverlay.Bar> bars = new LinkedHashMap<>();
   private final Map<UUID, BossBarOverlay.Match> matches = new HashMap<>();
   private final EasedAnimator blockHeight = new EasedAnimator(380.0F, EasedAnimator.Easing.CUBIC_IN_OUT);
   private boolean blockHeightSeeded;
   private float lastBlockW = 80.0F;

   private static long monoMs() {
      return System.nanoTime() / 1000000L;
   }

   public void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
      if (!BossBarConfig.enabled) {
         this.bars.clear();
         SEEN.clear();
         SEEN_AT.clear();
      } else {
         Minecraft mc = Minecraft.getInstance();
         Font font = mc.font;
         long now = System.currentTimeMillis();
         boolean animate = BossBarConfig.animations;
         float ms = (float)BossBarConfig.animationMs;

         for (BossBarOverlay.Bar b : this.bars.values()) {
            b.present = false;
         }

         int taken = 0;
         long mono = monoMs();
         Iterator<Entry<UUID, LerpingBossEvent>> seenIt = SEEN.entrySet().iterator();

         while (seenIt.hasNext()) {
            Entry<UUID, LerpingBossEvent> entry = seenIt.next();
            Long at = SEEN_AT.get(entry.getKey());
            if (at == null || mono - at > 700L) {
               seenIt.remove();
               SEEN_AT.remove(entry.getKey());
            } else if (taken < BossBarConfig.maxBars) {
               taken++;
               LerpingBossEvent ev = entry.getValue();
               BossBarOverlay.Bar b = this.bars.get(ev.getId());
               if (b == null) {
                  b = new BossBarOverlay.Bar();
                  b.id = ev.getId();
                  b.vis = new EasedAnimator(ms, EasedAnimator.Easing.CUBIC_IN_OUT);
                  b.fill = new EasedAnimator(ms * 1.2F, EasedAnimator.Easing.CUBIC_IN_OUT);
                  b.y = new EasedAnimator(ms, EasedAnimator.Easing.CUBIC_IN_OUT);
                  b.vis.snapTo(animate ? 0.0F : 1.0F);
                  float first = Math.max(0.0F, Math.min(1.0F, ev.getProgress()));
                  b.fill.snapTo(animate ? 0.0F : first);
                  b.trail = new EasedAnimator(ms * 2.0F, EasedAnimator.Easing.CUBIC_OUT);
                  b.trail.snapTo(animate ? 0.0F : first);
                  b.lastProgress = first;
                  this.bars.put(b.id, b);
               }

               b.present = true;
               b.progress = Math.max(0.0F, Math.min(1.0F, ev.getProgress()));
               if (b.progress < b.lastProgress - 0.002F) {
                  b.trailHoldUntil = now + 320L;
               }

               b.lastProgress = b.progress;
               b.name = ev.getName().getString();
               b.color = BossBarConfig.useBossColor ? colorOf(ev.getColor().name()) : 15921906;
               b.notches = notchCount(ev.getOverlay().name());
               b.number = this.numberText(ev, b.progress, mc, now);
            }
         }

         if (!this.bars.isEmpty()) {
            float elemScale = HudText.snapElement(graphics, BossBarConfig.scale);
            float numScale = HudText.snapScale(graphics, BossBarConfig.numberScale, elemScale);
            float nameScale = HudText.snapScale(graphics, BossBarConfig.nameScale, elemScale);
            int barW = BossBarConfig.barWidth;
            int barH = BossBarConfig.barHeight;
            int numH = (int)Math.ceil((double)(9.0F * numScale));
            int nameH = (int)Math.ceil((double)(9.0F * nameScale));
            List<BossBarOverlay.Bar> active = new ArrayList<>();

            for (BossBarOverlay.Bar bx : this.bars.values()) {
               if (bx.present) {
                  active.add(bx);
               }
            }

            int m = active.size();
            BossNumberLayout layout = BossBarConfig.numberLayout;
            if (layout == BossNumberLayout.ABOVE_BELOW) {
               layout = m == 1 ? BossNumberLayout.ABOVE : (m > 2 ? BossNumberLayout.SIDE : layout);
            }

            boolean anySide = false;
            float widest = (float)font.width("000") * numScale;

            for (int i = 0; i < m; i++) {
               BossBarOverlay.Bar bxx = active.get(i);
               computeLayout(bxx, i, m, layout, numH, nameH, barH);
               anySide |= bxx.numSide;
               widest = Math.max(widest, (float)font.width(bxx.number) * numScale);
            }

            int barX = anySide ? (int)Math.ceil((double)widest) + 5 : 0;
            float blockW = (float)(barX + barW + 1);
            this.lastBlockW = blockW;
            int spacing = BossBarConfig.barSpacing;
            int cursor = 0;

            for (BossBarOverlay.Bar bxx : active) {
               setTarget(bxx.y, (float)cursor, animate);
               bxx.yPlaced = true;
               cursor += bxx.entryH + spacing;
            }

            float totalH = (float)Math.max(0, cursor - spacing);
            if (m > 0) {
               if (!this.blockHeightSeeded) {
                  this.blockHeight.snapTo(totalH);
                  this.blockHeightSeeded = true;
               }

               setTarget(this.blockHeight, totalH, animate);
            }

            for (BossBarOverlay.Bar bxx : this.bars.values()) {
               bxx.vis.setDurationMs(bxx.present ? (float)BossBarConfig.style.fadeInMs : (float)BossBarConfig.style.fadeOutMs);
               setTarget(bxx.vis, bxx.present ? 1.0F : 0.0F, animate);
               if (bxx.present && Math.abs(bxx.fill.getTargetValue() - bxx.progress) > 5.0E-4F) {
                  setTarget(bxx.fill, bxx.progress, animate);
               }
            }

            float scale = elemScale;
            float blockH = this.blockHeight.update();
            int w = Math.round(blockW * elemScale);
            int h = Math.round(blockH * elemScale);
            int ox = BossBarConfig.offsetX;
            int oy = BossBarConfig.offsetY;
            int left;
            int top;
            switch (BossBarConfig.position) {
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

            boolean rightSide = BossBarConfig.position == HudPosition.TOP_RIGHT || BossBarConfig.position == HudPosition.BOTTOM_RIGHT;
            PoseStack pose = graphics.pose();
            pose.pushPose();

            try {
               pose.translate((float)left, (float)top, 0.0F);
               pose.scale(scale, scale, 1.0F);
               float slide = blockW * 0.55F + 20.0F;
               Iterator<BossBarOverlay.Bar> it = this.bars.values().iterator();

               while (it.hasNext()) {
                  BossBarOverlay.Bar bxxx = it.next();
                  float vis = bxxx.vis.update();
                  float fillAnim = bxxx.fill.update();
                  float trailAnim = fillAnim;
                  if (BossBarConfig.damageTrail && animate) {
                     if (fillAnim >= bxxx.trail.getCurrentValue()) {
                        bxxx.trail.snapTo(fillAnim);
                     } else if (now >= bxxx.trailHoldUntil) {
                        bxxx.trail.setTarget(fillAnim);
                     }

                     trailAnim = Math.max(fillAnim, bxxx.trail.update());
                  }

                  float yy = bxxx.y.update();
                  if (!bxxx.present && vis < 0.015F) {
                     it.remove();
                  } else if (!(vis < 0.02F)) {
                     float dx = animate ? (1.0F - vis) * slide * (rightSide ? 1.0F : -1.0F) : 0.0F;
                     pose.pushPose();

                     try {
                        pose.translate(dx, yy, 0.0F);
                        this.drawBar(graphics, font, bxxx, barX, barW, barH, blockW, numScale, nameScale, numH, nameH, vis, fillAnim, trailAnim, now);
                     } finally {
                        pose.popPose();
                     }
                  }
               }

               this.matches.keySet().retainAll(this.bars.keySet());
            } finally {
               pose.popPose();
            }
         }
      }
   }

   private static void setTarget(EasedAnimator a, float target, boolean animate) {
      if (animate) {
         a.setTarget(target);
      } else {
         a.snapTo(target);
      }
   }

   private static void computeLayout(BossBarOverlay.Bar b, int idx, int m, BossNumberLayout layout, int numH, int nameH, int barH) {
      boolean showNumber = BossBarConfig.showNumber;
      BossNameMode nameMode = BossBarConfig.nameMode;
      b.numTop = showNumber && (layout == BossNumberLayout.ABOVE || layout == BossNumberLayout.ABOVE_BELOW && idx == 0);
      b.numBottom = showNumber && (layout == BossNumberLayout.BELOW || layout == BossNumberLayout.ABOVE_BELOW && idx == m - 1 && m > 1);
      b.numSide = showNumber && !b.numTop && !b.numBottom;
      boolean named = nameMode != BossNameMode.HIDDEN;
      b.nameTop = named && (b.numTop || !b.numBottom && nameMode == BossNameMode.ABOVE);
      b.nameBottom = named && (b.numBottom || !b.numTop && nameMode == BossNameMode.BELOW);
      b.capTopH = Math.max(b.numTop ? numH : 0, b.nameTop ? nameH : 0);
      b.capBottomH = Math.max(b.numBottom ? numH : 0, b.nameBottom ? nameH : 0);
      b.mainH = Math.max(barH + 4, b.numSide ? numH : 0);
      b.entryH = (b.capTopH > 0 ? b.capTopH + 2 : 0) + b.mainH + (b.capBottomH > 0 ? b.capBottomH + 2 : 0);
   }

   private void drawBar(
      GuiGraphics g,
      Font font,
      BossBarOverlay.Bar b,
      int barX,
      int barW,
      int barH,
      float blockW,
      float numScale,
      float nameScale,
      int numH,
      int nameH,
      float vis,
      float fillAnim,
      float trailAnim,
      long now
   ) {
      float a = BossBarConfig.style.alpha(vis);
      boolean low = BossBarConfig.lowWarning && b.progress <= 0.2F && b.progress > 0.0F;
      float pulse = low ? 0.5F + 0.5F * (float)Math.sin((double)now / 320.0) : 0.0F;
      int color = low ? mix(b.color, 16726843, 0.4F + 0.45F * pulse) : b.color;
      this.drawBackdrop(g, blockW, b.entryH, a, vis);
      int capTopY = 0;
      int mainY = b.capTopH > 0 ? b.capTopH + 2 : 0;
      int capBottomY = mainY + b.mainH + 2;
      int barY = mainY + (b.mainH - barH) / 2;
      int trackEnd = barX + barW;
      int numArgb = argb(low ? color : 16777215, a);
      if (b.numTop) {
         drawText(g, font, b.number, 0, capTopY + (b.capTopH - numH) / 2, numScale, numArgb);
      }

      if (b.numBottom) {
         drawText(g, font, b.number, 0, capBottomY + (b.capBottomH - numH) / 2, numScale, numArgb);
      }

      if (b.numSide) {
         drawText(g, font, b.number, 0, mainY + (b.mainH - numH) / 2, numScale, numArgb);
      }

      if (b.nameTop) {
         this.drawName(g, font, b, trackEnd, capTopY + (b.capTopH - nameH) / 2, barW, numScale, nameScale, a, b.numTop);
      }

      if (b.nameBottom) {
         this.drawName(g, font, b, trackEnd, capBottomY + (b.capBottomH - nameH) / 2, barW, numScale, nameScale, a, b.numBottom);
      }

      if (BossBarConfig.showTrack) {
         float cy = (float)barY + (float)barH / 2.0F;
         SoftDraw.rect(g, (float)barX, cy - 0.5F, (float)trackEnd, cy + 0.5F, argb(16777215, 0.55F * a));
         SoftDraw.rect(g, (float)trackEnd, (float)barY - 2.0F, (float)trackEnd + 1.0F, (float)(barY + barH) + 2.0F, argb(16777215, 0.8F * a));
      }

      if (trailAnim > fillAnim + 0.002F) {
         float tx0 = (float)barX + (float)barW * Math.max(0.0F, Math.min(1.0F, fillAnim));
         float tx1 = (float)barX + (float)barW * Math.max(0.0F, Math.min(1.0F, trailAnim));
         SoftDraw.gradientV(
            g, tx0, (float)barY, tx1, (float)(barY + barH), argb(mix(color, 16777215, 0.75F), 0.62F * a), argb(mix(color, 16777215, 0.45F), 0.38F * a)
         );
      }

      float fillW = (float)barW * Math.max(0.0F, Math.min(1.0F, fillAnim));
      if (fillW > 0.4F) {
         float glow = (float)BossBarConfig.glowIntensity / 100.0F * ((float)HudConfig.glowIntensity / 100.0F);
         float x1 = (float)barX + fillW;
         if (BossBarConfig.barGlow && glow > 0.0F) {
            SoftDraw.soft(g, (float)barX, (float)barY, fillW, (float)barH, color, 0.32F * glow * a, 4.5F, true);
         }

         SoftDraw.gradientH(g, (float)barX, (float)barY, x1, (float)(barY + barH), argb(mix(color, 0, 0.42F), a), argb(color, a));
         SoftDraw.gradientV(g, (float)barX, (float)barY, x1, (float)barY + (float)barH * 0.55F, argb(16777215, 0.34F * a), argb(16777215, 0.0F));
         float headLen = Math.min(fillW, 5.0F);
         SoftDraw.gradientH(g, x1 - headLen, (float)barY, x1, (float)(barY + barH), argb(16777215, 0.0F), argb(16777215, 0.95F * a));
         if (BossBarConfig.headGlow && glow > 0.0F) {
            SoftDraw.soft(g, x1 - 2.0F, (float)barY - 0.5F, 2.0F, (float)barH + 1.0F, mix(color, 16777215, 0.55F), 0.85F * glow * a, 5.5F, true);
         }
      }

      for (int k = 1; k < b.notches; k++) {
         float nx = (float)barX + (float)barW * ((float)k / (float)b.notches);
         SoftDraw.rect(g, nx - 0.45F, (float)barY, nx + 0.45F, (float)(barY + barH), argb(0, 0.7F * a));
      }
   }

   private void drawBackdrop(GuiGraphics g, float blockW, int entryH, float a, float vis) {
      BossBackdrop mode = BossBarConfig.backdrop;
      ElementStyle st = BossBarConfig.style;
      float opacity = (float)BossBarConfig.backdropOpacity / 100.0F;
      float strength = (float)st.panelOpacity / 100.0F;
      int w = Math.round(blockW) + 8;
      int h = entryH + 6;
      if (mode == BossBackdrop.SHADOW && opacity > 0.0F) {
         SoftDraw.soft(g, -3.0F, -2.0F, blockW + 6.0F, (float)entryH + 4.0F, st.panelColor, strength * opacity * a, 7.0F, false);
      } else if (mode == BossBackdrop.BOX && opacity > 0.0F) {
         HudPanel.draw(g, -4, -3, w, h, st.panelColor, (double)((float)st.panelOpacity * opacity * a));
      }

      if (mode != BossBackdrop.NONE && st.borderStyle != PanelBorderStyle.NONE) {
         HudPanel.drawBorder(g, -4, -3, w, h, st.borderColor, (double)((float)st.borderOpacity * a), st.borderStyle, st.borderLength, st.borderOutset);
      }
   }

   private void drawName(
      GuiGraphics g, Font font, BossBarOverlay.Bar b, int trackEnd, int y, int barW, float numScale, float nameScale, float a, boolean sharesRowWithNumber
   ) {
      String name = b.name.toUpperCase(Locale.ROOT);
      int numberW = sharesRowWithNumber ? Math.round((float)font.width(b.number) * numScale) + 6 : 0;
      int maxPx = Math.max(10, Math.round((float)(trackEnd - numberW) / nameScale));
      name = font.plainSubstrByWidth(name, Math.min(maxPx, Math.round((float)barW / nameScale)));
      int textW = Math.round((float)font.width(name) * nameScale);
      drawText(g, font, name, trackEnd - textW, y, nameScale, argb(12107976, a));
   }

   private static void drawText(GuiGraphics g, Font font, String text, int x, int y, float textScale, int argb) {
      if ((argb >>> 24 & 0xFF) >= 16) {
         if (Math.abs(textScale - 1.0F) < 0.001F) {
            HudText.text(g, font, text, (float)x, (float)y, argb, true);
         } else {
            PoseStack pose = g.pose();
            pose.pushPose();
            pose.translate((float)x, (float)y, 0.0F);
            pose.scale(textScale, textScale, 1.0F);
            HudText.text(g, font, text, 0.0F, 0.0F, argb, true);
            pose.popPose();
         }
      }
   }

   private String numberText(LerpingBossEvent event, float progress, Minecraft mc, long now) {
      if (BossBarConfig.numberMode == BossNumberMode.HEALTH) {
         LivingEntity boss = this.findBoss(event, mc, now);
         if (boss != null) {
            float hp = boss.getHealth();
            return String.valueOf(hp <= 0.0F ? 0 : Math.max(1, (int)Math.ceil((double)hp)));
         }
      }

      int percent = progress <= 0.0F ? 0 : Math.max(1, Math.round(progress * 100.0F));
      return BossBarConfig.percentSign ? percent + "%" : String.valueOf(percent);
   }

   private LivingEntity findBoss(LerpingBossEvent event, Minecraft mc, long now) {
      if (mc.level != null && mc.player != null) {
         BossBarOverlay.Match match = this.matches.computeIfAbsent(event.getId(), k -> new BossBarOverlay.Match());
         LivingEntity cached = match.entity;
         if (cached != null && !cached.isRemoved() && cached.isAlive()) {
            return cached;
         } else {
            match.entity = null;
            if (now < match.nextScanMs) {
               return null;
            } else {
               match.nextScanMs = now + 1000L;
               String name = event.getName().getString();
               LivingEntity best = null;
               double bestDist = Double.MAX_VALUE;

               for (Entity e : mc.level.entitiesForRendering()) {
                  if (e instanceof LivingEntity) {
                     LivingEntity living = (LivingEntity)e;
                     if (living.getDisplayName().getString().equals(name)) {
                        double d = living.distanceTo(mc.player);
                        if (d < bestDist) {
                           bestDist = d;
                           best = living;
                        }
                     }
                  }
               }

               match.entity = best;
               return best;
            }
         }
      } else {
         return null;
      }
   }

   private static int notchCount(String overlayName) {
      int idx = overlayName.lastIndexOf(95);
      if (idx >= 0 && overlayName.startsWith("NOTCHED")) {
         try {
            return Integer.parseInt(overlayName.substring(idx + 1));
         } catch (NumberFormatException var3) {
            return 0;
         }
      } else {
         return 0;
      }
   }

   private static int colorOf(String name) {
      return switch (name) {
         case "PINK" -> 16740264;
         case "BLUE" -> 5680895;
         case "RED" -> 16732754;
         case "GREEN" -> 8118395;
         case "YELLOW" -> 16767050;
         case "PURPLE" -> 11898111;
         default -> 15921906;
      };
   }

   private static int argb(int rgb, float alpha) {
      int a = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
      return a << 24 | rgb & 16777215;
   }

   private static int mix(int from, int to, float t) {
      float c = Math.max(0.0F, Math.min(1.0F, t));
      int r = Math.round((float)(from >> 16 & 0xFF) * (1.0F - c) + (float)(to >> 16 & 0xFF) * c);
      int g = Math.round((float)(from >> 8 & 0xFF) * (1.0F - c) + (float)(to >> 8 & 0xFF) * c);
      int b = Math.round((float)(from & 0xFF) * (1.0F - c) + (float)(to & 0xFF) * c);
      return r << 16 | g << 8 | b;
   }

   private static final class Bar {
      UUID id;
      EasedAnimator vis;
      EasedAnimator fill;
      EasedAnimator y;
      boolean present;
      boolean yPlaced;
      String number = "";
      String name = "";
      int color = 15921906;
      int notches;
      float progress;
      float lastProgress;
      long trailHoldUntil;
      EasedAnimator trail;
      boolean numTop;
      boolean numBottom;
      boolean numSide;
      boolean nameTop;
      boolean nameBottom;
      int capTopH;
      int capBottomH;
      int mainH;
      int entryH;
   }

   @EventBusSubscriber(
      modid = "modernhud",
      value = {Dist.CLIENT}
   )
   public static class Events {
      @SubscribeEvent
      public static void onBossBar(BossEventProgress event) {
         if (BossBarConfig.enabled) {
            event.setCanceled(true);
            event.setIncrement(0);
            LerpingBossEvent ev = event.getBossEvent();
            if (BossBarOverlay.SEEN.size() < 64 || BossBarOverlay.SEEN.containsKey(ev.getId())) {
               BossBarOverlay.SEEN.put(ev.getId(), ev);
               BossBarOverlay.SEEN_AT.put(ev.getId(), BossBarOverlay.monoMs());
            }
         }
      }
   }

   private static final class Match {
      LivingEntity entity;
      long nextScanMs;
   }
}
