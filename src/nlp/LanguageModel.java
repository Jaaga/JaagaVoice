package nlp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;

import processing.core.PApplet;
import rita.RiAnalyzer;
import rita.RiTa;
import rita.support.RiLetterToSound;
import util.Tools;

/**
 * Main language model class. Contains statistical properties of the input texts
 * as well as text generation methods.
 * @author Heather Dewey-hagborg
 * @version 1.0
 */

public class LanguageModel extends PApplet implements Serializable {

	private static final long serialVersionUID = 2430698901058667085L;
	private static final int BIGRAM = 2;
	public int NGRAM;
	Map<Integer, HashMap<ArrayList<String>, Integer>> allNgramCounts; 
	List <String> beginnings;
	List <String>  ends;

	/**
	 * create a new ngram model
	 */
	private void initializeNgramMap(){
		allNgramCounts = Collections.synchronizedMap(new HashMap<Integer, HashMap<ArrayList<String>, Integer>>());
		for (int i=1; i<=NGRAM; i++){
			HashMap<ArrayList<String>, Integer> counts = new HashMap<ArrayList<String>, Integer>();
			allNgramCounts.put(i, counts);
		}
		beginnings = new ArrayList<String>();
		ends = new ArrayList<String>();
	}
	
	public LanguageModel(int ngram){
		NGRAM = ngram;
		initializeNgramMap();
	}
	/**
	 * 
	 * @param s -  string of text to train on. will be stripped of special chars, tokenized into sentences and characters.
	 */
	public void handle(String s){
		train(s);
	}

	private void train(String s){
		//replace URLS, hashtags, etc. with ~
		String stripped = Tools.replaceJunk(s, ". ");
		//break text into "sentences"
		String [] sents = RiTa.splitSentences(stripped);
		for (String sent: sents){
			//System.out.println("sentence: " + sent);
			//word tokenize, lowercase and remove punctuation
			sent = Tools.stripPunctuation(sent);
			//sent = Tools.stripSpecialChars(sent);
			sent = sent.toLowerCase();
			
			//System.out.println("training on: " + sent);
			String[] words = sent.split("\\s+");
			//String[] words = RiTa.tokenize(sent);
			//add start and end words to arrays
			beginnings.add(words[0]);
			ends.add(words[words.length-1]);
			
			//iterate over words in the string NGRAM at a time
			//update NGRAM counts
			for (int N=1; N<=NGRAM; N++){
				HashMap<ArrayList<String>, Integer> counts = allNgramCounts.get(N); //get ngram counts for N level
				for (int i=0; i< words.length-N+1; i++){
					String [] subWords = (String[]) ArrayUtils.subarray(words, i, i+N);
					ArrayList<String> ngram = new ArrayList<String>(Arrays.asList(subWords));

					if (counts.containsKey(ngram)){
						Integer x = counts.get(ngram);
						x++;
						counts.put(ngram, x);
					}
					else{
						counts.put(ngram, 1);
					}
				}
			}
		}
	}

	/**
	 * # unique ngrams in model
	 */
	public int getNumNgrams(int ngramLevel){
		return allNgramCounts.get(ngramLevel).size();
	}
	/**
	 * 
	 * @return number of ngrams in model
	 */
	public int getNumAllNgrams() {
		int total = 0;
		for (int i=1; i<=NGRAM; i++){
			total += getNumNgrams(i);
		}
		return total;
	}

	/**
	 * @return hashmap of ngram-> # of occurrences for a specific NGRAM level
	 */
	public HashMap<ArrayList<String>, Integer> getCounts(int ngramLevel){
		return allNgramCounts.get(ngramLevel);
	}

	/**
	 * @return hashmap of ngram-> # of occurences for a all NGRAM levels
	 */
	public Map<Integer, HashMap<ArrayList<String>, Integer>> getAllCounts(){
		return allNgramCounts;
	}

	/**
	 * 
	 * @return ArrayList<ArrayList<String>> flattened arraylist of ngrams
	 */
	public ArrayList<ArrayList<String>> getFlattenedNgramsList() {
		ArrayList<ArrayList<String>> s = new ArrayList<ArrayList<String>>();
		for(int i=1; i<=NGRAM; i++){
			Set<ArrayList<String>> keys = getCounts(i).keySet();
			s.addAll(keys);
		}
		return s;
	}
	
