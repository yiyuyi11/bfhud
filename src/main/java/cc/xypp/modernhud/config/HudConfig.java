package cc.xypp.modernhud.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.config.ModConfigEvent;

public class HudConfig {
   private static final Builder BUILDER = new Builder();
   private static final BooleanValue MASTER = BUILDER.comment(
         new String[]{"Master switch for the whole HUD. Off hides every ModernHUD element and lets the",
            "vanilla HUD draw again."}
      )
      .translation("modernhud.config.master")
      .define("master", true);
   private static final EnumValue<HudGuiScaleMode> GUI_SCALE_MODE = BUILDER.comment(
         new String[]{
            "Where the HUD's GUI scale comes from. MINECRAFT follows the game setting. FIXED uses",
            "hudGuiScale. RESOLUTION scales hudGuiScale with window height."
         }
      )
      .translation("modernhud.config.gui_scale_mode")
      .defineEnum("guiScaleMode", HudGuiScaleMode.MINECRAFT);
   private static final DoubleValue HUD_GUI_SCALE = BUILDER.comment(
         new String[]{"GUI scale for FIXED mode, or the scale at the reference height for RESOLUTION mode.", "2.0 equals Minecraft's GUI Scale 2."}
      )
      .translation("modernhud.config.hud_gui_scale")
      .defineInRange("hudGuiScale", 2.0, 0.5, 8.0);
   private static final IntValue GUI_SCALE_REFERENCE_HEIGHT = BUILDER.comment(
         "RESOLUTION mode only: the window height in pixels where hudGuiScale applies unchanged."
      )
      .translation("modernhud.config.gui_scale_reference_height")
      .defineInRange("guiScaleReferenceHeight", 1080, 360, 4320);
   private static final EnumValue<GlowQuality> GLOW_QUALITY = BUILDER.comment("Smoothness of glows: LOW, MEDIUM or HIGH.")
      .translation("modernhud.config.glow_quality")
      .defineEnum("glowQuality", GlowQuality.LOW);
   private static final BooleanValue PIXEL_PERFECT_TEXT = BUILDER.comment(
         new String[]{"Snap text so each font pixel covers a whole number of screen pixels. Removes uneven", "text at fractional scales."}
      )
      .translation("modernhud.config.pixel_perfect_text")
      .define("pixelPerfectText", true);
   private static final BooleanValue PIXEL_PERFECT_ELEMENTS = BUILDER.comment(
         new String[]{"Also snap whole widgets to whole screen pixels. Sharper, but scale options change in", "coarse steps."}
      )
      .translation("modernhud.config.pixel_perfect_elements")
      .define("pixelPerfectElements", false);
   private static final BooleanValue GUI_SCALE_SNAP = BUILDER.comment("RESOLUTION mode only: round the resulting GUI scale to a whole number.")
      .translation("modernhud.config.gui_scale_snap")
      .define("guiScaleSnap", true);
   private static final BooleanValue ANIMATIONS = BUILDER.comment(
         new String[]{"Turn all HUD animations (gliding bars, sliding slots, fades) on or off. Off makes", "values change instantly."}
      )
      .translation("modernhud.config.animations")
      .define("animations", true);
   private static final DoubleValue ANIMATION_SPEED = BUILDER.comment("Speed of all animations. 2.0 is twice as fast, 0.5 is half speed.")
      .translation("modernhud.config.animation_speed")
      .defineInRange("animationSpeed", 1.0, 0.25, 4.0);
   private static final IntValue GLOW_INTENSITY = BUILDER.comment("Strength of every glow in percent. 0 turns all glows off, 100 is the default.")
      .translation("modernhud.config.glow_intensity")
      .defineInRange("glowIntensity", 100, 0, 200);
   private static final IntValue HEALTH_ANIM_MS = BUILDER.comment("Time in milliseconds for health bars to move to a new value.")
      .translation("modernhud.config.health_anim_ms")
      .defineInRange("healthAnimMs", 280, 0, 3000);
   private static final IntValue ABSORPTION_ANIM_MS = BUILDER.comment("Time in milliseconds for absorption bars to move to a new value.")
      .translation("modernhud.config.absorption_anim_ms")
      .defineInRange("absorptionAnimMs", 220, 0, 3000);
   private static final IntValue FEEDBACK_OPACITY = BUILDER.comment("Opacity of the hit / Absorption-break feedback icon, 0-100.")
      .translation("modernhud.config.feedback_opacity")
      .defineInRange("feedbackOpacity", 100, 0, 100);
   private static final IntValue FEEDBACK_FADE_MS = BUILDER.comment("Fade-out time in milliseconds at the end of the icon's display time.")
      .translation("modernhud.config.feedback_fade_ms")
      .defineInRange("feedbackFadeMs", 250, 0, 10000);
   private static final BooleanValue ENABLED = BUILDER.comment("Show the player widget.").translation("modernhud.config.enabled").define("enabled", true);
   private static final BooleanValue SHOW_HEALTH_BAR = BUILDER.comment("Show your own health bar under the absorption bar.")
      .translation("modernhud.config.show_health_bar")
      .define("showHealthBar", true);
   private static final BooleanValue SHOW_NAME = BUILDER.comment("Show your own username next to the head icon.")
      .translation("modernhud.config.show_name")
      .define("showName", true);
   private static final BooleanValue PERSONAL_USE_TEAM_COLOR = BUILDER.comment("Color your name with your scoreboard team color. White if you have no team.")
      .translation("modernhud.config.personal_use_team_color")
      .define("useTeamColor", false);
   private static final BooleanValue SHOW_HEAD = BUILDER.comment("Show a small 2D render of your own head/skin.")
      .translation("modernhud.config.show_head")
      .define("showHead", true);
   private static final BooleanValue SHOW_TEAM_HUD = BUILDER.comment(
         new String[]{"Show the teammate list. Nothing is drawn if you have no team or no other tracked", "members."}
      )
      .translation("modernhud.config.show_team_hud")
      .define("enabled", true);
   private static final IntValue TEAM_HUD_MAX_PLAYERS = BUILDER.comment("Most teammates shown at once. Extra ones are hidden, sorted by name.")
      .translation("modernhud.config.team_hud_max_players")
      .defineInRange("maxPlayers", 6, 1, 16);
   private static final IntValue TEAM_HUD_BAR_WIDTH = BUILDER.comment(
         new String[]{"Width of each teammate health bar in pixels, before scaling. Long names can run past", "it."}
      )
      .translation("modernhud.config.team_hud_bar_width")
      .defineInRange("barWidth", 70, 16, 400);
   private static final DoubleValue TEAM_HUD_SCALE_FACTOR = BUILDER.comment("Size of teammate rows relative to your own widget. 0.9 makes them 10% smaller.")
      .translation("modernhud.config.team_hud_scale_factor")
      .defineInRange("scaleFactor", 0.95, 0.1, 1.5);
   private static final IntValue TEAM_HUD_ROW_GAP = BUILDER.comment("Vertical gap in px between teammate rows.")
      .translation("modernhud.config.team_hud_row_gap")
      .defineInRange("rowGap", 2, 0, 40);
   private static final IntValue TEAM_HUD_GROUP_GAP = BUILDER.comment("Vertical gap in pixels between the team list and your widget.")
      .translation("modernhud.config.team_hud_group_gap")
      .defineInRange("groupGap", 4, 0, 100);
   private static final BooleanValue TEAM_HUD_SHOW_HEAD = BUILDER.comment("Show each teammate's head next to their row.")
      .translation("modernhud.config.team_hud_show_head")
      .define("showHead", true);
   private static final BooleanValue SHOW_TEAM_ABSORPTION = BUILDER.comment(
         new String[]{"Show each teammate's absorption bar above their health bar, using the same style as", "your own."}
      )
      .translation("modernhud.config.team_hud_show_absorption")
      .define("showAbsorption", true);
   private static final BooleanValue TEAM_HUD_USE_TEAM_COLOR = BUILDER.comment(
         new String[]{"Color teammate names with their scoreboard team color. Falls back to nameColor if the", "team has none."}
      )
      .translation("modernhud.config.team_hud_use_team_color")
      .define("useTeamColor", false);
   private static final ConfigValue<String> TEAM_HUD_NAME_COLOR = BUILDER.comment(
         "Teammate name color when useTeamColor is off or the team has no color, as a hex code."
      )
      .translation("modernhud.config.team_hud_name_color")
      .define("nameColor", ColorConfig.toHex(15134453));
   private static final ConfigValue<String> TEAM_HUD_DOWN_GLOW_COLOR = BUILDER.comment(
         "Color of the pulsing glow over a row while that teammate is dead or spectating."
      )
      .translation("modernhud.config.team_hud_down_glow_color")
      .define("downGlowColor", ColorConfig.toHex(15225144));
   private static final BooleanValue SHOW_HP_TEXT = BUILDER.comment("Show your current HP as text somewhere on the widget.")
      .translation("modernhud.config.show_hp_text")
      .define("enabled", false);
   private static final EnumValue<HpTextFormat> HP_TEXT_FORMAT = BUILDER.comment("CLASSIC = '20/20' (current/max HP points). PERCENT = '100%'.")
      .translation("modernhud.config.hp_text_format")
      .defineEnum("format", HpTextFormat.PERCENT);
   private static final EnumValue<BarTextPosition> HP_TEXT_POSITION = BUILDER.comment("Where the HP text goes: BELOW_BAR, BELOW_NAME_HEAD or END_OF_BAR.")
      .translation("modernhud.config.hp_text_position")
      .defineEnum("position", BarTextPosition.BELOW_NAME_HEAD);
   private static final DoubleValue LOW_HEALTH_THRESHOLD_PERCENT = BUILDER.comment("Health percentage below which the HP text uses the low-health color.")
      .translation("modernhud.config.low_health_threshold_percent")
      .defineInRange("lowHealthThresholdPercent", 30.0, 0.0, 100.0);
   private static final BooleanValue SHOW_VITALS_GLOW = BUILDER.comment(
         new String[]{"Glow behind the health bar: a flash when you are hit and a slow pulse while health is", "low."}
      )
      .translation("modernhud.config.show_vitals_glow")
      .define("glowEnabled", true);
   private static final ConfigValue<String> VITALS_GLOW_COLOR = BUILDER.comment("Vitals glow color, as a hex code (e.g. \"FF3B30\" or \"#FF3B30\").")
      .translation("modernhud.config.vitals_glow_color")
      .define("vitalsGlowColor", ColorConfig.toHex(16726832));
   private static final ConfigValue<String> HP_TEXT_NORMAL_COLOR = BUILDER.comment(
         "HP text color above the low-health threshold, as a hex code (e.g. \"FFFFFF\")."
      )
      .translation("modernhud.config.hp_text_normal_color")
      .define("normalColor", ColorConfig.toHex(16777215));
   private static final ConfigValue<String> HP_TEXT_LOW_COLOR = BUILDER.comment("HP text color at/below the low-health threshold, as a hex code.")
      .translation("modernhud.config.hp_text_low_color")
      .define("lowColor", ColorConfig.toHex(16733525));
   private static final BooleanValue SHOW_ARMOR = BUILDER.comment("Show your current armor points as text.")
      .translation("modernhud.config.show_armor")
      .define("enabled", false);
   private static final EnumValue<HpTextFormat> ARMOR_TEXT_FORMAT = BUILDER.comment("CLASSIC = '12/20' (current/max armor points). PERCENT = '60%'.")
      .translation("modernhud.config.armor_text_format")
      .defineEnum("textFormat", HpTextFormat.CLASSIC);
   private static final EnumValue<BarTextPosition> ARMOR_TEXT_POSITION = BUILDER.comment(
         new String[]{"BELOW_NAME_HEAD puts armor on the info line after the HP text. END_OF_BAR and", "BELOW_BAR put it on its own row under the health bar."}
      )
      .translation("modernhud.config.armor_text_position")
      .defineEnum("textPosition", BarTextPosition.END_OF_BAR);
   private static final ConfigValue<String> ARMOR_TEXT_COLOR = BUILDER.comment("Armor text color, as a hex code (e.g. \"C0C0C0\" or \"#C0C0C0\").")
      .translation("modernhud.config.armor_text_color")
      .define("textColor", ColorConfig.toHex(12632256));
   private static final EnumValue<HudPosition> POSITION = BUILDER.comment(
         new String[]{"Screen corner the widget is attached to. CUSTOM uses the offsets as absolute", "coordinates."}
      )
      .translation("modernhud.config.position")
      .defineEnum("anchor", HudPosition.BOTTOM_LEFT);
   private static final IntValue OFFSET_X = BUILDER.comment("Horizontal distance from the anchor in pixels (absolute X when anchor is CUSTOM).")
      .translation("modernhud.config.offset_x")
      .defineInRange("offsetX", 12, -10000, 10000);
   private static final IntValue OFFSET_Y = BUILDER.comment("Vertical distance from the anchor in pixels (absolute Y when anchor is CUSTOM).")
      .translation("modernhud.config.offset_y")
      .defineInRange("offsetY", 2, -10000, 10000);
   private static final IntValue BAR_WIDTH = BUILDER.comment("Bar width in pixels, before scaling.")
      .translation("modernhud.config.bar_width")
      .defineInRange("barWidth", 70, 10, 1000);
   private static final IntValue BAR_HEIGHT = BUILDER.comment("Bar height in pixels, before scaling.")
      .translation("modernhud.config.bar_height")
      .defineInRange("barHeight", 5, 2, 200);
   private static final DoubleValue BAR_SCALE = BUILDER.comment("Overall scale multiplier applied to the whole widget.")
      .translation("modernhud.config.bar_scale")
      .defineInRange("barScale", 0.9, 0.1, 5.0);
   private static final IntValue SEGMENT_COUNT = BUILDER.comment(
         new String[]{"Number of segments in the absorption bar. 4 matches the four color tiers and the four", "vanilla Absorption levels."}
      )
      .translation("modernhud.config.segment_count")
      .defineInRange("segmentCount", 3, 1, 20);
   private static final DoubleValue POINTS_PER_SEGMENT = BUILDER.comment(
         new String[]{"Absorption points per segment. Vanilla gives 4 per level, so 4.0 means one segment per", "level."}
      )
      .translation("modernhud.config.points_per_segment")
      .defineInRange("pointsPerSegment", 4.0, 0.5, 100.0);
   private static final BooleanValue SHOW_OVERFLOW_TEXT = BUILDER.comment("Show a +N label when absorption is higher than the bar can display.")
      .translation("modernhud.config.show_overflow_text")
      .define("showOverflowText", false);
   private static final BooleanValue COLLAPSE_ABSORPTION_ROW = BUILDER.comment(
         new String[]{
            "Take the absorption bar's row out of the layout while you have no absorption, so the widget is",
            "shorter and the name moves down into its place. The row slides back in when absorption returns."
         }
      )
      .translation("modernhud.config.collapse_absorption_row")
      .define("collapseAbsorptionRow", true);
   private static final EnumValue<NamePosition> NAME_POSITION = BUILDER.comment("Where your name shows relative to the bars: ABOVE (MW default) or BELOW.")
      .translation("modernhud.config.name_position")
      .defineEnum("namePosition", NamePosition.ABOVE);
   private static final IntValue ABSORPTION_HEIGHT = BUILDER.comment("Height of the thin absorption strip above the health bar, in pixels.")
      .translation("modernhud.config.absorption_height")
      .defineInRange("absorptionHeight", 3, 1, 100);
   private static final EnumValue<AbsorptionColorMode> ABSORPTION_COLOR_MODE = BUILDER.comment(
         new String[]{"TIERED colors the plates gray, blue, purple, red as more fill up. CUSTOM always uses", "absorptionCustomColor."}
      )
      .translation("modernhud.config.absorption_color_mode")
      .defineEnum("absorptionColorMode", AbsorptionColorMode.CUSTOM);
   private static final ConfigValue<String> ABSORPTION_CUSTOM_COLOR = BUILDER.comment("Plate color when absorptionColorMode is CUSTOM, as a hex code.")
      .translation("modernhud.config.absorption_custom_color")
      .define("absorptionCustomColor", ColorConfig.toHex(1867478));
   private static final BooleanValue SHOW_PANEL = BUILDER.comment("Draw the background panel behind the whole widget.")
      .translation("modernhud.config.show_panel")
      .define("enabled", false);
   private static final ConfigValue<String> PANEL_COLOR = BUILDER.comment("Panel background color, as a hex code (e.g. \"0A0D11\" or \"#0A0D11\").")
      .translation("modernhud.config.panel_color")
      .define("panelColor", ColorConfig.toHex(14278371));
   private static final IntValue PANEL_OPACITY = BUILDER.comment("Panel opacity, 0 to 100.")
      .translation("modernhud.config.panel_opacity")
      .defineInRange("panelOpacity", 14, 0, 100);
   private static final EnumValue<PanelBorderStyle> BORDER_STYLE = BUILDER.comment(
         new String[]{
            "Border drawn over the panel. NONE, CORNERS (short L-shaped brackets), FULL (thin",
            "outline), DOTS, BOTTOM_DOTS or FRAME (outline with corner dots)."
         }
      )
      .translation("modernhud.config.border_style")
      .defineEnum("borderStyle", PanelBorderStyle.CORNERS);
   private static final ConfigValue<String> BORDER_COLOR = BUILDER.comment("Border/corner-bracket color, as a hex code (e.g. \"E8ECF0\" or \"#E8ECF0\").")
      .translation("modernhud.config.border_color")
      .define("borderColor", ColorConfig.toHex(15265008));
   private static final IntValue BORDER_OPACITY = BUILDER.comment("Border opacity, 0 to 100.")
      .translation("modernhud.config.border_opacity")
      .defineInRange("borderOpacity", 90, 0, 100);
   private static final IntValue BORDER_LENGTH = BUILDER.comment("Length of each bracket arm in pixels. Capped to a fraction of the panel size.")
      .translation("modernhud.config.border_length")
      .defineInRange("borderLength", 1, 1, 20);
   private static final IntValue BORDER_OUTSET = BUILDER.comment(
         new String[]{"How far brackets and dots sit outside the panel edge, in pixels. 0 puts them on the", "edge."}
      )
      .translation("modernhud.config.border_outset")
      .defineInRange("borderOutset", 1, 0, 20);
   private static final BooleanValue SHOW_FEEDBACK_WHEN_VICTIM = BUILDER.comment("Also play the hit and break cue and icon when you are the one being hit.")
      .translation("modernhud.config.show_feedback_when_victim")
      .define("showFeedbackWhenVictim", false);
   private static final EnumValue<HudPosition> FEEDBACK_POSITION = BUILDER.comment(
         new String[]{"Screen corner the icon is attached to. CUSTOM uses the offsets as absolute", "coordinates."}
      )
      .translation("modernhud.config.feedback_position")
      .defineEnum("anchor", HudPosition.CUSTOM);
   private static final IntValue FEEDBACK_OFFSET_X = BUILDER.comment("Horizontal distance from the anchor in pixels (absolute X when anchor is CUSTOM).")
      .translation("modernhud.config.feedback_offset_x")
      .defineInRange("offsetX", 377, -10000, 10000);
   private static final IntValue FEEDBACK_OFFSET_Y = BUILDER.comment("Vertical distance from the anchor in pixels (absolute Y when anchor is CUSTOM).")
      .translation("modernhud.config.feedback_offset_y")
      .defineInRange("offsetY", 132, -10000, 10000);
   private static final IntValue FEEDBACK_ICON_WIDTH = BUILDER.comment("Icon width in pixels, before scaling.")
      .translation("modernhud.config.feedback_icon_width")
      .defineInRange("width", 28, 4, 500);
   private static final IntValue FEEDBACK_ICON_HEIGHT = BUILDER.comment("Icon height in pixels, before scaling.")
      .translation("modernhud.config.feedback_icon_height")
      .defineInRange("height", 28, 4, 500);
   private static final DoubleValue FEEDBACK_ICON_SCALE = BUILDER.comment("Extra scale multiplier for the icon.")
      .translation("modernhud.config.feedback_icon_scale")
      .defineInRange("scale", 1.0, 0.1, 10.0);
   private static final BooleanValue SHOW_HIT_ICON = BUILDER.comment("Show an icon when a hit lands on a shield without breaking it.")
      .translation("modernhud.config.show_hit_icon")
      .define("showHitIcon", true);
   private static final IntValue HIT_ICON_DURATION_MS = BUILDER.comment("How long the hit icon stays on screen, in milliseconds.")
      .translation("modernhud.config.hit_icon_duration_ms")
      .defineInRange("hitIconDurationMs", 300, 50, 10000);
   private static final BooleanValue SHOW_BREAK_ICON = BUILDER.comment("Show an icon when you (the attacker) fully break someone's Absorption.")
      .translation("modernhud.config.show_break_icon")
      .define("showBreakIcon", true);
   private static final IntValue BREAK_ICON_DURATION_MS = BUILDER.comment("How long the break icon stays on screen, in milliseconds.")
      .translation("modernhud.config.break_icon_duration_ms")
      .defineInRange("breakIconDurationMs", 700, 50, 10000);
   public static final ModConfigSpec SPEC = BUILDER.build();
   /** Master switch state, mirrored from the config. */
   public static boolean master() {
      return master;
   }

