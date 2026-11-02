package cc.xypp.modernhud.client;

import net.minecraft.client.gui.GuiGraphics;

/** Batching shim: 1.21.1 fills are cheap enough that the batch is a pass-through. */
public final class HudBatch {
    private HudBatch() {
    }

    public static boolean active() {
        return false;
    }

    public static void run(GuiGraphics g, Runnable body) {
        body.run();
    }
}
