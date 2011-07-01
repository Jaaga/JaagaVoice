package speech;

import util.RunSystemCommand;
/**
 * 
 * @author Heather Dewey-hagborg
 * @version 1.0
 * This class just encapsulates the call to the TTS engine. could easily use a different engine here.
 *
 */
public class Speak {
	public static int festivalSpeak(String filename){
		return RunSystemCommand.runSystemCommand("festival --tts " + filename);
	}
}
