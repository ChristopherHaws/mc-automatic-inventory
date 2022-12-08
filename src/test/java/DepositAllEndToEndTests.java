import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.state.ChestMock;
import dev.chaws.automaticinventory.AutomaticInventory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DepositAllEndToEndTests {
	private static final Logger logger = LoggerFactory.getLogger(DepositAllEndToEndTests.class);

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
		var world = server.addSimpleWorld("world");

		var block = world.getBlockAt(2, 5, 2);
		block.setType(Material.CHEST);
		var chest = (ChestMock)ChestMock.mockState(block);
		chest.getInventory().addItem(new ItemStack(Material.STONE, 1));

		var player = server.addPlayer();
		player.setOp(true);
		player.setLocation(new Location(world, 0, 5, 0));
		player.getInventory().setItem(9, new ItemStack(Material.STONE, 1));

		Assertions.assertTrue(player.performCommand("depositall"));

//		server.getScheduler().performTicks(1000L);
//		while (server.getScheduler().getNumberOfQueuedAsyncTasks() != 0) {
//			server.getScheduler().waitAsyncTasksFinished();
//		}

		server.getScheduler().waitAsyncTasksFinished();
		server.getScheduler().performTicks(1000L);


		Assertions.assertFalse(player.getInventory().contains(Material.STONE));
		Assertions.assertTrue(chest.getInventory().contains(Material.STONE, 2));
	}

	@Test
	void foo() {
		Assertions.assertDoesNotThrow(() -> {
			for (Material mat : Material.values()) {
				var legacy = Bukkit.getUnsafe().fromLegacy(mat);

				if (mat != legacy) {
					logger.info(mat.toString() + ": " + legacy.toString());
				}
			}
		});
	}
}
