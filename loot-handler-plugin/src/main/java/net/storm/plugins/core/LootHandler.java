package net.storm.plugins.core;

import com.google.inject.Inject;
import com.google.inject.Provides;

import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.storm.api.Static;
import net.storm.api.domain.items.IBankItem;
import net.storm.api.events.ConfigButtonClicked;
import net.storm.api.movement.pathfinder.model.BankLocation;
import net.storm.api.plugins.Plugin;
import net.storm.api.plugins.PluginDescriptor;
import net.storm.api.plugins.PluginManager;
import net.storm.api.plugins.config.*;
import net.storm.api.plugins.exception.PluginInstantiationException;
import net.storm.plugins.components.PresetsDialog;
import net.storm.plugins.utils.Scraper;
import net.storm.plugins.wip.BankContains;
import net.storm.plugins.wip.Travel;
import net.storm.sdk.items.Bank;
import net.storm.sdk.items.GrandExchange;
import net.storm.sdk.items.Inventory;
import net.storm.sdk.widgets.Widgets;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.pf4j.Extension;

/**
 * 
 */
@PluginDescriptor(name = "Loot Handler", tags = { "zirnek", "loot", "sell", "seller", "grand exchange" })
@Extension
public final class LootHandler extends XPlugin {

	// Controls background threads. To stop the client from freezing during heavy
	// tasks for user experience.
	private ExecutorService executor;
	private PresetsDialog dialog;
	// Stores a Set of itemNames that are tradeable on the Grand Exchange.
	private Set<String> geTradeableItems;
	private Set<String> geTradeableLowercase;
	private String preset;

	/*
	 * 
	 */

	@Inject
	private ConfigManager configManager;

	@Inject
	private PluginManager pluginManager;

	@Subscribe
	public void onConfigButtonClicked(ConfigButtonClicked event) {
		if (event.getGroup().equals(LhConfig.GROUP)) {

			if (event.getKey().equals("fetchButton")) {

				configManager.setConfiguration(event.getGroup(), "npcDrops", "...");
				config.npcDrops(); // forces to reload the textField <- & ^
				configManager.setConfiguration(event.getGroup(), "npcDrops", "Loading. Please wait...");

				executor.submit(() -> {
					
					// Fetch NPC's drops
					config.npcDrops();
					configManager.setConfiguration(event.getGroup(), "npcDrops",
							Scraper.scrapeDrops(config.npcName().strip(), geTradeableItems));

				});

			} else if (event.getKey().equals("manageButton")) {

				dialog = new PresetsDialog(getRuneliteFrame(), () -> config.itemNames());
				EventQueue.invokeLater(() -> {

					// Open JDialog
					dialog.setVisible(true);

					final String name = dialog.getSelectedFileName();
					final String items = PresetsDialog.loadIniValue(name, "itemNames");

					log("Selected: " + name);
					log("itemNames: " + items);

					if (items != null) {
						executor.submit(() -> {
							log("items != null");
							config.itemNames(); // forces to reload the textField
							configManager.setConfiguration(event.getGroup(), "itemNames", items);
						});
					}

				});

			}

		}
	}

	@Override
	public void startUp() throws Exception {
		executor = Executors.newSingleThreadExecutor();
		
		// Assign config values from the script arguments, if any
		loadScriptArgs();
		
		// Populate the tradeable items Set<String> (these item names are tradeable on  the GE)
		if (geTradeableItems == null) {
			executor.submit(() -> {
				notify("Fetching tradeable items data...");
				geTradeableItems = Scraper.fetchGeTradeable();
				geTradeableLowercase = geTradeableItems.stream().map(x -> x.toLowerCase().strip())
						.collect(Collectors.toSet());
				notify("Finished fetching items data. Size: " + geTradeableItems.size());
			});
		}

		final Optional<Plugin> stormBankPin = pluginManager.getPlugins().stream()
				.filter(plugin -> plugin.getName().equalsIgnoreCase("storm bankpin")).findFirst();

		// Turn on Storm Bankpin for access to bank and the Grand Exchange.
		if (stormBankPin.isPresent()) {
			final Plugin bankPin = stormBankPin.get();

			if (!pluginManager.isPluginEnabled(bankPin)) {
				notify("Enabling plugin: " + bankPin.getName());
				pluginManager.setPluginEnabled(bankPin, true);
				pluginManager.startPlugin(bankPin);
			}
		} else notify("Storm Bankpin couldn't be turned on");
	}

