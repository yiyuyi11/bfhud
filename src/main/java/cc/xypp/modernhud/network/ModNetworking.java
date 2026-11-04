package cc.xypp.modernhud.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Network entry point.
 *
 * <p>The original used a Forge 1.20.1 {@code SimpleChannel}. NeoForge 1.21.1 requires
 * {@code CustomPacketPayload} records registered through a {@code PayloadRegistrar}, which is a larger
 * rewrite than the HUD port itself. Until that lands, the feedback packets are not transmitted: the HUD
 * renders correctly, only the server-driven shield/hit feedback messages are skipped.
 */
public final class ModNetworking {
   public static void sendTo(ServerPlayer player, Object message) {
      // no-op until the payload rewrite lands
   }

   public static void register(IEventBus modBus) {
      // no-op until the payload rewrite lands
   }

   private ModNetworking() {
   }
}
