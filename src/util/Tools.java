package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tecnick.htmlutils.htmlentities.HTMLEntities;

import rita.RiTa;

public class Tools {
	public static String replaceJunk(String input, String replacementText){
		//what is junk?
		String HTMLEntityRegex = "&#\\w+;";
		String URLregex = "((mailto:|(news|(ht|f)tp(s?))://){1}\\S+)";
		String HashRegex = "\\B#\\w\\w+";
		String ReplyRegex = "(RT |rt |via )?\\B@\\w\\w+";
		String rtRegex = " rt| RT";
		
		//remove HTML entities
		Pattern p1 = Pattern.compile(HTMLEntityRegex);
		Matcher m1 = p1.matcher(input);
		while (m1.find()){
			String found = m1.group();
			//System.out.println("found " + found);
			String replacement = HTMLEntities.unhtmlentities(found);
			//System.out.println("replacing with: " + replacement);
			input = input.replaceAll(found, replacement);
			System.out.println(input);
			m1 = p1.matcher(input);
		}
		
		String result1 = m1.replaceAll(" ");
		
		//remove URLs
		Pattern p2 = Pattern.compile(URLregex);
		Matcher m2 = p2.matcher(result1);
		String result2 = m2.replaceAll(" ");
		//remove hashtags
		Pattern p3 = Pattern.compile(HashRegex);
		Matcher m3 = p3.matcher(result2);
		String result3 = m3.replaceAll(replacementText);
		//remove replies
		Pattern p4 = Pattern.compile(ReplyRegex);
		Matcher m4 = p4.matcher(result3);
		String result4 = m4.replaceAll(replacementText);
		
		//remove rt from tweets
		Pattern p5 = Pattern.compile(rtRegex);
		Matcher m5 = p5.matcher(result4);
		String result5 = m5.replaceAll(replacementText);
		
		return result5;
	}
	
	public static List<String> getSentences(String input){
		List<String> retList = new ArrayList<String>();
		String [] sents = RiTa.splitSentences(input);
		for (String s: sents){
			retList.add(s);
		}
		return retList;
	}
	
	public static void addToFile(String filename, String text) throws IOException{
		File out = new File(filename);
		PrintWriter pw = null;
		if (out.exists()){
			pw = new PrintWriter(new FileWriter(filename, true));
		}
		else {
			pw = new PrintWriter(new FileWriter(filename));
		}
		
		pw.println(text);
		pw.close();
	}
	
	public static void writeFile(String filename, String message) throws IOException{
		PrintWriter pw = new PrintWriter(new FileWriter(filename));
		pw.write(message);
		pw.close();
	}

	public static void createFlagFile(String filename) throws IOException {
		File out = new File(filename);
		out.createNewFile();
	}
	
	public static void serialize(Object obj, String path) throws FileNotFoundException, IOException{
		ObjectOutputStream objout = new ObjectOutputStream (new FileOutputStream(path));
		objout.writeObject(obj);
		objout.close();
		System.out.println("wrote " + obj);
	}
	public static Object deserialize(String path) throws IOException, ClassNotFoundException{
		ObjectInputStream objin = new ObjectInputStream(new FileInputStream(path));
		Object obj = objin.readObject();
		objin.close();
		System.out.println("read " + obj);
		return obj;
	}

	public static String stripPunctuation(String sent) {
		String retString;
		//remove .,!?:-
		String punct = "[\\_#$&;.,:!?\"“”+()-/]";
		Pattern p1 = Pattern.compile(punct);
		Matcher m1 = p1.matcher(sent);
		retString = m1.replaceAll("");
		retString = retString.replace("[", " ");
		retString = retString.replace("]", " ");
		retString = retString.replace("@", " ");
		return retString;
	}

	public static String stripSpecialChars(String sent) {
		//System.out.println("Input to normalization: " + sent);
		String sent2  = Normalizer.normalize(sent, Normalizer.Form.NFD);
		String retString = sent2.replaceAll("[^\\p{ASCII}]", " ");
		//System.out.println("Output after normalization: " + retString);
		return retString;
	}
}
