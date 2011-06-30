package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import network.Doc;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.sun.syndication.feed.synd.SyndContentImpl;

public class DBUtil {
	private static final int SQL_LIMIT = 5000;
	static String url = "jdbc:mysql://localhost:3306/";
	static String dbName = "voice";
	static String driver = "com.mysql.jdbc.Driver";
	static String userName = "jaaga";
	static String password = "";
	static Connection conn = null;

	public static void initDB() {
		System.out.println("initializing DB");
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(url+dbName,userName,password);
			System.out.println("Connected to the database");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static boolean populateDB(List<SyndContentImpl> contents, String link, String title, String published) {
		boolean newDocs = false;
		
		if (conn == null)
			initDB();

		try {
			System.out.println("adding " + title);
			//System.out.println(contents);
			for(SyndContentImpl s: contents) {
				
				String html = s.getValue();
				Document doc = Jsoup.parse(html);
				//System.out.println("item " + doc.text());
				//check if we already have this doc saved
				String query1 = "select title from rss where link = ? and title = ? and published_at = ?";
				PreparedStatement pst = conn.prepareStatement(query1);
				pst.setString(1, link);
				pst.setString(2, title);
				pst.setString(3, published);
				ResultSet rs = pst.executeQuery();
				
				if (rs.next() == false){
					//this entry does NOT already exist
					//this is new data, set newDocs flag to true
					newDocs = true;
					//insert into DB
					String query2 = "insert into rss (title, published_at, link, content, isread) values (?, ?, ?, ?, false)";
					PreparedStatement pst2 = conn.prepareStatement(query2);
					
					pst2.setString(1, title);
					pst2.setString(2, published);
					pst2.setString(3, link);
					if (doc.text().length() > SQL_LIMIT) pst2.setString(4, doc.text().substring(0, SQL_LIMIT));
					else pst2.setString(4, doc.text());
	
					pst2.executeUpdate();
					pst2.close();
					
					System.out.println(doc.text());
				}
				else {
					System.out.println("already have " + title);
				}
			}
			conn.close();
			System.out.println("DB closed");
			
		} catch (Exception e) {
			System.out.println("couldn't populateDB");
			e.printStackTrace();
		}
		return newDocs;
	}
	
	public static void populateDB(List<Doc> docs) {
		if (conn == null)
			initDB();

		try {
			for(Doc d: docs) {
				String query = "insert into docs (title, domain, site,"+
						"published_at, uid, name) values (?, ?, ?, ?, ?, ?)";
				PreparedStatement pst = conn.prepareStatement(query);
				pst.setString(1, d.title);
				pst.setString(2, d.domain);
				pst.setString(3, d.site);
				pst.setString(4, d.published_at);
				pst.setString(5, d.uid);
				pst.setString(6, d.name);
				pst.executeUpdate();
				pst.close();
			}
			conn.close();
		} catch (Exception e) {
			System.out.println("couldn't populateDB");
			e.printStackTrace();
		}
	}

    public static String getLastUID() {
    	if (conn == null) 
			initDB();
		try {
			String query = "select max(uid) from docs ";
			PreparedStatement pst = conn.prepareStatement(query);
			ResultSet rs = pst.executeQuery();
			rs.next();
			return rs.getString(1);
		} catch (Exception e) {
			System.out.println("couldn't read lastUID");
			e.printStackTrace();
		}
		return null;
	}
	 
	public static List<Doc> getAllDocs(){
		if (conn == null) 
			initDB();

		List<Doc> docs = new ArrayList<Doc>();

		try {
			String query = "select title, domain, site, "+
			"published_at, uid, name from docs";
			PreparedStatement pst = conn.prepareStatement(query);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				Doc doc = new Doc();
				doc.title = rs.getString(1);
				doc.domain = rs.getString(2);
				doc.site = rs.getString(3);
				doc.published_at = rs.getString(4);
				doc.uid = rs.getString(5);
				doc.name = rs.getString(6);
				docs.add(doc);
			}
			rs.close();
			return docs;
		} catch (Exception e) {
			System.out.println("couldn't read docs from db");
			e.printStackTrace();
		}	
		return null;
	}
	
