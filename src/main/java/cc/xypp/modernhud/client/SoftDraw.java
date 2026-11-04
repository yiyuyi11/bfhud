package cc.xypp.modernhud.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

import java.util.function.Consumer;

/**
 * Soft drawing helpers, the real mesh implementation.
 *
 * <p>The 1.20.1 original built every gradient and glow as a {@code BufferBuilder} mesh with per-vertex
 * colours. 1.21.1 keeps that API, so this reproduces the original instead of approximating with stacked
 * fills: one {@link BufferBuilder} pass per shape, {@code POSITION_COLOR} quads, straight into
 * {@link BufferUploader}.
 */
public final class SoftDraw {
    private SoftDraw() {
    }

    /** One mesh in flight, already bound to the current pose matrix. */
    private static final class Batch {
        private final BufferBuilder bb;
        private final Matrix4f matrix;

        Batch(BufferBuilder bb, Matrix4f matrix) {
            this.bb = bb;
            this.matrix = matrix;
        }

        void quad(float x0, float y0, float x1, float y1, int argb) {
            quad(x0, y0, x1, y1, argb, argb, argb, argb);
        }

        /** Corner colours in order: top-left, bottom-left, bottom-right, top-right. */
        void quad(float x0, float y0, float x1, float y1, int c00, int c01, int c11, int c10) {
            bb.vertex(matrix, x0, y0, 0.0F).color(c00).endVertex();
            bb.vertex(matrix, x0, y1, 0.0F).color(c01).endVertex();
            bb.vertex(matrix, x1, y1, 0.0F).color(c11).endVertex();
            bb.vertex(matrix, x1, y0, 0.0F).color(c10).endVertex();
        }

        /** A flat-shaded triangle, used by the radial and arc helpers. */
        void tri(float x0, float y0, float x1, float y1, float x2, float y2, int argb) {
            bb.vertex(matrix, x0, y0, 0.0F).color(argb).endVertex();
            bb.vertex(matrix, x1, y1, 0.0F).color(argb).endVertex();
            bb.vertex(matrix, x2, y2, 0.0F).color(argb).endVertex();
            bb.vertex(matrix, x2, y2, 0.0F).color(argb).endVertex();
        }

        /** A triangle with independent corner colours. */
        void tri(float x0, float y0, float x1, float y1, float x2, float y2, int c0, int c1, int c2) {
            bb.vertex(matrix, x0, y0, 0.0F).color(c0).endVertex();
            bb.vertex(matrix, x1, y1, 0.0F).color(c1).endVertex();
            bb.vertex(matrix, x2, y2, 0.0F).color(c2).endVertex();
            bb.vertex(matrix, x2, y2, 0.0F).color(c2).endVertex();
        }
    }

    private static void mesh(GuiGraphics g, Consumer<Batch> body) {
        // BufferUploader draws with whatever shader is bound, and this path bypasses RenderType, so the
        // position-colour shader and the translucent blend state are set here explicitly.
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (g == null) return;
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        bb.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        try {
            body.accept(new Batch(bb, g.pose().last().pose()));
            BufferBuilder.RenderedBuffer rendered = bb.endOrDiscardIfEmpty();
            if (rendered != null) BufferUploader.drawWithShader(rendered);
        } finally {
            RenderSystem.disableBlend();
        }
    }

    public static void gradientV(GuiGraphics g, float x0, float y0, float x1, float y1, int argbTop, int argbBottom) {
        mesh(g, b -> b.quad(x0, y0, x1, y1, argbTop, argbBottom, argbBottom, argbTop));
    }

    public static void gradientH(GuiGraphics g, float x0, float y0, float x1, float y1, int argbLeft, int argbRight) {
        mesh(g, b -> b.quad(x0, y0, x1, y1, argbLeft, argbLeft, argbRight, argbRight));
    }

    public static void rect(GuiGraphics g, float x0, float y0, float x1, float y1, int argb) {
        mesh(g, b -> b.quad(x0, y0, x1, y1, argb));
    }

    public static void soft(GuiGraphics g, float x, float y, float w, float h, int rgb, float alphaCore, float spread, boolean additive) {
        soft(g, x, y, w, h, rgb, alphaCore, 0.0F, spread, additive);
    }

