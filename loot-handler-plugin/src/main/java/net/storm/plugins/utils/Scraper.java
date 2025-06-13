package net.storm.plugins.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *	This class handles web data scraping related functions.
 */
public final class Scraper {
	
	public static final String scrapeDrops(String npcName, Set<String>geTradeableItems) {
		try {
				final BiFunction<JsonArray, String, String>findMatch =(json, target) -> {
					final JsonArray titles = json.get(1).getAsJsonArray(); // second element contains the titles
	
			        for (JsonElement element : titles) {
			            final String title = element.getAsString();
			            if (title.equalsIgnoreCase(target))
			                return title;
			        }
			        return null;
				};
				
				/*
				 * Collects first 20 wiki search results for the NPC name entered.
				 * This is necessary to get the correct NPC name spelling, URLs are case sensitive.
				 */
				final URL opensearchUrl = new URL("https://oldschool.runescape.wiki/api.php?action=opensearch&search=" + npcName.replaceAll(" ", "_") + "&format=json&limit=20");
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(opensearchUrl.openStream(), StandardCharsets.UTF_8))) {
					final String jsonString = reader.lines().collect(Collectors.joining("\n"));
					
					final JsonParser jsonParser = new JsonParser();
					final JsonArray json = jsonParser.parse(jsonString).getAsJsonArray();
					
					final String fixedCaseNpcName = findMatch.apply(json, npcName.toLowerCase());
					npcName = fixedCaseNpcName;
		        }

				/*
				 * This is the mentioned case sensitive url, it wouldn't work if misspelled.
				 * The above snippet allows for case insensitive NPC name entries for this script's users :)
				 */
				String result = "";
				final URL queryUrl = new URL("https://oldschool.runescape.wiki/api.php?action=query&prop=revisions&rvprop=content&titles=" + npcName.trim().replaceAll(" ", "_") + "&format=json");
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(queryUrl.openStream(), StandardCharsets.UTF_8))) {
					result = reader.lines().collect(Collectors.joining("\n"));
				}
				
				/*
				 * Confirm if this wiki entry has an item drops section.
				 */
				if (result.contains("DropsLine")) {
				
				    final Pattern pattern = Pattern.compile("\\{\\{?Drops?Line\\s*\\|[^}]*?name\\s*=\\s*([^|}]+)");
				    final Matcher matcher = pattern.matcher(result);

				    final Set<String> items = new HashSet<>();
				    while (matcher.find()) {
				    	
				    	final String itemName = matcher.group(1).trim();
				    	if (geTradeableItems.contains(itemName))
				    		items.add(itemName);
				    }
				    
				    return String.join(", ", items);
				    
				} 	else 	return "Err. Couldn't find any drops.";
		} catch (IOException e) {
								return "Err. Connection to 07 wiki failed.";
		} catch (IllegalStateException i) {
								return "Err. #1 Invalid monster name.";
		} catch (NullPointerException n) {
								return "Err. #2 Invalid monster name.";
		}
	}
	
	/**
	 * 
	 * @return A Set of item name Strings tradeable on the Grand Exchange.
	 */
	public static final Set<String> fetchGeTradeable() {
	    final Set<String> tradeableNames = new HashSet<>();

	    try {
	        final URL url = new URL("https://prices.runescape.wiki/api/v1/osrs/mapping");
	        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.connect();

	        if (conn.getResponseCode() != 200) {
	            System.err.println("Failed to fetch mapping data");
	            return tradeableNames;
	        }

	        final JsonParser jsonParser = new JsonParser();
	        final JsonArray items = jsonParser.parse(new InputStreamReader(conn.getInputStream())).getAsJsonArray();

	        for (JsonElement element : items) {
	            final JsonObject obj = element.getAsJsonObject();
	            
	            if (obj.has("name")) // Format to lowercase, stripped
	                tradeableNames.add(obj.get("name").getAsString()); // .toLowerCase().strip()
	        }
	    } catch (Exception e) {
	        System.err.println("Error loading GE item names: " + e.getMessage());
	    }

	    return tradeableNames;
	}

}