	/**
	 * pretty print language model
	 */
	public void print(){
		System.out.println("Language Model");
		System.out.println(" using up to " + NGRAM + "-grams");
		for (int i=1; i<=NGRAM; i++){
			System.out.println(i +" -GRAMS");
			HashMap<ArrayList<String>, Integer> counts = allNgramCounts.get(i);
			Set<Entry<ArrayList<String>, Integer>> entries = counts.entrySet();

			Iterator<Entry<ArrayList<String>, Integer>> it = entries.iterator();
			while (it.hasNext()){
				Entry<ArrayList<String>, Integer> entry = it.next();
				System.out.print(entry.getValue() + " - ");
				for (String w: entry.getKey()) System.out.print(w + " ");
				System.out.println();
			}
		}
	}
	
	/**
	 * 
	 * @return a random beginning word pulled from our language model list of possible beginning words.
	 */
	public String getRandomBeginningWord(){
		String s = (String) RiTa.random(beginnings);
		return s;
	}
	
	/**
	 * Same as getrandomBeginningWord but returns the size of the possible words chosen from.
	 * @param words
	 * @return a random beginning word
	 */
	public int getRandomBeginningWord(List <String> words){
		String s = (String) RiTa.random(beginnings);
		words.add(s);
		return beginnings.size();
	}
/**
 * 
 * @return list of all beginning words we have encountered.
 */
	public List<String> getBeginnings() {
		return beginnings;
	}
/**
 * 
 * @return list of all end words we have encountered.
 * 
 */
	public List<String> getEnds() {
		return ends;
	}
/**
 * generate a phrase from the language model.
 * this is the simplest generation method.
 * @param numWords to generate
 * @return generated phrase
 */
	public List<String> generatePhrase(int numWords) {
		List <String> words = new ArrayList<String>();
		words.add(getRandomBeginningWord());

		//now choose a next word based on probability
		//repeat, increasing N context until stop condition
		for (int i=1; i<numWords; i++){
			System.out.println("choosing word " + i);
			//update ngram size based on # words chosen so far
			int n = 0;
			if (words.size()+1<=NGRAM)
				n = words.size()+1; 
			else n = NGRAM;

			//get ngrams
			HashMap<ArrayList<String>, Integer> ngrams = allNgramCounts.get(n);
			//get all possible next words
			Set<ArrayList<String>> allPoss = ngrams.keySet();

			List<String> currentNgram = words.subList(words.size()-(n-1), words.size());

			ArrayList<String>  weightedPossibilities = new ArrayList<String>();
			for (ArrayList<String> focus : allPoss){
				if (focus.subList(0, n-1).equals(currentNgram)){
					//add word to weighted possibility list times = weight/strength of this ngram
					for (int times=0; times<ngrams.get(focus); times++) weightedPossibilities.add(focus.get(focus.size()-1));
				}
			}
			if (weightedPossibilities.size()>0){
				//choose 
				//System.out.println("Number of options: " + weightedPossibilities.size());
				words.add((String) RiTa.random(weightedPossibilities));
			}
			else{
				break;
			}
		}
		return words;
	}
	
