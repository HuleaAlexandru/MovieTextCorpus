package MovieUnits;

import java.util.List;

import Utilities.Utilities;

public class UnknownScriptUnit extends ScriptUnit {
	
	public UnknownScriptUnit(int index, String text) {
		super(index, text);
	}
	
	@Override
	public void setTextualDetails() {
		return;
	}

	@Override
	public void setStructureDetails(List<ScriptUnit> scriptUnits) {
		return;
	}

	@Override
	public String getDetails() {
		return null;
	}

	@Override
	public String toString() {
		StringBuilder result = Utilities.resultsGenerator("UNKNOWN", this.text);
		
		if (startTime != null && endTime != null) {
			StringBuilder timeResult = this.getTime();
			timeResult.append(result);
			return timeResult.toString();
		}
		return result.toString();
	}
}
