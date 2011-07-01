package nlp;

import java.util.List;

/**
 * 
 * @author Heather Dewey-Hagborg based on http://www.brpreiss.com/books/opus5/html/page442.html
 * @version 1.0
 *
 */
public interface Solution extends Comparable, Cloneable {
	boolean isComplete(); //test if solution found
	List<String> getObjective(); //the result
	List<Solution> getSucessors(); //get children
}
