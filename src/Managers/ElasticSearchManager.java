package Managers;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.admin.cluster.health.ClusterIndexHealth;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;

import MovieUnits.Script;
import MovieUnits.ScriptUnit;
import MovieUnits.Subtitle;
import MovieUnits.SubtitleUnit;
import MovieUnits.Tuple;
import Utilities.Constants;
import Utilities.Utilities;

public class ElasticSearchManager {
	private List<Subtitle> subtitles;
	private List<Script> scripts;
	private List<String> movieNames;
	private List<Node> elasticSearchNodes;
	private Client client;
	
	public ElasticSearchManager() {
		Utilities.log("\n[" + Constants.INFO + "] " + "ElasticSearchManager\n");
		this.scripts = new ArrayList<Script>();
		this.subtitles = new ArrayList<Subtitle>();
		this.movieNames = new ArrayList<String>();
		this.elasticSearchNodes = new ArrayList<Node>();
		this.client = null;
	}

	public ElasticSearchManager(List<Script> scripts, List<Subtitle> subtitles, List<String> movieNames) {
		Utilities.log("\n[" + Constants.INFO + "] " + "ElasticSearchManager\n");
		this.scripts = new ArrayList<Script>(scripts);
		this.subtitles = new ArrayList<Subtitle>(subtitles);
		this.movieNames = new ArrayList<String>(movieNames);
		this.elasticSearchNodes = new ArrayList<Node>();
		this.client = null;
	}
	
	public void start() {
		List<Integer> badMovies = new ArrayList<Integer>();
		
		Settings settings = Settings.settingsBuilder()
		        .put("cluster.name", Constants.ELASTICSEARCH_CLUSTER_NAME)
		        .put("path.home", "./../../elasticsearch-2.1.1")
		        .build();
		/*
		 * start an elasticsearch server on localhost with NR_NODES nodes
		 * and wait unit the cluster health become green
		 */
		for (int i = 0; i < Constants.ELASTICSEARCH_NR_NODES; i++) {
			Node node = nodeBuilder()
					.settings(settings).node();
			this.elasticSearchNodes.add(node);
		}
		
		/*
		 * get a client for the specific cluster
		 */
		try {
			this.client = TransportClient.builder().settings(settings).build()
			        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		} catch (UnknownHostException e) {
			Utilities.log("[" + Constants.ERROR + "] " + "[ElasticSearchManager]: Oups, we have a problem with elasticsearch on creating a client\n");
			this.close();
			System.exit(1);
		}
		
		ClusterHealthResponse response = this.client.admin().cluster().prepareHealth().setWaitForGreenStatus().get();
		ClusterHealthStatus status = response.getStatus();
		if (!status.equals(ClusterHealthStatus.GREEN)) {
			Utilities.log("[" + Constants.ERROR + "] " + "[ElasticSearchManager]: Oups, we have a problem with elasticsearch's health\n");
			this.close();
			System.exit(1);
		}
		
		String step4Path = Constants.STEP4_FOLDER;
		String step4ScriptPath = Constants.STEP4_SCRIPTS_FOLDER;
		
		Utilities.createFolder(step4Path);
		Utilities.createFolder(step4ScriptPath);
		
		/* 
		 * create an index(database) 'movieName' and a type 'subtitle' in elasticsearch cluster 
		 * specifying the mapping
		 */
		for (int i = 0; i < this.movieNames.size(); i++) {
			String movieName = this.movieNames.get(i).toLowerCase();
			String index = movieName;
			String type = Constants.ELASTICSEARCH_SUBTITLE_TYPE;
			Subtitle subtitle = this.subtitles.get(i);
			Script script = this.scripts.get(i);
			String scriptOutputPath = Constants.STEP4_SCRIPTS_FOLDER + File.separator + script.getScriptName();
			if (!(new File(scriptOutputPath).exists())) {
				IndicesAdminClient indicesAdminClient = this.client.admin().indices();
				boolean indexExists = false;
				try {
					indicesAdminClient.prepareCreate(movieName)
						.setSettings(Settings.builder()             
				                .put("index.number_of_shards", 2)
				                .put("index.number_of_replicas", 1)
				        )
			        	.addMapping(Constants.ELASTICSEARCH_SUBTITLE_TYPE, "{\n" +                
			                "	\"" + Constants.ELASTICSEARCH_SUBTITLE_TYPE + "\": {\n" +
			                "		\"properties\": {\n" +
			                "			\"index\": {\n" +
			                "				\"type\": \"integer\"\n" +
			                "			},\n" +
			                "			\"subIndex\": {\n" +
			                "				\"type\": \"integer\"\n" +
			                "			},\n" +
			                "			\"text\": {\n" +
			                "				\"type\": \"string\",\n" +
			                "				\"analyzer\": \"english\"\n" +
			                "			}\n" +
			                "		}\n" +
			                "	}\n" +
			                "}")
			        	.get();
				}
				catch (Exception e){
					Utilities.log("[" + Constants.INFO + "] " + "[ElasticSearchManager]: Index " + movieName + " already exists\n");
					indexExists = true;
				}
				
				/*
				 * add sentences from subtitles' list 
				 */
				if (!indexExists) {
					boolean result = this.indexSubtitle(index, type, subtitle);
					if (!result) {
						Utilities.log("[" + Constants.ERROR + "] " + "[ElasticSearchManager]: Oups! We got problems when indexing in elasticsearch\n");
						this.close();
						System.exit(1);
					}
				}
				
				boolean good = syncronizeScriptSubtitle(index, type, script, subtitle);
				if (good && !script.hasErrors()) {
					Utilities.log("[" + Constants.INFO + "] " + "Good sync\n");
					Utilities.writeFile(script.toString(), scriptOutputPath);
				}
				else {
					Utilities.log("[" + Constants.INFO + "] " + "Bad sync\n");
					badMovies.add(i);
				}
			}
		}
		for (Integer movieIndex: badMovies) {
			this.eraseMovie(this.movieNames.get(movieIndex));
		}
		for (int i = badMovies.size() - 1; i >= 0; i--) {
			this.movieNames.remove(badMovies.get(i));
			this.scripts.remove(badMovies.get(i));
			this.subtitles.remove(badMovies.get(i));
		}
		 
		this.close();
		this.client.close();

	}
	
