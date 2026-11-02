package cc.xypp.modernhud.client;

import cc.xypp.modernhud.config.HudConfig;

public final class EasedAnimator {
   private float durationMs;
   private final EasedAnimator.Easing easing;
   private boolean retargeted;
   private float startValue;
   private float targetValue;
   private float currentValue;
   private long startTimeMs;
   private boolean initialized;

   public EasedAnimator(float durationMs, EasedAnimator.Easing easing) {
      this.durationMs = Math.max(1.0F, durationMs);
      this.easing = easing;
   }

   public void setDurationMs(float ms) {
      this.durationMs = Math.max(1.0F, ms);
   }

   private static long nowMs() {
      return System.nanoTime() / 1000000L;
   }

   public void snapTo(float value) {
      this.startValue = value;
      this.targetValue = value;
      this.currentValue = value;
      this.initialized = true;
   }

   public void setTarget(float target) {
      if (!this.initialized) {
         this.snapTo(target);
      } else if (!(Math.abs(target - this.targetValue) < 1.0E-5F)) {
         this.retargeted = this.currentValue != this.targetValue;
         this.startValue = this.currentValue;
         this.targetValue = target;
         this.startTimeMs = nowMs();
      }
   }

   public float update() {
      if (!this.initialized) {
         return this.currentValue;
      } else if (!HudConfig.animations) {
         this.currentValue = this.targetValue;
         return this.currentValue;
      } else {
         float dur = this.durationMs / (float)Math.max(0.1, HudConfig.animationSpeed);
         long elapsed = nowMs() - this.startTimeMs;
         if (elapsed >= (long)dur) {
            this.currentValue = this.targetValue;
            return this.currentValue;
         } else {
            float t = Math.max(0.0F, (float)elapsed / dur);
            this.currentValue = this.startValue + (this.targetValue - this.startValue) * this.applyEasing(t);
            return this.currentValue;
         }
      }
   }

   public float getCurrentValue() {
      return this.currentValue;
   }

   public float getTargetValue() {
      return this.targetValue;
   }

   public boolean isAnimating() {
      return this.initialized && this.currentValue != this.targetValue;
   }

   private float applyEasing(float t) {
      EasedAnimator.Easing e = this.retargeted && this.easing == EasedAnimator.Easing.CUBIC_IN_OUT ? EasedAnimator.Easing.CUBIC_OUT : this.easing;
      switch (e) {
         case LINEAR:
            return t;
         case CUBIC_OUT: {
            float inv = 1.0F - t;
            return 1.0F - inv * inv * inv;
         }
         case CUBIC_IN_OUT:
            return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow((double)(-2.0F * t + 2.0F), 3.0) / 2.0F;
         case QUINTIC_OUT:
         default: {
            float inv = 1.0F - t;
            return 1.0F - inv * inv * inv * inv * inv;
         }
      }
   }

   public static enum Easing {
      LINEAR,
      CUBIC_OUT,
      QUINTIC_OUT,
      CUBIC_IN_OUT;
   }
}
