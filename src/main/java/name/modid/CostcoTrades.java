package name.modid;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;

public class CostcoTrades {

    public static void registerTrades() {

        TradeOfferHelper.registerVillagerOffers(
                VillagerProfession.CARTOGRAPHER,
                1,
                factories -> factories.add((entity, random) ->
                        new TradeOffer(
                                new TradedItem(Items.EMERALD, 30),
                                new ItemStack(ModItems.COSTCO_MEMBERSHIP_CARD),
                                1,
                                5,
                                0.05f
                        )
                )
        );
    }
}