import javax.print.DocFlavor;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

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
    private final String[] words;
    private final int threshold;
    private final HashSet<String> dictionary;

    /**
     * Constucts an instance of the Autocorrect class.
     *
     * @param words     The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */
    public Autocorrect(String[] words, int threshold) {
        this.words = words;
        this.threshold = threshold;
        this.dictionary = new HashSet<>();

        for (int i = 0; i < words.length; i++) {
            dictionary.add(words[i]);
        }
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     *
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distnace, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        if (dictionary.contains(typed)) {
            return new String[0];
        }

        ArrayList<String> matches = new ArrayList<>();

        for (int i = 0; i < words.length; i++) {
            int distance = editDistance(typed, words[i]);
            if (distance <= threshold) {
                matches.add(words[i]);
            }
        }

        for (int i = 0; i < matches.size(); i++) {
            for (int j = i + 1; j < matches.size(); j++) {
                int dist1 = editDistance(typed, matches.get(i));
                int dist2 = editDistance(typed, matches.get(j));
                if (dist1 > dist2 || dist1 == dist2 && matches.get(i).compareTo(matches.get(j)) > 0) {
                    String temp = matches.get(i);
                    matches.set(i, matches.get(j));
                    matches.set(j, temp);
                }

            }
        }

        return matches.toArray(new String[0]);
    }

    private int editDistance(String a, String b) {
        int m = a.length();
        int n = b.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int i = 0; i <= n; i++) {
            dp[0][i] = i;
        }

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }

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
}