package name.modid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayDeque;
import java.util.Queue;

public class CostcoWorldState extends PersistentState {

    public boolean barriersRemoved = false;

    // Chunks waiting to be processed
    private final Queue<ChunkTask> pendingChunks = new ArrayDeque<>();

    // Prevent the same chunk from being queued multiple times
    private final java.util.HashSet<Long> queuedChunks = new java.util.HashSet<>();

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

    public CostcoWorldState() {
    }

    /**
     * Starts the barrier-removal system.
     */
    public void activateBarrierRemoval(
            ServerWorld world,
            BlockPos employeePos
    ) {
        barriersRemoved = true;
        markDirty();

        queueChunk(
                employeePos.getX() >> 4,
                employeePos.getZ() >> 4
        );
    }

    /**
     * Called when Minecraft loads a chunk.
     *
     * IMPORTANT:
     * This does NOT scan the chunk.
     * It only adds the chunk to our queue.
     */
    public void checkChunk(
            ServerWorld world,
            WorldChunk chunk
    ) {
        if (!barriersRemoved) {
            return;
        }

        queueChunk(
                chunk.getPos().x,
                chunk.getPos().z
        );
    }

    private void queueChunk(int chunkX, int chunkZ) {

        long key = ChunkTask.makeKey(chunkX, chunkZ);

        // Don't queue the same chunk repeatedly.
        if (queuedChunks.contains(key)) {
            return;
        }

        queuedChunks.add(key);

        pendingChunks.add(
                new ChunkTask(chunkX, chunkZ)
        );
    }

    /**
     * Processes a small amount of work every server tick.
     */
    public void processQueue(ServerWorld world) {

        if (!barriersRemoved) {
            return;
        }

        if (pendingChunks.isEmpty()) {
            return;
        }

        ChunkTask task = pendingChunks.poll();

        long key = task.key();

        try {
            processChunk(world, task.chunkX, task.chunkZ);
        } finally {
            queuedChunks.remove(key);
        }
    }

    /**
     * Processes one chunk in 8x8 pieces.
     */
    private void processChunk(
            ServerWorld world,
            int chunkX,
            int chunkZ
    ) {

        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        int bottomY = world.getBottomY();
        int topY = world.getTopYInclusive();

        BlockPos.Mutable pos = new BlockPos.Mutable();

        // 4 separate 8x8 areas.
        for (int sectionX = 0; sectionX < 2; sectionX++) {
            for (int sectionZ = 0; sectionZ < 2; sectionZ++) {

                int startSectionX =
                        startX + sectionX * 8;

                int startSectionZ =
                        startZ + sectionZ * 8;

                for (int x = startSectionX;
                     x < startSectionX + 8;
                     x++) {

                    for (int z = startSectionZ;
                         z < startSectionZ + 8;
                         z++) {

                        for (int y = bottomY;
                             y <= topY;
                             y++) {

                            pos.set(x, y, z);

                            if (world.getBlockState(pos)
                                    .isOf(Blocks.BARRIER)) {

                                world.setBlockState(
                                        pos,
                                        Blocks.AIR.getDefaultState(),
                                        3
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Small record representing a chunk waiting to be processed.
     */
    private record ChunkTask(
            int chunkX,
            int chunkZ
    ) {

        long key() {
            return makeKey(chunkX, chunkZ);
        }

        static long makeKey(
                int chunkX,
                int chunkZ
        ) {
            return ((long) chunkX << 32)
                    ^ (chunkZ & 0xffffffffL);
        }
    }
}