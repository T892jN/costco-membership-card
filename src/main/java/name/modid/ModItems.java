package name.modid;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {

    public static final Item COSTCO_MEMBERSHIP_CARD = Registry.register(
            Registries.ITEM,
            CostcoMembershipCard.id("costco_membership_card"),
            new Item(new Item.Settings().maxCount(1))
    );

    public static void registerModItems() {
        CostcoMembershipCard.LOGGER.info("Registering Costco Membership Card");
    }
}