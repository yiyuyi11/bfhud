package cc.xypp.modernhud.client.compat;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.ModList;

public final class SuperbWarfareCompat {
   private static final boolean LOADED = ModList.get().isLoaded("superbwarfare");
   private static Class<?> vehicleEntityClass;
   private static boolean lookupAttempted;

   private SuperbWarfareCompat() {
   }

   public static boolean loaded() {
      return LOADED;
   }

   public static boolean isInVehicle(Entity entity) {
      if (!LOADED) {
         return false;
      } else {
         Entity vehicle = entity.getVehicle();
         if (vehicle == null) {
            return false;
         } else {
            Class<?> clazz = vehicleEntityClass();
            return clazz != null && clazz.isInstance(vehicle);
         }
      }
   }

   private static Class<?> vehicleEntityClass() {
      if (!lookupAttempted) {
         lookupAttempted = true;

         try {
            vehicleEntityClass = Class.forName("com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity");
         } catch (Throwable var1) {
            vehicleEntityClass = null;
         }
      }

      return vehicleEntityClass;
   }
}