    /**
     * Core plus a smooth falloff ring, built as one quad per band with both corner colours at that band's
     * alpha. Bands overlap by one pixel so the result reads as a continuous blur rather than steps.
     */
    public static void soft(GuiGraphics g, float x, float y, float w, float h, int rgb, float alphaCore, float alphaEdge, float spread, boolean additive) {
        float rings = Math.max(1.0F, spread);
        int bands = 8;
        int core = argb(rgb, alphaCore);
        mesh(g, b -> {
            b.quad(x, y, x + w, y + h, core);
            for (int i = bands; i >= 1; i--) {
                float t = i / (float) bands;
                float alpha = alphaCore + (alphaEdge - alphaCore) * t;
                // Quadratic falloff is what the original glow shader produced; linear reads as a hard edge.
                alpha *= (1.0F - t) * (1.0F - t);
                if (alpha <= 0.004F) continue;
                float d = rings * t;
                int colour = argb(rgb, alpha);
                b.quad(x - d, y - d, x + w + d, y + h + d, colour);
            }
        });
    }

    public static void radial(GuiGraphics g, float cx, float cy, float radius, int rgb, float alphaCenter, boolean additive) {
        radial(g, cx, cy, radius, rgb, alphaCenter, additive, 0xF);
    }

    /** Concentric wedges from the centre outwards; {@code quarterMask} limits which quadrants are drawn. */
    public static void radial(GuiGraphics g, float cx, float cy, float radius, int rgb, float alphaCenter, boolean additive, int quarterMask) {
        if (radius <= 0.05F || quarterMask == 0) return;
        if (radius <= 0.05F || quarterMask == 0) return;
        int steps = 24;
        int rings = 6;
        mesh(g, b -> {
            for (int ring = 0; ring < rings; ring++) {
                float r1 = radius * (ring + 1) / rings;
                int c1 = argb(rgb, alphaCenter * (1.0F - (ring + 1) / (float) rings));
                for (int i = 0; i < steps; i++) {
                    float a0 = (float) (Math.PI * 2.0 * i / steps);
                    float a1 = (float) (Math.PI * 2.0 * (i + 1) / steps);
                    float am = (a0 + a1) * 0.5F;
                    boolean right = Math.cos(am) >= 0.0F;
                    boolean down = Math.sin(am) >= 0.0F;
                    int bit = (right ? 2 : 0) | (down ? 8 : 1);
                    if ((quarterMask & bit) == 0) continue;
                    float x1 = cx + (float) Math.cos(a0) * r1;
                    float y1 = cy + (float) Math.sin(a0) * r1;
                    float x2 = cx + (float) Math.cos(a1) * r1;
                    float y2 = cy + (float) Math.sin(a1) * r1;
                    b.tri(cx, cy, x1, y1, x2, y2, c1);
                }
            }
        });
    }

    /** Ring segment between two radii and two angles, with a soft outer glow band. */
    public static void arc(GuiGraphics g, float cx, float cy, float rInner, float rOuter, float a0, float a1, float glow, int rgb, float alpha, boolean additive) {
        int steps = 32;
        int colour = argb(rgb, alpha);
        int glowColour = argb(rgb, alpha * 0.45F);
        mesh(g, b -> {
            for (int i = 0; i < steps; i++) {
                float aa = a0 + (a1 - a0) * i / steps;
                float ab = a0 + (a1 - a0) * (i + 1) / steps;
                float xi0 = cx + (float) Math.cos(aa) * rInner;
                float yi0 = cy + (float) Math.sin(aa) * rInner;
                float xo0 = cx + (float) Math.cos(aa) * rOuter;
                float yo0 = cy + (float) Math.sin(aa) * rOuter;
                float xi1 = cx + (float) Math.cos(ab) * rInner;
                float yi1 = cy + (float) Math.sin(ab) * rInner;
                float xo1 = cx + (float) Math.cos(ab) * rOuter;
                float yo1 = cy + (float) Math.sin(ab) * rOuter;
                b.tri(xi0, yi0, xi1, yi1, xo1, yo1, colour, colour, colour);
                b.tri(xi0, yi0, xo1, yo1, xo0, yo0, colour, colour, colour);
                if (glow > 0.0F) {
                    float gr = rOuter + glow;
                    float gx0 = cx + (float) Math.cos(aa) * gr;
                    float gy0 = cy + (float) Math.sin(aa) * gr;
                    float gx1 = cx + (float) Math.cos(ab) * gr;
                    float gy1 = cy + (float) Math.sin(ab) * gr;
                    b.tri(xo0, yo0, xo1, yo1, gx1, gy1, glowColour, glowColour, glowColour);
                    b.tri(xo0, yo0, gx1, gy1, gx0, gy0, glowColour, glowColour, glowColour);
                }
            }
        });
    }

    private static int argb(int rgb, float alpha) {
        int a = (int) (Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);
        return a << 24 | rgb & 0xFFFFFF;
    }
}
