package nlp;

import java.util.List;


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
