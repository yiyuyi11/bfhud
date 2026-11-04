package cc.xypp.modernhud.config;

import cc.xypp.modernhud.client.compat.TaczCompat;
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

public class WeaponConfig {
   private static final Builder BUILDER = new Builder();
   private static final EnumValue<WeaponHudSource> PREFERRED_HUD_SOURCE = BUILDER.comment(
         new String[]{
            "Which weapon HUD is shown: TACZ_HUD (TACZ's own), TACZ_P_HUD (TaczPresence, falls back",
            "to TACZ_HUD if missing) or MODERNHUD (this mod). Only the chosen one is drawn."
         }
      )
      .translation("modernhud.config.weapon_hud_source")
      .defineEnum("hudSource", WeaponHudSource.MODERNHUD);
   private static final BooleanValue IGNORE_HIDE_GUI = BUILDER.comment(
         new String[]{"Keep the weapon widget and ammo alerts visible when F1 (hide GUI) is on. Everything", "else stays hidden."}
      )
      .translation("modernhud.config.weapon_ignore_hide_gui")
      .define("ignoreHideGui", false);
   private static final BooleanValue SHOW_NAME = BUILDER.comment("Show the held gun's name under the ammo numbers.")
      .translation("modernhud.config.weapon_show_name")
      .define("showName", true);
   private static final BooleanValue SHOW_FIRE_MODE = BUILDER.comment("Show the current fire mode (SEMI, AUTO or BURST).")
      .translation("modernhud.config.weapon_show_fire_mode")
      .define("showFireMode", true);
   private static final EnumValue<HeatDisplayMode> HEAT_DISPLAY_MODE = BUILDER.comment(
         new String[]{
            "How overheat is shown for guns with TACZ's heat mechanic (such as the minigun). NONE",
            "shows nothing, BAR adds a thin bar under the fire-mode row, FLASH makes the icon box",
            "glow and pulse faster as it heats up. TACZ's own heat display is only available by",
            "switching hudSource."
         }
      )
      .translation("modernhud.config.weapon_heat_display_mode")
      .defineEnum("heatDisplayMode", HeatDisplayMode.FLASH);
   private static final ConfigValue<String> HEAT_BAR_COLOR = BUILDER.comment("BAR mode: bar color while below the overheat-locked threshold, as a hex code.")
      .translation("modernhud.config.weapon_heat_bar_color")
      .define("heatBarColor", ColorConfig.toHex(6277352));
   private static final ConfigValue<String> HEAT_BAR_LOCKED_COLOR = BUILDER.comment(
         new String[]{"Color once the gun is overheated and locked. FLASH mode also uses it as the hot end of", "the glow."}
      )
      .translation("modernhud.config.weapon_heat_bar_locked_color")
      .define("heatBarLockedColor", ColorConfig.toHex(15225144));
   private static final ConfigValue<String> HEAT_FLASH_COLOR = BUILDER.comment(
         new String[]{"FLASH mode: glow color when the gun is cold. It fades to heatBarLockedColor as heat", "rises."}
      )
      .translation("modernhud.config.weapon_heat_flash_color")
      .define("heatFlashColor", ColorConfig.toHex(16767078));
   private static final BooleanValue SHOW_RESERVE_AMMO = BUILDER.comment(
         new String[]{"Show reserve ammo next to the magazine count. Hidden for guns that use inventory ammo", "directly."}
      )
      .translation("modernhud.config.weapon_show_reserve")
      .define("showReserveAmmo", true);
   private static final EnumValue<HudPosition> POSITION = BUILDER.comment(
         new String[]{"Screen corner the widget is attached to. CUSTOM uses the offsets as absolute", "coordinates."}
      )
      .translation("modernhud.config.weapon_position")
      .defineEnum("anchor", HudPosition.BOTTOM_RIGHT);
   private static final IntValue OFFSET_X = BUILDER.comment("Horizontal distance from the anchor in pixels (absolute X when anchor is CUSTOM).")
      .translation("modernhud.config.weapon_offset_x")
      .defineInRange("offsetX", 48, -10000, 10000);
   private static final IntValue OFFSET_Y = BUILDER.comment("Vertical distance from the anchor in pixels (absolute Y when anchor is CUSTOM).")
      .translation("modernhud.config.weapon_offset_y")
      .defineInRange("offsetY", 30, -10000, 10000);
   private static final DoubleValue SCALE = BUILDER.comment("Overall scale multiplier applied to the whole weapon widget.")
      .translation("modernhud.config.weapon_scale")
      .defineInRange("scale", 0.9, 0.1, 5.0);
   private static final DoubleValue ICON_SCALE = BUILDER.comment("Scale of TACZ's fixed 39x13 icon canvas. 1.5 draws it at about 58x19.")
      .translation("modernhud.config.weapon_icon_scale")
      .defineInRange("iconScale", 1.1, 0.25, 6.0);
   private static final DoubleValue AMMO_TEXT_SCALE = BUILDER.comment(
         "Scale of the big mag-ammo number specifically (independent of the overall widget scale)."
      )
      .translation("modernhud.config.weapon_ammo_text_scale")
      .defineInRange("ammoTextScale", 1.5, 0.5, 4.0);
   private static final IntValue FRAME_WIDTH = BUILDER.comment("Width of the whole widget in pixels, before scaling. Used for screen-edge anchoring.")
      .translation("modernhud.config.weapon_panel_width")
      .defineInRange("frameWidth", 129, 32, 1000);
   private static final IntValue FRAME_HEIGHT = BUILDER.comment(
         new String[]{"Height of the whole widget in pixels, before scaling. Must fit the icon plus the", "secondary weapon slot under it."}
      )
      .translation("modernhud.config.weapon_panel_height")
      .defineInRange("frameHeight", 40, 16, 1000);
   private static final BooleanValue ZERO_PAD_AMMO = BUILDER.comment("Pad ammo numbers to three digits (030 instead of 30).")
      .translation("modernhud.config.weapon_zero_pad_ammo")
      .define("zeroPadAmmo", true);
   private static final BooleanValue SHOW_SECONDARY = BUILDER.comment("Show the secondary weapon thumbnail.")
      .translation("modernhud.config.weapon_show_secondary")
      .define("showSecondary", true);
   private static final DoubleValue SECONDARY_ICON_SCALE = BUILDER.comment("Scale of the secondary weapon icon. Keep it below iconScale.")
      .translation("modernhud.config.weapon_secondary_icon_scale")
      .defineInRange("secondaryIconScale", 0.85, 0.15, 4.0);
   private static final DoubleValue SECONDARY_ALPHA = BUILDER.comment("Opacity of the secondary weapon icon, 0 to 1.")
      .translation("modernhud.config.weapon_secondary_alpha")
      .defineInRange("secondaryAlpha", 0.8, 0.1, 1.0);
   private static final BooleanValue SHOW_PANEL = BUILDER.comment("Draw a background box behind the icon boxes and the fire-mode row.")
      .translation("modernhud.config.weapon_show_panel")
      .define("enabled", true);
   private static final ConfigValue<String> PANEL_COLOR = BUILDER.comment("Panel background color, as a hex code (e.g. \"0A0D11\" or \"#0A0D11\").")
      .translation("modernhud.config.weapon_panel_color")
      .define("panelColor", ColorConfig.toHex(14278371));
   private static final IntValue PANEL_OPACITY = BUILDER.comment("Box opacity, 0 to 100. The secondary weapon box is also scaled by secondaryAlpha.")
      .translation("modernhud.config.weapon_panel_opacity")
      .defineInRange("panelOpacity", 10, 0, 100);
   private static final EnumValue<PanelBorderStyle> BORDER_STYLE = BUILDER.comment(
         new String[]{"Border style: NONE, CORNERS (short L-shaped brackets), FULL (thin outline), DOTS,", "BOTTOM_DOTS or FRAME."}
      )
      .translation("modernhud.config.weapon_border_style")
      .defineEnum("borderStyle", PanelBorderStyle.CORNERS);
   private static final ConfigValue<String> BORDER_COLOR = BUILDER.comment("Border/corner-bracket color, as a hex code (e.g. \"E8ECF0\" or \"#E8ECF0\").")
      .translation("modernhud.config.weapon_border_color")
      .define("borderColor", ColorConfig.toHex(15265008));
   private static final IntValue BORDER_OPACITY = BUILDER.comment("Border opacity, 0 to 100.")
      .translation("modernhud.config.weapon_border_opacity")
      .defineInRange("borderOpacity", 90, 0, 100);
   private static final IntValue BORDER_LENGTH = BUILDER.comment("Length of each bracket arm in pixels. Capped to a fraction of each box's size.")
      .translation("modernhud.config.weapon_border_length")
      .defineInRange("borderLength", 2, 1, 20);
   private static final IntValue BORDER_OUTSET = BUILDER.comment(
         new String[]{"How far brackets and dots sit outside the box edge, in pixels. 0 puts them on the", "edge."}
      )
      .translation("modernhud.config.weapon_border_outset")
      .defineInRange("borderOutset", 1, 0, 20);
   private static final BooleanValue SHOW_WEAPON_GLOW = BUILDER.comment(
         new String[]{"Flash behind the main icon when you switch weapons, and a red glow around the ammo row", "while the magazine is low."}
      )
      .translation("modernhud.config.weapon_show_glow")
      .define("glowEnabled", true);
   private static final ConfigValue<String> WEAPON_GLOW_COLOR = BUILDER.comment(
         new String[]{
            "The one glow color of the weapon widget, as a hex code. Used by the switch flash, the",
            "corner glow of the selected weapon box and the ammo glow. The low-ammo glow uses",
            "ammoLowColor."
         }
      )
      .translation("modernhud.config.weapon_glow_color")
      .define("weaponGlowColor", ColorConfig.toHex(15915405));
   private static final BooleanValue SHOW_AMMO_TYPE_ICON = BUILDER.comment("Small bullet icon next to the reserve count, tinted like the reserve number.")
      .translation("modernhud.config.weapon_show_ammo_type_icon")
      .define("showAmmoTypeIcon", true);
   private static final DoubleValue AMMO_TYPE_ICON_SCALE = BUILDER.comment("Size of that icon relative to the reserve number's height.")
      .translation("modernhud.config.weapon_ammo_type_icon_scale")
      .defineInRange("ammoTypeIconScale", 0.7, 0.1, 1.5);
   private static final BooleanValue SHOW_ATTACHMENT_DOTS = BUILDER.comment(
         new String[]{"Row of small squares above the main icon, one per attachment slot the gun has. Filled", "gold when equipped, dim when empty."}
      )
      .translation("modernhud.config.weapon_show_attachment_dots")
      .define("showAttachmentDots", true);
   private static final ConfigValue<String> ATTACHMENT_DOT_COLOR = BUILDER.comment("Color of filled attachment dots, as a hex code. Empty slots draw nothing.")
      .translation("modernhud.config.weapon_attachment_dot_color")
      .define("attachmentDotColor", ColorConfig.toHex(16763210));
   private static final IntValue SECONDARY_DOT_SIZE = BUILDER.comment("Size in pixels of the two corner squares on the secondary weapon box.")
      .translation("modernhud.config.weapon_secondary_dot_size")
      .defineInRange("secondaryDotSize", 2, 1, 12);
   private static final BooleanValue SHOW_SWITCH_SLIDE = BUILDER.comment("Icon box slides up into place when you switch weapons.")
      .translation("modernhud.config.weapon_show_switch_slide")
      .define("showSwitchSlide", true);
   private static final IntValue SWITCH_SLIDE_DISTANCE = BUILDER.comment(
         new String[]{"How far below its final position the icon starts on a switch, in pixels, before", "scaling."}
      )
      .translation("modernhud.config.weapon_switch_slide_distance")
      .defineInRange("switchSlideDistance", 8, 0, 100);
   private static final BooleanValue SHOW_SWITCH_BORDER_FLASH = BUILDER.comment("Brief gold border flash around the main icon box on a weapon switch.")
      .translation("modernhud.config.weapon_show_switch_border_flash")
      .define("showSwitchBorderFlash", true);
   private static final ConfigValue<String> SWITCH_BORDER_FLASH_COLOR = BUILDER.comment("Switch border-flash color, as a hex code. Defaults to a warm gold.")
      .translation("modernhud.config.weapon_switch_border_flash_color")
      .define("switchBorderFlashColor", ColorConfig.toHex(16763210));
   private static final IntValue SWITCH_BORDER_FLASH_MS = BUILDER.comment("Time in milliseconds for the flash to fade back to the normal border color.")
      .translation("modernhud.config.weapon_switch_border_flash_ms")
      .defineInRange("switchBorderFlashMs", 450, 50, 5000);
   private static final ConfigValue<String> AMMO_COLOR = BUILDER.comment("Mag ammo number color above the low-ammo threshold.")
      .translation("modernhud.config.weapon_ammo_color")
      .define("ammoColor", ColorConfig.toHex(16777215));
   private static final ConfigValue<String> AMMO_LOW_COLOR = BUILDER.comment("Mag ammo number color at/below the low-ammo threshold.")
      .translation("modernhud.config.weapon_ammo_low_color")
      .define("ammoLowColor", ColorConfig.toHex(15225144));
   private static final DoubleValue LOW_AMMO_THRESHOLD_PERCENT = BUILDER.comment(
         new String[]{"Below this percentage of magazine size the number uses the low-ammo color and the LOW", "AMMO alert appears."}
      )
      .translation("modernhud.config.weapon_low_ammo_threshold")
      .defineInRange("lowAmmoThresholdPercent", 20.0, 0.0, 100.0);
   private static final ConfigValue<String> RESERVE_COLOR = BUILDER.comment("Reserve number color, before reserveOpacity.")
      .translation("modernhud.config.weapon_reserve_color")
      .define("reserveColor", ColorConfig.toHex(15922938));
   private static final DoubleValue RESERVE_SCALE_OFFSET = BUILDER.comment(
         new String[]{
            "How much smaller the reserve number is than the magazine number. 0 makes them the same size,",
            "as in the reference. With pixel-perfect text any value above 0 drops the reserve by one whole",
            "screen pixel per font pixel, which is a big step at small sizes."
         }
      )
      .translation("modernhud.config.weapon_reserve_scale_offset")
      .defineInRange("reserveSizeDrop", 0.05, 0.0, 1.0);
   private static final IntValue RESERVE_OPACITY = BUILDER.comment("Reserve number opacity, 0 to 100.")
      .translation("modernhud.config.weapon_reserve_opacity")
      .defineInRange("reserveOpacity", 70, 0, 100);
   private static final ConfigValue<String> NAME_COLOR = BUILDER.comment("Weapon name text color.")
      .translation("modernhud.config.weapon_name_color")
      .define("nameColor", ColorConfig.toHex(15134453));
   private static final ConfigValue<String> FIRE_MODE_COLOR = BUILDER.comment("Fire mode text color.")
      .translation("modernhud.config.weapon_fire_mode_color")
      .define("fireModeColor", ColorConfig.toHex(10474480));
   private static final BooleanValue SHOW_FIRE_TYPE_FLASH = BUILDER.comment("Show the \"FIRE TYPE: ...\" flash announcement on weapon switch.")
      .translation("modernhud.config.weapon_show_fire_type_flash")
      .define("enabled", true);
   private static final ConfigValue<String> FIRE_TYPE_FLASH_COLOR = BUILDER.comment("Color during the initial flash phase, hex code.")
      .translation("modernhud.config.weapon_fire_type_flash_color")
      .define("flashColor", ColorConfig.toHex(16767078));
   private static final ConfigValue<String> FIRE_TYPE_HOLD_COLOR = BUILDER.comment("Color while the text is held, as a hex code.")
      .translation("modernhud.config.weapon_fire_type_hold_color")
      .define("holdColor", ColorConfig.toHex(16777215));
   private static final IntValue FIRE_TYPE_FLASH_MS = BUILDER.comment("How long the flash color lasts in milliseconds.")
      .translation("modernhud.config.weapon_fire_type_flash_ms")
      .defineInRange("flashMs", 150, 0, 5000);
   private static final IntValue FIRE_TYPE_HOLD_MS = BUILDER.comment("How long the held color stays fully visible before fading, in milliseconds.")
      .translation("modernhud.config.weapon_fire_type_hold_ms")
      .defineInRange("holdMs", 900, 0, 20000);
   private static final IntValue FIRE_TYPE_FADE_MS = BUILDER.comment("How long (ms) the fade-out takes once the hold period ends.")
      .translation("modernhud.config.weapon_fire_type_fade_ms")
      .defineInRange("fadeMs", 400, 0, 20000);
   private static final DoubleValue FIRE_TYPE_SCALE = BUILDER.comment("Text scale of the announcement.")
      .translation("modernhud.config.weapon_fire_type_scale")
      .defineInRange("scale", 1.0, 0.25, 4.0);
   private static final IntValue FIRE_TYPE_X_OFFSET = BUILDER.comment("Horizontal offset in px from screen center (positive = further right).")
      .translation("modernhud.config.weapon_fire_type_x_offset")
      .defineInRange("xOffset", 0, -2000, 2000);
   private static final IntValue FIRE_TYPE_Y_OFFSET = BUILDER.comment(
         new String[]{"Vertical offset from screen center in pixels. Positive moves it down, negative moves", "it up."}
      )
      .translation("modernhud.config.weapon_fire_type_y_offset")
      .defineInRange("yOffset", 70, -2000, 2000);
   private static final BooleanValue FIRE_TYPE_SHOW_LABEL = BUILDER.comment("true shows FIRE TYPE: FULL AUTO, false shows just FULL AUTO.")
      .translation("modernhud.config.weapon_fire_type_show_label")
      .define("showLabel", true);
   private static final BooleanValue SHOW_AMMO_ALERTS = BUILDER.comment("Show the RELOAD / NO AMMO / LOW AMMO pulsing alert text.")
      .translation("modernhud.config.weapon_show_ammo_alerts")
      .define("enabled", true);
   private static final ConfigValue<String> RELOAD_ALERT_COLOR = BUILDER.comment("\"RELOAD\" alert color (mag empty, reserve available), hex code.")
      .translation("modernhud.config.weapon_reload_alert_color")
      .define("reloadAlertColor", ColorConfig.toHex(16767078));
   private static final ConfigValue<String> NO_AMMO_ALERT_COLOR = BUILDER.comment("\"NO AMMO\" alert color (mag AND reserve both empty), hex code.")
      .translation("modernhud.config.weapon_no_ammo_alert_color")
      .define("noAmmoAlertColor", ColorConfig.toHex(15225144));
   private static final ConfigValue<String> LOW_AMMO_ALERT_COLOR = BUILDER.comment(
         "\"LOW AMMO\" alert color (mag below the low-ammo threshold above), hex code."
      )
      .translation("modernhud.config.weapon_low_ammo_alert_color")
      .define("lowAmmoAlertColor", ColorConfig.toHex(16767078));
   private static final DoubleValue AMMO_ALERT_SCALE = BUILDER.comment("Scale of the alert text.")
      .translation("modernhud.config.weapon_ammo_alert_scale")
      .defineInRange("ammoAlertScale", 1.0, 0.25, 4.0);
   private static final IntValue AMMO_ALERT_Y_OFFSET = BUILDER.comment("Vertical offset in px from screen center (positive = further down).")
      .translation("modernhud.config.weapon_ammo_alert_y_offset")
      .defineInRange("ammoAlertYOffset", 55, -2000, 2000);
   private static final BooleanValue AMMO_ALERT_PULSE = BUILDER.comment("The alert text pulses (breathes) while shown. Off = steady.")
      .translation("modernhud.config.weapon_ammo_alert_pulse")
      .define("ammoAlertPulse", false);
   private static final BooleanValue AMMO_ALERT_GLOW = BUILDER.comment("Soft glow in the alert's color behind the text.")
      .translation("modernhud.config.weapon_ammo_alert_glow")
      .define("ammoAlertGlow", false);
   private static final BooleanValue SHOW_GEAR = BUILDER.comment("Show the gear slots (melee on top, throwable / consumable below).")
      .translation("modernhud.config.weapon_show_gear")
      .define("showGear", false);
   private static final BooleanValue GEAR_COUNT_WHOLE_INVENTORY = BUILDER.comment(
         new String[]{"false counts hotbar items only. true counts the whole inventory (items are still found", "in the hotbar)."}
      )
      .translation("modernhud.config.weapon_gear_whole_inventory")
      .define("countWholeInventory", false);
   private static final BooleanValue GEAR_GLOW = BUILDER.comment("Soft round glow behind the gear icons.")
      .translation("modernhud.config.weapon_gear_glow")
      .define("glow", false);
   private static final BooleanValue GEAR_TOP_DOT_INNER = BUILDER.comment(
         new String[]{"Corner dots on the throwable slots. true puts the top dots on the outer corners, false", "on the inner corners facing each other."}
      )
      .translation("modernhud.config.weapon_gear_top_dot_inner")
      .define("topDotsOuter", false);
   private static final BooleanValue GEAR_AUTO_LAYOUT = BUILDER.comment(
         new String[]{
            "Only reserve room for slots that are in use. With no gear the weapon box moves into",
            "the corner, with only throwables the column shrinks to fit them."
         }
      )
      .translation("modernhud.config.weapon_gear_auto_layout")
      .define("autoLayout", true);
   private static final BooleanValue SEL_GLOW_ENABLED = BUILDER.comment("Glow in the inner corners of the selected weapon box, drawn behind the gun icon.")
      .translation("modernhud.config.weapon_sel_glow")
      .define("selectedGlow", true);
   private static final IntValue SEL_GLOW_OPACITY = BUILDER.comment("Strength of the corner glow, 0-100 (also scaled by the global glow intensity).")
      .translation("modernhud.config.weapon_sel_glow_opacity")
      .defineInRange("selectedGlowStrength", 80, 0, 100);
   private static final IntValue SEL_GLOW_RADIUS = BUILDER.comment("How far each corner glow reaches into the box, in pixels before scaling.")
      .translation("modernhud.config.weapon_sel_glow_radius")
      .defineInRange("selectedGlowReach", 30, 4, 64);
   private static final IntValue SEL_GLOW_TL = BUILDER.comment("Glow strength in the top-left corner, 0 to 100. 0 turns that corner off.")
      .translation("modernhud.config.weapon_sel_glow_tl")
      .defineInRange("selectedGlowTopLeft", 100, 0, 100);
   private static final IntValue SEL_GLOW_TR = BUILDER.comment("Glow strength in the top-right inner corner, 0 to 100.")
      .translation("modernhud.config.weapon_sel_glow_tr")
      .defineInRange("selectedGlowTopRight", 0, 0, 100);
   private static final IntValue SEL_GLOW_BR = BUILDER.comment("Glow strength in the bottom-right corner, 0 to 100.")
      .translation("modernhud.config.weapon_sel_glow_br")
      .defineInRange("selectedGlowBottomRight", 100, 0, 100);
   private static final IntValue SEL_GLOW_BL = BUILDER.comment("Glow strength in the bottom-left inner corner, 0 to 100.")
      .translation("modernhud.config.weapon_sel_glow_bl")
      .defineInRange("selectedGlowBottomLeft", 0, 0, 100);
   private static final BooleanValue AMMO_GLOW = BUILDER.comment(
         new String[]{"Glow for the ammo box: a light fill inside the border plus a halo on the magazine", "digits."}
      )
      .translation("modernhud.config.weapon_ammo_glow")
      .define("enabled", true);
   private static final IntValue AMMO_GLOW_OPACITY = BUILDER.comment("Strength of the halo around the magazine digits, 0 to 100.")
      .translation("modernhud.config.weapon_ammo_glow_opacity")
      .defineInRange("opacity", 35, 0, 100);
   private static final IntValue AMMO_GLOW_SPREAD = BUILDER.comment("How far the fill fades out past the border, in pixels before scaling.")
      .translation("modernhud.config.weapon_ammo_glow_spread")
      .defineInRange("spread", 5, 0, 30);
   private static final IntValue AMMO_GLOW_BOX = BUILDER.comment("Strength of the soft haze behind the ammo digits, 0 to 100. 0 turns it off.")
      .translation("modernhud.config.weapon_ammo_glow_box")
      .defineInRange("strength", 45, 0, 100);
   private static final IntValue AMMO_GLOW_HALO = BUILDER.comment("Reach of the digit halo in font pixels. 0 turns the halo off.")
      .translation("modernhud.config.weapon_ammo_glow_halo")
      .defineInRange("haloRadius", 2, 0, 4);
   private static final BooleanValue AMMO_GLOW_FOLLOW_LOW = BUILDER.comment("Color the digit halo with the low-ammo color while the magazine is low.")
      .translation("modernhud.config.weapon_ammo_glow_follow_low")
      .define("followLowAmmo", true);
   private static final BooleanValue AMMO_GLOW_PULSE = BUILDER.comment("Slowly breathe the haze and the digit halo in and out.")
      .translation("modernhud.config.weapon_ammo_glow_pulse")
      .define("pulse", false);
   private static final BooleanValue AMMO_GLOW_LOW_PULSE = BUILDER.comment(
         new String[]{"While the magazine is low, the red glow around the ammo row pulses (true) or stays", "steady (false)."}
      )
      .translation("modernhud.config.weapon_ammo_glow_low_pulse")
      .define("lowAmmoPulse", false);
   private static final IntValue INFO_BOX_RISE = BUILDER.comment(
         new String[]{
            "Height of the info box (ammo, name, fire mode): how many pixels it rises above the top of the main",
            "weapon box. Its bottom edge always lines up with the bottom of the secondary weapon box.",
            "0 makes the tops flush, negative values make the box shorter than the weapon box.",
            "The three rows stay tightly packed and centred; extra height only adds margin. Never smaller than the rows need."
         }
      )
      .translation("modernhud.config.weapon_info_box_rise")
      .defineInRange("infoBoxRise", 5, -10, 40);
   private static final BooleanValue AMMO_GLOW_RESERVE_PANEL = BUILDER.comment(
         "Small box in the glow color behind the reserve count. It replaces the divider line."
      )
      .translation("modernhud.config.weapon_ammo_glow_reserve_panel")
      .define("reservePanel", true);
   private static final IntValue AMMO_GLOW_RESERVE_PANEL_OPACITY = BUILDER.comment("Opacity of the reserve box, 0 to 100.")
      .translation("modernhud.config.weapon_ammo_glow_reserve_panel_opacity")
      .defineInRange("reservePanelOpacity", 25, 0, 100);
   private static final IntValue AMMO_GLOW_RESERVE_PANEL_HEIGHT = BUILDER.comment(
         "Height of the reserve box as a percentage of the digit height. 100 matches the digits, 90 is slightly shorter."
      )
      .translation("modernhud.config.weapon_ammo_glow_reserve_panel_height")
      .defineInRange("reservePanelHeight", 90, 40, 160);
   private static final BooleanValue SEL_GLOW_PULSE = BUILDER.comment("Slowly breathe the glow in and out instead of keeping it steady.")
      .translation("modernhud.config.weapon_sel_glow_pulse")
      .define("selectedGlowPulse", false);
   private static final IntValue SEL_GLOW_PULSE_MS = BUILDER.comment("Length of one pulse in milliseconds. Only used when selectedGlowPulse is on.")
      .translation("modernhud.config.weapon_sel_glow_pulse_ms")
      .defineInRange("selectedGlowPulseMs", 1800, 300, 6000);
   private static final IntValue SEL_GLOW_PULSE_DEPTH = BUILDER.comment("How far the pulse dips, 0 to 100. 100 fades the glow out completely at the low point.")
      .translation("modernhud.config.weapon_sel_glow_pulse_depth")
      .defineInRange("selectedGlowPulseDepth", 50, 0, 100);
   private static final IntValue SEL_GLOW_SWITCH_BOOST = BUILDER.comment("Extra brightness, 0 to 100, that flashes in when you switch weapons and fades out.")
      .translation("modernhud.config.weapon_sel_glow_switch_boost")
      .defineInRange("selectedGlowSwitchBoost", 40, 0, 100);
   public static final ElementStyle gearStyle = new ElementStyle(
      BUILDER, "style", "gear", new ElementStyle.Defaults(true, 14278371, 10, PanelBorderStyle.DOTS, 2, 0, 200, 200)
   );
   public static final ElementStyle infoBoxStyle = new ElementStyle(
      BUILDER, "style", "infobox", new ElementStyle.Defaults(false, 14278371, 10, PanelBorderStyle.CORNERS, 2, 1, 0, 0)
   );
   private static final IntValue INFO_NAME_GAP = BUILDER.comment("Gap in pixels between the ammo numbers and the gun name (0 = touching).")
      .translation("modernhud.config.weapon_info_name_gap")
      .defineInRange("nameGap", 1, 0, 16);
   private static final IntValue INFO_ROW_GAP = BUILDER.comment("Gap in pixels between the gun name and the fire mode row.")
      .translation("modernhud.config.weapon_info_row_gap")
      .defineInRange("rowGap", 2, 0, 16);
   private static final IntValue INFO_BOX_GAP = BUILDER.comment(
         new String[]{
            "Gap in pixels between the info box and the weapon boxes. The box gets narrower by the same amount,",
            "so raise appearance.frameWidth by that much to keep its width. Negative values pull it closer."
         }
      )
      .translation("modernhud.config.weapon_info_box_gap")
      .defineInRange("gapToWeapon", 6, -6, 40);
   private static final IntValue INFO_CONTENT_RAISE = BUILDER.comment(
         new String[]{
            "Moves everything inside the info box (ammo, name, fire mode) up by this many pixels. The box stays put.",
            "Capped so the rows never leave the box. Negative values move them down."
         }
      )
      .translation("modernhud.config.weapon_info_content_raise")
      .defineInRange("contentRaise", 4, -10, 30);
   private static final IntValue INFO_CONTENT_SCALE = BUILDER.comment(
         new String[]{
            "Size of the ammo digits, the reserve icon and the fire mode control inside the info box, in percent.",
            "With pixel-perfect text the digits change in whole-pixel steps, so small changes may not show."
         }
      )
      .translation("modernhud.config.weapon_info_content_scale")
      .defineInRange("contentScale", 92, 50, 130);
   public static int infoNameGap;
   public static int infoRowGap;
   public static int infoBoxGap;
   public static int infoContentRaise;
   public static int infoContentScale;
   public static final ElementStyle alertStyle = new ElementStyle(
      BUILDER, "style", "alert", new ElementStyle.Defaults(false, 14278371, 16, PanelBorderStyle.NONE, 3, 1, 160, 260)
   );
   public static final ForgeConfigSpec SPEC = BUILDER.build();
   public static boolean enabled = true;

