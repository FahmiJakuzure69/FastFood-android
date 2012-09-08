package mwsu.edu.stacked.FastFood.search;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stores information about a place returned by Yelp API
 * 	 * @author Zach
 */
public class Restaurant implements Parcelable {
	public String name;
	public String vicinity;
	public double[] location = new double[2];
	public String reference;
	public String id;
	public float avg_rating;
	
	public Restaurant() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	// Parcelables need this to work
	// I assume it's primarily responsible for "flattening" the
	// the object into something the android can pass inside intents
	public static final Parcelable.Creator<Restaurant> CREATOR
	= new Parcelable.Creator<Restaurant>() {
		public Restaurant createFromParcel(Parcel in) {
			return new Restaurant(in);
		}

		public Restaurant[] newArray(int size) {
			return new Restaurant[size];
		}
	};

	// More parcelable functions, pretty straightfoward
	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeString(name);
		out.writeString(vicinity);
		out.writeString(reference);
		out.writeString(id);
		out.writeDoubleArray(location);
		out.writeFloat(avg_rating);
	}

	private Restaurant(Parcel in) {
		name = in.readString();
		vicinity = in.readString();
		reference = in.readString();
		id = in.readString();
		in.readDoubleArray(location);
		avg_rating = in.readFloat();
	}
}
