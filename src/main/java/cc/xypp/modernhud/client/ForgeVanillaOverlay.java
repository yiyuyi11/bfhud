package cc.xypp.modernhud.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
@EventBusSubscriber(modid="modernhud",value=Dist.CLIENT)
public final class ForgeVanillaOverlay {
    @SubscribeEvent
    public static void pre(RenderGuiOverlayEvent.Pre event) {
        var player = Minecraft.getInstance().player;
        // The custom vitals and inventory widgets step aside while riding.
        if (player == null || player.isSpectator() || player.getRootVehicle() != player) return;
        var id = event.getOverlay().id();
        if (id.equals(VanillaGuiOverlay.HOTBAR.id())
                || id.equals(VanillaGuiOverlay.PLAYER_HEALTH.id())
                || id.equals(VanillaGuiOverlay.ARMOR_LEVEL.id())
                || id.equals(VanillaGuiOverlay.FOOD_LEVEL.id())
                || id.equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) {
            event.setCanceled(true);
        }
    }
}
