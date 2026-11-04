package cc.xypp.modernhud.client.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.Locale;

/**
 * LRTactical gear classification.
 *
 * <p>Reimplemented from the original mod's logic: it reads the item's registry id and classifies by path
 * keywords. That only needs the item id, not the 1.21.1 data-component API, so the classification works
 * exactly as before.
 */
public final class LrTacticalCompat {
    public enum Kind { NONE, MELEE, THROWABLE, CONSUMABLE }

    private static final boolean LOADED = ModList.get().isLoaded("lrtactical");

    private LrTacticalCompat() {
    }

    public static boolean loaded() {
        return LOADED;
    }

    public static Kind kind(ItemStack stack) {
        if (!LOADED || stack == null || stack.isEmpty()) return Kind.NONE;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null || !"lrtactical".equals(id.getNamespace())) return Kind.NONE;
        String path = id.getPath().toLowerCase(Locale.ROOT);
        if (path.contains("melee") || path.contains("shield")) return Kind.MELEE;
        if (path.contains("throwable") || path.contains("grenade")) return Kind.THROWABLE;
        return path.contains("consum") ? Kind.CONSUMABLE : Kind.NONE;
    }

    public static boolean isShield(ItemStack stack) {
        if (!LOADED || stack == null || stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && "lrtactical".equals(id.getNamespace())
                && id.getPath().toLowerCase(Locale.ROOT).contains("shield");
    }

    /** Stable grouping key: the item id without the stack count. */
    public static String key(ItemStack stack) {
        if (!LOADED || stack == null || stack.isEmpty()) return "";
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null ? "" : id.toString();
    }
}
