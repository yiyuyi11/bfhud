package cc.xypp.modernhud.client;

public final class Ease {
   private Ease() {
   }

   public static float clamp01(float t) {
      return t < 0.0F ? 0.0F : (t > 1.0F ? 1.0F : t);
   }

   public static float inOutCubic(float t) {
      t = clamp01(t);
      return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow((double)(-2.0F * t + 2.0F), 3.0) / 2.0F;
   }

   public static float outCubic(float t) {
      t = clamp01(t);
      float inv = 1.0F - t;
      return 1.0F - inv * inv * inv;
   }

   public static float outBack(float t) {
      t = clamp01(t);
      float c1 = 1.2F;
      float c3 = c1 + 1.0F;
      float u = t - 1.0F;
      return 1.0F + c3 * u * u * u + c1 * u * u;
   }
}
