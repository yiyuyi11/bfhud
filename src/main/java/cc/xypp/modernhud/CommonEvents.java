package cc.xypp.modernhud;

import cc.xypp.modernhud.config.CommonConfig;
import cc.xypp.modernhud.network.ModNetworking;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Remove;

/**
 * Shield-break feedback.
 *
 * <p>1.21.1 folded {@code LivingHurtEvent} into {@code LivingDamageEvent.Pre} and dropped the standalone
 * living-tick event, so this class listens to the pre-damage hook plus the effect-removed hook only.
 */
@EventBusSubscriber(modid = "modernhud")
public class CommonEvents {
   private static final Map<LivingEntity, Float> pendingHurtAbsorption = new WeakHashMap<>();
   private static final Map<LivingEntity, Long> lastBreakHandledTick = new WeakHashMap<>();

   @SubscribeEvent
   public static void onLivingDamage(LivingDamageEvent.Pre event) {
      LivingEntity entity = event.getEntity();
      if (entity.level().isClientSide()) return;
      float before = entity.getAbsorptionAmount() + event.getNewDamage();
      Float previous = pendingHurtAbsorption.remove(entity);
      if (previous == null) return;
      float after = entity.getAbsorptionAmount();
      if (after >= previous) return;
      boolean fullyBroken = after <= 0.0F;
      if (!fullyBroken && !CommonConfig.enableHitFeedback) return;
      if (fullyBroken && !CommonConfig.enableBreakFeedback) return;

      ServerPlayer attacker = event.getSource().getEntity() instanceof ServerPlayer sp ? sp : null;
      ServerPlayer victim = entity instanceof ServerPlayer vp ? vp : null;
      boolean lethal = event.getNewDamage() >= entity.getHealth();
      if (attacker != null) {
         send(attacker, fullyBroken, true, lethal);
      }
      if (victim != null && victim != attacker) send(victim, fullyBroken, false, lethal);
      if (fullyBroken) lastBreakHandledTick.put(entity, entity.level().getGameTime());
   }

   @SubscribeEvent
   public static void onEffectRemoved(Remove event) {
      if (!CommonConfig.clearAbsorptionOnEffectEnd) return;
      LivingEntity entity = event.getEntity();
      if (entity.level().isClientSide()) return;
      MobEffectInstance instance = event.getEffectInstance();
      if (instance == null || !instance.is(MobEffects.ABSORPTION)) return;
      if (entity.getAbsorptionAmount() <= 0.0F) return;
      entity.setAbsorptionAmount(0.0F);
      lastBreakHandledTick.put(entity, entity.level().getGameTime());
      if (CommonConfig.enableBreakFeedback && entity instanceof ServerPlayer serverPlayer) {
         send(serverPlayer, true, false, false);
      }
   }

   private static void send(ServerPlayer recipient, boolean fullyBroken, boolean recipientIsAttacker, boolean silent) {
      // network feedback disabled until the payload rewrite lands
   }
}