	@Override
	public void shutDown() throws Exception {
		executor.shutdownNow();

		configManager.setConfiguration(LhConfig.GROUP, "scriptEnabled", false);
		configManager.setConfiguration(LhConfig.GROUP, "npcName", "");
		configManager.setConfiguration(LhConfig.GROUP, "npcDrops", "");

		if (dialog != null)
			dialog.dispose();
	}

	@Subscribe
	public void onGameTick(GameTick game) {

		if (!config.scriptEnabled())
			return;

		if (geTradeableItems == null) {
			notify("Waiting on GE tradeable items list to load before proceeding...");
			return;
		}

		// May contain untradeable items (raw user input)
		// This will filter out the untradeable items from the config.
		final String[] rawItemsArray = config.itemNames().split(",");

		final Map<String, Integer> rawItemsMap = Arrays.stream(rawItemsArray)
				.map(s -> s.replaceAll("(?<=\\S)\\s*=\\s*(?=\\S)", "=")) // remove leading and trailing white space for: '=' splitter.
				.map(String::toLowerCase).map(String::strip)
				.collect(Collectors.toMap(s -> s.contains("=") ? s.substring(0, s.indexOf('=')) : s,
						s -> s.contains("=")
								? Integer.parseInt(s.substring(s.indexOf('=') + 1).replaceAll("[^0-9]", ""))
								: -1,
						(key, duplicate) -> duplicate // replace key with latter entry value (if present)
				));

		// An array of items from the config data that are tradeable on the Grand
		// Exchange
		final Map<String, Integer> itemsMap = rawItemsMap.entrySet().stream()
				.filter(x -> geTradeableLowercase.contains(x.getKey()))
				.collect(Collectors.toMap(x -> x.getKey(), y -> y.getValue(), (x, y) -> y));

		/*
		 * Confirm the bank can be accessed before doing anything.
		 */
		if (atStage(0, Bank.isPinScreenOpen())) {

			notify("Waiting for bank PIN to be entered.", true);

		/*
		 * If the bank is open, withdraw the items to sell.
		 */
		} else if (atStage(1, Bank.isOpen() && bankContainsItems(itemsMap) && Inventory.getFreeSlots() > 0)) {

			// If there's an item present that isn't on the to sell list -> deposit
			// inventory.
			if (atStage(1.1, Inventory.contains(x -> !itemsMap.keySet().contains(x.getName().toLowerCase())) && Bank.getFreeSlots() > 0)) {
				
				notify("Free bank slots: " + Bank.getFreeSlots(), true);
				Bank.depositInventory();

			} else if (atStage(1.2, !Bank.isMainTabOpen())) {
				
				Bank.openMainTab();
				
			} else if (atStage(1.3, Bank.isNotedWithdrawMode())) {

				final IBankItem item = bankContainedItem(itemsMap);

				if (item != null) {

					final int minAmount = itemsMap.get(item.getName().toLowerCase());
					final int count = minAmount == -1 ? item.getQuantity() : item.getQuantity() - minAmount;
					notify(item.getName() + ": minAmount=" + minAmount + ", count=" + count, true);
					
					Bank.withdraw(item.getName(), count);
				}

			} else Bank.setWithdrawMode(true);

		/*
		 * User doesn't own any items that are on the to sell list. Stop the script.
		*/
		} else if (atStage(2, Bank.isOpen() && !bankContainsItems(itemsMap) && !inventoryContainsItems(itemsMap))) {

			// Terminate the script.
			if (atStage(2.1, Inventory.isEmpty() || Bank.getFreeSlots() <= 0)) {

				try {
					pluginManager.setPluginEnabled(this, false);
					if (pluginManager.stopPlugin(this))
						notify(getName() + " has been terminated!");

				} catch (PluginInstantiationException e) {
					e.printStackTrace();
				}

			// Deposit Inventory.
			} else Bank.depositInventory();

		/*
		* If user has items to sell in the inventory, open the Grand Exchange.
		*/
		} else if (atStage(3, inventoryContainsItems(itemsMap) && !GrandExchange.isOpen())) {

			GrandExchange.open();

		/*
		* Handle the Grand Exchange trading.
		*/
		} else if (atStage(4, GrandExchange.isOpen() && (inventoryContainsItems(itemsMap) || !GrandExchange.isEmpty()))) {

			/*
			 * Collect sold items IF:
			 * 
			 * Offer slots are full / User has nothing left to sell.
			 */
			if (atStage(4.1, (GrandExchange.isFull() || !inventoryContainsItems(itemsMap))

					// All offers are sold (not in the process of selling)
					&& atStage(4.2, GrandExchange.getOffers().stream()
							.allMatch(x -> x.getState().equals(GrandExchangeOfferState.SOLD)))

					//
					&& atStage(4.3, GrandExchange.canCollect())

					// User has free inventory space for coins to collect.
					&& atStage(4.4, (Inventory.getFreeSlots() > 0 || Inventory.contains("Coins"))))) {

				GrandExchange.collect();

			/*
			* Set up offer.
			*/
			} else if (atStage(4.5, inventoryContainsItems(itemsMap))) {

				// If "Sell offer" interface is open, but no item is selected, close GE interface.
				if (atStage(4.6, GrandExchange.isSelling() && GrandExchange.getItemId() == -1)) {

					Widgets.closeInterfaces();

				// If Grand Exchange is open, and there are offer slots available: set up an offer.
				} else if (atStage(4.7, !GrandExchange.isSelling() && !GrandExchange.isFull())) {

					GrandExchange.sell(Inventory.getFirst(x -> itemsMap.keySet().contains(x.getName().toLowerCase())).getName());

				} else {
					
					final String itemName = GrandExchange.getItemName().toLowerCase();

					if (atStage(4.8, GrandExchange.getQuantity() != inventoryCount(itemName))) {

						GrandExchange.setQuantity(inventoryCount(itemName));

					} else if (atStage(4.9, GrandExchange.getPrice() > 1)) {

						GrandExchange.setPrice(1);

					} else GrandExchange.confirm();

				}

			}

		/*
		* If the bank is not open and there's no items to sell in the inventory. Travel
		* to the Grand Exchange bank, open it.
		*/
		} else if (atStage(5, !inventoryContainsItems(itemsMap))) {
			
			// Travel to the Grand Exchange
			if (atStage(5.1, Travel.toGrandExchange()))
				Bank.open(BankLocation.GRAND_EXCHANGE_BANK);
			
		} else notify("Encountered error: Reached the end of the if statement");

	}

