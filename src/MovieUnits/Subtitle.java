package MovieUnits;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import Utilities.Constants;
import Utilities.Utilities;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Subtitle {
	private String subtitleName;
	private List<SubtitleUnit> subtitleUnits;
	private StanfordCoreNLP sentenceSplitter;
	private int numberOfUnits;
	
	public Subtitle(String subtitleName, StanfordCoreNLP sentenceSplitter) {
		this.subtitleName = subtitleName;
		this.subtitleUnits = new ArrayList<SubtitleUnit>();
		this.sentenceSplitter = sentenceSplitter;
		this.numberOfUnits = -1;
	}
	
	public Subtitle(String subtitleName, String sequence, StanfordCoreNLP sentenceSplitter) {
		this.subtitleName = subtitleName;
		this.subtitleUnits = new ArrayList<SubtitleUnit>();
		this.sentenceSplitter = sentenceSplitter;
		this.numberOfUnits = -1;
		String[] tokens = sequence.split("(\r)*\n(\r)*\n");
		for (String token: tokens) {
			this.addSubtitleUnit(new SubtitleUnit(token, this.sentenceSplitter));
		}
	}
	
	public int getNumberOfUnits() {
		if (this.numberOfUnits != -1) {
			return this.numberOfUnits;
		}
		this.numberOfUnits = 0;
		for (int i = 0; i < this.subtitleUnits.size(); i++) {
			this.numberOfUnits += this.subtitleUnits.get(i).getText().size();
		}
		return this.numberOfUnits;
	}
	
	public String getSubtitleName() {
		return this.subtitleName;
	}
	
	public List<SubtitleUnit> getSubtitleUnits() {
		return this.subtitleUnits;
	}
	
	private boolean addSubtitleUnit(SubtitleUnit subtitleUnit) {
		return this.subtitleUnits.add(subtitleUnit);
	}
	
	public void importSubtitleFromFile(String filename) {
		String scriptContent = Utilities.readFile(filename);
		String[] tokens = scriptContent.split("\n");
		int firstIndex = Utilities.parseInt(tokens[0].split(":")[1].trim());
		for (int i = 0; i < (tokens.length + 1) / 5; i++) {
			String startTime = tokens[5 * i + 1].substring(tokens[5 * i + 1].indexOf(":") + 1).trim();
			String[] startTokens = startTime.split(":");
			Calendar start = Calendar.getInstance();
			start.set(0, 0, 0, Utilities.parseInt(startTokens[0]), Utilities.parseInt(startTokens[1]),
					Utilities.parseInt(startTokens[2]));
			
			String endTime = tokens[5 * i + 2].substring(tokens[5 * i + 2].indexOf(":") + 1).trim();
			String[] endTokens = endTime.split(":");
			Calendar end = Calendar.getInstance();
			end.set(0, 0, 0, Utilities.parseInt(endTokens[0]), Utilities.parseInt(endTokens[1]),
					Utilities.parseInt(endTokens[2]));
			
			List<String> text = new ArrayList<String>();
			String textLine = tokens[5 * i + 3].substring(tokens[5 * i + 3].indexOf(":") + 1).trim();
			while (true) {
				int index = textLine.indexOf(Constants.PARANTHESIS_CLOSE);
				if (index < 0) {
					break;
				}
				String sentence = textLine.substring(textLine.indexOf(Constants.PARANTHESIS_OPEN) + 1, index);
				text.add(sentence);
				if (textLine.length() <= index + 1) {
					break;
				}
				textLine = textLine.substring(index + 1);
			}
			
			this.subtitleUnits.add(new SubtitleUnit(firstIndex, start, end, text, this.sentenceSplitter));
			firstIndex++;
		}
	}
	 
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (SubtitleUnit s: this.subtitleUnits) {
			result.append(s.toString()).append("\n");
		}
		return result.toString();
	}
}