	/**
	 * Same as generate phrase but allows you to pass in the first word to start with.
	 * @param numWords
	 * @param start
	 * @return list of generated words in phrase
	 */
	public List<String> generatePhrase(int numWords, String start) {
		List <String> words = new ArrayList<String>();
		words.add(start);

		//now choose a next word based on probability
		//repeat, increasing N context until stop condition
		for (int i=1; i<numWords; i++){
			//System.out.println("choosing word " + i);
			//update ngram size based on # words chosen so far
			int n = 0;
			if (words.size()+1<=NGRAM)
				n = words.size()+1; 
			else n = NGRAM;

			//get ngrams
			HashMap<ArrayList<String>, Integer> ngrams = allNgramCounts.get(n);
			//get all possible next words
			Set<ArrayList<String>> allPoss = ngrams.keySet();

			List<String> currentNgram = words.subList(words.size()-(n-1), words.size());

			ArrayList<String>  weightedPossibilities = new ArrayList<String>();
			for (ArrayList<String> focus : allPoss){
				if (focus.subList(0, n-1).equals(currentNgram)){
					//add word to weighted possibility list times = weight/strength of this ngram
					for (int times=0; times<ngrams.get(focus); times++) weightedPossibilities.add(focus.get(focus.size()-1));
				}
			}
			if (weightedPossibilities.size()>0){
				//choose 
				//System.out.println("Number of options: " + weightedPossibilities.size());
				words.add((String) RiTa.random(weightedPossibilities));
			}
			else{
				break;
			}
		}
		return words;
	}
/**
 * Attempt to generate a phrase with a specified number of syllables
 * @param numSyllables to generate ie. 8
 * @return genearated phrase
 */
	public List<String> generateSyllables(int numSyllables) {
		DepthFirstSolver solver = new DepthFirstSolver();
		SyllableSolution solution = new SyllableSolution(this, NGRAM, numSyllables);
		solver.solve(solution);
		
		return solver.getBest().getObjective();
	}
	
	/**
	 * Same as generateSyllables but allows specification of starting word.
	 * @param numSyllables
	 * @param startWord
	 * @return list of generated word in phrase
	 */
	public List<String> generateSyllables(int numSyllables, String startWord) {
		//System.out.println("trying to generate with " + startWord);
		//System.out.println("poss: " + allNgramCounts.get(NGRAM).get(startWord));
		DepthFirstSolver solver = new DepthFirstSolver();
		SyllableSolution solution = new SyllableSolution(this, NGRAM, numSyllables, startWord);
		solver.solve(solution);
		if (solver.getBest()==null){
			//System.out.println("no solution");
			List<String> empty = new ArrayList<String>();
			return empty;
		}
		
		return solver.getBest().getObjective();
	}
	
	/**
	 * count syllables in a string
	 * @param syl
	 * @return number of estimated syllables
	 */
	public static int countSyllables(String syl) {
		// syllables are divided by / 
		int count = 1;
		if (syl.length() == 0) return count;
		String regex = "/";
		Pattern p1 = Pattern.compile(regex);
		Matcher m1 = p1.matcher(syl);
		while (m1.find()) count++;

		return count;
	}

	/**
	 * Count syllables in a list of strings.
	 * @param words
	 * @return number of estimated syllables.
	 */
	public int countSyllables(List <String> words) {
		RiAnalyzer ra = new RiAnalyzer(this);
		RiLetterToSound.VERBOSE = false;
		
		int count = 0;
		for (String s: words){			
			s = Tools.stripSpecialChars(s);
			s = s.replace("'", "");
			if (s.length() == 0 || s.equals(" ")) continue;
			try {
				ra.analyze(s);
				String syl = ra.getSyllables();
				count += countSyllables(syl);
			}
			catch(Exception e){
				count = 1;
				//e.printStackTrace();
				//System.out.println("Cant analyze " + s);
			}
		}
		return count;
	}
	
	/**
	 * Randomize the list of beginning words
	 * @return randomized list.
	 */
	public List<String> getRandomBeginnings() {
		Collections.shuffle(beginnings);
		return beginnings;
	}
	
