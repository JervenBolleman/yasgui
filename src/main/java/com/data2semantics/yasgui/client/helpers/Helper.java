package com.data2semantics.yasgui.client.helpers;

import java.util.ArrayList;
import java.util.HashMap;

import com.data2semantics.yasgui.client.settings.Settings;
import com.data2semantics.yasgui.shared.Prefix;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class Helper {
	private static String COOKIE_SETTINGS = "yasgui_settings";

	/**
	 * Implode arraylist into string
	 * 
	 * @param arrayList ArrayList to implode
	 * @param glue Glue (separator) for the seting
	 * @return concatenated arraylist
	 */
	public static String implode(ArrayList<String> arrayList, String glue) {
		String result = "";
		for (String stringItem : arrayList) {
			if (result.length() > 0) {
				result += glue;
			}
			result += stringItem;
		}
		return result;
	}

	/**
	 * Store settings as json string in cookie. If html5 local storage is possible, use that. 
	 * html5 storage does not send cookie info on every request, which reduces network load
	 * 
	 * @param settings
	 */
	public static void storeSettingsInCookie(Settings settings) {
		Storage html5Storage = Storage.getLocalStorageIfSupported();
		if (html5Storage != null) {
			html5Storage.setItem(COOKIE_SETTINGS, settings.toString());
		} else {
			//We are using a browser which does not support html5
			Cookies.removeCookie(COOKIE_SETTINGS);
			Cookies.setCookie(COOKIE_SETTINGS, settings.toString());
		}
		
	}
	
	/**
	 * Get settings from cookie (or html local storage if supported). Settings is saved as a json string, so need to parse as json object
	 * @return
	 */
	public static Settings getSettingsFromCookie() {
		Settings settings = new Settings();
		String jsonString;
		Storage html5Storage = Storage.getLocalStorageIfSupported();
		if (html5Storage != null) {
			jsonString = html5Storage.getItem(COOKIE_SETTINGS);
		} else {
			//We are using a browser which does not support html5
			jsonString = Cookies.getCookie(COOKIE_SETTINGS);
		}
		
		if (jsonString != null && jsonString.length() > 0) {
			JSONObject jsonObject = JSONParser.parseStrict(jsonString).isObject();
			if (jsonObject == null) {
				// Something went wrong. Just use original 'bare' settings
				// objects
			} else {
				settings = new Settings(jsonObject);
			}
		}
		return settings;
	}
	
	/**
	 * SmartGWT does have a link element, but this requires a form. Use this object to mimic a link by creating a label object
	 * 
	 * @param message Text of link
	 * @param handler Clickhandler: what to do on onclick
	 * @return Label
	 */
	public static Label getLink(String message, ClickHandler handler) {
	   Label link = new Label();
	   link = new Label(message);
	   link.setStyleName("clickable");
	   link.setHeight100();
	   link.setWidth100();
	   link.setCanSelectText(true);
	   link.addClickHandler(handler);
	   return link;

	}
	
	/**
	 * Create a label element which opens a new window for a given url
	 * 
	 * @param message Text of link
	 * @param url Url to open page for
	 * @return Label
	 */
	public static Label getLinkNewWindow(String message, final String url) {
		return getLink(message, new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				Window.open(url, "_blank", null);
			}});
	}
	
	/**
	 * Checks to query string and retrieves/stores all defined prefixes in an object variable
	 */
	public static HashMap<String, Prefix> getPrefixesFromQuery(String queryInputId) {
		String query = JsMethods.getValueUsingId(queryInputId);
		HashMap<String, Prefix> queryPrefixes = new HashMap<String, Prefix>();
		RegExp regExp = RegExp.compile("^\\s*PREFIX\\s*(\\w*):\\s*<(.*)>\\s*$", "gm");
		while (true) {
			MatchResult matcher = regExp.exec(query);
			if (matcher == null)
				break;
			queryPrefixes.put(matcher.getGroup(2), new Prefix(matcher.getGroup(1), matcher.getGroup(2)));
		}
		return queryPrefixes;
	}
}