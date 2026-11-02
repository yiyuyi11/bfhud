package cc.xypp.modernhud.client;

import cc.xypp.modernhud.config.ExtraHudConfig;
import net.minecraft.client.gui.GuiGraphics;

/** Anchor maths shared by the message bar and the inventory widgets. */
public final class HudAnchors {
   private HudAnchors() {
   }

   /**
    * Resolves an anchor plus offset to a top-left point.
    *
    * <p>Besides the four corners, {@code CENTER_RIGHT}/{@code CENTER_LEFT} pin a widget to the vertical
    * middle of that edge, and any {@code *_CENTER} anchor centres it horizontally.
    */
   public static int[] topLeft(GuiGraphics g, String anchor, int offsetX, int offsetY, int width, int height,
                               boolean belowMinimap) {
      boolean centerX = anchor != null && anchor.endsWith("_CENTER");
      boolean right = anchor != null && anchor.contains("RIGHT");
      boolean bottom = anchor != null && anchor.contains("BOTTOM");
      // Vertical centring only applies to CENTER_LEFT / CENTER_RIGHT; BOTTOM_CENTER is a bottom anchor.
      boolean middle = anchor != null && anchor.startsWith("CENTER");

      int x = centerX ? (g.guiWidth() - width) / 2 : right ? g.guiWidth() - offsetX - width : offsetX;
      int y;
      if (middle) {
         y = (g.guiHeight() - height) / 2 + offsetY;
      } else if (bottom) {
         y = g.guiHeight() - offsetY - height;
      } else {
         y = offsetY + (belowMinimap ? XaeroCompat.offset() : 0);
      }
      return new int[]{x, y};
   }

   /** True when the player is riding anything, including a boat or a minecart. */
   public static boolean riding(net.minecraft.client.player.LocalPlayer player) {
      return player != null && player.getRootVehicle() != player;
   }

   /** Reads the avoidance flag once so the widget code does not repeat the null handling. */
   public static boolean avoidMinimap() {
      try {
         return ExtraHudConfig.AVOID_XAERO.get();
      } catch (Throwable ex) {
         return false;
      }
   }
}
