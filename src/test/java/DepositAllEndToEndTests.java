import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.state.ChestMock;
import dev.chaws.automaticinventory.AutomaticInventory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DepositAllEndToEndTests {
	private ServerMock server;
	private AutomaticInventory plugin;

	@BeforeEach
	public void setUp()	{
		server = MockBukkit.mock();
		plugin = MockBukkit.load(AutomaticInventory.class);
	}

	@AfterEach
	public void tearDown()	{
		MockBukkit.unmock();
	}

	@Test
	void depositAllShouldDepositItemsInChests() {
		var world = new WorldMock(Material.DIRT, 3);
		server.addWorld(world);

		var block = world.getBlockAt(2, 3, 2);
		block.setType(Material.CHEST);
		var chest = (ChestMock)ChestMock.mockState(block);
		chest.getInventory().addItem(new ItemStack(Material.STONE, 1));

		var player = server.addPlayer();
		player.setOp(true);
		player.setLocation(new Location(world, 0, 3, 0));
		player.getInventory().setItem(9, new ItemStack(Material.STONE, 1));

		server.getScheduler().waitAsyncTasksFinished();

		Assertions.assertTrue(player.performCommand("depositall"));
		Assertions.assertTrue(chest.getInventory().contains(Material.STONE, 2));
	}
}
