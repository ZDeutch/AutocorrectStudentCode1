import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 *
 * @author Zach Blick
 * @author Zander Deutch
 */
public class Autocorrect {
    // Instance variables
    private final String[] words;
    private final int threshold;
    private final HashSet<String> dictionary;

    // HashMap to find the frequency of each word
    private final HashMap<String, Integer> frequency;

    /**
     * Constucts an instance of the Autocorrect class.
     *
     * @param words     The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */
    // Constructor
    public Autocorrect(String[] words, int threshold) {
        this.words = words;
        this.threshold = threshold;
        this.dictionary = new HashSet<>();
        this.frequency = new HashMap<>();

        // Add each word to the dictionary
        for (int i = 0; i < words.length; i++) {
            dictionary.add(words[i]);
        }

        // For frequency, add the file and read in each word and frequency into the hashmap
        try {
            BufferedReader reader = new BufferedReader(new FileReader("wordFrequency.txt"));
            String line = reader.readLine();
            while (line != null) {
                String word = line.split(" ")[0];
                int freq = Integer.parseInt(line.split(" ")[1]);
                frequency.put(word, freq);
                line = reader.readLine();
            }
            // otherwise throuw an error
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    // Getter to see the frequency of a given word
    public int getFrequency(String word) {
        if (frequency.containsKey(word)) {
            return frequency.get(word);
        }
        return 0;
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     *
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distnace, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        // Normalize to lowercase
        typed = typed.toLowerCase(Locale.ROOT);

        // If the word is spelled correctly then no checks are needed
        if (dictionary.contains(typed)) {
            return new String[0];
        }

        // Array to track all words within the edit threshold
        ArrayList<String> matches = new ArrayList<>();

        // Go through each word and determine its edit distance
        // If it meets the edit threshold then add to the matches
        for (int i = 0; i < words.length; i++) {
            int distance = editDistance(typed, words[i]);
            if (distance <= threshold) {
                matches.add(words[i]);
            }
        }

        // Bubble sort to determine the order of the suggestions
        for (int i = 0; i < matches.size(); i++) {
            for (int j = i + 1; j < matches.size(); j++) {
                int dist1 = editDistance(typed, matches.get(i));
                int dist2 = editDistance(typed, matches.get(j));

                // Sort by edit distance, in the event of a tie use the alphabetically earlier word
//                if (dist1 > dist2 || dist1 == dist2 && matches.get(i).compareTo(matches.get(j)) > 0) {
                // If breaking the tie by frequency, then see which has a higher frequency score
                if (dist1 > dist2 || dist1 == dist2 && getFrequency(matches.get(i)) < getFrequency(matches.get(j))) {
                    String temp = matches.get(i);
                    matches.set(i, matches.get(j));
                    matches.set(j, temp);
                }

            }
        }

        // Return the matches
        return matches.toArray(new String[0]);
    }

    public String topRecommendation(String typed) {
        typed = typed.toLowerCase(Locale.ROOT);

        if(dictionary.contains(typed)) {
            return "Word is Correctly Spelled";
        }

        String best = null;
        int bestDist = threshold + 1;

        for(String word: frequency.keySet()) {
            int dist = editDistance(typed, word);
            if(dist < bestDist || (dist == bestDist && getFrequency(word) > getFrequency(best))) {
                best = word;
                bestDist = dist;
            }
        }

        if(best != null) {
            return "Top Recommendation for " + typed + ": " + best;
        }

        String[] results = runTest(typed);
        if(results.length == 0) {
            return "No Matches Found.";
        }

        return "Top Recommendation for " + typed + ": " + results[0];
    }

    // Helper method to determine edit distance
    public int editDistance(String a, String b) {
        int m = a.length();
        int n = b.length();

        // Create your 2d array for tabulation approach
        int[][] dp = new int[m + 1][n + 1];


        // Base cases for the first row and first column
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int i = 0; i <= n; i++) {
            dp[0][i] = i;
        }
        // Go through the rest of the table and fill it out
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                // IF the chars are a match, then use the score from the top left diagonal
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                    // Otherwise add 1 to the minimum edit distance between the neighbors
                    // Each neighbor represents one manipulation: deletion, insertion, or substitution
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }

        // Return the final table element representing the total edit distance
        return dp[m][n];


    }


    /**
     * Loads a dictionary of words from the provided textfiles in the dictionaries directory.
     *
     * @param dictionary The name of the textfile, [dictionary].txt, in the dictionaries directory.
     * @return An array of Strings containing all words in alphabetical order.
     */
    private static String[] loadDictionary(String dictionary) {
        try {
            String line;
            BufferedReader dictReader = new BufferedReader(new FileReader("dictionaries/" + dictionary + ".txt"));
            line = dictReader.readLine();

            // Update instance variables with test data
            int n = Integer.parseInt(line);
            String[] words = new String[n];

            for (int i = 0; i < n; i++) {
                line = dictReader.readLine();
                words[i] = line;
            }
            return words;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Main method to type in terminal continuously
    public static void main(String[] args) {
        String[] words = loadDictionary("large");
        Autocorrect ac = new Autocorrect(words, 2);

        Scanner s = new Scanner(System.in);
        String word;
        // Switch with any word to see the top recommendation
        while (true) {
            word = s.nextLine();
            System.out.println(ac.topRecommendation(word));
        }
    }
}

