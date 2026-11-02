package cc.xypp.modernhud;

import cc.xypp.modernhud.sound.CombatSoundType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SoundRegistry {
   public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, "modernhud");
   public static final int STYLE_COUNT = 3;
   public static final DeferredHolder<SoundEvent, SoundEvent> ABSORPTION_BREAK = reg("absorption_break");
   public static final DeferredHolder<SoundEvent, SoundEvent> ABSORPTION_BREAK_3 = reg("absorption_break_3");
   public static final DeferredHolder<SoundEvent, SoundEvent> ARMOR_HIT = reg("armor_hit");
   private static final DeferredHolder<SoundEvent, SoundEvent>[] HIT = set("hit");
   private static final DeferredHolder<SoundEvent, SoundEvent>[] KILL = set("kill");
   private static final DeferredHolder<SoundEvent, SoundEvent>[] HEADSHOT_KILL = set("headshot_kill");

   private static DeferredHolder<SoundEvent, SoundEvent> reg(String name) {
      return SOUNDS.register(name,
              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("modernhud", name)));
   }

   @SuppressWarnings("unchecked")
   private static DeferredHolder<SoundEvent, SoundEvent>[] set(String base) {
      DeferredHolder<SoundEvent, SoundEvent>[] out = new DeferredHolder[3];

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
