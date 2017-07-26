package MovieUnits;

import java.util.Calendar;
import java.util.List;

import Utilities.Constants;

public abstract class ScriptUnit {
	protected int index;
	protected Calendar startTime;
	protected Calendar endTime;
	protected String text;
	protected Tuple subtitleId;
	
	public ScriptUnit(int index, String text) {
		this.index = index;
		this.startTime = null;
		this.endTime = null;
		this.text = text;
		this.subtitleId = null;
	}
	
	public abstract void setTextualDetails();
	
	public abstract void setStructureDetails(List<ScriptUnit> scriptUnits);
	
	public abstract String getDetails();
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public boolean isUnknown() {
		return this instanceof UnknownScriptUnit;
	}
	
	public boolean isSceneBoundary() {
		return this instanceof SceneBoundaryScriptUnit;
	}
	
	public boolean isCharacterName() {
		return this instanceof CharacterNameScriptUnit;
	}
	
	public boolean isDialogue() {
		return this instanceof DialogueScriptUnit;
	}
	
	public boolean isSceneDescription() {
		return this instanceof SceneDescriptionScriptUnit;
	}
	
	public boolean isMetaData() {
		return this instanceof MetaDataScriptUnit;
	}
	
	public int getIndex() {
		return index;
	}

	public Calendar getStartTime() {
		return startTime;
	}

	public Calendar getEndTime() {
		return endTime;
	}
	
	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}
	
	public String getText() {
		return this.text;
	}
	
	public void setSubtitleId(Tuple subtitleId) {
		this.subtitleId = subtitleId;
	}
	
	public Tuple getSubtitleId() {
		return this.subtitleId;
	}
	
	public boolean hasSubtitleSet() {
		if (this.subtitleId == null) {
			return false;
		}
		return true;
	}
	
	public boolean sameType(ScriptUnit unit) {
		if (unit == null) {
			return false;
		}
		if ((this.isSceneBoundary() && unit.isSceneBoundary()) || (this.isCharacterName() && unit.isCharacterName()) ||
				(this.isDialogue() && unit.isDialogue()) || (this.isSceneDescription() && unit.isSceneDescription()) || 
				(this.isMetaData() && unit.isMetaData()) || (this.isUnknown() && unit.isUnknown())) {
			return true;
		} 
		return false;
	}
	
	protected StringBuilder getTime() {
		if (this.startTime == null || this.endTime == null) {
			return new StringBuilder();
		}
		StringBuilder timeResult = new StringBuilder();
		timeResult.append(Constants.PARANTHESIS_OPEN);
		timeResult.append(this.startTime.get(Calendar.HOUR));
		timeResult.append(":");
		timeResult.append(this.startTime.get(Calendar.MINUTE));
		timeResult.append(":");
		timeResult.append(this.startTime.get(Calendar.SECOND));
		timeResult.append(" - ");
		timeResult.append(this.endTime.get(Calendar.HOUR));
		timeResult.append(":");
		timeResult.append(this.endTime.get(Calendar.MINUTE));
		timeResult.append(":");
		timeResult.append(this.endTime.get(Calendar.SECOND));
		timeResult.append(Constants.PARANTHESIS_CLOSE);
		timeResult.append(" ");
		return timeResult;
	}
}
