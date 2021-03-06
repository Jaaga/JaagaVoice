package run;

import java.io.File;
import java.io.IOException;
import java.util.List;

import network.RssReader;

import org.apache.commons.io.FileUtils;

import util.DBUtil;
import util.Tools;

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
/**
 * @author Heather Dewey-Hagborg
 * @version 1.0
 * 
 * This class handles reading RSS feeds and loading them into the database.
 * Arg 0 - name of file listing all RSS feeds (each on separate line) 
 * ie. feeds.txt
 * 
 * Example use:
 * java -jar runrss.jar feeds.txt
 */
public class RunRss {
	/**
	 * Main method, runs rss feed reader and saves to database
	 * @param string the feed file listing RSS feeds to read
	 */
	public static void main(String args[]){
		File feedFile = new File(args[0]);
		List<String> feeds = null;
		try {
			feeds = FileUtils.readLines(feedFile);
		} catch (IOException e1) {
			System.out.println("couldn't open rss feeds file");
			e1.printStackTrace();
			System.exit(-1);
		}
		System.out.println("feeds: " + feeds);

		for (String feed: feeds){
			RssReader reader = null;
			try {
				reader = new RssReader(feed);

			} catch (Exception e) {
				System.out.println("can't open " + feed);
				e.printStackTrace();
				continue;
			}
			
			SyndEntry entry = reader.getMostRecentEntry();
			//System.out.println(entry.toString());
			
			List<SyndContentImpl> contents = entry.getContents();
			if (contents.isEmpty()){
				if (entry.getDescription() != null){
					SyndContentImpl si = new SyndContentImpl();
					si.copyFrom(entry.getDescription());
					//si.setValue();
					contents.add(si);
				}
				else{
					System.out.println("no content");
					continue;
				}
			}
			
			String title = "";
			if (entry.getTitle() == null){
				System.out.println("no title");
			}
			else title = entry.getTitle();
			System.out.println(title);

			String link = "";
			if (entry.getLink() == null){
				System.out.println("no link");
				//TODO generate an id??
			}
			else link = entry.getLink();
			
			String date = "";
			if (entry.getPublishedDate() == null){
				System.out.println("no date");
			}
			else date = entry.getPublishedDate().toString();
			//store in DB and return a flag true if this is new content
			DBUtil.initDB();
			boolean newDocs =  DBUtil.populateDB(contents, link, title, date);

			if (newDocs)
				try {
					Tools.createFlagFile("NEW");
				} catch (IOException e) {
					System.out.println("COULDN'T CREATE NEW FLAG FILE");
					e.printStackTrace();
				}
		}
	}
}
