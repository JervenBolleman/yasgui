/*******************************************************************************
 * Copyright (c)  2012 Laurens Rietveld
 * 
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 * 
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.data2semantics.yasgui.client.helpers;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.data2semantics.yasgui.client.tab.optionbar.QueryConfigMenu;
import com.data2semantics.yasgui.shared.Prefix;
import com.data2semantics.yasgui.shared.exceptions.ElementIdException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.LayoutSpacer;

public class Helper {
	private static String CRAWL_USER_AGENTS = "googlebot|msnbot|baidu|curl|wget|Mediapartners-Google|slurp|ia_archiver|Gigabot|libwww-perl|lwp-trivial|bingbot";
	private static String PREFIX_PATTERN = "\\s*PREFIX\\s*(\\w*):\\s*<(.*)>\\s*$";
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
	public static HashMap<String, Prefix> getPrefixHashMapFromQuery(String query) {
		HashMap<String, Prefix> queryPrefixes = new HashMap<String, Prefix>();
		RegExp regExp = RegExp.compile(PREFIX_PATTERN, "gm");
		while (true) {
			MatchResult matcher = regExp.exec(query);
			if (matcher == null)
				break;
			queryPrefixes.put(matcher.getGroup(2), new Prefix(matcher.getGroup(1), matcher.getGroup(2)));
		}
		return queryPrefixes;
	}
	
	public static String getStackTraceAsString(Throwable e) {
		String stackTraceString = e.getClass().getName() + ": " + e.getMessage();
		for (StackTraceElement ste : e.getStackTrace()) {
			stackTraceString += "\n" + ste.toString();
		}
		return stackTraceString;
	}
	
	public static String getCausesStackTraceAsString(Throwable e) {
		String stackTraceString = "";
		Throwable cause = e.getCause();
		if (cause != null) {
			stackTraceString += "\ncause: " + Helper.getStackTraceAsString(cause);
			stackTraceString += getCausesStackTraceAsString(e.getCause());
		}
		return stackTraceString;
	}
	public static boolean recordIsEmpty(ListGridRecord record) {
		boolean empty = true;
		String[] attributes = record.getAttributes();
		for (String attribute: attributes) {
			if (record.getAttribute(attribute).length() > 0) {
				empty = false;
				break;
			}
		}
		return empty;
	}
	
	public static void drawTooltip(TooltipProperties tProp) throws ElementIdException {
		if (tProp.getId() == null || tProp.getId().length() == 0) {
			throw new ElementIdException("No Id provided to draw tooltip for");
		}
		if (!JsMethods.elementExists(tProp.getId())) {
			throw new ElementIdException("id '" + tProp.getId() + "' not found on page. Unable to draw tooltip");
		}
		JsMethods.drawTooltip(tProp.getId(), tProp.getContent(), tProp.getMy(), tProp.getAt(), tProp.getXOffset(), tProp.getYOffset());
	}
	
	public static String getAcceptHeaders(String mainAccept) {
		String acceptString = mainAccept + "," +
						QueryConfigMenu.CONTENT_TYPE_CONSTRUCT_TURTLE + ";q=0.9," + 
						QueryConfigMenu.CONTENT_TYPE_CONSTRUCT_XML + ";q=0.9," + 
						QueryConfigMenu.CONTENT_TYPE_SELECT_JSON + ";q=0.9," +
						QueryConfigMenu.CONTENT_TYPE_SELECT_XML + ";q=0.9," +
						"*/*;q=0.8";
		return acceptString;
	}
	
	/**
	 * Check whether visitor is a crawler. This way we can avoid the google screenshot containing lots of popups
	 */
	public static boolean isCrawler() {
		String userAgent = JsMethods.getUserAgent();
		RegExp regExp = RegExp.compile(".*(" + CRAWL_USER_AGENTS + ").*");
		MatchResult matcher = regExp.exec(userAgent);
		boolean matchFound = (matcher != null);
		return matchFound;
	}

	/**
	 * We are in debug mode when we are not in production mode, or when we have a debug url parameter set to 1
	 * @return
	 */
	public static boolean inDebugMode() {
		int debugValue = 0;
		if (!GWT.isProdMode()) {
			debugValue = 1;
		} else {
			String value = Window.Location.getParameter("DEBUG");
			if (value == null)
				value = Window.Location.getParameter("debug");
			if (value == null)
				value = Window.Location.getParameter("Debug");
			if (value != null) {
				try {
					debugValue = Integer.parseInt(value);
				} catch (Exception e) {
					// not an integer. ignore, and don't use debug mode
				}
			}
		}
		return (debugValue == 1? true: false);
	}
	
	public static String getHost(String url){
	    if(url == null || url.length() == 0)
	        return "";

	    int doubleslash = url.indexOf("//");
	    if(doubleslash == -1)
	        doubleslash = 0;
	    else
	        doubleslash += 2;

	    int end = url.indexOf('/', doubleslash);
	    end = end >= 0 ? end : url.length();

	    return url.substring(doubleslash, end);
	}


	/**  Based on : http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/2.3.3_r1/android/webkit/CookieManager.java#CookieManager.getBaseDomain%28java.lang.String%29
	 * Get the base domain for a given host or url. E.g. mail.google.com:8080 will return google.com:8080
	 * @param host 
	 * @return 
	 */
	public static String getBaseDomain(String url) {
	    String host = getHost(url);

	    int startIndex = 0;
	    int nextIndex = host.indexOf('.');
	    int lastIndex = host.lastIndexOf('.');
	    while (nextIndex < lastIndex) {
	        startIndex = nextIndex + 1;
	        nextIndex = host.indexOf('.', startIndex);
	    }
	    if (startIndex > 0) {
	        return host.substring(startIndex);
	    } else {
	        return host;
	    }
	}
	
	/**
	 * Get host of current location header (or parent location header, in case of iframe).
	 * Remove port notation as well
	 * @return
	 */
	public static String getCurrentHost() {
		String domain = getHost(JsMethods.getLocation());
		if (domain.indexOf(":") > 0) {
			domain = domain.substring(0, domain.indexOf(":"));
		}
		return domain;
	}

	public static String removeArgumentsFromUrl(String url, Set<String> keySet) {
		String[] splittedUrl = url.split("\\?");
		
		String baseUrl = splittedUrl[0];
		String argsString = "";
		if (splittedUrl.length > 1) {
			argsString = splittedUrl[1];
			String[] argsArray = argsString.split("&");
			ArrayList<String> cleanedArgList = new ArrayList<String>();
			for(String arg: argsArray) {
				String argKey = arg.split("=")[0];
				if (!keySet.contains(argKey)) {
					cleanedArgList.add(arg);
				} else {
					//this is one of the arguments we'd like to remove
				}
			}
			
			boolean firstArg = true;
			argsString = "";
			for (String cleanedArg: cleanedArgList) {
				//ok, so we have some arguments left. add these again to url
				if (!firstArg) {
					argsString += "&";
				}
				argsString += cleanedArg;
				firstArg = false;
			}
		}
		return baseUrl + (argsString.length() > 0 ? "?" + argsString: "");
		
	}
	public static LayoutSpacer getVSpacer() {
		LayoutSpacer spacer = new LayoutSpacer();
		spacer.setHeight100();
		return spacer;
	}
	public static LayoutSpacer getHSpacer() {
		LayoutSpacer spacer = new LayoutSpacer();
		spacer.setWidth100();
		return spacer;
	}
}
