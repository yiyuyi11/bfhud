package cc.xypp.modernhud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Gear block for hotbar slots 3-6.
 *
 * <p>Layout, chosen so nothing sticks out of the rectangle: slot 3 spans the full width on the top row,
 * and slots 4-6 split the bottom row into three equal cells. The block sits in the bottom-right corner,
 * above the weapon block that already reports slots 1-2, so the two never overlap.
 *
 * <p>Dependency-free on purpose: plain {@code fill} plus {@code renderItem}, no config reads and no panel
 * helpers, because an exception inside those helpers silently blanked the earlier revisions.
 */
public final class HotbarHud implements IGuiOverlay {
   private static final int FIRST = 2;
   private static final int LAST = 5;
   private static final int CELL = 18;
   private static final int GAP = 2;
   private static final int MARGIN_RIGHT = 8;
   /** Clears the weapon block, which shows slots 1-2. */
   private static final int MARGIN_BOTTOM = 132;

   private HotbarHud() {
   }

   public static IGuiOverlay create() {
      return new HotbarHud();
   }

   @Override
   public void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.options.hideGui) return;
      // In a vehicle the hotbar widgets step aside: the vehicle readouts own that corner.
      if (mc.player.getRootVehicle() != mc.player) return;
      // Hidden on battlefield_ops screens (deploy, lobby, editor): those draw their own layout there.
      // Checked by class name because battlefield_ops is an optional dependency.
      if (mc.screen != null && mc.screen.getClass().getName().startsWith("cn.bfops.")) return;

      Inventory inventory = mc.player.getInventory();
      // 2x2 grid: slots 3-4 on the top row, 5-6 below.
      int width = 2 * CELL + GAP;
      int height = 2 * CELL + GAP;

      // Sits directly above the weapon block and right-aligned with it, so the two can never overlap no
      // matter how wide the weapon frame is.
      int frameRight;
      int frameTop;
      try {
         int fw = Math.max(40, cc.xypp.modernhud.config.WeaponConfig.frameWidth);
         int fh = Math.max(20, cc.xypp.modernhud.config.WeaponConfig.frameHeight);
         int offX = cc.xypp.modernhud.config.WeaponConfig.offsetX;
         int offY = cc.xypp.modernhud.config.WeaponConfig.offsetY;
         frameRight = screenWidth - offX;
         frameTop = screenHeight - offY - fh;
      } catch (Throwable notLoaded) {
         frameRight = screenWidth - MARGIN_RIGHT;
         frameTop = screenHeight - MARGIN_BOTTOM;
      }
      // Horizontal: immediately right of the weapon frame, bottom-aligned with it. The clamp keeps it on
      // screen even if the frame is wider than expected.
      int left = frameRight + GAP;
      if (left + width > screenWidth - 2) left = screenWidth - 2 - width;
      int top = Math.max(2, frameTop);
      int selected = inventory.selected;

      // one backdrop for the whole block: the cells sit inside it, nothing protrudes
      graphics.fill(left - 3, top - 3, left + width + 3, top + height + 3, 0x70101820);
      graphics.fill(left, top, left + width, top + height, 0x60202830);

      int x = left;
      for (int slot = FIRST; slot <= LAST; slot++) {
         int col = (slot - FIRST) % 2;
         int row = (slot - FIRST) / 2;
         cell(graphics, mc, inventory, slot, left + col * (CELL + GAP), top + row * (CELL + GAP),
                 CELL, CELL, selected);
      }
   }

   private void cell(GuiGraphics graphics, Minecraft mc, Inventory inventory,
                     int slot, int x, int y, int w, int h, int selected) {
      boolean active = slot == selected;
      if (active) {
         graphics.fill(x, y, x + w, y + h, 0x40404a52);
         graphics.fill(x, y, x + w, y + 2, 0xffd9a566);
         graphics.fill(x, y + h - 2, x + w, y + h, 0xffd9a566);
         graphics.fill(x, y, x + 2, y + h, 0xffd9a566);
         graphics.fill(x + w - 2, y, x + w, y + h, 0xffd9a566);
      }
      ItemStack stack = inventory.getItem(slot);
      if (stack.isEmpty()) return;
      int iconX = x + (w - 16) / 2;
      int iconY = y + (h - 16) / 2;
      graphics.renderItem(stack, iconX, iconY);
      if (stack.getCount() > 1) {
         graphics.drawString(mc.font, String.valueOf(stack.getCount()),
                 x + w - 9, y + h - 9, 0xffffffff, true);
      }
   }
}
