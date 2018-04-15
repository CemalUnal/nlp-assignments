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

    private static final String corpusBeginRegex = "<corpus lang=\"[a-z]+\">";
    private static final String corpusEndRegex = "</corpus>";

    private static final String lexeltBeginRegex = "(<lexelt item=\")([a-zA-Z]+)(-)([a-zA-Z]+)(\">)";
    private static final String lexeltEndRegex = "</lexelt>";

    private static final String instanceBeginRegex = "(<instance id=\")([a-zA-Z]+-[a-zA-Z]+\\.[0-9]+)(\">)";
    private static final String instanceEndRegex = "</instance>";

    private static final String answerRegex = "(<answer instance=\")([a-zA-Z]+-[a-zA-Z]+\\.[0-9]+)(\"\\s)(senseid=\")([0-9]+)(\"/>)";

    private static final String contextBeginRegex = "<context>";
    private static final String contextEndRegex = "</context>";

    private static final String pTagRegex = "([a-zA-Z0-9!<>\"#$%&'()*+,\\-.:;?@\\[\\]^_`{|}~]+)(\\s)(<p=\")([a-zA-Z0-9!\"#$%&'()*+,\\-.:;?@\\[\\]^_`{|}~]+)(\"\\/>)";
    private static final String headTagRegex = "(<head>)([A-Za-z]+)";

    private String currentAmbiguousWord = "";
    private List<String> stopWords = new ArrayList<>();

    // is equal to N in the assignment sheet
    private static double allInstancesInTrainingSet = 0.0;

    private static Map<Integer, Map<List<UnorderedWord>, Double>> fOneFeatureVectorTrainSet = new HashMap<>();
    private static Map<Integer, Map<List<UnorderedWord>, Double>> fTwoFeatureVectorTrainSet = new HashMap<>();

    private static Map<String, Map<List<UnorderedWord>, Double>> fOneFeatureVectorTestSet = new HashMap<>();
    private static Map<String, Map<List<UnorderedWord>, Double>> fTwoFeatureVectorTestSet = new HashMap<>();

    public void readStopWords(String filePath) throws IOException {
        Path file = Paths.get(filePath);
        if (!Files.exists(file)) {
            throw new FileNotFoundException(filePath);
        }

        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;

        while ((line = reader.readLine()) != null) {
            if (!line.equals("")) {
                stopWords.add(line);
            }
        }
    }


    public void processInputLines(String filePath, String inputFileType) throws IOException {
        Path file = Paths.get(filePath);
        if (!Files.exists(file)) {
            throw new FileNotFoundException(filePath);
        }

        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        String ambiguousWord = null;
        String currentWordId = null;
        int currentSenseId = 0;
        boolean isContextBegan = false;

        List<String> tempUnorderedWords = new ArrayList<>();
        List<String> tempUnorderedWordTags = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            if (!line.equals("") && !line.matches(corpusBeginRegex) && !line.matches(corpusEndRegex)) {
//                if (line.matches(lexeltBeginRegex)) {
////                    ambiguousWord = getWordIdFromLexInstanceTag(line, lexeltBeginRegex);
//                }
                if (line.matches(instanceBeginRegex)) {
                    if (inputFileType.equals("train"))
                        allInstancesInTrainingSet++;
                    currentWordId = getWordIdFromInstanceTag(line, instanceBeginRegex);
                }
                if (line.matches(answerRegex)) {
                    currentSenseId = getSenseIdFromAnswerTag(line);
                }
                else if (line.matches(contextBeginRegex)) {
                    isContextBegan = true;
                }
                else if (line.matches(contextEndRegex)) {
                    isContextBegan = false;

                    if (inputFileType.equals("train"))
                        addToTrainFeatureVector(currentAmbiguousWord, currentSenseId, tempUnorderedWords, tempUnorderedWordTags);
                    else
                        addToTestFeatureVector(currentAmbiguousWord, currentWordId, tempUnorderedWords, tempUnorderedWordTags);
                    tempUnorderedWords.clear();
                    tempUnorderedWordTags.clear();
                }
                else if (isContextBegan) {
                    tempUnorderedWords = getWordFromPTag(line, pTagRegex, tempUnorderedWords, tempUnorderedWordTags);
                }
            }
        }
    }

    private Map<List<UnorderedWord>, Double> addToInnerMap(Map<List<UnorderedWord>, Double> map, List<UnorderedWord> unorderedWordsObjects) {
        if (map.containsKey(unorderedWordsObjects)) {
            double currentFrequency = map.get(unorderedWordsObjects);
            currentFrequency = currentFrequency + 1.0;
            map.put(
                    // unmodifiable so key cannot change hash code
                    Collections.unmodifiableList(unorderedWordsObjects), currentFrequency
            );
        } else {
            map.put(
                    // unmodifiable so key cannot change hash code
                    Collections.unmodifiableList(unorderedWordsObjects), 1.0
            );
        }

        return map;
    }

    private void addToTestMap(String wordId, List<UnorderedWord> unorderedWordsObjects, Map<String, Map<List<UnorderedWord>, Double>> featureVector) {
        Map<List<UnorderedWord>, Double> mapOfFeatures;

        if (featureVector.containsKey(wordId)) {
            mapOfFeatures = featureVector.get(wordId);
        } else {
            mapOfFeatures = new HashMap<>();
        }

        mapOfFeatures = addToInnerMap(mapOfFeatures, unorderedWordsObjects);

        featureVector.put(wordId, mapOfFeatures);
    }

    private void addToTrainMap(int senseId, List<UnorderedWord> unorderedWordsObjects, Map<Integer, Map<List<UnorderedWord>, Double>> featureVector) {
        Map<List<UnorderedWord>, Double> mapOfFeatures;
        if (featureVector.containsKey(senseId)) {
            mapOfFeatures = featureVector.get(senseId);
        } else {
            mapOfFeatures = new HashMap<>();
        }

        mapOfFeatures = addToInnerMap(mapOfFeatures, unorderedWordsObjects);

        featureVector.put(senseId, mapOfFeatures);
    }

    //    List<String> words, String ambiguousWord, List<String> wordTags,
