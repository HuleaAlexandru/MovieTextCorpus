import Managers.DownloadManager;
import Managers.ElasticSearchManager;
import Managers.ParseManager;
import Managers.ResultsManager;
import Managers.SanitizerManager;
import TopicApplication.TopicManager;
import Utilities.Constants;
import Utilities.Utilities;

public class Main {

	private static DownloadManager downloadManager;
	private static SanitizerManager sanitizerManager;
	private static ParseManager parseManager;
	private static ElasticSearchManager elasticSearchManager;
	private static ResultsManager resultsManager;
	private static TopicManager topicManager;
	
	public static void main(String[] args) {
		Utilities.startLog();
		
		downloadManager = new DownloadManager();
		downloadManager.start();
	
		sanitizerManager = new SanitizerManager(downloadManager.getScriptNames(), downloadManager.getSubtitleNames(), 
				downloadManager.getMovieNames());
		sanitizerManager.start();
	
		parseManager = new ParseManager(sanitizerManager.getScriptNames(), sanitizerManager.getSubtitleNames(), 
				downloadManager.getMovieNames());
		parseManager.start();
		
		int generateMask = (1 << Constants.META_DATA) + (1 << Constants.CHARACTER_NAME) + (1 << Constants.DIALOGUE) +
				(1 << Constants.SCENE_BOUNDARY) + (1 << Constants.SCENE_DESCRIPTION) + (1 << Constants.POS);
		resultsManager = new ResultsManager(parseManager.getScripts(), generateMask);
		resultsManager.generateFiles();
		
		topicManager = new TopicManager(resultsManager.getScripts(), Constants.CARS_FILENAME);
		topicManager.start();
		
		elasticSearchManager = new ElasticSearchManager(parseManager.getScripts(), parseManager.getSubtitles(), 
				downloadManager.getMovieNames());
		elasticSearchManager.start();
	}
}