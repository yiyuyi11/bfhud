package cc.xypp.modernhud.client;

import cc.xypp.modernhud.client.compat.SuperbWarfareCompat;
import cc.xypp.modernhud.config.AbsorptionColorMode;
import cc.xypp.modernhud.config.BarTextPosition;
import cc.xypp.modernhud.config.HpTextFormat;
import cc.xypp.modernhud.config.HudConfig;
import cc.xypp.modernhud.config.NamePosition;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.Team;

public class PlayerHudOverlay implements IGuiOverlay {
   private static final int HEAD_SIZE = 18;
   private static final int ROW_GAP = 2;
   private static final long DAMAGE_FLASH_MS = 350L;
   private final EasedAnimator healthAnimator = new EasedAnimator(280.0F, EasedAnimator.Easing.QUINTIC_OUT);
   private float lastHealthTarget = -1.0F;
   private long damageFlashStartMs = 0L;
   private final EasedAnimator absorptionAnimator = new EasedAnimator(220.0F, EasedAnimator.Easing.QUINTIC_OUT);
   private final EasedAnimator absorbRowAnimator = new EasedAnimator(260.0F, EasedAnimator.Easing.CUBIC_IN_OUT);
   private boolean absorbRowInit;
   private float lastAbsorptionTarget = -1.0F;
   private long absorptionFlashStartMs = 0L;
   private int lastContentWidth;
   static int lastFrameX;
   static int lastFrameY = 1073741823;
   static int lastFrameWidth;
   static int lastFrameHeight;
   private float textRatio = 1.0F;