//    List<UnorderedWord> unorderedWordObjectsForfOne, List<UnorderedWord> unorderedWordObjectsForfTwo
    private void addToTrainFeatureVector(String ambiguousWord, int senseId, List<String> words, List<String> wordTags) {
        List<UnorderedWord> unorderedWordObjectsForfOneTrainSet = new ArrayList<>();
        List<UnorderedWord> unorderedWordObjectsForfTwoTrainSet = new ArrayList<>();

        createWordsInWindowSize(words, ambiguousWord, wordTags, unorderedWordObjectsForfOneTrainSet, unorderedWordObjectsForfTwoTrainSet);

        addToTrainMap(senseId, unorderedWordObjectsForfOneTrainSet, fOneFeatureVectorTrainSet);
        addToTrainMap(senseId, unorderedWordObjectsForfTwoTrainSet, fTwoFeatureVectorTrainSet);
    }

    private void addToTestFeatureVector(String ambiguousWord, String wordId, List<String> words, List<String> wordTags) {
        List<UnorderedWord> unorderedWordObjectsForfOneTestSet = new ArrayList<>();
        List<UnorderedWord> unorderedWordObjectsForfTwoTestSet = new ArrayList<>();

        createWordsInWindowSize(words, ambiguousWord, wordTags, unorderedWordObjectsForfOneTestSet, unorderedWordObjectsForfTwoTestSet);

        addToTestMap(wordId, unorderedWordObjectsForfOneTestSet, fOneFeatureVectorTestSet);
        addToTestMap(wordId, unorderedWordObjectsForfTwoTestSet, fTwoFeatureVectorTestSet);

//        for (int i = 1; i < 4; i++) {
//            // prevent array index out of bounds exception
//            if (indexOfAmbiguousWord - i > -1) {
//                unorderedWord = words.get(indexOfAmbiguousWord - i);
//                unorderedWordTag = wordTags.get(indexOfAmbiguousWord - i);
//
//                unorderedWordObjectsForfOneTestSet.add(new UnorderedWord(unorderedWord, null, 0 - i));
//                unorderedWordObjectsForfTwoTestSet.add(new UnorderedWord(unorderedWord, unorderedWordTag, 0 - i));
//            }
//            // prevent array index out of bounds exception
//            if (indexOfAmbiguousWord + i < words.size()) {
//                unorderedWord = words.get(indexOfAmbiguousWord + i);
//                unorderedWordTag = wordTags.get(indexOfAmbiguousWord + i);
//
//                unorderedWordObjectsForfOneTestSet.add(new UnorderedWord(unorderedWord, null, i));
//                unorderedWordObjectsForfTwoTestSet.add(new UnorderedWord(unorderedWord, unorderedWordTag, i));
//
//            }
//        }
    }

    private void createWordsInWindowSize(List<String> words, String ambiguousWord, List<String> wordTags,
                                         List<UnorderedWord> unorderedWordObjectsForfOne, List<UnorderedWord> unorderedWordObjectsForfTwo) {

        int indexOfAmbiguousWord = words.indexOf(ambiguousWord);
        String unorderedWord;
        String unorderedWordTag;

        for (int i = 1; i < 4; i++) {
            // prevent array index out of bounds exception
            if (indexOfAmbiguousWord - i > -1) {
                unorderedWord = words.get(indexOfAmbiguousWord - i);
                unorderedWordTag = wordTags.get(indexOfAmbiguousWord - i);

                unorderedWordObjectsForfOne.add(new UnorderedWord(unorderedWord, null, 0 - i));
                unorderedWordObjectsForfTwo.add(new UnorderedWord(unorderedWord, unorderedWordTag, 0 - i));
            }
            // prevent array index out of bounds exception
            if (indexOfAmbiguousWord + i < words.size()) {
                unorderedWord = words.get(indexOfAmbiguousWord + i);
                unorderedWordTag = wordTags.get(indexOfAmbiguousWord + i);

                unorderedWordObjectsForfOne.add(new UnorderedWord(unorderedWord, null, i));
                unorderedWordObjectsForfTwo.add(new UnorderedWord(unorderedWord, unorderedWordTag, i));
            }
        }
    }

    private String getStemOfWord(String word) {
        Stemmer stemmer = new Stemmer();

        for (int i = 0; i < word.length(); i++)
            stemmer.add(word.charAt(i));

        stemmer.stem();

        return stemmer.toString();
    }

    private boolean isStopWord(String word) {
        return stopWords.contains(word);
    }

    //    "([a-zA-Z0-9!\"#$%&'()*+,\\-.:;?@\\[\\]^_`{|}~]+)(\\s)(<p=\")([a-zA-Z0-9!\"#$%&'()*+,\\-.:;?@\\[\\]^_`{|}~]+)(\"\\/>)"
