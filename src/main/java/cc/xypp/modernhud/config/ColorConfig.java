package cc.xypp.modernhud.config;

import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public final class ColorConfig {
   private static volatile boolean MIGRATING;

   private ColorConfig() {
   }

   public static String toHex(int rgb) {
      return String.format("%06X", rgb & 16777215);
   }

   public static int fromHex(String value, int fallbackRgb) {
      if (value == null) {
         return fallbackRgb;
      } else {
         String s = value.trim();
         if (s.startsWith("#")) {
            s = s.substring(1);
         } else if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
         }

         if (!s.isEmpty() && s.length() <= 6 && s.matches("[0-9a-fA-F]+")) {
            try {
               return Integer.parseInt(s, 16) & 16777215;
            } catch (NumberFormatException var4) {
               return fallbackRgb;
            }
         } else {
            return fallbackRgb;
         }
      }
   }

   public static void migrateDarkPanel(ConfigValue<String> color, IntValue opacity, int newRgb, int newOpacity) {
      if (!MIGRATING) {
         MIGRATING = true;

         try {
            if ("0A0D11".equalsIgnoreCase(((String)color.get()).replace("#", "").trim())) {
               color.set(toHex(newRgb));
               opacity.set(newOpacity);
               color.save();
            }
         } catch (RuntimeException var8) {
         } finally {
            MIGRATING = false;
         }
      }
   }
}
