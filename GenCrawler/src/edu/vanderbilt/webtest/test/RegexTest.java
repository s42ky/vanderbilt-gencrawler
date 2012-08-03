package edu.vanderbilt.webtest.test;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		HashMap<String, String> regexTests = new HashMap<String,String>();
		// TODO Auto-generated method stub
		String html1 = "<HTML xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\"><HEAD><META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><TITLE>Vanderbilt Conference Management System &gt;&gt; (30) Dissent: Accountable Anonymous Group Messaging</TITLE><LINK href=\"style.css\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\"></HEAD><BODY><DIV id=\"container\"><A href=\"index.php\" style=\"display:block\" title=\"Back to the Front Page\"><DIV id=\"header\"></DIV></A><DIV id=\"menu\"><TABLE><TBODY><TR><TD><A href=\"showsessions.php\">Show Sessions</A></TD><TD><A href=\"register.php\">Register</A></TD><TD><A href=\"login.php\">Login</A></TD></TR></TBODY></TABLE></DIV><DIV id=\"content\"><H2>(30) Dissent: Accountable Anonymous Group Messaging</H2><BR><TABLE><TBODY><TR><TD><A href=\"getpaper.php?paper_id={GET.paper_id}\">View Paper</A></TD><TD><IMG src=\"images/pdf.gif\"></TD></TR></TBODY></TABLE><BR><B><I></I></B><I>,</I><BR><BR><B>Abstract</B><BR>Abstract: (30) Dissent: Accountable Anonymous Group Messaging<BR><HR><H4>Comments</H4>There are<B>0</B>comments<OL id=\"commentlist\"></OL><H4><A href=\"login.php\">Login</A>to post a comment</H4><BR><DIV id=\"footer\">As this website is still in development, please report any issues you have with the software to<A href=\"mailto:scarf@paulisageek.com\">Paul Tarjan</A>so they may be addressed.<BR>Powered by<A href=\"http://scarf.sourceforge.net\">SCARF - Stanford Conference And Research Forum</A></DIV></DIV></DIV></BODY></HTML>";
		
		String html2 = "</TABLE><BR><B><I></I></B><I>,</I><BR><BR><B>Abstract</B><BR>Abstract: (30) Dissent: Accountable Anonymous Group Messaging<BR><HR><H4>";
		
		String html3 = "<LI>Posted by <span>Saturday, June 15, 2010 10:20pm</span> on <I>Friday May 25, 1:59pm</I>\n<BR>{SQL:comments.comment}</LI>";
		
		String html = html3;
		
		System.out.println(html);
		regexTests.put("(getpaper.php\\?paper_id=)\\d+", "$1{GET.paper_id}");
		regexTests.put("<B>.*?</B>\\s*<I>.*?</I>(\\s*<BR>){2}<B>Abstract.+?(<HR>)","$2");//(\\s*<br>){2}\\s*<b>Abstract.+?(<hr>)
		regexTests.put("<li.+(</li>)","");
		regexTests.put("(login.php)\\\\?referer=.+(\")","$1$2");
		regexTests.put("(<H2>).+(</H2>)", "$1$2");
		regexTests.put("(<TITLE>).+(</TITLE>)", "$1$2");
		regexTests.put("(<H4>Comments</H4>There are<B>)\\d*(</B>)", "$1$2");
		
		regexTests.put("(\\w+,?\\s+)?\\w+\\s+\\d{1,2},?\\s+(\\d{2,4},?\\s+)?\\d{1,2}:\\d{2}(:\\d{2})?([ap]m)?", "{DATETIME}");
		regexTests.put("\\w+\\s+\\d{1,2},\\s+", "{DATE}");
		
		for(String key : regexTests.keySet()) {
			Pattern p = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(html);
			html = m.replaceAll(regexTests.get(key));
			System.out.println("Strlen: "+Integer.toString(html.length()));
		}
		
		
		System.out.println("\n\n"+html);
	}

}