   public static void toggleEnabled() {
      enabled = !enabled;
   }
   public static WeaponHudSource preferredHudSource;
   public static boolean ignoreHideGui;
   public static boolean showName;
   public static boolean showFireMode;
   public static HeatDisplayMode heatDisplayMode;
   public static int heatBarColor;
   public static int heatBarLockedColor;
   public static int heatFlashColor;
   public static boolean showReserveAmmo;
   public static HudPosition position;
   public static int offsetX;
   public static int offsetY;
   public static double scale;
   public static double iconScale;
   public static double ammoTextScale;
   public static int frameWidth;
   public static int frameHeight;
   public static boolean zeroPadAmmo;
   public static boolean showSecondary;
   public static double secondaryIconScale;
   public static double secondaryAlpha;
   public static boolean showPanel;
   public static int panelColor;
   public static double panelOpacity;
   public static PanelBorderStyle borderStyle;
   public static int borderColor;
   public static double borderOpacity;
   public static int borderLength;
   public static int borderOutset;
   public static boolean showWeaponGlow;
   public static int weaponGlowColor;
   public static boolean showAmmoTypeIcon;
   public static double ammoTypeIconScale;
   public static boolean showAttachmentDots;
   public static int attachmentDotColor;
   public static int secondaryDotSize;
   public static boolean showSwitchSlide;
   public static int switchSlideDistance;
   public static boolean showSwitchBorderFlash;
   public static int switchBorderFlashColor;
   public static int switchBorderFlashMs;
   public static int ammoColor;
   public static int ammoLowColor;
   public static double lowAmmoThresholdPercent;
   public static int reserveColor;
   public static double reserveScaleOffset;
   public static double reserveOpacity;
   public static int nameColor;
   public static int fireModeColor;
   public static boolean showFireTypeFlash;
   public static int fireTypeFlashColor;
   public static int fireTypeHoldColor;
   public static int fireTypeFlashMs;
   public static int fireTypeHoldMs;
   public static int fireTypeFadeMs;
   public static double fireTypeScale;
   public static int fireTypeXOffset;
   public static int fireTypeYOffset;
   public static boolean fireTypeShowLabel;
   public static boolean showAmmoAlerts;
   public static int reloadAlertColor;
   public static int noAmmoAlertColor;
   public static int lowAmmoAlertColor;
   public static double ammoAlertScale;
   public static int ammoAlertYOffset;
   public static boolean ammoAlertPulse;
   public static boolean ammoAlertGlow;
   public static boolean showGear;
   public static boolean gearCountWholeInventory;
   public static boolean gearGlow;
   public static boolean gearTopDotInner;
   public static boolean gearAutoLayout;
   public static boolean selGlowEnabled;
   public static int selGlowOpacity;
   public static int selGlowRadius;
   public static int selGlowTopLeft;
   public static int selGlowTopRight;
   public static int selGlowBottomRight;
   public static int selGlowBottomLeft;
   public static boolean ammoGlow;
   public static int ammoGlowBox;
   public static int ammoGlowHalo;
   public static int ammoGlowOpacity;
   public static int ammoGlowSpread;
   public static boolean ammoGlowFollowLow;
   public static boolean ammoGlowPulse;
   public static boolean ammoGlowLowPulse;
   public static int infoBoxRise;
   public static boolean ammoGlowReservePanel;
   public static int ammoGlowReservePanelOpacity;
   public static int ammoGlowReservePanelHeight;
   public static boolean selGlowPulse;
   public static int selGlowPulseMs;
   public static int selGlowPulseDepth;
   public static int selGlowSwitchBoost;

