package edu.vanderbilt.webtest.plugins.proxy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.owasp.webscarab.httpclient.HTTPClient;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.owasp.webscarab.plugin.proxy.ProxyPlugin;

import edu.vanderbilt.webtest.crawl.CrawlDriver;

public class ProxyPageTokenizer extends ProxyPlugin {

	public ProxyPageTokenizer() {
		super();
		regexFilters = new HashMap<Pattern,String>();
		regexList = new ArrayList<Pattern>();
		
		//Kill all mailto: links
		addRegexFilter("<a href=[\"']mailto:[^>]+>(.+?)</a>", "<span>$1</span>");
		
		//DATETIME TOKENS
		//[Weekday[,]] Month DD[,] [YY]YY HH:MM[:SS] [am/pm]
		addRegexFilter("(\\w+,?\\s+)?\\w+\\s+\\d{1,2},?\\s+(\\d{2,4},?\\s+)?\\d{1,2}:\\d{2}(:\\d{2})?([ap]m)?", "{DATETIME}");
		
		//Weekday, Month DD, YYYY
		addRegexFilter("(\\w+,?\\s+)?\\w+\\s+\\d{1,2},?\\s+(\\d{2,4})?", "{DATE}");
	}
	
	private static List<Pattern> regexList;
	private static HashMap<Pattern,String> regexFilters; 
	
