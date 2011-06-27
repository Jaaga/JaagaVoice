package nlp;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class TestNLP {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		LanguageModel lm = new LanguageModel(3);
		String in = FileUtils.readFileToString(new File("/home/jaaga/speech/DATA/maha.txt"));
		//System.out.println(in);
		lm.handle(in);
		
		DepthFirstSolver solver = new DepthFirstSolver();
		SyllableSolution solution = new SyllableSolution(lm, 3, 8);
		solver.solve(solution);
		
		System.out.println("best: " + solver.getBest().getObjective());
	}

}