//    "(<head>)([A-Za-z]+)"
    private List<String> getWordFromPTag(String line, String regex, List<String> tempUnorderedWords, List<String> tempUnorderedWordTags) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        String unorderedWord;
        String tagOfWord;

        while (matcher.find()) {
            unorderedWord = matcher.group(1);
            tagOfWord = matcher.group(4);
            if (!isStopWord(unorderedWord)) {
                if (unorderedWord.matches(headTagRegex)) {
                    currentAmbiguousWord = unorderedWord;
                    tempUnorderedWords.add(unorderedWord);
                } else {
                    tempUnorderedWords.add(getStemOfWord(unorderedWord));
                }
                tempUnorderedWordTags.add(tagOfWord);
            }
        }

        return tempUnorderedWords;
    }

    //    "(<answer instance=\")([a-zA-Z]+-[a-zA-Z]+\\.[0-9]+)(\"\\s)(senseid=\")([0-9]+)(\"/>)"
    private int getSenseIdFromAnswerTag(String line) {
        Pattern pattern = Pattern.compile(answerRegex);
        Matcher matcher = pattern.matcher(line);

        String word = null;
        int senseId = 0;

        while (matcher.find()) {
            word = matcher.group(2);
            senseId = Integer.parseInt(matcher.group(5));
//            System.out.println(word  + " - " + senseId);
        }

        return senseId;
    }

    //    (<lexelt item=")([a-zA-Z]+)(-)([a-zA-Z]+)(">)
//    "(<head>)([A-Za-z]+)(\\s)(<p=\")([A-Z]+)(\"/></head>)"
//    (<instance id=")([a-zA-Z]+-[a-zA-Z]+\.[0-9]+)(">)
    private String getWordIdFromInstanceTag(String line, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        String wordId = null;

        while (matcher.find()) {
            wordId = matcher.group(2);
//            System.out.println(ambiguousWord);
        }

        return wordId;
    }

    public static Map<Integer, Map<List<UnorderedWord>, Double>> getfOneFeatureVectorTrainSet() {
        return fOneFeatureVectorTrainSet;
    }

    public static Map<Integer, Map<List<UnorderedWord>, Double>> getfTwoFeatureVectorTrainSet() {
        return fTwoFeatureVectorTrainSet;
    }

    public static Map<String, Map<List<UnorderedWord>, Double>> getfOneFeatureVectorTestSet() {
        return fOneFeatureVectorTestSet;
    }

    public static Map<String, Map<List<UnorderedWord>, Double>> getfTwoFeatureVectorTestSet() {
        return fTwoFeatureVectorTestSet;
    }

    public static double getAllInstancesInTrainingSet() {
        return allInstancesInTrainingSet;
    }
}
