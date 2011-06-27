package run;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import util.DBUtil;
import util.Tools;

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;

import network.RssReader;

public class RunRss {
	public static void main(String args[]){

		String [] feeds = {"http://deweyhagborg.wordpress.com/feed", 
				"http://feeds.feedburner.com/rhizome-art?format=xml",
				"http://feeds.feedburner.com/rhizome-fp?format=xml",
					"http://feeds.feedburner.com/rhizome-discuss?format=xml",
					"http://www.eyebeam.org/subscribe/full.xml",
				"http://feeds.feedburner.com/douglasrushkoff",
				"http://yruminate.wordpress.com/feed/"};
		//TODO get feeds from DB

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
