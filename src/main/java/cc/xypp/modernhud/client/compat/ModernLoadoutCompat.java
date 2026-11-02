package cc.xypp.modernhud.client.compat;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Method;
import java.util.Arrays;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

public final class ModernLoadoutCompat {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final boolean LOADED = ModList.get().isLoaded("mwloadout");
   private static final long HELD_POLL_MS = 250L;
   private static boolean bindAttempted;
   private static boolean bound;
   private static Method mVersion;
   private static Method mActive;
   private static Method mSpecialist;
   private static Method mProgress;
   private static Method mKills;
   private static Method mIds;
   private static Method mEarned;
   private static Method mHeld;
   private static Method mIcon;
   private static Method mTexture;
   private static Method mLabel;
   private static Method mSetExternal;
   private static long lastVersion = Long.MIN_VALUE;
   private static long lastHeldPollMs;
   private static boolean externalApplied;
   private static boolean active;
   private static boolean specialist;
   private static float progress;
   private static int[] kills = new int[0];
   private static String[] ids = new String[0];
   private static boolean[] earned = new boolean[0];
   private static boolean[] held = new boolean[0];
   private static ItemStack[] icons = new ItemStack[0];
   private static ResourceLocation[] textures = new ResourceLocation[0];
   private static String[] labels = new String[0];
   private static long[] earnedAtMs = new long[0];
   private static long lastChangeMs;
   private static String ladderKey = "";
   private static long selectionStartMs;
   private static final long PERK_POLL_MS = 250L;
   private static boolean perkBindAttempted;
   private static boolean perkBound;
   private static Method mPerkVersion;
   private static Method mPerkIds;
   private static Method mPerkLabels;
   private static Method mPerkTextures;
   private static long lastPerkVersion = Long.MIN_VALUE;
   private static long lastPerkPollMs;
   private static String[] perkIds = new String[0];
   private static String[] perkLabels = new String[0];
   private static ResourceLocation[] perkTextures = new ResourceLocation[0];
   private static long perkSelectionStartMs;

   private ModernLoadoutCompat() {
   }

   public static boolean loaded() {
      return LOADED;
   }

   public static boolean available() {
      if (!LOADED) {
         return false;
      } else {
         if (!bindAttempted) {
            bindAttempted = true;

            try {
               Class<?> api = Class.forName("dev.szo9k4.mwloadout.api.StreakApi");
               mVersion = api.getMethod("version");
               mActive = api.getMethod("active");
               mSpecialist = api.getMethod("specialist");
               mProgress = api.getMethod("progress");
               mKills = api.getMethod("kills");
               mIds = api.getMethod("ids");
               mEarned = api.getMethod("earned");
               mHeld = api.getMethod("held");
               mIcon = api.getMethod("icon", int.class);
               mTexture = api.getMethod("texture", int.class);
               mLabel = api.getMethod("label", int.class);
               mSetExternal = api.getMethod("setExternalHud", boolean.class);
               bound = true;
            } catch (Throwable var1) {
               LOGGER.warn(
                  "[{}] ModernLoadout is installed but has no streak API (needs a newer build) - killstreak HUD compat disabled: {}",
                  "modernhud",
                  var1.toString()
               );
               bound = false;
            }
         }

         return bound;
      }
   }

   public static void setExternalHud(boolean value) {
      if (available() && externalApplied != value) {
         try {
            mSetExternal.invoke(null, value);
            externalApplied = value;
         } catch (Throwable var2) {
            bound = false;
            LOGGER.warn("[{}] ModernLoadout streak API call failed, disabling compat: {}", "modernhud", var2.toString());
         }
      }
   }

   public static boolean refresh() {
      if (!available()) {
         return false;
      } else {
         try {
            long now = System.currentTimeMillis();
            long version = (Long)mVersion.invoke(null);
            if (version != lastVersion) {
               lastVersion = version;
               readSnapshot(now);
            } else if (active && !specialist && now - lastHeldPollMs >= 250L) {
               lastHeldPollMs = now;
               held = (boolean[])mHeld.invoke(null);
            }

            return active && kills.length > 0;
         } catch (Throwable var4) {
            bound = false;
            LOGGER.warn("[{}] ModernLoadout streak API read failed, disabling compat: {}", "modernhud", var4.toString());
            return false;
         }
      }
   }

