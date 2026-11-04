package cc.xypp.modernhud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * The remaining three hotbar slots (indices 6-8), stacked in the right-middle band.
 *
 * <p>Same dependency-free style as {@link HotbarHud}: plain fills and item rendering only.
 */
public final class RemainingSlotsHud implements IGuiOverlay {
   private static final int FIRST = 6;
   private static final int LAST = 8;
   private static final int CELL = 22;
   private static final int GAP = 2;
   private static final int MARGIN_RIGHT = 8;

   private RemainingSlotsHud() {
   }

   public static IGuiOverlay create() {
      return new RemainingSlotsHud();
   }

   @Override
   public void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.player.isSpectator() || mc.options.hideGui) return;
      // In a vehicle the hotbar widgets step aside: the vehicle readouts own that corner.
      if (mc.player.getRootVehicle() != mc.player) return;
      // Hidden on battlefield_ops screens (deploy, lobby, editor): those draw their own layout there.
      // Checked by class name because battlefield_ops is an optional dependency.
      if (mc.screen != null && mc.screen.getClass().getName().startsWith("cn.bfops.")) return;

      var inventory = mc.player.getInventory();
      int count = LAST - FIRST + 1;
      int height = count * CELL + (count - 1) * GAP;
      int left = screenWidth - MARGIN_RIGHT - CELL;
      int top = (screenHeight - height) / 2;
      int selected = inventory.selected;

      graphics.fill(left - 3, top - 3, left + CELL + 3, top + height + 3, 0x70101820);

      int y = top;
      for (int slot = FIRST; slot <= LAST; slot++) {
         boolean active = slot == selected;
         graphics.fill(left, y, left + CELL, y + CELL, active ? 0x90404a52 : 0x60202830);
         if (active) {
            graphics.fill(left, y, left + CELL, y + 2, 0xffd9a566);
            graphics.fill(left, y + CELL - 2, left + CELL, y + CELL, 0xffd9a566);
            graphics.fill(left, y, left + 2, y + CELL, 0xffd9a566);
            graphics.fill(left + CELL - 2, y, left + CELL, y + CELL, 0xffd9a566);
         }
         ItemStack stack = inventory.getItem(slot);
         if (!stack.isEmpty()) {
            graphics.renderItem(stack, left + 3, y + 3);
            if (stack.getCount() > 1) {
               graphics.drawString(mc.font, String.valueOf(stack.getCount()),
                       left + CELL - 9, y + CELL - 9, 0xffffffff, true);
            }
         }
         y += CELL + GAP;
      }
   }
}
