import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatasetOperations {

    private EditDistanceDetails edd;
    private Map<String, EditDistanceDetails> editDetailsOfWrongWords = new HashMap<>();

    private String typeOfOperation;
    private String correctLetters;
    private String wrongLetters;

    /**
     * Creates a sentence with sentence boundaries "<s>" and "</s>".
     *
     * @param correctedDatasetSentences all tokens that are in the input file
     * @return a complete sentence with boundaries
     */
    public List<String> addSentenceBoundary(List<String> correctedDatasetSentences) {
        String punctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
        StringJoiner stringJoiner = new StringJoiner(" ");
        List<String> sentences = new ArrayList<>();

        for (String sentence : correctedDatasetSentences) {

            List<String> tokens = separateIntoTokens(sentence);

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

            if (!stringJoiner.toString().equals("<s> </s>"))
                sentences.add(stringJoiner.toString());

            stringJoiner = new StringJoiner(" ");
        }
        return sentences;
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
                    editDetailsOfWrongWords.put(word, edd);

                    if (typeOfOperation.equals("insertion")) {
                        hmm.addToInsertionInfoMap(correctLetters, wrongLetters);
                    } else if (typeOfOperation.equals("deletion")) {
                        hmm.addToDeletionInfoMap(correctLetters, wrongLetters);
                    } else if (typeOfOperation.equals("substitution")) {
                        hmm.addToSubstitutionInfoMap(correctLetters, wrongLetters);
                    }
                }
            }
        }

        return minEditDistance;
    }

    private int getMinEditDistance(String token, String word, int tokenLength, int wordLength) {
        // If the correctWord is empty, the cost is equal to
        // the length of wrongWord
        if (tokenLength == 0) {
            if (wordLength - 1 == 0) {
                typeOfOperation = "insertion";
                String currentLetter = String.valueOf(word.charAt(wordLength - 1));
                String previousLetter = "#";

//                String correctLetters = String.valueOf(previousLetter);
                correctLetters = String.valueOf(previousLetter);
//                String wrongLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
                wrongLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);

                edd = new EditDistanceDetails(token, correctLetters,  wrongLetters, typeOfOperation, 0.0);
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

//                String correctLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
                correctLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
//                String wrongLetters = String.valueOf(previousLetter);
                wrongLetters = String.valueOf(previousLetter);

                edd = new EditDistanceDetails(token, correctLetters, wrongLetters, typeOfOperation, 0.0);
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
//            String typeOfOperation = "insertion";
            typeOfOperation = "insertion";
            String currentLetter = String.valueOf(word.charAt(wordLength - 1));
            String previousLetter = "#";

            if (wordLength - 2 >= 0) {
                previousLetter = String.valueOf(word.charAt(wordLength - 2));
            }

//            String correctLetters = String.valueOf(previousLetter);
            correctLetters = String.valueOf(previousLetter);
            wrongLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
//            String wrongLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);

            edd = new EditDistanceDetails(token, correctLetters,  wrongLetters, typeOfOperation, 0.0);
//            hmm.addToInsertionInfoMap(correctLetters, wrongLetters);

            minEditDistance = insertionEditDistance;
        }
        // type is deletion
        else if (deletionEditDistance <= insertionEditDistance && deletionEditDistance <= substitutionEditDistance) {
//            String typeOfOperation = "deletion";
            typeOfOperation = "deletion";

            char currentLetter = token.charAt(tokenLength - 1);
            char previousLetter = '-';

            if (wordLength - 1 >= 0) {
                previousLetter = word.charAt(wordLength - 1);
            }

//            String correctLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
            correctLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
//            String wrongLetters = String.valueOf(previousLetter);
            wrongLetters = String.valueOf(previousLetter);

            edd = new EditDistanceDetails(token, correctLetters, wrongLetters, typeOfOperation, 0.0);
//            hmm.addToDeletionInfoMap(correctLetters, wrongLetters);

            minEditDistance = deletionEditDistance;
        }
        // type is substitution
        else {
//            String typeOfOperation = "substitution";
            typeOfOperation = "substitution";
            char currentLetter = token.charAt(tokenLength - 1);
            char previousLetter = word.charAt(wordLength - 1);

            correctLetters = String.valueOf(currentLetter);
            wrongLetters = String.valueOf(previousLetter);

            edd = new EditDistanceDetails(token, String.valueOf(currentLetter),  String.valueOf(previousLetter), typeOfOperation, 0.0);
//            hmm.addToSubstitutionInfoMap(String.valueOf(currentLetter), String.valueOf(previousLetter));

            minEditDistance = substitutionEditDistance;
        }

        return 1 + minEditDistance;
    }

//    private int getMinEditDistanceIterative(String word, String token) {
//        int wordLength = word.length();
//        int tokenLength = token.length();
//
//        int[][] table = new int[wordLength + 1][tokenLength + 1];
//
//        int insertionEditDistance;
//        int deletionEditDistance;
//        int substitutionEditDistance;
//        int minEditDistance;
//
//        for (int i = 0; i <= wordLength; i++) {
//            for (int j = 0; j <= tokenLength; j++) {
//                // If first string is empty, only option is to
//                // insert all characters of second string
//                if (i == 0) {
//                    table[i][j] = j; // Min. operations = j
//                }
//                // If second string is empty, only option is to
//                // remove all characters of second string
//                else if (j == 0) {
//                    table[i][j] = i; // Min. operations = i
//                }
//
//                // If last characters are same, ignore last char
//                // and recur for remaining string
//                else if (word.charAt(i - 1) == token.charAt(j - 1)) {
//                    table[i][j] = table[i - 1][j - 1];
//                }
//
//                else {
//                    insertionEditDistance = table[i][j - 1];
//                    deletionEditDistance = table[i - 1][j];
//                    substitutionEditDistance = table[i - 1][j - 1];
//
//                    // type is insertion
//                    if (insertionEditDistance <= deletionEditDistance && insertionEditDistance <= substitutionEditDistance) {
//                        minEditDistance = insertionEditDistance;
//                    }
//                    // type is deletion
//                    else if (deletionEditDistance <= insertionEditDistance && deletionEditDistance <= substitutionEditDistance) {
//                        minEditDistance = deletionEditDistance;
//                    }
//                    // type is substitution
//                    else {
//                        minEditDistance = substitutionEditDistance;
//                    }
//
//                    table[i][j] = minEditDistance;
//                }
//                // If last character are different, consider all
//                // possibilities and find minimum
////                else {
////                    table[i][j] = 1 + min(table[i][j - 1],  // Insert
////                            table[i - 1][j],  // Remove
////                            table[i - 1][j - 1]); // Replace
////                }
//            }
//        }
//        return table[wordLength][tokenLength];
//    }

    public Map<String, EditDistanceDetails> getEditDetailsOfWrongWords() {
        return editDetailsOfWrongWords;
    }
}
