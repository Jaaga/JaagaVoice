package network;

import java.net.URL;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;


public class RssReader {
	URL url;
	XmlReader reader = null;
    SyndFeed feed = null;
    
	public RssReader(String _url) throws Exception {
		 
		url  = new URL(_url);
	    
	    try {
	    	reader = new XmlReader(url);
	    	feed = new SyndFeedInput().build(reader);
	    	System.out.println(feed.getTitle() + " created successfully");
	    	
	    } finally {
	    	if (reader != null)
	    		reader.close();
	    }
	}
	public SyndEntry getMostRecentEntry(){
		SyndEntry entry = (SyndEntry) feed.getEntries().get(0);
		
		return entry;
	}
}
