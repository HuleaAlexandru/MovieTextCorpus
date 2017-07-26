package MovieUnits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import Utilities.Constants;
import Utilities.Utilities;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Script {
	private String scriptName;
	private List<ScriptUnit> scriptUnits;
	private List<ScriptUnit> annotatedUnits;
	private int[] upperCaseLine;
	private int[] indentationLevel;
	private int[] numberOfWords;
	private HashMap<Integer, Integer> indentationForTypes;
	private int numberOfDialogueUnits;
	private List<ScriptUnit> dialogueUnits;
	private StanfordCoreNLP sentenceSplitter;
	private boolean errors;
	
	public Script(String scriptName, StanfordCoreNLP sentenceSplitter) {
		this.scriptName = scriptName;
		this.scriptUnits = new ArrayList<ScriptUnit>();
		this.annotatedUnits = new ArrayList<ScriptUnit>();
		this.numberOfDialogueUnits = -1;
		this.dialogueUnits = new ArrayList<ScriptUnit>();
		this.sentenceSplitter = sentenceSplitter;
		this.errors = false;
	}
	
	public Script(String scriptName, String sequence, StanfordCoreNLP sentenceSplitter) {
		String[] tokens = sequence.split("(\r)*(\n)|(\r)+(\n)*");
		
		this.scriptName = scriptName;
		this.scriptUnits = new ArrayList<ScriptUnit>();
		this.annotatedUnits = new ArrayList<ScriptUnit>();
		this.upperCaseLine = new int[tokens.length];
		this.indentationLevel = new int[tokens.length];
		this.numberOfWords = new int[tokens.length];
		this.indentationForTypes = new HashMap<Integer, Integer>(); 
		this.numberOfDialogueUnits = -1;
		this.dialogueUnits = new ArrayList<ScriptUnit>();
		this.sentenceSplitter = sentenceSplitter;
		this.errors = false;
		
		for (int i = 0; i < tokens.length; i++) {
			this.indentationLevel[i] = Utilities.getIndentationLevel(tokens[i]);
			tokens[i] = tokens[i].trim();
			Integer upperCaseResult = Utilities.allUpperCase(tokens[i]);
			if (upperCaseResult == Constants.UPPERCASE) {
				this.upperCaseLine[i] = Constants.UPPERCASE;
				this.numberOfWords[i] = tokens[i].split(" ").length;
			}
			else {
				this.upperCaseLine[i] = upperCaseResult;
				this.numberOfWords[i] = -1;
			}
			this.scriptUnits.add(new UnknownScriptUnit(i, tokens[i]));
		}
		
		this.getEmptyLines();
		ArrayList<Integer> bestIndentation = this.computeIndentationLevel();
		
		if (bestIndentation != null) {
			Utilities.log("[" + Constants.INFO + "] " + this.scriptName + " Indented script\n");
			this.setTypesBasedOnIndentation();
		}
		else {
			Utilities.log("[" + Constants.INFO + "] " + this.scriptName + " Unindented script\n");			
			this.setTypesWithoutIndentation();
		}
		this.generateScriptUnitFromSentences();
		this.repairScriptUnits();
		this.setDetails();
	}
	
	/**
	 * This method use the next functions to set the types based on indentation 
	 */
	private void setTypesBasedOnIndentation() {
		this.getMetaBasedOnArray();
		
		Integer sceneBoundariesIndentation = this.getSceneBoundariesBasedOnArray(true);
		if (sceneBoundariesIndentation == Constants.ERROR_CODE) {
			this.errors = true;
			Utilities.log("[" + Constants.ERROR + "] " + "Scene boundary indentation error: " + this.scriptName + "\n");
			//System.exit(1);
		}
		this.indentationForTypes.put(Constants.SCENE_BOUNDARY, sceneBoundariesIndentation);
		
		Integer characterNameIndentation = this.getCharacterNameBasedOnArray(true);
		if (characterNameIndentation == Constants.ERROR_CODE) {
			this.errors = true;
			Utilities.log("[" + Constants.ERROR + "] " + "Character name indentation error: " + this.scriptName + "\n");
			//System.exit(1);
		}
		this.indentationForTypes.put(Constants.CHARACTER_NAME, characterNameIndentation);
		
		this.setMetaInTheBegining();
		
		this.setCharacterNameBasedOnIndentation();
		
		Integer dialogueIndentation = this.getDialogueIndentation();
		if (dialogueIndentation == Constants.ERROR_CODE) {
			this.errors = true;
			Utilities.log("[" + Constants.ERROR + "] " + "Dialogue indentation error: " + this.scriptName + "\n");
			//System.exit(1);
		}
		this.indentationForTypes.put(Constants.DIALOGUE, dialogueIndentation);
		
		Integer sceneDescriptionIndentation = this.getSceneDescriptionIndentation();
		if (sceneDescriptionIndentation == Constants.ERROR_CODE) {
			this.errors = true;
			Utilities.log("[" + Constants.ERROR + "] " + "Scene description indentation error: " + this.scriptName + "\n");
			//System.exit(1);
		}
		this.indentationForTypes.put(Constants.SCENE_DESCRIPTION, sceneDescriptionIndentation);
		
		this.setDialogue(true);
		this.setSceneDescription(true);
		
		this.setBasedOnlyOnIndentation();
	}
	
	/**
	 * This method use the next functions to set the types without indentation
	 */
	private void setTypesWithoutIndentation() {
		this.getMetaBasedOnArray();
		this.getSceneBoundariesBasedOnArray(false);
		this.getCharacterNameBasedOnArray(false);
		this.setMetaInTheBegining();
		this.setCharacterNameWithoutIndentation();
		this.setDialogue(false);
		this.setSceneDescription(false);
		this.setTypesForLastLinesWithoutIndentation();
	}
	
	/**
	 * This method compute the first 5 indentation levels.
	 * @return An ArrayList with the best 5 indentation levels if their sum is more than 95% of 
	 * not empty lines or null otherwise.
	 */
	private ArrayList<Integer> computeIndentationLevel() {
		HashMap<Integer, Integer> indentationClusters = new HashMap<Integer, Integer>();
		int notEmptyLines = 0;
		int indentationSum = 0;
		for (int i = 0; i < this.indentationLevel.length; i++) {
			if (this.indentationLevel[i] == Constants.ERROR_CODE) {
				continue;
			}
			notEmptyLines++;
			Utilities.addToHashMap(indentationClusters, this.indentationLevel[i]);
		}
		ArrayList<Integer> bestIndentation = new ArrayList<Integer>();
		Set<Entry<Integer, Integer>> indentationSet = new HashSet<Entry<Integer, Integer>>(indentationClusters.entrySet());
		for (int i = 0; i < 5; i++) {
			Entry<Integer, Integer> bestEntry = null;
			for (Entry<Integer, Integer> entry: indentationSet) {
				if (bestEntry == null || bestEntry.getValue() < entry.getValue()) {
					bestEntry = entry;
				}
			}
			if (bestEntry != null) {
				bestIndentation.add(bestEntry.getKey());
				indentationSum += bestEntry.getValue();
				indentationSet.remove(bestEntry);
			}
		}
		Utilities.log("[" + Constants.INFO + "] " + "First 5 indentation proportion = " + (indentationSum * 1./ notEmptyLines) + "\n");
		if (indentationSum >= 0.95 * notEmptyLines) {
			return bestIndentation;
		}
		
		return null;
	}
	
	/**
	 * This method find the empty lines in script and set the scriptUnit to 0 for that line
	 */
	private void getEmptyLines() {
		int numberOfLines = this.scriptUnits.size();
		for (int i = 0; i < numberOfLines; i++) {
			if (this.scriptUnits.get(i).getText().isEmpty()) {
				this.scriptUnits.set(i, null);
			}
		}
	}

	/**
	 * This method set the UNKNOWN lines that contain specific META words and have all letters 
	 * upper case to META lines. The lines which are between parenthesis are considered META DATA.
	 */
	private void getMetaBasedOnArray() {
		int numberOfLines = this.scriptUnits.size();
		for (int i = 0; i < numberOfLines; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null || !unit.isUnknown()) {
				continue;
			}
			
			if (unit.getText().startsWith("(") && unit.getText().endsWith(")")) {
				this.scriptUnits.set(i, new MetaDataScriptUnit(unit.getIndex(), unit.getText()));
				continue;
			}
			
			if (i != numberOfLines - 1 && this.scriptUnits.get(i + 1) != null && this.scriptUnits.get(i + 1).isUnknown() &&
					 unit.getText().startsWith("(") && this.scriptUnits.get(i + 1).getText().endsWith(")")) {
				this.scriptUnits.set(i, new MetaDataScriptUnit(unit.getIndex(), unit.getText()));
				unit = this.scriptUnits.get(i + 1);
				this.scriptUnits.set(i + 1, new MetaDataScriptUnit(unit.getIndex(), unit.getText()));
				i++;
				continue;
			}
			
			if (this.upperCaseLine[i] == Constants.UPPERCASE) {
				for (String meta: Constants.META_ARRAY) {
					if (unit.getText().indexOf(meta) != -1) {
						MetaDataScriptUnit metaData = new MetaDataScriptUnit(unit.getIndex(), unit.getText(), Constants.META_DATA_TRANSITION);
						this.scriptUnits.set(i, metaData);
						break;
					}
				}
				continue;
			}
		}
		for (int i = numberOfLines - 1; i >= 0; i--) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null) {
				continue;
			}
			for (String endString: Constants.END_ARRAY) {
				if (unit.getText().indexOf(endString) != -1) {
					this.scriptUnits.set(i, new MetaDataScriptUnit(unit.getIndex(), unit.getText()));
					break;
				}
			}
			break;
		}
	}
	
	/**
	 * This method finds all upper case lines that has no type set yet and contain SCENE BOUNDARY words
	 * @param useIndentation Specify if the script has a good indentation and can be used
	 * @return the indentation level of the majority of the matched lines if the indentation is good
	 * or null otherwise
	 */
	private Integer getSceneBoundariesBasedOnArray(boolean useIndentation) {
		int numberOfLines = this.scriptUnits.size();
		HashMap<Integer, Integer> indentationLevelForSceneBoundaries = new HashMap<Integer, Integer>();
		for (int i = 0; i < numberOfLines; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null || !unit.isUnknown()) {
				continue;
			}
			boolean containsSceneBoundaryLabel = false;
			boolean found = false;
			for (String sceneBoundaryLabel: Constants.SCENE_BOUNDARY_ARRAY) {
				if (this.upperCaseLine[i] == Constants.UPPERCASE && unit.getText().startsWith(sceneBoundaryLabel)) {
					this.scriptUnits.set(i, new SceneBoundaryScriptUnit(unit.getIndex(), unit.getText()));
					if (useIndentation) {
						Utilities.addToHashMap(indentationLevelForSceneBoundaries, this.indentationLevel[i]);
					}
					found = true;
					break;
				}
				if (unit.getText().indexOf(sceneBoundaryLabel) != -1 || 
						unit.getText().indexOf(" " + sceneBoundaryLabel) != -1) {
					containsSceneBoundaryLabel = true;
				}
			}
			if (found) {
				for (int j = i + 1; j < numberOfLines; j++) {
					ScriptUnit currentUnit = this.scriptUnits.get(j);
					if (currentUnit == null || !currentUnit.isUnknown()) {
						break;
					}
					if (this.upperCaseLine[j] != Constants.UPPERCASE && 
							!Utilities.mostlyUpperCase(currentUnit.getText(), 4)) {
								break;
							}
					this.scriptUnits.set(j, new SceneBoundaryScriptUnit(currentUnit.getIndex(), currentUnit.getText()));
				}
				continue;
			}
			
			if (containsSceneBoundaryLabel && (this.upperCaseLine[i] == Constants.UPPERCASE || 
					Utilities.mostlyUpperCase(unit.getText(), 4))) {
				this.scriptUnits.set(i, new SceneBoundaryScriptUnit(unit.getIndex(), unit.getText()));
				if (useIndentation) {
					Utilities.addToHashMap(indentationLevelForSceneBoundaries, this.indentationLevel[i]);
				}
				for (int j = i + 1; j < numberOfLines; j++) {
					ScriptUnit currentUnit = this.scriptUnits.get(j);
					if (currentUnit == null || !currentUnit.isUnknown()) {
						break;
					}
					if (this.upperCaseLine[j] != Constants.UPPERCASE && 
							!Utilities.mostlyUpperCase(currentUnit.getText(), 4)) {
								break;
							}
					this.scriptUnits.set(j, new SceneBoundaryScriptUnit(currentUnit.getIndex(), currentUnit.getText()));
				}
				continue;
			}
		}
		if (useIndentation) {
			return Utilities.getBestKeyFromHashMap(indentationLevelForSceneBoundaries);
		}
		return null;
	}
	
	/**
	 * This method find the lines that contain specific CHARACTER NAME words and no type set yet
	 * If the words from array aren't relevant, it is computed the indentation level for the upper case
	 * lines with less or equal to 3 words if we can use indentation.
	 * @param useIndentation Specify if the script has a good indentation and can be used 
	 * @return the indentation level of the majority of the matched lines if the indentation is good
	 * or null otherwise
	 */
	private Integer getCharacterNameBasedOnArray(boolean useIndentation) {
		int numberOfLines = this.scriptUnits.size();
		HashMap<Integer, Integer> indentationLevelForCharacterName = new HashMap<Integer, Integer>();
		for (int i = 0; i < numberOfLines; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null || !unit.isUnknown()) {
				continue;
			}
			for (String characterNameLabel: Constants.CHARACTER_NAME_ARRAY) {
				if (unit.getText().indexOf(characterNameLabel) != -1 ||
						unit.getText().indexOf(characterNameLabel.toLowerCase()) != -1) {
					this.scriptUnits.set(i, new CharacterNameScriptUnit(unit.getIndex(), unit.getText()));
					if (useIndentation) {
						Utilities.addToHashMap(indentationLevelForCharacterName, this.indentationLevel[i]);
					}
					break;
				}
			}
		}
		if (!useIndentation) {
			return null;
		}
		
		int resultArray = Utilities.getBestKeyFromHashMap(indentationLevelForCharacterName);
		HashMap<Integer, Integer> indentationLevelForLess3Words = new HashMap<Integer, Integer>();
		for (int i = 0; i < numberOfLines; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null || !unit.isUnknown()) {
				continue;
			}
			if (this.upperCaseLine[i] == Constants.UPPERCASE && this.numberOfWords[i] <= 3) {
				Utilities.addToHashMap(indentationLevelForLess3Words, this.indentationLevel[i]);
			}
		}
		int resultLess3 = Utilities.getBestKeyFromHashMap(indentationLevelForLess3Words);
		if (resultArray != resultLess3) {
		//	Utilities.log("[" + Constants.INFO + "] " + "[Script]: Possible problems for character name: " + this.scriptName + "\n");
		}
		return resultLess3;
	}

	/**
	 * Set the CHARACTER NAME type for the lines with the specific indentation which are upper case and 
	 * aren't between 2 empty lines.
	 */
	private void setCharacterNameBasedOnIndentation() {
		int numberOfLines = this.scriptUnits.size();
		for (int i = 0; i < numberOfLines; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null || !unit.isUnknown()) {
				continue;
			}
			if (this.exception(i)) {
				continue;
			}
			if (this.indentationLevel[i] == this.indentationForTypes.get(Constants.CHARACTER_NAME) &&
					this.upperCaseLine[i] == Constants.UPPERCASE && i != 0 && i != numberOfLines - 1 &&
					!(this.scriptUnits.get(i - 1) == null && this.scriptUnits.get(i + 1) == null)) {
				this.scriptUnits.set(i, new CharacterNameScriptUnit(unit.getIndex(), unit.getText()));
			}
		}
	}
	
	/**
	 * Set the CHARACTER NAME type for the lines with less than 4 words which are upper case.
	 */
	private void setCharacterNameWithoutIndentation() {
		int numberOfLines = this.scriptUnits.size();
		for (int i = 0; i < numberOfLines; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null || !unit.isUnknown()) {
				continue;
			}
			if (this.exception(i)) {
				continue;
			}
			if (this.upperCaseLine[i] == Constants.UPPERCASE && this.numberOfWords[i] <= 3 
					&& i != numberOfLines - 1 && this.scriptUnits.get(i + 1) != null) {
				this.scriptUnits.set(i, new CharacterNameScriptUnit(unit.getIndex(), unit.getText()));
			}
		}
	}
	
	/**
	 * This method find the lines with no type set, which are after a CHARACTER NAME lines, or a meta
	 * after a CHARACTER NAME line
	 * @return the indentation level of the majority of the matched lines
	 */
	private Integer getDialogueIndentation() {
		int numberOfLines = this.scriptUnits.size();
		ScriptUnit lastType = null;
		HashMap<Integer, Integer> indentationLevelForDialogue = new HashMap<Integer, Integer>();
		for (int i = 0; i < numberOfLines; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null) {
				lastType = null;
				continue;
			}
			if (!unit.isUnknown()) {
				if (!unit.isMetaData()) {
					lastType = unit;
				}
				continue;
			}
			if (lastType != null && lastType.isCharacterName()) {
				this.scriptUnits.set(i, new DialogueScriptUnit(unit.getIndex(), unit.getText()));
				Utilities.addToHashMap(indentationLevelForDialogue, this.indentationLevel[i]);
				lastType = this.scriptUnits.get(i);
			}
		}
		return Utilities.getBestKeyFromHashMap(indentationLevelForDialogue);
	}
	
	/**
	 * This method find the lines with no type set, which are after SCENE BOUNDARY lines
	 * @return the indentation level of the majority of the matched lines
	 */
	private Integer getSceneDescriptionIndentation() {
		int numberOfLines = this.scriptUnits.size();
		ScriptUnit lastType = null;
		HashMap<Integer, Integer> indentationLevelForSceneDescription = new HashMap<Integer, Integer>();
		for (int i = 0; i < numberOfLines; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null) {
				if (lastType != null && lastType.isSceneDescription()) {
					lastType = null;
				}
				continue;
			}
			if (!unit.isUnknown()) {
				lastType = unit;
				continue;
			}
			if (lastType != null && lastType.isSceneBoundary()) {
				this.scriptUnits.set(i, new SceneDescriptionScriptUnit(unit.getIndex(), unit.getText()));
				Utilities.addToHashMap(indentationLevelForSceneDescription, this.indentationLevel[i]);
				lastType = this.scriptUnits.get(i);
			}
		}
		return Utilities.getBestKeyFromHashMap(indentationLevelForSceneDescription);
	}
	
	/**
	 * This method set the type for the lines with no type set, based only on indentation and some
	 * minor restrictions
	 */
	private void setBasedOnlyOnIndentation() {
		int numberOfLines = this.scriptUnits.size();
		ScriptUnit lastType = null;
		ScriptUnit savedType = null;
		for (int i = 0; i < numberOfLines; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null) {
				savedType = null;
				continue;
			}
			if (!unit.isUnknown()) {
				lastType = unit;
				savedType = unit;
				continue;
			}
			if ((unit.getText().startsWith("(") || unit.getText().endsWith(")")) && lastType != null && 
					lastType.isCharacterName()) {
				this.scriptUnits.set(i, new MetaDataScriptUnit(unit.getIndex(), unit.getText(), Constants.META_DATA_AUTHOR_DIRECTION));
				savedType = this.scriptUnits.get(i);
				continue;
			}
			if (this.upperCaseLine[i] == Constants.NOLETTERS) {
				this.scriptUnits.set(i, new MetaDataScriptUnit(unit.getIndex(), unit.getText()));
				continue;
			}
			
			if (this.indentationLevel[i] == this.indentationForTypes.get(Constants.SCENE_BOUNDARY) &&
					lastType != null && !lastType.isCharacterName()) {
				boolean found = false;
				for (String sceneBoundaryLabel: Constants.SCENE_BOUNDARY_ARRAY) {
					if (unit.getText().indexOf(" " + sceneBoundaryLabel) != -1) {
						this.scriptUnits.set(i, new SceneBoundaryScriptUnit(unit.getIndex(), unit.getText()));
						found = true;
						break;
					}
				}
				if (found) {
					for (int j = i - 1; j >= 0 && this.scriptUnits.get(j) != null && 
							this.scriptUnits.get(j).isUnknown(); j--) {
						this.scriptUnits.set(j, new SceneBoundaryScriptUnit(this.scriptUnits.get(j).getIndex(),
								this.scriptUnits.get(j).getText()));
					}
					lastType = this.scriptUnits.get(i);
					savedType = this.scriptUnits.get(i);
					continue;
				}
			}
			if (this.indentationLevel[i] == this.indentationForTypes.get(Constants.SCENE_DESCRIPTION) &&
					lastType != null && !lastType.isCharacterName()) {
				this.scriptUnits.set(i, new SceneDescriptionScriptUnit(unit.getIndex(), unit.getText()));
				for (int j = i - 1; j >= 0 && this.scriptUnits.get(j) != null && 
						this.scriptUnits.get(j).isUnknown(); j--) {
					this.scriptUnits.set(j, new SceneDescriptionScriptUnit(this.scriptUnits.get(j).getIndex(),
							this.scriptUnits.get(j).getText()));
				}
				lastType = this.scriptUnits.get(i);
				savedType = this.scriptUnits.get(i);
				continue;
			}
			if (this.indentationLevel[i] == this.indentationForTypes.get(Constants.CHARACTER_NAME)) {
				this.scriptUnits.set(i, new CharacterNameScriptUnit(unit.getIndex(), unit.getText()));
				for (int j = i - 1; j >= 0 && this.scriptUnits.get(j) != null && 
						this.scriptUnits.get(j).isUnknown(); j--) {
					this.scriptUnits.set(j, new CharacterNameScriptUnit(this.scriptUnits.get(j).getIndex(),
							this.scriptUnits.get(j).getText()));
				}
				lastType = this.scriptUnits.get(i);
				savedType = this.scriptUnits.get(i);
				continue;
			}
			if (this.indentationLevel[i] == this.indentationForTypes.get(Constants.DIALOGUE)) {	
				this.scriptUnits.set(i, new DialogueScriptUnit(unit.getIndex(), unit.getText()));
				for (int j = i - 1; j >= 0 && this.scriptUnits.get(j) != null && 
						this.scriptUnits.get(j).isUnknown(); j--) {
					if (this.indentationLevel[j] != this.indentationLevel[i] && 
							this.upperCaseLine[j] == Constants.UPPERCASE) {
						this.scriptUnits.set(j,  new CharacterNameScriptUnit(this.scriptUnits.get(j).getIndex(),
							this.scriptUnits.get(j).getText()));
						break;
					}
					this.scriptUnits.set(j, new DialogueScriptUnit(this.scriptUnits.get(j).getIndex(),
							this.scriptUnits.get(j).getText()));
				}
				lastType = this.scriptUnits.get(i);
				savedType = this.scriptUnits.get(i);
				continue;
			}
			// For the UNKNOWN lines we set the last type if there is a valid type and not null
			if (savedType != null) {
				if (savedType.isSceneBoundary()) {
					this.scriptUnits.set(i, new SceneBoundaryScriptUnit(unit.getIndex(), unit.getText()));
				} 
				else if (savedType.isCharacterName()) {
					this.scriptUnits.set(i, new CharacterNameScriptUnit(unit.getIndex(), unit.getText()));
				}
				else if (savedType.isDialogue()) {
					this.scriptUnits.set(i, new DialogueScriptUnit(unit.getIndex(), unit.getText()));
				}
				else if (savedType.isSceneDescription()) {
					this.scriptUnits.set(i, new SceneDescriptionScriptUnit(unit.getIndex(), unit.getText()));
				}
				else if (savedType.isMetaData()) {
					this.scriptUnits.set(i, new MetaDataScriptUnit(unit.getIndex(), unit.getText()));
				}
				continue;
			}
		}
		for (int i = 0; i < numberOfLines; i++) {
			if (this.scriptUnits.get(i) != null && this.scriptUnits.get(i).isUnknown()) {
				this.scriptUnits.set(i, new MetaDataScriptUnit(this.scriptUnits.get(i).getIndex(),
						this.scriptUnits.get(i).getText()));
			}
		}
	}
	
	/**
	 * This method set to DIALOGUE type all the lines which follow CHARACTER NAME lines or META lines
	 * which follow CHARACTER NAME lines
	 */
	private void setDialogue(boolean useIndentation) {
		int numberOfLines = this.scriptUnits.size();
		ScriptUnit lastType = null;
		for (int i = 0; i < numberOfLines; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null) {
				if (lastType != null && lastType.isDialogue()) {
					lastType = null;
				}
				continue;
			}
			if (!unit.isUnknown()) {
				if (!unit.isMetaData()) {
					lastType = unit;
				}
				continue;
			}
			if (lastType != null && (lastType.isCharacterName() || lastType.isDialogue())) {
				if (!useIndentation || (useIndentation && this.indentationLevel[i] == 
						this.indentationForTypes.get(Constants.DIALOGUE))) {
					this.scriptUnits.set(i, new DialogueScriptUnit(unit.getIndex(), unit.getText()));
					lastType = this.scriptUnits.get(i);
				}
			}
		}
	}
	
	/**
	 * This method set to SCENE DESCRIPTION type all the lines between SCENE BOUNDARY and CHARACTER NAME,
	 * between DIALOGUE and CHARACTER NAME, between DIALOGUE and SCENE BOUNDARY, ignoring META lines 
	 * already set
	 * Special case: lines between an initial META DATA transition and a not META DATA set line
	 */
	private void setSceneDescription(boolean useIndentation) {
		int numberOfLines = this.scriptUnits.size();
		for (int i = 0; i < numberOfLines; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null || unit.isUnknown() || (unit.isMetaData() && !((MetaDataScriptUnit)unit).isTransition())) {
				continue;
			}
			
			if (unit.isMetaData() && ((MetaDataScriptUnit)unit).isTransition()) {
				for (int j = i + 1; j < numberOfLines; j++) {
					ScriptUnit nextUnit = this.scriptUnits.get(j);
					if (nextUnit == null) {
						continue;
					}
					if (!nextUnit.isUnknown()) {
						break;
					}
					this.scriptUnits.set(j, new SceneDescriptionScriptUnit(this.scriptUnits.get(j).getIndex(), 
							this.scriptUnits.get(j).getText()));
				}
			}
			break;
		}
		
		ScriptUnit lastType = null;
		for (int i = 0; i < numberOfLines; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null) {
				continue;
			}
			if (!unit.isUnknown()) {
				if (unit.isMetaData()) {
					continue;
				}
				
				if (lastType != null && (lastType.isSceneBoundary() || lastType.isDialogue() || 
						lastType.isSceneDescription()) && (unit.isCharacterName() || unit.isSceneBoundary())) {
					for (int j = i - 1; j >= 0; j--) {
						ScriptUnit currentUnit = this.scriptUnits.get(j);
						if (currentUnit == null) {
							continue;
						}
						if (currentUnit != lastType) {
							if (currentUnit.isUnknown()) {
								if (!useIndentation || (useIndentation && this.indentationLevel[j] == 
										this.indentationForTypes.get(Constants.SCENE_DESCRIPTION))) {
									this.scriptUnits.set(j, new SceneDescriptionScriptUnit(this.scriptUnits.get(j).getIndex(), 
											this.scriptUnits.get(j).getText()));
								}
							}
						}
						else {
							break;
						}
					}
				}
				lastType = this.scriptUnits.get(i);
			}
		}
		if (lastType != null && (lastType.isSceneBoundary() || lastType.isDialogue()
				|| lastType.isSceneDescription())) {
			for (int j = numberOfLines - 1; j >= 0; j--) {
				ScriptUnit currentUnit = this.scriptUnits.get(j);
				if (currentUnit == null) {
					continue;
				}
				if (currentUnit != lastType) {
					if (currentUnit.isUnknown()) {
						if (!useIndentation || (useIndentation && this.indentationLevel[j] == 
								this.indentationForTypes.get(Constants.SCENE_DESCRIPTION))) {
							this.scriptUnits.set(j, new SceneDescriptionScriptUnit(this.scriptUnits.get(j).getIndex(),
									this.scriptUnits.get(j).getText()));
						}
					}
				}
				else {
					break;
				}
			}
		}
	}
	
	/**
	 * This method set a type for the UNKNOWN lines. For 2 or more consecutive lines UNKNOWN set 
	 * SCENE DESCRIPTION type and META otherwise.
	 */
	private void setTypesForLastLinesWithoutIndentation() {
		int numberOfLines = this.scriptUnits.size();
		int lastIndexNotUnknown = -1;
		for (int i = 0; i < numberOfLines; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null) {
				if (i - lastIndexNotUnknown >= 3) {
					for (int j = i - 1; j > lastIndexNotUnknown; j--) {
						this.scriptUnits.set(j, new SceneDescriptionScriptUnit(this.scriptUnits.get(j).getIndex(),
								this.scriptUnits.get(j).getText()));
					}
				}
				else if (i - lastIndexNotUnknown == 2) {
					this.scriptUnits.set(i - 1, new MetaDataScriptUnit(this.scriptUnits.get(i - 1).getIndex(), 
							this.scriptUnits.get(i - 1).getText()));
				}
				lastIndexNotUnknown = i;
				continue;
			}
			if (!unit.isUnknown()) {
				lastIndexNotUnknown = i;
				continue;
			}
		}
	}
	
	/**
	 * Set the type META for all the starting lines that are UNKNOWN and are alone
	 */
	private void setMetaInTheBegining() {
		int numberOfLines = this.scriptUnits.size();
		for (int i = 0; i < numberOfLines; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit == null) {
				continue;
			}
			if (!unit.isUnknown()) {
				break;
			}
			if (i != 0 && i != numberOfLines && this.scriptUnits.get(i - 1) == null && 
					this.scriptUnits.get(i + 1) == null) {
				this.scriptUnits.set(i, new MetaDataScriptUnit(unit.getIndex(), unit.getText()));
			}
		}
	}
	
	private boolean exception(int unitIndex) {
		// if the next not null line is already set and not META with () or DIALOGUE
		// this line cannot be CHARACTER NAME type
		int numberOfLines = this.scriptUnits.size();
		int i = unitIndex + 1;
		for (;i < numberOfLines; i++) {
			if (this.scriptUnits.get(i) == null) {
				continue;
			}
			if (this.scriptUnits.get(i).isUnknown()) {
				return false;
			}
			if (this.scriptUnits.get(i).isDialogue() || (this.scriptUnits.get(i).isMetaData() 
					&& this.scriptUnits.get(i).getText().startsWith("("))) {
				return false;
			}
			return true;
		}
		if (i == numberOfLines) {
			return false;
		}
		return true;
	}
	
	private void generateScriptUnitFromSentences() {
		ArrayList<ScriptUnit> finalScriptUnits = new ArrayList<ScriptUnit>();
		int index = 0;
		int numberOfScriptUnits = this.scriptUnits.size();
		StringBuilder text = new StringBuilder();
		ScriptUnit firstUnitFromGroup = null;
		for (int i = 0; i < numberOfScriptUnits + 1; i++) {
			ScriptUnit unit = null;
			if (i != numberOfScriptUnits) {
				unit = this.scriptUnits.get(i);
				if (firstUnitFromGroup == null) {
					firstUnitFromGroup = unit;
					if (unit != null) {
						text.setLength(0);
						text.append(unit.getText());
						if (unit.isMetaData() && unit.getText().startsWith("(") && unit.getText().indexOf(")") != -1) {
							finalScriptUnits.add(new MetaDataScriptUnit(index, text.toString().replaceAll(" +", " "), ((MetaDataScriptUnit)unit).getType()));
							index++;
							firstUnitFromGroup = null;
						}
					}
					continue;
				}
				if (firstUnitFromGroup.sameType(unit)) {
					if (firstUnitFromGroup.isMetaData() && firstUnitFromGroup.getText().startsWith("(") && 
							firstUnitFromGroup.getText().indexOf(")") == -1 && unit.getText().startsWith("(")) {
						finalScriptUnits.add(new MetaDataScriptUnit(index, text.toString().replaceAll(" +", " "), ((MetaDataScriptUnit)firstUnitFromGroup).getType()));
						index++;
						finalScriptUnits.add(new MetaDataScriptUnit(index, unit.getText().toString().replaceAll(" +", " "), ((MetaDataScriptUnit)unit).getType()));
						index++;
						text.setLength(0);
						firstUnitFromGroup = null;
						continue;
					}
					text.append(" " + unit.getText());
					continue;
				}
			}
			else {
				if (firstUnitFromGroup == null) {
					continue;
				}
			}
			if (firstUnitFromGroup.isSceneBoundary()) {
				finalScriptUnits.add(new SceneBoundaryScriptUnit(index, text.toString().replaceAll(" +", " ")));
				index++;
			}
			else if (firstUnitFromGroup.isCharacterName()) {
				finalScriptUnits.add(new CharacterNameScriptUnit(index, text.toString().replaceAll(" +", " ")));
				index++;
			}
			else if (firstUnitFromGroup.isMetaData()) {
				finalScriptUnits.add(new MetaDataScriptUnit(index, text.toString().replaceAll(" +", " "), ((MetaDataScriptUnit)firstUnitFromGroup).getType()));
				index++;
			}
			else {
				//List<String> sentences = Utilities.getSentences(text);
				List<String> sentences = Utilities.getSentencesBasedOnStanfordNLP(text.toString().replaceAll(" +", " "), this.sentenceSplitter);
				if (firstUnitFromGroup.isDialogue()) {
					for (String sentence: sentences) {
						finalScriptUnits.add(new DialogueScriptUnit(index, sentence));
						index++;
					}
				}
				else if (firstUnitFromGroup.isSceneDescription()) {
					for (String sentence: sentences) {
						finalScriptUnits.add(new SceneDescriptionScriptUnit(index, sentence));
						index++;
					}
				}
			}
			
			if (i != numberOfScriptUnits) {
				if (unit != null) {
					text.setLength(0);
					text.append(unit.getText());
				}
				else {
					text.setLength(0);
				}
				firstUnitFromGroup = unit;
				if (unit != null && unit.isMetaData() && unit.getText().startsWith("(") && unit.getText().indexOf(")") != -1) {
					finalScriptUnits.add(new MetaDataScriptUnit(index, text.toString().replaceAll(" +", " "), ((MetaDataScriptUnit)unit).getType()));
					index++;
					firstUnitFromGroup = null;
				}
			}
		}
		
		this.scriptUnits = finalScriptUnits;
	}
	
	private void repairScriptUnits() {
		int numberOfUnits = this.scriptUnits.size();
		int nrFails = 0;
		for (int i = numberOfUnits - 1; i >= 0; i--) {
			ScriptUnit unit = this.scriptUnits.get(i);
			boolean hasLettersOrDigits = false;
			String text = unit.getText();
			for (int j = 0; j < text.length(); j++) {
				if (Character.isAlphabetic(text.charAt(j))) {
					hasLettersOrDigits = true;
					break;
				}
				if (Character.isDigit(text.charAt(j))) {
					hasLettersOrDigits = true;
					break;
				}
			}
			if (!hasLettersOrDigits) {
				for (int j = i; j < numberOfUnits - 1; j++) {
					this.scriptUnits.set(j, this.scriptUnits.get(j + 1));
					this.scriptUnits.get(j).setIndex(j);
				}
				nrFails++;
				numberOfUnits--;
			}
		}
		for (int i = this.scriptUnits.size() - 1; i >= numberOfUnits; i--) {
			this.scriptUnits.remove(i);
		}
		//Utilities.log("Number of empty lines = " + nrFails + "\n");
		
		nrFails = 0;
		int totalFails = 0;
		
		for (int i = 0; i < numberOfUnits; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit != null && !unit.isMetaData() && unit.getText().startsWith("(") && unit.getText().endsWith(")")) {
				MetaDataScriptUnit metaDataUnit = new MetaDataScriptUnit(unit.getIndex(), unit.getText(), Constants.META_DATA_AUTHOR_DIRECTION);
				this.scriptUnits.set(i, metaDataUnit);
				continue;
			}
			if (unit != null && !unit.isMetaData()) {
				String text = unit.getText();
				int openParanthesis = 0;
				int j = 0;
				for (; j < text.length(); j++) {
					if (text.charAt(j) == '(') {
						openParanthesis++;
						continue;
					}
					if (text.charAt(j) == ')') {
						openParanthesis = Math.max(0, openParanthesis - 1);
						continue;
					}
					if (openParanthesis == 0 && (Character.isAlphabetic(text.charAt(j)) || 
							Character.isDigit(text.charAt(j)))) {
						break;
					}
				}
				if (j == text.length()) {
					StringBuilder scriptText = new StringBuilder();
					for (j = 0; j < text.length(); j++) {
						if (text.charAt(j) == '(') {
							scriptText.append("(");
							openParanthesis++;
							continue;
						}
						if (text.charAt(j) == ')') {
							if (openParanthesis == 0) {
								openParanthesis = 0;
							}
							else {
								openParanthesis--;
								scriptText.append(")");
							}
							continue;
						}
						if (openParanthesis == 0) {
							continue;
						}
						scriptText.append(text.charAt(j));
					}
					MetaDataScriptUnit metaDataUnit = new MetaDataScriptUnit(unit.getIndex(), scriptText.toString(), Constants.META_DATA_AUTHOR_DIRECTION);
					this.scriptUnits.set(i, metaDataUnit);
					continue;
				}
			}
		}
		
		for (int i = 0; i < numberOfUnits; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			ScriptUnit nextUnit = null;
			if (i != numberOfUnits - 1) {
				nextUnit = this.scriptUnits.get(i + 1);
			}
			ScriptUnit lastUnit = null;
			if (i != 0) {
				lastUnit = this.scriptUnits.get(i - 1);
			}
			
			if (unit.isCharacterName() && nextUnit == null) {
				nrFails++;
				continue;
			}
			if (unit.isCharacterName()) {
				if (!((nextUnit.isMetaData() && nextUnit.getText().startsWith("(")) || nextUnit.isDialogue())) {
					if (lastUnit != null && nextUnit != null) {
						if ((lastUnit.isSceneDescription() && nextUnit.isSceneDescription()) || 
								(lastUnit.isSceneDescription() && nextUnit.isMetaData() && ((MetaDataScriptUnit)nextUnit).isTransition()) || 
								(nextUnit.isSceneDescription() && lastUnit.isMetaData() && ((MetaDataScriptUnit)lastUnit).isTransition())) {
							this.scriptUnits.set(i, new SceneDescriptionScriptUnit(unit.getIndex(), unit.getText()));
							continue;
						}
					}
					
					boolean upperCase = Utilities.mostlyUpperCase(unit.getText(), 0);
				
					if (upperCase) {
						this.scriptUnits.set(i, new MetaDataScriptUnit(unit.getIndex(), unit.getText()));
						continue;
					}
					//Utilities.log("Index of character fail = " + i + "\n");
					nrFails++;
					continue;
				}
				if (nextUnit.isMetaData()) {
					if (!nextUnit.getText().startsWith("(")) {
						nrFails++;
					}
				}
			}
		}
		//Utilities.log("Number of character name fails = " + nrFails + "\n");
		totalFails += nrFails;
		nrFails = 0;
		for (int i = 0; i < numberOfUnits; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit.isDialogue()) {
				int j = i;
				for (; j >= 0; j--) {
					ScriptUnit currentUnit = this.scriptUnits.get(j);
					if (currentUnit == null || currentUnit.isDialogue()) {
						continue;
					}
					if (currentUnit.isCharacterName() || (currentUnit.isMetaData() && 
							(currentUnit.getText().startsWith("(") || currentUnit.getText().endsWith(")")))) {
						break;
					}
					//Utilities.log("Index of dialogue fail = " + i + "\n");
					nrFails++;
					break;
				}
				if (j < 0) {
					nrFails++;
				}
			}
		}
		//Utilities.log("Number of dialogue fails = " + nrFails + "\n");
		totalFails += nrFails;
		nrFails = 0;
		for (int i = 0; i < numberOfUnits - 1; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			ScriptUnit nextUnit = this.scriptUnits.get(i + 1);
			if (unit.isSceneBoundary()) {
				boolean contains = false;
				for (String s: Constants.SCENE_BOUNDARY_ARRAY) {
					if (unit.getText().contains(s)) {
						contains = true;
						break;
					}
				}
				if (!contains) {
					nrFails++;
					continue;
				}
				if (nextUnit.isSceneBoundary()) {
					//Utilities.log("Index of scene boundary fail = " + i + "\n");
					nrFails++;
					continue;
				}
			}
		}
		//Utilities.log("Number of scene boundaries fails = " + nrFails + "\n");
		totalFails += nrFails;
		nrFails = 0;
		
		for (int i = 0; i < numberOfUnits; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			ScriptUnit lastUnit = null;
			if (i != 0) {
				lastUnit = this.scriptUnits.get(i - 1);
			}
			ScriptUnit nextUnit = null;
			if (i != numberOfUnits - 1) {
				nextUnit = this.scriptUnits.get(i + 1);
			}
			if (unit.isSceneDescription() && lastUnit != null && nextUnit != null) {
				if (lastUnit.isCharacterName() || (lastUnit.isMetaData() && nextUnit.isDialogue()) ||
						(lastUnit.isDialogue() && nextUnit.isDialogue())) {
					nrFails++;
					//Utilities.log("Index of scene description fail = " + i + "\n");
				}
			}
		}
		
		//Utilities.log("Number of scene description fails = " + nrFails + "\n");
		totalFails += nrFails;
		nrFails = 0;
		
		for (int i = 0; i < numberOfUnits; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			ScriptUnit lastUnit = null;
			if (i != 0) {
				lastUnit = this.scriptUnits.get(i - 1);
			}
			ScriptUnit nextUnit = null;
			if (i != numberOfUnits - 1) {
				nextUnit = this.scriptUnits.get(i + 1);
			}
			if (lastUnit != null && nextUnit != null && lastUnit.isMetaData() && nextUnit.isMetaData() &&
					((MetaDataScriptUnit)lastUnit).isAuthorDirection() && ((MetaDataScriptUnit)nextUnit).isAuthorDirection() && 
					!unit.isDialogue() && !unit.isMetaData()) {
				MetaDataScriptUnit metaDataUnit = new MetaDataScriptUnit(unit.getIndex(), unit.getText(), Constants.META_DATA_AUTHOR_DIRECTION);
				this.scriptUnits.set(i, metaDataUnit);
				nrFails++;
			}
		}
		
		//Utilities.log("Number of character and meta fails = " + nrFails + "\n");
		totalFails += nrFails;
		nrFails = 0;
		
		if (totalFails > 10) {
			this.errors = true;
		}
		Utilities.log("Number of structural fails = " + totalFails + "\n");
		
	}
	
	private void setDetails() {
		int numberOfUnits = this.scriptUnits.size();
		for (int i = 0; i < numberOfUnits; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit.isMetaData()) {
				unit.setTextualDetails();
				unit.setStructureDetails(this.scriptUnits);
			}
		}
		
		for (int i = 0; i < numberOfUnits; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit.isCharacterName()) {
				unit.setTextualDetails();
				unit.setStructureDetails(this.scriptUnits);
			}
		}
		
		for (int i = 0; i < numberOfUnits; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit.isDialogue()) {
				unit.setTextualDetails();
				unit.setStructureDetails(this.scriptUnits);
			}
		}
		
		for (int i = 0; i < numberOfUnits; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit.isSceneBoundary()) {
				unit.setTextualDetails();
				unit.setStructureDetails(this.scriptUnits);
			}
		}
		
		for (int i = 0; i < numberOfUnits; i++) {
			ScriptUnit unit = this.scriptUnits.get(i);
			if (unit.isSceneDescription()) {
				unit.setTextualDetails();
				unit.setStructureDetails(this.scriptUnits);
			}
		}
	}
	
	public void importScriptFromFile(String filename) {
		String scriptContent = Utilities.readFile(filename);
		String[] tokens = scriptContent.split("\n");
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			String scriptUnitType = token.substring(0, token.indexOf(":"));
			scriptUnitType = scriptUnitType.substring(scriptUnitType.indexOf(Constants.PARANTHESIS_OPEN) + 1, 
					scriptUnitType.indexOf(Constants.PARANTHESIS_CLOSE));
			String text = token.substring(token.indexOf(":") + 1).trim();
			
			if (scriptUnitType.equals("SCENE_BOUNDARY")) {
				this.scriptUnits.add(new SceneBoundaryScriptUnit(i, text));
			}
			else if (scriptUnitType.equals("SCENE_DESCRIPTION")) {
				this.scriptUnits.add(new SceneDescriptionScriptUnit(i, text));
			}
			else if (scriptUnitType.equals("CHARACTER_NAME")) {
				this.scriptUnits.add(new CharacterNameScriptUnit(i, text));
			}
			else if (scriptUnitType.equals("DIALOGUE")) {
				this.scriptUnits.add(new DialogueScriptUnit(i, text));
			}
			else if (scriptUnitType.equals("META_DATA")) {
				this.scriptUnits.add(new MetaDataScriptUnit(i, text));
			}
		}
		this.setDetails();
	}
	
	public List<ScriptUnit> getScriptUnits() {
		return scriptUnits;
	}
	
	public int getNumberOfDialogueUnits() {
		if (this.numberOfDialogueUnits != -1) {
			return this.numberOfDialogueUnits;
		}
		int count = 0;
		int numberOfUnits = this.scriptUnits.size();
		for (int i = 0; i < numberOfUnits; i++) {
			ScriptUnit scriptUnit = this.scriptUnits.get(i);
			if (scriptUnit.isDialogue()) {
				count++;
			}
		}
		this.numberOfDialogueUnits = count;
		return this.numberOfDialogueUnits;
	}
	
	public void printScriptUnits() {
		for (int i = 0; i < this.scriptUnits.size(); i++) {
			System.out.println(this.scriptUnits.get(i));
		}
	}
	
	public String getScriptName() {
		return this.scriptName;
	}
	
	public List<ScriptUnit> getDialogueUnits() {
		if (!this.dialogueUnits.isEmpty()) {
			return this.dialogueUnits;
		}
		for (int i = 0; i < this.scriptUnits.size(); i++) {
			if (this.scriptUnits.get(i).isDialogue()) {
				this.dialogueUnits.add(this.scriptUnits.get(i));
			}
		}
		return this.dialogueUnits;
	}
	
	public boolean hasErrors() {
		return this.errors;
	}
	
	public void setAnnotatedUnits(List<ScriptUnit> annotatedUnits) {
		this.annotatedUnits = annotatedUnits;
	}
	
	public List<ScriptUnit> getAnnotatedUnits() {
		return this.annotatedUnits;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < this.scriptUnits.size(); i++) {
			if (this.scriptUnits.get(i) == null) {
				result.append("null");
			}
			else {
				result.append(this.scriptUnits.get(i).toString());
			}
		}
		return result.toString();
	}
}