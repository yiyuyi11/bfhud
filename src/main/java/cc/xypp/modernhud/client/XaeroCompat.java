package cc.xypp.modernhud.client;

import cc.xypp.modernhud.config.ExtraHudConfig;

/** Optional Xaero's Minimap bridge: reports the vertical space the minimap occupies. */
public final class XaeroCompat {
   private static Boolean present;

   private XaeroCompat() {
   }

   public static boolean loaded() {
      if (present == null) {
         try {
            Class.forName("xaero.hud.minimap.Minimap");
            present = Boolean.TRUE;
         } catch (Throwable ex) {
            present = Boolean.FALSE;
         }
      }
      return present;
   }

   /** Minimap height plus the configured gap, or 0 when Xaero is absent or avoidance is off. */
   public static int offset() {
      if (!loaded() || !HudAnchors.avoidMinimap()) return 0;
      int height;
      int margin;
      try {
         height = ExtraHudConfig.XAERO_HEIGHT.get();
         margin = ExtraHudConfig.XAERO_MARGIN.get();
      } catch (Throwable ex) {
         return 0;
      }
      return height + margin;
   }
}
