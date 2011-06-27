package nlp;

import java.util.List;

public interface Solution extends Comparable, Cloneable {
	boolean isComplete(); //test if solution found
	List<String> getObjective(); //the result
	List<Solution> getSucessors(); //get children
}
