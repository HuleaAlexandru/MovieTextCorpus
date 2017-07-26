package MovieUnits;

import java.util.ArrayList;
import java.util.List;

import Utilities.Constants;
import Utilities.Utilities;

public class CharacterNameScriptUnit extends ScriptUnit {

	private String characterName;
	// suma de 1 << bit pentru ca pot fi mai multe tipuri concomitent
	private int type;
	private List<MetaDataScriptUnit> metaDataChildren;
	private List<DialogueScriptUnit> dialogueChildren;
	
	public CharacterNameScriptUnit(int index, String text) {
		super(index, text);
		this.type = Constants.CHARACTER_NAME_NO_TYPE;
		this.metaDataChildren = new ArrayList<MetaDataScriptUnit>();
		this.dialogueChildren = new ArrayList<DialogueScriptUnit>();
	}
	
	@Override
	public String getDetails() {
		String type = "";
		if (this.type == Constants.CHARACTER_NAME_NO_TYPE) {
			type += "NO_TYPE ";
		}
		if (isVO()) {
			type += "VO ";
		}
		if (isOS()) {
			type += "OS ";
		}
		if (isContd()) {
			type += "CONTD ";
		}
		
		StringBuilder result = Utilities.resultsGenerator("Index", this.index + "");
		result.append(Utilities.resultsGenerator("Text", this.text));
		result.append(Utilities.resultsGenerator("CharacterName", this.characterName));
		result.append(Utilities.resultsGenerator("Type", type));
		StringBuilder metaDataChildrenBuilder = new StringBuilder();
		for (MetaDataScriptUnit metaDataChild: this.metaDataChildren) {
			metaDataChildrenBuilder.append(Constants.PARANTHESIS_OPEN);
			metaDataChildrenBuilder.append(metaDataChild.getText());
			metaDataChildrenBuilder.append(Constants.PARANTHESIS_CLOSE);
			metaDataChildrenBuilder.append(" ");
		}
		result.append(Utilities.resultsGenerator("MetaDataChildren", metaDataChildrenBuilder.toString()));
		StringBuilder dialogueChildrenBuilder = new StringBuilder();
		for (DialogueScriptUnit dialogueChild: this.dialogueChildren) {
			dialogueChildrenBuilder.append(Constants.PARANTHESIS_OPEN);
			dialogueChildrenBuilder.append(dialogueChild.getText());
			dialogueChildrenBuilder.append(Constants.PARANTHESIS_CLOSE);
			dialogueChildrenBuilder.append(" ");
		}
		result.append(Utilities.resultsGenerator("DialogueChildren", dialogueChildrenBuilder.toString()));
		result.append("\n");
		
		return result.toString();
	}
	
	@Override
	public void setTextualDetails() {
		String text = new String(this.text);
		int paranthesisOpenIndex = text.indexOf("(");
		int paranthesisCloseIndex = -1;
		if (paranthesisOpenIndex == -1) {
			this.characterName = text.trim();
			return;
		}
		this.characterName = text.substring(0, paranthesisOpenIndex).trim();
		while (true) {
			paranthesisCloseIndex = text.indexOf(")", paranthesisOpenIndex + 1);
			if (paranthesisCloseIndex == -1) {
				paranthesisCloseIndex = text.length();
			}
			String subText = text.substring(paranthesisOpenIndex, paranthesisCloseIndex);
			StringBuilder detail = new StringBuilder();
			for (int i = 0; i < subText.length(); i++) {
				if (Character.isAlphabetic(subText.charAt(i)) || Character.isDigit(subText.charAt(i))) {
					detail.append(subText.charAt(i));
				}
			}
			subText = detail.toString();
			if (subText.toLowerCase().equals("vo") || subText.toLowerCase().equals("v0")) {
				this.type += (1 << Constants.CHARACTER_NAME_VO);
			}
			else if (subText.toLowerCase().equals("os")) {
				this.type += (1 << Constants.CHARACTER_NAME_OS);
			}
			else if (subText.toLowerCase().equals("contd")) {
				this.type += (1 << Constants.CHARACTER_NAME_CONTD);
			}
			
			paranthesisOpenIndex = text.indexOf("(", paranthesisOpenIndex + 1);
			if (paranthesisOpenIndex == -1) {
				break;
			}
		}
	}
	
	@Override
	public void setStructureDetails(List<ScriptUnit> scriptUnits) {
		int numberOfUnits = scriptUnits.size();
		for (int i = this.index + 1; i < numberOfUnits; i++) {
			ScriptUnit unit = scriptUnits.get(i);
			if (unit.isMetaData() && ((MetaDataScriptUnit)unit).isAuthorDirection()) {
				this.metaDataChildren.add((MetaDataScriptUnit)unit);
				continue;
			}
			if (unit.isDialogue()) {
				this.dialogueChildren.add((DialogueScriptUnit)unit);
				continue;
			}
			break;
		}
	}
	
	public String getCharacterName() {
		return this.characterName;
	}
	
	public List<MetaDataScriptUnit> getMetaDataChildren() {
		return this.metaDataChildren;
	}

	public List<DialogueScriptUnit> getDialogueChildren() {
		return this.dialogueChildren;
	}
	
	public boolean isVO() {
		return (this.type & (1 << Constants.CHARACTER_NAME_VO)) != 0;
	}
	
	public boolean isOS() {
		return (this.type & (1 << Constants.CHARACTER_NAME_OS)) != 0;
	}
	
	public boolean isContd() {
		return (this.type & (1 << Constants.CHARACTER_NAME_CONTD)) != 0;
	}
	
	@Override
	public String toString() {
		StringBuilder result = Utilities.resultsGenerator("CHARACTER_NAME", this.text);
		
		if (startTime != null && endTime != null) {
			StringBuilder timeResult = this.getTime();
			timeResult.append(result);
			return timeResult.toString();
		}
		return result.toString();
	}
}
