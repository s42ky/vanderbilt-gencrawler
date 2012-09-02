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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.owasp.webscarab.httpclient.HTTPClient;
import org.owasp.webscarab.model.HttpUrl;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.owasp.webscarab.plugin.proxy.ProxyPlugin;

import edu.vanderbilt.webtest.crawl.CrawlDriver;

public class ProxyLogger extends ProxyPlugin {
	public final Logger LOGGER = Logger.getLogger(ProxyLogger.class.getName());
	
	SessionInspector _inspector;
	
	private static Boolean sql_input_log_ready = false;
	
	private static String workingDir = CrawlDriver.loggingDir;
	private static String project = CrawlDriver.project;
	//private static String dbname = CrawlDriver.dbname;
	private static String traceDir = workingDir + project + "/";
	private static String sqlLog = CrawlDriver.sqlLog;

	private static BufferedReader input;
	
	private static Request lastRequest = null;
	private static String lastRequestSession = null;
	private static String lastRequestSID = null;
	
	public static List<String> lastSelectQueries = new ArrayList<String>();
	
	public ProxyLogger()  {
		LOGGER.setLevel(Level.INFO);
		LOGGER.info("Query logger startup...");
		//Startup
		try {
			input = new BufferedReader(new FileReader(sqlLog));
		
			_inspector = new SessionInspector();
			
			while (input.readLine() != null) {
				//Skip through lines already existing in log
				continue;
			}
			sql_input_log_ready = true;
			
		} catch(IOException e) {
			e.printStackTrace();
			sql_input_log_ready = false;
		}

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
    		
    		//Cache values for SqlLogger   
    	    lastRequestSID = ssindex;
    	    lastRequestSession = session;
    		
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
    		
    		//*
    		Request notify_server = new Request();
    		notify_server.setMethod("POST");
    		notify_server.setVersion("HTTP/1.1");
    		notify_server.setHeader("host", "localhost:8080");
    		notify_server.setHeader("Content-Type", "application/x-www-form-urlencoded");
    		notify_server.setURL(new HttpUrl("http://localhost:8080/iScope/Portal?method=" + method + "&script=" + script));
    		
    		ByteArrayOutputStream content = new ByteArrayOutputStream();
    		try {
    			if (request.getContent().length != 0) {
                    content.write(request.getContent());
                    content.write(("&").getBytes());
                }
    			LOGGER.debug("session="+session+"&timestamp="+time+"&sid="+ssindex);
                content.write(("session="+session+"&timestamp="+time+"&sid="+ssindex).getBytes());
            } catch (IOException e){
            	e.printStackTrace();
    		}
    		notify_server.setHeader("Content-Length", Integer.toString(content.size()));
    		notify_server.setContent(content.toByteArray());
			
    		//HTTPClient log_client = HTTPClientFactory.getInstance().getHTTPClient();
    		//log_client.fetchResponse(notify_server);
    		//*/
    		
			//Flush log to make sure nothing new
			parseLog();
			
			lastSelectQueries.clear();
			//Cache request parameters
			lastRequest = request;
			Response response = this.client.fetchResponse(request);
			parseLog();
			
			return response;
		}
	}
	
	
	private void writeLog(String query) throws IOException {
		if(query.length()==0) return; //Don't log empty strings
		
		String script = (lastRequest==null)?"null":lastRequest.getURL().toString();
		String session = "null";
		String session_req = lastRequestSession;
		String sid = lastRequestSID;
		
		if (sid!= null && sid!="null") {
		    session = _inspector.inspect(sid);
		}
		
	    if(! session.equals(session_req)) {
	    	System.err.println("Session values have changed.");
	    }
		
	    String time = String.valueOf(System.currentTimeMillis());
	    
	    //Add SELECT queries for proxy use
	    if(query.toUpperCase().startsWith("SELECT"))
	    	lastSelectQueries.add(query);
	    
	    
	    
    	BufferedWriter bw = new BufferedWriter(new FileWriter(traceDir + project + ".log", true));
		if (session != null && query != null && script != null) {    // QUERY + SCRIPT + SESSION + TIME.
			bw.write("[QUERY][" + query + "]");
			bw.write("[SCRIPT][" + script + "]");
			bw.write("[SESSION][" + session + "]");
			if (time != null) bw.write("[TIMESTAMP][" + time + "]");
			bw.write("\n");
			LOGGER.debug(project + " " + query + " time " + time);
		}
		bw.close();
	}
	
	private void parseLog() {
		if(!sql_input_log_ready) return;
		
		String line = null;
		Boolean needSpace = false;
		
		Boolean buildingQuery = false;
		String curQuery = "";
		
		try {
			while ((line = input.readLine()) != null) {
				if(line.equals("{")) {
					buildingQuery = true;
					needSpace = false;
					curQuery = "";
				} else if(line.equals("}")) {
					buildingQuery = false;
					writeLog(curQuery);
				} else if(buildingQuery) {
					if(needSpace) curQuery += " ";
					curQuery += line;
					needSpace = (line.length()>0) && (!line.substring(line.length()-1).equals(" "));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			sql_input_log_ready = false;
		}
	}
}


