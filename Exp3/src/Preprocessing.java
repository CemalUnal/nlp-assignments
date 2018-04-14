import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private List<String> tempUnorderedWordTags = new ArrayList<>();

    private static Map<Integer, List<List<UnorderedWord>>> fOneFeatureVector = new HashMap<>();
//    private static Map<String, List<String>> fTwoFeatureVector = new HashMap<>();

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

                    addToFeatureOneVector(currentAmbiguousWord, currentSenseId, tempUnorderedWords);
                    tempUnorderedWords.clear();
                }
                else if (isContextBegan) {
                    tempUnorderedWords = getWordFromPTag(line, pTagRegex, tempUnorderedWords);
                }
            }
        }
    }

//    public static Map<Integer, List<List<UnorderedWord>>> fOneFeatureVector = new HashMap<>();
    private void addToMap(int senseId, List<UnorderedWord> unorderedWordsObjects) {
        List<List<UnorderedWord>> listOfFeatures;
        if (fOneFeatureVector.containsKey(senseId)) {
            listOfFeatures = fOneFeatureVector.get(senseId);
        } else {
            listOfFeatures = new ArrayList<>();
        }

        listOfFeatures.add(unorderedWordsObjects);
        fOneFeatureVector.put(senseId, listOfFeatures);
    }

    private String getStemOfWord(String word) {
        Stemmer stemmer = new Stemmer();

        for (int i = 0; i < word.length(); i++)
            stemmer.add(word.charAt(i));

        stemmer.stem();

        return stemmer.toString();
    }
//
//    List<String> tempUnorderedWords = new ArrayList<>();
//    List<UnorderedWord> unorderedWordObjects = new ArrayList<>();
    private void addToFeatureOneVector(String ambiguousWord, int senseId, List<String> tempUnorderedWords) {
        int indexOfAmbiguousWord = tempUnorderedWords.indexOf(ambiguousWord);
        String unorderedWord;

        List<UnorderedWord> unorderedWordObjects = new ArrayList<>();

        for (int i = 1; i < 4; i++) {
            // prevent array index out of bounds exception
            if (indexOfAmbiguousWord - i > -1) {
                unorderedWord = tempUnorderedWords.get(indexOfAmbiguousWord - i);

                unorderedWordObjects.add(new UnorderedWord(unorderedWord, null, 0 - i));
            }
            // prevent array index out of bounds exception
            if (indexOfAmbiguousWord + i < tempUnorderedWords.size()) {
                unorderedWord = tempUnorderedWords.get(indexOfAmbiguousWord + i);

                unorderedWordObjects.add(new UnorderedWord(unorderedWord, null, i));
            }
        }

        addToMap(senseId, unorderedWordObjects);
    }

    private boolean isStopWord(String word) {
        return stopWords.contains(word);
    }

//    "([a-zA-Z0-9!\"#$%&'()*+,\\-.:;?@\\[\\]^_`{|}~]+)(\\s)(<p=\")([a-zA-Z0-9!\"#$%&'()*+,\\-.:;?@\\[\\]^_`{|}~]+)(\"\\/>)"
//    "(<head>)([A-Za-z]+)"
    private List<String> getWordFromPTag(String line, String regex, List<String> tempUnorderedWords) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        String unorderedWord = null;
        String tagOfWord = null;

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
//                tempUnorderedWordTags.add(tagOfWord);
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

    public static Map<Integer, List<List<UnorderedWord>>> getfOneFeatureVector() {
        return fOneFeatureVector;
    }
}
