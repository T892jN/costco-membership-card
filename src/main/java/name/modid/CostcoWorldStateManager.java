package name.modid;

import net.minecraft.server.world.ServerWorld;

public class CostcoWorldStateManager {

    public static CostcoWorldState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                CostcoWorldState.TYPE
        );
    }
}