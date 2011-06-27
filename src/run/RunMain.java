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

	public static void main(String args[]){
		////////////////////////////////////////
		//////// DO ONCE ON START  ////////////
		///////////////////////////////////////

		//check for a previous saved language (markov) model
		//if it exists deserialize and load it.
		String lmPath = args[0];
		System.out.println("using " + lmPath);
		int N = Integer.parseInt(args[1]);
		File lmFile = new File(lmPath);
		LanguageModel lm = null; 

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

		/////////////////////////////////
		////  LOOP  /////////////////////
		/////////////////////////////////
		//while (true){
		for (int i=0; i<4; i++){
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

				if (newDocs.size()==0) {
					System.out.println("we have NO unread docs");
				}

				//delete file "NEW"
				flag.delete();

				//add new data to LM
				for (Doc d: newDocs){
					System.out.println("\nsending to LM: " + d.title);
					lm.handle(d.title); 
					System.out.println("\nsending to LM: " + d.content);
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
			else System.out.println("no new data to read");

			//generate text using pluggable method
			
			GenerateSableFile sable = new GenerateSableFile();
			List<String> words1 = lm.generateSyllables(8);
			List<String> words2 = lm.generateSyllables(8);
			List<String> words3 = lm.generateSyllables(8);
			List<String> words4 = lm.generateSyllables(8);
			System.out.println(words1);
			System.out.println(words2);
			System.out.println(words3);
			System.out.println(words4);
			
			try {
				//sable.generate(words1, "tospeak.sable");
				sable.generate(words1, words2, words3, words4, "tospeak.sable");
				System.out.println("generated: tospeak.sable");
			} catch (IOException e) {
				e.printStackTrace();
			}

			//speak!
			int success = Speak.festivalSpeak("tospeak.sable");
			System.out.println("speaking result " + success);
		}
	}
	
}
