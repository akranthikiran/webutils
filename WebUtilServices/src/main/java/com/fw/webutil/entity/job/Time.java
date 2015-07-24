package com.fw.webutil.entity.job;

public class Time
{
	private int hour;
	private int minute;
	private boolean am;
	
	public Time()
	{}

	public Time(int hour, int minute, boolean am)
	{
		this.hour = hour;
		this.minute = minute;
		this.am = am;
	}

	public int getHour()
	{
		return hour;
	}

	public void setHour(int hour)
	{
		this.hour = hour;
	}

	public int getMinute()
	{
		return minute;
	}

	public void setMinute(int minute)
	{
		this.minute = minute;
	}

	public boolean isAm()
	{
		return am;
	}

	public void setAm(boolean am)
	{
		this.am = am;
	}

}