	private void eraseMovie(String movieName) {
		movieName = movieName.toLowerCase();
		String folders[] = {
				Constants.STEP1_SCRIPTS_FOLDER,
				Constants.STEP1_SUBTITLES_FOLDER,
				Constants.STEP2_SCRIPTS_FOLDER,
				Constants.STEP2_SUBTITLES_FOLDER,
				Constants.STEP3_SCRIPTS_FOLDER,
				Constants.STEP3_SUBTITLES_FOLDER,
				Constants.DETAILS_FOLDER,
				Constants.TOPIC_FOLDER};
		for (String folder: folders) {
			List<String> eraseFiles = new ArrayList<String>();
			File folderFile = new File(folder);
			if (!folderFile.exists()) {
				continue;
			}
			String[] files = folderFile.list();
			for (String file: files) {
				if (file.toLowerCase().startsWith(movieName)) {
					eraseFiles.add(file);
				}
			}
			for (String eraseFile: eraseFiles) {
				File file = new File(folder + File.separator + eraseFile);
				if (file.exists()) {
					file.delete();
				}
			}
		}
		String movieInfos = Utilities.readFile(Constants.MOVIES_INFOS);
		String[] lines = movieInfos.split("\n");
		StringBuilder result = new StringBuilder();
		for (String line: lines) {
			if (line.toLowerCase().startsWith(movieName + " ")) {
				result.append("//");
			}
			result.append(line);
			result.append("\n");
		}
		new File(Constants.MOVIES_INFOS).delete();
		Utilities.writeFile(result.toString(), Constants.MOVIES_INFOS);
		client.admin().indices().delete(new DeleteIndexRequest(movieName)).actionGet();
	}
	
