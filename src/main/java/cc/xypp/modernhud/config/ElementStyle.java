package cc.xypp.modernhud.config;

import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public final class ElementStyle {
   private final IntValue opacityV;
   private final IntValue fadeInV;
   private final IntValue fadeOutV;
   private final BooleanValue panelEnabledV;
   private final ConfigValue<String> panelColorV;
   private final IntValue panelOpacityV;
   private final EnumValue<PanelBorderStyle> borderStyleV;
   private final ConfigValue<String> borderColorV;
   private final IntValue borderOpacityV;
   private final IntValue borderLengthV;
   private final IntValue borderOutsetV;
   private final ElementStyle.Defaults d;
   private final int opacityDefault;
   public int opacity = 100;
   public int fadeInMs;
   public int fadeOutMs;
   public boolean panelEnabled;
   public int panelColor;
   public int panelOpacity;
   public PanelBorderStyle borderStyle;
   public int borderColor = 15265008;
   public int borderOpacity = 90;
   public int borderLength;
   public int borderOutset;

   public ElementStyle(Builder b, String section, String keyPrefix, ElementStyle.Defaults d) {
      this(b, section, keyPrefix, d, 100);
   }

   public ElementStyle(Builder b, String section, String keyPrefix, ElementStyle.Defaults d, int opacityDefault) {
      this.d = d;
      this.opacityDefault = opacityDefault;
      String t = "modernhud.config." + keyPrefix + "_style_";
      b.comment("Look of this element: opacity, fade times, panel and border.").push(section);
      this.opacityV = b.comment("Opacity of the whole element, 0 to 100.").translation(t + "opacity").defineInRange("opacity", opacityDefault, 0, 100);
      this.fadeInV = b.comment("Fade-in time in milliseconds. 0 is instant.").translation(t + "fade_in_ms").defineInRange("fadeInMs", d.fadeInMs(), 0, 5000);
      this.fadeOutV = b.comment("Fade-out time in milliseconds. 0 is instant.")
         .translation(t + "fade_out_ms")
         .defineInRange("fadeOutMs", d.fadeOutMs(), 0, 5000);
      this.panelEnabledV = b.comment("Draw a flat background panel behind the element.").translation(t + "panel").define("panelEnabled", d.panelEnabled());
      this.panelColorV = b.comment("Panel color as a hex code, for example D9DEE3.")
         .translation(t + "panel_color")
         .define("panelColor", ColorConfig.toHex(d.panelColor()));
      this.panelOpacityV = b.comment("Panel opacity, 0 to 100.").translation(t + "panel_opacity").defineInRange("panelOpacity", d.panelOpacity(), 0, 100);
      this.borderStyleV = b.comment(
            new String[]{
               "Border style: NONE, CORNERS (short L-shaped brackets), FULL (thin outline),",
               "DOTS (one dot per corner), BOTTOM_DOTS (bottom corners only) or FRAME (outline with corner dots)."
            }
         )
         .translation(t + "border_style")
         .defineEnum("borderStyle", d.borderStyle());
      this.borderColorV = b.comment("Border color as a hex code, for example E8ECF0.")
         .translation(t + "border_color")
         .define("borderColor", ColorConfig.toHex(15265008));
      this.borderOpacityV = b.comment("Border opacity, 0 to 100.").translation(t + "border_opacity").defineInRange("borderOpacity", 90, 0, 100);
      this.borderLengthV = b.comment("Bracket arm length for CORNERS, or dot size plus 1 for DOTS and FRAME, in pixels.")
         .translation(t + "border_length")
         .defineInRange("borderLength", d.borderLength(), 1, 20);
      this.borderOutsetV = b.comment("How far brackets and dots sit outside the element's edge, in pixels.")
         .translation(t + "border_outset")
         .defineInRange("borderOutset", d.borderOutset(), 0, 20);
      b.pop();
      this.refresh();
   }

   public void refresh() {
      ColorConfig.migrateDarkPanel(this.panelColorV, this.panelOpacityV, this.d.panelColor(), this.d.panelOpacity());

      try {
         this.opacity = (Integer)this.opacityV.get();
         this.fadeInMs = (Integer)this.fadeInV.get();
         this.fadeOutMs = (Integer)this.fadeOutV.get();
         this.panelEnabled = (Boolean)this.panelEnabledV.get();
         this.panelColor = ColorConfig.fromHex((String)this.panelColorV.get(), this.d.panelColor());
         this.panelOpacity = (Integer)this.panelOpacityV.get();
         this.borderStyle = (PanelBorderStyle)this.borderStyleV.get();
         this.borderColor = ColorConfig.fromHex((String)this.borderColorV.get(), 15265008);
         this.borderOpacity = (Integer)this.borderOpacityV.get();
         this.borderLength = (Integer)this.borderLengthV.get();
         this.borderOutset = (Integer)this.borderOutsetV.get();
      } catch (RuntimeException var2) {
         this.opacity = this.opacityDefault;
         this.fadeInMs = this.d.fadeInMs();
         this.fadeOutMs = this.d.fadeOutMs();
         this.panelEnabled = this.d.panelEnabled();
         this.panelColor = this.d.panelColor();
         this.panelOpacity = this.d.panelOpacity();
         this.borderStyle = this.d.borderStyle();
         this.borderColor = 15265008;
         this.borderOpacity = 90;
         this.borderLength = this.d.borderLength();
         this.borderOutset = this.d.borderOutset();
      }
   }

   public float alpha(float alpha) {
      return alpha * ((float)this.opacity / 100.0F);
   }

   public static record Defaults(
      boolean panelEnabled, int panelColor, int panelOpacity, PanelBorderStyle borderStyle, int borderLength, int borderOutset, int fadeInMs, int fadeOutMs
   ) {
   }
}
