package com.dricco.android.cabtab;

import java.text.NumberFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.format.Time;
import android.widget.TextView;

import com.dricco.android.cabtab.PickupType.Rate;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapAndMetering extends MapActivity {
	
	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private double mySpeedMeterPerSec;
	
	Location perviousLocation;
	double totalDistance = 0.00;
	boolean firstLocationChange = true;
	int previousTime;
	double inital_charge;
	
	double current_tarif_km;
	double current_tariff_min;
	int total_km;
	int timer_units = 0;
	
    private static final int UPDATE_TIME = 1;
    public StopWatch mStopWatch;
    boolean first_km_or_timer = true;
    boolean timer_won = false;
    String fair_rate = "";
    
	//PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	 //PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "" +
	 //		                                         "My Tag");

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.map); // bind the layout to the activity
		
		//Don't allow phone to go to sleep when in this window
		//wakelock.acquire();
		
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		
		// Start the time
		mStopWatch = new StopWatch();
		mStopWatch.reset();
		mStopWatch.start();
		
		Typeface tf = Typeface.createFromAsset(getAssets(),
        "fonts/digital-7.ttf");
        TextView fareTextView = (TextView) findViewById(R.id.fare_string);
        TextView tariffTextView = (TextView) findViewById(R.id.tariff_string);
        TextView extraTextView = (TextView) findViewById(R.id.extra_string);
        fareTextView.setTypeface(tf);
        tariffTextView.setTypeface(tf);
        extraTextView.setTypeface(tf);
        
        Bundle extras = getIntent().getExtras(); 
        
        if(extras !=null) {
            inital_charge = extras.getDouble("INITIAL_CHARGE");
            double extra_charge = extras.getDouble("EXTRA_CHARGE");
           
            fareTextView.setText(nf.format(inital_charge).toString());
            extraTextView.setText(nf.format(extra_charge).toString());
        } else {
        	fareTextView.setText("0.00");
        	fareTextView.setText("0.00");
        }
        
        fair_rate = extras.getString("Rate");
        
		// create a map view
		mapView = (MapView) findViewById(R.id.mapview);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setStreetView(true);
		mapController = mapView.getController();
		mapController.setZoom(16); // Zoom 1 is world view
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, new GeoUpdateHandler());
		
		Timer displayTimer = new Timer();
        displayTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                mHandler.sendMessage(mHandler.obtainMessage(UPDATE_TIME, mStopWatch.getElapsedTime()));
            }
        }, 100, 100);
	}
	

    private Handler mHandler = new Handler() {
	    @Override public void handleMessage(Message msg) {
	    	long millies = (Long) msg.obj;
	    	int seconds = (int) (millies / 1000);
	    	
	    	if (first_km_or_timer == true) {
	    		if ((seconds >= 170) && (total_km < 1)) {
	    			mStopWatch.reset();
		        	mStopWatch.start();
		        	timer_won = true;
		        	first_km_or_timer = false;
	    		}
	    	} else {
		        if (seconds >= 60) {
		        	timer_units++;
		        	mStopWatch.reset();
		        	mStopWatch.start();
		    		NumberFormat nf = NumberFormat.getInstance();
		    		nf.setMinimumFractionDigits(2);
		    		nf.setMaximumFractionDigits(2);
					TextView fareTextView = (TextView) findViewById(R.id.fare_string);
					
					double charge = updateRate();
					fareTextView.setText(nf.format(charge) + "");
		            return;
		        }
	    	}
	        
	        TextView timerStatusTextView = (TextView) findViewById(R.id.timerstatus);
	        
	        if (mStopWatch.IsRunning()) {
	        	timerStatusTextView.setText("Timer running");
	        } else {
	        	timerStatusTextView.setText("Timer stopped");
	        }

	        switch (msg.what) {
	            case UPDATE_TIME:
	            	TextView timerTextView = (TextView) findViewById(R.id.timer);
	            	timerTextView.setText(seconds + " s");
	        	    break;
	            default:
	                super.handleMessage(msg);
	        }
	    }
    };

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public class GeoUpdateHandler implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMinimumFractionDigits(2);
			nf.setMaximumFractionDigits(2);
			
			int currentTime = Time.SECOND;
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(lat, lng);
			mapController.animateTo(point);
			mapView.invalidate();
			
			// add marker
			MapOverlay mapOverlay = new MapOverlay(MapAndMetering.this);
			mapOverlay.setPointToDraw(point);
			List<Overlay> listOfOverlays = mapView.getOverlays();
			listOfOverlays.clear();
			listOfOverlays.add(mapOverlay);
			if(location.hasSpeed()) {
				mySpeedMeterPerSec = location.getSpeed();
			}
			
			//Covert m/s to km/h
			double kph = (mySpeedMeterPerSec/1000) * (60*60);
			
			if(kph >= 21) {
				// Calculate based on distance
				mStopWatch.stop();
			} else {
				// Calculate based on time
				if(mStopWatch.IsRunning() == false)
				    mStopWatch.start();
			}
			    
			if(firstLocationChange) {
				perviousLocation = location;
				previousTime = currentTime;
				firstLocationChange = false;
			}
			
			float distanceChanged = perviousLocation.distanceTo(location);
			
			totalDistance = totalDistance + distanceChanged;
			
			TextView currentSpeed = (TextView)findViewById(R.id.speed);
			currentSpeed.setText(nf.format(kph) + " km/h");
			
			TextView totalDistanceString = (TextView)findViewById(R.id.distance);
			totalDistanceString.setText(nf.format(totalDistance/1000) + " km");
			
			perviousLocation = location;
			previousTime = currentTime;
			
			//convert distance to km
			total_km = (int) ((totalDistance/1000));
			
			// Take away the initial km and reset the timer is we have travelled a km 
			// but not passed 170 seconds
			if(timer_won == false) {
			    if(total_km >= 1) {
		            total_km -= 1;
		            first_km_or_timer = false;
			    }
			}
			
			TextView tariffTextView = (TextView) findViewById(R.id.tariff_string);
			
			if(total_km > 29) {
				tariffTextView.setText("3");
			} else if (total_km > 14) {
				tariffTextView.setText("2");
			} else {
				tariffTextView.setText("1");
			}
			
			TextView fareTextView = (TextView) findViewById(R.id.fare_string);
			double charge = updateRate();
			fareTextView.setText(nf.format(charge) + "");
			
		}
		

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
	
	public double updateRate() {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		
		double total_fare = inital_charge;
		int km_for_cal = total_km;
		int timer_for_Cal = timer_units;
        
		double tariffA = 0.00;
		double tariffB = 0.00;
		double tariffC = 0.00;
		double tariffA_min = 0.00;
		double tariffB_min = 0.00;
		double tariffC_min = 0.00;
		
		if (fair_rate.equals(Rate.PREMIUM.toString())) {
			tariffA = Double.valueOf(getResources().getString(R.string.prem_rate_tariffA_km));
			tariffB = Double.valueOf(getResources().getString(R.string.prem_rate_tariffB_km));
			tariffC = Double.valueOf(getResources().getString(R.string.prem_rate_tariffC_km));
			tariffA_min = Double.valueOf(getResources().getString(R.string.prem_rate_tariffA_min));
			tariffB_min = Double.valueOf(getResources().getString(R.string.prem_rate_tariffB_min));
			tariffC_min = Double.valueOf(getResources().getString(R.string.prem_rate_tariffC_min));
		} else if(fair_rate.equals(Rate.STANDARD.toString())){
			tariffA = Double.valueOf(getResources().getString(R.string.stand_rate_tariffA_km));
			tariffB = Double.valueOf(getResources().getString(R.string.stand_rate_tariffB_km));
			tariffC = Double.valueOf(getResources().getString(R.string.stand_rate_tariffC_km));
			tariffA_min = Double.valueOf(getResources().getString(R.string.stand_rate_tariffA_min));
			tariffB_min = Double.valueOf(getResources().getString(R.string.stand_rate_tariffB_min));
			tariffC_min = Double.valueOf(getResources().getString(R.string.stand_rate_tariffC_min));
		}
		
		// Calculate the price
        if (km_for_cal > 29) {
        	int extra_kms = km_for_cal - 29;
        	total_fare += (extra_kms * tariffC);
        	km_for_cal -= extra_kms;
        }
        
        if (km_for_cal > 14) {
        	int extra_kms = km_for_cal - 14;
        	total_fare += (extra_kms * tariffB);
        	km_for_cal -= extra_kms;
        }
        
        if (timer_for_Cal > 29) {
        	int extra_timer_units = timer_for_Cal - 29;
        	total_fare += (extra_timer_units * tariffC_min);
        	timer_for_Cal -= extra_timer_units;
        }
        
        if (timer_for_Cal > 14) {
        	int extra_timer_units = timer_for_Cal - 14;
        	total_fare += (extra_timer_units * tariffB_min);
        	timer_for_Cal -= extra_timer_units;
        }
        
        total_fare = total_fare + (km_for_cal * tariffA) + (timer_for_Cal * tariffA_min);		
        
        return total_fare;
	}
}



class MapOverlay extends Overlay
{
  private GeoPoint pointToDraw;
  private MapAndMetering MapAndMetering;
  
public MapOverlay(MapAndMetering mapAndMetering) {
	this.MapAndMetering = mapAndMetering;
}

public void setPointToDraw(GeoPoint point) {
    pointToDraw = point;
  }

  public GeoPoint getPointToDraw() {
    return pointToDraw;
  }
  
  @Override
  public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
    super.draw(canvas, mapView, shadow);           

    // convert point to pixels
    Point screenPts = new Point();
    mapView.getProjection().toPixels(pointToDraw, screenPts);

    // add marker
    Bitmap bmp = BitmapFactory.decodeResource(MapAndMetering.getResources(), R.drawable.maps_icon2);
    canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 24, null);    
    return true;
  }
} 
