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

public class FReader {

    private String regex = "(<err targ=)([a-zA-Z!\"#$%&'()*+,-.:;?@\\[\\]^_`{|}~\\s]+)(>)(\\s)([a-zA-Z!\"#$%&'()*+,-.:;?@\\[\\]^_`{|}~\\s]+)(\\s)(</err>)";

    private List<String> rawDatasetLines = new ArrayList<>();
    private List<String> errTags = new ArrayList<>();

    // since there can be more than one correct word for one wrong word,
    // we need a list of these correct words.
    private Map<String, List<String>> wrongAndCorrectWordForms = new HashMap<>();

    public List<String> getCorrectedDatasetLines(String filePath) throws IOException {
        Path file = Paths.get(filePath);
        if (!Files.exists(file)) {
            throw new FileNotFoundException(filePath);
        }

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        List<String> correctedDatasetLines = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            if (!line.equals("")) {
                line = line.toLowerCase();
                rawDatasetLines.add(line);

                extractErrorTagFromLine(line);

                line = getCorrectedLine(line);
                correctedDatasetLines.add(line);

            }
        }

        return correctedDatasetLines;
    }

    public void initializeWrongCorrectWordsMap() {
        String correctAndWrongWordTag;
        errTags.clear();
//        System.out.println(rawDatasetLines.size());
        for (String line : rawDatasetLines) {
//            System.out.println(line);
            correctAndWrongWordTag = extractErrorTagFromLine(line);

            addWrongAndCorrectWordForms(correctAndWrongWordTag, line);
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

    private void addWrongAndCorrectWordForms(String correctAndWrongWordTag, String line) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(correctAndWrongWordTag);

        String correctWord;
        String wrongWord;

        while (matcher.find()) {
            correctWord = matcher.group(2);
            wrongWord = matcher.group(5);

//            addToMap(correctAndWrongWordForms, correctWord, wrongWord);
            addToMap(wrongAndCorrectWordForms, wrongWord, correctWord);
        }
    }

    private void addToMap(Map<String, List<String>> map, String key, String value) {
        DatasetOperations datasetOperations = new DatasetOperations();
        HiddenMarkovModel hmm = new HiddenMarkovModel();

//        System.out.println(key  + " - " + value);

        int minEditDistance = datasetOperations.calculateMinEditDistance(key, hmm.getUnigramCountsMap());
//        System.out.println(minEditDistance);
        if (minEditDistance == 1) {
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

//    public List<String> read(String filePath) throws Exception {
//        Path file = Paths.get(filePath);
//        if (!Files.exists(file)) {
//            throw new FileNotFoundException(filePath);
//        }
//
//        BufferedReader reader = new BufferedReader(new FileReader(filePath));
//        String line;
//        String correctAndWrongWordTag;
//        List<String> correctedDatasetLines = new ArrayList<>();
//        List<String> errTags = new ArrayList<>();
////        List<String> correctAndWrongWordTags = new ArrayList<>();
//
//        while ((line = reader.readLine()) != null) {
//            if (!line.equals("")) {
//                line = line.toLowerCase();
//
////                correctAndWrongWordTags.add(extractErrorTagFromLine(line, errTags));
//
////                addWrongAndCorrectWordForms(correctAndWrongWordTag, line);
//
//                line = getCorrectedLine(line, errTags);
//                correctedDatasetLines.add(line);
//
////                allTokens.addAll(separateIntoTokens(line));
//
////                correctedDataset.add(line);
//            }
//        }
//
//        reader = new BufferedReader(new FileReader(filePath));
//
//        while ((line = reader.readLine()) != null) {
//            if (!line.equals("")) {
//                correctAndWrongWordTag = extractErrorTagFromLine(line, errTags);
//                addWrongAndCorrectWordForms(correctAndWrongWordTag, line);
//            }
//        }
//
////        for (String tag : correctAndWrongWordTags) {
////            addWrongAndCorrectWordForms(tag, line);
////        }
//
//        return correctedDatasetLines;
//    }
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

//    public Map<String, List<String>> getCorrectAndWrongWordForms() {
//        return correctAndWrongWordForms;
//    }

    public Map<String, List<String>> getWrongAndCorrectWordForms() {
        return wrongAndCorrectWordForms;
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
