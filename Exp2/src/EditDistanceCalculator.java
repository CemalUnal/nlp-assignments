import java.util.*;
import java.util.regex.Pattern;

public class EditDistanceCalculator {

    private static Map<List<String>, Double> insertionInfoMap = new HashMap<>();
    private static Map<List<String>, Double> deletionInfoMap = new HashMap<>();
    private static Map<List<String>, Double> substitutionInfoMap = new HashMap<>();

    private static String typeOfOperation;
    private static String correctLetters;
    private static String wrongLetters;

    /**
     * Determines whether the minimum edit distance of two words is
     * equal to 1.
     * Also it fills the insertion, substitution, deletion maps
     * according to type of operation
     *
     * @param word wrong word
     * @param unigramCountsMap unigram counts in the dataset
     *
     * @return true if minimum edit distance is one
     *         false if minimum edit distance is not one
     */
    public boolean minEditDistanceIsOne(String word, Map<String, Double> unigramCountsMap) {
        boolean minEditDistanceIsOne = false;

        for (Map.Entry<String, Double> entry : unigramCountsMap.entrySet()) {
            String datasetToken = entry.getKey();

            minEditDistanceIsOne = getMinEditDistance(datasetToken, word);

            if (minEditDistanceIsOne) {
                switch (typeOfOperation) {
                    case "insertion":
                        addToInsertionInfoMap(correctLetters, wrongLetters);
                        break;
                    case "deletion":
                        addToDeletionInfoMap(correctLetters, wrongLetters);
                        break;
                    case "substitution":
                        addToSubstitutionInfoMap(correctLetters, wrongLetters);
                        break;
                }
            }
        }

        return minEditDistanceIsOne;
    }

