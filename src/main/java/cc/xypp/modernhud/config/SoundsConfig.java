package cc.xypp.modernhud.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class SoundsConfig {
   private static final Builder BUILDER = new Builder();
   public static final ForgeConfigSpec SPEC = BUILDER.build();
   private static final IntValue STYLE = BUILDER.comment("Sound style for all cues: 1 = classic, 2 = style 2, 3 = style 3.")
      .translation("modernhud.config.sound_style")
      .defineInRange("style", 1, 1, 3);
   private static final IntValue HIT_STYLE = BUILDER.comment("Style for the hit cue only. 0 uses the style above.")
      .translation("modernhud.config.sound_hit_style")
      .defineInRange("hitStyle", 0, 0, 3);
   private static final IntValue KILL_STYLE = BUILDER.comment("Style for the kill cue only. 0 uses the style above.")
      .translation("modernhud.config.sound_kill_style")
      .defineInRange("killStyle", 0, 0, 3);
   private static final IntValue HEADSHOT_STYLE = BUILDER.comment("Style for the headshot-kill cue only. 0 uses the style above.")
      .translation("modernhud.config.sound_headshot_style")
      .defineInRange("headshotKillStyle", 0, 0, 3);
   private static final IntValue BREAK_STYLE = BUILDER.comment(
         new String[]{"Style for the shield-break cue only. Styles 1 and 2 share one sound, 3 has its own. 0", "uses the style above."}
      )
      .translation("modernhud.config.sound_break_style")
      .defineInRange("shieldBreakStyle", 0, 0, 3);
   private static final BooleanValue HIT_ON = BUILDER.comment("Hear the hit cue.").translation("modernhud.config.sound_hit_on").define("enabled", true);
   private static final DoubleValue HIT_VOL = BUILDER.comment("Volume, 0.0-2.0 (Minecraft clamps the final loudness at full).")
      .translation("modernhud.config.sound_hit_volume")
      .defineInRange("volume", 1.0, 0.0, 2.0);
   private static final DoubleValue HIT_PITCH = BUILDER.comment("Pitch, 0.5-2.0.")
      .translation("modernhud.config.sound_hit_pitch")
      .defineInRange("pitch", 1.0, 0.5, 2.0);
   private static final BooleanValue KILL_ON = BUILDER.comment("Hear the kill cue.").translation("modernhud.config.sound_kill_on").define("enabled", true);
   private static final DoubleValue KILL_VOL = BUILDER.comment("Volume, 0.0-2.0.")
      .translation("modernhud.config.sound_kill_volume")
      .defineInRange("volume", 1.0, 0.0, 2.0);
   private static final DoubleValue KILL_PITCH = BUILDER.comment("Pitch, 0.5-2.0.")
      .translation("modernhud.config.sound_kill_pitch")
      .defineInRange("pitch", 1.0, 0.5, 2.0);
   private static final BooleanValue HEADSHOT_ON = BUILDER.comment("Hear the headshot-kill cue.")
      .translation("modernhud.config.sound_headshot_on")
      .define("enabled", true);
   private static final DoubleValue HEADSHOT_VOL = BUILDER.comment("Volume, 0.0-2.0.")
      .translation("modernhud.config.sound_headshot_volume")
      .defineInRange("volume", 1.0, 0.0, 2.0);
   private static final DoubleValue HEADSHOT_PITCH = BUILDER.comment("Pitch, 0.5-2.0.")
      .translation("modernhud.config.sound_headshot_pitch")
      .defineInRange("pitch", 1.0, 0.5, 2.0);
   private static final BooleanValue SHIELD_HIT_ON = BUILDER.comment("Hear the shield-hit cue.")
      .translation("modernhud.config.sound_shield_hit_on")
      .define("enabled", true);
   private static final DoubleValue SHIELD_HIT_VOL = BUILDER.comment("Volume, 0.0-2.0.")
      .translation("modernhud.config.sound_shield_hit_volume")
      .defineInRange("volume", 1.0, 0.0, 2.0);
   private static final DoubleValue SHIELD_HIT_PITCH = BUILDER.comment("Pitch, 0.5-2.0.")
      .translation("modernhud.config.sound_shield_hit_pitch")
      .defineInRange("pitch", 1.0, 0.5, 2.0);
   private static final BooleanValue SHIELD_BREAK_ON = BUILDER.comment("Hear the shield-break cue.")
      .translation("modernhud.config.sound_shield_break_on")
      .define("enabled", true);
   private static final DoubleValue SHIELD_BREAK_VOL = BUILDER.comment("Volume, 0.0-2.0.")
      .translation("modernhud.config.sound_shield_break_volume")
      .defineInRange("volume", 2.0, 0.0, 2.0);
   private static final DoubleValue SHIELD_BREAK_PITCH = BUILDER.comment("Pitch, 0.5-2.0.")
      .translation("modernhud.config.sound_shield_break_pitch")
      .defineInRange("pitch", 1.0, 0.5, 2.0);
   private static final BooleanValue OVERRIDE_OTHERS = BUILDER.comment(
         new String[]{"Cancel sounds from TACZ and SuperbWarfare that match the patterns below, so their hit", "and kill cues do not play over yours."}
      )
      .translation("modernhud.config.sound_override")
      .define("enabled", true);
   private static final ConfigValue<String> OVERRIDE_PATTERNS = BUILDER.comment(
         new String[]{
            "Sound ids to silence, separated by commas. * is a wildcard, for example tacz:*kill*.",
            "If a sound still plays, turn on debugLog, trigger it and copy its id from",
            "logs/latest.log (lines starting with ModernHud sound:)."
         }
      )
      .translation("modernhud.config.sound_override_patterns")
      .define(
         "patterns",
         "tacz:*kill*,tacz:*headshot*,tacz:*hit_mark*,tacz:*hitmarker*,superbwarfare:*indication*,superbwarfare:*headshot*,superbwarfare:*kill*,superbwarfare:*hitmarker*,superbwarfare:hit"
      );
   private static final BooleanValue DEBUG_LOG = BUILDER.comment(
         new String[]{"Write the id of every sound played by TACZ or SuperbWarfare to logs/latest.log, to", "find out what to silence."}
      )
      .translation("modernhud.config.sound_debug_log")
      .define("debugLog", false);
   public static int style;
   public static int hitStyle;
   public static int killStyle;
   public static int headshotStyle;
   public static int breakStyle;
   public static boolean hitOn;
   public static boolean killOn;
   public static boolean headshotOn;
   public static boolean shieldHitOn;
   public static boolean shieldBreakOn;
   public static float hitVolume;
   public static float hitPitch;
   public static float killVolume;
   public static float killPitch;
   public static float headshotVolume;
   public static float headshotPitch;
   public static float shieldHitVolume;
   public static float shieldHitPitch;
   public static float shieldBreakVolume;
   public static float shieldBreakPitch;
   public static boolean overrideOthers;
   public static boolean debugLog;
   private static volatile List<Pattern> overridePatterns = List.of();

   public static int styleFor(int override) {
      return override > 0 ? override : style;
   }

   public static boolean shouldOverride(String id) {
      for (Pattern p : overridePatterns) {
         if (p.matcher(id).matches()) {
            return true;
         }
      }

      return false;
   }

   private static List<Pattern> compile(String csv) {
      List<Pattern> out = new ArrayList<>();

      for (String part : csv.split(",")) {
         String glob = part.trim().toLowerCase();
         if (!glob.isEmpty()) {
            StringBuilder re = new StringBuilder();

            for (char c : glob.toCharArray()) {
               if (c == '*') {
                  re.append(".*");
               } else {
                  re.append(Pattern.quote(String.valueOf(c)));
               }
            }

            out.add(Pattern.compile(re.toString()));
         }
      }

      return out;
   }

   @SubscribeEvent
   static void onLoad(ModConfigEvent event) {
      if (event.getConfig().getSpec() == SPEC) {
         refresh();
      }
   }

   public static void refresh() {
      style = (Integer)STYLE.get();
      hitStyle = (Integer)HIT_STYLE.get();
      killStyle = (Integer)KILL_STYLE.get();
      headshotStyle = (Integer)HEADSHOT_STYLE.get();
      breakStyle = (Integer)BREAK_STYLE.get();
      hitOn = (Boolean)HIT_ON.get();
      hitVolume = ((Double)HIT_VOL.get()).floatValue();
      hitPitch = ((Double)HIT_PITCH.get()).floatValue();
      killOn = (Boolean)KILL_ON.get();
      killVolume = ((Double)KILL_VOL.get()).floatValue();
      killPitch = ((Double)KILL_PITCH.get()).floatValue();
      headshotOn = (Boolean)HEADSHOT_ON.get();
      headshotVolume = ((Double)HEADSHOT_VOL.get()).floatValue();
      headshotPitch = ((Double)HEADSHOT_PITCH.get()).floatValue();
      shieldHitOn = (Boolean)SHIELD_HIT_ON.get();
      shieldHitVolume = ((Double)SHIELD_HIT_VOL.get()).floatValue();
      shieldHitPitch = ((Double)SHIELD_HIT_PITCH.get()).floatValue();
      shieldBreakOn = (Boolean)SHIELD_BREAK_ON.get();
      shieldBreakVolume = ((Double)SHIELD_BREAK_VOL.get()).floatValue();
      shieldBreakPitch = ((Double)SHIELD_BREAK_PITCH.get()).floatValue();
      overrideOthers = (Boolean)OVERRIDE_OTHERS.get();
      overridePatterns = compile((String)OVERRIDE_PATTERNS.get());
      debugLog = (Boolean)DEBUG_LOG.get();
   }

   static {
      BUILDER.comment(new String[]{"Sound style used by the cues: 1 = classic, 2 = style 2, 3 = style 3. A cue's own style", "overrides this unless it is 0."})
         .push("style");
      BUILDER.pop();
      BUILDER.comment("Ordinary hit (no shield involved)").push("hit");
      BUILDER.pop();
      BUILDER.comment("Kill").push("kill");
      BUILDER.pop();
      BUILDER.comment("Headshot kill").push("headshotKill");
      BUILDER.pop();
      BUILDER.comment("A hit that lands on an active shield. Plays instead of the ordinary hit cue.").push("shieldHit");
      BUILDER.pop();
      BUILDER.comment("Shield fully broken").push("shieldBreak");
      BUILDER.pop();
      BUILDER.comment("Silence the hit and kill sounds of other mods so only these cues are heard.").push("override");
      BUILDER.pop();
   }
}
