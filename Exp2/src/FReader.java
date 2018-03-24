import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

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

    private String regex = "(<ERR targ=)([a-zA-Z!\"#$%&'()*+,-.:;?@\\[\\]^_`{|}~\\s]+)(>)(\\s)([a-zA-Z!\"#$%&'()*+,-.:;?@\\[\\]^_`{|}~\\s]+)(\\s)(</ERR>)";

    // since there can be more than one wrong word for one correct word,
    // we need a list of wrong words.
    private Map<String, List<String>> correctAndWrongWordForms = new HashMap<>();
    private Map<String, List<String>> wrongAndCorrectWordForms = new HashMap<>();

    public void read(String filePath) throws Exception {
        Path file = Paths.get(filePath);
        if (!Files.exists(file)) {
            throw new FileNotFoundException(filePath);
        }

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        String correctAndWrongWordTag;

        while ((line = reader.readLine()) != null) {
//            line = line.toLowerCase();
            correctAndWrongWordTag = extractErrorTagFromLine(line);
            addCorrectAndWrongWordForms(correctAndWrongWordTag);
        }
    }

    private String extractErrorTagFromLine(String line) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()) {
            stringBuilder.append(matcher.group());
        }
        return stringBuilder.toString();
    }

    private void addToMap(Map<String, List<String>> map, String key, String value) {
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

    private void addCorrectAndWrongWordForms(String correctAndWrongWordTag) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(correctAndWrongWordTag);

        String correctWord;
        String wrongWord;

        while (matcher.find()) {
            correctWord = matcher.group(2);
            wrongWord = matcher.group(5);

            addToMap(correctAndWrongWordForms, correctWord, wrongWord);
            addToMap(wrongAndCorrectWordForms, wrongWord, correctWord);
        }
    }

    public Map<String, List<String>> getCorrectAndWrongWordForms() {
        return correctAndWrongWordForms;
    }

    public Map<String, List<String>> getWrongAndCorrectWordForms() {
        return wrongAndCorrectWordForms;
    }

    public void printMap() {
        for (Map.Entry<String, List<String>> entry: wrongAndCorrectWordForms.entrySet()) {
            System.out.println("Word is : " + entry.getKey());
            System.out.println("Word list :");
            for (String wrongWord : entry.getValue()) {
                System.out.println(wrongWord);
            }
        }
    }

}
