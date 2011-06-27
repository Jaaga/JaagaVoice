package network;

public class Doc {
	public String domain;
	public String title;
	public String site;
	public String published_at;
	public String uid;
	public String name;
	public String content;
	public int id;
	
	public Doc(){
		
	}
	@Override
	public String toString(){
		return "\n\n" + title + " - " + name + ":" + domain + site;
		
	}
}
