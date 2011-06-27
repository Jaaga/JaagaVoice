package run;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

import network.*;
import util.DBUtil;

public class RunDB_TEST {

	private static Connection conn;

	
	public static void main(String[] argv) {
		DBUtil.initDB();		

		System.out.println("running...");
		
		//create a beevolve object to get text from crawl
		String url = "http://analytics.beevolve.com/api/v1/datacontent/access_token=2xs-85ae137124a638977cb3";
		Beevolve bee = null;
		try {
			bee = new Beevolve(url);
		} catch (MalformedURLException e) {
			System.out.println("couldn't open Beevolve URL \n" + url);
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("initialized beevolve object");
		
		//run crawl
		try {
			bee.search("0"); //test - no saved UID
		} catch (IOException e) {
			System.out.println("couldn't search");
			e.printStackTrace();
		}
		System.out.println("search successful");

		DBUtil.populateDB(bee.getResult().docs);
		

	}
}
