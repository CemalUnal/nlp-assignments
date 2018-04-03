import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preprocessing {

    private String regex = "(<err targ=)([a-zA-Z!\"#$%&'()*+,-.:;?@\\[\\]^_`{|}~\\s]+)(>)(\\s)([a-zA-Z!\"#$%&'()*+,-.:;?@\\[\\]^_`{|}~\\s]+)(\\s)(</err>)";

    /* Holds the Bigram counts. Bigram Language Model */
    private static Map<String, Double> bigramCountsMap = new HashMap<>();
    /* Holds the Unigram counts. Unigram Language Model */
    private static Map<String, Double> unigramCountsMap = new HashMap<>();

    private static Map<List<String>, Double> insertionInfoMap = new HashMap<>();
    private static Map<List<String>, Double> deletionInfoMap = new HashMap<>();
    private static Map<List<String>, Double> substitutionInfoMap = new HashMap<>();

    private List<String> rawDatasetLines = new ArrayList<>();
    private static List<String> errTags = new ArrayList<>();
    private static List<String> wrongLines = new ArrayList<>();

    private static double totalWrongWordCount = 0.0;

    // since there can be more than one correct word for one wrong word,
    // we need a list of these correct words.
    private static Map<String, List<String>> wrongAndCorrectWordForms = new HashMap<>();

    public void processDatasetLines(String filePath) throws IOException {
        Path file = Paths.get(filePath);
        if (!Files.exists(file)) {
            throw new FileNotFoundException(filePath);
        }

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        EditDistanceCalculator editDistanceCalculator = new EditDistanceCalculator();

        String line;

        while ((line = reader.readLine()) != null) {
            if (!line.equals("")) {
                line = line.toLowerCase();
                rawDatasetLines.add(line);

                extractErrorTagFromLine(line);

                wrongLines.add(line);

                line = getCorrectedLine(line);

                // line'a sentence boundary ekle
                line = editDistanceCalculator.addSentenceBoundary(line);
//                wrongLine = editDistanceCalculator.addSentenceBoundary(wrongLine);
                // unigram map'e ekle
                addToUnigramMap(line);
                addToBigramMap(line);

//                correctedDatasetLines.add(line);
//                wrongLines.add(wrongLine);
//                sentencesWithWrongWords.put(wrongLine, new ArrayList<>(wrongWords));
//                wrongWords.clear();
            }
        }
    }

    public void initializeWrongCorrectWordsMap() {
        String correctAndWrongWordTag;
        errTags.clear();
//        System.out.println(rawDatasetLines.size());
        for (String line : rawDatasetLines) {
//            System.out.println(line);
            correctAndWrongWordTag = extractErrorTagFromLine(line);

            addWrongAndCorrectWordForms(correctAndWrongWordTag);
        }
    }

    private String extractErrorTagFromLine(String line) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        StringBuilder stringBuilder = new StringBuilder();

        while (matcher.find()) {
            stringBuilder.append(matcher.group());
            errTags.add(matcher.group());
        }

        return stringBuilder.toString();
    }

    private void addWrongAndCorrectWordForms(String correctAndWrongWordTag) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(correctAndWrongWordTag);

        String correctWord;
        String wrongWord;

        while (matcher.find()) {
            correctWord = matcher.group(2);
            wrongWord = matcher.group(5);

            addToWrongAndCorrectWordFormsMap(wrongAndCorrectWordForms, wrongWord, correctWord);
        }

        totalWrongWordCount++;
    }

    private String getCorrectedLine(String line) {
        for (String errTag : errTags) {
            int beginIndex = line.indexOf(errTag);
            int lastIndex = errTag.length();

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(errTag);

            String correctWord;

            while (matcher.find()) {
                correctWord = matcher.group(2);

                lastIndex = lastIndex + beginIndex;
                line = line.substring(0, beginIndex) + correctWord + line.substring(lastIndex, line.length());
            }
        }
        errTags.clear();

        return line;
    }

    private void addToWrongAndCorrectWordFormsMap(Map<String, List<String>> map, String key, String value) {
        EditDistanceCalculator editDistanceCalculator = new EditDistanceCalculator();

        boolean minEditDistanceIsOne = editDistanceCalculator.minEditDistanceIsOne(key, unigramCountsMap);

        if (minEditDistanceIsOne) {
            // if the map contains the correct word, then update its wrong words
            if (map.containsKey(key)) {
                // And we do not want to add the same
                // wrong word more than one time
                if (!map.get(key).contains(value)) {
                    map.get(key).add(value);
                }
            } else {
                // if the map not contains the correct word, then add the current correct word to map
                List<String> words = new ArrayList<>();
                words.add(value);
                map.put(key, words);
            }
        }
    }


    /**
     * Adds given word to given map. And it handles frequencies inside itself.
     *
     * @param map a map to add an element to it
     * @param word a word to add to map
     */
    private void addWordNGramToMap(Map<String, Double> map,  String word) {
        if (map.containsKey(word)) {
            double currentFrequency = map.get(word);
            currentFrequency = currentFrequency + 1.0;
            map.put(word, currentFrequency);
        } else {
            map.put(word, 1.0);
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

    /**
     * Creates the unigram model.
     * Adds all unigrams in the training set to a hashmap by calling
     * the addWordToMap method of its parent class.
     *
     * @param line line
     */
    public void addToUnigramMap(String line) {
        String[] words = line.split("\\s+");
        for (String word : words) {
            addWordNGramToMap(unigramCountsMap, word);
        }
    }

    /**
     * Creates the bigram model.
     * Adds all bigrams in the training set to a hashmap by calling
     * the addWordToMap method of its parent class.
     *
     * @param line line
     */
    public void addToBigramMap(String line) {
        String[] words = line.split("\\s+");
        for (int i = 1; i < words.length; i++) {
            String bigramToken = words[i - 1] + " " + words[i];
            addWordNGramToMap(bigramCountsMap, bigramToken);
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
        double count = 0.0;

        for (Map.Entry<String, Double> entry: unigramCountsMap.entrySet()) {
            // TODO: NULL POINTER VERDI
            double tempCount = entry.getKey().split(Pattern.quote(word), -1).length - 1;
            tempCount = tempCount * entry.getValue();
            count = count + tempCount;
        }
        return count;
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

    public Map<String, Double> getUnigramCountsMap() {
        return unigramCountsMap;
    }

    public Map<String, List<String>> getWrongAndCorrectWordForms() {
        return wrongAndCorrectWordForms;
    }

    public List<String> getRawDatasetLines() {
        return rawDatasetLines;
    }

    public List<String> getWrongLines() {
        return wrongLines;
    }

    public List<String> getErrTags() {
        return errTags;
    }

    public String getRegex() {
        return regex;
    }

    public double getTotalWrongWordCount() {
        return totalWrongWordCount;
    }

    public static void printMap() {
        for (Map.Entry<String, List<String>> entry: wrongAndCorrectWordForms.entrySet()) {
            System.out.println("Word is : " + entry.getKey());
            System.out.println("Word list :");
            for (String wrongWord : entry.getValue()) {
                System.out.println(wrongWord);
            }
        }
    }

//    public void printMap() {
//        for (Map.Entry<String, Double> entry: bigramCountsMap.entrySet()) {
//            System.out.println("Word is : " + entry.getKey());
//            System.out.println("Word count is: " + entry.getValue());
//        }
//    }

}
