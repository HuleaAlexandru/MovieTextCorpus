package MovieUnits;

import java.util.ArrayList;
import java.util.List;

import Utilities.Constants;
import Utilities.Utilities;

public class DialogueScriptUnit extends ScriptUnit {

	private MetaDataScriptUnit metaDataParent;
	private CharacterNameScriptUnit characterNameParent;
	private List<Tuple> innerMeta;
	
	public DialogueScriptUnit(int index, String text) {
		super(index, text);
		this.metaDataParent = null;
		this.characterNameParent = null;
		this.innerMeta = new ArrayList<Tuple>();
	}
	
	@Override
	public String getDetails() {
		StringBuilder result = Utilities.resultsGenerator("Index", this.index + "");
		result.append(Utilities.resultsGenerator("Text", this.text));
		if (this.metaDataParent != null) {
			result.append(Utilities.resultsGenerator("MetaDataParent", this.metaDataParent.getText()));
		}
		else {
			result.append(Utilities.resultsGenerator("MetaDataParent", ""));
		}
		if (this.characterNameParent != null) {
			result.append(Utilities.resultsGenerator("CharacterNameParent", this.characterNameParent.getText()));
		}
		else {
			result.append(Utilities.resultsGenerator("CharacterNameParent", ""));
		}
		StringBuilder innerMetaBuilder = new StringBuilder();
		for (Tuple tuple: this.innerMeta) {
			innerMetaBuilder.append(Constants.PARANTHESIS_OPEN);
			innerMetaBuilder.append(tuple.getFirst());
			innerMetaBuilder.append(Constants.PARANTHESIS_CLOSE);
			innerMetaBuilder.append(" ");
		}
		result.append(Utilities.resultsGenerator("innerMetaData", innerMetaBuilder.toString()));
		result.append("\n");
		
		return result.toString();
	}
	
	@Override
	public void setTextualDetails() {
		String text = new String(this.text);
		int start = 0;
		while(true) {
			int paranthesisOpen = text.indexOf("(", start);
			int paranthesisClose = text.indexOf(")", paranthesisOpen + 1);
			if (paranthesisOpen == -1 || paranthesisClose == -1) {
				break;
			}
			String startPos = paranthesisOpen + "";
			String endPos = paranthesisClose + "";
			Tuple tuple = new Tuple(text.substring(paranthesisOpen + 1, paranthesisClose), startPos);
			tuple.addElement(endPos);
			this.innerMeta.add(tuple);
			start = paranthesisOpen + 1;
		}
	}

	@Override
	public void setStructureDetails(List<ScriptUnit> scriptUnits) {
		for (int i = this.index - 1; i >= 0; i--) {
			ScriptUnit unit = scriptUnits.get(i);
			if (this.metaDataParent == null && unit.isMetaData() && ((MetaDataScriptUnit)unit).isAuthorDirection()) {
				this.metaDataParent = (MetaDataScriptUnit)unit;
			}
			if (unit.isCharacterName()) {
				this.characterNameParent = (CharacterNameScriptUnit)unit;
				break;
			}
		}
	}

	public MetaDataScriptUnit getMetaDataParent() {
		return this.metaDataParent;
	}

	public CharacterNameScriptUnit getCharacterNameParent() {
		return this.characterNameParent;
	}
	
	@Override
	public String toString() {
		StringBuilder result = Utilities.resultsGenerator("DIALOGUE", this.text);
		
		if (startTime != null && endTime != null) {
			StringBuilder timeResult = this.getTime();
			timeResult.append(result);
			return timeResult.toString();
		}
		return result.toString();
	}
}