package com.dricco.android.cabtab;

import java.util.Timer;
import java.util.TimerTask;

public class Pulses {
	
	Timer timer;
	private int time_pulse_counter = 0;
	private int distance_pulse_counter = 0;
	
	public int getTime_pulse_counter() {
		return time_pulse_counter;
	}

	public void setTime_pulse_counter(int time_pulse_counter) {
		this.time_pulse_counter = time_pulse_counter;
	}
	
	public int getDistance_pulse_counter() {
		return distance_pulse_counter;
	}

	public void setDistance_pulse_counter(int distance_pulse_counter) {
		this.distance_pulse_counter = distance_pulse_counter;
	}
	
	public void incrementTimePulseCounter() {
		setTime_pulse_counter(getTime_pulse_counter() + 1);
	}
	
	public void incrementDistancePulseCounter() {
	    setDistance_pulse_counter(getDistance_pulse_counter() + 1);
	}
	
	public int calTimePulseInterval(String meters) {
		final int seconds_per_minute = 60;
		double time_pulse_interval = seconds_per_minute / 
				                         Double.parseDouble(meters);
		return (int)(time_pulse_interval*1000);
	}
	
	public void generateTimePulse(String meters) {
		Timer timer = new Timer();
		int interval_milliseconds = calTimePulseInterval(meters);
		timer.schedule(new TimePulse() , interval_milliseconds) ;
	}

    class TimePulse extends TimerTask {
	    public void run() {
	        System.out.println("Generating a time pulse");
	        timer.cancel() ; //Terminate the thread
	    }
	}
	
	public double calDistancePulseInterval(Double tarrif_price) {
		final double unit_charge = 0.20;
		double distance_pulse_interval = tarrif_price / unit_charge;
		return distance_pulse_interval;
	}
}