package com.data2semantics.yasgui.client.settings;

/*
 * #%L
 * YASGUI
 * %%
 * Copyright (C) 2013 Laurens Rietveld
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import com.data2semantics.yasgui.client.helpers.JsMethods;
import com.data2semantics.yasgui.client.helpers.JsonHelper;
import com.data2semantics.yasgui.client.helpers.LocalStorageHelper;
import com.data2semantics.yasgui.shared.SettingKeys;
import com.data2semantics.yasgui.shared.exceptions.SettingsException;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;

public class Settings extends JsonHelper {
	private ArrayList<TabSettings> tabArray = new ArrayList<TabSettings>();
	private Defaults defaults;
	
	
	/**
	 * DEFAULTS
	 */
	public static int DEFAULT_SELECTED_TAB = 0;
	
	public Settings() {}
	
	public Settings(String jsonString) throws IOException {
		this.addToSettings(jsonString);
	}
	


	public void addToSettings(String jsonString) throws IOException {
		JSONValue jsonVal = JSONParser.parseStrict(jsonString);
		if (jsonVal != null) {
			JSONObject jsonObject = jsonVal.isObject();
			if (jsonObject != null) {
				addToSettings(jsonObject);
			} else {
				throw new IOException("Unable to convert json value to json object");
			}
		} else {
			throw new IOException("Unable to parse json settings string");
		}
	}
	
	public void addToSettings(JSONObject jsonObject) {
		Set<String> keys = jsonObject.keySet();
		for (String key : keys) {
			if (key.equals(SettingKeys.TAB_SETTINGS)) {
				JSONArray jsonArray = jsonObject.get(key).isArray();
				for (int i = 0; i < jsonArray.size(); i++) {
					//Add as TabSettings to tab arraylist
					tabArray.add(new TabSettings(this, jsonArray.get(i).isObject()));
				}
			} else if (key.equals(SettingKeys.DEFAULTS)) {
				if (defaults == null) {
					defaults = new Defaults(jsonObject.get(key).isObject());
				} else {
					defaults.update(jsonObject.get(key).isObject());
				}
				
			} else {
				put(key, jsonObject.get(key));
			}
		}
	}
	
	public void initDefaultTab() {
		this.setSelectedTabNumber(DEFAULT_SELECTED_TAB);
		addTabSettings(new TabSettings(this, defaults));
	}
	
	public boolean inSingleEndpointMode() {
		boolean singleEndpointMode = false;
		if (containsKey(SettingKeys.SINGLE_ENDPOINT_MODE)) {
			singleEndpointMode = get(SettingKeys.SINGLE_ENDPOINT_MODE).isBoolean().booleanValue();
		}
		return singleEndpointMode;
	}

	
	public void setSelectedTabNumber(int selectedTabNumber) {
		put(SettingKeys.SELECTED_TAB_NUMBER, new JSONNumber(selectedTabNumber));
	}
	
	public void addTabSettings(TabSettings tabSettings) {
		tabArray.add(tabSettings);
	}
	
	public ArrayList<TabSettings> getTabArray() {
		return tabArray;
	}
	
	public JSONArray getTabArrayAsJson() {
		JSONArray jsonArray = new JSONArray();
		for(int i = 0; i < tabArray.size(); i++) {
			jsonArray.set(i, (JSONObject)tabArray.get(i));
		}
		return jsonArray;
	}
	
	
	public int getSelectedTabNumber() {
		int selectedTab = (int)get(SettingKeys.SELECTED_TAB_NUMBER).isNumber().doubleValue();
		if (selectedTab >= tabArray.size()) {
			//Something is wrong, tab does not exist. take last tab
			selectedTab = tabArray.size()-1;
		}
		return selectedTab;
	}
	
	public void removeTabSettings(int index) {
		tabArray.remove(index);
	}
	
	public TabSettings getSelectedTabSettings() throws SettingsException {
		if (getSelectedTabNumber() >= 0) {
			return tabArray.get(getSelectedTabNumber());
		} else {
			return new TabSettings(this, getDefaults());
		}
	}
	
	public Defaults getDefaults() {
		return defaults;
	}
	
	public String getGoogleAnalyticsId() {
		String id = getString(SettingKeys.GOOGLE_ANALYTICS_ID);
		return id;
	}
	
	public boolean useGoogleAnalytics() {
		String analyticsId = getGoogleAnalyticsId();
		return (analyticsId != null && analyticsId.length() > 0 && getTrackingConsent());
	}
	
	public void setTrackingConsent(boolean consent) {
		put(SettingKeys.TRACKING_CONSENT, JSONBoolean.getInstance(consent));
	}
	
	public boolean getTrackingConsent() {
		boolean consent = true;
		if (containsKey(SettingKeys.TRACKING_CONSENT)) {
			consent = get(SettingKeys.TRACKING_CONSENT).isBoolean().booleanValue();
		}
		return consent;
	}
	
	public void setTrackingQueryConsent(boolean consent) {
		put(SettingKeys.TRACKING_QUERIES_CONSENT, JSONBoolean.getInstance(consent));
	}
	
	public boolean getTrackingQueryConsent() {
		boolean consent = true;
		if (containsKey(SettingKeys.TRACKING_QUERIES_CONSENT)) {
			consent = get(SettingKeys.TRACKING_QUERIES_CONSENT).isBoolean().booleanValue();
		}
		return consent;
	}
	
	public boolean cookieConsentAnswered() {
		return (containsKey(SettingKeys.TRACKING_CONSENT) && containsKey(SettingKeys.TRACKING_QUERIES_CONSENT));
	}
	
	public boolean useBitly() {
		boolean useBitly = false;
		if (containsKey(SettingKeys.USE_BITLY) && get(SettingKeys.USE_BITLY).isBoolean() != null) {
			useBitly = get(SettingKeys.USE_BITLY).isBoolean().booleanValue();
		}
		return useBitly;
	}
	
	public boolean isDbSet() {
		boolean dbSet = false;
		if (containsKey(SettingKeys.DB_SET) && get(SettingKeys.DB_SET).isBoolean() != null) {
			dbSet = get(SettingKeys.DB_SET).isBoolean().booleanValue();
		}
		return dbSet;
	}
	
	
	
	/**
	 * Returns JSON representation of this object
	 */
	public String toString() {
		put(SettingKeys.TAB_SETTINGS, getTabArrayAsJson());
		put(SettingKeys.DEFAULTS, defaults);
		return super.toString();
	}
	
	public String getBrowserTitle() {
		String title = "YASGUI";//default value
		if (containsKey(SettingKeys.BROWSER_TITLE)) {
			title = get(SettingKeys.BROWSER_TITLE).isString().stringValue();
		}
		return title;
	}
	
	
	public void setBrowserTitle(String title) {
		put(SettingKeys.BROWSER_TITLE, new JSONString(title));
	}
	
	public static Settings retrieveSettings() throws IOException {
		Settings settings = new Settings();
		String defaultSettings = JsMethods.getDefaultSettings();
		if (defaultSettings == null || defaultSettings.length() == 0) {
			throw new IOException("Failed to load default settings from javascript.");
		}
		 
		//First create settings object with the proper default values
		//need default values when creating settings objects, as not all values might be filled in our cache and stuff
		settings.addToSettings(defaultSettings);
		
		settings = addUrlArgToSettings(settings);
		
		String settingsString = LocalStorageHelper.getSettingsStringFromCookie();
		if (settingsString != null && settingsString.length() > 0) {
			settings.addToSettings(settingsString);
			
			//add installation + url settings again. The settings retrieved from cookie might have stale default values
			settings.addToSettings(defaultSettings);
			settings = addUrlArgToSettings(settings);
		} else {
			//no options in cache. we already have default values and settings object
			//now initialize a tab with default values
			settings.initDefaultTab();
		}
		return settings;
	}
	
	/**
	 * If we have settings passed as argument (e.g. in an iframe setting, add these to settings object)
	 * @param settings
	 * @return
	 * @throws IOException
	 */
	private static Settings addUrlArgToSettings(Settings settings) throws IOException {
		//If we have settings passed as argument (e.g. in an iframe setting, add these to settings object)
		String jsonSettings = Window.Location.getParameter(SettingKeys.JSON_SETTINGS_ARGUMENT);
		if (jsonSettings != null && jsonSettings.length() > 0) {
			settings.addToSettings(jsonSettings);
		}
		return settings;
	}
}