   private static void readSnapshot(long now) throws ReflectiveOperationException {
      boolean wasSpecialist = specialist;
      boolean[] oldEarned = earned;
      float oldProgress = progress;
      active = (Boolean)mActive.invoke(null);
      specialist = (Boolean)mSpecialist.invoke(null);
      progress = (Float)mProgress.invoke(null);
      kills = (int[])((int[])mKills.invoke(null)).clone();
      ids = (String[])((String[])mIds.invoke(null)).clone();
      earned = (boolean[])((boolean[])mEarned.invoke(null)).clone();
      held = specialist ? new boolean[kills.length] : (boolean[])mHeld.invoke(null);
      lastHeldPollMs = now;
      int n = kills.length;
      icons = new ItemStack[n];
      textures = new ResourceLocation[n];
      labels = new String[n];
      long[] oldEarnedAt = earnedAtMs;
      earnedAtMs = new long[n];

      for (int i = 0; i < n; i++) {
         icons[i] = (ItemStack)mIcon.invoke(null, i);
         String tex = (String)mTexture.invoke(null, i);
         textures[i] = tex != null && !tex.isEmpty() ? ResourceLocation.tryParse(tex) : null;
         labels[i] = (String)mLabel.invoke(null, i);
         boolean sameLadder = wasSpecialist == specialist && oldEarned.length == n;
         if (earned[i] && sameLadder && !oldEarned[i]) {
            earnedAtMs[i] = now;
         } else if (sameLadder && i < oldEarnedAt.length) {
            earnedAtMs[i] = oldEarnedAt[i];
         }
      }

      if (active && n > 0) {
         String key = specialist + "|" + Arrays.toString(kills) + "|" + String.join(",", ids);
         if (!key.equals(ladderKey)) {
            ladderKey = key;
            selectionStartMs = now;
         }
      }

      if (Math.abs(progress - oldProgress) > 0.01F || oldEarned.length != n) {
         lastChangeMs = now;
      }

      for (int ix = 0; ix < Math.min(oldEarned.length, n); ix++) {
         if (earned[ix] != oldEarned[ix]) {
            lastChangeMs = now;
         }
      }
   }

   public static boolean perksAvailable() {
      if (!LOADED) {
         return false;
      } else {
         if (!perkBindAttempted) {
            perkBindAttempted = true;

            try {
               Class<?> api = Class.forName("dev.szo9k4.mwloadout.api.StreakApi");
               mPerkVersion = api.getMethod("selectedPerksVersion");
               mPerkIds = api.getMethod("selectedPerkIds");
               mPerkLabels = api.getMethod("selectedPerkLabels");
               mPerkTextures = api.getMethod("selectedPerkTextures");
               perkBound = true;
            } catch (Throwable var1) {
               LOGGER.warn("[{}] ModernLoadout has no selected-perks API (needs a newer build) - perk toast disabled: {}", "modernhud", var1.toString());
               perkBound = false;
            }
         }

         return perkBound;
      }
   }

   public static boolean refreshPerks() {
      if (!perksAvailable()) {
         return false;
      } else {
         try {
            long now = System.currentTimeMillis();
            if (now - lastPerkPollMs >= 250L) {
               lastPerkPollMs = now;
               long version = (Long)mPerkVersion.invoke(null);
               if (version != lastPerkVersion) {
                  lastPerkVersion = version;
                  perkIds = (String[])((String[])mPerkIds.invoke(null)).clone();
                  perkLabels = (String[])((String[])mPerkLabels.invoke(null)).clone();
                  String[] tex = (String[])mPerkTextures.invoke(null);
                  perkTextures = new ResourceLocation[tex.length];

                  for (int i = 0; i < tex.length; i++) {
                     perkTextures[i] = tex[i] != null && !tex[i].isEmpty() ? ResourceLocation.tryParse(tex[i]) : null;
                  }

                  if (perkIds.length > 0) {
                     perkSelectionStartMs = now;
                  }
               }
            }

            return perkIds.length > 0;
         } catch (Throwable var6) {
            perkBound = false;
            LOGGER.warn("[{}] ModernLoadout perk API read failed, disabling perk toast: {}", "modernhud", var6.toString());
            return false;
         }
      }
   }

   public static int perkCount() {
      return perkIds.length;
   }

   public static String perkLabel(int i) {
      return perkLabels[i] == null ? "" : perkLabels[i];
   }

   public static ResourceLocation perkTexture(int i) {
      return perkTextures[i];
   }

   public static long perkSelectionStartMs() {
      return perkSelectionStartMs;
   }

   public static boolean isSpecialist() {
      return specialist;
   }

   public static float progress() {
      return progress;
   }

   public static int rungCount() {
      return kills.length;
   }

   public static int kills(int i) {
      return kills[i];
   }

   public static boolean earned(int i) {
      return earned[i];
   }

   public static boolean held(int i) {
      return i < held.length && held[i];
   }

   public static ItemStack icon(int i) {
      return icons[i] == null ? ItemStack.EMPTY : icons[i];
   }

   public static ResourceLocation texture(int i) {
      return textures[i];
   }

   public static String label(int i) {
      return labels[i] == null ? "" : labels[i];
   }

   public static long earnedAtMs(int i) {
      return earnedAtMs[i];
   }

   public static long lastChangeMs() {
      return lastChangeMs;
   }

   public static String ladderKey() {
      return ladderKey;
   }

   public static long selectionStartMs() {
      return selectionStartMs;
   }

   public static boolean anyReady() {
      if (specialist) {
         return false;
      } else {
         for (int i = 0; i < kills.length; i++) {
            if (earned[i] && held(i)) {
               return true;
            }
         }

         return false;
      }
   }
}
