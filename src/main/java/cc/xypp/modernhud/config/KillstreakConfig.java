package cc.xypp.modernhud.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class KillstreakConfig {
   private static final Builder BUILDER = new Builder();
   public static final ForgeConfigSpec SPEC = BUILDER.build();
   private static final BooleanValue ENABLED = BUILDER.comment(
         new String[]{"Draw the tracker with ModernHUD. ModernLoadout's own vertical bar is hidden while this", "is on."}
      )
      .translation("modernhud.config.ks_enabled")
      .define("enabled", true);
   private static final BooleanValue ATTACH_TO_WEAPON_HUD = BUILDER.comment(
         new String[]{"Stack the tracker above the weapon widget, on the same edge and at the same scale. Off", "uses position and the offsets below."}
      )
      .translation("modernhud.config.ks_attach")
      .define("attachToWeaponHud", false);
   private static final EnumValue<HudPosition> POSITION = BUILDER.comment("Screen corner the tracker is anchored to when attachToWeaponHud is off.")
      .translation("modernhud.config.ks_position")
      .defineEnum("position", HudPosition.BOTTOM_RIGHT);
   private static final IntValue OFFSET_X = BUILDER.comment(
         new String[]{"Horizontal distance from the anchor in pixels. Only used when attachToWeaponHud is", "off."}
      )
      .translation("modernhud.config.ks_offset_x")
      .defineInRange("offsetX", 8, -2000, 4000);
   private static final IntValue OFFSET_Y = BUILDER.comment("Vertical distance from the anchor in pixels. Only used when attachToWeaponHud is off.")
      .translation("modernhud.config.ks_offset_y")
      .defineInRange("offsetY", 120, -2000, 4000);
   private static final DoubleValue SCALE_FACTOR = BUILDER.comment(
         new String[]{"Scale relative to the weapon widget (1.0 is the same size). Absolute scale when", "attachToWeaponHud is off."}
      )
      .translation("modernhud.config.ks_scale")
      .defineInRange("scaleFactor", 1.0, 0.25, 3.0);
   private static final IntValue SLOT_SIZE = BUILDER.comment("Side of one square slot in px.")
      .translation("modernhud.config.ks_slot_size")
      .defineInRange("slotSize", 25, 16, 48);
   private static final IntValue SLOT_GAP = BUILDER.comment("Gap between slots in px.")
      .translation("modernhud.config.ks_slot_gap")
      .defineInRange("slotGap", 2, 0, 24);
   private static final IntValue GROUP_GAP = BUILDER.comment(
         new String[]{"Gap in pixels between the tracker and the weapon widget. Only used when", "attachToWeaponHud is on."}
      )
      .translation("modernhud.config.ks_group_gap")
      .defineInRange("groupGap", 10, 0, 48);
   private static final BooleanValue SHOW_PROGRESS_BAR = BUILDER.comment(
         new String[]{
            "Segmented bar next to the slots with one segment per kill still needed for the next",
            "reward. It fills as kills come in and resets for the next slot."
         }
      )
      .translation("modernhud.config.ks_show_bar")
      .define("showProgressBar", true);
   private static final BooleanValue SHOW_SELECTION = BUILDER.comment(
         new String[]{"Flash the selected streaks or Specialist perks once when you join and whenever the", "loadout changes."}
      )
      .translation("modernhud.config.ks_show_selection")
      .define("showSelection", false);
   private static final IntValue SELECTION_SECONDS = BUILDER.comment("How long the selection stays visible before fading, in seconds.")
      .translation("modernhud.config.ks_selection_seconds")
      .defineInRange("selectionSeconds", 4, 1, 30);
   private static final BooleanValue AUTO_HIDE = BUILDER.comment(
         new String[]{
            "Slide the tracker away after autoHideSeconds without progress and bring it back on a",
            "kill, a reward or a loadout change. Ready rewards keep it visible. Specialist always",
            "works this way."
         }
      )
      .translation("modernhud.config.ks_auto_hide_enabled")
      .define("autoHide", false);
   private static final IntValue AUTO_HIDE_SECONDS = BUILDER.comment("Seconds without a change before the tracker slides out.")
      .translation("modernhud.config.ks_auto_hide")
      .defineInRange("autoHideSeconds", 5, 1, 120);
   private static final ConfigValue<String> READY_COLOR = BUILDER.comment("Glow and border color of a reward that is ready to use, as a hex code.")
      .translation("modernhud.config.ks_ready_color")
      .define("readyGlowColor", ColorConfig.toHex(10941836));
   private static final ConfigValue<String> BAR_COLOR = BUILDER.comment("Color of the kill bar segments and the next slot's accent, as a hex code.")
      .translation("modernhud.config.ks_bar_color")
      .define("killBarColor", ColorConfig.toHex(16777215));
   private static final ConfigValue<String> ACTIVE_COLOR = BUILDER.comment("Color of Specialist perks that are already active, as a hex code.")
      .translation("modernhud.config.ks_active_color")
      .define("activeGlowColor", ColorConfig.toHex(10941836));
   private static final BooleanValue PERK_TOAST = BUILDER.comment(
         new String[]{"Show the perk popup. In Specialist mode it replaces the small blink of the selected", "perk icons above the tracker."}
      )
      .translation("modernhud.config.perk_toast")
      .define("enabled", true);
   private static final EnumValue<HudPosition> PERK_POSITION = BUILDER.comment("Screen corner the toast slides in from / sits in.")
      .translation("modernhud.config.perk_position")
      .defineEnum("position", HudPosition.TOP_RIGHT);
   private static final IntValue PERK_OFFSET_X = BUILDER.comment("Horizontal offset in px from that corner's edge.")
      .translation("modernhud.config.perk_offset_x")
      .defineInRange("offsetX", 4, -2000, 4000);
   private static final IntValue PERK_OFFSET_Y = BUILDER.comment("Vertical offset in px from that corner's edge.")
      .translation("modernhud.config.perk_offset_y")
      .defineInRange("offsetY", 40, -2000, 4000);
   private static final DoubleValue PERK_SCALE = BUILDER.comment("Overall scale of the toast.")
      .translation("modernhud.config.perk_scale")
      .defineInRange("scale", 1.0, 0.5, 3.0);
   private static final IntValue PERK_HOLD_SECONDS = BUILDER.comment("How long the popup stays on screen before sliding out, in seconds.")
      .translation("modernhud.config.perk_hold")
      .defineInRange("holdSeconds", 4, 1, 20);
   private static final IntValue PERK_ICON_SIZE = BUILDER.comment("Size of each perk icon in px.")
      .translation("modernhud.config.perk_icon_size")
      .defineInRange("iconSize", 18, 10, 40);
   private static final BooleanValue PERK_FRAME = BUILDER.comment("Draw a thin outline around the popup with a dot on each corner.")
      .translation("modernhud.config.perk_frame")
      .define("frame", true);
   private static final BooleanValue PERK_HIGHLIGHT = BUILDER.comment("A very light, thin highlight strip behind each perk's name.")
      .translation("modernhud.config.perk_highlight")
      .define("nameHighlight", true);
   private static final IntValue ANIMATION_MS = BUILDER.comment(
         new String[]{"Time in milliseconds for slots to slide between the queue and the ready row, and to", "fade."}
      )
      .translation("modernhud.config.ks_animation_ms")
      .defineInRange("animationMs", 420, 0, 3000);
   private static final BooleanValue SPECIALIST_HIDE_FINAL = BUILDER.comment("Specialist: hide the fourth (8 kill) slot. Only the three perk slots are shown.")
      .translation("modernhud.config.ks_specialist_hide_final")
      .define("specialistHideFinalStage", true);
   private static final BooleanValue SPECIALIST_STOP_WHEN_FULL = BUILDER.comment(
         new String[]{"Specialist: once every shown perk is earned, play the completion once, slide away and", "stop updating."}
      )
      .translation("modernhud.config.ks_specialist_stop_when_full")
      .define("specialistStopWhenFull", true);
   private static final IntValue PERK_STAGGER_MS = BUILDER.comment("Delay in milliseconds between one perk row sliding in or out and the next.")
      .translation("modernhud.config.perk_stagger_ms")
      .defineInRange("rowStaggerMs", 110, 0, 1000);
   private static final BooleanValue PERK_GLOW = BUILDER.comment("Soft glow behind each perk icon and around each perk's box.")
      .translation("modernhud.config.perk_glow")
      .define("glow", true);
   public static final ElementStyle slotStyle = new ElementStyle(
      BUILDER, "style", "ks", new ElementStyle.Defaults(true, 14278371, 16, PanelBorderStyle.BOTTOM_DOTS, 3, 1, 460, 460), 90
   );
   public static final ElementStyle perkStyle = new ElementStyle(
      BUILDER, "style", "perk", new ElementStyle.Defaults(false, 14278371, 14, PanelBorderStyle.CORNERS, 1, 0, 480, 420)
   );
   public static boolean enabled;
   public static boolean attachToWeaponHud;
   public static HudPosition position;
   public static int offsetX;
   public static int offsetY;
   public static double scaleFactor;
   public static int slotSize;
   public static int slotGap;
   public static int groupGap;
   public static boolean showProgressBar;
   public static boolean showSelection;
   public static int selectionSeconds;
   public static boolean autoHide;
   public static int autoHideSeconds;
   public static int readyColor;
   public static int barColor;
   public static int activeColor;
   public static boolean perkToast;
   public static HudPosition perkPosition;
   public static int perkOffsetX;
   public static int perkOffsetY;
   public static double perkScale;
   public static int perkHoldSeconds;
   public static int perkIconSize;
   public static boolean perkFrame;
   public static boolean perkHighlight;
   public static int animationMs;
   public static boolean specialistHideFinalStage;
   public static boolean specialistStopWhenFull;
   public static int perkStaggerMs;
   public static boolean perkGlow;

   @SubscribeEvent
   static void onLoad(ModConfigEvent event) {
      try {
         refresh();
      } catch (IllegalStateException var2) {
      }
   }

   public static void refresh() {
      enabled = (Boolean)ENABLED.get();
      attachToWeaponHud = (Boolean)ATTACH_TO_WEAPON_HUD.get();
      position = (HudPosition)POSITION.get();
      offsetX = (Integer)OFFSET_X.get();
      offsetY = (Integer)OFFSET_Y.get();
      scaleFactor = (Double)SCALE_FACTOR.get();
      slotSize = (Integer)SLOT_SIZE.get();
      slotGap = (Integer)SLOT_GAP.get();
      groupGap = (Integer)GROUP_GAP.get();
      showProgressBar = (Boolean)SHOW_PROGRESS_BAR.get();
      showSelection = (Boolean)SHOW_SELECTION.get();
      selectionSeconds = (Integer)SELECTION_SECONDS.get();
      autoHide = (Boolean)AUTO_HIDE.get();
      autoHideSeconds = (Integer)AUTO_HIDE_SECONDS.get();
      readyColor = ColorConfig.fromHex((String)READY_COLOR.get(), 10941836);
      barColor = ColorConfig.fromHex((String)BAR_COLOR.get(), 16777215);
      activeColor = ColorConfig.fromHex((String)ACTIVE_COLOR.get(), 10941836);
      perkToast = (Boolean)PERK_TOAST.get();
      perkPosition = (HudPosition)PERK_POSITION.get();
      perkOffsetX = (Integer)PERK_OFFSET_X.get();
      perkOffsetY = (Integer)PERK_OFFSET_Y.get();
      perkScale = (Double)PERK_SCALE.get();
      perkHoldSeconds = (Integer)PERK_HOLD_SECONDS.get();
      perkIconSize = (Integer)PERK_ICON_SIZE.get();
      perkFrame = (Boolean)PERK_FRAME.get();
      perkHighlight = (Boolean)PERK_HIGHLIGHT.get();
      animationMs = (Integer)ANIMATION_MS.get();
      specialistHideFinalStage = (Boolean)SPECIALIST_HIDE_FINAL.get();
      specialistStopWhenFull = (Boolean)SPECIALIST_STOP_WHEN_FULL.get();
      perkStaggerMs = (Integer)PERK_STAGGER_MS.get();
      perkGlow = (Boolean)PERK_GLOW.get();
      slotStyle.refresh();
      perkStyle.refresh();
   }

   static {
      BUILDER.comment(
            new String[]{
               "Killstreak and Specialist tracker: square slots and a segmented kill bar. Needs", "ModernLoadout, which does the tracking; this only draws it."
            }
         )
         .push("killstreak");
      BUILDER.pop();
      BUILDER.comment(
            new String[]{
               "Popup listing the perks you selected when you join or when the loadout changes. It", "slides in, holds, then slides out. Needs ModernLoadout."
            }
         )
         .push("perkToast");
      BUILDER.pop();
   }
}
