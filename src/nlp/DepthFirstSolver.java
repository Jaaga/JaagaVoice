package nlp;

import java.util.List;

/**
 * 
 * @author Heather Dewey-Hagborg based on http://www.brpreiss.com/books/opus5/html/page442.html
 * @version 1.0
 * backtracking depth first
 *
 */
public class DepthFirstSolver extends AbstractSolver {

	@Override
	protected boolean search(Solution solution) {
		if (solution.isComplete()){
			updateBest(solution);
			return true;
		}
		else{
			List<Solution> children = solution.getSucessors();
			
			for (Solution s: children){
				boolean done = search(s);
				if (done){
					return true;
				}
			}
		}
		return false;
	}

	
}
