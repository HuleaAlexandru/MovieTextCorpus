package MovieUnits;

import java.util.ArrayList;
import java.util.List;

import Utilities.Constants;
import Utilities.Utilities;

public class SceneDescriptionScriptUnit extends ScriptUnit {
	
	private SceneBoundaryScriptUnit sceneBoundaryParent;
	private List<Tuple> innerMeta;
	
	public SceneDescriptionScriptUnit(int index, String text) {
		super(index, text);
		this.sceneBoundaryParent = null;
		this.innerMeta = new ArrayList<Tuple>();
	}
	
	@Override
	public String getDetails() {
		StringBuilder result = Utilities.resultsGenerator("Index", this.index + "");
		result.append(Utilities.resultsGenerator("Text", this.text));
		if (this.sceneBoundaryParent != null) {
			result.append(Utilities.resultsGenerator("SceneBoundaryParent", this.sceneBoundaryParent.getText()));
		}
		else {
			result.append(Utilities.resultsGenerator("SceneBoundaryParent", ""));
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
//		if (this.innerMeta.isEmpty()) {
//			return;
//		}
//		Utilities.log(this.text + " " + this.innerMeta + "\n");
	}

	@Override
	public void setStructureDetails(List<ScriptUnit> scriptUnits) {
		for (int i = this.index - 1; i >= 0; i--) {
			ScriptUnit unit = scriptUnits.get(i);
			if (unit.isSceneBoundary()) {
				this.sceneBoundaryParent = (SceneBoundaryScriptUnit)unit;
				break;
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = Utilities.resultsGenerator("SCENE_DESCRIPTION", this.text);
	
		if (startTime != null && endTime != null) {
			StringBuilder timeResult = this.getTime();
			timeResult.append(result);
			return timeResult.toString();
		}
		return result.toString();
	}
}
