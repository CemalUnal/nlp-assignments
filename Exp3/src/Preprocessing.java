import java.io.*;

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
    private static double allLexeltsInTrainingSet = 0.0;
    private static List<String> ambiguousWords = new ArrayList<>();

    private static Map<String, Map<String, Double>> featureVectorTrainSet = new HashMap<>();
    //    private static Map<String, Map<String, Double>> featureVectorTestSet = new HashMap<>();
    private static Map<String, List<String>> featureVectorTestSet = new HashMap<>();

    private String outputFile;

    public FWriter openOutputFile(String resultsFile) throws IOException {
        FWriter fileWriter = new FWriter();

        fileWriter.openFile(resultsFile);

        return fileWriter;
    }
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

    public void processInputLines(String filePath, String inputFileType, FWriter fWriter) throws IOException {
        Path file = Paths.get(filePath);
        if (!Files.exists(file)) {
            throw new FileNotFoundException(filePath);
        }

//        outputFile = resultsFile;
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        String currentWordId = null;
        String currentSenseId = "";
        boolean isContextBegan = false;

        List<String> unorderedWords = new ArrayList<>();
        List<String> unorderedWordTags = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            if (!line.equals("") && !line.matches(corpusBeginRegex) && !line.matches(corpusEndRegex)) {
                if (line.matches(lexeltBeginRegex) && inputFileType.equals("train")) {
                    allLexeltsInTrainingSet++;
                }
                else if (line.matches(instanceBeginRegex) && inputFileType.equals("test")) {
//                    ambiguousWords.add(getWordIdFromInstanceTag(line, instanceBeginRegex));
                    currentWordId = getWordIdFromInstanceTag(line, instanceBeginRegex);
                }
                else if (line.matches(answerRegex)) {
                    currentSenseId = getSenseIdFromAnswerTag(line);
                }
                else if (line.matches(contextBeginRegex)) {
                    isContextBegan = true;
                }
                else if (line.matches(contextEndRegex)) {
                    isContextBegan = false;

                    processWordsInWindowSize(currentAmbiguousWord, currentSenseId, unorderedWords, unorderedWordTags,
                            inputFileType, currentWordId, fWriter);

                    unorderedWords.clear();
                    unorderedWordTags.clear();
                }
                else if (isContextBegan) {
                    unorderedWords = getWordFromPTag(line, pTagRegex, unorderedWords, unorderedWordTags);
                }
            }
        }
    }

    private void addToInnerMap(Map<String, Double> innerMap, String item) {
        if (innerMap.containsKey(item)) {
            double currentFrequency = innerMap.get(item);
            currentFrequency = currentFrequency + 1.0;
            innerMap.put(item, currentFrequency);
        } else {
            innerMap.put(item, 1.0);
        }
    }

    private void addWordToFeatureVector(String word, String wordTag, int position, String senseId) {
        Map<String, Double> innerMap = featureVectorTrainSet.get(senseId);

        if (innerMap == null) {
            innerMap = new HashMap<>();
        }

        addToInnerMap(innerMap, word);
        addToInnerMap(innerMap, wordTag + Integer.toString(position));

        featureVectorTrainSet.put(senseId, innerMap);
    }

    // this method is used to prevent copy paste code.
    private void cemal(String unorderedWord, String unorderedWordTag, int position, String senseId, String inputType,
                       String currentWordId, List<String> currentFeatureList) {
        if (!isStopWord(unorderedWord)) {
            unorderedWord = getStemOfWord(unorderedWord);

            if (inputType.equalsIgnoreCase("train")) {
                addWordToFeatureVector(unorderedWord, unorderedWordTag, position, senseId);
            }
            else if (inputType.equalsIgnoreCase("test")) {
//                List<String> currentFeatureList = featureVectorTestSet.get(senseId);
//
//                if (currentFeatureList == null) {
//                    currentFeatureList = new ArrayList<>();
//                }

                currentFeatureList.add(unorderedWord);
                currentFeatureList.add(unorderedWordTag + Integer.toString(position));



//                featureVectorTestSet.put(currentWordId, currentFeatureList);
            }
        }
    }

    private void processWordsInWindowSize(String ambiguousWord, String senseId, List<String> words, List<String> wordTags,
                                          String inputType, String currentWordId, FWriter fWriter) {

        int indexOfAmbiguousWord = words.indexOf(ambiguousWord);
        String unorderedWord;
        String unorderedWordTag;
        List<String> currentFeatureList = new ArrayList<>();

        for (int i = 1; i < 4; i++) {
            // prevent IndexOutOfBoundsException
            if (indexOfAmbiguousWord - i > -1) {
                unorderedWord = words.get(indexOfAmbiguousWord - i);
                unorderedWordTag = wordTags.get(indexOfAmbiguousWord - i);

                cemal(unorderedWord, unorderedWordTag, 0 - i, senseId, inputType, currentWordId, currentFeatureList);
            }
            // prevent IndexOutOfBoundsException
            if (indexOfAmbiguousWord + i < words.size()) {
                unorderedWord = words.get(indexOfAmbiguousWord + i);
                unorderedWordTag = wordTags.get(indexOfAmbiguousWord + i);

                cemal(unorderedWord, unorderedWordTag, i, senseId, inputType, currentWordId, currentFeatureList);
            }
        }

        if (inputType.equalsIgnoreCase("test")) {
//            NaiveBayes naiveBayes = new NaiveBayes();
            try {
                NaiveBayes.getNaiveBayesProbability(currentWordId, currentFeatureList, fWriter);
            } catch (IOException e) {
                e.printStackTrace();
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
//            if (!isStopWord(unorderedWord)) {
            if (unorderedWord.matches(headTagRegex)) {
                currentAmbiguousWord = unorderedWord;
                tempUnorderedWords.add(unorderedWord);
            } else {
//                tempUnorderedWords.add(getStemOfWord(unorderedWord));
                tempUnorderedWords.add(unorderedWord);
            }
            tempUnorderedWordTags.add(tagOfWord);
//            }
        }

        return tempUnorderedWords;
    }

    //    "(<answer instance=\")([a-zA-Z]+-[a-zA-Z]+\\.[0-9]+)(\"\\s)(senseid=\")([0-9]+)(\"/>)"
    private String getSenseIdFromAnswerTag(String line) {
        Pattern pattern = Pattern.compile(answerRegex);
        Matcher matcher = pattern.matcher(line);

        String word = null;
        String senseId = "";

        while (matcher.find()) {
            word = matcher.group(2);
            senseId = matcher.group(5);
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

    public static Map<String, Map<String, Double>> getFeatureVectorTrainSet() {
        return featureVectorTrainSet;
    }

    public static Map<String, List<String>> getFeatureVectorTestSet() {
        return featureVectorTestSet;
    }

    public static double getAllLexeltsInTrainingSet() {
        return allLexeltsInTrainingSet;
    }

    public static List<String> getAmbiguousWords() {
        return ambiguousWords;
    }
}