	public static List<Doc> getAllRssDocs(boolean markRead){
		if (conn == null) 
			initDB();

		List<Doc> docs = new ArrayList<Doc>();

		try {
			String query = "select title, published_at, content, id from rss";
			PreparedStatement pst = conn.prepareStatement(query);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				Doc doc = new Doc();
				doc.title = rs.getString(1);
				doc.published_at = rs.getString(2);
				doc.content = rs.getString(3);
				doc.id = rs.getInt(4);
				docs.add(doc);
			}
			rs.close();
			if (markRead && docs.size()>0) {
				markRead(docs.get(0).id, 
						docs.get(docs.size()-1).id);
			}
			return docs;
		} catch (Exception e) {
			System.out.println("couldn't read docs from db");
			e.printStackTrace();
		}	
		return null;
	}


	public static List<Doc> getUnreadDocs() {
		return getUnreadDocs(false);
	}

	public static List<Doc> getUnreadDocs(boolean markRead) {
		if (conn == null) 
			initDB();

		List<Doc> docs = new ArrayList<Doc>();

		try {
			String query = "select title, domain, site, "+
			"published_at, uid, name from docs where isread = false";
			PreparedStatement pst = conn.prepareStatement(query);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				Doc doc = new Doc();
				doc.title = rs.getString(1);
				doc.domain = rs.getString(2);
				doc.site = rs.getString(3);
				doc.published_at = rs.getString(4);
				doc.uid = rs.getString(5);
				doc.name = rs.getString(6);
				docs.add(doc);
			}
			rs.close();

			if (markRead && docs.size()>0) {
				markRead(docs.get(0).uid, 
						docs.get(docs.size()-1).uid);
			}

			return docs;
		} catch (Exception e) {
			System.out.println("couldn't read docs from db");
			e.printStackTrace();
		}	
		return null;
	}
	
	public static List<Doc> getUnreadRssDocs(boolean markRead) {
		if (conn == null) 
			initDB();

		List<Doc> docs = new ArrayList<Doc>();

		try {
			String query = "select title, published_at, id, content from rss where isread = false";
			PreparedStatement pst = conn.prepareStatement(query);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				Doc doc = new Doc();
				doc.title = rs.getString(1);
				doc.published_at = rs.getString(2);
				doc.id = rs.getInt("id");
				doc.content = rs.getString(4);
				docs.add(doc);
			}
			rs.close();

			if (markRead && docs.size()>0) {
				markRead(docs.get(0).id, 
						docs.get(docs.size()-1).id);
			}

			return docs;
		} catch (Exception e) {
			System.out.println("couldn't read docs from db");
			e.printStackTrace();
		}	
		return null;
	}
	
	public static void markRead(String beginUID, String endUID) {
		if (conn == null)
			initDB();

		try {
			String query = "update docs set isread = true where "+
			"uid >= ? and uid <= ?";
			PreparedStatement pst = conn.prepareStatement(query);

			pst.setString(1, beginUID);
			pst.setString(2, endUID);
			pst.executeUpdate();
			pst.close();
		} catch (Exception e) {
			System.out.println("markRead failed");
			e.printStackTrace();
		}
	}

	public static void markRead(int beginUID, int endUID) {
		if (conn == null)
			initDB();

		try {
			String query = "update rss set isread = true where "+
			"id >= ? and id <= ?";
			System.out.println("marking READ " + beginUID + " - " + endUID);
			PreparedStatement pst = conn.prepareStatement(query);
			pst.setLong(1, beginUID);
			pst.setLong(2, endUID);
			pst.executeUpdate();
			pst.close();
		} catch (Exception e) {
			System.out.println("markRead failed");
			e.printStackTrace();
		}
	}

	public static void main(String[] argv) {
		initDB();		

		System.out.println("running...");

		System.out.println("Last UID: "+getLastUID());

		List<Doc> docs = getUnreadDocs(false);
		for(Doc d: docs) {
			System.out.println(d.title);
		}
	}
}
