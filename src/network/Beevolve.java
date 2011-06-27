package network;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Beevolve {
	Gson gson = new GsonBuilder().create();
	BeeResult mostRecentResult;
	String urlString;
	
	public Beevolve(String _urlString) throws MalformedURLException{
		urlString = _urlString;
	}
	public String search(String UID) throws IOException{
		URL url = new URL(urlString + "&seq_id=" + UID);
		URLConnection conn = url.openConnection ();
		// Get the response
		Reader rd = new InputStreamReader(conn.getInputStream());
		mostRecentResult = gson.fromJson(rd, BeeResult.class);
		rd.close();
		//save the uid of the last doc for next read
		int last = mostRecentResult.docs.size()-1;
		Doc d = mostRecentResult.docs.get(last);
		return d.uid;
	}
	
	public BeeResult getResult(){
		return mostRecentResult;
	}
	
	public List<String> getTexts(){
		//return mostRecentResult just the texts
		List<String> texts = new ArrayList<String>();
		
		for (Doc d: mostRecentResult.docs){
			texts.add(d.title);
		}
		
		return texts;
	}

}
