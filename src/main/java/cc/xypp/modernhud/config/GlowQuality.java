package cc.xypp.modernhud.config;

public enum GlowQuality {
   LOW(3, 3),
   MEDIUM(5, 5),
   HIGH(8, 8);

   private final int rings;
   private final int segs;

   private GlowQuality(int rings, int segs) {
      this.rings = rings;
      this.segs = segs;
   }

   public int rings() {
      return this.rings;
   }

   public int segs() {
      return this.segs;
   }
}
