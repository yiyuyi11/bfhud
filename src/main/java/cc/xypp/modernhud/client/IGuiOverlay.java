package cc.xypp.modernhud.client;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Local stand-in for the Forge-era overlay interface.
 *
 * <p>NeoForge 1.21.1 removed {@code IGuiOverlay} in favour of {@code LayeredDraw.Layer}. Keeping a thin
 * local interface with the same shape lets the ported overlay classes stay recognisable while a single
 * adapter bridges them to the new layer system.
 */
public interface IGuiOverlay {
    void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight);
}
