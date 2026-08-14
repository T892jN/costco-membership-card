package name.modid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;

public class CostcoWorldState extends PersistentState {

    public boolean barriersRemoved = false;

    private final Queue<ChunkTask> pendingChunks = new ArrayDeque<>();
    private final HashSet<Long> queuedChunks = new HashSet<>();

    /*
     * Increased to 50,000 to prevent the massive delay.
     * This will scan chunks roughly 25x faster.
     */
    private static final int BLOCKS_PER_TICK = 50000;

    /*
     * OPTIMIZATION: If your barriers are only built on the surface,
     * change these to reflect those heights (e.g., 60 and 100).
     * Leaving them at -64 and 320 will scan the entire vertical chunk.
     */
    private static final int SCAN_MIN_Y = -64;
    private static final int SCAN_MAX_Y = 320;


    public static final Codec<CostcoWorldState> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.BOOL.fieldOf("barriersRemoved")
                            .forGetter(state -> state.barriersRemoved)
            ).apply(instance, barriersRemoved -> {
                CostcoWorldState state = new CostcoWorldState();
                state.barriersRemoved = barriersRemoved;
                return state;
            }));


    public static final PersistentStateType<CostcoWorldState> TYPE =
            new PersistentStateType<>(
                    "costco_world",
                    context -> new CostcoWorldState(),
                    context -> CODEC,
                    null
            );


    public void activateBarrierRemoval(ServerWorld world, BlockPos employeePos) {
        barriersRemoved = true;
        markDirty();

        int viewDistance = world.getServer().getPlayerManager().getViewDistance();
        System.out.println("[COSTCO] Barrier removal activated!");

        // 1. Scan around every online player
        for (net.minecraft.server.network.ServerPlayerEntity player : world.getPlayers()) {
            int playerChunkX = player.getBlockX() >> 4;
            int playerChunkZ = player.getBlockZ() >> 4;

            for (int chunkX = playerChunkX - viewDistance; chunkX <= playerChunkX + viewDistance; chunkX++) {
                for (int chunkZ = playerChunkZ - viewDistance; chunkZ <= playerChunkZ + viewDistance; chunkZ++) {
                    if (world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
                        queueChunk(chunkX, chunkZ);
                    }
                }
            }
        }

        // 2. Scan the World Spawn chunks (which never unload)
        BlockPos spawnPos = world.getSpawnPos();
        int spawnChunkX = spawnPos.getX() >> 4;
        int spawnChunkZ = spawnPos.getZ() >> 4;

        // Scan a 5x5 grid around world spawn just to be safe
        for (int chunkX = spawnChunkX - 2; chunkX <= spawnChunkX + 2; chunkX++) {
            for (int chunkZ = spawnChunkZ - 2; chunkZ <= spawnChunkZ + 2; chunkZ++) {
                if (world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
                    queueChunk(chunkX, chunkZ);
                }
            }
        }
    }
    public void checkChunk(ServerWorld world, WorldChunk chunk) {
        if (!barriersRemoved) {
            return;
        }
        System.out.println("[COSTCO] CHUNK_LOAD detected: " + chunk.getPos());
        queueChunk(chunk.getPos().x, chunk.getPos().z);
    }


    private void queueChunk(int chunkX, int chunkZ) {
        long key = makeChunkKey(chunkX, chunkZ);

        if (queuedChunks.contains(key)) {
            return;
        }

        queuedChunks.add(key);
        pendingChunks.add(new ChunkTask(chunkX, chunkZ));

        System.out.println("[COSTCO] Queuing chunk: " + chunkX + ", " + chunkZ);
    }


    public void processQueue(ServerWorld world) {
        if (!barriersRemoved) {
            return;
        }

        int blocksChecked = 0;

        while (blocksChecked < BLOCKS_PER_TICK) {
            ChunkTask task = pendingChunks.peek();

            if (task == null) {
                return;
            }

            int checked = processChunk(world, task);
            blocksChecked += checked;

            if (task.finished()) {
                pendingChunks.poll();
                queuedChunks.remove(makeChunkKey(task.chunkX, task.chunkZ));
                System.out.println("[COSTCO] Finished chunk: " + task.chunkX + ", " + task.chunkZ);
            }

            if (checked == 0) {
                return;
            }
        }
    }


    private int processChunk(ServerWorld world, ChunkTask task) {
        // Enforce the configured Y-bounds, constrained by the actual world limits
        int bottomY = Math.max(SCAN_MIN_Y, world.getBottomY());
        int topY = Math.min(SCAN_MAX_Y, world.getTopYInclusive());

        int checked = 0;
        BlockPos.Mutable pos = new BlockPos.Mutable();

        while (!task.finished() && checked < BLOCKS_PER_TICK) {
            int worldX = (task.chunkX << 4) + task.x;
            int worldZ = (task.chunkZ << 4) + task.z;
            int worldY = bottomY + task.y;

            pos.set(worldX, worldY, worldZ);

            if (world.getBlockState(pos).isOf(Blocks.BARRIER)) {
                System.out.println("[COSTCO] Removing barrier at " + pos);
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            }

            checked++;
            task.y++;

            // Check if we reached the top limit for this column
            if (bottomY + task.y > topY) {
                task.y = 0;
                task.z++;

                // End of this row
                if (task.z >= 16) {
                    task.z = 0;
                    task.x++;

                    // Entire chunk finished
                    if (task.x >= 16) {
                        task.finished = true;
                    }
                }
            }
        }

        return checked;
    }


    private static long makeChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xffffffffL);
    }


    private static class ChunkTask {
        final int chunkX;
        final int chunkZ;

        int x = 0;
        int z = 0;
        int y = 0;

        boolean finished = false;

        ChunkTask(int chunkX, int chunkZ) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        boolean finished() {
            return finished;
        }
    }
}