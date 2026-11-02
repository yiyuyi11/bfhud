package cc.xypp.modernhud.client;

import cc.xypp.modernhud.config.HudConfig;
import cc.xypp.modernhud.config.HudPosition;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

public class FeedbackIconOverlay implements IGuiOverlay {
   public void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
      boolean showBreak = HudConfig.showBreakIcon && this.withinDuration(ClientFeedback.millisSinceBreakTrigger(), HudConfig.breakIconDurationMs);
      boolean showHit = !showBreak && HudConfig.showHitIcon && this.withinDuration(ClientFeedback.millisSinceHitTrigger(), HudConfig.hitIconDurationMs);
      if (showBreak || showHit) {
         BarTextures.Texture icon = showBreak ? BarTextures.ICON_BREAK : BarTextures.ICON_HIT;
         long elapsed = showBreak ? ClientFeedback.millisSinceBreakTrigger() : ClientFeedback.millisSinceHitTrigger();
         int duration = showBreak ? HudConfig.breakIconDurationMs : HudConfig.hitIconDurationMs;
         float fadeLen = Math.max(1.0F, Math.min((float)HudConfig.feedbackFadeMs, (float)duration));
         float alpha = Math.max(0.0F, Math.min(1.0F, (float)((long)duration - elapsed) / fadeLen));
         alpha = alpha * alpha * (3.0F - 2.0F * alpha) * ((float)HudConfig.feedbackOpacity / 100.0F);
         int width = HudConfig.feedbackIconWidth;
         int height = HudConfig.feedbackIconHeight;
         double scale = HudConfig.feedbackIconScale;
         int[] pos = this.resolvePosition(screenWidth, screenHeight, width, height, scale);
         PoseStack pose = graphics.pose();
         pose.pushPose();
         pose.translate((float)pos[0], (float)pos[1], 0.0F);
         pose.scale((float)scale, (float)scale, 1.0F);
         RenderSystem.enableBlend();

         try {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            icon.blitFull(graphics, 0, 0, width, height);
         } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
         }

         pose.popPose();
      }
   }

   private boolean withinDuration(long elapsed, int durationMs) {
      return elapsed >= 0L && elapsed < (long)durationMs;
   }

   private int[] resolvePosition(int screenWidth, int screenHeight, int width, int height, double scale) {
      int scaledWidth = (int)Math.round((double)width * scale);
      int scaledHeight = (int)Math.round((double)height * scale);
      HudPosition position = HudConfig.feedbackPosition;
      int x;
      int y;
      switch (position) {
         case TOP_LEFT:
            x = HudConfig.feedbackOffsetX;
            y = HudConfig.feedbackOffsetY;
            break;
         case TOP_RIGHT:
            x = screenWidth - scaledWidth - HudConfig.feedbackOffsetX;
            y = HudConfig.feedbackOffsetY;
            break;
         case BOTTOM_RIGHT:
            x = screenWidth - scaledWidth - HudConfig.feedbackOffsetX;
            y = screenHeight - scaledHeight - HudConfig.feedbackOffsetY;
            break;
         case CUSTOM:
            x = HudConfig.feedbackOffsetX;
            y = HudConfig.feedbackOffsetY;
            break;
         case BOTTOM_LEFT:
         default:
            x = HudConfig.feedbackOffsetX;
            y = screenHeight - scaledHeight - HudConfig.feedbackOffsetY;
      }

      return new int[]{x, y};
   }
}
