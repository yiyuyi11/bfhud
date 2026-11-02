package cc.xypp.modernhud.config;

import java.util.LinkedHashSet;
import java.util.Set;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public final class ConfigPresets {
   public static final String[] NAMES = new String[]{"default", "performance", "minimal"};

   private ConfigPresets() {
   }

   private static ConfigPresets.Setting s(ModConfigSpec spec, String path, Object value) {
      return new ConfigPresets.Setting(spec, path, value);
   }

   private static ConfigPresets.Setting[] performance() {
      return new ConfigPresets.Setting[]{
         s(HudConfig.SPEC, "animation.glowQuality", "LOW"),
         s(HudConfig.SPEC, "hpText.glowEnabled", false),
         s(WeaponConfig.SPEC, "ammoGlow.haloRadius", 0),
         s(WeaponConfig.SPEC, "ammoGlow.pulse", false),
         s(WeaponConfig.SPEC, "ammoGlow.lowAmmoPulse", false),
         s(WeaponConfig.SPEC, "gear.selectedGlowPulse", false),
         s(HitIndicationConfig.SPEC, "hitIndication.glow", false),
         s(BossBarConfig.SPEC, "bossBar.barGlow", false),
         s(BossBarConfig.SPEC, "bossBar.headGlow", false),
         s(BossBarConfig.SPEC, "bossBar.damageTrail", false),
         s(KillstreakConfig.SPEC, "perkToast.glow", false)
      };
   }

   private static ConfigPresets.Setting[] minimal() {
      return new ConfigPresets.Setting[]{
         s(HudConfig.SPEC, "teamHud.enabled", false),
         s(KillstreakConfig.SPEC, "killstreak.enabled", false),
         s(KillstreakConfig.SPEC, "perkToast.enabled", false),
         s(BossBarConfig.SPEC, "bossBar.enabled", false),
         s(WeaponConfig.SPEC, "ammoGlow.enabled", false),
         s(WeaponConfig.SPEC, "gear.selectedGlow", false)
      };
   }

   public static int apply(String name) {
      Set<ModConfigSpec> touched = new LinkedHashSet<>();
      int changed = 0;
      switch (name) {
         case "default":
            for (ModConfigSpec spec : all()) {
               int n = ConfigPaths.resetAll(spec);
               if (n > 0) {
                  touched.add(spec);
                  changed += n;
               }
            }
            break;
         case "performance":
         case "minimal":
            for (ConfigPresets.Setting st : name.equals("performance") ? performance() : minimal()) {
               ConfigValue<?> cv = ConfigPaths.find(st.spec(), st.path());
               if (cv != null && ConfigPaths.set(st.spec(), st.path(), st.value())) {
                  touched.add(st.spec());
                  changed++;
               }
            }
            break;
         default:
            return -1;
      }

      for (ModConfigSpec specx : touched) {
         ConfigPaths.save(specx);
      }

      ConfigRefresh.refreshAll();
      return changed;
   }

   public static ModConfigSpec[] all() {
      return new ModConfigSpec[]{
         HudConfig.SPEC, WeaponConfig.SPEC, KillstreakConfig.SPEC, BossBarConfig.SPEC, HitIndicationConfig.SPEC, CompassConfig.SPEC, SoundsConfig.SPEC
      };
   }

   private static record Setting(ModConfigSpec spec, String path, Object value) {
   }
}
