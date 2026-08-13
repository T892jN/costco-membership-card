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
    
                    System.out.println(
                            "[COSTCO TEST] Chunk loaded: " + chunk.getPos()
                    );
    
                    System.out.println(
                            "[COSTCO TEST] barriersRemoved = "
                                    + state.barriersRemoved
                    );
    
                    // If the membership has been accepted,
                    // remove barriers from this newly loaded chunk.
                    state.checkChunk(world, chunk);
                }
        );
    }
}