	public void addRegexFilter(String find, String replace) {
		Pattern p = Pattern.compile(find, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		ProxyPageTokenizer.regexFilters.put(p, replace);
		ProxyPageTokenizer.regexList.add(p);
	}
	
	@Override
	public String getPluginName() {
		return "ProxyPageTokenizer";
	}

	@Override
	public HTTPClient getProxyPlugin(HTTPClient in) {
		return new Plugin(in);
	}
	
	private class Token {
		private String _type = null;
		private String _var = null;
		private String _param = null;
		
		private SortedSet<String> mult = new TreeSet<String>();
		
		private Boolean unique = true;
		
		Token(String type, String var) { 
			_type = type;
			_var = var;
			mult.add(type + ":" + var);
		}
		
        Token(String type, String var, String param) {
            _type = type;
            _var = var;
            //_param = param;
            mult.add(type + ":" + var);
        }
        
        public void add(String type, String var, String param) {
            unique = (type.equals(_type) && var.equals(_var));
            
            if(unique) return;
            
            String sig;
            //if(param==null)
                    sig = type + ":" + var;
            //else
            //      sig = type + ":" + var + "." + param;
            
            mult.add(sig);
        }
		
		@Override
		public String toString() {
			if(!unique) return null;
			
			if(_param==null)
				return "{"+_type.toUpperCase()+":"+_var+"}";
			else
				return "{"+_type.toUpperCase()+":"+_var+"#"+_param+"}";
        }
        
        public String toMultString() {
            if(unique || mult.size()==1) return toString();
            return mult.toString();
        }
    }

//Sorts in descending order of string length
public class DescLengthComparator implements Comparator<String> {

	public int compare(String o1, String o2) {
		if(o1==null) return -1;
		if(o2==null) return 1;
		return o2.length() - o1.length();
	}

}
	
	
/**
 * The actual WebScarab plugin.
 * 
 * @author Cor Paul
 */
private class Plugin implements HTTPClient {
	public final Logger LOGGER = Logger.getLogger(Plugin.class.getName());
	private HTTPClient client;
	
	private SortedMap<String, Token> tokens = new TreeMap<String, Token>(new DescLengthComparator());

	private String logbase = "/srv/logger/replace/";
	
	public Plugin(HTTPClient in) {
		LOGGER.info("Adding ProxyPageTokenizer plugin.");
        this.client = in;
    }
        
    private ArrayList<String> lastQueries = new ArrayList<String>();

	public Response fetchResponse(Request request) throws IOException {
    //System.err.println("Calling ProxyPageTokenizer::fetchResponse()");
    
    //TODO untokenize GET and POST params
    
    
    LOGGER.debug("CLEARING TOKENS");
    tokens.clear();
    Response response = this.client.fetchResponse(request);
    
	String contentType = response.getHeader("Content-Type");
	
	//Short circuit if not HTML
	if (!contentType.contains("text/html")) return response;
            
        String msg = new String(response.getContent());
        
        tokenizeSqlResults(msg);
        tokenizeRequestVariables(msg, request);
        tokenizeSessionVariables(msg);
        addMiscTokens(msg,request);
        
        msg = tokenizePage(msg);
        msg = regexReplace(msg);
        
        //String pagename = request.getURL().getPath().replace("/", ":");
        //logTokens(pagename+".log");
        logTokens("last.log", request);
        response.setContent(msg.getBytes());
        
        return response;
    }
    
    private void tokenizeSqlResults(String pageContent) {
        //Short circuit if no work to do
        if(ProxyLogger.lastSelectQueries.size()==0) return;
            
		java.sql.Connection conn = null;
		
		Properties props = new Properties();
        props.put("user", "logger");
        props.put("password", "");
        
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+CrawlDriver.dbname, props);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
            
        while(!ProxyLogger.lastSelectQueries.isEmpty()) {
            String q = ProxyLogger.lastSelectQueries.remove(0);
            lastQueries.add(q);
            
            try {
                Statement s = conn.createStatement();
                if(s.execute(q)) { //TODO only returns first ResultSet. Do we need to handle more?
                    ResultSet rs = s.getResultSet();
                    ResultSetMetaData meta = rs.getMetaData();
                    int ncols = meta.getColumnCount();
                    
                    //Process rows
                    for(int i=0; rs.next(); ++i) {
                        //For each result, replace with {SQL:TABLE.COLUMN#RowNo}
                        int j;
                        for(j=1; j<=ncols; ++j) {
                            String val = rs.getString(j);
                            String field = meta.getTableName(j);
                            if(field == null || field.length()==0)
                                field = "[ALIAS]";
                            field += "."+meta.getColumnName(j);
                            
                            if(val==null) continue;
                            
                            if(tokens.containsKey(val)) {
                                tokens.get(val).add("SQL", field, Integer.toString(i));
                                //System.out.println("Killing key "+val);
                            } else {
                                Token tk = new Token("SQL", field, Integer.toString(i));
                                tokens.put(val, tk);
                                //System.out.println("Adding replacement: "+val+" --> "+tk.toString());
                            }
                            
                            //DEBUG
                            if(val.contains("Anushi Shah Ryan Millay")) {
                                LOGGER.debug("Processing key "+val+" --> "+field);
                                if(tokens.get(val)==null) LOGGER.debug("NOT ADDED!");
                                else LOGGER.debug(tokens.get(val).toMultString());
                            }
                        }
                    }
                }
                s.close();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void tokenizeRequestVariables(String pageContent, Request request) {
		//TODO POST variables
		
		String http_get_params = request.getURL().getParameters();
		if(http_get_params==null) return;
		
		Pattern param_regex = Pattern.compile("([^&=?]+)=([^&=]+)");
		
		Matcher m = param_regex.matcher(http_get_params);
		while(m.find()) {
		        //Add a full key=val replacement
		    if(!tokens.containsKey(m.group(0)))
		        tokens.put(m.group(0), new Token("GET", m.group(1)+"-full"));
		    else
		        tokens.get(m.group(0)).add("GET", m.group(1)+"-full", null);
		    
		    //Add val replacement
		    if(!tokens.containsKey(m.group(2)))
		        tokens.put(m.group(2), new Token("GET", m.group(1)));
		    else
		        tokens.get(m.group(2)).add("GET", m.group(1), null);
		}
    }
    
    private void tokenizeSessionVariables(String pageContent) {
    	//TODO
    }
    

    private void addMiscTokens(String pageContent, Request request) {
        //Scarf referrer: current page and URIencoded current page
	
        String pageRequest = request.getURL().getPath();
        if(request.getURL().getParameters()!= null)
            pageRequest += request.getURL().getParameters();
        
        tokens.put(pageRequest, new Token("ENVIRONMENT", "request"));
        try {
            tokens.put(URLEncoder.encode(pageRequest, "UTF-8"), new Token("ENVIRONMENT", "request"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private String tokenizePage(String pageContent) {
        String key;
        Token replacestr;
        for(Iterator<String> it = tokens.keySet().iterator(); it.hasNext(); ) {
        	key = it.next();
        	if(key.length()<2) continue;
        	replacestr = tokens.get(key);
        	if(replacestr == null || replacestr.toString()==null) continue;
        	pageContent = pageContent.replace(key, replacestr.toString());
        }
        return pageContent;
    }
    
    private String regexReplace(String pageContent) {
		if(regexFilters.size()==0) LOGGER.debug("No regex filters");
    		
    	for(Pattern p : regexList) {
    		Matcher m = p.matcher(pageContent);
    		pageContent = m.replaceAll(regexFilters.get(p));
    	}
    	
    	return pageContent;
    }
    
    private void logTokens(String fname, Request request) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logbase  + fname));
                
            for(Iterator<String> it = tokens.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                bw.write("\""+key+"\" --> ");
                Token tk = tokens.get(key);
                if(tk==null || tk.toMultString()==null) {
                    bw.write("NULL\n");
                    continue;
                }
		
                if(key.length()<2) {
					bw.write("[Short] ");
        		}
				bw.write(tk.toMultString()+"\n");
		    }
		            
		    bw.write("\n\n--------------------------------------------------------------\n\n");
		            
		    for(Iterator<String> it = lastQueries.iterator(); it.hasNext(); ) {
		        bw.write(it.next()+"\n");
		    }
		    
		    lastQueries.clear();
		    
		    bw.write("\n\n--------------------------------------------------------------\n\n");
		    //Post parameters
		    bw.write(request.getURL().toString()+"\n");
		    bw.write(request.getURL().getPath()+"\n");
		    bw.write(request.getURL().getQuery()+"\n");
		    if(request.getURL().getParameters()==null)
		        bw.write("NULL params\n");
		    else
		        bw.write(request.getURL().getParameters().toString()+"\n");
		    bw.write(new String(request.getContent()));
		    bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        	e.printStackTrace();
        }
    }
}
}