	/*
	 * add subtitle units in the index, using bulk for parallel processing
	 */
	private boolean indexSubtitle(String index, String type, Subtitle subtitle) {
		boolean result = true;
		BulkRequestBuilder bulkRequest = this.client.prepareBulk();
		List<SubtitleUnit> subtitleUnits = subtitle.getSubtitleUnits();
		
		for (SubtitleUnit unit: subtitleUnits) {
			List<String> text = unit.getText();
			int numberOfSentences = text.size();
			for (int i = 1; i <= numberOfSentences; i++) {
				try {
					bulkRequest.add(this.client.prepareIndex(index, type)
					    .setSource(jsonBuilder()
					                .startObject()
					                    .field("index", unit.getIndex())
					                    .field("subIndex", i)
					                    .field("text", text.get(i - 1))
					                .endObject()));
				} catch (IOException e) {
					Utilities.log("[" + Constants.INFO + "] " + "[ElasticSearchManager]: Oups! An exception was raised trying to index subtitle " + subtitle.getSubtitleName() + "\n");
					result = false;
				}
			}
		}
		BulkResponse bulkResponse = bulkRequest.get();
		int nr = 0;
		int total = 0;
		if (bulkResponse.hasFailures()) {
			 for (BulkItemResponse response : bulkResponse) {
	            if (response.isFailed()) {
	                nr++;
	            }
	            total++;
	        }
			Utilities.log("[" + Constants.INFO + "] " + "[ElasticSearchManager]: Oups! An exception was raised trying to get index response "
					+ subtitle.getSubtitleName() + " " + nr + " successfull " + (total - nr) + " failed\n");
			result = false;
		}
		return result;
	}
	
	/*
	 * for every dialogue unit from script return a range of values where it can be found in subtitle units
	 * limited by default in 15% of the subtitle units and after by neighbours dialogue units which already
	 * have a subtitle unit set   
	 */
	private List<Tuple> getRanges(Script script, Subtitle subtitle, int dialogueId, float acceptedError) {
		int numberOfDialogueUnits = script.getNumberOfDialogueUnits();
		int numberOfSubtitleUnits = subtitle.getSubtitleUnits().size();
		float subtitleId = (dialogueId * numberOfSubtitleUnits * 1.f / numberOfDialogueUnits);
		int minIndex = (int)Math.max(subtitleId - acceptedError * numberOfSubtitleUnits, 0);
		int maxIndex = (int)Math.min(subtitleId + acceptedError * numberOfSubtitleUnits, numberOfSubtitleUnits);
		int minSubIndex = 0;
		int maxSubIndex = 10;
		List<ScriptUnit> dialogueUnits = script.getDialogueUnits();
		
		for (int i = dialogueId; i >= 0; i--) {
			Tuple subtitleTuple = dialogueUnits.get(i).getSubtitleId();
			if (subtitleTuple == null) {
				continue;
			}
			int auxMinIndex = Integer.parseInt(subtitleTuple.getFirst());
			if (minIndex <= auxMinIndex) {
				minIndex = auxMinIndex;
				minSubIndex = Integer.parseInt(subtitleTuple.getSecond());
			}
			break;
		}
		
		for (int i = dialogueId; i < numberOfDialogueUnits; i++) {
			Tuple subtitleTuple = dialogueUnits.get(i).getSubtitleId();
			if (subtitleTuple == null) {
				continue;
			}
			int auxMaxIndex = Integer.parseInt(subtitleTuple.getFirst());
			if (maxIndex >= auxMaxIndex) {
				maxIndex = auxMaxIndex;
				maxSubIndex = Integer.parseInt(subtitleTuple.getSecond());
			}
			break;
		}
		
		List<Tuple> result = new ArrayList<Tuple>();
		result.add(new Tuple("" + minIndex, "" + minSubIndex));
		result.add(new Tuple("" + maxIndex, "" + maxSubIndex));
		return result;
	}
	
