package com.data2semantics.yasgui.server;

import java.io.File;
import com.data2semantics.yasgui.client.YasguiService;
import com.data2semantics.yasgui.server.fetchers.EndpointsFetcher;
import com.data2semantics.yasgui.server.fetchers.PrefixesFetcher;
import com.data2semantics.yasgui.shared.exceptions.FetchException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class YasguiServiceImpl extends RemoteServiceServlet implements YasguiService {
	public static String CACHE_DIR = "/cache";
	

	public String fetchPrefixes(boolean forceUpdate) throws IllegalArgumentException, FetchException {
		String prefixes = "";
		try {
			prefixes = PrefixesFetcher.fetch(forceUpdate, new File(getServletContext().getRealPath(CACHE_DIR))); 
		} catch (Exception e) {
			throw new FetchException("Unable to fetch prefixes", e);
		}
		return prefixes;
	}
	
	
	public String fetchEndpoints(boolean forceUpdate) throws IllegalArgumentException, FetchException {
		String endpoints = "";
		try {
			endpoints = EndpointsFetcher.fetch(forceUpdate, new File(getServletContext().getRealPath(CACHE_DIR))); 
		} catch (Exception e) {
			throw new FetchException("Unable to fetch endpoints: " + e.getMessage() + "\n", e);
		}
		return endpoints;
	}
}
