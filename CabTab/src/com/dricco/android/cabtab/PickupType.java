package com.dricco.android.cabtab;

import java.text.NumberFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class PickupType extends Activity{
	
    int no_of_adults = 0;
    int no_of_children = 0;
    double booking = 0.00;
    double initial_charge = 0.00;
    double extra_charges = 0.00;
   
    public enum Rate {
	     STANDARD, PREMIUM, EXTRA_PREMIUM
	 }
   
    Rate rate;
   
    public void updateCharges() {
	    final TextView extraCharges = (TextView)findViewById(R.id.extra_charges_value);
	   
	    extra_charges = booking;
	   
	    if(no_of_adults > 1) {
		    extra_charges = extra_charges + ((no_of_adults -1 ) * 1.00);       
	    }
	   
	   
	    if(no_of_children > 6) {
	 	   extra_charges = extra_charges + 3.00;
	    } else if(no_of_children > 4) {
	 	   extra_charges = extra_charges + 2.00;
	    } else if(no_of_children > 1) {
		    extra_charges = extra_charges + 1.00;
	    }
	   
	    extraCharges.setText(NumberFormat.getCurrencyInstance().format((extra_charges)));
    } 
   
    private void createGpsDisabledAlert(){
	   AlertDialog.Builder builder = new AlertDialog.Builder(this);
	   builder.setMessage("GPS is required for calculating the fair! Would you like to enable it?")
	        .setCancelable(false)
	        .setPositiveButton("Enable GPS",
	             new DialogInterface.OnClickListener(){
	             public void onClick(DialogInterface dialog, int id){
	                  showGpsOptions();
	             }
	        });
	        builder.setNegativeButton("Do nothing",
	             new DialogInterface.OnClickListener(){
	             public void onClick(DialogInterface dialog, int id){
	                  dialog.cancel();
	             }
	        });
	   AlertDialog alert = builder.create();
	   alert.show();
    }
    
    public void displaybookhelp(View v) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
  	   builder.setMessage("When a taxi is booked by telephone, email, fax, text, " +
  	   	   "(orany method other than by hailing in the street " +
           "or engaging at a taxi rank or transport terminal), a booking fee may apply. " +
           "The purpose of this charge is to incentivise drivers to provide such a service, " +
           "usually through a dispatch operator. " +
           "It also compensates and incentivises drivers for travelling " +
           "the distance necessary to collect the passenger. " +
           "A booking charge may not be charged when engaging a taxi at a taxi rank, " +
           "including airport or other transport terminal ranks, or if you hail a taxi in the street.")
  	        .setCancelable(false)
  	        .setPositiveButton("Close",
  	             new DialogInterface.OnClickListener(){
  	             public void onClick(DialogInterface dialog, int id){
  	            	dialog.cancel();
  	             }
  	        });
  	   AlertDialog alert = builder.create();
  	   alert.show();
    }

    
    private void showGpsOptions(){
		Intent gpsOptionsIntent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(gpsOptionsIntent);
	}
   
   
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
     
      LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
      
      if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
          createGpsDisabledAlert();
      }
      
      //Set rate based on time
      final Calendar calendar = Calendar.getInstance();
      
      // get the day
      int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);
      
	  // get the current hour
      int current_hour = calendar.get(Calendar.HOUR_OF_DAY);
      
      final TextView rateType = (TextView)findViewById(R.id.rate_type_value);
      
      if (day_of_week == 1 || ((current_hour > 20) || (current_hour < 8))) {
    	  // We are in the premium rate
    	  rate = Rate.PREMIUM;
    	  initial_charge = 4.45;
    	  rateType.setText("Premium");
      } else {
    	// We are in the standard rate
    	  rate = Rate.STANDARD;
    	  initial_charge = 4.15;
    	  rateType.setText("Standard");
      }
      
      final TextView initalCharge = (TextView)findViewById(R.id.initial_charge_value);
      initalCharge.setText(NumberFormat.getCurrencyInstance().format((initial_charge)));
      
      RadioButton radioBookedNo = (RadioButton)findViewById(R.id.pickup_no);
      radioBookedNo.setOnClickListener(new OnClickListener() {

          public void onClick(View v) {
              booking = 0.00;
              updateCharges();
          }
      });
      
      RadioButton radioBookedYes = (RadioButton)findViewById(R.id.pickup_yes);
      radioBookedYes.setOnClickListener(new OnClickListener() {

          public void onClick(View v) {
              booking = 2.00;
              updateCharges();
          }
      });
           
      
      
      //Set string for persons seek bar
      SeekBar seekBarPerson = (SeekBar)findViewById(R.id.no_of_persons_seekbar);
      seekBarPerson.setProgress(1);
      
      final TextView seekBarValuePerson = (TextView)findViewById(R.id.no_of_persons_seekbar_seekbarvalue);
     
      seekBarPerson.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
              // TODO Auto-generated method stub
              seekBarValuePerson.setText(String.valueOf(progress));
              no_of_adults = progress;
              updateCharges();
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
              // TODO Auto-generated method stub
          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
              // TODO Auto-generated method stub
          }
      }); 
      
      //Set string for children seek bar
      SeekBar seekBarChildren = (SeekBar)findViewById(R.id.no_of_children_seekbar);
      final TextView seekBarValueChildren = (TextView)findViewById(R.id.no_of_children_seekbar_seekbarvalue);
     
      seekBarChildren.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
              // TODO Auto-generated method stub
              seekBarValueChildren.setText(String.valueOf(progress));
              no_of_children = progress;
              updateCharges();
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
              // TODO Auto-generated method stub
          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
              // TODO Auto-generated method stub
          }
      }); 
      
      // Handle button click
      final Button start_meter_button = (Button) findViewById(R.id.start_meter_button);
      start_meter_button.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
                  // Perform action on click
    	      Intent intent = new Intent(getApplicationContext(), MapAndMetering.class);
    	      Bundle b = new Bundle();
    	      b.putDouble("INITIAL_CHARGE", initial_charge);
    	      b.putDouble("EXTRA_CHARGE", extra_charges);
    	      b.putString("Rate", rate.toString());
    	      intent.putExtras(b);
    	      startActivity(intent);
          }
      });
   }
}
