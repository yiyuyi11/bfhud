package cc.xypp.modernhud.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.config.ModConfigEvent;

public class HitIndicationConfig {
   private static final Builder BUILDER = new Builder();
   public static final ModConfigSpec SPEC = BUILDER.build();
   private static final BooleanValue ENABLE = BUILDER.comment("Master switch for the directional damage arc.")
      .translation("modernhud.config.hit_enable")
      .define("enableHitIndication", true);
   private static final BooleanValue IGNORE_HIDE_GUI = BUILDER.comment("Keep the arc and hit markers visible when F1 (hide GUI) is on.")
      .translation("modernhud.config.hit_ignore_hide_gui")
      .define("ignoreHideGui", true);
   private static final IntValue RADIUS = BUILDER.comment("Distance (px) of the arc from the crosshair.")
      .translation("modernhud.config.hit_radius")
      .defineInRange("radius", 120, 20, 400);
   private static final IntValue ARC_SPAN = BUILDER.comment("How wide the arc is, in degrees of the circle around the crosshair.")
      .translation("modernhud.config.hit_arc_span")
      .defineInRange("arcSpanDegrees", 38, 8, 120);
   private static final IntValue THICKNESS = BUILDER.comment("Thickness of the arc's solid core in pixels. The glow adds a soft edge.")
      .translation("modernhud.config.hit_thickness")
      .defineInRange("thickness", 4, 1, 16);
   private static final IntValue OPACITY = BUILDER.comment("Peak opacity of the arc, 0-100.")
      .translation("modernhud.config.hit_opacity")
      .defineInRange("opacity", 90, 0, 100);
   private static final IntValue FADE_IN_MS = BUILDER.comment("Time (ms) for the arc to flash in.")
      .translation("modernhud.config.hit_fade_in_ms")
      .defineInRange("fadeInMs", 60, 0, 1000);
   private static final IntValue HOLD_MS = BUILDER.comment("Time (ms) the arc stays at full strength.")
      .translation("modernhud.config.hit_hold_ms")
      .defineInRange("holdMs", 350, 0, 5000);
   private static final IntValue FADE_OUT_MS = BUILDER.comment("Time (ms) for the arc to fade away.")
      .translation("modernhud.config.hit_fade_out_ms")
      .defineInRange("fadeOutMs", 450, 0, 5000);
   private static final IntValue MAX_COUNT = BUILDER.comment("Most arcs shown at once. 0 means unlimited. The oldest is dropped first.")
      .translation("modernhud.config.hit_max_count")
      .defineInRange("maxIndicatorCount", 0, 0, 32);
   private static final BooleanValue MERGE = BUILDER.comment(
         new String[]{"Hits from nearly the same direction refresh the existing arc instead of adding a new", "one."}
      )
      .translation("modernhud.config.hit_merge")
      .define("mergeSameDirection", true);
   private static final BooleanValue GLOW = BUILDER.comment("Soft glow around the arc. Quality and strength follow the global glow settings.")
      .translation("modernhud.config.hit_glow")
      .define("glow", false);
   private static final ConfigValue<String> HIT_COLOR = BUILDER.comment("Arc color as a hex code.")
      .translation("modernhud.config.hit_color")
      .define("hitIndicatorColor", ColorConfig.toHex(16711680));
   private static final BooleanValue ABSORPTION_BLUE = BUILDER.comment(
         new String[]{"Draw the arc in absorptionIndicatorColor while you still have Absorption left. It", "turns red once the Absorption is gone."}
      )
      .translation("modernhud.config.hit_absorption_blue")
      .define("absorptionBlueIndicator", true);
   private static final ConfigValue<String> ABSORPTION_COLOR = BUILDER.comment("Arc color while Absorption is still up, as a hex code.")
      .translation("modernhud.config.hit_absorption_color")
      .define("absorptionIndicatorColor", ColorConfig.toHex(3116287));
   private static final BooleanValue ENABLE_HIT_MARKERS = BUILDER.comment("Show a brief crosshair marker on landing a critical hit or a kill.")
      .translation("modernhud.config.hit_enable_hit_markers")
      .define("enableHitMarkers", false);
   public static boolean enableHitIndication;
   public static boolean ignoreHideGui;
   public static int radius;
   public static int arcSpanDegrees;
   public static int thickness;
   public static int opacity;
   public static int fadeInMs;
   public static int holdMs;
   public static int fadeOutMs;
   public static int maxIndicatorCount;
   public static boolean mergeSameDirection;
   public static boolean glow;
   public static int hitIndicatorColor;
   public static boolean absorptionBlueIndicator;
   public static int absorptionIndicatorColor;
   public static boolean enableHitMarkers;

   @SubscribeEvent
   static void onLoad(ModConfigEvent event) {
      try {
         refresh();
      } catch (IllegalStateException var2) {
      }
   }

   public static void refresh() {
      enableHitIndication = (Boolean)ENABLE.get();
      ignoreHideGui = (Boolean)IGNORE_HIDE_GUI.get();
      radius = (Integer)RADIUS.get();
      arcSpanDegrees = (Integer)ARC_SPAN.get();
      thickness = (Integer)THICKNESS.get();
      opacity = (Integer)OPACITY.get();
      fadeInMs = (Integer)FADE_IN_MS.get();
      holdMs = (Integer)HOLD_MS.get();
      fadeOutMs = (Integer)FADE_OUT_MS.get();
      maxIndicatorCount = (Integer)MAX_COUNT.get();
      mergeSameDirection = (Boolean)MERGE.get();
      glow = (Boolean)GLOW.get();
      hitIndicatorColor = ColorConfig.fromHex((String)HIT_COLOR.get(), 16726832);
      absorptionBlueIndicator = (Boolean)ABSORPTION_BLUE.get();
      absorptionIndicatorColor = ColorConfig.fromHex((String)ABSORPTION_COLOR.get(), 3116287);
      enableHitMarkers = (Boolean)ENABLE_HIT_MARKERS.get();
   }

   static {
      BUILDER.comment(new String[]{"Damage direction: an arc around the crosshair that points at whoever hit you and", "rotates with your view."})
         .push("hitIndication");
      BUILDER.pop();
   }
}