	/*
	 * search with MATCH_PHRASE_SEARCH, with a decreasing min_score, then search with MATCH_SEARCH,
	 * then with COMMON_SEARCH. Set the startTime and endTime for the rest of script units which have
	 * not a startTime and endTime already set.
	 */
	private boolean syncronizeScriptSubtitle(String index, String type, Script script,
			Subtitle subtitle) {
		
		int count = 0;
		
		float maxScore = Constants.MAX_SCORE_MATCH_PHRASE;
		float minScore = Constants.MIN_SCORE_MATCH_PHRASE;
		float step = Constants.STEP_MATCH_PHRASE;
		
	//	Utilities.log("MATCH PHRASE\n");
		float max = maxScore;
		float min = max - step;
		for (; min >= minScore ;) {
			count += this.search(index, type, script, subtitle, min, max, Constants.MATCH_PHRASE_SEARCH);
			max = min;
			min = max - step;
		}
		
	//	Utilities.log("MATCH\n");
		maxScore = Constants.MAX_SCORE_MATCH;
		minScore = Constants.MIN_SCORE_MATCH;
		step = Constants.STEP_MATCH;
		
		max = maxScore;
		min = max - step;
		for (; min >= minScore ;) {
			count += this.search(index, type, script, subtitle, min, max, Constants.MATCH_SEARCH);
			max = min;
			min = max - step;
		}
		
	//	Utilities.log("COMMON\n");
		maxScore = Constants.MAX_SCORE_COMMON_MATCH;
		minScore = Constants.MIN_SCORE_COMMON_MATCH;
		step = Constants.STEP_COMMON_MATCH;
		
		max = maxScore;
		min = max - step;
		for (; min >= minScore ;) {
			count += this.search(index, type, script, subtitle, min, max, Constants.COMMON_MATCH_SEARCH);
			max = min;
			min = max - step;
		}
		
		List<SubtitleUnit> subtitleUnits = subtitle.getSubtitleUnits();
		List<ScriptUnit> scriptUnits = script.getScriptUnits();
		List<Integer> mins = new ArrayList<Integer>();
		List<Integer> maxs = new ArrayList<Integer>();
		Integer lastMin = -1;	
		for (int i = 0; i < scriptUnits.size(); i++) {
			ScriptUnit scriptUnit = scriptUnits.get(i);
			Tuple subtitleId = scriptUnit.getSubtitleId();
			if (subtitleId != null) {
				lastMin = Integer.parseInt(subtitleId.getFirst()) - 1;
			}
			mins.add(lastMin);
		}
		
		Integer lastMax = subtitleUnits.size();
		for (int i = scriptUnits.size() - 1; i >= 0; i--) {
			ScriptUnit scriptUnit = scriptUnits.get(i);
			Tuple subtitleId = scriptUnit.getSubtitleId();
			if (subtitleId != null) {
				lastMax = Integer.parseInt(subtitleId.getFirst()) - 1;
			}
			maxs.add(lastMax);
		}
		Collections.reverse(maxs);
		
		Calendar lastTime = subtitleUnits.get(subtitleUnits.size() - 1).getEndTime();
		Integer lastHour = lastTime.get(Calendar.HOUR);
		Integer lastMinute = lastTime.get(Calendar.MINUTE);
		Integer lastSecond = lastTime.get(Calendar.SECOND);
		if (lastSecond == 59) {
			if (lastMinute == 59) {
				lastHour++;
			}
			else{
				lastMinute++;
			}
		}
		else {
			lastSecond++;
		}
		for (int i = 0; i < scriptUnits.size(); i++) {
			Calendar startTime = Calendar.getInstance();
			startTime.set(0, 0, 0, 0, 0, 0);
			Calendar endTime = Calendar.getInstance();
			endTime.set(0, 0, 0, lastHour, lastMinute, lastSecond);
			
			if (mins.get(i) == -1) {
				scriptUnits.get(i).setStartTime(startTime);
			}
			else {
				Calendar subtitleStartTime = subtitleUnits.get(mins.get(i)).getStartTime();
				scriptUnits.get(i).setStartTime(subtitleStartTime);
			}
			
			if (maxs.get(i) == subtitleUnits.size()) {
				scriptUnits.get(i).setEndTime(endTime);
			}
			else {
				Calendar subtitleEndTime = subtitleUnits.get(maxs.get(i)).getEndTime();
				scriptUnits.get(i).setEndTime(subtitleEndTime);
			}
		}
		Utilities.log("\n[" + Constants.INFO + "] [ElasticSearchManager]: Subtitle Units for " + index + " " + subtitle.getNumberOfUnits() + "\n");
		Utilities.log("[" + Constants.INFO + "] [ElasticSearchManager]: Script Units for " + index + " " + script.getDialogueUnits().size() + "\n");
		Utilities.log("[" + Constants.INFO + "] [ElasticSearchManager]: Total hits for " + index + " " + count + "\n");
		Utilities.log("[" + Constants.INFO + "] [ElasticSearchManager]: Hit percentage for " + index + " " + (count * 1./ script.getNumberOfDialogueUnits()) + "%\n");
		return count > script.getNumberOfDialogueUnits() / 3;
	}
	