   @SubscribeEvent
   static void onLoad(ModConfigEvent event) {
      try {
         refresh();
      } catch (IllegalStateException var2) {
      }
   }

   public static void refresh() {
      preferredHudSource = (WeaponHudSource)PREFERRED_HUD_SOURCE.get();
      ignoreHideGui = (Boolean)IGNORE_HIDE_GUI.get();
      showName = (Boolean)SHOW_NAME.get();
      showFireMode = (Boolean)SHOW_FIRE_MODE.get();
      heatDisplayMode = (HeatDisplayMode)HEAT_DISPLAY_MODE.get();
      heatBarColor = ColorConfig.fromHex((String)HEAT_BAR_COLOR.get(), 6277352);
      heatBarLockedColor = ColorConfig.fromHex((String)HEAT_BAR_LOCKED_COLOR.get(), 15225144);
      heatFlashColor = ColorConfig.fromHex((String)HEAT_FLASH_COLOR.get(), 16767078);
      showReserveAmmo = (Boolean)SHOW_RESERVE_AMMO.get();
      position = (HudPosition)POSITION.get();
      offsetX = (Integer)OFFSET_X.get();
      offsetY = (Integer)OFFSET_Y.get();
      scale = (Double)SCALE.get();
      iconScale = (Double)ICON_SCALE.get();
      ammoTextScale = (Double)AMMO_TEXT_SCALE.get();
      frameWidth = (Integer)FRAME_WIDTH.get();
      frameHeight = (Integer)FRAME_HEIGHT.get();
      zeroPadAmmo = (Boolean)ZERO_PAD_AMMO.get();
      showSecondary = (Boolean)SHOW_SECONDARY.get();
      secondaryIconScale = (Double)SECONDARY_ICON_SCALE.get();
      secondaryAlpha = (Double)SECONDARY_ALPHA.get();
      showPanel = (Boolean)SHOW_PANEL.get();
      panelColor = ColorConfig.fromHex((String)PANEL_COLOR.get(), 14278371);
      panelOpacity = (double)((Integer)PANEL_OPACITY.get()).intValue();
      borderStyle = (PanelBorderStyle)BORDER_STYLE.get();
      borderColor = ColorConfig.fromHex((String)BORDER_COLOR.get(), 15265008);
      borderOpacity = (double)((Integer)BORDER_OPACITY.get()).intValue();
      borderLength = (Integer)BORDER_LENGTH.get();
      borderOutset = (Integer)BORDER_OUTSET.get();
      showWeaponGlow = (Boolean)SHOW_WEAPON_GLOW.get();
      weaponGlowColor = ColorConfig.fromHex((String)WEAPON_GLOW_COLOR.get(), 15915405);
      showAmmoTypeIcon = (Boolean)SHOW_AMMO_TYPE_ICON.get();
      ammoTypeIconScale = (Double)AMMO_TYPE_ICON_SCALE.get();
      showAttachmentDots = (Boolean)SHOW_ATTACHMENT_DOTS.get();
      attachmentDotColor = ColorConfig.fromHex((String)ATTACHMENT_DOT_COLOR.get(), 16763210);
      secondaryDotSize = (Integer)SECONDARY_DOT_SIZE.get();
      showSwitchSlide = (Boolean)SHOW_SWITCH_SLIDE.get();
      switchSlideDistance = (Integer)SWITCH_SLIDE_DISTANCE.get();
      showSwitchBorderFlash = (Boolean)SHOW_SWITCH_BORDER_FLASH.get();
      switchBorderFlashColor = ColorConfig.fromHex((String)SWITCH_BORDER_FLASH_COLOR.get(), 16763210);
      switchBorderFlashMs = (Integer)SWITCH_BORDER_FLASH_MS.get();
      ammoColor = ColorConfig.fromHex((String)AMMO_COLOR.get(), 16777215);
      ammoLowColor = ColorConfig.fromHex((String)AMMO_LOW_COLOR.get(), 15225144);
      lowAmmoThresholdPercent = (Double)LOW_AMMO_THRESHOLD_PERCENT.get();
      reserveColor = ColorConfig.fromHex((String)RESERVE_COLOR.get(), 15922938);
      reserveScaleOffset = (Double)RESERVE_SCALE_OFFSET.get();
      reserveOpacity = (double)((Integer)RESERVE_OPACITY.get()).intValue();
      nameColor = ColorConfig.fromHex((String)NAME_COLOR.get(), 15134453);
      fireModeColor = ColorConfig.fromHex((String)FIRE_MODE_COLOR.get(), 10474480);
      showFireTypeFlash = (Boolean)SHOW_FIRE_TYPE_FLASH.get();
      fireTypeFlashColor = ColorConfig.fromHex((String)FIRE_TYPE_FLASH_COLOR.get(), 16767078);
      fireTypeHoldColor = ColorConfig.fromHex((String)FIRE_TYPE_HOLD_COLOR.get(), 16777215);
      fireTypeFlashMs = (Integer)FIRE_TYPE_FLASH_MS.get();
      fireTypeHoldMs = (Integer)FIRE_TYPE_HOLD_MS.get();
      fireTypeFadeMs = (Integer)FIRE_TYPE_FADE_MS.get();
      fireTypeScale = (Double)FIRE_TYPE_SCALE.get();
      fireTypeXOffset = (Integer)FIRE_TYPE_X_OFFSET.get();
      fireTypeYOffset = (Integer)FIRE_TYPE_Y_OFFSET.get();
      fireTypeShowLabel = (Boolean)FIRE_TYPE_SHOW_LABEL.get();
      showAmmoAlerts = (Boolean)SHOW_AMMO_ALERTS.get();
      reloadAlertColor = ColorConfig.fromHex((String)RELOAD_ALERT_COLOR.get(), 16767078);
      noAmmoAlertColor = ColorConfig.fromHex((String)NO_AMMO_ALERT_COLOR.get(), 15225144);
      lowAmmoAlertColor = ColorConfig.fromHex((String)LOW_AMMO_ALERT_COLOR.get(), 16767078);
      ammoAlertScale = (Double)AMMO_ALERT_SCALE.get();
      ammoAlertYOffset = (Integer)AMMO_ALERT_Y_OFFSET.get();
      ColorConfig.migrateDarkPanel(PANEL_COLOR, PANEL_OPACITY, 14278371, 14);
      ammoAlertPulse = (Boolean)AMMO_ALERT_PULSE.get();
      ammoAlertGlow = (Boolean)AMMO_ALERT_GLOW.get();
      showGear = (Boolean)SHOW_GEAR.get();
      gearCountWholeInventory = (Boolean)GEAR_COUNT_WHOLE_INVENTORY.get();
      gearGlow = (Boolean)GEAR_GLOW.get();
      gearTopDotInner = !(Boolean)GEAR_TOP_DOT_INNER.get();
      gearAutoLayout = (Boolean)GEAR_AUTO_LAYOUT.get();
      selGlowEnabled = (Boolean)SEL_GLOW_ENABLED.get();
      selGlowOpacity = (Integer)SEL_GLOW_OPACITY.get();
      selGlowRadius = (Integer)SEL_GLOW_RADIUS.get();
      selGlowTopLeft = (Integer)SEL_GLOW_TL.get();
      selGlowTopRight = (Integer)SEL_GLOW_TR.get();
      selGlowBottomRight = (Integer)SEL_GLOW_BR.get();
      selGlowBottomLeft = (Integer)SEL_GLOW_BL.get();
      ammoGlow = (Boolean)AMMO_GLOW.get();
      ammoGlowBox = (Integer)AMMO_GLOW_BOX.get();
      ammoGlowHalo = (Integer)AMMO_GLOW_HALO.get();
      ammoGlowOpacity = (Integer)AMMO_GLOW_OPACITY.get();
      ammoGlowSpread = (Integer)AMMO_GLOW_SPREAD.get();
      ammoGlowFollowLow = (Boolean)AMMO_GLOW_FOLLOW_LOW.get();
      ammoGlowPulse = (Boolean)AMMO_GLOW_PULSE.get();
      ammoGlowLowPulse = (Boolean)AMMO_GLOW_LOW_PULSE.get();
      infoBoxRise = (Integer)INFO_BOX_RISE.get();
      infoNameGap = (Integer)INFO_NAME_GAP.get();
      infoRowGap = (Integer)INFO_ROW_GAP.get();
      infoBoxGap = (Integer)INFO_BOX_GAP.get();
      infoContentRaise = (Integer)INFO_CONTENT_RAISE.get();
      infoContentScale = (Integer)INFO_CONTENT_SCALE.get();
      infoBoxStyle.refresh();
      ammoGlowReservePanel = (Boolean)AMMO_GLOW_RESERVE_PANEL.get();
      ammoGlowReservePanelOpacity = (Integer)AMMO_GLOW_RESERVE_PANEL_OPACITY.get();
      ammoGlowReservePanelHeight = (Integer)AMMO_GLOW_RESERVE_PANEL_HEIGHT.get();
      selGlowPulse = (Boolean)SEL_GLOW_PULSE.get();
      selGlowPulseMs = (Integer)SEL_GLOW_PULSE_MS.get();
      selGlowPulseDepth = (Integer)SEL_GLOW_PULSE_DEPTH.get();
      selGlowSwitchBoost = (Integer)SEL_GLOW_SWITCH_BOOST.get();
      gearStyle.refresh();
      alertStyle.refresh();
      syncHudSource();
   }

