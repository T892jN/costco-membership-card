package name.modid;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class ModEntities {

    public static final RegistryKey<EntityType<?>> COSTCO_EMPLOYEE_KEY =
            RegistryKey.of(
                    RegistryKeys.ENTITY_TYPE,
                    CostcoMembershipCard.id("costco_employee")
            );

    public static final EntityType<CostcoEmployeeEntity> COSTCO_EMPLOYEE =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    COSTCO_EMPLOYEE_KEY,
                    EntityType.Builder.create(
                                    CostcoEmployeeEntity::new,
                                    SpawnGroup.CREATURE
                            )
                            .dimensions(0.6f, 1.95f)
                            .build(COSTCO_EMPLOYEE_KEY)
            );

    public static void registerModEntities() {
        CostcoMembershipCard.LOGGER.info("Registering Costco Employee");
    }
}