package net.storm.plugins.wip;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.storm.api.domain.Interactable;
import net.storm.api.domain.items.IItem;
import net.storm.api.movement.WalkOptions;
import net.storm.api.movement.pathfinder.model.BankLocation;
import net.storm.sdk.entities.Players;
import net.storm.sdk.game.Game;
import net.storm.sdk.items.Equipment;
import net.storm.sdk.items.Inventory;
import net.storm.sdk.items.Items;
import net.storm.sdk.movement.Walker;
import net.storm.sdk.widgets.Dialog;

public class Travel {
    
    /**
     * Travels to the Grand Exchange, if not already there.
     * <li>Supports: <code>Ring of Wealth</code>, <code>Amulet of Glory</code></li>
     * @return <b>true</b> - If player is located at the Grand Exchange.
     */
    public static boolean toGrandExchange() {
        
        // Grand Exchange Area (within the walls)
        final WorldArea exchangeZone = new WorldArea(3137, 3467, 53, 50, 0);
        
        // The Grand Exchange Area vicinity (Edgeville & Barb village to Varrock South & East gate)
        final WorldArea vicinityZone = new WorldArea(3067, 3385, 202, 136, 0);
        
        // Local Player world map coordinates
        final WorldPoint playerPos = Players.getLocal().getWorldLocation();
        
        /*
         * IS Located at the Grand Exchange.
         */
        if (exchangeZone.contains(playerPos)) {
            
            return true;
        
        /*
         * Currently travelling to the Grand Exchange.
         */
        } else if (Players.getLocal().isMoving()) {
            
            return exchangeZone.contains(playerPos);
            
        /*
         * IS teleporting.
         */
        } else if (Dialog.isOpen() && Dialog.hasOption("Grand Exchange") || Dialog.hasOption("Edgeville")) {
            
            Dialog.chooseOption("Grand Exchange", "Edgeville");
            return false;
            
        /*
         *  IS NOT currently teleporting.
         */
        } else if (Players.getLocal().isIdle()) {
                
            // First item contained in Equipment/Inventory, or null if none are found.
            final BiPredicate<IItem, String[]>existingItem = (item, itemsArr) -> 
                    List.of(itemsArr).contains(item.getName()) && (Equipment.contains(item.getId()) || Inventory.contains(item.getId()));
            
            // If Grand Exchange does not contain player.
            if (!exchangeZone.contains(playerPos)) {
                
                final BooleanSupplier walk = () -> Walker.walkTo(BankLocation.GRAND_EXCHANGE_BANK.getArea(), WalkOptions.builder().build());
                
                final Interactable row = Items.getFirst(item -> existingItem.test(item, ChargesItems.ROW.get()));
                final String rowAction = "Grand Exchange";
                
                if (row != null && Game.getWildyLevel() <= 30) {
                    
                    row.interact(row.hasAction(rowAction) ? rowAction : "Rub");
                    
                // If Edgeville to Varrock does not contain player.
                } else if (!vicinityZone.contains(playerPos)) {
                    
                    final Interactable glory = Items.getFirst(item -> existingItem.test(item, ChargesItems.GLORY.get()));
                    final String gloryAction = "Edgeville";
                    
                    if (glory != null && Game.getWildyLevel() <= 30) {
                        
                        glory.interact(glory.hasAction(gloryAction) ? gloryAction : "Rub");
                        
                    } else  walk.getAsBoolean(); // If player is not in GE vicinity & has no glory.
                } else      walk.getAsBoolean(); // If player is in GE vicinity.
                
            }
            
            return false;
            
        } else return false;
    }

}
