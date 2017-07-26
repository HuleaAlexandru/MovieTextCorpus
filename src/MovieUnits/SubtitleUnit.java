package MovieUnits;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import Utilities.Constants;
import Utilities.Utilities;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class SubtitleUnit {
	private int index;
	private Calendar startTime;
	private Calendar endTime;
	private List<String> text;
	private StanfordCoreNLP sentenceSplitter;
	
	public SubtitleUnit(int index, Calendar startTime, Calendar endTime, List<String> text, StanfordCoreNLP sentenceSplitter) {
		this.index = index;
		this.startTime = startTime;
		this.endTime = endTime;
		this.text = new ArrayList<String>(text);
		this.sentenceSplitter = sentenceSplitter;
	}
	
	public SubtitleUnit(String sequence, StanfordCoreNLP sentenceSplitter) {
		this.sentenceSplitter = sentenceSplitter;
		String[] tokens = sequence.split("\n");
		
		this.index = Utilities.parseInt(tokens[0]);
		
		String[] timeLine = tokens[1].split("-->");
		String start = timeLine[0].substring(0, timeLine[0].indexOf(","));
		String[] time = start.split(":");
		this.startTime = Calendar.getInstance();
		this.startTime.set(0, 0, 0, Utilities.parseInt(time[0]), Utilities.parseInt(time[1]),
				Utilities.parseInt(time[2]));
		
		String end = timeLine[1].substring(0, timeLine[1].indexOf(","));
		time = end.split(":");
		this.endTime = Calendar.getInstance();
		this.endTime.set(0, 0, 0, Utilities.parseInt(time[0]), Utilities.parseInt(time[1]),
				Utilities.parseInt(time[2]));
		
		String subtitleText = "";
		for (int i = 2; i < tokens.length; i++) {
			subtitleText += tokens[i] + " ";
		}
		//List<String> sentences = Utilities.getSentences(subtitleText);
		List<String> sentences = Utilities.getSentencesBasedOnStanfordNLP(subtitleText, this.sentenceSplitter);
		this.text = new ArrayList<String>(sentences);
	}
	
	@Override
	public String toString() {
		StringBuilder result = Utilities.resultsGenerator("Index", this.index + "");
		
		result.append(Constants.PARANTHESIS_OPEN);
		result.append("StartTime");
		result.append(Constants.PARANTHESIS_CLOSE);
		result.append(": ");
		result.append(this.startTime.get(Calendar.HOUR));
		result.append(":");
		result.append(this.startTime.get(Calendar.MINUTE));
		result.append(":");
		result.append(this.startTime.get(Calendar.SECOND));
		result.append("\n");
		
		result.append(Constants.PARANTHESIS_OPEN);
		result.append("EndTime");
		result.append(Constants.PARANTHESIS_CLOSE);
		result.append(": ");
		result.append(this.endTime.get(Calendar.HOUR));
		result.append(":");
		result.append(this.endTime.get(Calendar.MINUTE));
		result.append(":");
		result.append(this.endTime.get(Calendar.SECOND));
		result.append("\n");
		
		result.append(Constants.PARANTHESIS_OPEN);
		result.append("Text");
		result.append(Constants.PARANTHESIS_CLOSE);
		result.append(": ");
		
		for (String sentence: text) {
			result.append(Constants.PARANTHESIS_OPEN);
			result.append(sentence);
			result.append(Constants.PARANTHESIS_CLOSE);
			result.append(" ");
		}
		result.append("\n");
		return result.toString();
	}

	public int getIndex() {
		return this.index;
	}

	public Calendar getStartTime() {
		return this.startTime;
	}

	public Calendar getEndTime() {
		return this.endTime;
	}

	public List<String> getText() {
		return this.text;
	}
}