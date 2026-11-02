package cc.xypp.modernhud.client;

import cc.xypp.modernhud.config.HudConfig;

public final class ClientFeedback {
   private static long lastHitTriggerMillis = -1L;
   private static long lastBreakTriggerMillis = -1L;

   private ClientFeedback() {
   }

   public static void handle(boolean fullyBroken, boolean recipientIsAttacker, boolean silent) {
      if (recipientIsAttacker || HudConfig.showFeedbackWhenVictim) {
         if (!silent) {
            ClientCombatSounds.playShield(fullyBroken);
         }

         if (fullyBroken) {
            if (recipientIsAttacker) {
               lastBreakTriggerMillis = System.currentTimeMillis();
            }
         } else {
            lastHitTriggerMillis = System.currentTimeMillis();
         }
      }
   }

   public static long millisSinceBreakTrigger() {
      return lastBreakTriggerMillis < 0L ? -1L : System.currentTimeMillis() - lastBreakTriggerMillis;
   }

   public static long millisSinceHitTrigger() {
      return lastHitTriggerMillis < 0L ? -1L : System.currentTimeMillis() - lastHitTriggerMillis;
   }
}
