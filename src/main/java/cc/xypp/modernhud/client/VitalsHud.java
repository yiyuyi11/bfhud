package cc.xypp.modernhud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

/**
 * Vitals block, bottom-right.
 *
 * <p>Two states:
 * <ul>
 *   <li>full health - three hairlines only: health, then armour and hunger side by side under it;</li>
 *   <li>damaged - one bar where the missing part is a translucent black and the remaining health is solid
 *       red, filled from the right edge, with the white value and plus sign sitting inside the bar on the
 *       right, vertically centred.</li>
 * </ul>
 */
public final class VitalsHud implements IGuiOverlay {
   private static final int BAR_W = 104;
   /** Height of the health bar while hurt; tall enough to hold the value inside it. */
   private static final int BAR_H = 11;
   /** Hairline height shared by every bar when nothing is missing. */
   private static final int THIN = 2;
   private static final int GAP = 2;
   private static final int MARGIN_RIGHT = 8;
   private static final int MARGIN_BOTTOM = 6;

   private static final int HEALTH = 0xffc42424;
   /** Undamaged bar: white, so a full-health readout is silent instead of alarming. */
   private static final int HEALTH_FULL = 0xffeeeff2;
   private static final net.minecraft.resources.ResourceLocation MARKER_TEXTURE =
         new net.minecraft.resources.ResourceLocation("modernhud", "textures/gui/health_marker.png");

   private static final int HEALTH_LOW = 0xff8f1a1a;
   /** Empty part of the bar: black at just under half opacity, so the world shows through faintly. */
   private static final int EMPTY = 0x73000000;
   private static final int ARMOUR = 0xff5c8fc7;
   private static final int HUNGER = 0xffc9a227;

   private VitalsHud() {
   }

   public static IGuiOverlay create() {
      return new VitalsHud();
   }

   @Override
   public void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player == null || player.isSpectator() || mc.options.hideGui) return;
      if (player.getRootVehicle() != player) return;
      // The deployment screen draws its own layout; a floating vitals block in the corner would sit on top
      // of its panels. Checked by class name because battlefield_ops is an optional dependency.
      if (mc.screen != null && mc.screen.getClass().getName().startsWith("cn.bfops.")) return;

      float health = player.getHealth();
      float maxHealth = Math.max(20.0F, player.getMaxHealth());
      float ratio = Math.max(0.0F, Math.min(1.0F, health / maxHealth));
      boolean hurt = ratio < 0.999F;

      int healthRow = hurt ? BAR_H : THIN;
      int height = healthRow + GAP + THIN;

      int left = Math.max(2, screenWidth - MARGIN_RIGHT - BAR_W);
      int top = Math.max(2, screenHeight - MARGIN_BOTTOM - height);

      if (hurt) {
         int fill = Math.round(BAR_W * ratio);
         graphics.fill(left, top, left + BAR_W, top + BAR_H, EMPTY);
         if (fill > 0) {
            graphics.fill(left + BAR_W - fill, top, left + BAR_W, top + BAR_H,
                    ratio <= 0.3F ? HEALTH_LOW : HEALTH);
         }
      String value = String.valueOf(Math.round(health));
      // The "+" suffix became a framed box: as a glyph it read as part of the number, and a box with a gap
      // says "this is a unit marker" instead.
      int boxSize = 9;
      int gapAfterNumber = 5;
      int totalWidth = mc.font.width(value) + gapAfterNumber + boxSize;
      int textX = left + BAR_W - 3 - totalWidth;
      int textY = top + (BAR_H - mc.font.lineHeight) / 2 + 1;
      graphics.drawString(mc.font, value, textX, textY, 0xffffffff, false);
      int boxX = textX + mc.font.width(value) + gapAfterNumber;
      int boxY = top + (BAR_H - boxSize) / 2;
      // The unit marker is its own texture now, so its look is editable without touching layout code.
      graphics.blit(MARKER_TEXTURE, boxX, boxY, 0, 0, boxSize, boxSize, boxSize, boxSize);
      } else {
         // Undamaged state reads as a plain white hairline; red is reserved for "you are hurt".
         BarRender.bar(graphics, left, top, BAR_W, THIN, 1.0F, HEALTH_FULL);
      }

      int y = top + healthRow + GAP;
      int half = (BAR_W - GAP) / 2;
      BarRender.bar(graphics, left, y, half, THIN, player.getArmorValue() / 20.0F, ARMOUR);
      BarRender.bar(graphics, left + half + GAP, y, half, THIN,
              player.getFoodData().getFoodLevel() / 20.0F, HUNGER);
   }
}
