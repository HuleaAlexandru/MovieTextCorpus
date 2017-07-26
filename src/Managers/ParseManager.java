package Managers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import MovieUnits.Script;
import MovieUnits.Subtitle;
import Utilities.Constants;
import Utilities.Utilities;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class ParseManager {
	private List<String> scriptNames;
	private List<String> subtitleNames;
	private List<String> movieNames;
	private List<Subtitle> subtitles;
	private List<Script> scripts;
	private StanfordCoreNLP sentenceSplitter;
	
	public ParseManager() {
		Utilities.log("\n[" + Constants.INFO + "] " + "ParseManager\n");
		this.scriptNames = new ArrayList<String>();
		this.subtitleNames = new ArrayList<String>();
		this.movieNames = new ArrayList<String>();
		this.subtitles = new ArrayList<Subtitle>();
		this.scripts = new ArrayList<Script>();
		Properties proprieties = new Properties();
		proprieties.put("annotators", "tokenize, ssplit");
		this.sentenceSplitter = new StanfordCoreNLP(proprieties);
	}
	
	public ParseManager(List<String> scriptsName, List<String> subtitlesName, List<String> movieNames) {
		Utilities.log("\n[" + Constants.INFO + "] " + "ParseManager\n");
		this.scriptNames = new ArrayList<String>(scriptsName);
		this.subtitleNames = new ArrayList<String>(subtitlesName);
		this.movieNames = new ArrayList<String>(movieNames);
		this.subtitles = new ArrayList<Subtitle>();
		this.scripts = new ArrayList<Script>();
		Properties proprieties = new Properties();
		proprieties.put("annotators", "tokenize, ssplit");
		this.sentenceSplitter = new StanfordCoreNLP(proprieties);
	}
	
	public List<Subtitle> getSubtitles() {
		return this.subtitles;
	}

	public List<Script> getScripts() {
		return this.scripts;
	}
	
	private boolean addScript(Script script) {
		return this.scripts.add(script);
	}
	
	private boolean addSubtitle(Subtitle subtitle) {
		return this.subtitles.add(subtitle);
	}
	
	public void start() {
		int numberOfMovies = this.scriptNames.size();
		String step3Path = Constants.STEP3_FOLDER;
		String step3SubtitlePath = Constants.STEP3_SUBTITLES_FOLDER;
		String step3ScriptPath = Constants.STEP3_SCRIPTS_FOLDER;
		
		Utilities.createFolder(step3Path);
		Utilities.createFolder(step3SubtitlePath);
		Utilities.createFolder(step3ScriptPath);
		
		for (int i = 0; i < numberOfMovies; i++) {
			String scriptOutputPath = Constants.STEP3_SCRIPTS_FOLDER + File.separator + this.scriptNames.get(i);
			if (!(new File(scriptOutputPath).exists())) {
				Utilities.log("[" + Constants.INFO + "] " + "Parse " + this.movieNames.get(i) + " script file\n");
				String scriptPath = Constants.STEP2_SCRIPTS_FOLDER + File.separator + this.scriptNames.get(i);
				String scriptContent = Utilities.readFile(scriptPath);
				this.addScript(new Script(this.scriptNames.get(i), scriptContent, this.sentenceSplitter));
				Utilities.writeFile(this.scripts.get(this.scripts.size() - 1).toString(), scriptOutputPath);
			}
			else {
				Utilities.log("[" + Constants.INFO + "] " + "Import " + this.movieNames.get(i) + " script file\n");
				Script script = new Script(this.scriptNames.get(i), this.sentenceSplitter);
				script.importScriptFromFile(scriptOutputPath);
				this.addScript(script);
			}
			
			String subtitleOutputPath = Constants.STEP3_SUBTITLES_FOLDER + File.separator + this.subtitleNames.get(i);
			if (!(new File(subtitleOutputPath).exists())) {
				Utilities.log("[" + Constants.INFO + "] " + "Parse " + this.movieNames.get(i) + " subtitle file\n");
				String subtitlePath = Constants.STEP2_SUBTITLES_FOLDER + File.separator + this.subtitleNames.get(i);
				String subtitleContent = Utilities.readFile(subtitlePath);
				this.addSubtitle(new Subtitle(this.subtitleNames.get(i), subtitleContent, this.sentenceSplitter));
				Utilities.writeFile(this.subtitles.get(this.subtitles.size() - 1).toString(), subtitleOutputPath);
			}
			else {
				Utilities.log("[" + Constants.INFO + "] " + "Import " + this.movieNames.get(i) + " subtitle file\n");
				Subtitle subtitle = new Subtitle(this.subtitleNames.get(i), this.sentenceSplitter);
				subtitle.importSubtitleFromFile(subtitleOutputPath);
				this.addSubtitle(subtitle);
			}
		}
	}
}
