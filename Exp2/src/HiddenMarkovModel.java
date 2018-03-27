import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiddenMarkovModel {

    /* Holds the Bigram counts. Bigram Language Model */
    private static Map<String, Double> bigramCountsMap = new HashMap<>();
    /* Holds the Unigram counts. Unigram Language Model */
    private static Map<String, Double> unigramCountsMap = new HashMap<>();

    private static Map<List<String>, Double> insertionInfoMap = new HashMap<>();
    private static Map<List<String>, Double> deletionInfoMap = new HashMap<>();
    private static Map<List<String>, Double> substitutionInfoMap = new HashMap<>();

    private static double totalCount = 1.0;

    /**
     * Adds given word to given map. And it handles frequencies inside itself.
     *
     * @param map a map to add an element to it
     * @param word a word to add to map
     */
    private void addWordToMap(Map<String, Double> map,  String word) {
        if (map.containsKey(word)) {
            double currentFrequency = map.get(word);
            currentFrequency = currentFrequency + 1.0;
            map.put(word, currentFrequency);
        } else {
            map.put(word, 1.0);
        }
    }

    /**
     * Creates the unigram model.
     * Adds all unigrams in the training set to a hashmap by calling
     * the addWordToMap method of its parent class.
     *
     * @param trainingSet training set
     */
    public void createUnigramModel(List<String> trainingSet) {
        for (String sentence : trainingSet) {
            String[] words = sentence.split("\\s+");
            for (String word : words) {
                addWordToMap(unigramCountsMap, word);
                totalCount++;
            }
        }
    }

    /**
     * Creates the bigram model.
     * Adds all bigrams in the training set to a hashmap by calling
     * the addWordToMap method of its parent class.
     *
     * @param trainingSet training set
     */
    public void createBigramModel(List<String> trainingSet) {
        for (String sentence : trainingSet) {
            String[] words = sentence.split("\\s+");
            for (int i = 1; i < words.length; i++) {
                String bigramToken = words[i - 1] + " " + words[i];
                addWordToMap(bigramCountsMap, bigramToken);
            }
        }
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

    private double calculateProbability(String unigramToken, String bigramToken) {
        double probability = 0.0;

        if (bigramCountsMap.containsKey(bigramToken) && unigramCountsMap.containsKey(unigramToken)) {
            probability = bigramCountsMap.get(bigramToken) / unigramCountsMap.get(unigramToken);
        }

        return probability;
    }

    public double getInitialProbability(String word) {
        String unigramToken = "<s>";
        String bigramToken = unigramToken + " " + word;

        return calculateProbability(unigramToken, bigramToken);
    }

    public double getTransitionProbability(String previousWord, String currentWord) {
        String bigramToken = previousWord + " " + currentWord;

        return calculateProbability(previousWord, bigramToken);
    }

    public double getEmissionProbability(String correctLetters, String wrongLetters) {
        double numerator = 0.0;
        double denominator;

//        for (Map.Entry<String, EditDistanceDetails> entry: editDetailsOfWrongWords.entrySet()) {
//            String tempCorrectLetters = entry.getValue().getCorrectLetters();
//            String tempWrongLetters = entry.getValue().getWrongLetters();
//            if (tempCorrectLetters.equals(correctLetters) && tempWrongLetters.equals(wrongLetters)) {
//                numerator = numerator + 1.0;
//            }
//        }

//        denominator = getWordCount("c");

//        System.out.println(denominator);
//        String bigramToken = previousWord + " " + currentWord;

        return numerator;
    }

//    private double getWordCount(String word) {
//        double count = 0.0;
//        List<String> allLines = FReader.getAllLines();
//
////        Pattern pattern = Pattern.compile("cr");
//        for (String line : allLines) {
//            count = count + line.split(Pattern.quote(word), -1).length - 1;
//        }
//
//        return count;
//    }

//    private static double getTransitionProbability(String type, String wrongWord, String correctionWord) {
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(correctionWord.charAt(deleteIndex - 1));
//        stringBuilder.append(correctionWord.charAt(deleteIndex));
//
//        String correctWordPart = stringBuilder.toString();
//        char missingWordPart = correctionWord.charAt(deleteIndex);
////        correctWordPart kac kere missingWordPart'a donusmus
////        correctWordPart'in count'unu bul
//    }

    public Map<List<String>, Double> getInsertionInfoMap() {
        return insertionInfoMap;
    }

    public Map<List<String>, Double> getDeletionInfoMap() {
        return deletionInfoMap;
    }

    public Map<List<String>, Double> getSubstitutionInfoMap() {
        return substitutionInfoMap;
    }

    public void printMap() {
        for (Map.Entry<String, Double> entry: bigramCountsMap.entrySet()) {
            System.out.println("Word is : " + entry.getKey());
            System.out.println("Word count is: " + entry.getValue());
        }
    }

    public Map<String, Double> getUnigramCountsMap() {
        return unigramCountsMap;
    }
}
