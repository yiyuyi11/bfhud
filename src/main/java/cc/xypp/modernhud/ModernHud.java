package cc.xypp.modernhud;

import cc.xypp.modernhud.client.ClientEvents;
import cc.xypp.modernhud.client.ModKeybinds;
import cc.xypp.modernhud.config.BossBarConfig;
import cc.xypp.modernhud.config.CommonConfig;
import cc.xypp.modernhud.config.ConfigRefresh;
import cc.xypp.modernhud.config.ExtraHudConfig;
import cc.xypp.modernhud.config.HitIndicationConfig;
import cc.xypp.modernhud.config.HudConfig;
import cc.xypp.modernhud.config.SoundsConfig;
import cc.xypp.modernhud.config.WeaponConfig;
import cc.xypp.modernhud.network.ModNetworking;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

/**
 * ModernHUD 1.21.1 NeoForge entry point.
 *
 * <p>Scope: everything except the killstreak, compass and squad widgets. Registration follows the NeoForge
 * pattern — the mod bus and container arrive through the constructor instead of the old static
 * {@code FMLJavaModLoadingContext} / {@code ModLoadingContext} singletons.
 */
@Mod("modernhud")
public class ModernHud {
    public static final String MOD_ID = "modernhud";

    public ModernHud() {
        IEventBus modBus=net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        var container=net.minecraftforge.fml.ModLoadingContext.get();
        SoundRegistry.SOUNDS.register(modBus);
        ModNetworking.register(modBus);
        // RegisterGuiLayersEvent and RegisterKeyMappingsEvent are MOD-bus events. The @EventBusSubscriber
        // annotation defaults to the game bus, and its `bus` attribute is removed in 1.21.1, so the listeners
        // are attached explicitly here instead.
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == Dist.CLIENT) {
        modBus.addListener(ClientEvents::onRegisterOverlays);
        modBus.addListener(ClientEvents::onClientSetup);
        modBus.addListener(ModKeybinds.ModBus::onRegisterKeys);
        }
        // The config classes keep their values in plain static fields filled by refresh(); without this the
        // feature flags stay at their Java defaults (all off) and every HUD layer returns immediately.
        modBus.addListener(ModernHud::onConfigLoad);
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, HudConfig.SPEC, "modernhud-hud.toml");
        container.registerConfig(ModConfig.Type.CLIENT, WeaponConfig.SPEC, "modernhud-weapon.toml");
        container.registerConfig(ModConfig.Type.CLIENT, HitIndicationConfig.SPEC, "modernhud-hitindication.toml");
        container.registerConfig(ModConfig.Type.CLIENT, BossBarConfig.SPEC, "modernhud-bossbar.toml");
        container.registerConfig(ModConfig.Type.CLIENT, SoundsConfig.SPEC, "modernhud-sounds.toml");
        container.registerConfig(ModConfig.Type.CLIENT, ExtraHudConfig.SPEC, "modernhud-extra.toml");
    }

    private static void onConfigLoad(net.minecraftforge.fml.event.config.ModConfigEvent event) {
        ConfigRefresh.refreshAll();
    }

}
