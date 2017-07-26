package Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import MovieUnits.Tuple;
import TopicApplication.SynsetWrapper;
import edu.smu.tspell.wordnet.AdjectiveSynset;
import edu.smu.tspell.wordnet.AdverbSynset;
import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.VerbSynset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.WordSense;

public class UtilitiesWordNet {
	
	// scaderea ponderilor pentru most important in functie de distanta scade proportional cu DIVISION_FACTOR
	public static final Double MOST_IMPORTANT = 10.0;
	public static final Double IMPORTANT = 5.0;
	public static final Double LESS_IMPORTANT = 1.0;
	public static final Integer MAX_DISTANCE = 1;
	public static final Double DIVISION_FACTOR = 1.5;
	
	public static final Integer NONE = -1;
	public static final Integer NOUN = 0;
	public static final Integer VERB = 1;
	public static final Integer ADJECTIVE = 2;
	public static final Integer ADVERB = 3;
	
	public static final Integer LIMIT_SCORE = 10;
	public static final Double LIMIT_EPISODIC = 1./30;
	public static final Double LIMIT_PRIMARY = 1./10;
	public static final Integer EPISODIC = 0;
	public static final Integer PRIMARY = 1;
	public static WordNetDatabase database;
	
	public static void getNounHypernyms(String word, Map<Tuple, Double> neighbours, int distance, Double score){
		if(distance <= 0) {
			return;
		}
		
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset synset = database.getSynsets(word, SynsetType.NOUN)[0];
		
		for(NounSynset hypernymSynset : ((NounSynset)synset).getHypernyms()) {
			for(String wordForm : hypernymSynset.getWordForms()){
				if (isPrimarySense(hypernymSynset, wordForm)) {
					getNounHypernyms(wordForm, neighbours, distance - 1, score / DIVISION_FACTOR);
					if (!neighbours.containsKey(new Tuple(wordForm, getType(hypernymSynset).toString())) || 
							neighbours.get(new Tuple(wordForm, getType(hypernymSynset).toString())) < score) { 
						neighbours.put(new Tuple(wordForm, getType(hypernymSynset).toString()), score);
						//System.out.println(wordForm + " " + score);
					}
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
	}
	
	public static void getNounNeighbours(Synset synset, Map<Tuple, Double> neighbours) {
		NounSynset nounSynset = (NounSynset)synset;
		Double score = LESS_IMPORTANT;
		
		//System.out.println("Adjectives:");
		for (AdjectiveSynset adjective: nounSynset.getAttributes()) {	
			for (String wordForm: adjective.getWordForms()) {
				if (isPrimarySense(adjective, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(adjective).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		System.out.println("Member Holonyms:");
		for (Synset memberHolonym: nounSynset.getMemberHolonyms()) {	
			for (String wordForm: memberHolonym.getWordForms()) {
				if (isPrimarySense(memberHolonym, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(memberHolonym).toString()), score);
					System.out.println(wordForm + " " + score);
				}
				else {
					System.out.println("not added " + wordForm);
				}
			}
		}
		System.out.println();
		
		System.out.println("Part Holonyms:");
		for (Synset partHolonym: nounSynset.getPartHolonyms()) {	
			for (String wordForm: partHolonym.getWordForms()) {
				if (isPrimarySense(partHolonym, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(partHolonym).toString()), score);
					System.out.println(wordForm + " " + score);
				}
				else {
					System.out.println("not added " + wordForm);
				}
			}
		}
		System.out.println();
		
		System.out.println("Member Meronyms:");
		for (Synset memberMeronym: nounSynset.getMemberMeronyms()) {	
			for (String wordForm: memberMeronym.getWordForms()) {
				if (isPrimarySense(memberMeronym, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(memberMeronym).toString()), score);
					System.out.println(wordForm + " " + score);
				}
				else {
					System.out.println("not added " + wordForm);
				}
			}
		}
		System.out.println();
		
		System.out.println("Part Meronyms:");
		for (Synset partMeronym: nounSynset.getPartMeronyms()) {	
			for (String wordForm: partMeronym.getWordForms()) {
				if (isPrimarySense(partMeronym, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(partMeronym).toString()), score);
					System.out.println(wordForm + " " + score);
				}
				else {
					System.out.println("not added " + wordForm);
				}
			}
		}
		System.out.println();
		
		//System.out.println("Substance Holonyms:");
		for (Synset substanceHolonym: nounSynset.getSubstanceHolonyms()) {	
			for (String wordForm: substanceHolonym.getWordForms()) {
				if (isPrimarySense(substanceHolonym, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(substanceHolonym).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		//System.out.println("Substance Meronyms:");
		for (Synset substanceMeronym: nounSynset.getSubstanceMeronyms()) {	
			for (String wordForm: substanceMeronym.getWordForms()) {
				if (isPrimarySense(substanceMeronym, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(substanceMeronym).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		//System.out.println("Topic Members:");
		for (Synset topicMember: nounSynset.getTopicMembers()) {	
			for (String wordForm: topicMember.getWordForms()) {
				if (isPrimarySense(topicMember, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(topicMember).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		//System.out.println("Topics:");
		for (Synset topic: nounSynset.getTopics()) {	
			for (String wordForm: topic.getWordForms()) {
				if (isPrimarySense(topic, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(topic).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		score = MOST_IMPORTANT;
		
		//System.out.println("Hypernyms:");
		for (Synset hypernym: nounSynset.getHypernyms()) {	
			for (String wordForm: hypernym.getWordForms()) {
				if (isPrimarySense(hypernym, wordForm)) {
					getNounHypernyms(wordForm, neighbours, MAX_DISTANCE, score / DIVISION_FACTOR);
					if (!neighbours.containsKey(new Tuple(wordForm, getType(hypernym).toString())) || 
							neighbours.get(new Tuple(wordForm, getType(hypernym).toString())) < score) { 
						neighbours.put(new Tuple(wordForm, getType(hypernym).toString()), score);
						//System.out.println(wordForm + " " + score);
					}
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		//System.out.println("Hyponyms:");
		for (Synset hyponym: nounSynset.getHyponyms()) {	
			for (String wordForm: hyponym.getWordForms()) {
				if (isPrimarySense(hyponym, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(hyponym).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		//System.out.println("Instance Hypernyms:");
		for (Synset instanceHypernym: nounSynset.getInstanceHypernyms()) {	
			for (String wordForm: instanceHypernym.getWordForms()) {
				if (isPrimarySense(instanceHypernym, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(instanceHypernym).toString()), score);
					//System.out.println(wordForm);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		//System.out.println("Instance Hyponyms:");
		for (Synset instanceHyponym: nounSynset.getInstanceHyponyms()) {	
			for (String wordForm: instanceHyponym.getWordForms()) {
				if (isPrimarySense(instanceHyponym, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(instanceHyponym).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
	}
	
	public static void getVerbHypernyms(String word, Map<Tuple, Double> neighbours, int distance, Double score){
		if(distance <= 0) {
			return;
		}
		
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset synset = database.getSynsets(word, SynsetType.VERB)[0];
		
		for(VerbSynset hypernymSynset : ((VerbSynset)synset).getHypernyms()) {
			for(String wordForm : hypernymSynset.getWordForms()){
				if (isPrimarySense(hypernymSynset, wordForm)) {
					getVerbHypernyms(wordForm, neighbours, distance - 1, score / DIVISION_FACTOR);
					if (!neighbours.containsKey(new Tuple(wordForm, getType(hypernymSynset).toString())) || 
							neighbours.get(new Tuple(wordForm, getType(hypernymSynset).toString())) < score) {
							neighbours.put(new Tuple(wordForm, getType(hypernymSynset).toString()), score);
							//System.out.println("added " + wordForm + " " + score);
					}
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
	}
	
	public static void getVerbNeighbours(Synset synset, Map<Tuple, Double> neighbours, String word) {
		VerbSynset verbSynset = (VerbSynset)synset;
		Double score = LESS_IMPORTANT;
		
		//System.out.println("Entailments:");
		for (VerbSynset entailments: verbSynset.getEntailments()) {	
			for (String wordForm: entailments.getWordForms()) {
				if (isPrimarySense(entailments, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(entailments).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		//System.out.println("Outcome:");
		for (VerbSynset outcome: verbSynset.getOutcomes()) {	
			for (String wordForm: outcome.getWordForms()) {
				if (isPrimarySense(outcome, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(outcome).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		//System.out.println("Topics:");
		for (Synset topic: verbSynset.getTopics()) {	
			for (String wordForm: topic.getWordForms()) {
				if (isPrimarySense(topic, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(topic).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		score = MOST_IMPORTANT;
		//System.out.println("Hypernyms:");
		for (Synset hypernym: verbSynset.getHypernyms()) {	
			for (String wordForm: hypernym.getWordForms()) {
				if (isPrimarySense(hypernym, wordForm)) {
					getVerbHypernyms(wordForm, neighbours, MAX_DISTANCE, score / DIVISION_FACTOR);
					if (!neighbours.containsKey(new Tuple(wordForm, getType(hypernym).toString())) || 
							neighbours.get(new Tuple(wordForm, getType(hypernym).toString())) < score) { 
						neighbours.put(new Tuple(wordForm, getType(hypernym).toString()), score);
						//System.out.println(wordForm + " " + score);
					}
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		//System.out.println("Troponyms:");
		for (Synset troponym: verbSynset.getTroponyms()) {	
			for (String wordForm: troponym.getWordForms()) {
				if (isPrimarySense(troponym, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(troponym).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
				
		}
		//System.out.println();
	}
	
	public static void getAdjectiveNeighbours(Synset synset, Map<Tuple, Double> neighbours, String word) {
		AdjectiveSynset adjectiveSynset = (AdjectiveSynset)synset;
		Double score = LESS_IMPORTANT;
		
		//System.out.println("Attributes:");
		for (NounSynset attributes: adjectiveSynset.getAttributes()) {	
			for (String wordForm: attributes.getWordForms()) {
				if (isPrimarySense(attributes, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(attributes).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		//System.out.println("Topics:");
		for (Synset topic: adjectiveSynset.getTopics()) {	
			for (String wordForm: topic.getWordForms()) {
				if (isPrimarySense(topic, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(topic).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		score = IMPORTANT;
		
		//System.out.println("Similar:");
		for (AdjectiveSynset similar: adjectiveSynset.getSimilar()) {	
			for (String wordForm: similar.getWordForms()) {
				if (isPrimarySense(similar, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(similar).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		//System.out.println("Related:");
		for (AdjectiveSynset related: adjectiveSynset.getRelated()) {	
			for (String wordForm: related.getWordForms()) {
				if (isPrimarySense(related, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(related).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		//System.out.println("Participle:");
		WordSense participle = adjectiveSynset.getParticiple(word);
		if (participle != null) {
			neighbours.put(new Tuple(participle.getWordForm(), getType(participle.getSynset()).toString()), score);
			//System.out.println(participle.getWordForm());	
			//System.out.println();
		}
		
		//System.out.println("Pertainyms:");
		for (WordSense pertainym: adjectiveSynset.getPertainyms(word)) {
			neighbours.put(new Tuple(pertainym.getWordForm(), getType(pertainym.getSynset()).toString()), score);
			//System.out.println(pertainym.getWordForm());	
		}
		//System.out.println();
	}
	
	public static void getAdverbNeighbours(Synset synset, Map<Tuple, Double> neighbours, String word) {
		AdverbSynset adverbSynset = (AdverbSynset)synset;
		Double score = LESS_IMPORTANT;
		
		//System.out.println("Topics:");
		for (Synset topic: adverbSynset.getTopics()) {	
			for (String wordForm: topic.getWordForms()) {
				if (isPrimarySense(topic, wordForm)) {
					neighbours.put(new Tuple(wordForm, getType(topic).toString()), score);
					//System.out.println(wordForm + " " + score);
				}
				else {
					//System.out.println("not added " + wordForm);
				}
			}
		}
		//System.out.println();
		
		score = IMPORTANT;
		
		//System.out.println("Pertainyms:");
		for (WordSense pertainym: adverbSynset.getPertainyms(word)) {
			neighbours.put(new Tuple(pertainym.getWordForm(), getType(pertainym.getSynset()).toString()), score);
			//System.out.println(pertainym.getWordForm());	
		}
		//System.out.println();
	}
	
	public static boolean isNoun(String word) {
		String tag = word.substring(word.lastIndexOf("{") + 1, word.lastIndexOf("}"));
		if (tag.startsWith("N")) {
			return true;
		}
		return false;
	}
	
	public static boolean isVerb(String word) {
		String tag = word.substring(word.lastIndexOf("{") + 1, word.lastIndexOf("}"));
		if (tag.startsWith("V")) {
			return true;
		}
		return false;
	}
	
	public static boolean isAdjective(String word) {
		String tag = word.substring(word.lastIndexOf("{") + 1, word.lastIndexOf("}"));
		if (tag.startsWith("J") || tag.equals("RP")) {
			return true;
		}
		return false;
	}
	
	public static boolean isAdverb(String word) {
		String tag = word.substring(word.lastIndexOf("{") + 1, word.lastIndexOf("}"));
		if (tag.startsWith("RB") || tag.equals("WRB") || tag.equals("RP")) {
			return true;
		}
		return false;
	}
	
	public static Integer getTypeFromStanfordTag(String word) {
		if (isNoun(word)) {
			return NOUN;
		}
		if (isVerb(word)) {
			return VERB;
		}
		if (isAdjective(word)) {
			return ADJECTIVE;
		}
		if (isAdverb(word)) {
			return ADVERB;
		}
		return NONE;
	}
	
	public static Integer getType(Synset synset) {
		SynsetType type = synset.getType();
		if (type == SynsetType.NOUN) {
			return NOUN;
		}
		if (type == SynsetType.VERB) {
			return VERB;
		}
		if (type == SynsetType.ADJECTIVE) {
			return ADJECTIVE;
		}
		if (type == SynsetType.ADVERB) {
			return ADVERB;
		}
		
		return NONE;
	}
	
	public static List<SynsetWrapper> sortSynsetByUsage(List<Synset> synsets, String word) {
		List<SynsetWrapper> wrapper = new ArrayList<SynsetWrapper>();
		for (int i = 0; i < synsets.size(); i++) {
			wrapper.add(new SynsetWrapper(synsets.get(i), word, i));
		}
		Collections.sort(wrapper);
		return wrapper;
	}
	
	public static boolean isPrimarySense(Synset synset, String word) {
		Integer wordType = getType(synset);
		Synset[] synsets = database.getSynsets(word);
		List<SynsetWrapper> wrapper = sortSynsetByUsage(new ArrayList<Synset>(Arrays.asList(synsets)), word);
		if (wrapper.isEmpty()) {
			return false;
		}
		for (int i = 0; i < wrapper.size(); i++) {
			Synset candidate = wrapper.get(i).getSynset();
			if (getType(candidate) == wordType) {
				return synset.equals(candidate);
			}
		}
		return false;
	}
	
	public static Map<Tuple, Double> createTaxonomy(String filename) {
		System.setProperty("wordnet.database.dir", "/usr/local/WordNet-3.0/dict/");
	
		database = WordNetDatabase.getFileInstance();
		
		String fileContent = Utilities.readFile(filename);
		String[] textLines = fileContent.split("\n");
		List<Synset> taxonomySynset = new ArrayList<Synset>();
		List<String> taxonomyString = new ArrayList<String>();
		for (String line: textLines) {
			if (line.startsWith("//")) {
				continue;
			}
			String[] tokens = line.split(" ");
			String word = tokens[0].trim();
			Integer synsetIndex = Integer.parseInt(tokens[1].trim());
			Synset[] synsets = database.getSynsets(word);
			if (synsets.length == 0) {
				//System.out.println("Niciun sinonim gasit in baza de date");
				return null;
			}
			if (synsetIndex > synsets.length) {
				//System.out.println("Index invalid");
				return null;
			}
		
			List<SynsetWrapper> wrapper = sortSynsetByUsage(new ArrayList<Synset>(Arrays.asList(synsets)), word);	
			Synset synset = wrapper.get(synsetIndex - 1).getSynset();
			taxonomySynset.add(synset);
			taxonomyString.add(word);
		}
		
		Map<Tuple, Double> neighbours = new HashMap<Tuple, Double>();
		for (int i = 0; i < taxonomySynset.size(); i++) {
			Synset synset = taxonomySynset.get(i);
			String word = taxonomyString.get(i);
			if (synset.getType() == SynsetType.NOUN) {
				getNounNeighbours(synset, neighbours);
			}
			else if (synset.getType() == SynsetType.VERB) {
				getVerbNeighbours(synset, neighbours, word);
			}
			else if (synset.getType() == SynsetType.ADJECTIVE) {
				getAdjectiveNeighbours(synset, neighbours, word);
			}
			else if (synset.getType() == SynsetType.ADVERB) {
				getAdverbNeighbours(synset, neighbours, word);
			}
			Integer type = getType(synset);
			neighbours.put(new Tuple(word, type.toString()), MOST_IMPORTANT);
		}
		
		Utilities.log("Taxonomy for " + filename + ":\n");
		Set< Entry<Tuple, Double> > entries = neighbours.entrySet();
		List< Entry<Tuple, Double> > entriesList = new ArrayList< Entry<Tuple, Double> >(entries);
		Collections.sort(entriesList, new Comparator< Entry<Tuple, Double> >() {

			@Override
			public int compare(Entry<Tuple, Double> o1, Entry<Tuple, Double> o2) {
				double x1 = o1.getValue();
				double x2 = o2.getValue();
				int y1 = (int)x1;
				int y2 = (int)x2;
				if (y1 < y2) {
					return 1;
				}
				if (y1 > y2) {
					return -1;
				}
				return o1.getKey().getFirst().compareTo(o2.getKey().getFirst());
			}
		});
		
		int ind = 0;
		for (Entry<Tuple, Double> e: entriesList) {
			if (ind != 0) {
				Utilities.log(", ");
			}
			ind++;
			Utilities.log(e.getKey().getFirst());
		}
		Utilities.log("\n");
		return neighbours;	
	}
}