	/**
	 * Randomize the list children of children of this phrase generated so far
	 * @param words
	 * @return list of child words
	 */
	public List<String> getRandomChildren(List<String> words){
		List<String> children = getChildren(words);
		Collections.shuffle(children);
		return children;
	}
	/**
	 * Find all possible contnuations of this phrase so far.
	 * @param words
	 * @return list of strings of possible next words.
	 */
	public List<String> getChildren(List<String> words){
		int n=words.size()+1;
		if (words.size() >= NGRAM) n=NGRAM;

		//get ngrams
		HashMap<ArrayList<String>, Integer> ngrams = allNgramCounts.get(n);
		//get all possible next words
		Set<ArrayList<String>> allPoss = ngrams.keySet();

		List<String> currentNgram = words.subList(words.size()-(n-1), words.size());
		ArrayList<String>  weightedPossibilities = new ArrayList<String>();

		for (ArrayList<String> focus : allPoss){
			if (focus.subList(0, n-1).equals(currentNgram)){
				//add word to weighted possibility list times = weight/strength of this ngram
				for (int times=0; times<ngrams.get(focus); times++) weightedPossibilities.add(focus.get(focus.size()-1));
			}
		}
		return weightedPossibilities;
	}

	
/**
 * attempt to get placement of stresses within this string 
 * @param s
 * @return tokenized list of stresses in string
 */
	public List<String> getStress(String s) {
		List<String> stressesTokenized = new ArrayList<String>();
		RiAnalyzer ra = new RiAnalyzer(this);
		String stresses;
		try{
			ra.analyze(s);
			stresses = ra.getStresses();
			
		}
		catch (Exception e){
			e.printStackTrace();
			stresses = "1"; //DEFAULT if error
			System.out.println("choked on " + s);
			//System.exit(-1);
		}
		//iterate over and make a list
		for (int i=0; i< stresses.length(); i++){
			char c = stresses.charAt(i);
			if (c!= '/')  stressesTokenized.add(Character.toString(c));
		}
		
		return stressesTokenized;
	}
	
	/**
	 * Attempt to get stresses of all words in this list
	 * return as list
	 * @param words
	 * @return list of stresses per word in list passed in
	 */
	public List<String> getStresses(List<String> words) {
		ArrayList<String> stresses = new ArrayList<String>();
		for (String w: words){
			//System.out.println("input: " + w);
			List<String> stressSeq = getStress(w);
			
			stresses.addAll(stressSeq);
		}
		return stresses;
	}
/**
 * Determine if the word passed in is an end word.
 * Is it a noun? Or is it contained in our model of end words encountered so far?
 * @param string
 * @return true if the word is an end word, false otherwise
 */
	public boolean isEndWord(String string) {
		if (string.length() == 0) return false;
		
		String [] tokens = {string};
		String[] tags = RiTa.posTag(tokens);
		
		if (tags[0].contains("nn")) {
			//System.out.println("is noun");
			return true; //end on noun OK
		}
		if (ends.contains(string) 
				&& !tags[0].equals("dt") 
				&& !tags[0].equals("in")) {
			//System.out.println("is an end");
			return true; //end word
		}
		else return false;
	}

	/**
	 * Attempt to resolve the existing words in this generated phrase to a sentence with a proper ending word.
	 * @param existingWords
	 * @return list of words in the resolved phrase
	 */
	public List<String> resolve(List<String> existingWords) {
		List <String> words = new ArrayList<String>();
		words.addAll(existingWords);

		//now choose a next word based on probability
		//repeat, increasing N context until stop condition
		while (! isEndWord(words.get(words.size()-1))){
			//System.out.println(words);
			//System.out.println("choosing word towards end word");
			
			//update ngram size based on # words chosen so far
			//limit to bigram for this
			int n = 0;
			if (words.size()+1<=BIGRAM)
				n = words.size()+1; 
			else n = BIGRAM;

			//get ngrams
			HashMap<ArrayList<String>, Integer> ngrams = allNgramCounts.get(n);
			//get all possible next words
			Set<ArrayList<String>> allPoss = ngrams.keySet();

			List<String> currentNgram = words.subList(words.size()-(n-1), words.size());

			ArrayList<String>  weightedPossibilities = new ArrayList<String>();
			for (ArrayList<String> focus : allPoss){
				if (focus.subList(0, n-1).equals(currentNgram)){
					//add word to weighted possibility list times = weight/strength of this ngram
					for (int times=0; times<ngrams.get(focus); times++) weightedPossibilities.add(focus.get(focus.size()-1));
				}
			}
			if (weightedPossibilities.size()>0){
				//choose 
				//System.out.println("Number of options: " + weightedPossibilities.size());
				words.add((String) RiTa.random(weightedPossibilities));
			}
			else{
				//System.out.println("no more options");
				break;
			}
		}
		return words;
	}
}
