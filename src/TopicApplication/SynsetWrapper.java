package TopicApplication;

import edu.smu.tspell.wordnet.Synset;

public class SynsetWrapper implements Comparable<SynsetWrapper> {
	private Synset synset;
	private String word;
	private int index;
	
	public SynsetWrapper(Synset synset, String word, int index) {
		this.synset = synset;
		this.word = word;
		this.index = index;
	}
	
	@Override
	public int compareTo(SynsetWrapper o) {
		Synset hisSyset = o.getSynset();
		String hisWord = o.getWord();
		int hisIndex = o.getIndex();
	
		int myCount = 0;
		int hisCount = 0;
		try {
			myCount = synset.getTagCount(word);
			hisCount = hisSyset.getTagCount(hisWord);
		}
		catch (Exception e){ }
		
		if (myCount == hisCount) {
			return this.index - hisIndex;
		}
		return hisCount - myCount;
	}

	public Synset getSynset() {
		return this.synset;
	}

	public String getWord() {
		return this.word;
	}

	public int getIndex() {
		return this.index;
	}
}