	/*
	 * for every dialogue units in a script, elasticsearch will search in the indexed subtitle units
	 * filtering by min_score, and by ranges of every dialogue unit
	 */
	private int search(String index, String type, Script script, Subtitle subtitle,
			float minScore, float maxScore, int searchType) {
		int count = 0;
		int dialogueId = -1;
		ArrayList<Integer> occur = new ArrayList<Integer>();
		for (int i = 0; i < 20; i++) {
			occur.add(0);
		}
		Map<Integer, Tuple> scriptToSubtitle = new HashMap<Integer, Tuple>();
		MultiSearchRequestBuilder multiSearch = this.client.prepareMultiSearch();
		List<Integer> minIndexArray = new ArrayList<Integer>();
		List<Integer> minSubIndexArray = new ArrayList<Integer>();
		List<Integer> maxIndexArray = new ArrayList<Integer>();
		List<Integer> maxSubIndexArray = new ArrayList<Integer>();
		
		List<ScriptUnit> dialogueUnits = script.getDialogueUnits();
		for (ScriptUnit dialogueUnit: dialogueUnits) {
			dialogueId++;
			if (dialogueUnit.hasSubtitleSet()) {
				continue;
			}
			String str = dialogueUnit.getText();
			List<Tuple> minMax = this.getRanges(script, subtitle, dialogueId, Constants.ACCEPTED_ERROR);
			Integer minIndex = Integer.parseInt(minMax.get(0).getFirst());
			minIndexArray.add(minIndex);
			Integer minSubIndex = Integer.parseInt(minMax.get(0).getSecond());
			minSubIndexArray.add(minSubIndex);
			Integer maxIndex = Integer.parseInt(minMax.get(1).getFirst());
			maxIndexArray.add(maxIndex);
			Integer maxSubIndex = Integer.parseInt(minMax.get(1).getSecond());
			maxSubIndexArray.add(maxSubIndex);
			
			if (searchType == Constants.MATCH_PHRASE_SEARCH) {
				multiSearch.add(this.client.prepareSearch(index)
				        .setTypes(type)
				        .setQuery(QueryBuilders.boolQuery()
				        		.must(QueryBuilders.matchPhraseQuery("text", str))
				        		.filter(QueryBuilders.rangeQuery("index").from(minIndex).to(maxIndex)))
				        		.setMinScore(minScore));
			}
			else if (searchType == Constants.MATCH_SEARCH) {
				multiSearch.add(this.client.prepareSearch(index)
				        .setTypes(type)
				        .setQuery(QueryBuilders.boolQuery()
				        		.must(QueryBuilders.matchQuery("text", str))
				        		.filter(QueryBuilders.rangeQuery("index").from(minIndex).to(maxIndex)))
				        		.setMinScore(minScore));
			}
			else if (searchType == Constants.COMMON_MATCH_SEARCH) {
				multiSearch.add(this.client.prepareSearch(index)
				        .setTypes(type)
				        .setQuery(QueryBuilders.boolQuery()
				        		.must(QueryBuilders.commonTermsQuery("text", str))
				        		.filter(QueryBuilders.rangeQuery("index").from(minIndex).to(maxIndex)))
				        		.setMinScore(minScore));
			}
		}
		
		try {
			MultiSearchResponse response = multiSearch.execute().get();
			MultiSearchResponse.Item[] responses = response.getResponses();
			for (int j = 0, requestId = 0; j < dialogueUnits.size(); j++, requestId++) {
				if (dialogueUnits.get(j).hasSubtitleSet()) {
					requestId--;
					continue;
				}
				SearchResponse myResponse = responses[requestId].getResponse();
				if (myResponse == null) {
					continue;
				}
				if (myResponse.getHits().getTotalHits() > 0) {
					SearchHit bestHit = null;
					Iterator<SearchHit> iterator = myResponse.getHits().iterator();
					while(iterator.hasNext()) {
						SearchHit hit = iterator.next();
						Map<String, Object> source = hit.getSource();
						int hitIndex = Integer.parseInt(source.get("index").toString());
						int hitSubIndex = Integer.parseInt(source.get("subIndex").toString());
						if (hitIndex > minIndexArray.get(requestId) || (hitIndex ==  minIndexArray.get(requestId) && 
								hitSubIndex >  minSubIndexArray.get(requestId))) {
							if (hitIndex < maxIndexArray.get(requestId) || (hitIndex == maxIndexArray.get(requestId) &&
									hitSubIndex < maxSubIndexArray.get(requestId))) {
								bestHit = hit;
								break;
							}
						}
					}
					if (bestHit == null) {
						continue;
					}
					count++;
					float pos = bestHit.getScore();
					int position = (int)Math.min(pos, 19);
					occur.set(position, occur.get(position) + 1);
					
				//	Utilities.log(j + " " + dialogueUnits.get(j).getText() + " " + bestHit.getSourceAsString() + 
				//		bestHit.getScore() + "\n");
					
					Map<String, Object> source = bestHit.getSource();
					Tuple subtitleTuple = new Tuple(source.get("index").toString(), 
							source.get("subIndex").toString());
					subtitleTuple.addElement(new Float(pos).toString());
					scriptToSubtitle.put(j, subtitleTuple);
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			Utilities.log("[" + Constants.ERROR + "] " + "[ElasticSearchManager]: Oups! We got an error in multisearch from elasticsearch\n");
			this.close();
			System.exit(1);
		}
		
		int nrOfErrors = this.handleErrors(scriptToSubtitle, dialogueUnits, Constants.HANDLE_ERRORS_LESS_ERRORS_WINS_NOBODY_WINS);
	//	System.out.println("nr of errors = " + nrOfErrors);
		count -= nrOfErrors;
		
		//System.out.println("\nHits for " + index + " " + count + "(" + minScore + ", " + maxScore + ")\n");
		return count;
	}
	
	private int handleErrors(Map<Integer, Tuple> scriptToSubtitle, List<ScriptUnit> dialogueUnits, int handleType) {
		int nrOfErrors = 0;
		Set<Entry<Integer, Tuple>> entrySet = scriptToSubtitle.entrySet();
		Map<Entry<Integer, Tuple>, Integer> preprocessedErrors = null;
		
		if (handleType == Constants.HANDLE_ERRORS_LESS_ERRORS_WINS_BETTER_SCORE_WINS || 
				handleType == Constants.HANDLE_ERRORS_LESS_ERRORS_WINS_NOBODY_WINS) {
			preprocessedErrors = new HashMap<Map.Entry<Integer,Tuple>, Integer>();
			for (Entry<Integer, Tuple> e1: entrySet) {
				int e1Key = e1.getKey();
				Integer e1Index = Integer.parseInt(e1.getValue().getFirst());
				Integer e1SubIndex = Integer.parseInt(e1.getValue().getSecond());
				int selfErrors = 0;
				for (Entry<Integer, Tuple> e2: scriptToSubtitle.entrySet()) {
					if (e1 == e2) {
						continue;
					}
					int e2Key = e2.getKey();
					Integer e2Index = Integer.parseInt(e2.getValue().getFirst());
					Integer e2SubIndex = Integer.parseInt(e2.getValue().getSecond());
						
					if (e1Index == e2Index && e1SubIndex == e2SubIndex) {
						selfErrors++;
						continue;
					}
					if (e1Key < e2Key && (e1Index > e2Index || (e1Index == e2Index && e1SubIndex > e2SubIndex))) {
						selfErrors++;
						continue;
					}
					if (e1Key > e2Key && (e1Index < e2Index || (e1Index == e2Index && e1SubIndex < e2SubIndex))) {
						selfErrors++;
						continue;
					}
				}
				preprocessedErrors.put(e1, selfErrors);
			}
		}
		
		for (Entry<Integer, Tuple> e1: entrySet) {
			boolean bad = false;
			int e1Key = e1.getKey();
			Integer e1Index = Integer.parseInt(e1.getValue().getFirst());
			Integer e1SubIndex = Integer.parseInt(e1.getValue().getSecond());
			Float e1Score = Float.parseFloat(e1.getValue().getThird());
			for (Entry<Integer, Tuple> e2: scriptToSubtitle.entrySet()) {
				if (e1 == e2) {
					continue;
				}
				int e2Key = e2.getKey();
				Integer e2Index = Integer.parseInt(e2.getValue().getFirst());
				Integer e2SubIndex = Integer.parseInt(e2.getValue().getSecond());
				Float e2Score = Float.parseFloat(e2.getValue().getThird());
				
				if (handleType == Constants.HANDLE_ERRORS_NOBODY_WINS) {
					if (e1Index == e2Index && e1SubIndex == e2SubIndex) {
						bad = true;
						break;
					}
					if (e1Key < e2Key && (e1Index > e2Index || (e1Index == e2Index && e1SubIndex > e2SubIndex))) {
						bad = true;
						break;
					}
					if (e1Key > e2Key && (e1Index < e2Index || (e1Index == e2Index && e1SubIndex < e2SubIndex))) {
						bad = true;
						break;
					}
				}
				else if (handleType == Constants.HANDLE_ERRORS_BETTER_SCORE_WINS) {
					if (e1Score > e2Score) {
						continue;
					}
					if (e1Index == e2Index && e1SubIndex == e2SubIndex) {
						bad = true;
						break;
					}
					if (e1Key < e2Key && (e1Index > e2Index || (e1Index == e2Index && e1SubIndex > e2SubIndex))) {
						bad = true;
						break;
					}
					if (e1Key > e2Key && (e1Index < e2Index || (e1Index == e2Index && e1SubIndex < e2SubIndex))) {
						bad = true;
						break;
					}
				}
				else if (handleType == Constants.HANDLE_ERRORS_LESS_ERRORS_WINS_NOBODY_WINS) {
					int e1Errors = preprocessedErrors.get(e1);
					int e2Errors = preprocessedErrors.get(e2);
					if (e1Errors == 0 && e2Errors == 0) {
						continue;
					}
					if (e1Errors >= e2Errors) {
						if (e1Index == e2Index && e1SubIndex == e2SubIndex) {
							bad = true;
							break;
						}
						if (e1Key < e2Key && (e1Index > e2Index || (e1Index == e2Index && e1SubIndex > e2SubIndex))) {
							bad = true;
							break;
						}
						if (e1Key > e2Key && (e1Index < e2Index || (e1Index == e2Index && e1SubIndex < e2SubIndex))) {
							bad = true;
							break;
						}
					}
				}
				else if (handleType == Constants.HANDLE_ERRORS_LESS_ERRORS_WINS_BETTER_SCORE_WINS) {
					int e1Errors = preprocessedErrors.get(e1);
					int e2Errors = preprocessedErrors.get(e2);
					if (e1Errors == 0 && e2Errors == 0) {
						continue;
					}
					if (e1Errors > e2Errors || (e1Errors == e2Errors && e1Score <= e2Score)) {
						if (e1Index == e2Index && e1SubIndex == e2SubIndex) {
							bad = true;
							break;
						}
						if (e1Key < e2Key && (e1Index > e2Index || (e1Index == e2Index && e1SubIndex > e2SubIndex))) {
							bad = true;
							break;
						}
						if (e1Key > e2Key && (e1Index < e2Index || (e1Index == e2Index && e1SubIndex < e2SubIndex))) {
							bad = true;
							break;
						}
					}
				}
			}
			if (bad) {
			//	System.out.println("bad: " + e1Key + " " + e1Index + " " + e1SubIndex);
				nrOfErrors++;
				continue;
			}
			dialogueUnits.get(e1Key).setSubtitleId(new Tuple(e1Index.toString(), e1SubIndex.toString()));
		}
		return nrOfErrors;
	}
	
	private void close() {
		ClusterHealthResponse infos = this.client.admin().cluster().prepareHealth().get();
		
		for (ClusterIndexHealth info : infos) {                 
		    String indexName = info.getIndex();                       
		    Utilities.log("[" + Constants.INFO + "] " + "[ElasticSearchManager]: indexName = " +
		    		indexName + "\n");
		}
		
		for (int i = 0; i < this.elasticSearchNodes.size(); i++) {
			this.elasticSearchNodes.get(i).close();
		}
	}
}
