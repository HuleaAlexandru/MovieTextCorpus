package MovieUnits;

import java.util.List;

import Utilities.Constants;
import Utilities.Utilities;

public class MetaDataScriptUnit extends ScriptUnit {

	private int type;
	
	public MetaDataScriptUnit(int index, String text) {
		super(index, text);
		this.type = Constants.META_DATA_NO_TYPE;
	}
	
	public MetaDataScriptUnit(int index, String text, int type) {
		super(index, text);
		this.type = type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public int getType() {
		return this.type;
	}
	
	public boolean isTransition() {
		return this.type == Constants.META_DATA_TRANSITION;
	}
	
	public boolean isAuthorDirection() {
		return this.type == Constants.META_DATA_AUTHOR_DIRECTION;
	}
	
	public boolean isNoType() {
		return this.type == Constants.META_DATA_NO_TYPE;
	}

	@Override
	public String getDetails() {
		String type = null;
		if (this.isTransition()) {
			type = "TRANSITION";
		}
		else if (this.isAuthorDirection()) {
			type = "AUTHOR_DIRECTION";
		}
		else if (this.isNoType()) {
			type = "NO_TYPE";
		}
		
		StringBuilder result = Utilities.resultsGenerator("Index", this.index + "");
		result.append(Utilities.resultsGenerator("Text", this.text));
		result.append(Utilities.resultsGenerator("Type", type));
		result.append("\n");
		
		return result.toString();
	}
	
	@Override
	public void setTextualDetails() {
		String text = new String(this.text);
		int nrLetters = 0;

		boolean upperCase = Utilities.mostlyUpperCase(text, 0);
		for (int j = 0; j < text.length(); j++) {
			if (Character.isAlphabetic(text.charAt(j))) {
				nrLetters++;
			}
		}
		
		if (nrLetters > 0 && upperCase) {
			for (String meta: Constants.META_ARRAY) {
				if (text.indexOf(meta) != -1) {
					this.type = Constants.META_DATA_TRANSITION;
					return;
				}
			}
		}
	}

	@Override
	public void setStructureDetails(List<ScriptUnit> scriptUnits) {
		int numberOfUnits = scriptUnits.size();
		ScriptUnit unit = scriptUnits.get(this.index);
		if (!unit.isMetaData() || !((MetaDataScriptUnit)unit).isNoType()) {
			return;
		}
		
		int nrLetters = 0;
		String text = this.getText();
		boolean upperCase = Utilities.mostlyUpperCase(text, 0);
		for (int j = 0; j < text.length(); j++) {
			if (Character.isAlphabetic(text.charAt(j))) {
				nrLetters++;
			}
		}
		
		ScriptUnit lastUnit = null;
		ScriptUnit nextUnit = null;
		if (this.index != 0) {
			lastUnit = scriptUnits.get(this.index  - 1);
		}
		if (this.index  != numberOfUnits - 1) {
			nextUnit = scriptUnits.get(this.index  + 1);
		}
		if (unit.getText().startsWith("(") || unit.getText().endsWith(")")) {
			if (lastUnit != null && lastUnit.isCharacterName()) {
				((MetaDataScriptUnit)unit).setType(Constants.META_DATA_AUTHOR_DIRECTION);
				return;
			}
			if (nextUnit != null && nextUnit.isDialogue()) {
				((MetaDataScriptUnit)unit).setType(Constants.META_DATA_AUTHOR_DIRECTION);
				return;
			}
			if (lastUnit != null && lastUnit.isDialogue() && nrLetters > 0 && !upperCase) {
				((MetaDataScriptUnit)unit).setType(Constants.META_DATA_AUTHOR_DIRECTION);
				return;
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = Utilities.resultsGenerator("META_DATA", this.text);
		
		if (startTime != null && endTime != null) {
			StringBuilder timeResult = this.getTime();
			timeResult.append(result);
			return timeResult.toString();
		}
		return result.toString();
	}
}
