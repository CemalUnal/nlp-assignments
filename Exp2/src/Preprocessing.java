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

    private List<String> rawDatasetLines = new ArrayList<>();
    private static List<String> errorTags = new ArrayList<>();
    private static List<String> wrongLines = new ArrayList<>();

    private static double totalWrongWordCount = 0.0;

    // since there can be more than one correct word for one wrong word,
    // we need a list of these correct words.
    private static Map<String, List<String>> wrongAndCorrectWordForms = new HashMap<>();

    /**
     * Open the given input dataset and reads it line by line,
     * Converts each line to lowercase,
     * Corrects each line and adds sentence boundaries to them,
     * After that it fills the unigram and bigram counts maps.
     *
     * @param filePath path of input dataset
     *
     */
    public void processDatasetLines(String filePath) throws IOException {
        Path file = Paths.get(filePath);
        if (!Files.exists(file)) {
            throw new FileNotFoundException(filePath);
        }

        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;

        while ((line = reader.readLine()) != null) {
            if (!line.equals("")) {
                line = line.toLowerCase();
                rawDatasetLines.add(line);

                extractErrorTagFromLine(line);

                wrongLines.add(line);

                line = getCorrectedLine(line);

                line = addSentenceBoundary(line);

                addToUnigramMap(line);
                addToBigramMap(line);
            }
        }
    }

    /**
     * Initializes the wrongAndCorrectWordForms map
     *
     */
    public void initializeWrongCorrectWordsMap() {
        String correctAndWrongWordTag;
        errorTags.clear();

        for (String line : rawDatasetLines) {
            correctAndWrongWordTag = extractErrorTagFromLine(line);

            addWrongAndCorrectWordForms(correctAndWrongWordTag);
        }
    }

    /**
     * Extracts the error tags from given line of
     * the input dataset
     *
     * An error tag is for example "<ERR targ=sister> siter </ERR>"
     *
     * @param line a line of the input dataset
     *
     * @return all error tags in the line
     */
    private String extractErrorTagFromLine(String line) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        StringBuilder stringBuilder = new StringBuilder();

        while (matcher.find()) {
            stringBuilder.append(matcher.group());
            errorTags.add(matcher.group());
        }

        return stringBuilder.toString();
    }

    /**
     * Extracts the wrong word and its correct versions from line.
     *
     * After that it calls the addToWrongAndCorrectWordFormsMap method
     * with the parameters wrongAndCorrectWordForms, wrongWord and correctWord
     *
     * @param correctAndWrongWordTag correctAndWrongWordTag "<ERR targ=sister> siter </ERR>)"
     *
     */
    private void addWrongAndCorrectWordForms(String correctAndWrongWordTag) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(correctAndWrongWordTag);

        String correctWord;
        String wrongWord;

        while (matcher.find()) {
            correctWord = matcher.group(2);
            wrongWord = matcher.group(5);

            addToWrongAndCorrectWordFormsMap(wrongAndCorrectWordForms, wrongWord, correctWord);
            totalWrongWordCount++;
        }
    }

    /**
     * Returns the corrected version of the wrong typed line
     *
     * @param line a line of the input dataset
     *
     * @return corrected version of the line
     */
    private String getCorrectedLine(String line) {
        for (String errTag : errorTags) {
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
        errorTags.clear();

        return line;
    }

    /**
     * Adds the wrong words and their correct forms to the
     * wrongAndCorrectWordForms map by calling
     *
     * @param map wrongAndCorrectWordForms
     * @param key wrong word
     * @param value correct version of the wrong word
     *
     */
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
     * Removes punctuation marks from the beginning and
     * end of all strings in a line (call these strings plain word)
     * Also adds sentence boundaries
     *
     * @param line a line in the dataset
     * @return a line with plain words
     */
    public String addSentenceBoundary(String line) {
        String punctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
        StringJoiner stringJoiner = new StringJoiner(" ");

        List<String> tokens = separateIntoTokens(line);

        stringJoiner.add("<s>");
        for (String token : tokens) {
            // if the last char of the string is a punctuation mark,
            // then simply delete it.
            if (token.length() > 1 && punctuation.indexOf(token.charAt(token.length() - 1)) != -1) {
                token = token.substring(0, token.length() - 1);
            }

            // if the first char of the string is a punctuation mark,
            // then simply delete it.
            if (token.length() > 1 && punctuation.indexOf(token.charAt(0)) != -1) {
                token = token.substring(1);
            }

            // if the token is not a punctuation mark
            if (punctuation.indexOf(token.charAt(token.length() - 1)) == -1) {
                stringJoiner.add(token);
            }
        }
        stringJoiner.add("</s>");

        return stringJoiner.toString();
    }

    /**
     * Separates the given line into tokens
     *
     * @param line a line of the input dataset
     *
     * @return allTokens
     */
    private ArrayList<String> separateIntoTokens (String line) {
        ArrayList<String> allTokens = new ArrayList<>();

        Pattern pattern = Pattern.compile("[^\\s]+"); // this regex is used to separate punctuation marks
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            allTokens.add(matcher.group());
        }

        return allTokens;
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

    /**
     * Returns unigramCountsMap
     *
     * @return unigramCountsMap
     *
     */
    public Map<String, Double> getUnigramCountsMap() {
        return unigramCountsMap;
    }

    /**
     * Returns bigramCountsMap
     *
     * @return bigramCountsMap
     *
     */
    public Map<String, Double> getBigramCountsMap() {
        return bigramCountsMap;
    }

    /**
     * Returns wrongAndCorrectWordForms
     *
     * @return wrongAndCorrectWordForms
     *
     */
    public Map<String, List<String>> getWrongAndCorrectWordForms() {
        return wrongAndCorrectWordForms;
    }

    /**
     * Returns wrongLines
     *
     * @return wrongLines
     *
     */
    public List<String> getWrongLines() {
        return wrongLines;
    }

    /**
     * Returns regex
     *
     * @return regex
     *
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Returns totalWrongWordCount
     *
     * @return totalWrongWordCount
     *
     */
    public double getTotalWrongWordCount() {
        return totalWrongWordCount;
    }

}
