package com.kiwihealthcare.bpppgcollector.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

	/**
	 * MM-dd HH:mm
	 */
	public static final String FORMAT_STYLE_2 = "MM-dd HH:mm";
	/**
	 * yyyy-MM-dd
	 */
	public static final String FORMAT_STYLE_3 = "yyyy-MM-dd";
	/**
	 * HH:mm
	 */
	public static final String FORMAT_STYLE_4 = "HH:mm";
	/**
	 * yyyy-MM-dd HH:mm
	 */
	public static final String FORMAT_STYLE_5 = "yyyy-MM-dd HH:mm";
	/**
	 * MM-dd
	 */
	public static final String FORMAT_STYLE_6 = "MM-dd";
	/**
	 * E, dd MMM yyyy hh:mm a zz
	 */
	public static final String FORMAT_STYLE_7 = "E, dd MMM yyyy hh:mm a zz";
	/**
	 * HH
	 */
	public static final String FORMAT_STYLE_8 = "HH";
	/**
	 * yyyy
	 */
	public static final String FORMAT_STYLE_9 = "yyyy";
	/**
	 * yyyy_MM_dd_HH_mm_ss
	 */
	public static final String FORMAT_STYLE_10 = "yyyy_MM_dd_HH_mm_ss";
	
	public static final long _1SECOND = 1000L;
	public static final long _1MINUTE = 60*_1SECOND;
	public static final long _1HOUR = 60*_1MINUTE;
	public static final long _1DAY = 24*_1HOUR;
	public static final long _1WEEK = 7*_1DAY;
	public static final long _1MONTH = 30*_1DAY;
	public static final long _3MONTH = 90*_1DAY;
	public static final long _6MONTH = 180*_1DAY;
	public static final long _1YEAR = 360*_1DAY;
	
	public static final int MONDAY = 1;
	public static final int TUESDAY = 2;
	public static final int WEDNESDAY = 3;
	public static final int THURSDAY = 4;
	public static final int FRIDAY = 5;
	public static final int SATURDAY = 6;
	public static final int SUNDAY = 7;
	
	public static String getStringWithStyle(String style, Date date) {
		SimpleDateFormat format = new SimpleDateFormat(style);
		return format.format(date);
	}
	
	public static String getStringWithStyle(String style, long date) {
		return getStringWithStyle(style, new Date(date));
	}
	
	public static Date getDate(long date) {
		return new Date(date);
	}
	
	public static long getLongWithStyle(String style, Locale locale, String date) {
		SimpleDateFormat format = new SimpleDateFormat(style, locale);
		try {
			Date d = format.parse(date);
			return d.getTime();
		} catch(ParseException e) {
			e.printStackTrace();
		}
		return -1L;
	}
	
	/**
	 * yyyy-MM-dd
	 * 
	 * @param year
	 * @param monthOfYear
	 * @param dayOfMonth
	 * @return
	 */
	public static String getStringWithStyle3(int year, int monthOfYear, int dayOfMonth) {
		StringBuilder date = new StringBuilder();
		String yearS = String.valueOf(year);
		while(yearS.length() < 4) {
			yearS = "0"+yearS;
		}
		date.append(yearS);
		date.append("-");
		String monthS = String.valueOf(monthOfYear+1);
		while(monthS.length() < 2) {
			monthS = "0"+monthS;
		}
		date.append(monthS);
		date.append("-");
		String dayS = String.valueOf(dayOfMonth);
		while(dayS.length() < 2) {
			dayS = "0"+dayS;
		}
		date.append(dayS);
		return date.toString();
	}
	
	/**
	 * HH:mm
	 * 
	 * @param hourOfDay
	 * @param minute
	 * @return
	 */
	public static String getStringWithStyle4(int hourOfDay, int minute) {
		StringBuilder time = new StringBuilder();
		String hourS = String.valueOf(hourOfDay);
		while(hourS.length() < 2) {
			hourS = "0"+hourS;
		}
		time.append(hourS);
		time.append(":");
		String minuteS = String.valueOf(minute);
		while(minuteS.length() < 2) {
			minuteS = "0"+minuteS;
		}
		time.append(minuteS);
		return time.toString();
	}
	
	/**
	 * yyyy-MM-dd HH:mm
	 * 
	 * @param year
	 * @param monthOfYear
	 * @param dayOfMonth
	 * @param hourOfDay
	 * @param minute
	 * @return
	 */
	public static long getLongWithStyle5(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minute) {
		String dateS = getStringWithStyle3(year, monthOfYear, dayOfMonth);
		String timeS = getStringWithStyle4(hourOfDay, minute);
		String value = dateS+" "+timeS;
		SimpleDateFormat format = new SimpleDateFormat(FORMAT_STYLE_5);
		try {
			return format.parse(value).getTime();
		} catch(ParseException e) {
			e.printStackTrace();
		}
		return -1L;
	}
	
	public static long getLongNow() {
		return new Date().getTime();
	}
	
	public static long getLongOfLatestDayOfWeek(int dayOfWeek) {
		switch(dayOfWeek) {
		case MONDAY:
			return getLongOfLatestMondayOfWeek();
		case TUESDAY:
			return getLongOfLatestTuesdayOfWeek();
		case WEDNESDAY:
			return getLongOfLatestWednesdayOfWeek();
		case THURSDAY:
			return getLongOfLatestThursdayOfWeek();
		case FRIDAY:
			return getLongOfLatestFridayOfWeek();
		case SATURDAY:
			return getLongOfLatestSaturdayOfWeek();
		case SUNDAY:
			return getLongOfLatestSundayOfWeek();
		}
		return -1L;
	}
	
	private static long getLongOfLatestMondayOfWeek() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		if(day == Calendar.TUESDAY) {
			calendar.add(Calendar.DAY_OF_MONTH, -1);
		} else if(day == Calendar.WEDNESDAY) {
			calendar.add(Calendar.DAY_OF_MONTH, -2);
		} else if(day == Calendar.THURSDAY) {
			calendar.add(Calendar.DAY_OF_MONTH, -3);
		} else if(day == Calendar.FRIDAY) {
			calendar.add(Calendar.DAY_OF_MONTH, -4);
		} else if(day == Calendar.SATURDAY) {
			calendar.add(Calendar.DAY_OF_MONTH, -5);
		} else if(day == Calendar.SUNDAY) {
			calendar.add(Calendar.DAY_OF_MONTH, -6);
		}
		return calendar.getTimeInMillis();
	}
	
	private static long getLongOfLatestTuesdayOfWeek() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.setFirstDayOfWeek(Calendar.TUESDAY);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
		return calendar.getTimeInMillis();
	}
	
	private static long getLongOfLatestWednesdayOfWeek() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.setFirstDayOfWeek(Calendar.WEDNESDAY);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		return calendar.getTimeInMillis();
	}
	
	private static long getLongOfLatestThursdayOfWeek() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.setFirstDayOfWeek(Calendar.THURSDAY);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
		return calendar.getTimeInMillis();
	}
	
	private static long getLongOfLatestFridayOfWeek() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.setFirstDayOfWeek(Calendar.FRIDAY);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		return calendar.getTimeInMillis();
	}
	
	private static long getLongOfLatestSaturdayOfWeek() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.setFirstDayOfWeek(Calendar.SATURDAY);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		return calendar.getTimeInMillis();
	}
	
	private static long getLongOfLatestSundayOfWeek() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.setFirstDayOfWeek(Calendar.SUNDAY);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		return calendar.getTimeInMillis();
	}

}
