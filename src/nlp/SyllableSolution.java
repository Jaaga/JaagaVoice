package nlp;

import java.util.ArrayList;
import java.util.List;


public class SyllableSolution implements Solution {
	List <String> words = new ArrayList<String>();
	LanguageModel lm;
	int ngram; 
	int goal; //num syllables we want
	List<String> children;
	
	SyllableSolution(LanguageModel _lm, int _ngram, int _goal){
		lm = _lm;
		ngram = _ngram;
		goal = _goal;
	}
	
	SyllableSolution(LanguageModel _lm, int _ngram, int _goal, String _start){
		lm = _lm;
		ngram = _ngram;
		goal = _goal;
		
		//given a start word
		words.add(_start);
	}

	@Override
	public List<String> getObjective() {
		return words;
	}

	@Override
	public List<Solution> getSucessors() {
		List<Solution> possibleSolutions  = new ArrayList<Solution>();
		
		//if no words yet generate first word get all first words - in random order
		if (words.isEmpty()) children = lm.getRandomBeginnings();
		
		//if we are already over on syllable count return empty list
		else if(lm.countSyllables(words)> goal) return possibleSolutions;
		
		//return all possible words from this word - in random order
		//use context
		//weight words by adding them more than once to this returned list
		else children = lm.getRandomChildren(words);
		//System.out.println("words: " + words);

		//System.out.println(children);
		//pack each successor into a solution
		for (String s: children){
			SyllableSolution ss = new SyllableSolution(lm, ngram, goal);
			ss.words.addAll(words);
			ss.words.add(s);
			ss.children = lm.getRandomChildren(ss.words);
			possibleSolutions.add(ss);
		}
		return possibleSolutions;
	}

	@Override
	public boolean isComplete() {
		// check if this solution has solved the problem
		boolean solutionFound = false;
		if (words.size()<1) return solutionFound;
		
		//count syllables
		int count = lm.countSyllables(words);
				
		//if solution meets requirements return true
		//if we reached our syllable count
		if (count == goal){
			solutionFound = true;
		}
	
		//OR if the last word is an end word - has no more children - thats ok too
		if (testEndWordSolution()) solutionFound = true;
		
		return solutionFound;
	}
	
	boolean testEndWordSolution(){
		//if we have a list of children
		if (!(children==null)){
			//if there are no available children
			//and we already have some words chosen
			if(children.isEmpty() && words.size()>0){
				System.out.println("no more children here - end word - solution");
				return true;
			}
		}
		return false;
	}
	@Override
	public int compareTo(Object o) {
		return 0;
	}

}