   public void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
      if (HudConfig.enabled) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player != null) {
            if (!player.isSpectator()) {
               // The status block steps aside in any vehicle, not only Superb Warfare hulls: the vehicle
               // readouts own the right side and the minimap owns the corner it would otherwise sit in.
               if (!SuperbWarfareCompat.isInVehicle(player) && !HudAnchors.riding(player)) {
                  Font font = Minecraft.getInstance().font;
                  double scale = (double)HudText.snapElement(graphics, HudConfig.barScale);
                  this.textRatio = HudText.layoutRatio((float)(scale * Minecraft.getInstance().getWindow().getGuiScale()));
                  this.updateAbsorbRow(player);
                  int panelPad = HudConfig.showPanel ? HudPanel.padding() : 0;
                  int measuredHeight = this.layoutPass(player, font, false, null, HudConfig.barWidth);
                  int totalWidth = this.lastContentWidth + panelPad * 2;
                  int totalHeight = measuredHeight + panelPad * 2;
                  int[] pos = this.resolvePosition(screenWidth, screenHeight, totalWidth, totalHeight, scale);
                  lastFrameX = pos[0];
                  lastFrameY = pos[1];
                  lastFrameWidth = (int)Math.round((double)totalWidth * scale);
                  lastFrameHeight = (int)Math.round((double)totalHeight * scale);
                  PoseStack pose = graphics.pose();
                  pose.pushPose();
                  pose.translate((float)pos[0], (float)pos[1], 0.0F);
                  pose.scale((float)scale, (float)scale, 1.0F);
                  if (HudConfig.showPanel) {
                     HudPanel.draw(graphics, 0, 0, totalWidth, totalHeight, HudConfig.panelColor, HudConfig.panelOpacity);
                     HudPanel.drawBorder(
                        graphics,
                        0,
                        0,
                        totalWidth,
                        totalHeight,
                        HudConfig.borderColor,
                        HudConfig.borderOpacity,
                        HudConfig.borderStyle,
                        HudConfig.borderLength,
                        HudConfig.borderOutset
                     );
                  }

                  pose.translate((float)panelPad, (float)panelPad, 0.0F);
                  this.layoutPass(player, font, true, graphics, HudConfig.barWidth);
                  pose.popPose();
               }
            }
         }
      }
   }

   private void updateAbsorbRow(LocalPlayer player) {
      if (!HudConfig.collapseAbsorptionRow) {
         this.absorbRowAnimator.snapTo(1.0F);
         this.absorbRowInit = true;
      } else {
         boolean needed = player.getAbsorptionAmount() > 0.01F || this.absorptionAnimator.getCurrentValue() > 0.01F;
         if (!this.absorbRowInit) {
            this.absorbRowAnimator.snapTo(needed ? 1.0F : 0.0F);
            this.absorbRowInit = true;
         }

         this.absorbRowAnimator.setDurationMs((float)HudConfig.absorptionAnimMs + 40.0F);
         this.absorbRowAnimator.setTarget(needed ? 1.0F : 0.0F);
         this.absorbRowAnimator.update();
      }
   }

   private int tw(Font font, String s) {
      return Math.round((float)font.width(s) * this.textRatio);
   }

   private int layoutPass(LocalPlayer player, Font font, boolean draw, GuiGraphics graphics, int barWidth) {
      int lineH = 9;
      int healthH = Math.max(2, Math.round((float)HudConfig.barHeight * 0.5F));
      int plateH = Math.max(2, HudConfig.absorptionHeight);
      int headWidth = HudConfig.showHead ? 24 : 0;
      int contentWidth = barWidth;
      boolean nameAbove = HudConfig.namePosition != NamePosition.BELOW;
      int nameH = HudConfig.showName ? lineH + 2 : 0;
      boolean hpBelowNameHead = HudConfig.showHpText && HudConfig.hpTextPosition == BarTextPosition.BELOW_NAME_HEAD;
      boolean armorBelowNameHead = HudConfig.showArmor && HudConfig.armorTextPosition == BarTextPosition.BELOW_NAME_HEAD;
      int infoLineH = !hpBelowNameHead && !armorBelowNameHead ? 0 : lineH + 2;
      if (hpBelowNameHead || armorBelowNameHead) {
         contentWidth = Math.max(barWidth, this.sharedInfoLineWidth(font, player, hpBelowNameHead, armorBelowNameHead));
      }

      boolean hpBelowBar = HudConfig.showHealthBar && HudConfig.showHpText && HudConfig.hpTextPosition == BarTextPosition.BELOW_BAR;
      boolean hpEndOfBar = HudConfig.showHealthBar && HudConfig.showHpText && HudConfig.hpTextPosition == BarTextPosition.END_OF_BAR;
      boolean armorOwnRow = HudConfig.showArmor && HudConfig.armorTextPosition != BarTextPosition.BELOW_NAME_HEAD;
      if (hpBelowBar) {
         contentWidth = Math.max(contentWidth, this.tw(font, this.hpString(player)));
      }

      if (hpEndOfBar) {
         contentWidth = Math.max(contentWidth, barWidth + 4 + this.tw(font, this.hpString(player)));
      }

      if (armorOwnRow) {
         contentWidth = Math.max(contentWidth, this.tw(font, this.armorString(player)));
      }

      if (HudConfig.showOverflowText && player.getAbsorptionAmount() > 0.0F) {
         float overflow = BarRender.overflow(player.getAbsorptionAmount(), HudConfig.segmentCount, HudConfig.pointsPerSegment);
         if (overflow > 0.01F) {
            contentWidth = Math.max(contentWidth, barWidth + 4 + this.tw(font, "+" + this.formatOneOrZeroDecimals(overflow)));
         }
      }

      float absorbVis = HudConfig.collapseAbsorptionRow ? this.absorbRowAnimator.getCurrentValue() : 1.0F;
      int absorbH = Math.round((float)(plateH + 2) * absorbVis);
      int barBlockH = absorbH;
      if (HudConfig.showHealthBar) {
         barBlockH = absorbH + healthH;
         if (hpBelowBar) {
            barBlockH += lineH + 2;
         }
      }

      if (armorOwnRow) {
         barBlockH += 2 + lineH;
      }

      int totalBlockH = nameH + infoLineH + barBlockH;
      int rowH = Math.max(totalBlockH, HudConfig.showHead ? 18 : 0);
      int barBlockY = nameAbove ? nameH + infoLineH : 0;
      if (draw && HudConfig.showHead) {
         int headY = barBlockY + (barBlockH - 18) / 2;
         headY = Math.max(0, Math.min(headY, rowH - 18));
         renderHead(graphics, player, 0, headY, 18);
      }

      int cursorY = Math.max(0, (rowH - totalBlockH) / 2);
      if (HudConfig.showName && nameAbove) {
         if (draw) {
            HudText.text(graphics, font, player.getName(), (float)headWidth, (float)cursorY, this.resolveNameColor(player), true);
         }

         cursorY += nameH;
      }

      if (infoLineH > 0) {
         if (draw) {
            this.drawSharedInfoLine(graphics, font, player, headWidth, cursorY, hpBelowNameHead, armorBelowNameHead);
         }

         cursorY += infoLineH;
      }

      if (draw) {
         float absorptionRaw = Math.max(0.0F, player.getAbsorptionAmount());
         if (this.lastAbsorptionTarget < 0.0F) {
            this.absorptionAnimator.snapTo(absorptionRaw);
         } else if (absorptionRaw < this.lastAbsorptionTarget - 5.0E-4F) {
            this.absorptionFlashStartMs = System.currentTimeMillis();
         }

         this.lastAbsorptionTarget = absorptionRaw;
         this.absorptionAnimator.setDurationMs((float)HudConfig.absorptionAnimMs);
         this.absorptionAnimator.setTarget(absorptionRaw);
         float animatedAbsorption = this.absorptionAnimator.update();
         if (animatedAbsorption > 0.01F && absorbVis >= 0.85F) {
            boolean customColor = HudConfig.absorptionColorMode == AbsorptionColorMode.CUSTOM;
            if (HudConfig.showVitalsGlow) {
               long flashElapsed = System.currentTimeMillis() - this.absorptionFlashStartMs;
               float flashAlpha = flashElapsed >= 0L && flashElapsed < 350L ? 1.0F - (float)flashElapsed / 350.0F : 0.0F;
               flashAlpha *= flashAlpha;
               if (flashAlpha > 0.01F) {
                  int glowColor = customColor ? HudConfig.absorptionCustomColor : -11228184;
                  GlowEffect.drawGlowRect(graphics, headWidth, cursorY, barWidth, plateH, glowColor, flashAlpha, 3.0F);
               }
            }

            BarRender.plates(
               graphics,
               headWidth,
               cursorY,
               barWidth,
               plateH,
               animatedAbsorption,
               HudConfig.segmentCount,
               HudConfig.pointsPerSegment,
               customColor,
               HudConfig.absorptionCustomColor
            );
            if (HudConfig.showOverflowText) {
               float overflow = BarRender.overflow(absorptionRaw, HudConfig.segmentCount, HudConfig.pointsPerSegment);
               if (overflow > 0.01F) {
                  String txt = "+" + this.formatOneOrZeroDecimals(overflow);
                  HudText.text(graphics, font, txt, (float)(headWidth + barWidth + 4), (float)(cursorY + (plateH - lineH) / 2), -3151617, true);
               }
            }
         }
      }

      cursorY += absorbH;
      if (HudConfig.showHealthBar) {
         float maxHealth = Math.max(1.0F, player.getMaxHealth());
         float target = Math.max(0.0F, Math.min(1.0F, player.getHealth() / maxHealth));
         if (draw) {
            if (this.lastHealthTarget < 0.0F) {
               this.healthAnimator.snapTo(target);
            } else if (target < this.lastHealthTarget - 5.0E-4F) {
               this.damageFlashStartMs = System.currentTimeMillis();
            }

            this.lastHealthTarget = target;
            this.healthAnimator.setDurationMs((float)HudConfig.healthAnimMs);
            this.healthAnimator.setTarget(target);
            float animatedHealth = this.healthAnimator.update();
            double percent = 100.0 * (double)target;
            boolean isLow = percent <= HudConfig.lowHealthThresholdPercent;
            int color = isLow ? -1552072 : -723208;
            if (HudConfig.showVitalsGlow) {
               long flashElapsed = System.currentTimeMillis() - this.damageFlashStartMs;
               float flashAlpha = flashElapsed >= 0L && flashElapsed < 350L ? 1.0F - (float)flashElapsed / 350.0F : 0.0F;
               flashAlpha *= flashAlpha;
               float pulseAlpha = isLow ? 0.22F + 0.15F * (1.0F + (float)Math.sin((double)System.currentTimeMillis() / 260.0)) : 0.0F;
               float glowAlpha = Math.max(flashAlpha, pulseAlpha);
               if (glowAlpha > 0.01F) {
                  GlowEffect.drawGlowRect(graphics, headWidth, cursorY, barWidth, healthH, HudConfig.vitalsGlowColor, glowAlpha, 3.0F);
               }
            }

            BarRender.bar(graphics, headWidth, cursorY, barWidth, healthH, animatedHealth, color);
            if (hpEndOfBar) {
               this.drawHpText(graphics, font, player, headWidth + barWidth + 4, cursorY + (healthH - lineH) / 2);
            }
         }

         cursorY += healthH;
         if (hpBelowBar) {
            if (draw) {
               this.drawHpText(graphics, font, player, headWidth, cursorY);
            }

            cursorY += lineH + 2;
         }

         if (armorOwnRow) {
            cursorY += 2;
         }
      }

      if (armorOwnRow) {
         if (draw) {
            this.drawArmorText(graphics, font, player, headWidth, cursorY);
         }

         cursorY += lineH;
      }

      if (HudConfig.showName && !nameAbove) {
         if (draw) {
            HudText.text(graphics, font, player.getName(), (float)headWidth, (float)cursorY, this.resolveNameColor(player), true);
         }

         cursorY += lineH;
      }

      this.lastContentWidth = headWidth + contentWidth;
      return Math.max(rowH, cursorY);
   }

   private int sharedInfoLineWidth(Font font, LocalPlayer player, boolean hp, boolean armor) {
      int w = 0;
      if (hp) {
         w += this.tw(font, this.hpString(player));
         if (armor) {
            w += 6;
         }
      }

      if (armor) {
         w += this.tw(font, this.armorString(player));
      }

      return w;
   }

   private void drawSharedInfoLine(GuiGraphics graphics, Font font, LocalPlayer player, int x, int y, boolean hp, boolean armor) {
      int cursorX = x;
      if (hp) {
         String text = this.hpString(player);
         int color = this.hpColor(player);
         HudText.text(graphics, font, text, (float)x, (float)y, color, true);
         cursorX = x + this.tw(font, text) + 6;
      }

      if (armor) {
         String text = this.armorString(player);
         HudText.text(graphics, font, text, (float)cursorX, (float)y, HudConfig.armorTextColor | 0xFF000000, true);
      }
   }

   private void drawHpText(GuiGraphics graphics, Font font, LocalPlayer player, int x, int y) {
      HudText.text(graphics, font, this.hpString(player), (float)x, (float)y, this.hpColor(player), true);
   }

   private void drawArmorText(GuiGraphics graphics, Font font, LocalPlayer player, int x, int y) {
      HudText.text(graphics, font, this.armorString(player), (float)x, (float)y, HudConfig.armorTextColor | 0xFF000000, true);
   }

   private String hpString(LocalPlayer player) {
      float maxHealth = Math.max(1.0F, player.getMaxHealth());
      float health = Math.max(0.0F, player.getHealth());
      double percent = 100.0 * (double)health / (double)maxHealth;
      return HudConfig.hpTextFormat == HpTextFormat.PERCENT ? Math.round(percent) + "%" : String.format("%.1f/%.1f", health, maxHealth);
   }

   private int hpColor(LocalPlayer player) {
      float maxHealth = Math.max(1.0F, player.getMaxHealth());
      float health = Math.max(0.0F, player.getHealth());
      double percent = 100.0 * (double)health / (double)maxHealth;
      return percent <= HudConfig.lowHealthThresholdPercent ? HudConfig.hpTextLowColor : HudConfig.hpTextNormalColor;
   }

   private String armorString(LocalPlayer player) {
      int armor = player.getArmorValue();
      return HudConfig.armorTextFormat == HpTextFormat.PERCENT ? Math.round(100.0 * (double)armor / 20.0) + "%" : armor + "/20";
   }

   private String formatOneOrZeroDecimals(float value) {
      long rounded = (long)Math.round(value);
      return Math.abs(value - (float)rounded) < 0.05F ? String.valueOf(rounded) : String.format("%.1f", value);
   }

   static void renderHead(GuiGraphics graphics, AbstractClientPlayer player, int x, int y, int size) {
      renderHead(graphics, player.getSkin().texture(), x, y, size);
   }

   static void renderHead(GuiGraphics graphics, ResourceLocation skin, int x, int y, int size) {
      RenderSystem.setShaderTexture(0, skin);
      RenderSystem.enableBlend();
      graphics.blit(skin, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
      graphics.blit(skin, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
      RenderSystem.disableBlend();
   }

   private int resolveNameColor(AbstractClientPlayer player) {
      if (HudConfig.personalUseTeamColor) {
         Team team = player.getTeam();
         ChatFormatting fmt = team != null ? team.getColor() : null;
         if (fmt != null && fmt.getColor() != null) {
            return 0xFF000000 | fmt.getColor();
         }
      }

      return -1;
   }

   private int[] resolvePosition(int screenWidth, int screenHeight, int width, int height, double scale) {
      int scaledWidth = (int)Math.round((double)width * scale);
      int scaledHeight = (int)Math.round((double)height * scale);
      // Top-anchored layouts sit below Xaero's minimap; without Xaero the offset is zero.
      int minimap = XaeroCompat.offset();
      int x;
      int y;
      switch (HudConfig.position) {
         case TOP_LEFT:
            x = HudConfig.offsetX;
            y = HudConfig.offsetY;
            break;
         case TOP_RIGHT:
            x = screenWidth - scaledWidth - HudConfig.offsetX;
            y = HudConfig.offsetY;
            break;
         case BOTTOM_RIGHT:
            x = screenWidth - scaledWidth - HudConfig.offsetX;
            y = screenHeight - scaledHeight - HudConfig.offsetY;
            break;
         case CUSTOM:
            x = HudConfig.offsetX;
            y = HudConfig.offsetY;
            break;
         default:
            x = HudConfig.offsetX;
            y = screenHeight - scaledHeight - HudConfig.offsetY;
      }

      return new int[]{x, y};
   }
}
