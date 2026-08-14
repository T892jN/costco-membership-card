package name.modid;

import net.fabricmc.api.ModInitializer;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class CostcoMembershipCard implements ModInitializer {
	public static final String MOD_ID = "costco-membership-card";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		CostcoTrades.registerTrades();
		ModEntities.registerModEntities();
		CostcoChunkHandler.register();

		ServerTickEvents.END_WORLD_TICK.register(
				world -> {

					CostcoWorldState state =
							CostcoWorldStateManager.get(world);

					state.processQueue(world);
				}
		);

		FabricDefaultAttributeRegistry.register(
				ModEntities.COSTCO_EMPLOYEE,
				VillagerEntity.createVillagerAttributes()
		);

		LOGGER.info("Costco Membership Card loaded!");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
