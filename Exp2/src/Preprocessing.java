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
    //    private static List<String> errTags = new ArrayList<>();
    private static List<String> wrongLines = new ArrayList<>();

    private static double totalWrongWordCount = 0.0;
    private String correctedLine;

    // since there can be more than one correct word for one wrong word,
    // we need a list of these correct words.
    private static Map<String, List<String>> wrongAndCorrectWordForms = new HashMap<>();

    public void processDatasetLines(String filePath) throws IOException {
        Path file = Paths.get(filePath);
        if (!Files.exists(file)) {
            throw new FileNotFoundException(filePath);
        }

        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        String correctAndWrongWordTag;

        while ((line = reader.readLine()) != null) {
            if (!line.equals("")) {
                line = line.toLowerCase();

                wrongLines.add(line);

//                long startTime = System.nanoTime();
                correctAndWrongWordTag = extractErrorTagFromLine(line);
//                double cemal1 = (double) (System.nanoTime() - startTime) / 1000000000.0;
//                System.out.println("1: " + (double) (System.nanoTime() - startTime) / 1000000000.0 + " seconds.");
//                line = getCorrectedLine(line);

//                startTime = System.nanoTime();
                line = addSentenceBoundary(correctedLine);
//                System.out.println("2: " + (double) (System.nanoTime() - startTime) / 1000000000.0 + " seconds.");
//                double cemal2 = (double) (System.nanoTime() - startTime) / 1000000000.0;

//                startTime = System.nanoTime();
                addToUnigramMap(line);
//                System.out.println("3: " + (double) (System.nanoTime() - startTime) / 1000000000.0 + " seconds.");
//                double cemal3 = (double) (System.nanoTime() - startTime) / 1000000000.0;

//                startTime = System.nanoTime();
                addToBigramMap(line);
//                System.out.println("4: " + (double) (System.nanoTime() - startTime) / 1000000000.0 + " seconds.");
//                double cemal4 = (double) (System.nanoTime() - startTime) / 1000000000.0;

//                startTime = System.nanoTime();
                addWrongAndCorrectWordForms(correctAndWrongWordTag);
//                System.out.println("5: " + (double) (System.nanoTime() - startTime) / 1000000000.0 + " seconds.");
//                double cemal5 = (double) (System.nanoTime() - startTime) / 1000000000.0;
//
//                double x = Math.max(cemal1, cemal2);
//                double y = Math.max(cemal3, cemal4);
//                double z = Math.max(y, x);
//                z = Math.max(z, cemal5);
//
//                System.out.println("Max: " + z);
            }
        }
    }

//    public void initializeWrongCorrectWordsMap() {
//        String correctAndWrongWordTag;
//        errTags.clear();
////        System.out.println(rawDatasetLines.size());
//        for (String line : rawDatasetLines) {
////            System.out.println(line);
//            correctAndWrongWordTag = extractErrorTagFromLine(line);
//
//            addWrongAndCorrectWordForms(correctAndWrongWordTag);
//        }
//    }

    private String extractErrorTagFromLine(String line) {
        correctedLine = line;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(correctedLine);
        StringBuilder stringBuilder = new StringBuilder();

        while (matcher.find()) {
            stringBuilder.append(matcher.group());
            String errorTag = matcher.group();
//            errTags.add(matcher.group());

            int beginIndex = correctedLine.indexOf(errorTag);
            int lastIndex = errorTag.length();

            Pattern pattern2 = Pattern.compile(regex);
            Matcher matcher2 = pattern2.matcher(errorTag);

            String correctWord;
//            System.out.println(line);
            while (matcher2.find()) {
                correctWord = matcher2.group(2);

                lastIndex = lastIndex + beginIndex;
                correctedLine = correctedLine.substring(0, beginIndex) + correctWord + correctedLine.substring(lastIndex, correctedLine.length());
            }

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
            totalWrongWordCount++;
        }
    }

//    private String getCorrectedLine(String line) {
//        for (String errTag : errTags) {
//            int beginIndex = line.indexOf(errTag);
//            int lastIndex = errTag.length();
//
//            Pattern pattern = Pattern.compile(regex);
//            Matcher matcher = pattern.matcher(errTag);
//
//            String correctWord;
////            System.out.println(line);
//            while (matcher.find()) {
//                correctWord = matcher.group(2);
//
//                lastIndex = lastIndex + beginIndex;
//                line = line.substring(0, beginIndex) + correctWord + line.substring(lastIndex, line.length());
//            }
//        }
//        errTags.clear();
//
//        return line;
//    }

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
        String punctuation = "!\"#$%&'()*+,-./:;<=>@[\\]^_`{|}~";
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

    private ArrayList<String> separateIntoTokens (String line) {
        ArrayList<String> allTokens = new ArrayList<>();

//        Pattern pattern = Pattern.compile("(\\w+)|[^\\s]|\\p{Punct}"); // this regex is used to separate punctuation marks
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
    private void addWordToNGramMap(Map<String, Double> map,  String word) {
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
            addWordToNGramMap(unigramCountsMap, word);
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
            addWordToNGramMap(bigramCountsMap, bigramToken);
        }
    }

    public Map<String, Double> getUnigramCountsMap() {
        return unigramCountsMap;
    }

    public Map<String, Double> getBigramCountsMap() {
        return bigramCountsMap;
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

//    public List<String> getErrTags() {
//        return errTags;
//    }

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
