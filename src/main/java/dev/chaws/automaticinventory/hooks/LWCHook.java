package dev.chaws.automaticinventory.hooks;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Permission;
import com.griefcraft.scripting.event.LWCAccessEvent;
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
            if (protection == null)
                return false;

            LWCAccessEvent event = new LWCAccessEvent(player, protection, Permission.Access.NONE);
            LWC.getInstance().getModuleLoader().dispatchEvent(event);
            return protection.isRealOwner(player) || event.getAccess() == Permission.Access.PLAYER;
        }
        return true;
    }
}
