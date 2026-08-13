package name.modid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.chunk.WorldChunk;

public class CostcoWorldState extends PersistentState {

    public boolean barriersRemoved = false;

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

        removeBarriersFromChunk(
                world,
                employeePos.getX() >> 4,
                employeePos.getZ() >> 4
        );
    }

    public void checkChunk(ServerWorld world, WorldChunk chunk) {
        if (!barriersRemoved) {
            return;
        }

        removeBarriersFromChunk(
                world,
                chunk.getPos().x,
                chunk.getPos().z
        );
    }

    private void removeBarriersFromChunk(
            ServerWorld world,
            int chunkX,
            int chunkZ
    ) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                for (int y = world.getBottomY(); y <= world.getTopYInclusive(); y++) {

                    pos.set(x, y, z);

                    if (world.getBlockState(pos).isOf(Blocks.BARRIER)) {
                        world.setBlockState(
                                pos,
                                Blocks.AIR.getDefaultState()
                        );
                    }
                }
            }
        }
    }
}