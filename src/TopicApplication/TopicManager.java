package TopicApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import MovieUnits.Script;
import MovieUnits.ScriptUnit;
import MovieUnits.Tuple;
import Utilities.Constants;
import Utilities.Utilities;
import Utilities.UtilitiesWordNet;

public class TopicManager {
	List<Script> scripts;
	String topicFilename;
	Map<Tuple, Double> taxonomy;
	List< List<Scene> > scenes;
	Integer topicType;
	
	public TopicManager(List<Script> scripts, String topicFilename) {
		Utilities.log("\n[" + Constants.INFO + "] " + "TopicManager\n");
		this.scripts = scripts;
		this.topicFilename = topicFilename;
		this.taxonomy = null;
		this.scenes = new ArrayList< List<Scene> >();	
		this.topicType = UtilitiesWordNet.NONE;
	}
	
	public void start() {
		this.createTaxonomy();
		if (this.taxonomy == null) {
			Utilities.log("Empty taxonomy\n");
			return;
		}
		this.createScenes();
		this.markScenes();
	}
	
	private void createTaxonomy() {
		this.taxonomy = UtilitiesWordNet.createTaxonomy(this.topicFilename);
	}
 	
	private void createScenes() {
		for (Script script: this.scripts) {
			List<Scene> scenes = new ArrayList<Scene>();
			List<ScriptUnit> annotatedUnits = script.getAnnotatedUnits();
			int startIndex = 0;
			int endIndex = annotatedUnits.size();
			for (int i = 0; i < annotatedUnits.size(); i++) {
				ScriptUnit unit = annotatedUnits.get(i);
				if (unit.isSceneBoundary()) {
					endIndex = i;
//					Utilities.log("\n[" + Constants.INFO + "] " + "Movie " + script.getScriptName() + 
//							" scene " + startIndex + " " + endIndex + "\n");
					scenes.add(new Scene(script, startIndex, endIndex));
					startIndex = i;
				}
			}
			endIndex = annotatedUnits.size();
			scenes.add(new Scene(script, startIndex, endIndex));
			this.scenes.add(scenes);	
		}
	}

	private void markScenes() {	
		Utilities.createFolder(Constants.TOPIC_FOLDER);
		
		for (int i = 0; i < this.scripts.size(); i++) {
			String scriptName = this.scripts.get(i).getScriptName();
			String movieName = scriptName.substring(0, scriptName.lastIndexOf("_"));
			File topicFile = new File(Constants.TOPIC_FOLDER + File.separator + movieName + "_" + this.topicFilename);
			if (!topicFile.exists()) {
				StringBuilder topicContent = new StringBuilder();
				int markedScenes = 0;
				List<Scene> scenesList = new ArrayList<Scene>();
				for (int j = 0; j < this.scenes.get(i).size(); j++) {
					Scene scene = this.scenes.get(i).get(j);
					scene.computeScore(this.taxonomy);
					if (scene.getScore() >= UtilitiesWordNet.LIMIT_SCORE) {
						markedScenes++;
						scenesList.add(scene);
					}
				}
				topicContent.append("Marked Scenes " + markedScenes + " out of " + this.scenes.get(i).size() + "\n");
				if (markedScenes * 1. / this.scenes.get(i).size() < UtilitiesWordNet.LIMIT_EPISODIC) {
					String message = "The topic " + this.topicFilename + " for " + movieName + " was not found in the text\n";
					//Utilities.log(message);
					topicContent.append(message);
				}
				else if (markedScenes * 1. / this.scenes.get(i).size() <= UtilitiesWordNet.LIMIT_PRIMARY) {
					this.topicType = UtilitiesWordNet.EPISODIC;
					String message = "The topic " + this.topicFilename + " for " + movieName + " is episodic\n";
					StringBuilder episodicBuilder = new StringBuilder();
					episodicBuilder.append(message);
					episodicBuilder.append("Relevant scenes:\n");
					for (int j = 0; j < scenesList.size(); j++) {
						episodicBuilder.append("Scene: " + scenesList.get(j).getStartIndex() + " " + scenesList.get(j).getEndIndex() + "\n");
					}
					//Utilities.log(episodicBuilder.toString());
					topicContent.append(episodicBuilder);
				}
				else {
					this.topicType = UtilitiesWordNet.PRIMARY;
					String message = "The topic " + this.topicFilename + " for " + movieName + " is a primary topic\n";
					StringBuilder primaryBuilder = new StringBuilder();
					primaryBuilder.append(message);
					primaryBuilder.append("Relevant scenes:\n");
					for (int j = 0; j < scenesList.size(); j++) {
						primaryBuilder.append("Scene: " + scenesList.get(j).getStartIndex() + " " + scenesList.get(j).getEndIndex() + "\n");
					}
					//Utilities.log(primaryBuilder.toString());
					topicContent.append(primaryBuilder);
				}
				Utilities.writeFile(topicContent.toString(), topicFile.getPath());
			}
		}
	}
}
