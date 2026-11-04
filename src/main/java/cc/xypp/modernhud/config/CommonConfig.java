package cc.xypp.modernhud.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class CommonConfig {
   private static final Builder BUILDER = new Builder();
   private static final BooleanValue ENABLE_HIT_FEEDBACK = BUILDER.comment(
         new String[]{"Notify the attacker and the victim (sound only) when a hit lands on Absorption without", "depleting it."}
      )
      .translation("modernhud.config.enable_hit_feedback")
      .define("enableHitFeedback", true);
   private static final BooleanValue ENABLE_BREAK_FEEDBACK = BUILDER.comment(
         new String[]{"Notify the attacker (sound and icon) and the victim (sound only) when Absorption is", "fully depleted."}
      )
      .translation("modernhud.config.enable_break_feedback")
      .define("enableBreakFeedback", true);
   private static final BooleanValue CLEAR_ABSORPTION_ON_EFFECT_END = BUILDER.comment(
         new String[]{
            "In vanilla, absorption hearts stay after the Absorption effect ends. With this on they",
            "are cleared when the effect ends, so the HUD bar clears with it. Turn it off for",
            "vanilla behavior."
         }
      )
      .translation("modernhud.config.clear_absorption_on_effect_end")
      .define("clearAbsorptionOnEffectEnd", true);
   private static final BooleanValue COMBAT_SOUNDS = BUILDER.comment("Master switch for the hit / kill / headshot-kill cues.")
      .translation("modernhud.config.combat_sounds")
      .define("enabled", true);
   private static final BooleanValue HIT_SOUND = BUILDER.comment("Play a cue to the attacker on a non-fatal hit.")
      .translation("modernhud.config.combat_sound_hit")
      .define("hitSound", true);
   private static final BooleanValue KILL_SOUND = BUILDER.comment("Play a cue to the attacker on a kill.")
      .translation("modernhud.config.combat_sound_kill")
      .define("killSound", true);
   private static final BooleanValue HEADSHOT_KILL_SOUND = BUILDER.comment(
         "Play a cue on a headshot kill. Only TACZ reports headshots. It replaces the kill cue."
      )
      .translation("modernhud.config.combat_sound_headshot")
      .define("headshotKillSound", true);
   private static final BooleanValue SHIELD_PRIORITY = BUILDER.comment(
         new String[]{
            "While the victim still has Absorption, play the shield cue (shield hit or break)",
            "instead of the hit cue. Once the shield is gone the hit cue plays. A killing blow",
            "plays the kill cue and no shield cue."
         }
      )
      .translation("modernhud.config.combat_sound_shield_priority")
      .define("shieldPriority", true);
   private static final BooleanValue RESTRICT_TO_TACZ = BUILDER.comment(
         new String[]{
            "true: only TACZ damage triggers hit and kill cues. false: any damage a player deals",
            "does (melee, bows, SuperbWarfare, other mods). Headshots always need TACZ."
         }
      )
      .translation("modernhud.config.combat_sound_restrict_tacz")
      .define("restrictToTacz", false);
   private static final IntValue HIT_COOLDOWN = BUILDER.comment(
         new String[]{"Minimum time between hit cues for one player in milliseconds. Prevents sound spam from", "fast guns. 0 means none."}
      )
      .translation("modernhud.config.combat_sound_hit_cooldown")
      .defineInRange("hitCooldownMs", 20, 0, 2000);
   private static final IntValue KILL_COOLDOWN = BUILDER.comment("Minimum time (ms) between kill cues per player (softens multi-kills). 0 = none.")
      .translation("modernhud.config.combat_sound_kill_cooldown")
      .defineInRange("killCooldownMs", 0, 0, 2000);
   public static final ForgeConfigSpec SPEC = BUILDER.build();
   public static boolean enableHitFeedback;
   public static boolean enableBreakFeedback;
   public static boolean clearAbsorptionOnEffectEnd;
   public static boolean combatSoundsEnabled;
   public static boolean hitSoundEnabled;
   public static boolean killSoundEnabled;
   public static boolean headshotKillSoundEnabled;
   public static boolean shieldSoundPriority;
   public static boolean restrictCombatSoundsToTacz;
   public static int hitSoundCooldownMs;
   public static int killSoundCooldownMs;

   @SubscribeEvent
   static void onLoad(ModConfigEvent event) {
      try {
         refresh();
      } catch (IllegalStateException var2) {
      }
   }

   public static void refresh() {
      enableHitFeedback = (Boolean)ENABLE_HIT_FEEDBACK.get();
      enableBreakFeedback = (Boolean)ENABLE_BREAK_FEEDBACK.get();
      clearAbsorptionOnEffectEnd = (Boolean)CLEAR_ABSORPTION_ON_EFFECT_END.get();
      combatSoundsEnabled = (Boolean)COMBAT_SOUNDS.get();
      hitSoundEnabled = (Boolean)HIT_SOUND.get();
      killSoundEnabled = (Boolean)KILL_SOUND.get();
      headshotKillSoundEnabled = (Boolean)HEADSHOT_KILL_SOUND.get();
      shieldSoundPriority = (Boolean)SHIELD_PRIORITY.get();
      restrictCombatSoundsToTacz = (Boolean)RESTRICT_TO_TACZ.get();
      hitSoundCooldownMs = (Integer)HIT_COOLDOWN.get();
      killSoundCooldownMs = (Integer)KILL_COOLDOWN.get();
   }

   static {
      BUILDER.comment(new String[]{"Server-side detection of Absorption being hit or depleted. Only the attacker and the", "victim are notified."})
         .push("shieldFeedback");
      BUILDER.pop();
      BUILDER.comment(
            new String[]{
               "Hit, kill and headshot-kill cues for the attacker. The server decides when they play;",
               "each player picks style, volume and pitch in their own Sounds settings. Shield cues",
               "use the shieldFeedback options above."
            }
         )
         .push("combatSounds");
      BUILDER.pop();
   }
}
