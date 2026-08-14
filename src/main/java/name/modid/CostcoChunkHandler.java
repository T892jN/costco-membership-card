package name.modid;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;

public class CostcoChunkHandler {

    public static void register() {

        ServerChunkEvents.CHUNK_LOAD.register(
                (ServerWorld world, WorldChunk chunk) -> {

                    CostcoWorldState state =
                            CostcoWorldStateManager.get(world);

                    if (state.barriersRemoved) {

                        state.checkChunk(
                                world,
                                chunk
                        );
                    }
                }
        );
    }
}