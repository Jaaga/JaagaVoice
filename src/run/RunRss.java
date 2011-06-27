package run;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import java.util.ArrayList;
import java.util.List;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import util.DBUtil;
import util.Tools;

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;

import network.RssReader;
/*
 * pass file listing all RSS feeds (each on separate line) as argument 1
 */
public class RunRss {
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
