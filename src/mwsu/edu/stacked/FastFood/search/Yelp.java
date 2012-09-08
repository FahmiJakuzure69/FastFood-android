package mwsu.edu.stacked.FastFood.search;

import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

public class Yelp {
	private static final String API_URL = "http://api.yelp.com/v2/search";

	// These are secret... (api keys used to make yelp queries)
	private static final String CONSUMER_KEY = "yemXbFv_WSBWmMDm4CtANA";
	private static final String CONSUMER_SECRET = "Seu4hs16FeXDaBe7ttQRrLyY8Lg";
	private static final String TOKEN = "E4YfnwNSAF_JFpGEGRFCm_M2bb6Olazh";
	private static final String TOKEN_SECRET = "ysDU00p0p9XIyIy762VqaZ99tKw";
	
	// extra api key for further testing
//	private static final String consumerKey = "UhKjK92sJNyitgvoXnEisQ";
//	private static final String consumerSecret = "BpDN0kqU74jCdJALLMajJmfsGu0";
//	private static final String token = "Y2Wqafl8ORMC5BnKFmdNeG-6fnICW6E8";
//	private static final String tokenSecret = "9SAIThkpznhD_hgjt-jK9J2J-vc";
	
	OAuthService service;
	Token accessToken;
	
	private JSONObject placesObject;
	private ArrayList <Restaurant> places;
	private Random random = new Random();
	private JSONObject err_msg;
	

	public ArrayList <Restaurant> getPlaces() {
		return this.places;
	}
	
	public Yelp() {
		this.service = new ServiceBuilder().provider(YelpApiV2.class).apiKey(CONSUMER_KEY).apiSecret(CONSUMER_SECRET).build();
    	this.accessToken = new Token(TOKEN, TOKEN_SECRET);
    	
    	places = new ArrayList<Restaurant>();
	}
	
	
	/**
	 * Create a new yelp query that searches by lat lon coordinates, good
	 * for using GPS locations.
	 * 
	 * @param category - comma delimited string containing pertinant categories
	 * @param latitude 
	 * @param longitude
	 * @param radius - radius to filter results by
	 * @return - true if a connection to yelp was established, false otherwise
	 */
	public boolean searchByLatLon(String term, double latitude, double longitude, float radius, String types) {
		
		// Indicate the url we're sending an oauth request to
		OAuthRequest request = new OAuthRequest(Verb.GET, API_URL);
		
		// Add the parameters for the search
		if(types != null)
			request.addQuerystringParameter("category_filter", types);
		request.addQuerystringParameter("ll", latitude + "," + longitude);
		request.addQuerystringParameter("radius_filter", Float.toString(radius));
		request.addQuerystringParameter("term", term);
		
		// Retreieve the response
		this.service.signRequest(this.accessToken, request);
		Response response = request.send();
		
		String searchBody = response.getBody();
		int responseCode = response.getCode();
		
		// If the error code is 404, a connection could not be established
		if(responseCode != 404) {
			try {
				// Place results in a json array and parse them
				placesObject = new JSONObject(searchBody);
				parsePlaces();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Create a new yelp query that searches by city name
	 * 
	 * @param location name of the city to search
	 * @param types a string containing one or many types delimited 
	 * by commas
	 * @return returns true if query is successful, returns false when
	 * a connection cannot be made to yelp.
	 */
	public boolean searchByCity(String term, String location, String types) {
		
		// Indicate the url we're sending an oauth request to
		OAuthRequest request = new OAuthRequest(Verb.GET, API_URL);
		
		// Add the parameters for the search
		if(types != null)
			request.addQuerystringParameter("category_filter", types);
		request.addQuerystringParameter("location", location);
		request.addQuerystringParameter("term", term);
		
		// Retrieve the response
		this.service.signRequest(this.accessToken, request);
		Response response = request.send();
		
		String searchBody = response.getBody();
		int responseCode = response.getCode();
		
		// If the error code is 404, a connection could not be established
		if(responseCode != 404) {
			try {
				// Place results in a json array and parse them
				placesObject = new JSONObject(searchBody);
				parsePlaces();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return true;
		}
		else
			return false;
	}

	private void parsePlaces() throws JSONException {		
		if(placesObject.has("error")){
			err_msg = placesObject.getJSONObject("error");
		}
		
		if(placesObject.has("businesses")) {
			JSONArray results = placesObject.getJSONArray("businesses");
	
			// Iterate through each result
			for(int i = 0; i < results.length(); i++){
				JSONObject placeObject = results.getJSONObject(i);
				Restaurant place = new Restaurant();
	
				// Get the name
				place.name = placeObject.getString("name");
	
				// Get the location, which is another json object
				JSONObject location = placeObject.getJSONObject("location");
				
				// Get the vicinity
				place.vicinity = location.getJSONArray("address").getString(0) + "," + location.getString("city");
				
				// Get the lat/lon coordinates
				place.location[0] = location.getJSONObject("coordinate").getDouble("latitude");
				place.location[1] = location.getJSONObject("coordinate").getDouble("longitude");
				
				// Get the average rating
				place.avg_rating = (float) placeObject.getDouble("rating");
	
				boolean shouldAdd = true;
				
				// Attempt to make sure this isn't a duplicate entry, by checking the vicinity and name
				for(Restaurant restaurant : places) {
					if(restaurant.name.equals(place.name) || restaurant.vicinity.equals(place.vicinity))
						shouldAdd = false;
				}
				
				if(shouldAdd)
					places.add(place);
			}
		}
	}

	/**
	 * Returns human readable results from a yelp query.
	 * good for debugging purposes
	 */
	public void PrintPlaces() {
		for(Restaurant place: places){
			System.out.println(place.name);	// Restaurant name
			System.out.println(place.vicinity);	// City and address
			
			// Lat/lon coordinates
			System.out.println("\n" + place.location[0] + "," + place.location[1]);

			System.out.println();
		}
	}
	
	/**
	 * Returns a yelp 1.0 message code.  Since this object was written using
	 * yelp 2.0, it attempts to convert the yelp 2.0 error message to it's
	 * old message code.
	 * @return
	 */
	public int getMessageCode() {
		String msg = "";
		String id = "";
		if(err_msg != null){
			try {
				msg = err_msg.getString("field");
				id = err_msg.getString("id");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if(msg.equals("location"))
				return 200;
			if(id.equals("INTERNAL_ERROR"))
				return 4;
		}
		return 1000;
	}
	
	/**
	 * Return the yelp error message for a failed query.
	 * @return The yelp error message, returns null if there was
	 * no error
	 */
	public String getLastError() {
		String msg = null;
		if(err_msg != null) {
			try {
				msg = err_msg.getString("text");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return msg;
		
	}

	public Restaurant getPlaceAt(int i) {
		return places.get(i);
	}

	/**
	 * Returns n number of random places found in the yelp query
	 * @param n
	 * @return
	 */
	public ArrayList<Restaurant> getNPlacesAtRandom(int n) {
		// Can't return more places than we have
		if(n > getNumLocations())
			return null;
		else {
			ArrayList<Restaurant> placeMitten = new ArrayList<Restaurant>();
			int randomIndex;

			for(int i = 0; i < n; i++) {
				randomIndex = random.nextInt(getNumLocations());
				placeMitten.add(places.get(randomIndex));
				places.remove(randomIndex);
			}
			
			return placeMitten;
		}
	}

	public int getNumLocations() {
		return places.size();
	}

}

