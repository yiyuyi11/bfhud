package cc.xypp.modernhud;

import cc.xypp.modernhud.sound.CombatSoundType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class SoundRegistry {
   public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, "modernhud");
   public static final int STYLE_COUNT = 3;
   public static final RegistryObject<SoundEvent> ABSORPTION_BREAK = reg("absorption_break");
   public static final RegistryObject<SoundEvent> ABSORPTION_BREAK_3 = reg("absorption_break_3");
   public static final RegistryObject<SoundEvent> ARMOR_HIT = reg("armor_hit");
   private static final RegistryObject<SoundEvent>[] HIT = set("hit");
   private static final RegistryObject<SoundEvent>[] KILL = set("kill");
   private static final RegistryObject<SoundEvent>[] HEADSHOT_KILL = set("headshot_kill");

   private static RegistryObject<SoundEvent> reg(String name) {
      return SOUNDS.register(name,
              () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("modernhud", name)));
   }

   @SuppressWarnings("unchecked")
   private static RegistryObject<SoundEvent>[] set(String base) {
      RegistryObject<SoundEvent>[] out = new RegistryObject[3];

      for (int style = 1; style <= 3; style++) {
         out[style - 1] = reg(style == 1 ? base : base + "_" + style);
      }

      return out;
   }

   private static int idx(int style) {
      return Math.max(1, Math.min(3, style)) - 1;
   }

   public static SoundEvent combat(CombatSoundType type, int style) {
      return switch (type) {
         case HIT -> HIT[idx(style)].get();
         case KILL -> KILL[idx(style)].get();
         case HEADSHOT_KILL -> HEADSHOT_KILL[idx(style)].get();
      };
   }

   public static SoundEvent shieldBreak(int style) {
      return style >= 3 ? ABSORPTION_BREAK_3.get() : ABSORPTION_BREAK.get();
   }

   private SoundRegistry() {
   }
}
