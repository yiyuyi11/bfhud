package cc.xypp.modernhud.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class BarTextures {
   public static final BarTextures.Texture EMPTY = new BarTextures.Texture("textures/gui/bar_empty.png", 21, 6);
   public static final BarTextures.Texture WHITE = new BarTextures.Texture("textures/gui/bar_fill_white.png", 21, 6);
   public static final BarTextures.Texture BLUE = new BarTextures.Texture("textures/gui/bar_fill_blue.png", 21, 6);
   public static final BarTextures.Texture PURPLE = new BarTextures.Texture("textures/gui/bar_fill_purple.png", 21, 6);
   public static final BarTextures.Texture RED = new BarTextures.Texture("textures/gui/bar_fill_red.png", 21, 6);
   public static final BarTextures.Texture HEALTH_EMPTY = new BarTextures.Texture("textures/gui/health_empty.png", 96, 6);
   public static final BarTextures.Texture HEALTH_FILL = new BarTextures.Texture("textures/gui/health_fill.png", 96, 6);
   public static final BarTextures.Texture ICON_BREAK = new BarTextures.Texture("textures/gui/icon_break.png", 256, 256);
   public static final BarTextures.Texture ICON_HIT = new BarTextures.Texture("textures/gui/icon_hit.png", 256, 256);

   private BarTextures() {
   }

   public static BarTextures.Texture tierFor(int filledSegments) {
      if (filledSegments > 3) {
         return RED;
      } else if (filledSegments > 2) {
         return PURPLE;
      } else {
         return filledSegments > 1 ? BLUE : WHITE;
      }
   }

   public static final class Texture {
      public final ResourceLocation location;
      public final int width;
      public final int height;

      Texture(String path, int width, int height) {
         this.location = ResourceLocation.fromNamespaceAndPath("modernhud", path);
         this.width = width;
         this.height = height;
      }

      public void blitPartial(GuiGraphics graphics, int x, int y, int slotWidth, int slotHeight, float fillFraction) {
         fillFraction = Math.max(0.0F, Math.min(1.0F, fillFraction));
         int drawWidth = Math.round((float)slotWidth * fillFraction);
         int srcWidth = Math.round((float)this.width * fillFraction);
         if (drawWidth > 0 && srcWidth > 0 && slotHeight > 0) {
            RenderSystem.enableBlend();
            graphics.blit(this.location, x, y, drawWidth, slotHeight, 0.0F, 0.0F, srcWidth, this.height, this.width, this.height);
            RenderSystem.disableBlend();
         }
      }

      public void blitFull(GuiGraphics graphics, int x, int y, int slotWidth, int slotHeight) {
         this.blitPartial(graphics, x, y, slotWidth, slotHeight, 1.0F);
      }
   }
}
