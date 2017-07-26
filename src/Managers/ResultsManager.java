package Managers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import MovieUnits.CharacterNameScriptUnit;
import MovieUnits.DialogueScriptUnit;
import MovieUnits.MetaDataScriptUnit;
import MovieUnits.SceneBoundaryScriptUnit;
import MovieUnits.SceneDescriptionScriptUnit;
import MovieUnits.Script;
import MovieUnits.ScriptUnit;
import Utilities.Constants;
import Utilities.Utilities;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class ResultsManager {
	private List<Script> scripts;
	private int generateMask;
	private StanfordCoreNLP POSAnnotator;
	
	public ResultsManager(List<Script> scripts, int generateMask) {
		//Utilities.log("\n[" + Constants.INFO + "] " + "ResultsManager\n");
		this.scripts = scripts;
		this.generateMask = generateMask;
		Properties proprieties = new Properties();
		proprieties.setProperty("annotators", "tokenize, ssplit, pos");
		this.POSAnnotator = new StanfordCoreNLP(proprieties);
		this.importPosFiles();
		
	}
	
	private String getPos(String text) {
		StringBuilder result = new StringBuilder();
		Annotation annotation = new Annotation(text);
		this.POSAnnotator.annotate(annotation);
	
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for(CoreMap sentence: sentences) {
			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class);
				String pos = token.get(PartOfSpeechAnnotation.class);
				result.append(word);
				result.append(Constants.PARANTHESIS_OPEN);
				result.append(pos);
				result.append(Constants.PARANTHESIS_CLOSE);
				result.append(" ");
			}
		}
		return result.toString();
	}
	
	public void generateFiles() {
		String detailsFolder = Constants.DETAILS_FOLDER;
		Utilities.createFolder(detailsFolder);
	
		if ((this.generateMask & (1 << Constants.META_DATA)) != 0) {
			this.generateMetaDataDetails();
		}
		if ((this.generateMask & (1 << Constants.CHARACTER_NAME)) != 0) {
			this.generateCharacterNameDetails();
		}
		if ((this.generateMask & (1 << Constants.DIALOGUE)) != 0) {
			this.generateDialogueDetails();
		}
		if ((this.generateMask & (1 << Constants.SCENE_BOUNDARY)) != 0) {
			this.generateSceneBoundaryDetails();
		}
		if ((this.generateMask & (1 << Constants.SCENE_DESCRIPTION)) != 0) {
			this.generateSceneDescriptionDetails();
		}
		if ((this.generateMask & (1 << Constants.POS)) != 0) {
			this.generatePOSDetails();
		}
	}
	
	private void generateMetaDataDetails() {	
		for (Script script: this.scripts) {
			String scriptName = script.getScriptName();
			String detailsOutputPath = Constants.DETAILS_FOLDER + File.separator + scriptName + "_metaData";
			if (!(new File(detailsOutputPath).exists())) {
				StringBuilder result = new StringBuilder();
				List<ScriptUnit> scriptUnits = script.getScriptUnits();
				for (ScriptUnit unit: scriptUnits) {
					if (unit.isMetaData()) {
						result.append(unit.getDetails());
					}
				}
				Utilities.writeFile(result.toString(), detailsOutputPath);
			}
		}
	}
	
	private void generateCharacterNameDetails() {
		for (Script script: this.scripts) {
			String scriptName = script.getScriptName();
			String detailsOutputPath = Constants.DETAILS_FOLDER + File.separator + scriptName + "_characterName";
			if (!(new File(detailsOutputPath).exists())) {
				StringBuilder result = new StringBuilder();
				List<ScriptUnit> scriptUnits = script.getScriptUnits();
				for (ScriptUnit unit: scriptUnits) {
					if (unit.isCharacterName()) {
						result.append(unit.getDetails());
					}
				}
				Utilities.writeFile(result.toString(), detailsOutputPath);
			}
		}
	}

	private void generateDialogueDetails() {
		for (Script script: this.scripts) {
			String scriptName = script.getScriptName();
			String detailsOutputPath = Constants.DETAILS_FOLDER + File.separator + scriptName + "_dialogue";
			if (!(new File(detailsOutputPath).exists())) {
				StringBuilder result = new StringBuilder();
				
				List<ScriptUnit> scriptUnits = script.getScriptUnits();
				for (ScriptUnit unit: scriptUnits) {
					if (unit.isDialogue()) {
						result.append(unit.getDetails());
					}
				}
				Utilities.writeFile(result.toString(), detailsOutputPath);
			}
		}
	}
	
	private void generateSceneBoundaryDetails() {
		for (Script script: this.scripts) {
			String scriptName = script.getScriptName();
			String detailsOutputPath = Constants.DETAILS_FOLDER + File.separator + scriptName + "_sceneBoundary";
			if (!(new File(detailsOutputPath).exists())) {
				StringBuilder result = new StringBuilder();
				List<ScriptUnit> scriptUnits = script.getScriptUnits();
				for (ScriptUnit unit: scriptUnits) {
					if (unit.isSceneBoundary()) {
						result.append(unit.getDetails());
					}
				}
				Utilities.writeFile(result.toString(), detailsOutputPath);
			}
		}
	}

	private void generateSceneDescriptionDetails() {
		for (Script script: this.scripts) {
			String scriptName = script.getScriptName();
			String detailsOutputPath = Constants.DETAILS_FOLDER + File.separator + scriptName + "_sceneDescription";
			if (!(new File(detailsOutputPath).exists())) {
				StringBuilder result = new StringBuilder();	
				List<ScriptUnit> scriptUnits = script.getScriptUnits();
				for (ScriptUnit unit: scriptUnits) {
					if (unit.isSceneDescription()) {
						result.append(unit.getDetails());
					}
				}
				Utilities.writeFile(result.toString(), detailsOutputPath);
			}
		}
	}
	
	private void generatePOSDetails() {
		for (Script script: this.scripts) {
			String scriptName = script.getScriptName();
			String detailsOutputPath = Constants.DETAILS_FOLDER + File.separator + scriptName + "_pos";
			if (!(new File(detailsOutputPath).exists())) {
				List<ScriptUnit> annotatedUnits = new ArrayList<ScriptUnit>();
				StringBuilder result = new StringBuilder();
				List<ScriptUnit> scriptUnits = script.getScriptUnits();
				for (ScriptUnit unit: scriptUnits) {
					String text = this.getPos(unit.getText());
					if (unit.isCharacterName()) {
						annotatedUnits.add(new CharacterNameScriptUnit(unit.getIndex(), text));
						result.append(Utilities.resultsGenerator("CHARACTER_NAME", text));
					}
					else if (unit.isDialogue()) {
						annotatedUnits.add(new DialogueScriptUnit(unit.getIndex(), text));
						result.append(Utilities.resultsGenerator("DIALOGUE", text));
					}
					else if (unit.isMetaData() 
							//&& ((MetaDataScriptUnit)unit).isTransition()
							) {
						annotatedUnits.add(new MetaDataScriptUnit(unit.getIndex(), text));
						result.append(Utilities.resultsGenerator("META_DATA", text));
					}
					else if (unit.isSceneBoundary()) {
						annotatedUnits.add(new SceneBoundaryScriptUnit(unit.getIndex(), text));
						result.append(Utilities.resultsGenerator("SCENE_BOUNDARY", text));
					}
					else if (unit.isSceneDescription()) {
						annotatedUnits.add(new SceneDescriptionScriptUnit(unit.getIndex(), text));
						result.append(Utilities.resultsGenerator("SCENE_DESCRIPTION", text));
					}
				}
				Utilities.writeFile(result.toString(), detailsOutputPath);
				script.setAnnotatedUnits(annotatedUnits);
			}
		}
	}
	
	private void importPosFiles() {
		for (Script script: this.scripts) {
			List<ScriptUnit> annotatedUnits = new ArrayList<ScriptUnit>(); 
			String scriptName = script.getScriptName();
			String detailsOutputPath = Constants.DETAILS_FOLDER + File.separator + scriptName + "_pos";
			if ((new File(detailsOutputPath).exists())) {
				String fileContent = Utilities.readFile(detailsOutputPath);
				String[] tokens = fileContent.split("\n");
				for (int i = 0; i < tokens.length; i++) {
					String token = tokens[i];
					String scriptUnitType = token.substring(0, token.indexOf(":"));
					scriptUnitType = scriptUnitType.substring(scriptUnitType.indexOf(Constants.PARANTHESIS_OPEN) + 1, 
							scriptUnitType.indexOf(Constants.PARANTHESIS_CLOSE));
					String text = token.substring(token.indexOf(":") + 1).trim();
					
					if (scriptUnitType.equals("SCENE_BOUNDARY")) {
						annotatedUnits.add(new SceneBoundaryScriptUnit(i, text));
					}
					else if (scriptUnitType.equals("SCENE_DESCRIPTION")) {
						annotatedUnits.add(new SceneDescriptionScriptUnit(i, text));
					}
					else if (scriptUnitType.equals("CHARACTER_NAME")) {
						annotatedUnits.add(new CharacterNameScriptUnit(i, text));
					}
					else if (scriptUnitType.equals("DIALOGUE")) {
						annotatedUnits.add(new DialogueScriptUnit(i, text));
					}
					else if (scriptUnitType.equals("META_DATA")) {
						annotatedUnits.add(new MetaDataScriptUnit(i, text));
					}
				}
				script.setAnnotatedUnits(annotatedUnits);
			}
		}
	}
	
	public List<Script> getScripts() {
		return this.scripts;
	}
}