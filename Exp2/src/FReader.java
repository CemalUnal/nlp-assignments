import javax.xml.crypto.Data;
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

public class FReader {

    private String regex = "(<err targ=)([a-zA-Z!\"#$%&'()*+,-.:;?@\\[\\]^_`{|}~\\s]+)(>)(\\s)([a-zA-Z!\"#$%&'()*+,-.:;?@\\[\\]^_`{|}~\\s]+)(\\s)(</err>)";

    private List<String> rawDatasetLines = new ArrayList<>();
    private static List<String> errTags = new ArrayList<>();
//    private static List<String> wrongLines = new ArrayList<>();
    private static Map<String, List<String>> sentencesWithWrongWords = new HashMap<>();
//    private static List<String> wrongWords = new ArrayList<>();
    private static List<String> wrongLines = new ArrayList<>();

    // since there can be more than one correct word for one wrong word,
    // we need a list of these correct words.
    private static Map<String, List<String>> wrongAndCorrectWordForms = new HashMap<>();

//    public List<String> getCorrectedDatasetLines(String filePath) throws IOException {
    public void getCorrectedDatasetLines(String filePath) throws IOException {

        Path file = Paths.get(filePath);
        if (!Files.exists(file)) {
            throw new FileNotFoundException(filePath);
        }

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        String correctAndWrongWordTag;
//        String wrongLine;
//        List<String> correctedDatasetLines = new ArrayList<>();
        DatasetOperations datasetOperations = new DatasetOperations();
        HiddenMarkovModel hmm = new HiddenMarkovModel();

        while ((line = reader.readLine()) != null) {
            if (!line.equals("")) {
                line = line.toLowerCase();
                rawDatasetLines.add(line);

                correctAndWrongWordTag = extractErrorTagFromLine(line);
                addWrongAndCorrectWordForms(correctAndWrongWordTag);

                wrongLines.add(line);

                line = getCorrectedLine(line);

                // line'a sentence boundary ekle
                line = datasetOperations.addSentenceBoundary(line);
//                wrongLine = datasetOperations.addSentenceBoundary(wrongLine);
                // unigram map'e ekle
                hmm.addToUnigramMap(line);
                hmm.addToBigramMap(line);

//                correctedDatasetLines.add(line);
//                wrongLines.add(wrongLine);
//                sentencesWithWrongWords.put(wrongLine, new ArrayList<>(wrongWords));
//                wrongWords.clear();
            }
        }

        // bigram map'e ekle
//        hmm.createBigramModel(correctedDatasetLines);

//        return correctedDatasetLines;
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

//            addToMap(correctAndWrongWordForms, correctWord, wrongWord);
//            System.out.println(wrongWord + " - " + correctWord);
            addToMap(wrongAndCorrectWordForms, wrongWord, correctWord);
        }
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

    private void addToMap(Map<String, List<String>> map, String key, String value) {
        DatasetOperations datasetOperations = new DatasetOperations();
        HiddenMarkovModel hmm = new HiddenMarkovModel();

        boolean minEditDistanceIsOne = datasetOperations.calculateMinEditDistance(key, hmm.getUnigramCountsMap());

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

    public Map<String, List<String>> getSentencesWithWrongWords() {
        return sentencesWithWrongWords;
    }

    //    public static List<String> getAllLines() {
//        return allLines;
//    }

//    public void printMap() {
//        for (Map.Entry<String, List<String>> entry: wrongAndCorrectWordForms.entrySet()) {
//            System.out.println("Word is : " + entry.getKey());
//            System.out.println("Word list :");
//            for (String wrongWord : entry.getValue()) {
//                System.out.println(wrongWord);
//            }
//        }
//    }

}
