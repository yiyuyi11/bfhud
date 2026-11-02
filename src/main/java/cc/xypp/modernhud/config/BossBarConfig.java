package cc.xypp.modernhud.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.config.ModConfigEvent;

public class BossBarConfig {
   private static final Builder BUILDER = new Builder();
   public static final ModConfigSpec SPEC = BUILDER.build();
   private static final BooleanValue ENABLED = BUILDER.comment("Replace vanilla's boss bars with the compact corner version. Off = vanilla look.")
      .translation("modernhud.config.boss_enabled")
      .define("enabled", true);
   private static final EnumValue<HudPosition> POSITION = BUILDER.comment("Screen corner the boss bars are tucked into.")
      .translation("modernhud.config.boss_position")
      .defineEnum("position", HudPosition.TOP_LEFT);
   private static final IntValue OFFSET_X = BUILDER.comment("Horizontal offset in px from that corner's edge.")
      .translation("modernhud.config.boss_offset_x")
      .defineInRange("offsetX", 8, -2000, 4000);
   private static final IntValue OFFSET_Y = BUILDER.comment("Vertical offset in px from that corner's edge.")
      .translation("modernhud.config.boss_offset_y")
      .defineInRange("offsetY", 90, -2000, 4000);
   private static final DoubleValue SCALE = BUILDER.comment("Scale of the whole block. Keep it at a whole number if the pixel font looks uneven.")
      .translation("modernhud.config.boss_scale")
      .defineInRange("scale", 1.0, 0.5, 4.0);
   private static final IntValue BAR_WIDTH = BUILDER.comment("Bar length in pixels, before scaling.")
      .translation("modernhud.config.boss_bar_width")
      .defineInRange("barWidth", 70, 30, 300);
   private static final IntValue BAR_HEIGHT = BUILDER.comment(
         new String[]{"Thickness of the filled part in pixels, before scaling. The empty track stays a thin", "line."}
      )
      .translation("modernhud.config.boss_bar_height")
      .defineInRange("barHeight", 3, 1, 12);
   private static final IntValue BAR_SPACING = BUILDER.comment("Gap between boss bars in pixels, before scaling.")
      .translation("modernhud.config.boss_bar_spacing")
      .defineInRange("barSpacing", 1, 0, 40);
   private static final IntValue MAX_BARS = BUILDER.comment("Most boss bars shown at once; extra ones are simply not drawn.")
      .translation("modernhud.config.boss_max_bars")
      .defineInRange("maxBars", 2, 1, 10);
   private static final BooleanValue SHOW_NUMBER = BUILDER.comment("Show the number next to the bar (see numberMode).")
      .translation("modernhud.config.boss_show_number")
      .define("showNumber", true);
   private static final EnumValue<BossNumberMode> NUMBER_MODE = BUILDER.comment(
         new String[]{
            "PERCENT shows progress from 0 to 100. HEALTH shows exact health when the boss entity",
            "is loaded and matches the bar name, otherwise the percent."
         }
      )
      .translation("modernhud.config.boss_number_mode")
      .defineEnum("numberMode", BossNumberMode.HEALTH);
   private static final BooleanValue PERCENT_SIGN = BUILDER.comment("Append a % to percent numbers.")
      .translation("modernhud.config.boss_percent_sign")
      .define("percentSign", false);
   private static final DoubleValue NUMBER_SCALE = BUILDER.comment("Number text scale. Whole numbers (1, 2) keep the font sharp.")
      .translation("modernhud.config.boss_number_scale")
      .defineInRange("numberScale", 1.0, 0.5, 3.0);
   private static final EnumValue<BossNameMode> NAME_MODE = BUILDER.comment(
         new String[]{"Boss name: HIDDEN, ABOVE (small, right-aligned over the bar) or BELOW (left-aligned", "under the bar)."}
      )
      .translation("modernhud.config.boss_name_mode")
      .defineEnum("nameMode", BossNameMode.ABOVE);
   private static final DoubleValue NAME_SCALE = BUILDER.comment("Size of the name label. 1.0 = the font's native size (sharpest).")
      .translation("modernhud.config.boss_name_scale")
      .defineInRange("nameScale", 1.0, 0.5, 3.0);
   private static final BooleanValue USE_BOSS_COLOR = BUILDER.comment("Tint the bar with the boss event's color. Off keeps it white.")
      .translation("modernhud.config.boss_use_color")
      .define("useBossColor", true);
   private static final BooleanValue SHOW_TRACK = BUILDER.comment("Draw a thin line along the whole bar so the empty part stays visible.")
      .translation("modernhud.config.boss_show_track")
      .define("showTrack", true);
   private static final BooleanValue BAR_GLOW = BUILDER.comment("Soft glow outline around the filled part of the bar.")
      .translation("modernhud.config.boss_bar_glow")
      .define("barGlow", true);
   private static final BooleanValue HEAD_GLOW = BUILDER.comment("Make the leading edge of the fill glow.")
      .translation("modernhud.config.boss_head_glow")
      .define("headGlow", true);
   private static final EnumValue<BossBackdrop> BACKDROP = BUILDER.comment("What sits behind each bar: NONE, SHADOW (soft dark fade) or BOX (a panel).")
      .translation("modernhud.config.boss_backdrop")
      .defineEnum("backdrop", BossBackdrop.SHADOW);
   private static final IntValue BACKDROP_OPACITY = BUILDER.comment("Strength of the per-bar shadow / box, 0-100.")
      .translation("modernhud.config.boss_backdrop_opacity")
      .defineInRange("backdropOpacity", 55, 0, 100);
   private static final EnumValue<BossNumberLayout> NUMBER_LAYOUT = BUILDER.comment(
         new String[]{
            "Where numbers go: SIDE (left of each bar), ABOVE_BELOW (first bar above, last bar",
            "below, made for two bars), ABOVE or BELOW. The boss name shares the row when a number",
            "is above or below."
         }
      )
      .translation("modernhud.config.boss_number_layout")
      .defineEnum("numberLayout", BossNumberLayout.ABOVE_BELOW);
   private static final IntValue GLOW_INTENSITY = BUILDER.comment("Strength of the bar glow and head glow, 0 to 100. Needs barGlow or headGlow on.")
      .translation("modernhud.config.boss_glow_intensity")
      .defineInRange("glowIntensity", 60, 0, 100);
   private static final BooleanValue ANIMATIONS = BUILDER.comment(
         new String[]{"Bars slide and fade in and out, the fill eases to its value and the stack moves", "smoothly when a bar appears or goes."}
      )
      .translation("modernhud.config.boss_animations")
      .define("animations", true);
   private static final IntValue ANIMATION_MS = BUILDER.comment("Length of those animations in milliseconds.")
      .translation("modernhud.config.boss_animation_ms")
      .defineInRange("animationMs", 380, 100, 2000);
   private static final BooleanValue LOW_WARNING = BUILDER.comment("Bar and number pulse red when the bar drops to 20% or less.")
      .translation("modernhud.config.boss_low_warning")
      .define("lowWarning", true);
   private static final BooleanValue DAMAGE_TRAIL = BUILDER.comment(
         new String[]{"When the boss loses health, the lost part stays as a lighter trail and then drains", "away."}
      )
      .translation("modernhud.config.boss_damage_trail")
      .define("damageTrail", true);
   public static final ElementStyle style = new ElementStyle(
      BUILDER, "style", "boss", new ElementStyle.Defaults(true, 14278371, 16, PanelBorderStyle.NONE, 3, 1, 380, 380)
   );
   public static boolean enabled;
   public static HudPosition position;
   public static int offsetX;
   public static int offsetY;
   public static double scale;
   public static int barWidth;
   public static int barHeight;
   public static int barSpacing;
   public static int maxBars;
   public static boolean showNumber;
   public static BossNumberMode numberMode;
   public static boolean percentSign;
   public static double numberScale;
   public static BossNameMode nameMode;
   public static double nameScale;
   public static boolean useBossColor;
   public static boolean showTrack;
   public static boolean barGlow;
   public static boolean headGlow;
   public static BossBackdrop backdrop;
   public static int backdropOpacity;
   public static BossNumberLayout numberLayout;
   public static int glowIntensity;
   public static boolean animations;
   public static int animationMs;
   public static boolean lowWarning;
   public static boolean damageTrail;

