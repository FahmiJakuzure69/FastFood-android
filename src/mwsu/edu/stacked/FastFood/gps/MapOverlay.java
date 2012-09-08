package mwsu.edu.stacked.FastFood.gps;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * Stores extra items that can be displayed on a map
 * 
 * @author Opal
 */
public class MapOverlay extends ItemizedOverlay <OverlayItem>  {
	private ArrayList <OverlayItem> itemList = new ArrayList<OverlayItem>();
	
	@Override
	protected OverlayItem createItem (int i) 
	{
		return itemList.get(i);
	}
	
	@Override
	public int size () 
	{
		return itemList.size();
	}
	
	// Create this overlay using a single standard marker
	public  MapOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	/**
	 * Adds an item to this overlay
	 * 
	 * @param p - lat/lon coordinate of this item
	 * @param title - name that gets displayed under the item
	 * @param snippet - a snipped about this item
	 */
	public void addItem(GeoPoint p, String title, String snippet) {
		OverlayItem newItem = new OverlayItem(p, title, snippet);
		itemList.add(newItem);
		populate();
	}

	@Override
	public void draw (Canvas canvas, MapView mapView, boolean shadow) 
	{
		super.draw(canvas, mapView, shadow);

		// Draw each item using the standard marker
		for(OverlayItem item : itemList){
			GeoPoint point = item.getPoint();
			Point ptScreenCoord = new Point();
			mapView.getProjection().toPixels(point, ptScreenCoord);

			// Show the title of this overlay item under the marker
			Paint paint = new Paint();
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setTextSize(12);
			paint.setARGB(150, 0, 0, 0);

			canvas.drawText(item.getTitle(), ptScreenCoord.x, ptScreenCoord.y + 12, paint);
		}
	}
}
