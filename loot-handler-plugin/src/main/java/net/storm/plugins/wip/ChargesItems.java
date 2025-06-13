package net.storm.plugins.wip;

/**
 * A class containing arrays of RS2Items with charges.
 * <li><b>!</b> The arrays should be returned in an ascending charges order. (<b>lowest -> highest</b>)</li>
 */
public enum ChargesItems {
	
	ROW(		new String[] {"Ring of wealth (1)", "Ring of wealth (2)", "Ring of wealth (3)", "Ring of wealth (4)", "Ring of wealth (5)"}), 
	GLORY(	new String[] {"Amulet of glory(1)", "Amulet of glory(2)", "Amulet of glory(3)", "Amulet of glory(4)", "Amulet of glory(5)", "Amulet of glory(6)"});
	
	/**An array containing the names of the specified item with all of its charges variants returned in an ascending order.*/
	private final String[] chargesItem;
    
	ChargesItems(final String[] chargesItem) {
        this.chargesItem = chargesItem;
    }
	
	/**
	 * 
	 * @return {@link #chargesItem}
	 */
	public final String[] get() {
		return chargesItem;
	}
	
	@Override
    public String toString() {
        return String.join(", ", chargesItem);
    }

}
