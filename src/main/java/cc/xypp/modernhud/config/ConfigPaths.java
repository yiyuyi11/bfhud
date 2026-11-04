package cc.xypp.modernhud.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig.Entry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.ValueSpec;

public final class ConfigPaths {
   private ConfigPaths() {
   }

   public static ConfigValue<?> find(ForgeConfigSpec spec, String path) {
      return spec.getValues().get(Arrays.asList(path.split("\\."))) instanceof ConfigValue<?> cv ? cv : null;
   }

   public static boolean set(ForgeConfigSpec spec, String path, Object value) {
      ConfigValue cv = find(spec, path);
      if (cv == null) {
         return false;
      } else {
         Object v = value;
         if (cv.get() instanceof Enum<?> current && value instanceof String name) {
            v = Enum.valueOf(current.getDeclaringClass(), name);
         }

         if (!Objects.equals(cv.get(), v)) {
            cv.set(v);
         }

         return true;
      }
   }

   public static int resetAll(ForgeConfigSpec spec) {
      int[] changed = new int[]{0};
      walk(spec, spec.getSpec(), new ArrayList<>(), (cv, vs) -> {
         Object def = vs.getDefault();
         if (!Objects.equals(cv.get(), def)) {
            ((net.minecraftforge.common.ForgeConfigSpec.ConfigValue<Object>)cv).set(def);
            changed[0]++;
         }
      });
      return changed[0];
   }

   private static void walk(ForgeConfigSpec owner, UnmodifiableConfig cfg, List<String> prefix, BiConsumer<ConfigValue<?>, ValueSpec> out) {
      for (Entry e : cfg.entrySet()) {
         List<String> path = new ArrayList<>(prefix);
         path.add(e.getKey());
         Object v = e.getValue();
         if (v instanceof UnmodifiableConfig sub) {
            walk(owner, sub, path, out);
         } else if (v instanceof ValueSpec) {
            ValueSpec vs = (ValueSpec)v;
            if (owner.getValues().get(path) instanceof ConfigValue<?> cv) {
               out.accept(cv, vs);
            }
         }
      }
   }

   public static void save(ForgeConfigSpec spec) {
      try {
         spec.save();
      } catch (RuntimeException var2) {
      }
   }
}
