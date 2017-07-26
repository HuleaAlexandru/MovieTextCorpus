package Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import MovieUnits.Tuple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class Utilities {

	public static void createFolder(String path) {
		File folder = new File(path);
		
		if (!folder.exists()) {
			boolean created = folder.mkdir();
			if (!created) {
				Utilities.log("[" + Constants.ERROR + "]: " + path + " folder not created. Exit program\n");
				System.exit(1);
			}
		}
	}
	
	public static String readFile(String filename) {
		File file = new File(filename);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1));
			StringBuilder text = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null) {                         
			    text.append(line);
			    text.append("\n");
			}
			reader.close();
			return new String(text);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void writeFile(String text, String path) {
		try {
			File outputFile = new File(path);
			if (outputFile.exists()) {
				return;
			}
			PrintWriter writer = new PrintWriter(path, "ISO-8859-1");
			writer.write(text);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static Integer parseInt(String s) {
		s = s.replaceAll("\\D+","");
		
		int index = 0;
		while(index < s.length() && s.charAt(index) == '0') {
			index++;
		}
		if (index == s.length()) {
			return 0;
		}
		s = s.substring(index);
		return Integer.parseInt(s);
	}
	
	public static Integer allUpperCase(String line) {
		boolean existsChar = false;
		for (int j = 0; j < line.length(); j++) {
			if (Character.isLetter(line.charAt(j))){
				existsChar = true;
				if (Character.isLowerCase(line.charAt(j))) {
					return Constants.ERROR_CODE;
				}
			}
		}
		if (existsChar) {
			return Constants.UPPERCASE;
		}
		return Constants.NOLETTERS;
	}
	
	public static int getIndentationLevel(String line) {
		if (line.trim().isEmpty()) {
			return Constants.ERROR_CODE;
		}
		int result = 0;
		for (int i = 0; i < line.length(); i++) {
			if (!Character.isWhitespace(line.charAt(i))) {
				result = i;
				break;
			}
		}
		return result;
	}
	
	public static void addToHashMap(HashMap<Integer, Integer> hashMap, Integer key) {
		Integer value = hashMap.get(key);
		if (value == null) {
			hashMap.put(key, 1);
		}
		else {
			hashMap.put(key, value + 1);
		}
	}
	
	public static Integer getBestKeyFromHashMap(HashMap<Integer, Integer> hashMap) {
		boolean error = true;
		int maxKey = -1;
		int maxValue = -1;
		for (Entry<Integer, Integer> entry: hashMap.entrySet()) {
			Integer key = entry.getKey();
			Integer value = entry.getValue();
			if (value > maxValue) {
				maxValue = value;
				maxKey = key;
				error = false;
			}
			else if (value == maxValue) {
				error = true;
			}
		}
		if (error) {
			return Constants.ERROR_CODE;
		}
		return maxKey;
	}
	
	public static boolean mostlyUpperCase(String line, int error) {
		int i = 0;
		while (i < line.length() && error >= 0) {
			if (Character.isLowerCase(line.charAt(i))) {
				error -= 1;
			}
			i++;
		}
		if (i == line.length()) {
			return true;
		}
		return false;
	}
	
	public static List<String> getSentences(String text) {
		text = text.trim();
		List<String> sentences = new ArrayList<String>();
		List<Integer> v = new ArrayList<Integer>();
		
		StringBuilder dots = new StringBuilder();
		for (int i = 10; i > 1; i--) {
			dots.setLength(0);
			for (int j = i; j > 0; j--) {
				dots.append(".");
			}
			int index = 0;
			while (true) {
				int result = text.indexOf(dots.toString(), index);
				if (result != -1) {
					v.add(result);
					index = result + 1;
				}
				else {
					break;
				}
			}
		}

		String separators = "!|\\?|\\.";
		String[] tokens = text.split(separators);
		
		int len = 0;
		StringBuilder sentence = new StringBuilder();
		
		for (String token : tokens) {
			int separatorIndex = len + token.length();
			len += token.length() + 1;
			token = token.replaceAll("(\r)*(\n)+|(\r)+(\n)*", " ");
			token = token.replaceAll(" +", " ");
			token = token.trim();
			if (text.length() <= separatorIndex) {
				sentence.append(token);
				sentences.add(sentence.toString());
				sentence.setLength(0);
			}
			else {
				switch (text.charAt(separatorIndex)) {
				case '?':
					sentence.append(token);
					sentence.append("?");
					if (!sentence.equals("?")) {
						sentences.add(sentence.toString());
					}
					sentence.setLength(0);
					break;
	
				case '!':
					sentence.append(token);
					sentence.append("!");
					if (!sentence.equals("!")) {
						sentences.add(sentence.toString());
					}
					sentence.setLength(0);
					break;
					
				case '.':
					boolean found = false;
					for (Integer i : v) {
						if (separatorIndex >= i && separatorIndex <= i + 2) {
							found = true;
						}
					}
					if (found) {
						sentence.append(token);
						sentence.append(".");
					}
					else {
						sentence.append(token);
						sentence.append(".");
						if (!sentence.equals(".")) {
							sentences.add(sentence.toString());
						}
						sentence.setLength(0);
						break;
					}
					
				default:
					break;
				}
			}
		}
		if (!sentence.equals("")) {
			sentences.add(sentence.toString());
		}
		return sentences;
	}

	public static List<String> getSentencesBasedOnStanfordNLP(String text, StanfordCoreNLP sentenceSplitter) {	
		List<String> result = new ArrayList<String>();
		Annotation annotation = new Annotation(text);
		sentenceSplitter.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap sentence: sentences) {
			result.add(sentence.get(TextAnnotation.class));
		}
		return result;
	}
	
	public static void startLog() {
		FileWriter writer;
		try {
			writer = new FileWriter(Constants.LOG_FILENAME);
			writer.close();
		} catch (IOException e) {
			System.out.println("[" + Constants.EXCEPTION + "] " + "[Utilities]: Exception raised trying to start log");
		}
	}
	
	public static void log(String message) {
		FileWriter writer;
		try {
			writer = new FileWriter(Constants.LOG_FILENAME, true);
			writer.append(message);
			writer.close();
		} catch (IOException e) {
			System.out.println("[" + Constants.EXCEPTION + "] " + "[Utilities]: Exception raised trying to log");
		}
	}
	
	public static List<Tuple> sortMap(Map<String, Integer> map) {
		List<Tuple> result = new ArrayList<Tuple>();
		
		for (Entry<String, Integer> e : map.entrySet()) {
			result.add(new Tuple(e.getKey(), e.getValue().toString()));
		}
		Collections.sort(result, new Comparator<Tuple>() {

			@Override
			public int compare(Tuple o1, Tuple o2) {
				Integer i1 = Integer.parseInt(o1.getSecond());
				Integer i2 = Integer.parseInt(o2.getSecond());
				return i2 - i1;
			}
		});
		return result;
	}
	
	public static StringBuilder resultsGenerator(String header, String value) {
		StringBuilder result = new StringBuilder();
		result.append(Constants.PARANTHESIS_OPEN);
		result.append(header);
		result.append(Constants.PARANTHESIS_CLOSE);
		result.append(": ");
		result.append(value);
		result.append("\n");
		return result;
	}
}