   public static void toggleMaster() {
      master = !master;
   }

   public static boolean enabled = true;
   /** Master switch state, mirrored from the config. */
   public static boolean master = true;
   public static boolean showHealthBar;
   public static boolean showName;
   public static boolean personalUseTeamColor;
   public static boolean showHead;
   public static boolean showTeamHud;
   public static int teamHudMaxPlayers;
   public static int teamHudBarWidth;
   public static double teamHudScaleFactor;
   public static int teamHudRowGap;
   public static int teamHudGroupGap;
   public static boolean teamHudShowHead;
   public static boolean showTeamAbsorption;
   public static boolean teamHudUseTeamColor;
   public static int teamHudNameColor;
   public static int teamHudDownGlowColor;
   public static boolean showHpText;
   public static HpTextFormat hpTextFormat;
   public static BarTextPosition hpTextPosition;
   public static double lowHealthThresholdPercent;
   public static boolean showVitalsGlow;
   public static int vitalsGlowColor;
   public static int hpTextNormalColor;
   public static int hpTextLowColor;
   public static boolean showArmor;
   public static HpTextFormat armorTextFormat;
   public static BarTextPosition armorTextPosition;
   public static int armorTextColor;
   public static HudPosition position;
   public static int offsetX;
   public static int offsetY;
   public static int barWidth;
   public static int barHeight;
   public static double barScale;
   public static int segmentCount;
   public static double pointsPerSegment;
   public static boolean showOverflowText;
   public static boolean collapseAbsorptionRow;
   public static NamePosition namePosition;
   public static int absorptionHeight;
   public static AbsorptionColorMode absorptionColorMode;
   public static int absorptionCustomColor;
   public static boolean showPanel;
   public static int panelColor;
   public static double panelOpacity;
   public static PanelBorderStyle borderStyle;
   public static int borderColor;
   public static double borderOpacity;
   public static int borderLength;
   public static int borderOutset;
   public static boolean showFeedbackWhenVictim;
   public static HudPosition feedbackPosition;
   public static int feedbackOffsetX;
   public static int feedbackOffsetY;
   public static int feedbackIconWidth;
   public static int feedbackIconHeight;
   public static double feedbackIconScale;
   public static boolean showHitIcon;
   public static int hitIconDurationMs;
   public static boolean showBreakIcon;
   public static int breakIconDurationMs;
   public static HudGuiScaleMode guiScaleMode = HudGuiScaleMode.MINECRAFT;
   public static double hudGuiScale = 2.0;
   public static int guiScaleReferenceHeight = 1080;
   public static GlowQuality glowQuality = GlowQuality.HIGH;
   public static boolean pixelPerfectText = true;
   public static boolean pixelPerfectElements = false;
   public static boolean guiScaleSnap = true;
   public static boolean animations = true;
   public static double animationSpeed = 1.0;
   public static int glowIntensity = 100;
   public static int healthAnimMs = 280;
   public static int absorptionAnimMs = 220;
   public static int feedbackOpacity = 100;
   public static int feedbackFadeMs = 250;

