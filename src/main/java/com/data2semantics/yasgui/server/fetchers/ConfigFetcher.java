package com.data2semantics.yasgui.server.fetchers;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.data2semantics.yasgui.server.Helper;
import com.data2semantics.yasgui.shared.SettingKeys;

/**
 * Servlet implementation class ConfigServlet
 */
public class ConfigFetcher {
	public static String CONFIG_DIR = "/config/";
	private static String CONFIG_FILE = "config.json";
	
	public static String getValueAsString(String path, String key) {
		String value = null;
		try {
			JSONObject jsonObject = getJsonObjectFromPath(path);
			value = jsonObject.getString(key);
		} catch (Exception e) {
			//do nothing. return default
		}
		return value;
	}
	
	/**
	 * check whether config json string is defined and parsable. if so, return the string
	 * 
	 * @param path
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String getJson(String path) throws IOException {
		String jsonString;
		try {
			JSONObject jsonObject = getJsonObjectFromPath(path);
			jsonObject = addInfo(jsonObject);
			jsonObject = removeInfo(jsonObject);
			jsonString = jsonObject.toString();
		} catch (IOException e) {
			throw e; 
		} catch (Exception e) {
			throw new IOException("Unable to parse config file on server", e);
		}
			
		return jsonString;
	}
	
	public static JSONObject getJsonObjectFromPath(String path) throws ParseException, JSONException, FileNotFoundException, IOException {
		File configFile = new File( path + CONFIG_DIR + CONFIG_FILE);
		return getJsonObjectFromFile(configFile);
	}
	
	public static JSONObject getJsonObjectFromPath(File path) throws ParseException, JSONException, FileNotFoundException, IOException {
		File configFile = new File( path.getAbsolutePath() + CONFIG_DIR + CONFIG_FILE);
		return getJsonObjectFromFile(configFile);
	}
	
	private static JSONObject getJsonObjectFromFile(File configFile) throws IOException, JSONException {
		if (!configFile.exists()) {
			throw new IOException("Unable to load config file from server. Trying to load: " + configFile.getAbsolutePath());
		} else {
			String jsonString = IOUtils.toString(new FileReader(configFile));
			return new JSONObject(jsonString);
		}
	}
	
	
	
	/**
	 * We send this config to clientside. remove setting only used on serverside, and possible sensitive (e.g. api keys)
	 * 
	 * @param jsonObject
	 * @return
	 */
	public static JSONObject removeInfo(JSONObject jsonObject) {
		//remove all bitly stuff. not used on clientside anyway
		if (jsonObject.has(SettingKeys.BITLY_API_KEY)) jsonObject.remove(SettingKeys.BITLY_API_KEY);
		if (jsonObject.has(SettingKeys.BITLY_API_KEY)) jsonObject.remove(SettingKeys.BITLY_USERNAME);
		
		//remove all mysql things. don't want this on clientside
		if (jsonObject.has(SettingKeys.MYSQL_HOST)) jsonObject.remove(SettingKeys.MYSQL_HOST);
		if (jsonObject.has(SettingKeys.MYSQL_PASSWORD)) jsonObject.remove(SettingKeys.MYSQL_PASSWORD);
		if (jsonObject.has(SettingKeys.MYSQL_USERNAME)) jsonObject.remove(SettingKeys.MYSQL_USERNAME);
		if (jsonObject.has(SettingKeys.MYSQL_DB)) jsonObject.remove(SettingKeys.MYSQL_DB);
		return jsonObject;
	}
	
	/**
	 * induce information we'd like in there
	 * @param jsonObject
	 * @return
	 * @throws JSONException 
	 */
	public static JSONObject addInfo(JSONObject json) throws JSONException {
		json.put(SettingKeys.DB_SET, (Helper.containsKey(json, SettingKeys.MYSQL_HOST) &&
				Helper.containsKey(json, SettingKeys.MYSQL_USERNAME) &&
				Helper.containsKey(json, SettingKeys.MYSQL_PASSWORD) &&
				Helper.containsKey(json, SettingKeys.MYSQL_DB)));
		json.put(SettingKeys.USE_BITLY, (Helper.containsKey(json, SettingKeys.BITLY_API_KEY) &&
				Helper.containsKey(json, SettingKeys.BITLY_USERNAME)));
		return json;
	}
	
}