    /**
     * Determines whether the minimum edit distance of two words is
     * equal to 1.
     * Also determines the type of operation (insertion, deletion, substitution)
     * and determines the letters that are replaced to get the wrong word.
     *
     * @param correctWord correct word in the unigram map
     * @param wrongWord wrong word
     *
     * @return true if minimum edit distance is one
     *         false if minimum edit distance is not one
     */
    public boolean getMinEditDistance(String correctWord, String wrongWord) {
        int lengthOfCorrectWord = correctWord.length();
        int lengthOfWrongWord = wrongWord.length();

        // neither length of two words are not same nor differences between them is is not 1,
        // then do not need to calculate minimum edit distance
        if (!(Math.abs(lengthOfCorrectWord - lengthOfWrongWord) == 1 || lengthOfCorrectWord - lengthOfWrongWord == 0)) {
            return false;
        }

        if (lengthOfWrongWord > lengthOfCorrectWord) {
            typeOfOperation = "insertion";
            char previousChar = '#';
            for (int i = 0; i < lengthOfWrongWord; i++) {
                if (i == lengthOfCorrectWord || correctWord.charAt(i) != wrongWord.charAt(i)) {
                    correctLetters = String.valueOf(previousChar);
                    wrongLetters = correctLetters + String.valueOf(wrongWord.charAt(i));
                    return true;
                }
                previousChar = wrongWord.charAt(i);
            }
        } else if (lengthOfCorrectWord > lengthOfWrongWord) {
            typeOfOperation = "deletion";
            char previousChar = '#';
            for (int i = 0; i < lengthOfCorrectWord; i++) {
                if (i == lengthOfWrongWord || wrongWord.charAt(i) != correctWord.charAt(i)) {
                    correctLetters = String.valueOf(previousChar) + String.valueOf(correctWord.charAt(i));
                    wrongLetters = String.valueOf(previousChar);
                    return true;
                }
                previousChar = wrongWord.charAt(i);
            }
        } else {
            typeOfOperation = "substitution";
            for (int i = 0; i < lengthOfWrongWord; i++) {
                if (wrongWord.charAt(i) != correctWord.charAt(i)) {
                    correctLetters = String.valueOf(correctWord.charAt(i));
                    wrongLetters = String.valueOf(wrongWord.charAt(i));
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Calculates the bigram probability
     *
     * @param unigramToken unigram token
     * @param bigramToken bigram token
     *
     * @return calculated probability
     */
    private double calculateProbability(String unigramToken, String bigramToken) {
        Preprocessing preprocessing = new Preprocessing();
        double probability = 0.0;

        Map<String, Double> unigramCountsMap = preprocessing.getUnigramCountsMap();
        Map<String, Double> bigramCountsMap = preprocessing.getBigramCountsMap();

        if (bigramCountsMap.containsKey(bigramToken) && unigramCountsMap.containsKey(unigramToken)) {
            probability = bigramCountsMap.get(bigramToken) / unigramCountsMap.get(unigramToken);
        }

        return probability;
    }

    /**
     * Calculates the transition probability by
     * calling the bigram probability calculator method
     *
     * For example bigram token is "I watch"
     * then previousWord is I and currentWord is watch.
     *
     * @param previousWord previous word
     * @param currentWord current word
     *
     * @return calculated transition probability
     */
    public double getTransitionProbability(String previousWord, String currentWord) {
        String bigramToken = previousWord + " " + currentWord;

        return calculateProbability(previousWord, bigramToken);
    }

    /**
     * Calculates the emission probability by
     * using the insertion, substitution, deletion maps
     *
     * For example; the correct word is "actress" and the wrong
     * word is  "acress" then the correctLetters will be
     * "ct" and the wrongLetters will be "c"
     *
     * Probability is equal to the number of events "ct"
     * will become "t" divided by "ct" count in the whole dataset.
     *
     * @param correctLetters correct letters
     * @param wrongLetters wrong letters
     *
     * @return calculated emission probability
     */
    public double getEmissionProbability(String correctLetters, String wrongLetters) {
        double numerator = 0.0;
        double denominator;

        List<String> temp = Arrays.asList(correctLetters, wrongLetters);

        if (insertionInfoMap.containsKey(temp)) {
            numerator = insertionInfoMap.get(temp);
        }

        else if (deletionInfoMap.containsKey(temp)) {
            numerator = deletionInfoMap.get(temp);
        }

        else if (substitutionInfoMap.containsKey(temp)) {
            numerator = substitutionInfoMap.get(temp);
        }

        denominator = getWordCount(correctLetters);

        return numerator / denominator;
    }

    /**
     * Calculates the number of occurrences of given word
     * in the given dataset.
     *
     * @param word word
     *
     * @return number of occurrences in the given dataset
     */
    private double getWordCount(String word) {
        Preprocessing preprocessing = new Preprocessing();
        Map<String, Double> unigramCountsMap = preprocessing.getUnigramCountsMap();
        double count = 0.0;

        for (Map.Entry<String, Double> entry: unigramCountsMap.entrySet()) {
            double tempCount = entry.getKey().split(Pattern.quote(word), -1).length - 1;
            tempCount = tempCount * entry.getValue();
            count = count + tempCount;
        }
        return count;
    }

    /**
     * Adds correctLetters and wrongLetters to
     * insertion, substitution, deletion maps
     *
     * @param map insertion, substitution, deletion maps
     * @param correctLetters correct letters
     * @param wrongLetters wrong letters
     *
     */
    private void addToInfoMap(Map<List<String>, Double> map, String correctLetters, String wrongLetters) {
        if (map.containsKey(Arrays.asList(correctLetters, wrongLetters))) {
            double currentFrequency = map.get(Arrays.asList(correctLetters, wrongLetters));
            currentFrequency = currentFrequency + 1.0;
            map.put(
                    // unmodifiable so key cannot change hash code
                    Collections.unmodifiableList(Arrays.asList(correctLetters, wrongLetters)), currentFrequency
            );
        } else {
            map.put(
                    // unmodifiable so key cannot change hash code
                    Collections.unmodifiableList(Arrays.asList(correctLetters, wrongLetters)), 1.0
            );
        }
    }

    /**
     * Calls addToInfoMap with correctLetters and wrongLetters
     * and insertionInfoMap
     *
     * @param correctLetters correct letters
     * @param wrongLetters wrong letters
     *
     */
    public void addToInsertionInfoMap(String correctLetters, String wrongLetters) {
        addToInfoMap(insertionInfoMap, correctLetters, wrongLetters);
    }

    /**
     * Calls addToInfoMap with correctLetters and wrongLetters
     * and deletionInfoMap
     *
     * @param correctLetters correct letters
     * @param wrongLetters wrong letters
     *
     */
    public void addToDeletionInfoMap(String correctLetters, String wrongLetters) {
        addToInfoMap(deletionInfoMap, correctLetters, wrongLetters);
    }

    /**
     * Calls addToInfoMap with correctLetters and wrongLetters
     * and substitutionInfoMap
     *
     * @param correctLetters correct letters
     * @param wrongLetters wrong letters
     *
     */
    public void addToSubstitutionInfoMap(String correctLetters, String wrongLetters) {
        addToInfoMap(substitutionInfoMap, correctLetters, wrongLetters);
    }

    /**
     * Returns correctLetters
     *
     * @return correctLetters
     *
     */
    public String getCorrectLetters() {
        return correctLetters;
    }

    /**
     * Returns wrongLetters
     *
     * @return wrongLetters
     *
     */
    public String getWrongLetters() {
        return wrongLetters;
    }
}