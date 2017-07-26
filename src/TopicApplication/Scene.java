package TopicApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import MovieUnits.MetaDataScriptUnit;
import MovieUnits.Script;
import MovieUnits.ScriptUnit;
import MovieUnits.Tuple;
import Utilities.Utilities;
import Utilities.UtilitiesWordNet;

public class Scene {
	private Script script;
	private List<ScriptUnit> scriptUnits;
	private List<ScriptUnit> annotatedUnits;
	private Double score;
	private int startIndex;
	private int endIndex;
	
	public Scene(Script script, int startIndex, int endIndex) {
		this.script = script;
		List<ScriptUnit> scriptUnits = script.getScriptUnits();
		List<ScriptUnit> annotatedUnits = script.getAnnotatedUnits();
		this.score = 0.0;
		this.scriptUnits = new ArrayList<ScriptUnit>();
		this.annotatedUnits = new ArrayList<ScriptUnit>();
		for (int i = startIndex; i < endIndex; i++) {
			this.scriptUnits.add(scriptUnits.get(i));
			this.annotatedUnits.add(annotatedUnits.get(i));
		}
		this.startIndex = startIndex + 1;
		this.endIndex = endIndex + 1;
	}
	
	public int getStartIndex() {
		return this.startIndex;
	}
	
	public int getEndIndex() {
		return this.endIndex;
	}
	
	public void computeScore(Map<Tuple, Double> taxnonomy) {
		for (int i = 0; i < this.scriptUnits.size(); i++) {
			ScriptUnit scriptUnit = this.scriptUnits.get(i);
			ScriptUnit annotatedUnit = this.annotatedUnits.get(i);
			if (scriptUnit.isMetaData() && !((MetaDataScriptUnit)scriptUnit).isAuthorDirection()) {
				continue;
			}
			String line = annotatedUnit.getText();
			String[] words = line.split(" ");
			for (String word: words) {
				String actualWord = word.substring(0, word.lastIndexOf("{"));
				Integer wordType = UtilitiesWordNet.getTypeFromStanfordTag(word);
				if (taxnonomy.containsKey(new Tuple(actualWord, wordType.toString()))) {
					Double actualScore = taxnonomy.get(new Tuple(actualWord, wordType.toString()));
					this.score += actualScore;
					Utilities.log("Movie " + this.script.getScriptName() + " for scene " + 
							this.startIndex + "-" + this.endIndex + " found " + actualWord + "\n");
				}
			}
			for (Entry<Tuple, Double> e: taxnonomy.entrySet()) {
				String taxonomyWord = e.getKey().getFirst();
				if (taxonomyWord.contains(" ")) {
					// many words that make an expression
					if (line.contains(taxonomyWord)) {
						Double actualScore = e.getValue();
						this.score += actualScore;
						Utilities.log("Movie " + this.script.getScriptName() + " for scene " + 
								this.startIndex + "-" + this.endIndex + " found " + taxonomyWord + "\n");
					}
				}
			}
		}
		if (this.score > 0) {
			Utilities.log("Movie " + this.script.getScriptName() + " for scene " + 
					this.startIndex + "-" + this.endIndex + " final score " + this.score + "\n\n");
		}
	}
	
	public Double getScore() {
		return this.score;
	}
}
