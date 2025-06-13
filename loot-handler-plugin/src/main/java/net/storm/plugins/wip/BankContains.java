package net.storm.plugins.wip;

import java.util.Map;
import java.util.function.BiPredicate;

import net.storm.api.domain.items.IItem;
import net.storm.sdk.items.Bank;

public class BankContains implements BiPredicate<IItem, Map<String, Integer>> {

	@Override
	public boolean test(IItem item, Map<String, Integer> itemsMap) {
		return (!item.isPlaceholder() && itemsMap.keySet().contains(item.getName().toLowerCase()) 
    			
    			// true - if bank contains more than retain value. 
    			&& (Integer.compare(Bank.getFirst(item.getName()).getQuantity(), itemsMap.get(item.getName().toLowerCase())) > 0));
	}
	
}
