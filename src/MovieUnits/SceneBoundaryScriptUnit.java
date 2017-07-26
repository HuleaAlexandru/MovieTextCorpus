package MovieUnits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utilities.Constants;
import Utilities.Utilities;

public class SceneBoundaryScriptUnit extends ScriptUnit {

	public static Map<String, Integer> wordCount = new HashMap<String, Integer>();
	private List<String> metaData;
	private String time;
	private String location;
	
	public SceneBoundaryScriptUnit(int index, String text) {
		super(index, text);
		this.metaData = new ArrayList<String>();
		this.time = "";
		this.location = "";
	}
	
	@Override
	public String getDetails() {
		StringBuilder result = Utilities.resultsGenerator("Index", this.index + "");
		result.append(Utilities.resultsGenerator("Text", this.text));
		result.append(Utilities.resultsGenerator("Time", this.time));
		result.append(Utilities.resultsGenerator("Location", this.location));
		StringBuilder metaDataBuilder = new StringBuilder();
		for (String meta: this.metaData) {
			metaDataBuilder.append(Constants.PARANTHESIS_OPEN);
			metaDataBuilder.append(meta);
			metaDataBuilder.append(Constants.PARANTHESIS_CLOSE);
			metaDataBuilder.append(" ");
		}
		result.append(Utilities.resultsGenerator("MetaData", metaDataBuilder.toString()));
		result.append("\n");
		
		return result.toString();
	}

	private String sanitize(String text) {
		int start = 0;
		while (true) {
			int openParanthesis = text.indexOf("(", start);
			int closedParanthesis = text.indexOf(")", openParanthesis + 1);
			if (openParanthesis == -1 || closedParanthesis == -1) {
				break;
			}
			String subText = text.substring(openParanthesis + 1, closedParanthesis);
			boolean goodString = false;
			for (int i = 0; i < subText.length(); i++) {
				if (Character.isAlphabetic(subText.charAt(i)) || Character.isDigit(subText.charAt(i))) {
					goodString = true;
					break;
				}
			}
			if (!goodString) {
				text = text.substring(0, openParanthesis) + text.substring(closedParanthesis + 1);
			}
			start = openParanthesis + 1;
		}
		while (true) {
			int first = text.indexOf("-");
			int last = text.lastIndexOf("-");
			if (first == -1 && last == -1) {
				break;
			}
			boolean foundFirst = false;
			boolean foundLast = false;
			if (first != -1) {
				for (int i = 0; i < first; i++) {
					if (Character.isAlphabetic(text.charAt(i)) || Character.isDigit(text.charAt(i))) {
						foundFirst = true;
						break;
					}
				}
				if (!foundFirst) {
					text = text.substring(first + 1);
				}
			}
			if (last != -1) {
				for (int i = text.length() - 1; i > last; i--) {
					if (Character.isAlphabetic(text.charAt(i)) || Character.isDigit(text.charAt(i))) {
						foundLast = true;
						break;
					}
				}
				if (!foundLast) {
					text = text.substring(0, last);
				}
			}
			if (foundFirst && foundLast) {
				break;
			}
		}
		return text;
	}
	
	@Override
	public void setTextualDetails() {
		String text = new String(this.text);
//		StringBuilder word = new StringBuilder();
//		for (int i = 0; i < text.length(); i++) {
//			if (!Character.isAlphabetic(text.charAt(i))) {
//				String key = word.toString().trim();
//				if (wordCount.containsKey(key)) {
//					wordCount.put(key, wordCount.get(key) + 1);
//				}
//				else {
//					wordCount.put(key, 1);
//				}
//				word.setLength(0);
//				continue;
//			}
//			word.append(text.charAt(i));
//		}
//		if (word.length() != 0) {
//			String key = word.toString().trim();
//			if (wordCount.containsKey(key)) {
//				wordCount.put(key, wordCount.get(key) + 1);
//			}
//			else {
//				wordCount.put(key, 1);
//			}
//		}
		for (String meta: Constants.SCENE_BOUNDARIES_META_DATA) {
			if (text.contains(meta)) {
				this.metaData.add(meta);
				int occur = text.indexOf(meta);
				text = text.substring(0, occur) + text.substring(occur + meta.length());
			}
		}
		String timeAndLocation = this.sanitize(text);
		for (String timeString: Constants.SCENE_BOUNDARIES_TIME) {
			if (text.contains(timeString)) {
				int occur = text.indexOf(timeString);
				int left = text.lastIndexOf("-", occur - 1);
				int right = text.indexOf("-", occur + 1);
				if (left == -1) {
					left = 0;
				}
				if (right == -1) {
					right = text.length() - 1;
				}
				this.time = text.substring(left, right + 1);
				if (this.time.startsWith("-")) {
					this.time = this.time.substring(1);
				}
				if (this.time.endsWith("-")) {
					this.time = this.time.substring(0, this.time.length() - 1);
				}
				this.time = this.time.trim().replaceAll(" +", " ");
				this.time = this.sanitize(this.time);
				
				String leftSide = text.substring(0, left);
				String rightSide = text.substring(right + 1);
				boolean hasLettersOrDigits = false;
				for (int i = 0; i < leftSide.length(); i++) {
					if (Character.isAlphabetic(leftSide.charAt(i)) || Character.isDigit(leftSide.charAt(i))) {
						hasLettersOrDigits = true;
						break;
					}
				}
				if (!hasLettersOrDigits) {
					leftSide = "";
				}
				hasLettersOrDigits = false;
				for (int i = 0; i < rightSide.length(); i++) {
					if (Character.isAlphabetic(rightSide.charAt(i)) || Character.isDigit(rightSide.charAt(i))) {
						hasLettersOrDigits = true;
						break;
					}
				}
				if (!hasLettersOrDigits) {
					rightSide = "";
				}
				text = leftSide + rightSide;
			}
		}
		text = text.replaceAll(" +", " ");
		this.location = text;
		this.location = this.sanitize(this.location);
		
		boolean found = false;
		for (String s: Constants.SCENE_BOUNDARY_ARRAY) {
			if (this.location.contains(s)) {
				found = true;
				break;
			}
		}
		if (!found) {
			this.time = "";
			for (String timeString: Constants.SCENE_BOUNDARIES_TIME) {
				if (timeAndLocation.contains(timeString)) {
					this.time += timeString + " ";
					int occur = timeAndLocation.indexOf(timeString);
					timeAndLocation = timeAndLocation.substring(0, occur) + 
							timeAndLocation.substring(occur + timeString.length());
				}
			}
			this.location = timeAndLocation;
			this.time = this.time.replaceAll("<|>|#", "");
			this.time = this.time.trim().replaceAll(" +", " ");
			this.location = this.location.replaceAll("<|>|#", "");
			this.location = this.location.trim().replaceAll(" +", " ");
		}
		else {
			this.time = this.time.replaceAll("<|>|#", "");
			this.time = this.time.trim().replaceAll(" +", " ");
			this.location = this.location.replaceAll("<|>|#", "");
			this.location = this.location.trim().replaceAll(" +", " ");
		}
		
		//Utilities.log("Location = " + this.location + " time = " + this.time + " meta = " + this.metaData + "\n");
	}

	@Override
	public void setStructureDetails(List<ScriptUnit> scriptUnits) {
		return;
	}

	@Override
	public String toString() {
		StringBuilder result = Utilities.resultsGenerator("SCENE_BOUNDARY", this.text);
		
		if (startTime != null && endTime != null) {
			StringBuilder timeResult = this.getTime();
			timeResult.append(result);
			return timeResult.toString();
		}
		return result.toString();
	}
}
