package network;

import java.util.List;

public class BeeResult {
	public String seq_id;
	public List<Doc> docs; 
	
	public BeeResult(){
		
	}
	@Override
	public String toString(){
		
		return seq_id + docs;
		
	}
}
