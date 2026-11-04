package cc.xypp.modernhud.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Layout options for the widgets that are not part of the original mod: the message bar, the remaining
 * inventory strip, and the Xaero minimap avoidance shared by both.
 */
public final class ExtraHudConfig {
   private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

   public static final ForgeConfigSpec.BooleanValue SHOW_MESSAGES = BUILDER
      .comment("Draw the recent chat lines as a HUD message bar.")
      .define("showMessages", true);

   public static final ForgeConfigSpec.BooleanValue HIDE_VANILLA_CHAT = BUILDER
      .comment(new String[] {
         "Stop the vanilla chat overlay from drawing at the bottom-left, so only the repositioned",
         "message bar shows. The chat input screen is unaffected: typing still works as before."
      })
      .define("hideVanillaChat", true);

   public static final ForgeConfigSpec.ConfigValue<String> MESSAGE_ANCHOR = BUILDER
      .comment("Message bar anchor on foot: TOP_LEFT / TOP_RIGHT / BOTTOM_LEFT / BOTTOM_RIGHT / CENTER_LEFT / CENTER_RIGHT.")
      .define("messageAnchor", "TOP_LEFT");

   public static final ForgeConfigSpec.ConfigValue<String> MESSAGE_VEHICLE_ANCHOR = BUILDER
      .comment("Message bar anchor while riding a vehicle. The minimap stays top-left, so this normally matches.")
      .define("messageVehicleAnchor", "TOP_LEFT");

   public static final ForgeConfigSpec.IntValue MESSAGE_X = BUILDER
      .comment("Message bar distance from its anchor, in HUD pixels.")
      .defineInRange("messageOffsetX", 12, 0, 4096);

   public static final ForgeConfigSpec.IntValue MESSAGE_Y = BUILDER
      .defineInRange("messageOffsetY", 12, 0, 4096);

   public static final ForgeConfigSpec.IntValue MESSAGE_WIDTH = BUILDER
      .comment("Backdrop width of the message bar.")
      .defineInRange("messageWidth", 220, 60, 600);

   public static final ForgeConfigSpec.IntValue MESSAGE_LINES = BUILDER
      .comment("How many recent chat lines to show.")
      .defineInRange("messageLines", 5, 1, 12);

   public static final ForgeConfigSpec.BooleanValue SHOW_REMAINING = BUILDER
      .comment("Draw the hotbar as two blocks: the first slots along the bottom, the residue on the right edge.")
      .define("showRemaining", true);

   public static final ForgeConfigSpec.BooleanValue HIDE_VANILLA_HUD = BUILDER
      .comment(new String[] {
         "Hide the vanilla hotbar, health, armour and hunger rows. The mod draws its own, so leaving the",
         "vanilla ones visible would double every reading."
      })
      .define("hideVanillaHud", true);

   public static final ForgeConfigSpec.ConfigValue<String> HOTBAR_ANCHOR = BUILDER
      .comment("Anchor of the first hotbar group: BOTTOM_CENTER keeps it where the vanilla hotbar sat.")
      .define("hotbarAnchor", "BOTTOM_RIGHT");

   public static final ForgeConfigSpec.IntValue HOTBAR_X = BUILDER
      .defineInRange("hotbarOffsetX", 8, 0, 4096);

   public static final ForgeConfigSpec.IntValue HOTBAR_Y = BUILDER
      .defineInRange("hotbarOffsetY", 104, 0, 4096);

   public static final ForgeConfigSpec.DoubleValue HOTBAR_SCALE = BUILDER
      .defineInRange("hotbarScale", 1.0, 0.5, 3.0);

   public static final ForgeConfigSpec.ConfigValue<String> REMAINING_ANCHOR = BUILDER
      .comment("Remaining-inventory anchor: the right-middle band is the default so it misses the kill info above and the hotbar below.")
      .define("remainingAnchor", "CENTER_RIGHT");

   public static final ForgeConfigSpec.IntValue REMAINING_X = BUILDER
      .comment("Remaining-inventory distance from its anchor, in HUD pixels.")
      .defineInRange("remainingOffsetX", 10, 0, 4096);

   public static final ForgeConfigSpec.IntValue REMAINING_Y = BUILDER
      .defineInRange("remainingOffsetY", 0, -4096, 4096);

   public static final ForgeConfigSpec.DoubleValue REMAINING_SCALE = BUILDER
      .defineInRange("remainingScale", 1.0, 0.5, 3.0);

   public static final ForgeConfigSpec.IntValue REMAINING_SPLIT = BUILDER
      .comment("Hotbar index where the strip starts. 6 means slots 7-9 are lifted out of the hotbar.")
      .defineInRange("remainingSplit", 6, 1, 9);

   public static final ForgeConfigSpec.IntValue STATUS_X = BUILDER
      .comment("Vitals block: distance from the left edge, in HUD pixels.")
      .defineInRange("statusOffsetX", 6, 0, 4096);

   public static final ForgeConfigSpec.IntValue STATUS_Y = BUILDER
      .comment("Vitals block: distance from the bottom edge. 0 puts it flush with the screen bottom.")
      .defineInRange("statusOffsetY", 2, 0, 4096);

   public static final ForgeConfigSpec.IntValue STATUS_WIDTH = BUILDER
      .comment("Vitals block: bar width, in HUD pixels.")
      .defineInRange("statusBarWidth", 120, 40, 400);

   public static final ForgeConfigSpec.BooleanValue AVOID_XAERO = BUILDER
      .comment("Push top-anchored widgets below Xaero's minimap so they never overlap it.")
      .define("avoidXaeroMinimap", true);

   public static final ForgeConfigSpec.IntValue XAERO_HEIGHT = BUILDER
      .comment("Minimap height used for the avoidance offset when Xaero is present.")
      .defineInRange("xaeroMinimapHeight", 128, 0, 512);

   public static final ForgeConfigSpec.IntValue XAERO_MARGIN = BUILDER
      .comment("Extra gap between the minimap and the widgets below it.")
      .defineInRange("xaeroMinimapMargin", 8, 0, 128);

   public static final ForgeConfigSpec SPEC = BUILDER.build();

   private ExtraHudConfig() {
   }
}
