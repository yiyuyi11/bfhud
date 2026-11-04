package cc.xypp.modernhud.client.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.GunHeatData;
import com.tacz.guns.resource.pojo.data.gun.GunReloadData;
import com.tacz.guns.util.AttachmentDataUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public final class TaczCompat {
   private static final boolean LOADED = ModList.get().isLoaded("tacz");
   private static final int CANVAS_W = 39;
   private static final int CANVAS_H = 13;
   private static IGun iGunCache;
   private static ItemStack iGunCacheStack;
   private static long iGunCacheMs = -1000L;
   private static ClientGunIndex indexCache;
   private static ItemStack indexCacheStack;
   private static long indexCacheMs = -1000L;
   public static final int NO_RESERVE = Integer.MIN_VALUE;
   public static final int INFINITE = -1;
   private static int reserveResult;
   private static ItemStack reserveResultGun;
   private static long reserveResultMs = -1000L;
   private static int invTotalCache;
   private static ItemStack invTotalGun;
   private static long invTotalMs = -1000L;
   private static Boolean presenceLoadedCache;

   private TaczCompat() {
   }

   public static boolean loaded() {
      return LOADED;
   }

   public static void setVanillaGunHudEnabled(boolean enabled) {
      if (LOADED) {
         try {
            Class<?> renderConfigClass = Class.forName("com.tacz.guns.config.client.RenderConfig");
            Field field = renderConfigClass.getField("GUN_HUD_ENABLE");
            Object configValue = field.get(null);
            if (configValue instanceof Boolean) {
               field.set(null, enabled);
               return;
            }

            Method setMethod = configValue.getClass().getMethod("set", Object.class);
            setMethod.invoke(configValue, enabled);
         } catch (Throwable var5) {
         }
      }
   }

   public static boolean presenceLoaded() {
      if (presenceLoadedCache == null) {
         try {
            presenceLoadedCache = ModList.get().isLoaded("tacz_presence");
         } catch (Throwable var1) {
            presenceLoadedCache = false;
         }
      }

      return presenceLoadedCache;
   }

   public static void setPresenceHudType(boolean taczPHud) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException: Cannot invoke "org.jetbrains.java.decompiler.struct.gen.VarType.isGeneric()" because "newRet" is null
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.getInferredExprType(InvocationExprent.java:634)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.DoStatement.toJava(DoStatement.java:146)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.CatchStatement.toJava(CatchStatement.java:166)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:241)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement.toJava(RootStatement.java:36)
      //   at org.jetbrains.java.decompiler.main.ClassWriter.writeMethod(ClassWriter.java:1283)
      //
      // Bytecode:
      // 00: invokestatic cc/xypp/modernhud/client/compat/TaczCompat.presenceLoaded ()Z
      // 03: ifne 07
      // 06: return
      // 07: ldc "com.tacz.presence.config.PresenceConfig"
      // 09: invokestatic java/lang/Class.forName (Ljava/lang/String;)Ljava/lang/Class;
      // 0c: astore 1
      // 0d: ldc "com.tacz.presence.config.PresenceConfig$HudType"
      // 0f: invokestatic java/lang/Class.forName (Ljava/lang/String;)Ljava/lang/Class;
      // 12: astore 2
      // 13: aconst_null
      // 14: astore 3
      // 15: aload 2
      // 16: invokevirtual java/lang/Class.getEnumConstants ()[Ljava/lang/Object;
      // 19: astore 4
      // 1b: aload 4
      // 1d: arraylength
      // 1e: istore 5
      // 20: bipush 0
      // 21: istore 6
      // 23: iload 6
      // 25: iload 5
      // 27: if_icmpge 62
      // 2a: aload 4
      // 2c: iload 6
      // 2e: aaload
      // 2f: astore 7
      // 31: aload 7
      // 33: checkcast java/lang/Enum
      // 36: invokevirtual java/lang/Enum.name ()Ljava/lang/String;
      // 39: astore 8
      // 3b: iload 0
      // 3c: ifeq 4c
      // 3f: aload 8
      // 41: ldc "TACZ_P_HUD"
      // 43: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 46: ifeq 5c
      // 49: goto 56
      // 4c: aload 8
      // 4e: ldc "TACZ_HUD"
      // 50: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 53: ifeq 5c
      // 56: aload 7
      // 58: astore 3
      // 59: goto 62
      // 5c: iinc 6 1
      // 5f: goto 23
      // 62: aload 3
      // 63: ifnonnull 67
      // 66: return
      // 67: aload 1
      // 68: ldc "GUN_HUD_TYPE"
      // 6a: invokevirtual java/lang/Class.getField (Ljava/lang/String;)Ljava/lang/reflect/Field;
      // 6d: astore 4
      // 6f: aload 4
      // 71: aconst_null
      // 72: invokevirtual java/lang/reflect/Field.get (Ljava/lang/Object;)Ljava/lang/Object;
      // 75: astore 5
      // 77: aload 5
      // 79: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 7c: ldc "set"
      // 7e: bipush 1
      // 7f: anewarray 52
      // 82: dup
      // 83: bipush 0
      // 84: ldc java/lang/Object
      // 86: aastore
      // 87: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 8a: astore 6
      // 8c: aload 6
      // 8e: aload 5
      // 90: bipush 1
      // 91: anewarray 4
      // 94: dup
      // 95: bipush 0
      // 96: aload 3
      // 97: aastore
      // 98: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 9b: pop
      // 9c: goto a0
      // 9f: astore 1
      // a0: return
   }

   public static boolean isGun(ItemStack stack) {
      return LOADED && iGun(stack) != null;
   }

   private static IGun iGun(ItemStack gun) {
      long now = Util.getMillis();
      if (iGunCacheStack == gun && now - iGunCacheMs < 20L) {
         return iGunCache;
      } else {
         IGun g = IGun.getIGunOrNull(gun);
         iGunCache = g;
         iGunCacheStack = gun;
         iGunCacheMs = now;
         return g;
      }
   }

   public static ResourceLocation gunId(ItemStack gun) {
      IGun iGun = iGun(gun);
      return iGun == null ? null : iGun.getGunId(gun);
   }

   public static boolean usesInventoryAmmo(ItemStack gun) {
      IGun iGun = iGun(gun);
      return iGun != null && iGun.useInventoryAmmo(gun);
   }

   private static ClientGunIndex clientIndex(ItemStack gun) {
      long now = Util.getMillis();
      if (indexCacheStack == gun && now - indexCacheMs < 20L) {
         return indexCache;
      } else {
         IGun iGun = iGun(gun);
         ClientGunIndex idx = iGun == null ? null : (ClientGunIndex)TimelessAPI.getClientGunIndex(iGun.getGunId(gun)).orElse(null);
         indexCache = idx;
         indexCacheStack = gun;
         indexCacheMs = now;
         return idx;
      }
   }

   public static int magAmmo(Player p, ItemStack gun) {
      IGun iGun = iGun(gun);
      if (iGun == null) {
         return 0;
      } else if (iGun.useInventoryAmmo(gun)) {
         int total;
         if (!needCheckAmmo(p)) {
            total = 9999;
         } else if (iGun.useDummyAmmo(gun)) {
            total = iGun.getDummyAmmoAmount(gun);
         } else {
            total = inventoryAmmoTotal(p, gun);
         }

         return Math.min(9999, total + barrelBonus(gun));
      } else {
         return iGun.getCurrentAmmoCount(gun) + barrelBonus(gun);
      }
   }

   private static int barrelBonus(ItemStack gun) {
      IGun iGun = iGun(gun);
      if (iGun != null && iGun.hasBulletInBarrel(gun)) {
         ClientGunIndex index = clientIndex(gun);
         if (index != null && index.getGunData() != null) {
            return index.getGunData().getBolt() == Bolt.OPEN_BOLT ? 0 : 1;
         } else {
            return 1;
         }
      } else {
         return 0;
      }
   }

   public static boolean hasHeat(ItemStack gun) {
      ClientGunIndex index = clientIndex(gun);
      return index != null && index.getGunData() != null && index.getGunData().hasHeatData();
   }

   public static float heatFraction(ItemStack gun) {
      IGun iGun = iGun(gun);
      ClientGunIndex index = clientIndex(gun);
      if (iGun != null && index != null && index.getGunData() != null) {
         GunData data = index.getGunData();
         GunHeatData heatData = data.getHeatData();
         return data.hasHeatData() && heatData != null && !(heatData.getHeatMax() <= 0.0F)
            ? Math.max(0.0F, Math.min(1.0F, iGun.getHeatAmount(gun) / heatData.getHeatMax()))
            : 0.0F;
      } else {
         return 0.0F;
      }
   }

   public static boolean isOverheatLocked(ItemStack gun) {
      IGun iGun = iGun(gun);
      return iGun != null && iGun.isOverheatLocked(gun);
   }

   public static int magCapacity(ItemStack gun) {
      ClientGunIndex index = clientIndex(gun);
      if (index == null) {
         return -1;
      } else {
         GunData data = index.getGunData();
         return data == null ? -1 : AttachmentDataUtils.getAmmoCountWithAttachment(gun, data);
      }
   }

   public static int[] attachmentSlotCounts(ItemStack gun) {
      IGun iGun = iGun(gun);
      if (iGun == null) {
         return new int[]{0, 0};
      } else {
         int total = 0;
         int filled = 0;

         for (AttachmentType type : AttachmentType.values()) {
            if (type != AttachmentType.NONE && iGun.allowAttachmentType(gun, type)) {
               total++;
               if (!DefaultAssets.isEmptyAttachmentId(iGun.getAttachmentId(gun, type))) {
                  filled++;
               }
            }
         }

         return new int[]{total, filled};
      }
   }

   public static int reserveAmmo(Player p, ItemStack gun) {
      IGun iGun = iGun(gun);
      if (iGun == null) {
         return 0;
      } else {
         long now = Util.getMillis();
         if (reserveResultGun == gun && now - reserveResultMs < 50L) {
            return reserveResult;
         } else {
            reserveResultGun = gun;
            reserveResultMs = now;
            reserveResult = computeReserve(p, gun, iGun);
            return reserveResult;
         }
      }
   }

   private static int computeReserve(Player p, ItemStack gun, IGun iGun) {
      if (iGun.useInventoryAmmo(gun)) {
         return Integer.MIN_VALUE;
      } else if (isReloadInfinite(gun)) {
         return -1;
      } else if (iGun.useDummyAmmo(gun)) {
         return iGun.getDummyAmmoAmount(gun);
      } else {
         return !needCheckAmmo(p) ? 9999 : inventoryAmmoTotal(p, gun);
      }
   }

   private static boolean isReloadInfinite(ItemStack gun) {
      ClientGunIndex index = clientIndex(gun);
      if (index != null && index.getGunData() != null) {
         GunReloadData rd = index.getGunData().getReloadData();
         return rd != null && rd.isInfinite();
      } else {
         return false;
      }
   }

   private static boolean needCheckAmmo(Player p) {
      try {
         IGunOperator op = IGunOperator.fromLivingEntity(p);
         return op == null || op.needCheckAmmo();
      } catch (Throwable var2) {
         return true;
      }
   }

   private static int inventoryAmmoTotal(Player p, ItemStack gun) {
      long now = Util.getMillis();
      if (invTotalGun == gun && now - invTotalMs < 50L) {
         return invTotalCache;
      } else {
         int n = 0;
         int size = p.getInventory().getContainerSize();

         for (int i = 0; i < size; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty()) {
               Item item = s.getItem();
               if (item instanceof IAmmoBox box && box.isAmmoBoxOfGun(gun, s)) {
                  if (box.isAllTypeCreative(s) || box.isCreative(s)) {
                     invTotalCache = 9999;
                     invTotalGun = gun;
                     invTotalMs = now;
                     return 9999;
                  }

                  n += box.getAmmoCount(s);
                  continue;
               }

               if (item instanceof IAmmo ammo && ammo.isAmmoOfGun(gun, s)) {
                  n += s.getCount();
               }
            }
         }

         invTotalCache = n;
         invTotalGun = gun;
         invTotalMs = now;
         return n;
      }
   }

   public static String fireMode(ItemStack gun) {
      IGun iGun = iGun(gun);
      if (iGun == null) {
         return "";
      } else {
         FireMode mode = iGun.getFireMode(gun);
         return mode == null ? "" : mode.name();
      }
   }

   public static String[] supportedFireModes(ItemStack gun) {
      ClientGunIndex index = clientIndex(gun);
      if (index != null && index.getGunData() != null) {
         List<FireMode> modes = index.getGunData().getFireModeSet();
         if (modes != null && !modes.isEmpty()) {
            LinkedHashSet<String> distinct = new LinkedHashSet<>();

            for (FireMode m : modes) {
               if (m != null) {
                  distinct.add(m.name());
               }
            }

            return distinct.toArray(new String[0]);
         } else {
            return new String[0];
         }
      } else {
         return new String[0];
      }
   }

   public static boolean hasHudTexture(ItemStack gun) {
      ClientGunIndex index = clientIndex(gun);
      if (index == null) {
         return false;
      } else {
         GunDisplayInstance display = index.getDefaultDisplay();
         return display != null && display.getHUDTexture() != null;
      }
   }

   public static int[] iconSize(float scale) {
      return new int[]{Math.round(39.0F * scale), Math.round(13.0F * scale)};
   }

   public static void renderHudTexture(GuiGraphics g, ItemStack gun, int x, int y, float scale) {
      ClientGunIndex index = clientIndex(gun);
      if (index != null) {
         GunDisplayInstance display = index.getDefaultDisplay();
         if (display != null) {
            ResourceLocation tex = display.getHUDTexture();
            if (tex != null) {
               RenderSystem.enableBlend();

               try {
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  PoseStack pose = g.pose();
                  pose.pushPose();

                  try {
                     pose.translate((float)x, (float)y, 0.0F);
                     pose.scale(scale, scale, 1.0F);
                     g.blit(tex, 0, 0, 0.0F, 0.0F, 39, 13, 39, 13);
                  } finally {
                     pose.popPose();
                  }
               } finally {
                  RenderSystem.disableBlend();
               }
            }
         }
      }
   }
}
