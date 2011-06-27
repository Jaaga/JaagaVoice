package speech;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

import rita.RiAnalyzer;

import nlp.LanguageModel;

import util.Tools;

public class GenerateSableFile extends PApplet{
	private String sableHeader = "<?xml version=\"1.0\"?><!DOCTYPE SABLE PUBLIC \"-//SABLE//DTD SABLE speech mark up//EN\" \"Sable.v0_2.dtd\"[]>\n<SABLE>\n<SPEAKER NAME=\"jaaga\">\n";

	public void generate(List<String> words, String pathname) throws IOException{
		String content = "";
		RiAnalyzer ra = new RiAnalyzer(this);
		
		int sylCounter = 0;
		for (String w: words){
			try {
				ra.analyze(w);
				String syl = ra.getSyllables();
				sylCounter += LanguageModel.countSyllables(syl);
			}
			catch (Exception e){
				e.printStackTrace();
				sylCounter += 1;
			}
			
			if (sylCounter >=2 ){
				content += "<EMPH> " + w + " </EMPH><BREAK /> ";
				sylCounter=0;
			}
			else{
				content += w + " ";
			}
		}
		content += ".";
		String sableFooter = "\n</SPEAKER>\n</SABLE>"; //TODO add end speaker here
	
		Tools.writeFile(pathname, sableHeader + content + sableFooter);
	}
	
	public void generate(List<String> words1, List<String> words2, List<String> words3, 
			List<String> words4, String pathname) throws IOException{

		String content = "";
		RiAnalyzer ra = new RiAnalyzer(this);
		
		int sylCounter = 0;
		ArrayList <List<String>> words = new ArrayList <List<String>>();
		words.add(words1);
		words.add(words2);
		words.add(words3);
		words.add(words4);
		
		for (List<String> wordList: words){
			for (String w: wordList){
				try {
					ra.analyze(w);
					String syl = ra.getSyllables();
					sylCounter += LanguageModel.countSyllables(syl);
				}
				catch (Exception e){
					e.printStackTrace();
					sylCounter += 1;
				}

				if (sylCounter >=2 ){
					content += "<EMPH> " + w + " </EMPH><BREAK LEVEL=\"MEDIUM\"/> ";
					sylCounter=0;
				}
				else{
					content += w + " ";
				}
			}
			content += "<BREAK LEVEL=\"LARGE\"/>\n";
		}

		String sableFooter = "\n</SPEAKER>\n</SABLE>"; 
	
		Tools.writeFile(pathname, sableHeader + content + sableFooter);
	}
}
