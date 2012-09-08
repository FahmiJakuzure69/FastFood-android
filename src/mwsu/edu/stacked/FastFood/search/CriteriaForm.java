package mwsu.edu.stacked.FastFood.search;

import java.util.HashMap;

import mwsu.edu.stacked.R;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

/**
 * Handles grabbing criteria from the user to narrow down a yelp query
 * 
 * @author Ian
 *
 */
public class CriteriaForm extends Activity {

	@Override
	public void onCreate(Bundle savedInstance) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstance);
		setContentView(R.layout.food_crit);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		// Set the listener for the types spinner
		Spinner spinner = (Spinner) findViewById(R.id.spinner2);
		spinner.setOnItemSelectedListener(types);
	}
	
	// Handle the return button, which just finishes the activity
	public void clickHandler(View view){
		switch (view.getId()) {
		
		case R.id.button1 :
				finish();
				break;
		}
	}
	
	/**
	 * Returns the data entered in a way so yelp can use it
	 */
	@Override
	public void finish(){
		boolean ischecked = false;
		Intent criteria = new Intent();
		
		// Hashmap for converting readable entries to yelp category codes
		HashMap<String,String> cuisines = new HashMap<String,String>();
		cuisines.put("Any", "any");
		cuisines.put("Afghan","afghani");
		cuisines.put("American (new)","newamerican");
		cuisines.put("American (traditional)","tradamerican");
		cuisines.put("Barbeque","bbq");
		cuisines.put("Brazilian","brazilian");
		cuisines.put("Breakfast & Brunch","breakfast_brunch");
		cuisines.put("Buffets","buffets");
		cuisines.put("Burgers","burgers");
		cuisines.put("Cafes","cafes");
		cuisines.put("Caribbean","caribbean");
		cuisines.put("Chicken Wings","chicken_wings");
		cuisines.put("Chinese","chinese");
		cuisines.put("Delis","delis");
		cuisines.put("Diners","diners");
		cuisines.put("French","french");
		cuisines.put("German","german");
		cuisines.put("Greek","greek");
		cuisines.put("Hawaiian","hawaiian");
		cuisines.put("Indian","indpak");
		cuisines.put("Italian","italian");
		cuisines.put("Japanese","japanese");
		cuisines.put("Korean","korean");
		cuisines.put("Mexican","mexican");
		cuisines.put("Pizza","pizza");
		cuisines.put("Sandwiches","sandwiches");
		cuisines.put("Seafood","seafood");
		cuisines.put("Southern","southern");
		cuisines.put("Sushi Bars","sushi");
		cuisines.put("Thai","thai");
		cuisines.put("Vegetarian","vegetarian");
		
		// Get the values from the spinners and run them through the hashmap
		String cuisine = (String) ((Spinner) findViewById(R.id.spinner1)).getSelectedItem();
		criteria.putExtra("cuisineDisplay", cuisine);
		cuisine = cuisines.get(cuisine);
		criteria.putExtra("cuisine", cuisine);
		
		String type = (String) ((Spinner) findViewById(R.id.spinner2)).getSelectedItem();
		criteria.putExtra("type", type);
		
		// Grab the wildcard
		CheckBox wildcard = (CheckBox)findViewById(R.id.checkBox1);
		if(wildcard.isChecked()){
			ischecked = true;
		}
		
		// Send it back to the calling activity 
		criteria.putExtra("wildcard", ischecked);
		setResult(RESULT_OK, criteria);   
		super.finish();
	}
	
	/**
	 * Listener that changes contents of the cuisine spinner
	 * based on the value in the types spinner
	 */
	OnItemSelectedListener types = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position,
				long arg3) {
			
			String item = (String)parent.getItemAtPosition(position);
			
			Spinner spinner = (Spinner)findViewById(R.id.spinner1);
			
			// Show cuisines pertinant to dine-in
			if(item.equals("Dine In")){
				ArrayAdapter<String> test = new ArrayAdapter<String>(CriteriaForm.this,
						android.R.layout.simple_spinner_item, 
						getResources().getStringArray(R.array.cuisinesDinein));
				test.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(test);
				
				
			}
			
			// Show cuisines that are fast food
			if(item.equals("Fast Food")){
				ArrayAdapter<String> test = new ArrayAdapter<String>(CriteriaForm.this,
						android.R.layout.simple_spinner_item, 
						getResources().getStringArray(R.array.cuisinesFastFood));
				test.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(test);
			}
		}

		// Not interested in this
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
		
	};
	
	
	
	
	
	
}


