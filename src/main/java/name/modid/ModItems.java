package name.modid;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;

public class ModItems {

    public static final RegistryKey<Item> COSTCO_MEMBERSHIP_CARD_KEY =
            RegistryKey.of(
                    RegistryKeys.ITEM,
                    CostcoMembershipCard.id("costco_membership_card")
            );

    public static final Item COSTCO_MEMBERSHIP_CARD = Registry.register(
            Registries.ITEM,
            COSTCO_MEMBERSHIP_CARD_KEY,
            new Item(new Item.Settings().registryKey(COSTCO_MEMBERSHIP_CARD_KEY).maxCount(1))
    );

    public static final RegistryKey<Item> COSTCO_EMPLOYEE_SPAWN_EGG_KEY =
            RegistryKey.of(
                    RegistryKeys.ITEM,
                    CostcoMembershipCard.id("costco_employee_spawn_egg")
            );

    public static final Item COSTCO_EMPLOYEE_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            COSTCO_EMPLOYEE_SPAWN_EGG_KEY,
            new SpawnEggItem(
                    ModEntities.COSTCO_EMPLOYEE,
                    new Item.Settings()
                            .registryKey(COSTCO_EMPLOYEE_SPAWN_EGG_KEY)
                            .maxCount(64)
            )
    );

    public static void registerModItems() {
        CostcoMembershipCard.LOGGER.info("Registering Costco Membership Card");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(COSTCO_MEMBERSHIP_CARD);
            entries.add(COSTCO_EMPLOYEE_SPAWN_EGG);
        });
    }
}