package cc.xypp.modernhud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Vitals: health, armour and hunger as three stacked bars.
 *
 * <p>Dependency-free on purpose: plain fills and the vanilla font, no config reads and none of the original
 * widget plumbing. The original {@code PlayerHudOverlay} kept disappearing behind one of its own feature
 * flags, so this guarantees a visible readout at the bottom-left, above nothing else.
 */
public final class StatusHud implements IGuiOverlay {
   private static int barWidth() {
      try {
         return cc.xypp.modernhud.config.ExtraHudConfig.STATUS_WIDTH.get();
      } catch (Throwable notLoaded) {
         return 120;
      }
   }

   private static int offsetX() {
      try {
         return cc.xypp.modernhud.config.ExtraHudConfig.STATUS_X.get();
      } catch (Throwable notLoaded) {
         return 6;
      }
   }

   private static int offsetY() {
      try {
         return cc.xypp.modernhud.config.ExtraHudConfig.STATUS_Y.get();
      } catch (Throwable notLoaded) {
         return 2;
      }
   }

   private static final int BAR_H = 6;
   private static final int GAP = 2;
   private static final int MARGIN_LEFT = 6;
   private static final int MARGIN_BOTTOM = 2;
   /** Minimap height plus a small gap; keeps the bars off the map. */
   private static final int MINIMAP_CLEAR = 136;

   private StatusHud() {
   }

   public static IGuiOverlay create() {
      return new StatusHud();
   }

   @Override
   public void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.options.hideGui) return;

      var player = mc.player;
      int height = BAR_H * 3 + GAP * 2;
      int BAR_W = barWidth();
      int left = offsetX();
      // Directly below the minimap: flush to the bottom edge, inside whatever gap the map leaves there.
      int top = Math.max(0, screenHeight - offsetY() - height);

      graphics.fill(left - 2, top - 2, left + BAR_W + 2, top + height + 2, 0x70101820);

      float health = player.getHealth();
      float maxHealth = Math.max(1.0F, player.getMaxHealth());
      bar(graphics, left, top, BAR_W, BAR_H, health / maxHealth, 0xffd94f4f, 0xffe88080);

      float armour = player.getArmorValue() / 20.0F;
      bar(graphics, left, top + BAR_H + GAP, BAR_W, BAR_H, armour, 0xff5c8fc7, 0xff8fb6dd);

      int food = player.getFoodData().getFoodLevel();
      bar(graphics, left, top + (BAR_H + GAP) * 2, BAR_W, BAR_H, food / 20.0F, 0xffc9a227, 0xffe0c060);

      String text = Math.round(health) + "/" + Math.round(maxHealth);
      graphics.drawString(mc.font, text, left + BAR_W - mc.font.width(text),
              top + height + 1, 0xffd8dde0, true);
   }

   private void bar(GuiGraphics graphics, int x, int y, int w, int h, float fraction, int colour, int topColour) {
      float value = Math.max(0.0F, Math.min(1.0F, fraction));
      graphics.fill(x, y, x + w, y + h, 0x60101820);
      int filled = Math.round(w * value);
      if (filled <= 0) return;
      graphics.fill(x, y, x + filled, y + h, colour);
      graphics.fill(x, y, x + filled, y + 1, topColour);
   }
}