   @SubscribeEvent
   static void onLoad(ModConfigEvent event) {
      try {
         refresh();
      } catch (IllegalStateException var2) {
      }
   }

   public static void refresh() {
      master = (Boolean)MASTER.get();
      enabled = (Boolean)ENABLED.get();
      showHealthBar = (Boolean)SHOW_HEALTH_BAR.get();
      showName = (Boolean)SHOW_NAME.get();
      personalUseTeamColor = (Boolean)PERSONAL_USE_TEAM_COLOR.get();
      showHead = (Boolean)SHOW_HEAD.get();
      showHpText = (Boolean)SHOW_HP_TEXT.get();
      hpTextFormat = (HpTextFormat)HP_TEXT_FORMAT.get();
      hpTextPosition = (BarTextPosition)HP_TEXT_POSITION.get();
      lowHealthThresholdPercent = (Double)LOW_HEALTH_THRESHOLD_PERCENT.get();
      showVitalsGlow = (Boolean)SHOW_VITALS_GLOW.get();
      vitalsGlowColor = ColorConfig.fromHex((String)VITALS_GLOW_COLOR.get(), 16726832);
      hpTextNormalColor = ColorConfig.fromHex((String)HP_TEXT_NORMAL_COLOR.get(), 16777215);
      hpTextLowColor = ColorConfig.fromHex((String)HP_TEXT_LOW_COLOR.get(), 16733525);
      showArmor = (Boolean)SHOW_ARMOR.get();
      armorTextFormat = (HpTextFormat)ARMOR_TEXT_FORMAT.get();
      armorTextPosition = (BarTextPosition)ARMOR_TEXT_POSITION.get();
      armorTextColor = ColorConfig.fromHex((String)ARMOR_TEXT_COLOR.get(), 12632256);
      position = (HudPosition)POSITION.get();
      offsetX = (Integer)OFFSET_X.get();
      offsetY = (Integer)OFFSET_Y.get();
      barWidth = (Integer)BAR_WIDTH.get();
      barHeight = (Integer)BAR_HEIGHT.get();
      barScale = (Double)BAR_SCALE.get();
      segmentCount = (Integer)SEGMENT_COUNT.get();
      pointsPerSegment = (Double)POINTS_PER_SEGMENT.get();
      showOverflowText = (Boolean)SHOW_OVERFLOW_TEXT.get();
      collapseAbsorptionRow = (Boolean)COLLAPSE_ABSORPTION_ROW.get();
      namePosition = (NamePosition)NAME_POSITION.get();
      absorptionHeight = (Integer)ABSORPTION_HEIGHT.get();
      absorptionColorMode = (AbsorptionColorMode)ABSORPTION_COLOR_MODE.get();
      absorptionCustomColor = ColorConfig.fromHex((String)ABSORPTION_CUSTOM_COLOR.get(), 1867478);
      showPanel = (Boolean)SHOW_PANEL.get();
      panelColor = ColorConfig.fromHex((String)PANEL_COLOR.get(), 14278371);
      panelOpacity = (double)((Integer)PANEL_OPACITY.get()).intValue();
      borderStyle = (PanelBorderStyle)BORDER_STYLE.get();
      borderColor = ColorConfig.fromHex((String)BORDER_COLOR.get(), 15265008);
      borderOpacity = (double)((Integer)BORDER_OPACITY.get()).intValue();
      borderLength = (Integer)BORDER_LENGTH.get();
      borderOutset = (Integer)BORDER_OUTSET.get();
      showFeedbackWhenVictim = (Boolean)SHOW_FEEDBACK_WHEN_VICTIM.get();
      feedbackPosition = (HudPosition)FEEDBACK_POSITION.get();
      feedbackOffsetX = (Integer)FEEDBACK_OFFSET_X.get();
      feedbackOffsetY = (Integer)FEEDBACK_OFFSET_Y.get();
      feedbackIconWidth = (Integer)FEEDBACK_ICON_WIDTH.get();
      feedbackIconHeight = (Integer)FEEDBACK_ICON_HEIGHT.get();
      feedbackIconScale = (Double)FEEDBACK_ICON_SCALE.get();
      showHitIcon = (Boolean)SHOW_HIT_ICON.get();
      hitIconDurationMs = (Integer)HIT_ICON_DURATION_MS.get();
      showBreakIcon = (Boolean)SHOW_BREAK_ICON.get();
      breakIconDurationMs = (Integer)BREAK_ICON_DURATION_MS.get();
      showTeamHud = (Boolean)SHOW_TEAM_HUD.get();
      teamHudMaxPlayers = (Integer)TEAM_HUD_MAX_PLAYERS.get();
      teamHudBarWidth = (Integer)TEAM_HUD_BAR_WIDTH.get();
      teamHudScaleFactor = (Double)TEAM_HUD_SCALE_FACTOR.get();
      teamHudRowGap = (Integer)TEAM_HUD_ROW_GAP.get();
      teamHudGroupGap = (Integer)TEAM_HUD_GROUP_GAP.get();
      teamHudShowHead = (Boolean)TEAM_HUD_SHOW_HEAD.get();
      showTeamAbsorption = (Boolean)SHOW_TEAM_ABSORPTION.get();
      teamHudUseTeamColor = (Boolean)TEAM_HUD_USE_TEAM_COLOR.get();
      teamHudNameColor = ColorConfig.fromHex((String)TEAM_HUD_NAME_COLOR.get(), 15134453);
      teamHudDownGlowColor = ColorConfig.fromHex((String)TEAM_HUD_DOWN_GLOW_COLOR.get(), 15225144);
      ColorConfig.migrateDarkPanel(PANEL_COLOR, PANEL_OPACITY, 14278371, 14);
      guiScaleMode = (HudGuiScaleMode)GUI_SCALE_MODE.get();
      hudGuiScale = (Double)HUD_GUI_SCALE.get();
      guiScaleReferenceHeight = (Integer)GUI_SCALE_REFERENCE_HEIGHT.get();
      glowQuality = (GlowQuality)GLOW_QUALITY.get();
      pixelPerfectText = (Boolean)PIXEL_PERFECT_TEXT.get();
      pixelPerfectElements = (Boolean)PIXEL_PERFECT_ELEMENTS.get();
      guiScaleSnap = (Boolean)GUI_SCALE_SNAP.get();
      animations = (Boolean)ANIMATIONS.get();
      animationSpeed = (Double)ANIMATION_SPEED.get();
      glowIntensity = (Integer)GLOW_INTENSITY.get();
      healthAnimMs = (Integer)HEALTH_ANIM_MS.get();
      absorptionAnimMs = (Integer)ABSORPTION_ANIM_MS.get();
      feedbackOpacity = (Integer)FEEDBACK_OPACITY.get();
      feedbackFadeMs = (Integer)FEEDBACK_FADE_MS.get();
   }

