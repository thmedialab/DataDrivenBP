package com.kiwihealthcare.bpppgcollector.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class CNweatherUtils {
	JSONObject weatherJsonObject = null;
	JSONObject weatherinfo = null;

	public boolean init() {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(
				"http://www.weather.com.cn/data/cityinfo/101010100.html");
		String strjsonString = "";

		try {

			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(entity.getContent()));
				String line = null;
				while ((line = reader.readLine()) != null) {
					strjsonString += line;
				}
			}
			weatherJsonObject = new JSONObject(strjsonString);
			weatherinfo = weatherJsonObject.getJSONObject("weatherinfo");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (weatherinfo == null) {
			return false;
		} else {
			return true;
		}

	}

	public String getTemp1() throws JSONException {
		if (weatherinfo != null) {
			return weatherinfo.getString("temp1");
		} else {
			return null;
		}
	}

	public String getTemp2() throws JSONException {
		if (weatherinfo != null) {
			return weatherinfo.getString("temp2");
		} else {
			return null;
		}
	}

	public String getWeather() throws JSONException {
		if (weatherinfo != null) {
			return weatherinfo.getString("weather");
		} else {
			return null;
		}
	}

	public String getPtime() throws JSONException {
		return weatherinfo.getString("ptime");
	}

	public String getWeatherString() {
		try {
			return weatherinfo.getString("city") + " "
					+ weatherinfo.getString("temp1") + "~"
					+ weatherinfo.getString("temp2") + " "
					+ weatherinfo.getString("weather") + " "
					+ weatherinfo.getString("ptime");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
}
