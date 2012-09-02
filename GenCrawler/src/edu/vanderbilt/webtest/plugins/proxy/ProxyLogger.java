/*
 * Logs request headers and SQL queries
 * 
 * Usage:
 * 		Add to WebScarabWrapper (Crawljax plugin) as a plugin
 * 
 * Requires:
 * 		AuditLogging plugin for MySQL
 * 
 * NOTE: probably not thread safe yet
 */

package edu.vanderbilt.webtest.plugins.proxy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.owasp.webscarab.httpclient.HTTPClient;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.owasp.webscarab.plugin.proxy.ProxyPlugin;

import edu.vanderbilt.webtest.crawl.CrawlDriver;
import edu.vanderbilt.webtest.util.SessionInspector;
import edu.vanderbilt.webtest.util.SqlLogger;

public class ProxyLogger extends ProxyPlugin {
	public final Logger LOGGER = Logger.getLogger(ProxyLogger.class.getName());
	
	SessionInspector _inspector;
	
	private static String workingDir = CrawlDriver.loggingDir;
	private static String project = CrawlDriver.project;
	//private static String dbname = CrawlDriver.dbname;
	private static String traceDir = workingDir + project + "/";
	
	public static List<String> lastSelectQueries = new ArrayList<String>();
	
	private static SqlLogger sqlLogger = new SqlLogger(CrawlDriver.sqlLog);
	
	public ProxyLogger()  {
		LOGGER.setLevel(Level.INFO);
		LOGGER.info("Query logger startup...");
		//Startup
		_inspector = new SessionInspector();
	}

	@Override
	public String getPluginName() {
		return "ProxyLogger";
	}

	@Override
	public HTTPClient getProxyPlugin(HTTPClient in) {
		return new Plugin(in);
	}
	
	private class Plugin implements HTTPClient {
		/**
		 * The HTTPClient incoming.
		 */
		private HTTPClient client;

		/**
		 * Constructor.
		 * @param in HTTPClient
		 */
		public Plugin(HTTPClient in) {
			this.client = in;
		}
		
		/**
		 * Modify response.
		 * @param request The incoming request.
		 * @throws IOException Thrown on read or write error.
		 * @return The new, modified response.
		 */
		public Response fetchResponse(Request request) throws IOException {
			//TODO Make thread-safe by locking here
			
			//Send request to Tomcat server
			String cookie = request.getHeader("Cookie");
			String ssindex = "";
			if (cookie != null) {
				StringTokenizer st = new StringTokenizer(cookie, " ;");
				//Scan for PHPSESSID
				while(st.hasMoreTokens()) {
					String c = st.nextToken();
					if(c.substring(0,c.indexOf("=")).equals("PHPSESSID")) {
						ssindex = c.substring(c.indexOf("=")+1).trim();
						break;
					}
				}
				//System.out.println(c);
			}
			String method = request.getMethod();
    		String script = request.getURL().toString();
    		String session = _inspector.getSession(ssindex);
    		String time = String.valueOf(System.currentTimeMillis());
    		
    		BufferedWriter bw = new BufferedWriter(new FileWriter(traceDir + project + ".log", true));
    		bw.write("[REQUEST]["+method+"][SCRIPT]["+script+"][SESSION]["+session+"][TIMESTAMP]["+time+"][PARA][");
    		LOGGER.debug("[REQUEST]["+method+"][SCRIPT]["+script+"][SESSION]["+session+"][TIMESTAMP]["+time+"][PARA][");
                
            // POST parameters. NOTE: might need to modify how extract
            String post = new String(request.getContent());
            //System.err.println("'"+post+"'");
            //System.out.write(request.getContent());
            //System.out.println("$$");
            bw.write(post.replace("=", ":"));
            bw.write("]\n");
            bw.close();
    		
			
			sqlLogger.processRequest(request);
            
			//Flush log to make sure nothing new
			sqlLogger.flushLog();
			lastSelectQueries.clear();
			Response response = this.client.fetchResponse(request);
			
			Vector<String> queries = sqlLogger.getQueriesLogged();
			for(String query : queries) {
				writeLog(query);
			}
			return response;
		}
	}
	
	
	private void writeLog(String query) throws IOException {
		if(query.length()==0) return; //Don't log empty strings

	    if(query.toUpperCase().startsWith("SELECT"))
	    	lastSelectQueries.add(query);

	    BufferedWriter bw = new BufferedWriter(new FileWriter(traceDir + project + ".log", true));
		bw.write(query+"\n");
		bw.close();
	}
}


