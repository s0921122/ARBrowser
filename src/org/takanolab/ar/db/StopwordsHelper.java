package org.takanolab.ar.db;

import java.util.TreeSet;

public class StopwordsHelper {
	String[] stopwords = null;
    static TreeSet<String> stopwordsSet = new TreeSet<String>();

    // From Lucene
    public static final String[] ENGLISH_STOP_WORDS = {
    	"a", "an", "and", "are", "as", "at", "be", "but", "by",
        "for", "if", "in", "into", "is", "it",
        "no", "not", "of", "on", "or", "such",
        "that", "the", "their", "then", "there", "these",
        "they", "this", "to", "was", "will", "with", "why"
    };
    
    public static final String[] STOP_WORDS =
    {
        "00", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
        "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
        "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
        "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
        "50", "51", "52", "53", "54", "55", "56", "57", "58", "59",
        "60", "61", "62", "63", "64", "65", "66", "67", "68", "69",
        "70", "71", "72", "73", "74", "75", "76", "77", "78", "79",
        "80", "81", "82", "83", "84", "85", "86", "87", "88", "89",
        "90", "91", "92", "93", "94", "95", "96", "97", "98", "99",
        "100", "000", "$", "01", "02", "03", "04", "05", "06", "07", "08", "09",
        "about", "after", "all", "also", "an", "and",
        "another", "any", "are", "as", "at", "be",
        "because", "been", "before", "being", "between",
        "both", "but", "by", "came", "can", "come",
        "could", "did", "do", "does", "each", "else",
        "for", "from", "get", "got", "has", "had",
        "he", "have", "her", "here", "him", "himself",
        "his", "how","if", "in", "into", "is", "it",
        "its", "just", "like", "make", "many", "me",
        "might", "more", "most", "much", "must", "my",
        "never", "now", "of", "on", "only", "or",
        "other", "our", "out", "over", "re", "said",
        "same", "see", "should", "since", "so", "some",
        "still", "such", "take", "than", "that", "the",
        "their", "them", "then", "there", "these",
        "they", "this", "those", "through", "to", "too",
        "under", "up", "use", "very", "want", "was",
        "way", "we", "well", "were", "what", "when",
        "where", "which", "while", "who", "will",
        "with", "would", "you", "your", "us",
        "a", "b", "c", "d", "e", "f", "g", "h", "i",
        "j", "k", "l", "m", "n", "o", "p", "q", "r",
        "s", "t", "u", "v", "w", "x", "y", "z",
        "wikipedia", "search", "scholar", "news", "maps", "oh"
        //"img", "src", "div", "span", "class", "br", "href", "ismap", "boarder"
    };
    
    public StopwordsHelper() {
            for (String term: ENGLISH_STOP_WORDS) {
                stopwordsSet.add(term);
            }
            for (String term: STOP_WORDS) {
                stopwordsSet.add(term);
            }

    }
    
    public boolean isStopword(String term) {
    	boolean b = stopwordsSet.contains(term);
    	// For debug
//    	if (b) {
//    		System.out.println(term + " is stopword");
//    	} else {
//    		System.out.println(term + " is not stopword");
//    	}
    	
    	return b;
    }
    
    public TreeSet getStopWoredsSet() {
        return this.stopwordsSet;
    }
    
    public String[] getStopWords() {
        return this.stopwords;
    }
    
}