	/**
	 * The total amount of an item (Noted & Unnoted) contained in the inventory
	 * 
	 * @param itemName
	 * @return
	 */
	private final int inventoryCount(String itemName) {
		return Inventory.getAll().stream().filter(x -> x.getName().equalsIgnoreCase(itemName))
				.mapToInt(x -> x.getQuantity()).sum();
	}

	/**
	 * 
	 * @param items
	 * @return <b>true</b> - If the <b>Inventory</b> contains any of the matching
	 *         items.
	 */
	private final boolean inventoryContainsItems(Map<String, Integer> itemsMap) {
		return Inventory.contains(x -> itemsMap.keySet().contains(x.getName().toLowerCase()));
	}

	/**
	 * Checks if the bank contains more items than should be retained.
	 * @param itemsMap - key: <code>itemName</code>, value: <code>retainQuantity</code>
	 * @return <b>true</b> - If the <b>Bank</b> contains any of the matching items.
	 */
	private final boolean bankContainsItems(Map<String, Integer> itemsMap) {
		return Bank.contains(item -> new BankContains().test(item, itemsMap));
	}

	/**
	 * 
	 * @param itemsMap
	 * @return First item in the bank that has above retain quantity.
	 */
	private final IBankItem bankContainedItem(Map<String, Integer> itemsMap) {
		return Bank.getFirst(item -> new BankContains().test(item, itemsMap));
	}
	
	/**
	 *  Load preset from script args, if present.
	 */
	private void loadScriptArgs() {
		
		// ensures the arg configs are set only on the first onStart() method call.
		if (preset == null) {
		
			preset = Static.getScriptArgs()[0];
			
			if (preset.length() == 0)
				log("No script arguments found");
			else {
				log("Script arguments: \"" +  preset + "\"");
				
				// Load & Assign the preset's 'items to sell' String
				final String items = PresetsDialog.loadIniValue(preset, "itemNames");
				
				config.itemNames(); // forces to reload the textField
				configManager.setConfiguration(LhConfig.GROUP, "itemNames", items);
				
				// Enable the Script
				configManager.setConfiguration(LhConfig.GROUP, "scriptEnabled", true);
			}
		}
	}

	@Provides
	LhConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(LhConfig.class);
	}

}
