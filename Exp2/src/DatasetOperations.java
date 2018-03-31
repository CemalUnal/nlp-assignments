import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatasetOperations {

    private EditDistanceDetails edd;
//    private Map<String, EditDistanceDetails> editDetailsOfWrongWords = new HashMap<>();

    private String typeOfOperation;
    private String correctLetters;
    private String wrongLetters;

    /**
     * Creates a sentence with sentence boundaries "<s>" and "</s>".
     *
     * @param line all tokens that are in the input file
     * @return a complete sentence with boundaries
     */
    public String addSentenceBoundary(String line) {
        String punctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
        StringJoiner stringJoiner = new StringJoiner(" ");

//        for (String sentence : correctedDatasetSentences) {

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

//            if (!stringJoiner.toString().equals("<s> </s>"))
//                sentences.add(stringJoiner.toString());

//            stringJoiner = new StringJoiner(" ");
//        }
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

    public int calculateMinEditDistance(String word, Map<String, Double> unigramCountsMap) {
        HiddenMarkovModel hmm = new HiddenMarkovModel();
        int minEditDistance = 0;

        for (Map.Entry<String, Double> entry : unigramCountsMap.entrySet()) {
            String datasetToken = entry.getKey();
            int tokenLength = datasetToken.length();
            int wordLength = word.length();

            if (Math.abs(tokenLength - wordLength) == 1 || Math.abs(tokenLength - wordLength) == 0) {
                minEditDistance = getMinEditDistance(datasetToken, word, tokenLength, wordLength);

                if (minEditDistance == 1) {
//                    editDetailsOfWrongWords.put(word, edd);
//                    System.out.println(datasetToken + " - " + word);

                    if (typeOfOperation.equals("insertion")) {
                        hmm.addToInsertionInfoMap(correctLetters, wrongLetters);
                    } else if (typeOfOperation.equals("deletion")) {
                        hmm.addToDeletionInfoMap(correctLetters, wrongLetters);
                    } else if (typeOfOperation.equals("substitution")) {
                        hmm.addToSubstitutionInfoMap(correctLetters, wrongLetters);
                    }

                    return minEditDistance;
                }
            }
        }
//        System.out.println(minEditDistance);
        return minEditDistance;
    }

    public int getMinEditDistance(String token, String word, int tokenLength, int wordLength) {
        // If the correctWord is empty, the cost is equal to
        // the length of wrongWord
        if (tokenLength == 0) {
            if (wordLength - 1 == 0) {
                typeOfOperation = "insertion";
                String currentLetter = String.valueOf(word.charAt(wordLength - 1));
                String previousLetter = "#";

                correctLetters = String.valueOf(previousLetter);
                wrongLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);

//                edd = new EditDistanceDetails(token, correctLetters, word, wrongLetters);
//                hmm.addToInsertionInfoMap(correctLetters, wrongLetters);
            }
            return wordLength;
        }

        // If the wrongWord is empty, the cost is equal to
        // the length of the correctWord
        if (wordLength == 0) {
            if (tokenLength - 1 == 0) {
                typeOfOperation = "deletion";
                char currentLetter = token.charAt(tokenLength - 1);
                char previousLetter = '-';

                correctLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
                wrongLetters = String.valueOf(previousLetter);

//                edd = new EditDistanceDetails(token, correctLetters, word, wrongLetters);
//                hmm.addToDeletionInfoMap(correctLetters, wrongLetters);
            }
            return tokenLength;
        }

        // If last characters of two strings are same, nothing
        // much to do. Ignore last characters and get count for
        // remaining strings.
        if (token.charAt(tokenLength - 1) == word.charAt(wordLength - 1))
            return getMinEditDistance(token, word, tokenLength - 1, wordLength - 1);

        // If last characters are not same, calculate all three operation cost
        int insertionEditDistance = getMinEditDistance(token, word, tokenLength, wordLength - 1);
        int deletionEditDistance = getMinEditDistance(token, word, tokenLength - 1, wordLength);
        int substitutionEditDistance = getMinEditDistance(token, word, tokenLength - 1, wordLength - 1);

        int minEditDistance;

        // type is insertion
        if (insertionEditDistance <= deletionEditDistance && insertionEditDistance <= substitutionEditDistance) {
            typeOfOperation = "insertion";
            String currentLetter = String.valueOf(word.charAt(wordLength - 1));
            String previousLetter = "#";

            if (wordLength - 2 >= 0) {
                previousLetter = String.valueOf(word.charAt(wordLength - 2));
            }

            correctLetters = String.valueOf(previousLetter);
            wrongLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);

//            edd = new EditDistanceDetails(token, correctLetters, word, wrongLetters);
//            hmm.addToInsertionInfoMap(correctLetters, wrongLetters);

            minEditDistance = insertionEditDistance;
        }
        // type is deletion
        else if (deletionEditDistance <= insertionEditDistance && deletionEditDistance <= substitutionEditDistance) {
            typeOfOperation = "deletion";

            char currentLetter = token.charAt(tokenLength - 1);
            char previousLetter = '-';

            if (wordLength - 1 >= 0) {
                previousLetter = word.charAt(wordLength - 1);
            }

            correctLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
            wrongLetters = String.valueOf(previousLetter);

//            edd = new EditDistanceDetails(token, correctLetters, word, wrongLetters);
//            hmm.addToDeletionInfoMap(correctLetters, wrongLetters);

            minEditDistance = deletionEditDistance;
        }
        // type is substitution
        else {
            typeOfOperation = "substitution";
            char currentLetter = token.charAt(tokenLength - 1);
            char previousLetter = word.charAt(wordLength - 1);

            correctLetters = String.valueOf(currentLetter);
            wrongLetters = String.valueOf(previousLetter);

//            edd = new EditDistanceDetails(token, String.valueOf(currentLetter), word, String.valueOf(previousLetter));
//            hmm.addToSubstitutionInfoMap(String.valueOf(currentLetter), String.valueOf(previousLetter));

            minEditDistance = substitutionEditDistance;
        }

        return 1 + minEditDistance;
    }

    public String getCorrectLetters() {
        return correctLetters;
    }

    public String getWrongLetters() {
        return wrongLetters;
    }
}
