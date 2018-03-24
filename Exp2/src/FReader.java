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

    // since there can be more than one wrong word for one correct word,
    // we need a list of wrong words.
    private Map<String, List<String>> correctAndWrongWordForms = new HashMap<>();

    public void read(String filePath) throws Exception {
        Path file = Paths.get(filePath);
        if (!Files.exists(file)) {
            throw new FileNotFoundException(filePath);
        }

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        String correctAndWrongWordTag;

        while ((line = reader.readLine()) != null) {
            if (!line.equals("")) {
                System.out.println(line);
                correctAndWrongWordTag = extractErrorTagFromLine(line);
                if (!correctAndWrongWordTag.equals(""))
                    addCorrectAndWrongWordForms(correctAndWrongWordTag);
            }
        }
    }

    private String extractErrorTagFromLine(String line) {
        String errorTagRegex = "<ERR targ=[a-zA-Z\\p{Punct}]+> [a-zA-Z\\p{Punct}\\s]+ </ERR>";

        Pattern pattern = Pattern.compile(errorTagRegex);
        Matcher matcher = pattern.matcher(line);
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()) {
            stringBuilder.append(matcher.group());
        }
        return stringBuilder.toString();
    }
//[^<>/=]+
    private void addCorrectAndWrongWordForms(String correctAndWrongWordTag) {
//        System.out.println(correctAndWrongWordTag);
//        Pattern pattern = Pattern.compile("(<ERR targ=)([a-zA-Z]+)(>)(\\s)([a-zA-Z\\s]+)(\\s)(</ERR>)");
        Pattern pattern = Pattern.compile("(<ERR targ=)([a-zA-Z\\p{Punct}]+)(>)(\\s)([a-zA-Z\\p{Punct}\\s]+)(\\s)(</ERR>)");
        //                                      1               2            3   4              5             6      7
        //SORUN: \p{Punct} REGEXI </ERR> TAGLERINI MATCH ETTIGI ICIN DUZGUN AYIRAMIYOR...
        Matcher matcher = pattern.matcher(correctAndWrongWordTag);

        String correctWord;
        String wrongWord;

        while (matcher.find()) {
            correctWord = matcher.group(2);
            wrongWord = matcher.group(5);

            System.out.println(matcher.group(0));
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
            System.out.println(matcher.group(3));
            System.out.println(matcher.group(4));
            System.out.println(matcher.group(5));
            System.out.println(matcher.group(6));
            System.out.println(matcher.group(7));
            System.out.println("------------ - ------------------ ------------------- ---------------- ---------------------- ");
//            // if the map contains the correct word, then update its wrong words
//            if (correctAndWrongWordForms.containsKey(correctWord)) {
//                // And we do not want to add the same
//                // wrong word more than one time
//                if (!correctAndWrongWordForms.get(correctWord).contains(wrongWord)) {
//                    correctAndWrongWordForms.get(correctWord).add(wrongWord);
//                }
//            } else {
//                // if the map not contains the correct word, then add the current correct word to map
//                List<String> wrongWords = new ArrayList<>();
//                wrongWords.add(wrongWord);
//                correctAndWrongWordForms.put(correctWord, wrongWords);
//            }
        }
    }

    public Map<String, List<String>> getCorrectAndWrongWordForms() {
        return correctAndWrongWordForms;
    }

    public void printMap() {
        for (Map.Entry<String, List<String>> entry: correctAndWrongWordForms.entrySet()) {
            System.out.println("Correct word is : " + entry.getKey());
            System.out.println("Wrong word list :");
            for (String wrongWord : entry.getValue()) {
                System.out.println(wrongWord);
            }
        }
    }

}
