package Managers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Utilities.Constants;
import Utilities.Utilities;

public class SanitizerManager {
	private List<String> scriptNames;
	private List<String> subtitleNames;
	private List<String> movieNames;
	
	public SanitizerManager() {
		Utilities.log("\n[" + Constants.INFO + "] " + "SanitizeManager\n");
		this.scriptNames = new ArrayList<String>();
		this.subtitleNames = new ArrayList<String>();
		this.movieNames = new ArrayList<String>();
	}
	
	public SanitizerManager(List<String> scriptsName, List<String> subtitlesName, List<String> movieNames) {
		Utilities.log("\n[" + Constants.INFO + "] " + "SanitizeManager\n");
		this.scriptNames = new ArrayList<String>(scriptsName);
		this.subtitleNames = new ArrayList<String>(subtitlesName);
		this.movieNames = new ArrayList<String>(movieNames);
	}
	
	public List<String> getScriptNames() {
		return this.scriptNames;
	}
	
	public List<String> getSubtitleNames() {
		return this.subtitleNames;
	}
	
	private String sanitizeScript(String htmlFile, String startMatch, String endMatch,
			String regexToReplace, List<String> htmlTags) {
		Integer startIndex = htmlFile.lastIndexOf(startMatch);
		if (startIndex == -1) {
			startIndex = htmlFile.lastIndexOf(startMatch.toUpperCase());
		}
		Integer endIndex = htmlFile.indexOf(endMatch);
		if (endIndex == -1) {
			endIndex = htmlFile.indexOf(endMatch.toUpperCase());
		}
		if (startIndex == -1 && endIndex == -1) {
			return htmlFile;
		}
		if (endIndex == -1) {
			endIndex = htmlFile.length();
		}
		if (startIndex >= endIndex) { 
			return htmlFile;
		}
		String text = htmlFile.substring(startIndex + startMatch.length(), endIndex);
		String result = text.replaceAll(regexToReplace, "");
		for (String htmlTag: htmlTags) {
			while(true) {
				int openTag = result.indexOf("<" + htmlTag);
				if (openTag == -1) {
					break;
				}
				int closeTag = result.indexOf(">", openTag + 1);
				if (closeTag == -1) {
					break;
				}
				result = result.substring(0, openTag) + result.substring(closeTag + 1);
			}
		}
		
		result = result.replaceAll("(((\r)*\n))(((\r)*\n))+", "\n\n");
		
		StringBuilder resultString = new StringBuilder();
		String[] lines = result.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = new String(lines[i]);
			line = line.trim();
			StringBuilder start = new StringBuilder();
			int nrDigits = 0;
			for (int j = 0; j < line.length() && line.charAt(j) != ' ' && line.charAt(j) != '.'; j++) {
				start.append(line.charAt(j));
				if (Character.isDigit(line.charAt(j))) {
					nrDigits++;
				}
			}
			String startString = start.toString();
			if (nrDigits > 0 && line.length() >= startString.length() * 2 && 
					nrDigits >= startString.length() - 1 && line.endsWith(startString)) {
				line = lines[i].substring(0, lines[i].lastIndexOf(startString));
				int endOfString = line.length();
				for (int j = line.length() - 1; j >= 0; j--) {
					if (Character.isWhitespace(line.charAt(j))) {
						endOfString--;
						continue;
					}
					break;
				}
				line = line.substring(0, endOfString);
				resultString.append(line);
				resultString.append("\n");
			}
			else {
				resultString.append(lines[i]);
				resultString.append("\n");
			}
		}
		
		return resultString.toString();
	}
	
	private String sanitizeSubtitle(String content, String regexToReplace) {
		String result = content.replaceAll(regexToReplace, "");
		result = result.trim();
		result = result.replaceAll("((\n)( )*)((\n)( )*)+", "\n\n");
		return result;
	}
	
	public void start() {
		int numberOfMovies = this.scriptNames.size();
		
		String step2Path = Constants.STEP2_FOLDER;
		String scriptsPath = Constants.STEP2_SCRIPTS_FOLDER;
		String subtitlesPath = Constants.STEP2_SUBTITLES_FOLDER;
		
		Utilities.createFolder(step2Path);
		Utilities.createFolder(scriptsPath);
		Utilities.createFolder(subtitlesPath);
		
		for (int i = 0; i < numberOfMovies; i++) {
			String scriptOutputFile = scriptsPath + File.separator + this.scriptNames.get(i);
			if (!(new File(scriptOutputFile).exists())) {
				Utilities.log("[" + Constants.INFO + "] " + "Sanitize " + this.movieNames.get(i) + " script file\n");
				String scriptInputFile = Constants.STEP1_SCRIPTS_FOLDER + File.separator + this.scriptNames.get(i);
				String scriptContent = Utilities.readFile(scriptInputFile);
				String parsedScript = "";
				while (true) {
					List<String> htmlTags = new ArrayList<String>();
					htmlTags.add("FONT");
					parsedScript = this.sanitizeScript(scriptContent,  "<pre>", "</pre>", "<b>|</b>|<i>|</i>|<u>|</u>|<U>|</U>|<div>|</div>|<BR>|</FONT>|\\*", htmlTags);
					if (parsedScript.equals(scriptContent)) {
						break;
					}
					scriptContent = parsedScript;
				}
				Utilities.writeFile(parsedScript, scriptOutputFile);
			}
			else {
				Utilities.log("[" + Constants.INFO + "] " + "Import " + this.movieNames.get(i) + " script file\n");
			}
			String subtitleOutputFile = subtitlesPath + File.separator + this.subtitleNames.get(i);
			if (!(new File(subtitleOutputFile).exists())) {
				Utilities.log("[" + Constants.INFO + "] " + "Sanitize " + this.movieNames.get(i) + " subtitle file\n");
				String subtitleInputFile = Constants.STEP1_SUBTITLES_FOLDER + File.separator + this.subtitleNames.get(i);
				String subtitleContent = Utilities.readFile(subtitleInputFile);
				String parsedSubtitle = this.sanitizeSubtitle(subtitleContent, "<[^>]*>");
				Utilities.writeFile(parsedSubtitle, subtitleOutputFile);
			}
			else {
				Utilities.log("[" + Constants.INFO + "] " + "Import " + this.movieNames.get(i) + " subtitle file\n");
			}
		}	
	}
}