package dev.chaws.automaticinventory.hooks;

import com.griefcraft.lwc.LWC;
import org.bukkit.Location;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;

public class LWCHook {

    private static boolean enabled = false;

    public static void enable() {
        enabled = true;
    }

    public static boolean canUseContainer(Inventory inventory, Player player) {
        if (inventory.getHolder() instanceof BlockInventoryHolder || inventory.getHolder() instanceof DoubleChest) {
            Location location = inventory.getLocation();
            return canUseContainer(location, player);
        }
        return false;
    }

    public static boolean canUseContainer(Location location, Player player) {
        if (enabled) {
            var protection = LWC.getInstance().findProtection(location);
            return protection != null && protection.isRealOwner(player);
        }
        return true;
    }
}
