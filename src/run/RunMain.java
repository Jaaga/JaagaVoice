package run;

import java.io.File;
import java.io.IOException;
import java.util.List;

import network.Doc;
import nlp.LanguageModel;
import speech.GenerateSableFile;
import speech.Speak;
import util.DBUtil;
import util.Tools;
/*
 * ARGS - 0 - language model name
 *        1 - NGRAM level to use ie. 3,4,5
 */
public class RunMain {
	static LanguageModel lm;
	static String lmPath;
	
	public static void loop(){
		try{
			//check for existence of file "NEW"
			//means we have new data for LM
			File flag = new File("NEW");
			if (flag.exists()){
				System.out.println("new data to process");
				//if it exists load sentences from
				//new  entries from the DB
				//mark these entries as read
				List<Doc> newDocs;
				//TODO get only unread RSS docs
				newDocs = DBUtil.getUnreadRssDocs(true);

				/*if (newDocs.size()==0) {
					System.out.println("we have NO unread docs");
				}*/

				//delete file "NEW"
				flag.delete();

				//add new data to LM
				for (Doc d: newDocs){
					System.out.println("\nNew Data: " + d.title);
					lm.handle(d.title); 
					System.out.println("\nNew Data: " + d.content);
					lm.handle(d.content);
				}

				//reserialize language model
				try {
					Tools.serialize(lm, lmPath);
				} catch (Exception e) {
					System.out.println("Couldn't reserialize updated language model");
					e.printStackTrace();
					System.exit(-1);
				}
			}
			//else System.out.println("no new data to read");

			//generate text using pluggable method

			GenerateSableFile sable = new GenerateSableFile();
			List<String> words1 = lm.generateSyllables(8);
			List<String> words2 = generateNextLine(lm, words1, false);
			List<String> words3 = generateNextLine(lm, words2, false);
			List<String> words4 = generateNextLine(lm, words3, true);
			String text = "\n";
			for (String s: words1) text = text.concat(s + " ");	
			text = text.concat("\n");
			for (String s: words2) text = text.concat(s + " ");
			text = text.concat("\n");
			for (String s: words3) text = text.concat(s + " ");
			text = text.concat("\n");
			for (String s: words4) text = text.concat(s + " ");
			text = text.concat("\n");

			System.out.println(text);

			try {
				sable.generate(words1, words2, words3, words4, "tospeak.sable");
			} catch (IOException e) {
				e.printStackTrace();
			}

			//could write text to a file here for screen output
			
			//speak!
			System.out.println(".");
			int success = Speak.festivalSpeak("tospeak.sable");
			System.out.println(".");
			
			//Thread.sleep(20000);
			
			//System.out.println(".");
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	public static void setup(String args[]){
		//check for a previous saved language (markov) model
		//if it exists deserialize and load it.
		lmPath = args[0];
		System.out.println("using " + lmPath);
		int N = Integer.parseInt(args[1]);
		File lmFile = new File(lmPath);

		if (lmFile.exists()){
			System.out.println("language model file " + lmPath + " exists");
			try {
				lm = (LanguageModel) Tools.deserialize(lmPath);
			} catch (Exception e) {
				System.out.println("language model file found but can't be opened");
				System.out.println("the file may be corrupted you may want to delete it and re-initialize the model");
				e.printStackTrace();
				System.exit(-1);
			}
			if (lm.getBeginnings()==null) {
				System.out.println("no beginning words!");
				System.exit(-1);
			}
		}
		else{
			//No saved LM file - INIT a new LM
			System.out.println("making new language model file");
			lm = new LanguageModel(N);
			//TODO get only RSS docs
			List<Doc> newDocs = DBUtil.getAllRssDocs(true);

			//add ALL data to LM
			for (Doc d: newDocs){
				System.out.println("\nsending to LM: " + d.title);
				lm.handle(d.title);
				System.out.println("\nsending to LM: " + d.content);
				lm.handle(d.content);
			}

			//serialize language model
			try {
				Tools.serialize(lm, lmPath);
			} catch (Exception e) {
				System.out.println("Couldn't reserialize updated language model");
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
	public static void main(String args[]){
		setup(args);
		while(true) loop();

		
	}

	private static List<String> generateNextLine(LanguageModel lm,
			List<String> prevWords, boolean isLastLine) {
		List<String> nextLine;
		//System.out.println("prevWords: " + prevWords);
		if ( lm.isEndWord(prevWords.get(prevWords.size()-1) ))
			nextLine = lm.generateSyllables(8);

		else {
			String last = prevWords.get(prevWords.size()-1); //get last word in last phrase
			List<String> next = lm.generatePhrase(2, last);
			//System.out.println("next: " + next);
			if (next.size() > 1){
				//System.out.println("size large enough");
				nextLine = lm.generateSyllables(8, next.get(next.size()-1));
				if (nextLine.isEmpty()) nextLine = lm.generateSyllables(8);
			}
			else{
				//System.out.println("size too small");
				nextLine = lm.generateSyllables(8);
			}
		}
		if (isLastLine){
			//System.out.println("is last line");
			if (! lm.isEndWord(nextLine.get(nextLine.size()-1) )){
				//System.out.println("not properly resolved");
				nextLine = lm.resolve(nextLine);
			}
		}
		//System.out.println(nextLine);
		return nextLine;
	}

}
