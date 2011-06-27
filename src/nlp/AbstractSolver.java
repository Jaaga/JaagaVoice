package nlp;

import java.util.List;

/*
 * http://www.brpreiss.com/books/opus5/html/page442.html
 */

public abstract class AbstractSolver implements Solver {
	protected Solution bestSolution;
	protected List<String> bestObjective;
	
	protected abstract boolean search(Solution initial);
	
	@Override
	public Solution solve(Solution initial) {
		bestSolution = null;
		bestObjective = null; //score of the solution - how good is it
		search(initial);
		return bestSolution;
	}
	public void updateBest(Solution solution){
		if (solution.isComplete()){
			bestSolution = solution;
			bestObjective = solution.getObjective();
			//System.out.println("updated best");
		}
	}
	public Solution getBest(){
		return bestSolution;
	}
}
