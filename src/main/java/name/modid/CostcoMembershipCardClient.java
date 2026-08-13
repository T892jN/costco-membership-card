package name.modid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.VillagerEntityRenderer;

public class CostcoMembershipCardClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(
                ModEntities.COSTCO_EMPLOYEE,
                VillagerEntityRenderer::new
        );
    }
}