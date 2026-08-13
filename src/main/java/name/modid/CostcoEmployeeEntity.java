package name.modid;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;

public class CostcoEmployeeEntity extends VillagerEntity {

    public CostcoEmployeeEntity(
            EntityType<? extends VillagerEntity> entityType,
            World world
    ) {
        super(entityType, world, VillagerType.PLAINS);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.getItem() == ModItems.COSTCO_MEMBERSHIP_CARD) {

            if (!this.getWorld().isClient) {

                // Consume the membership card
                stack.decrement(1);

                ServerWorld serverWorld = (ServerWorld) this.getWorld();

                // Get the persistent Costco world state
                CostcoWorldState state =
                        CostcoWorldStateManager.get(serverWorld);

                // Activate barrier removal AND remove barriers
                // in the employee's current chunk.
                state.activateBarrierRemoval(
                        serverWorld,
                        this.getBlockPos()
                );

                // Tell the player what happened
                player.sendMessage(
                        Text.literal(
                                "Costco membership accepted! Barriers removed!"
                        ),
                        false
                );
            }

            return ActionResult.SUCCESS;
        }

        return super.interactMob(player, hand);
    }
}