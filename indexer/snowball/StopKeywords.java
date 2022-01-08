package edu.ucr.indexer.snowball;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class StopKeywords {
	private static Set<String> stopWords = new HashSet<String>();

	static {
		init();
	}

	public static void init() {

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					StopKeywords.class.getResourceAsStream("stopwords.txt")));
			while (reader.ready()) {
				String s = reader.readLine();
				StringTokenizer st = new StringTokenizer(s, " ,");
				while (st.hasMoreTokens()) {
					stopWords.add(st.nextToken());
				}
			}
			reader.close();
		} catch (IOException e) {
		}

	}

	public static boolean isStopWord(String e) {
		return stopWords.contains(e.toLowerCase());
	}

	public static void main(String[] args) {
		System.out.println(isStopWord("at"));
	}

	public static Set<String> get() {
		return stopWords;
	}

}
