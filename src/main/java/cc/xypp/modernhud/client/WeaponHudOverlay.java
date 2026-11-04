package cc.xypp.modernhud.client;

import cc.xypp.modernhud.client.compat.SuperbWarfareCompat;
import cc.xypp.modernhud.client.compat.TaczCompat;
import cc.xypp.modernhud.config.GlowQuality;
import cc.xypp.modernhud.config.HeatDisplayMode;
import cc.xypp.modernhud.config.HudConfig;
import cc.xypp.modernhud.config.WeaponConfig;
import cc.xypp.modernhud.config.WeaponHudSource;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class WeaponHudOverlay implements IGuiOverlay {
   private static final ResourceLocation FIRE_ICON_SEMI = new ResourceLocation("modernhud", "textures/hud/fire_mode_semi.png");
   private static final ResourceLocation FIRE_ICON_AUTO = new ResourceLocation("modernhud", "textures/hud/fire_mode_auto.png");
   private static final ResourceLocation FIRE_ICON_BURST = new ResourceLocation("modernhud", "textures/hud/fire_mode_burst.png");
   private static final int PAD = 6;
   static final int ICON_BOX_W = 64;
   static final int ICON_BOX_H = 32;
   static final int SECONDARY_GAP = 2;
   static final int SECONDARY_BOX_H = 16;
   static final int CONTENT_H = 50;
   private static final int FIRE_ICON_SIZE = 7;
   private static final int FIRE_ICON_W = 14;
   private static final int FIRE_CELL_W = 21;
   private static final int FIRE_CELL_H = 10;
   private static final int FIRE_RIGHT_INSET = 10;
   private static final int INFO_PAD_Y = 3;
   private static final int AMMO_TEXT_RAISE = 3;
   private static final float RESERVE_ICON_BOOST = 1.05F;
   private static final int FIRE_ICON_TEX_SIZE = 128;
   private static final int FIRE_ICON_CROP_U = 33;
   private static final int FIRE_ICON_CROP_V = 30;
   private static final int FIRE_ICON_CROP_W = 68;
   private static final int FIRE_ICON_CROP_H = 68;
   private static final int ATTACHMENT_DOT_SIZE = 4;
   private static final int ATTACHMENT_DOT_GAP = 3;
   static final int CHIP_PAD_X = 4;
   private static final int CHIP_PAD_Y = 2;
   private ResourceLocation currentGunId;
   private ResourceLocation secondaryGunId;
   private long switchAnimStartMs = 0L;
   private static final long PUSH_ANIM_MS = 220L;
   private static final long SWITCH_GLOW_MS = 400L;
   private String lastFireModeSeen;
   private long fireModeChangeStartMs = 0L;
   private static final long VANILLA_HUD_REASSERT_MS = 1000L;
   private long lastVanillaHudReassertMs = 0L;
   private boolean contextSuppressed;
   private long lastContextSuppressMs = 0L;
   private static final long NEUTRAL_SCAN_INTERVAL_MS = 250L;
   private ResourceLocation[] cachedNeutralGuns = new ResourceLocation[0];
   private long lastNeutralScanMs = 0L;
   private static final int HEAT_BAR_H = 3;
   private static final float HEAT_BAR_WIDTH_FRACTION = 0.55F;
   private int haloRgb;
   private float haloAlpha;

   private static float contentScale() {
      return (float)WeaponConfig.infoContentScale / 100.0F;
   }

   private static int fireCellW() {
      return Math.max(10, Math.round(21.0F * contentScale()));
   }

   private static int fireCellH() {
      return Math.max(6, Math.round(10.0F * contentScale()));
   }

   private void updateContextSuppression(boolean want) {
      if (want) {
         long now = Util.getMillis();
         if (!this.contextSuppressed || now - this.lastContextSuppressMs >= 1000L) {
            this.contextSuppressed = true;
            this.lastContextSuppressMs = now;
            TaczCompat.setVanillaGunHudEnabled(false);
         }
      } else if (this.contextSuppressed) {
         this.contextSuppressed = false;
         WeaponConfig.syncHudSource();
      }
   }

   public void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
      if (TaczCompat.loaded()) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player != null) {
            boolean spectator = player.isSpectator();
            boolean inVehicle = SuperbWarfareCompat.isInVehicle(player);
            this.updateContextSuppression(spectator || inVehicle);
            if (WeaponConfig.enabled) {
               if (!Minecraft.getInstance().options.hideGui || WeaponConfig.ignoreHideGui) {
                  if (spectator) {
                     this.currentGunId = null;
                  } else if (inVehicle) {
                     this.currentGunId = null;
                  } else {
                     ItemStack held = player.getMainHandItem();
                     if (!held.isEmpty() && !TaczCompat.isGun(held) && this.currentGunId != null && LoadoutGear.isGearItem(held)) {
                        ItemStack keep = this.findInHotbar(player.getInventory(), this.currentGunId);
                        if (keep != null) {
                           held = keep;
                        }
                     }

                     if (!held.isEmpty() && TaczCompat.isGun(held)) {
                        Font font = Minecraft.getInstance().font;
                        this.trackWeaponSlots(player, held);
                        this.reassertVanillaHudOff();
                        String heldFireMode = TaczCompat.fireMode(held);
                        if (this.lastFireModeSeen != null && heldFireMode != null && !heldFireMode.equals(this.lastFireModeSeen)) {
                           this.fireModeChangeStartMs = Util.getMillis();
                        }

                        this.lastFireModeSeen = heldFireMode;
                        int frameW = WeaponConfig.frameWidth;
                        int frameH = WeaponConfig.frameHeight;
                        if (LoadoutGear.enabled()) {
                           LoadoutGear.update(player, false);
                        }

                        int[] pos = resolvePosition(screenWidth, screenHeight, frameW + LoadoutGear.extraWidth(), frameH, (double)effectiveScale(graphics));
                        int originX = pos[0];
                        int originY = pos[1];
                        int iconBoxX = frameW - 64;
                        int textRightEdge = iconBoxX - WeaponConfig.infoBoxGap;
                        long switchElapsed = Util.getMillis() - this.switchAnimStartMs;
                        int pushOffset = 0;
                        if (WeaponConfig.showSwitchSlide) {
                           float animT = switchElapsed >= 220L ? 1.0F : Math.max(0.0F, (float)switchElapsed / 220.0F);
                           float invT = 1.0F - animT;
                           float eased = 1.0F - invT * invT * invT * invT * invT;
                           pushOffset = Math.round((1.0F - eased) * (float)WeaponConfig.switchSlideDistance);
                        }

                        float switchGlowAlpha = 0.0F;
                        if (WeaponConfig.showWeaponGlow && switchElapsed >= 0L && switchElapsed < 400L) {
                           float glowT = 1.0F - (float)switchElapsed / 400.0F;
                           switchGlowAlpha = glowT * glowT;
                        }

                        float switchBorderFlashAlpha = 0.0F;
                        if (WeaponConfig.showSwitchBorderFlash && switchElapsed >= 0L && switchElapsed < (long)WeaponConfig.switchBorderFlashMs) {
                           float flashT = 1.0F - (float)switchElapsed / (float)WeaponConfig.switchBorderFlashMs;
                           switchBorderFlashAlpha = flashT * flashT;
                        }

                        PoseStack pose = graphics.pose();
                        pose.pushPose();
                        pose.translate((float)originX, (float)originY, 0.0F);
                        float wScale = effectiveScale(graphics);
                        pose.scale(wScale, wScale, 1.0F);
                        pose.translate(0.0F, (float)((frameH - 50) / 2), 0.0F);
                        this.drawInfoBox(graphics, font, player, held, textRightEdge);
                        pose.pushPose();
                        pose.translate(0.0F, (float)pushOffset, 0.0F);
                        this.drawPrimaryIcon(graphics, held, iconBoxX, 0, 64, 32, switchGlowAlpha, switchBorderFlashAlpha);
                        this.drawHeatFlash(graphics, held, iconBoxX, 0, 64, 32);
                        if (WeaponConfig.showAttachmentDots) {
                           this.drawAttachmentDots(graphics, held, iconBoxX, 0, 64);
                        }

                        if (WeaponConfig.showSecondary) {
                           this.drawSecondaryWeapon(graphics, player, iconBoxX, 34);
                        }

                        pose.popPose();
                        if (LoadoutGear.enabled()) {
                           LoadoutGear.draw(graphics, frameW + 2, false);
                        }

                        pose.popPose();
                        if (WeaponConfig.showFireTypeFlash) {
                           this.drawFireTypeFlash(graphics, font, held, screenWidth, screenHeight);
                        }
                     } else {
                        this.currentGunId = null;
                        this.renderNeutral(graphics, player, screenWidth, screenHeight);
                     }
                  }
               }
            }
         }
      }
   }

   private void reassertVanillaHudOff() {
      long now = Util.getMillis();
      if (now - this.lastVanillaHudReassertMs >= 1000L) {
         this.lastVanillaHudReassertMs = now;
         WeaponConfig.syncHudSource();
      }
   }

   private void restoreVanillaHudIfIdle() {
      if (WeaponConfig.preferredHudSource == WeaponHudSource.MODERNHUD) {
         TaczCompat.setVanillaGunHudEnabled(true);
      }
   }

   private void renderNeutral(GuiGraphics graphics, LocalPlayer player, int screenWidth, int screenHeight) {
      if (WeaponConfig.enabled && TaczCompat.loaded()) {
         if (!Minecraft.getInstance().options.hideGui || WeaponConfig.ignoreHideGui) {
            long now = Util.getMillis();
            if (now - this.lastNeutralScanMs >= 250L) {
               this.lastNeutralScanMs = now;
               this.cachedNeutralGuns = this.findGunsInHotbarOrdered(player.getInventory(), 2);
            }

            ResourceLocation[] guns = this.cachedNeutralGuns;
            if (guns.length == 0) {
               this.restoreVanillaHudIfIdle();
            } else {
               this.reassertVanillaHudOff();
               int frameW = WeaponConfig.frameWidth;
               if (LoadoutGear.enabled()) {
                  LoadoutGear.update(player, true);
               }

               int[] pos = resolvePosition(
                  screenWidth, screenHeight, frameW + LoadoutGear.extraWidth(), WeaponConfig.frameHeight, (double)effectiveScale(graphics)
               );
               int iconBoxX = frameW - 64;
               PoseStack pose = graphics.pose();
               pose.pushPose();
               pose.translate((float)pos[0], (float)pos[1], 0.0F);
               float wScale = effectiveScale(graphics);
               pose.scale(wScale, wScale, 1.0F);
               pose.translate(0.0F, (float)((WeaponConfig.frameHeight - 50) / 2), 0.0F);
               int firstY = 8;
               this.drawNeutralIcon(graphics, player, guns[0], iconBoxX, firstY);
               if (guns.length > 1) {
                  this.drawNeutralIcon(graphics, player, guns[1], iconBoxX, firstY + 16 + 2);
               }

               if (LoadoutGear.enabled()) {
                  LoadoutGear.draw(graphics, frameW + 2, true);
               }

               pose.popPose();
            }
         }
      }
   }

   private ResourceLocation[] findGunsInHotbarOrdered(Inventory inventory, int max) {
      ResourceLocation[] found = new ResourceLocation[max];
      int count = 0;

      for (int i = 0; i < 9 && count < max; i++) {
         ItemStack stack = inventory.getItem(i);
         if (!stack.isEmpty()) {
            ResourceLocation id = TaczCompat.gunId(stack);
            if (id != null) {
               boolean duplicate = false;

               for (int j = 0; j < count; j++) {
                  if (found[j].equals(id)) {
                     duplicate = true;
                     break;
                  }
               }

               if (!duplicate) {
                  found[count++] = id;
               }
            }
         }
      }

      return count == max ? found : Arrays.copyOf(found, count);
   }

   private void drawChipBox(GuiGraphics g, int x, int y, int w, int h, double extraAlphaMul) {
      if (WeaponConfig.showPanel) {
         HudPanel.draw(g, x, y, w, h, WeaponConfig.panelColor, WeaponConfig.panelOpacity * extraAlphaMul);
         HudPanel.drawBorder(
            g,
            x,
            y,
            w,
            h,
            WeaponConfig.borderColor,
            WeaponConfig.borderOpacity * extraAlphaMul,
            WeaponConfig.borderStyle,
            WeaponConfig.borderLength,
            WeaponConfig.borderOutset
         );
      }
   }

   private void drawSecondaryChipBox(GuiGraphics g, int x, int y, int w, int h, double extraAlphaMul) {
      if (WeaponConfig.showPanel) {
         HudPanel.draw(g, x, y, w, h, WeaponConfig.panelColor, WeaponConfig.panelOpacity * extraAlphaMul);
         HudPanel.drawBottomDots(g, x, y, w, h, WeaponConfig.borderColor, WeaponConfig.borderOpacity * extraAlphaMul, WeaponConfig.secondaryDotSize);
      }
   }

   private void trackWeaponSlots(LocalPlayer player, ItemStack held) {
      ResourceLocation gunId = TaczCompat.gunId(held);
      if (gunId != null) {
         if (!gunId.equals(this.currentGunId)) {
            if (this.currentGunId != null && !this.currentGunId.equals(gunId)) {
               this.secondaryGunId = this.currentGunId;
            }

            this.currentGunId = gunId;
            this.switchAnimStartMs = Util.getMillis();
            this.lastFireModeSeen = null;
         }

         if (gunId.equals(this.secondaryGunId)) {
            this.secondaryGunId = null;
         }

         if (this.secondaryGunId == null || this.findInHotbar(player.getInventory(), this.secondaryGunId) == null) {
            this.secondaryGunId = this.findOtherGunInHotbar(player.getInventory(), gunId);
         }
      }
   }

   private ResourceLocation findOtherGunInHotbar(Inventory inventory, ResourceLocation excludeGunId) {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = inventory.getItem(i);
         if (!stack.isEmpty()) {
            ResourceLocation id = TaczCompat.gunId(stack);
            if (id != null && !id.equals(excludeGunId)) {
               return id;
            }
         }
      }

      return null;
   }

   private WeaponHudOverlay.AmmoRow ammoRow(GuiGraphics g, Font font, LocalPlayer player, ItemStack gun) {
      int mag = TaczCompat.magAmmo(player, gun);
      int cap = TaczCompat.magCapacity(gun);
      String magText = this.ammoString(mag);
      boolean low = cap > 0 && 100.0 * (double)mag / (double)cap <= WeaponConfig.lowAmmoThresholdPercent;
      int magColor = (low ? WeaponConfig.ammoLowColor : WeaponConfig.ammoColor) | 0xFF000000;
      double textScale = WeaponConfig.ammoTextScale * (double)contentScale();
      float[] scales = HudText.snapPair(g, textScale, Math.max(0.3, textScale - WeaponConfig.reserveScaleOffset));
      float ammoScale = scales[0];
      float reserveScale = scales[1];
      String reserveText = WeaponConfig.showReserveAmmo ? this.reserveString(player, gun) : "";
      boolean hasReserve = !reserveText.isEmpty();
      String sep = " | ";
      float magW = (float)font.width(magText) * ammoScale;
      float sepW = hasReserve ? (float)font.width(sep) * reserveScale : 0.0F;
      float reserveW = hasReserve ? (float)font.width(reserveText) * reserveScale : 0.0F;
      int ammoRowH = Math.round(9.0F * ammoScale);
      int reserveRowH = Math.round(9.0F * reserveScale);
      int iconW = 0;
      int iconGap = 0;
      if (hasReserve && WeaponConfig.showAmmoTypeIcon) {
         iconW = Math.max(1, Math.round((float)reserveRowH * (float)WeaponConfig.ammoTypeIconScale * 1.05F));
         iconGap = 4;
      }

      float ammoRowW = magW + sepW + reserveW + (float)iconGap + (float)iconW;
      int reserveAlpha = Math.max(1, (int)Math.round(WeaponConfig.reserveOpacity * 2.55));
      int reserveColor = reserveAlpha << 24 | WeaponConfig.reserveColor & 16777215;
      return new WeaponHudOverlay.AmmoRow(
         magText,
         magColor,
         low,
         ammoScale,
         reserveScale,
         reserveText,
         hasReserve,
         magW,
         sepW,
         reserveW,
         ammoRowH,
         reserveRowH,
         iconW,
         iconGap,
         ammoRowW,
         reserveColor
      );
   }

   private int drawAmmoRow(GuiGraphics g, Font font, WeaponHudOverlay.AmmoRow r, int boxX, int boxY, int boxW, int boxH, int rightEdge, int rowTop) {
      String magText = r.magText();
      int magColor = r.magColor();
      boolean low = r.low();
      float ammoScale = r.ammoScale();
      float reserveScale = r.reserveScale();
      String reserveText = r.reserveText();
      boolean hasReserve = r.hasReserve();
      float magW = r.magW();
      float sepW = r.sepW();
      float reserveW = r.reserveW();
      int ammoRowH = r.ammoRowH();
      int reserveRowH = r.reserveRowH();
      int iconW = r.iconW();
      int iconGap = r.iconGap();
      float ammoRowW = r.ammoRowW();
      int reserveColor = r.reserveColor();
      HudPanel.drawStyled(g, boxX, boxY, boxW, boxH, WeaponConfig.infoBoxStyle, 1.0F);
      int cursorY = rowTop + 3 - 3;
      float rowRightX = (float)(rightEdge - 4);
      float cursorX = rowRightX;
      this.drawAmmoGlow(g, boxX, boxY, boxW, boxH, cursorY, ammoRowH, low);
      if (hasReserve && WeaponConfig.showAmmoTypeIcon) {
         cursorX = rowRightX - (float)iconW;
         int iconY = cursorY + (ammoRowH - iconW) / 2;
         this.drawAmmoTypeIcon(g, Math.round(cursorX), iconY, iconW, WeaponConfig.reserveColor, (float)(WeaponConfig.reserveOpacity / 100.0));
         cursorX -= (float)iconGap;
      }

      if (hasReserve) {
         int reserveTextH = Math.round(9.0F * reserveScale);
         float reserveY = (float)cursorY + (float)(ammoRowH - reserveTextH) / 2.0F;
         cursorX -= reserveW;
         boolean panel = WeaponConfig.ammoGlowReservePanel && WeaponConfig.ammoGlowReservePanelOpacity > 0;
         if (panel) {
            float digitH = 7.0F * reserveScale;
            float padX = 2.5F * reserveScale;
            float ph = Math.max(2.0F, digitH * (float)WeaponConfig.ammoGlowReservePanelHeight / 100.0F);
            float py0 = reserveY + digitH - ph;
            int pa = Math.max(1, Math.min(255, Math.round((float)WeaponConfig.ammoGlowReservePanelOpacity * 2.55F)));
            PixelDraw.fillUnits(g, cursorX - padX, py0, reserveW + padX * 2.0F, ph, pa << 24 | WeaponConfig.weaponGlowColor & 16777215);
         }

         float magHalo = this.haloAlpha;
         this.haloAlpha = 0.0F;
         this.drawZeroPaddedNumber(g, font, reserveText, cursorX, reserveY, reserveScale, reserveColor);
         this.haloAlpha = magHalo;
         cursorX -= sepW;
         if (!panel) {
            int sepAlpha = Math.max(1, Math.round((float)(reserveColor >>> 24 & 0xFF) * 0.65F));
            PixelDraw.fillUnits(
               g,
               cursorX + (sepW - reserveScale) / 2.0F,
               reserveY + reserveScale * 0.5F,
               reserveScale,
               reserveScale * 7.0F,
               sepAlpha << 24 | reserveColor & 16777215
            );
         }
      }

      cursorX -= magW;
      this.drawZeroPaddedNumber(g, font, magText, cursorX, (float)cursorY, ammoScale, magColor);
      return rowTop + 3 + ammoRowH;
   }

   private void drawInfoBox(GuiGraphics g, Font font, LocalPlayer player, ItemStack gun, int rightEdge) {
      WeaponHudOverlay.AmmoRow row = this.ammoRow(g, font, player, gun);
      boolean nameShown = WeaponConfig.showName;
      String[] modes = WeaponConfig.showFireMode ? TaczCompat.supportedFireModes(gun) : new String[0];
      boolean fireShown = modes.length > 1;
      boolean heatShown = WeaponConfig.heatDisplayMode == HeatDisplayMode.BAR && TaczCompat.hasHeat(gun);
      int blockH = row.ammoRowH() + WeaponConfig.infoNameGap + 9 + WeaponConfig.infoRowGap + fireCellH();
      int boxH = Math.max(50 + WeaponConfig.infoBoxRise, blockH + 6);
      int boxBottom = 50;
      int boxY = boxBottom - boxH;
      int nameW = nameShown ? (int)Math.ceil((double)HudText.width(g, font, gun.getHoverName().getString())) : 0;
      int fireW = fireShown ? fireCellW() * modes.length + 10 - 4 : 0;
      int contentW = Math.max(Math.round(row.ammoRowW()), Math.max(nameW, fireW));
      int boxW = Math.max(contentW + 8, rightEdge);
      int boxX = rightEdge - boxW;
      int centered = boxY + Math.round((float)(boxH - blockH) / 2.0F);
      int digitTop = Math.max(boxY + 1, Math.min(centered - WeaponConfig.infoContentRaise, boxBottom - blockH - 1));
      int nameY = digitTop + row.ammoRowH() + WeaponConfig.infoNameGap;
      int fireY = nameY + 9 + WeaponConfig.infoRowGap;
      int rowTop = digitTop + 3 - 3;
      this.drawAmmoRow(g, font, row, boxX, boxY, boxW, boxH, rightEdge, rowTop);
      if (nameShown) {
         this.drawNameRow(g, font, gun, rightEdge, nameY);
      }

      if (fireShown) {
         this.drawFireModeSegmented(g, font, gun, rightEdge, fireY);
      }

      if (heatShown) {
         this.drawHeatBar(g, gun, rightEdge, fireShown ? fireY - 3 - 1 : fireY + (fireCellH() - 3) / 2);
      }
   }

   private int drawNameRow(GuiGraphics g, Font font, ItemStack gun, int rightEdge, int y) {
      if (!WeaponConfig.showName) {
         return y;
      } else {
         String name = gun.getHoverName().getString();
         float nameW = HudText.width(g, font, name);
         int nameColor = WeaponConfig.nameColor | 0xFF000000;
         HudText.text(g, font, name, (float)Math.round((float)(rightEdge - 4) - nameW), (float)y, nameColor, true);
         return y + 9;
      }
   }

   private void drawFireModeSegmented(GuiGraphics g, Font font, ItemStack gun, int rightEdge, int y) {
      if (WeaponConfig.showFireMode) {
         String[] modes = TaczCompat.supportedFireModes(gun);
         if (modes.length > 1) {
            String activeMode = TaczCompat.fireMode(gun);
            int cellW = fireCellW();
            int cellH = fireCellH();
            int boxW = cellW * modes.length;
            int boxX = rightEdge - 10 - boxW;
            if (WeaponConfig.showPanel) {
               HudPanel.draw(g, boxX, y, boxW, cellH, WeaponConfig.panelColor, WeaponConfig.panelOpacity);
            }

            for (int i = 0; i < modes.length; i++) {
               String mode = modes[i];
               int cellX = boxX + i * cellW;
               boolean isActive = mode.equals(activeMode);
               if (i > 0 && WeaponConfig.showPanel) {
                  int dividerAlpha = Math.max(1, (int)Math.round(WeaponConfig.borderOpacity * 0.6 * 2.55));
                  int dividerColor = dividerAlpha << 24 | WeaponConfig.borderColor & 16777215;
                  g.fill(cellX, y, cellX + 1, y + cellH, dividerColor);
               }

               if (isActive) {
                  g.fill(cellX, y, cellX + cellW, y + cellH, 654311423);
               }

               if (isActive && WeaponConfig.showPanel) {
                  HudPanel.drawFullBorder(g, cellX, y, cellW, cellH, WeaponConfig.borderColor, WeaponConfig.borderOpacity);
               }

               if (PixelDraw.isFireIcon(mode)) {
                  PixelDraw.fireCellIcon(g, mode, (float)cellX, (float)y, (float)cellW, (float)cellH, isActive ? -1 : -2135640908);
               } else {
                  String label = mode.isEmpty() ? "?" : mode.substring(0, 1);
                  int fmColor = isActive ? -1 : WeaponConfig.fireModeColor | 0xFF000000;
                  HudText.text(
                     g, font, label, (float)cellX + ((float)cellW - HudText.width(g, font, label)) / 2.0F, (float)(y + (cellH - 9) / 2), fmColor, true
                  );
               }
            }
         }
      }
   }

   private void drawHeatBar(GuiGraphics g, ItemStack gun, int rightEdge, int y) {
      if (WeaponConfig.heatDisplayMode == HeatDisplayMode.BAR && TaczCompat.hasHeat(gun)) {
         float fraction = TaczCompat.heatFraction(gun);
         boolean locked = TaczCompat.isOverheatLocked(gun);
         int color = 0xFF000000 | (locked ? WeaponConfig.heatBarLockedColor : WeaponConfig.heatBarColor);
         int barW = Math.max(1, Math.round((float)rightEdge * 0.55F));
         int barX = rightEdge - 4 - barW;
         BarRender.barRTL(g, barX, y, barW, 3, fraction, color);
      }
   }

   private void drawHeatFlash(GuiGraphics g, ItemStack gun, int x, int y, int w, int h) {
      if (WeaponConfig.heatDisplayMode == HeatDisplayMode.FLASH && TaczCompat.hasHeat(gun)) {
         float fraction = TaczCompat.heatFraction(gun);
         boolean locked = TaczCompat.isOverheatLocked(gun);
         if (!(fraction <= 0.001F) || locked) {
            int color = lerpRgb(WeaponConfig.heatFlashColor, WeaponConfig.heatBarLockedColor, fraction);
            long periodMs = locked ? 260L : (long)Math.round(700.0F - 500.0F * fraction);
            float pulse = 0.5F + 0.5F * (float)Math.sin((double)System.currentTimeMillis() / (double)periodMs);
            float alpha = (locked ? 0.55F : 0.2F + 0.3F * fraction) * (0.5F + 0.5F * pulse);
            GlowEffect.drawGlowRect(g, x, y, w, h, color, alpha, 4.0F);
         }
      }
   }

   private static int lerpRgb(int from, int to, float t) {
      float clamped = Math.max(0.0F, Math.min(1.0F, t));
      int fr = from >> 16 & 0xFF;
      int fg = from >> 8 & 0xFF;
      int fb = from & 0xFF;
      int tr = to >> 16 & 0xFF;
      int tg = to >> 8 & 0xFF;
      int tb = to & 0xFF;
      int r = Math.round((float)fr + (float)(tr - fr) * clamped);
      int gr = Math.round((float)fg + (float)(tg - fg) * clamped);
      int b = Math.round((float)fb + (float)(tb - fb) * clamped);
      return r << 16 | gr << 8 | b;
   }

   private void drawZeroPaddedNumber(GuiGraphics g, Font font, String text, float x, float y, float scale, int color) {
      float halo = this.haloAlpha;
      if (WeaponConfig.zeroPadAmmo && text.length() > 1) {
         int splitIndex = 0;

         while (splitIndex < text.length() - 1 && text.charAt(splitIndex) == '0') {
            splitIndex++;
         }

         if (splitIndex == 0) {
            this.drawTextHalo(g, font, text, x, y, scale, this.haloRgb, halo, WeaponConfig.ammoGlowHalo);
            this.drawScaledText(g, font, text, x, y, scale, color);
         } else {
            String dimPart = text.substring(0, splitIndex);
            String brightPart = text.substring(splitIndex);
            int alpha = color >>> 24 & 0xFF;
            int dimAlpha = Math.max(1, Math.round((float)alpha * 0.35F));
            int dimColor = dimAlpha << 24 | color & 16777215;
            this.drawScaledText(g, font, dimPart, x, y, scale, dimColor, false);
            float brightX = x + (float)font.width(dimPart) * scale;
            this.drawTextHalo(g, font, brightPart, brightX, y, scale, this.haloRgb, halo, WeaponConfig.ammoGlowHalo);
            this.drawScaledText(g, font, brightPart, brightX, y, scale, color);
         }
      } else {
         this.drawTextHalo(g, font, text, x, y, scale, this.haloRgb, halo, WeaponConfig.ammoGlowHalo);
         this.drawScaledText(g, font, text, x, y, scale, color);
      }
   }

   private void drawAmmoTypeIcon(GuiGraphics g, int x, int y, int size, int colorRgb, float alpha) {
      int a = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
      if (a > 0 && size > 0) {
         PixelDraw.fireIcon(g, "RESERVE", (float)x, (float)y, (float)size, a << 24 | colorRgb & 16777215);
      }
   }

   private void drawScaledText(GuiGraphics g, Font font, String text, float screenX, float screenY, float scale, int color) {
      this.drawScaledText(g, font, text, screenX, screenY, scale, color, true);
   }

   private void drawScaledText(GuiGraphics g, Font font, String text, float screenX, float screenY, float scale, int color, boolean shadow) {
      PoseStack pose = g.pose();
      pose.pushPose();
      pose.translate(screenX, screenY, 0.0F);
      pose.scale(scale, scale, 1.0F);
      HudText.text(g, font, text, 0.0F, 0.0F, color, shadow);
      pose.popPose();
   }

   private String reserveString(LocalPlayer player, ItemStack gun) {
      int reserve = TaczCompat.reserveAmmo(player, gun);
      if (reserve == -1) {
         return "∞";
      } else {
         return reserve == Integer.MIN_VALUE ? "" : this.ammoString(reserve);
      }
   }

   private String ammoString(int count) {
      return WeaponConfig.zeroPadAmmo ? String.format("%03d", count) : String.valueOf(count);
   }

   private ResourceLocation iconFor(String mode) {
      return switch (mode) {
         case "SEMI" -> FIRE_ICON_SEMI;
         case "AUTO" -> FIRE_ICON_AUTO;
         case "BURST" -> FIRE_ICON_BURST;
         default -> null;
      };
   }

   private void drawFireTypeFlash(GuiGraphics g, Font font, ItemStack gun, int screenWidth, int screenHeight) {
      String rawMode = TaczCompat.fireMode(gun);
      if (rawMode != null && !rawMode.isEmpty()) {
         long flashMs = (long)Math.max(0, WeaponConfig.fireTypeFlashMs);
         long holdMs = (long)Math.max(0, WeaponConfig.fireTypeHoldMs);
         long fadeMs = (long)Math.max(1, WeaponConfig.fireTypeFadeMs);
         long totalMs = flashMs + holdMs + fadeMs;
         long elapsed = Util.getMillis() - Math.max(this.switchAnimStartMs, this.fireModeChangeStartMs);
         if (elapsed >= 0L && elapsed < totalMs) {
            int color;
            float alpha;
            if (elapsed < flashMs) {
               color = WeaponConfig.fireTypeFlashColor;
               alpha = 1.0F;
            } else if (elapsed < flashMs + holdMs) {
               color = WeaponConfig.fireTypeHoldColor;
               alpha = 1.0F;
            } else {
               color = WeaponConfig.fireTypeHoldColor;
               alpha = 1.0F - (float)(elapsed - flashMs - holdMs) / (float)fadeMs;
            }

            int a = Math.max(1, Math.round(alpha * 255.0F));
            int argb = a << 24 | color & 16777215;
            String modeLabel = this.fireTypeLabel(rawMode);
            String prefix = WeaponConfig.fireTypeShowLabel ? "FIRE TYPE: " : "";
            float scale = HudText.snapScale(g, WeaponConfig.fireTypeScale);
            int prefixW = font.width(prefix);
            int modeW = font.width(modeLabel);
            int totalW = Math.round((float)(prefixW + modeW) * scale);
            int textH = Math.round(9.0F * scale);
            int x = screenWidth / 2 - totalW / 2 + WeaponConfig.fireTypeXOffset;
            int y = screenHeight / 2 - textH / 2 + WeaponConfig.fireTypeYOffset;
            PoseStack pose = g.pose();
            pose.pushPose();
            pose.translate((float)x, (float)y, 0.0F);
            pose.scale(scale, scale, 1.0F);
            if (!prefix.isEmpty()) {
               HudText.text(g, font, prefix, 0.0F, 0.0F, argb, true);
            }

            HudText.text(g, font, modeLabel, (float)prefixW, 0.0F, argb, true);
            g.fill(prefixW, 9, prefixW + modeW, 9 + 1, argb);
            pose.popPose();
         }
      }
   }

   private String fireTypeLabel(String rawMode) {
      return switch (rawMode) {
         case "SEMI" -> "SEMI AUTO";
         case "AUTO" -> "FULL AUTO";
         case "BURST" -> "BURST";
         default -> rawMode;
      };
   }

   private void drawAttachmentDots(GuiGraphics g, ItemStack gun, int boxX, int boxY, int boxW) {
      int[] counts = TaczCompat.attachmentSlotCounts(gun);
      int filled = counts[1];
      if (filled > 0) {
         int inset = 3;
         int dotsW = filled * 4 + (filled - 1) * 3;
         int maxW = boxW - inset * 2;
         if (dotsW > maxW) {
            dotsW = maxW;
         }

         int x = boxX + boxW - inset - dotsW;
         int y = boxY + inset;
         int color = 0xFF000000 | WeaponConfig.attachmentDotColor & 16777215;

         for (int i = 0; i < filled; i++) {
            g.fill(x, y, x + 4, y + 4, color);
            x += 7;
         }
      }
   }

   private void drawPrimaryIcon(GuiGraphics g, ItemStack gun, int boxX, int boxY, int boxW, int boxH, float switchGlowAlpha, float switchBorderFlashAlpha) {
      this.drawChipBox(g, boxX, boxY, boxW, boxH, 1.0);
      if (switchBorderFlashAlpha > 0.01F && WeaponConfig.showPanel) {
         HudPanel.drawBorder(
            g,
            boxX,
            boxY,
            boxW,
            boxH,
            WeaponConfig.switchBorderFlashColor,
            WeaponConfig.borderOpacity * (double)switchBorderFlashAlpha,
            WeaponConfig.borderStyle,
            WeaponConfig.borderLength,
            WeaponConfig.borderOutset
         );
      }

      this.drawSelectedGlow(g, boxX, boxY, boxW, boxH, switchGlowAlpha);
      if (TaczCompat.hasHudTexture(gun)) {
         int[] size = TaczCompat.iconSize((float)WeaponConfig.iconScale);
         int ix = boxX + (boxW - size[0]) / 2;
         int iy = boxY + (boxH - size[1]) / 2;
         TaczCompat.renderHudTexture(g, gun, ix, iy, (float)WeaponConfig.iconScale);
      } else {
         int size = Math.min(boxW, boxH) - 8;
         g.renderItem(gun, boxX + (boxW - size) / 2, boxY + (boxH - size) / 2);
      }

      if (switchGlowAlpha > 0.01F) {
         GlowEffect.drawGlowRect(g, boxX, boxY, boxW, boxH, WeaponConfig.weaponGlowColor, switchGlowAlpha, 3.0F);
      }
   }

   private void drawSelectedGlow(GuiGraphics g, int x, int y, int w, int h, float switchGlowAlpha) {
      if (WeaponConfig.selGlowEnabled && WeaponConfig.selGlowOpacity > 0) {
         float a = (float)WeaponConfig.selGlowOpacity / 100.0F;
         if (WeaponConfig.selGlowPulse) {
            double phase = (double)(System.currentTimeMillis() % (long)WeaponConfig.selGlowPulseMs) / (double)WeaponConfig.selGlowPulseMs;
            float wave = 0.5F - 0.5F * (float)Math.cos(phase * Math.PI * 2.0);
            a *= 1.0F - (float)WeaponConfig.selGlowPulseDepth / 100.0F * (1.0F - wave);
         }

         a += (float)WeaponConfig.selGlowSwitchBoost / 100.0F * Math.max(0.0F, switchGlowAlpha) * 0.5F;
         a = Math.min(1.0F, a * ((float)HudConfig.glowIntensity / 100.0F));
         if (!(a <= 0.003F)) {
            float r = (float)WeaponConfig.selGlowRadius;
            int c = WeaponConfig.weaponGlowColor;
            glowCorner(g, (float)x, (float)y, r, c, a, WeaponConfig.selGlowTopLeft, 1);
            glowCorner(g, (float)(x + w), (float)y, r, c, a, WeaponConfig.selGlowTopRight, 2);
            glowCorner(g, (float)(x + w), (float)(y + h), r, c, a, WeaponConfig.selGlowBottomRight, 4);
            glowCorner(g, (float)x, (float)(y + h), r, c, a, WeaponConfig.selGlowBottomLeft, 8);
         }
      }
   }

   private static void glowCorner(GuiGraphics g, float cx, float cy, float radius, int rgb, float alpha, int strength, int quarter) {
      if (strength > 0) {
         float k = (float)strength / 100.0F;
         SoftDraw.radial(g, cx, cy, radius * (0.6F + 0.4F * k), rgb, 0.5F * alpha * k, true, quarter);
      }
   }

   private void drawAmmoGlow(GuiGraphics g, int x, int y, int w, int h, int rowY, int rowH, boolean low) {
      this.haloAlpha = 0.0F;
      if (WeaponConfig.ammoGlow) {
         float pulse = 1.0F;
         if (WeaponConfig.ammoGlowPulse) {
            float wave = 0.5F - 0.5F * (float)Math.cos((double)System.currentTimeMillis() / 700.0);
            pulse = 0.6F + 0.4F * wave;
         }

         int glow = WeaponConfig.weaponGlowColor;
         if (low) {
            float wave = 0.5F + 0.5F * (float)Math.sin((double)System.currentTimeMillis() / 260.0);
            glow = WeaponConfig.ammoGlowLowPulse ? lerpRgb(WeaponConfig.weaponGlowColor, WeaponConfig.ammoLowColor, wave) : WeaponConfig.ammoLowColor;
            if (WeaponConfig.ammoGlowLowPulse) {
               pulse = 0.75F + 0.25F * wave;
            }
         }

         float k = (float)HudConfig.glowIntensity / 100.0F;
         if (WeaponConfig.ammoGlowBox > 0) {
            float a = (float)WeaponConfig.ammoGlowBox / 100.0F * pulse * k;
            float reach = Math.max(0.0F, (float)WeaponConfig.ammoGlowSpread);
            float cx = (float)x + (float)w * 0.45F;
            float cy = (float)rowY + (float)rowH * 0.5F;
            float ry = (float)h * 0.4F + reach;
            float rx = (float)w * 0.78F + reach;
            PoseStack ps = g.pose();
            ps.pushPose();
            ps.translate(cx, cy, 0.0F);
            ps.scale(rx / ry, 1.0F, 1.0F);
            SoftDraw.radial(g, 0.0F, 0.0F, ry, glow, Math.min(0.9F, a * 0.9F), false);
            ps.popPose();
         }

         this.haloRgb = low ? glow : WeaponConfig.weaponGlowColor;
         this.haloAlpha = WeaponConfig.ammoGlowHalo > 0 ? (float)WeaponConfig.ammoGlowOpacity / 100.0F * pulse * k : 0.0F;
      }
   }

   private void drawTextHalo(GuiGraphics g, Font font, String text, float x, float y, float scale, int rgb, float alpha, int radiusPx) {
      if (!(alpha <= 0.004F) && radiusPx > 0 && !text.isEmpty()) {
         int[][] dirs = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
         int count = HudConfig.glowQuality == GlowQuality.LOW ? 4 : 8;
         int rings = HudConfig.glowQuality == GlowQuality.HIGH ? 2 : 1;
         HudBatch.run(g, () -> {
            for (int ring = 1; ring <= rings; ring++) {
               float r = (float)radiusPx * scale * (rings == 1 ? 0.75F : (ring == 1 ? 0.5F : 1.0F));
               float a = alpha * (rings == 1 ? 0.22F : (ring == 1 ? 0.3F : 0.16F));
               int argb = Math.min(255, Math.round(a * 255.0F)) << 24 | rgb & 16777215;
               if (argb >>> 24 != 0) {
                  for (int i = 0; i < count; i++) {
                     int[] d = dirs[i];
                     float diag = d[0] != 0 && d[1] != 0 ? 0.7071F : 1.0F;
                     this.drawScaledText(g, font, text, x + (float)d[0] * r * diag, y + (float)d[1] * r * diag, scale, argb, false);
                  }
               }
            }
         });
      }
   }

   private void drawSecondaryWeapon(GuiGraphics g, LocalPlayer player, int iconBoxX, int stripY) {
      if (this.secondaryGunId != null) {
         ItemStack secondaryStack = this.findInHotbar(player.getInventory(), this.secondaryGunId);
         if (secondaryStack == null) {
            this.secondaryGunId = null;
         } else {
            this.drawCarriedIcon(g, player, secondaryStack, iconBoxX, stripY, WeaponConfig.secondaryAlpha);
         }
      }
   }

   private void drawNeutralIcon(GuiGraphics g, LocalPlayer player, ResourceLocation gunId, int boxX, int boxY) {
      ItemStack stack = this.findInHotbar(player.getInventory(), gunId);
      if (stack != null) {
         this.drawCarriedIcon(g, player, stack, boxX, boxY, WeaponConfig.secondaryAlpha);
      }
   }

   private void drawCarriedIcon(GuiGraphics g, LocalPlayer player, ItemStack stack, int boxX, int boxY, double alphaMul) {
      int boxW = 64;
      int boxH = 16;
      this.drawSecondaryChipBox(g, boxX, boxY, boxW, boxH, alphaMul);
      float[] curColor = RenderSystem.getShaderColor();
      float[] prevColor = new float[]{curColor[0], curColor[1], curColor[2], curColor[3]};

      try {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float)alphaMul);
         if (TaczCompat.hasHudTexture(stack)) {
            int[] size = TaczCompat.iconSize((float)WeaponConfig.secondaryIconScale);
            int ix = boxX + boxW - size[0] - 2;
            int iy = boxY + (boxH - size[1]) / 2;
            TaczCompat.renderHudTexture(g, stack, ix, iy, (float)WeaponConfig.secondaryIconScale);
         } else {
            int size = Math.min(boxH - 4, 20);
            g.renderItem(stack, boxX + boxW - size - 2, boxY + (boxH - size) / 2);
         }
      } finally {
         RenderSystem.setShaderColor(prevColor[0], prevColor[1], prevColor[2], prevColor[3]);
      }

      if (WeaponConfig.showReserveAmmo) {
         int reserve = TaczCompat.reserveAmmo(player, stack);
         String text = reserve == -1 ? "∞" : (reserve == Integer.MIN_VALUE ? "" : String.valueOf(reserve));
         if (!text.isEmpty()) {
            Font font = Minecraft.getInstance().font;
            int tx = boxX + 4;
            int ty = boxY + (boxH - 9) / 2;
            int a = Math.max(1, (int)Math.round(alphaMul * 255.0));
            int color = a << 24 | WeaponConfig.reserveColor & 16777215;
            HudText.text(g, font, text, (float)tx, (float)ty, color, true);
         }
      }
   }

   private ItemStack findInHotbar(Inventory inventory, ResourceLocation gunId) {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = inventory.getItem(i);
         if (!stack.isEmpty() && gunId.equals(TaczCompat.gunId(stack))) {
            return stack;
         }
      }

      return null;
   }

   static float effectiveScale(GuiGraphics g) {
      return HudText.snapElement(g, WeaponConfig.scale);
   }

   static int gearWidth() {
      return LoadoutGear.extraWidth();
   }

   static int[] resolvePosition(int screenWidth, int screenHeight, int width, int height, double scale) {
      int scaledWidth = (int)Math.round((double)width * scale);
      int scaledHeight = (int)Math.round((double)height * scale);
      int x;
      int y;
      switch (WeaponConfig.position) {
         case TOP_LEFT:
            x = WeaponConfig.offsetX;
            y = WeaponConfig.offsetY;
            break;
         case TOP_RIGHT:
            x = screenWidth - scaledWidth - WeaponConfig.offsetX;
            y = WeaponConfig.offsetY;
            break;
         case BOTTOM_LEFT:
            x = WeaponConfig.offsetX;
            y = screenHeight - scaledHeight - WeaponConfig.offsetY;
            break;
         case CUSTOM:
            x = WeaponConfig.offsetX;
            y = WeaponConfig.offsetY;
            break;
         default:
            x = screenWidth - scaledWidth - WeaponConfig.offsetX;
            y = screenHeight - scaledHeight - WeaponConfig.offsetY;
      }

      return new int[]{x, y};
   }

   private static record AmmoRow(
      String magText,
      int magColor,
      boolean low,
      float ammoScale,
      float reserveScale,
      String reserveText,
      boolean hasReserve,
      float magW,
      float sepW,
      float reserveW,
      int ammoRowH,
      int reserveRowH,
      int iconW,
      int iconGap,
      float ammoRowW,
      int reserveColor
   ) {
   }
}