   public static void setFeedbackPosition(int x, int y) {
      feedbackPosition = HudPosition.CUSTOM;
      feedbackOffsetX = x;
      feedbackOffsetY = y;
      FEEDBACK_POSITION.set(HudPosition.CUSTOM);
      FEEDBACK_OFFSET_X.set(x);
      FEEDBACK_OFFSET_Y.set(y);
   }

   public static void saveConfig() {
      SPEC.save();
   }

   static {
      BUILDER.comment("Your own status widget: head, name, health and absorption bars, HP text.").push("general");
      BUILDER.pop();
      BUILDER.comment("Numeric health readout.").push("hpText");
      BUILDER.pop();
      BUILDER.comment("Armor readout, shown as text (for example 14/20). Visible even at 0.").push("armor");
      BUILDER.pop();
      BUILDER.comment("Where the widget is anchored on screen").push("position");
      BUILDER.pop();
      BUILDER.comment("Bar size and how Absorption points map onto it").push("appearance");
      BUILDER.pop();
      BUILDER.comment(new String[]{"Background panel behind the widget: a flat fill with an optional corner or line", "border."}).push("panel");
      BUILDER.pop();
      BUILDER.comment(new String[]{"Shield feedback. The sounds themselves (style, volume, pitch) are in the Sounds tab.", "They only play for you."})
         .push("sounds");
      BUILDER.pop();
      BUILDER.comment("Position and size of the shield hit / break icon.").push("feedbackIcon");
      BUILDER.pop();
      BUILDER.comment("Which icons appear and how long they stay, in milliseconds.").push("feedbackIconTiming");
      BUILDER.pop();
      BUILDER.comment(new String[]{"Teammate list for your scoreboard team: one compact row per teammate above your own", "widget."}).push("teamHud");
      BUILDER.pop();
      BUILDER.comment(
            new String[]{"Scaling, animation and glow settings shared by every HUD element. Fade times for each", "element are in that element's own section."}
         )
         .push("animation");
      BUILDER.pop();
   }
}
