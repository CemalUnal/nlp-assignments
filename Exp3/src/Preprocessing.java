import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preprocessing {

    private static final String CORPUS_BEGIN_REGEX = "<corpus lang=\"[a-z]+\">";
    private static final String CORPUS_END_REGEX = "</corpus>";

    private static final String INSTANCE_BEGIN_REGEX = "(<instance id=\")([a-zA-Z]+-[a-zA-Z]+\\.[0-9]+)(\">)";

    private static final String ANSWER_REGEX = "(<answer instance=\")([a-zA-Z]+-[a-zA-Z]+\\.[0-9]+)(\"\\s)(senseid=\")([0-9]+)(\"/>)";

    private static final String CONTEXT_BEGIN_REGEX = "<context>";
    private static final String CONTEXT_END_REGEX = "</context>";

    private static final String P_TAG_REGEX = "([a-zA-Z0-9\\p{Punct}]+)(\\s)(<p=\")([a-zA-Z0-9\\p{Punct}]+)(\"\\/>)";
    private static final String HEAD_TAG_REGEX = "(<head>)([A-Za-z]+)";

    // is equal to N in the assignment sheet
    private static double numberOfAllWordsInTrainingSet = 0.0;

    private String currentAmbiguousWord = "";
    private List<String> stopWords = new ArrayList<>();

    private static Map<String, List<String>> wordsWithSenseIds = new HashMap<>();
    private static Map<String, Map<String, Double>> featureVectorTrainSet = new HashMap<>();

    /**
     * Opens the given output file and returns
     * a FWriter instance
     *
     * @param resultsFile path for the output file
     *
     * @return FWriter FWriter instance for the output file
     */
    public FWriter openOutputFile(String resultsFile) throws IOException {
        FWriter fileWriter = new FWriter();

        fileWriter.openFile(resultsFile);

        return fileWriter;
    }

    /**
     * Opens the given stopwords file and creates
     * an array with all the stop words in this file.
     *
     * @param filePath path for the stopwords file
     *
     */
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

    /**
     * Reads the train and test files line by line
     * and creates train and test feature sets
     *
     * @param filePath path for the input file
     * @param inputFileType type of the input file. 'test' or 'train'
     * @param fWriter FWriter instance
     *
     */
    public void processInputLines(String filePath, String inputFileType, FWriter fWriter) throws IOException {
        Path file = Paths.get(filePath);
        if (!Files.exists(file)) {
            throw new FileNotFoundException(filePath);
        }

        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        String currentWordId = null;
        String currentSenseId = "";
        boolean hasContextBegan = false;

        List<String> unorderedWords = new ArrayList<>();
        List<String> unorderedWordTags = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            if (!line.equals("") && !line.matches(CORPUS_BEGIN_REGEX) && !line.matches(CORPUS_END_REGEX)) {
                if (line.matches(INSTANCE_BEGIN_REGEX)) {
                    currentWordId = getWordIdFromInstanceTag(line, INSTANCE_BEGIN_REGEX);
                }
                else if (line.matches(ANSWER_REGEX)) {
                    currentSenseId = getSenseIdFromAnswerTag(line);
                }
                else if (line.matches(CONTEXT_BEGIN_REGEX)) {
                    hasContextBegan = true;
                }
                else if (line.matches(CONTEXT_END_REGEX)) {
                    hasContextBegan = false;

                    processWordsInWindowSize(currentAmbiguousWord, currentSenseId, unorderedWords, unorderedWordTags,
                            inputFileType, currentWordId, fWriter);

                    unorderedWords.clear();
                    unorderedWordTags.clear();
                }
                else if (hasContextBegan) {
                    unorderedWords = getWordFromPTag(line, P_TAG_REGEX, unorderedWords, unorderedWordTags);
                }
            }
        }
    }

    /**
     * Selects the words that are in the window size
     * from the current context according to 'head' tag.
     *
     * @param ambiguousWord current ambiguous word. (available in the 'head' tag. <head>accident <p="NN"/></head>)
     * @param senseId current sense id
     * @param words all words in the current context
     * @param wordTags all word tags in the current context
     * @param inputType type of the input file. 'test' or 'train'
     * @param currentWordId current word id. (available in the 'instance' tag. <instance id="accident-n.800001">)
     * @param fWriter FWriter instance
     *
     */
    private void processWordsInWindowSize(String ambiguousWord, String senseId, List<String> words, List<String> wordTags,
                                          String inputType, String currentWordId, FWriter fWriter) {

        if (inputType.equalsIgnoreCase("train"))
            numberOfAllWordsInTrainingSet ++;

        int indexOfAmbiguousWord = words.indexOf(ambiguousWord);
        String unorderedWord;
        String unorderedWordTag;
        List<String> currentFeatureList = new ArrayList<>();

        for (int i = 1; i < 4; i++) {
            // prevent IndexOutOfBoundsException
            if (indexOfAmbiguousWord - i > -1) {
                unorderedWord = words.get(indexOfAmbiguousWord - i);
                unorderedWordTag = wordTags.get(indexOfAmbiguousWord - i);

                createFeatureSet(unorderedWord, unorderedWordTag, 0 - i, senseId, inputType, currentFeatureList);
            }
            // prevent IndexOutOfBoundsException
            if (indexOfAmbiguousWord + i < words.size()) {
                unorderedWord = words.get(indexOfAmbiguousWord + i);
                unorderedWordTag = wordTags.get(indexOfAmbiguousWord + i);

                createFeatureSet(unorderedWord, unorderedWordTag, i, senseId, inputType, currentFeatureList);
            }
        }

        if (inputType.equalsIgnoreCase("train")) {
            String currentWord = getWordFromWordId(currentWordId);
            List<String> innerList = wordsWithSenseIds.get(currentWord);

            if (innerList == null) {
                innerList = new ArrayList<>();
            }

            innerList.add(senseId);

            wordsWithSenseIds.put(currentWord, innerList);
        }

        if (inputType.equalsIgnoreCase("test")) {
            try {
                NaiveBayes.getNaiveBayesProbability(currentWordId, currentFeatureList, fWriter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Creates feature vectors both train file and test file.
     * If the current word in the window (window size is +-3)
     * is not a stop word then take the stem of it and add the
     * stem to the related feature vector.
     *
     * This method is used to prevent copy paste code.
     *
     * @param unorderedWord a word that is in the window
     * @param unorderedWordTag tag of the unordered word
     * @param position position of the unordered word tag
     * @param senseId current sense id
     * @param inputType type of the input file. 'test' or 'train'
     * @param currentFeatureList current word id. (available in the 'instance' tag. <instance id="accident-n.800001">)
     */
    private void createFeatureSet(String unorderedWord, String unorderedWordTag, int position, String senseId, String inputType,
                                  List<String> currentFeatureList) {

        if (!isStopWord(unorderedWord)) {
            unorderedWord = getStemOfWord(unorderedWord);

            if (inputType.equalsIgnoreCase("train")) {
                addWordToTrainFeatureVector(unorderedWord, unorderedWordTag, position, senseId);
            }
            else if (inputType.equalsIgnoreCase("test")) {
                currentFeatureList.add(unorderedWord);
                currentFeatureList.add(unorderedWordTag + Integer.toString(position));
            }
        }
    }

    /**
     * Adds given parameters to the featureVectorTrainSet
     *
     * @param word a word that is in the window
     * @param wordTag tag of the unordered word
     * @param position position of the unordered word tag
     * @param senseId current sense id
     *
     */
    private void addWordToTrainFeatureVector(String word, String wordTag, int position, String senseId) {
        Map<String, Double> innerMap = featureVectorTrainSet.get(senseId);

        if (innerMap == null) {
            innerMap = new HashMap<>();
        }

        addToInnerMap(innerMap, word);
        addToInnerMap(innerMap, wordTag + Integer.toString(position));

        featureVectorTrainSet.put(senseId, innerMap);
    }

    /**
     * Adds given parameter to inner map of featureVectorTrainSet
     *
     * @param innerMap inner map of featureVectorTrainSet
     * @param item current item to add
     *
     */
    private void addToInnerMap(Map<String, Double> innerMap, String item) {
        if (innerMap.containsKey(item)) {
            double currentFrequency = innerMap.get(item);
            currentFrequency = currentFrequency + 1.0;
            innerMap.put(item, currentFrequency);
        } else {
            innerMap.put(item, 1.0);
        }
    }

    /**
     * Calls the stemmer method with the given
     * word and returns the stem of given word.
     *
     * @param word a word to take its stem
     *
     * @return stem of the given word
     */

    private String getStemOfWord(String word) {
        Stemmer stemmer = new Stemmer();

        for (int i = 0; i < word.length(); i++)
            stemmer.add(word.charAt(i));

        stemmer.stem();

        return stemmer.toString();
    }

    /**
     * Checks whether the stopWords list contains
     * the given word or not.
     *
     * @param word a word to check whether is a stop word or not
     *
     * @return  true if given word is a stop word
     *          false if the given word is not a stop word
     */
    private boolean isStopWord(String word) {
        return stopWords.contains(word);
    }

    /**
     * Extracts all context words from 'p' tags.
     *
     * @param line a context line of the input file
     * @param regex a regex to match 'p' tag (ex. late <p="RB"/>)
     * @param unorderedWords list of unordered words in a context line
     * @param unorderedWordTags list of unordered word tags in a context line
     *
     * @return  list of unordered words in a context line
     */
    private List<String> getWordFromPTag(String line, String regex, List<String> unorderedWords, List<String> unorderedWordTags) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        String unorderedWord;
        String tagOfWord;

        while (matcher.find()) {
            unorderedWord = matcher.group(1);
            tagOfWord = matcher.group(4);

            if (unorderedWord.matches(HEAD_TAG_REGEX)) {
                currentAmbiguousWord = unorderedWord;
                unorderedWords.add(unorderedWord);
            } else {
                unorderedWords.add(unorderedWord);
            }
            unorderedWordTags.add(tagOfWord);
        }

        return unorderedWords;
    }

    /**
     * Extracts sense id from 'answer' tag. (ex. <answer instance="accident-n.800001" senseid="532675"/>)
     *
     * @param line a line of the input file that contains answer tag
     *
     * @return  current sense id
     */
    private String getSenseIdFromAnswerTag(String line) {
        Pattern pattern = Pattern.compile(ANSWER_REGEX);
        Matcher matcher = pattern.matcher(line);

        String senseId = "";

        while (matcher.find()) {
            senseId = matcher.group(5);
        }

        return senseId;
    }

    /**
     * Extracts word id from 'instance' tags.
     *
     * @param line a context line of the input file
     * @param regex a regex to match 'instance' tag (ex. <instance id="accident-n.800001">)
     *
     * @return  current word id
     */
    private String getWordIdFromInstanceTag(String line, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        String wordId = null;

        while (matcher.find()) {
            wordId = matcher.group(2);
        }

        return wordId;
    }

    /**
     * Extracts word from given word id.
     *
     * @param wordId word id (ex. accident-n.700001)
     *
     * @return  current word id
     */
    public String getWordFromWordId(String wordId) {
        String wordIdRegex = "([a-zA-Z]+)(-)([a-zA-Z]+\\.[0-9]+)";

        Pattern pattern = Pattern.compile(wordIdRegex);
        Matcher matcher = pattern.matcher(wordId);

        String word = "";

        while (matcher.find()) {
            word = matcher.group(1);
        }

        return word;
    }

    /**
     * Returns the feature vector for the train set.
     *
     * @return  featureVectorTrainSet
     */
    public static Map<String, Map<String, Double>> getFeatureVectorTrainSet() {
        return featureVectorTrainSet;
    }

    public static Map<String, List<String>> getWordsWithSenseIds() {
        return wordsWithSenseIds;
    }

    /**
     * Returns all the words that has 'lexelt' tag. (ex. <lexelt item="accident-n">)
     *
     * @return  allLexeltsInTrainingSet
     */
    public static double getNumberOfAllWordsInTrainingSet() {
        return numberOfAllWordsInTrainingSet;
    }
}