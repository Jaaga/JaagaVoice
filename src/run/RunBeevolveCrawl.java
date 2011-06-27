package run;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import network.BeeResult;
import network.Beevolve;
import util.DBUtil;
import util.Tools;

public class RunBeevolveCrawl {
	public static void main(String[] args){
		String UID = DBUtil.getLastUID();
		System.out.println("last UID: " + UID);
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
			UID = bee.search(UID);
		} catch (IOException e) {
			System.out.println("couldn't search");
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("search successful, last uid: " + UID);
		//write last UID to file for next crawl
		try {
			Tools.writeFile("UID.txt", UID);
		} catch (IOException e1) {
			System.out.println("couldn't write UID to file");
			e1.printStackTrace();
			System.exit(-1);
		}
		
		BeeResult br = bee.getResult();
		//TEMP debug
		List<String> texts = bee.getTexts();
		System.out.println("found the following texts:");
		System.out.println(texts);
		
		if (br.docs.size() > 0){
			//save GSON object as fields in DB
			DBUtil.populateDB(bee.getResult().docs);

			//create new data flag file
			try {
				Tools.createFlagFile("NEW");
			} catch (IOException e) {
				System.out.println("couldn't create flag file");
				e.printStackTrace();
			}
		}
	}
}