   public static void syncHudSource() {
      enabled = preferredHudSource == WeaponHudSource.MODERNHUD;
      TaczCompat.setVanillaGunHudEnabled(!enabled);
      if (preferredHudSource != WeaponHudSource.MODERNHUD) {
         TaczCompat.setPresenceHudType(preferredHudSource == WeaponHudSource.TACZ_P_HUD);
      }
   }

   public static void cycleHudSource() {
      WeaponHudSource next = switch (preferredHudSource) {
         case MODERNHUD -> WeaponHudSource.TACZ_HUD;
         case TACZ_HUD -> TaczCompat.presenceLoaded() ? WeaponHudSource.TACZ_P_HUD : WeaponHudSource.MODERNHUD;
         case TACZ_P_HUD -> WeaponHudSource.MODERNHUD;
      };
      preferredHudSource = next;
      PREFERRED_HUD_SOURCE.set(next);
      syncHudSource();
   }

   static {
      BUILDER.comment("Weapon widget for TACZ guns. Only active when TACZ is installed.").push("general");
      BUILDER.pop();
      BUILDER.comment("Where the weapon widget is anchored on screen").push("position");
      BUILDER.pop();
      BUILDER.comment("Icon scale and the fixed size of the widget. The size does not change from gun to gun.").push("appearance");
      BUILDER.pop();
      BUILDER.comment(new String[]{"Secondary weapon slot: a small, dim icon of the last different gun you held, if it is", "still in your hotbar."})
         .push("secondary");
      BUILDER.pop();
      BUILDER.comment(new String[]{"Boxes behind the icons and ammo readout: fills, borders and the weapon-switch", "animation."}).push("panel");
      BUILDER.pop();
      BUILDER.comment("Text colors, as hex codes with or without a leading #.").push("colors");
      BUILDER.pop();
      BUILDER.comment(
            new String[]{"Fire mode announcement shown above the crosshair when you pull a gun: a flash color,", "then a held color, then it fades out."}
         )
         .push("fireTypeFlash");
      BUILDER.pop();
      BUILDER.comment(new String[]{"Center-screen alerts for RELOAD, NO AMMO and LOW AMMO. Not shown for guns that use", "inventory ammo or while overheated."})
         .push("ammoAlerts");
      BUILDER.pop();
      BUILDER.comment(
            new String[]{
               "Slots next to the weapon box (needs lrtactical): your melee item on top and up to two",
               "throwables or consumables below with their counts. A used-up item shows a red X and 0."
            }
         )
         .push("gear");
      BUILDER.pop();
      BUILDER.comment(
            new String[]{
               "The box around the ammo numbers, gun name and fire mode: spacing between its rows,",
               "and its own panel and border (separate from the weapon icon boxes). Its height is",
               "appearance.infoBoxRise."
            }
         )
         .push("infoBox");
      BUILDER.pop();
      BUILDER.comment("Soft glow behind the ammo readout (mag | reserve)").push("ammoGlow");
      BUILDER.pop();
   }
}
