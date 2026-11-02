package cc.xypp.modernhud.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ConfigRefresh {
   private static final Logger LOGGER = LogManager.getLogger("ModernHud/Config");

   private ConfigRefresh() {
   }

   public static void refreshAll() {
      run("hud", HudConfig::refresh);
      run("weapon", WeaponConfig::refresh);
      run("killstreak", KillstreakConfig::refresh);
      run("bossbar", BossBarConfig::refresh);
      run("hitindication", HitIndicationConfig::refresh);
      run("common", CommonConfig::refresh);
      run("compass", CompassConfig::refresh);
      run("sounds", SoundsConfig::refresh);
   }

   private static void run(String name, Runnable refresh) {
      try {
         refresh.run();
      } catch (RuntimeException var3) {
         LOGGER.debug("Config '{}' not refreshed: {}", name, var3.toString());
      }
   }
}
