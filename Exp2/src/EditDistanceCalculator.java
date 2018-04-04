import java.util.*;
import java.util.regex.Pattern;

public class EditDistanceCalculator {

    private static Map<List<String>, Double> insertionInfoMap = new HashMap<>();
    private static Map<List<String>, Double> deletionInfoMap = new HashMap<>();
    private static Map<List<String>, Double> substitutionInfoMap = new HashMap<>();

    private static String typeOfOperation;
    private static String correctLetters;
    private static String wrongLetters;

    public boolean minEditDistanceIsOne(String word, Map<String, Double> unigramCountsMap) {
        double minEditDistance;
        boolean minEditDistanceIsOne = false;

        for (Map.Entry<String, Double> entry : unigramCountsMap.entrySet()) {
            String datasetToken = entry.getKey();
            int tokenLength = datasetToken.length();
            int wordLength = word.length();

            if (Math.abs(tokenLength - wordLength) == 1 || tokenLength - wordLength == 0) {
                minEditDistance = getMinEditDistance(datasetToken, word, tokenLength, wordLength);

                if (minEditDistance == 1) {
                    if (typeOfOperation.equals("insertion")) {
                        addToInsertionInfoMap(correctLetters, wrongLetters);
                    } else if (typeOfOperation.equals("deletion")) {
                        addToDeletionInfoMap(correctLetters, wrongLetters);
                    } else if (typeOfOperation.equals("substitution")) {
                        addToSubstitutionInfoMap(correctLetters, wrongLetters);
                    }
                    minEditDistanceIsOne = true;
//                    return minEditDistance;
                }
            }
        }

        return minEditDistanceIsOne;
    }

    public int getMinEditDistance(String token, String word, int tokenLength, int wordLength) {
        // If the correctWord is empty, the cost is equal to
        // the length of wrongWord
        if (tokenLength == 0) {
            if (wordLength - 1 == 0) {
                typeOfOperation = "insertion";
                String currentLetter = String.valueOf(word.charAt(wordLength - 1));
                String previousLetter = "#";

                correctLetters = String.valueOf(previousLetter);
                wrongLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
            }
            return wordLength;
        }

        // If the wrongWord is empty, the cost is equal to
        // the length of the correctWord
        if (wordLength == 0) {
            if (tokenLength - 1 == 0) {
                typeOfOperation = "deletion";
                char currentLetter = token.charAt(tokenLength - 1);
                char previousLetter = '-';

                correctLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
                wrongLetters = String.valueOf(previousLetter);
            }
            return tokenLength;
        }

        // If last characters of two strings are same, nothing
        // much to do. Ignore last characters and get count for
        // remaining strings.
        if (token.charAt(tokenLength - 1) == word.charAt(wordLength - 1))
            return getMinEditDistance(token, word, tokenLength - 1, wordLength - 1);

        // If last characters are not same, calculate all three operation cost
        int insertionEditDistance = getMinEditDistance(token, word, tokenLength, wordLength - 1);
        int deletionEditDistance = getMinEditDistance(token, word, tokenLength - 1, wordLength);
        int substitutionEditDistance = getMinEditDistance(token, word, tokenLength - 1, wordLength - 1);

        int minEditDistance;

        // type is insertion
        if (insertionEditDistance <= deletionEditDistance && insertionEditDistance <= substitutionEditDistance) {
            typeOfOperation = "insertion";
            String currentLetter = String.valueOf(word.charAt(wordLength - 1));
            String previousLetter = "#";

            if (wordLength - 2 >= 0) {
                previousLetter = String.valueOf(word.charAt(wordLength - 2));
            }

            correctLetters = String.valueOf(previousLetter);
            wrongLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);

            minEditDistance = insertionEditDistance;
        }
        // type is deletion
        else if (deletionEditDistance <= insertionEditDistance && deletionEditDistance <= substitutionEditDistance) {
            typeOfOperation = "deletion";

            char currentLetter = token.charAt(tokenLength - 1);
            char previousLetter = '-';

            if (wordLength - 1 >= 0) {
                previousLetter = word.charAt(wordLength - 1);
            }

            correctLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
            wrongLetters = String.valueOf(previousLetter);

            minEditDistance = deletionEditDistance;
        }
        // type is substitution
        else {
            typeOfOperation = "substitution";
            char currentLetter = token.charAt(tokenLength - 1);
            char previousLetter = word.charAt(wordLength - 1);

            correctLetters = String.valueOf(currentLetter);
            wrongLetters = String.valueOf(previousLetter);

            minEditDistance = substitutionEditDistance;
        }

        return 1 + minEditDistance;
    }

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


    public double getTransitionProbability(String previousWord, String currentWord) {
        String bigramToken = previousWord + " " + currentWord;

        return calculateProbability(previousWord, bigramToken);
    }

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

    public void addToInsertionInfoMap(String correctLetters, String wrongLetters) {
        addToInfoMap(insertionInfoMap, correctLetters, wrongLetters);
    }

    public void addToDeletionInfoMap(String correctLetters, String wrongLetters) {
        addToInfoMap(deletionInfoMap, correctLetters, wrongLetters);
    }

    public void addToSubstitutionInfoMap(String correctLetters, String wrongLetters) {
        addToInfoMap(substitutionInfoMap, correctLetters, wrongLetters);
    }

    public Map<List<String>, Double> getInsertionInfoMap() {
        return insertionInfoMap;
    }

    public Map<List<String>, Double> getDeletionInfoMap() {
        return deletionInfoMap;
    }

    public Map<List<String>, Double> getSubstitutionInfoMap() {
        return substitutionInfoMap;
    }

    public String getCorrectLetters() {
        return correctLetters;
    }

    public String getWrongLetters() {
        return wrongLetters;
    }
}