   @SubscribeEvent
   static void onLoad(ModConfigEvent event) {
      try {
         refresh();
      } catch (IllegalStateException var2) {
      }
   }

   public static void refresh() {
      enabled = (Boolean)ENABLED.get();
      position = (HudPosition)POSITION.get();
      offsetX = (Integer)OFFSET_X.get();
      offsetY = (Integer)OFFSET_Y.get();
      scale = (Double)SCALE.get();
      barWidth = (Integer)BAR_WIDTH.get();
      barHeight = (Integer)BAR_HEIGHT.get();
      barSpacing = (Integer)BAR_SPACING.get();
      maxBars = (Integer)MAX_BARS.get();
      showNumber = (Boolean)SHOW_NUMBER.get();
      numberMode = (BossNumberMode)NUMBER_MODE.get();
      percentSign = (Boolean)PERCENT_SIGN.get();
      numberScale = (Double)NUMBER_SCALE.get();
      nameMode = (BossNameMode)NAME_MODE.get();
      nameScale = (Double)NAME_SCALE.get();
      useBossColor = (Boolean)USE_BOSS_COLOR.get();
      showTrack = (Boolean)SHOW_TRACK.get();
      barGlow = (Boolean)BAR_GLOW.get();
      headGlow = (Boolean)HEAD_GLOW.get();
      backdrop = (BossBackdrop)BACKDROP.get();
      backdropOpacity = (Integer)BACKDROP_OPACITY.get();
      numberLayout = (BossNumberLayout)NUMBER_LAYOUT.get();
      glowIntensity = (Integer)GLOW_INTENSITY.get();
      animations = (Boolean)ANIMATIONS.get();
      animationMs = (Integer)ANIMATION_MS.get();
      lowWarning = (Boolean)LOW_WARNING.get();
      damageTrail = (Boolean)DAMAGE_TRAIL.get();
      style.refresh();
   }

   static {
      BUILDER.comment(new String[]{"Compact boss bars in a screen corner, replacing vanilla's wide bars at the top of the", "screen."}).push("bossBar");
      BUILDER.pop();
   }
}
