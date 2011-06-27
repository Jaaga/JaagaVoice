package speech;

import util.RunSystemCommand;;

public class Speak {
	public static int festivalSpeak(String filename){
		return RunSystemCommand.runSystemCommand("festival --tts " + filename);
	}
}
