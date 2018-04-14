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

    private static Map<Integer, Map<List<UnorderedWord>, Double>> fOneFeatureVector = new HashMap<>();
    private static Map<Integer, Map<List<UnorderedWord>, Double>> fTwoFeatureVector = new HashMap<>();

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


    public void processDatasetLines(String filePath) throws IOException {
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
//                if (line.matches(instanceBeginRegex)) {
//                    currentWordId = getWordIdFromLexInstanceTag(line, instanceBeginRegex);
//                }
                if (line.matches(answerRegex)) {
                    currentSenseId = getSenseIdFromAnswerTag(line);
                }
                else if (line.matches(contextBeginRegex)) {
                    isContextBegan = true;
                }
                else if (line.matches(contextEndRegex)) {
                    isContextBegan = false;

                    addToFeatureVector(currentAmbiguousWord, currentSenseId, tempUnorderedWords, tempUnorderedWordTags);
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

//    public static Map<Integer, List<List<UnorderedWord>>> fOneFeatureVector = new HashMap<>();
    private void addToFeatureMap(int senseId, List<UnorderedWord> unorderedWordsObjects, Map<Integer, Map<List<UnorderedWord>, Double>> map) {
        Map<List<UnorderedWord>, Double> mapOfFeatures;
        if (map.containsKey(senseId)) {
            mapOfFeatures = map.get(senseId);
        } else {
            mapOfFeatures = new HashMap<>();
        }

        mapOfFeatures = addToInnerMap(mapOfFeatures, unorderedWordsObjects);

//        System.out.println(mapOfFeatures);

//        listOfFeatures.add(unorderedWordsObjects);
        map.put(senseId, mapOfFeatures);
    }

    private String getStemOfWord(String word) {
        Stemmer stemmer = new Stemmer();

        for (int i = 0; i < word.length(); i++)
            stemmer.add(word.charAt(i));

        stemmer.stem();

        return stemmer.toString();
    }

//    List<String> tempUnorderedWords = new ArrayList<>();
//    List<UnorderedWord> unorderedWordObjects = new ArrayList<>();
    private void addToFeatureVector(String ambiguousWord, int senseId, List<String> words, List<String> wordTags) {
        int indexOfAmbiguousWord = words.indexOf(ambiguousWord);
        String unorderedWord;
        String unorderedWordTag;

        List<UnorderedWord> unorderedWordObjectsForfOne = new ArrayList<>();
        List<UnorderedWord> unorderedWordObjectsForfTwo = new ArrayList<>();

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

        addToFeatureMap(senseId, unorderedWordObjectsForfOne, fOneFeatureVector);
//        addToFeatureMap(senseId, unorderedWordObjectsForfTwo, fTwoFeatureVector);
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

////    (<lexelt item=")([a-zA-Z]+)(-)([a-zA-Z]+)(">)
////    "(<head>)([A-Za-z]+)(\\s)(<p=\")([A-Z]+)(\"/></head>)"
//    private String getWordIdFromLexInstanceTag(String line, String regex) {
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(line);
//
//        String ambiguousWord = null;
//
//        while (matcher.find()) {
//            ambiguousWord = matcher.group(2);
////            System.out.println(ambiguousWord);
//        }
//
//        return ambiguousWord;
//    }

    public static Map<Integer, Map<List<UnorderedWord>, Double>> getfOneFeatureVector() {
        return fOneFeatureVector;
    }

    public static Map<Integer, Map<List<UnorderedWord>, Double>> getfTwoFeatureVector() {
        return fTwoFeatureVector;
    }
}
