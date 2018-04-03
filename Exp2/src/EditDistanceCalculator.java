import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditDistanceCalculator {

    private static String typeOfOperation;
    private static String correctLetters;
    private static String wrongLetters;

    /**
     * Removes punctuation marks from the beginning and
     * end of all strings in a line (call these strings plain word)
     * Also adds sentence boundaries
     *
     * @param line a line in the dataset
     * @return a line with plain words
     */
    public String addSentenceBoundary(String line) {
        String punctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
        StringJoiner stringJoiner = new StringJoiner(" ");

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

    public boolean minEditDistanceIsOne(String word, Map<String, Double> unigramCountsMap) {
        Preprocessing preprocessing = new Preprocessing();

        double minEditDistance;
        boolean minEditDistanceIsOne = false;

        for (Map.Entry<String, Double> entry : unigramCountsMap.entrySet()) {
            String datasetToken = entry.getKey();
            int tokenLength = datasetToken.length();
            int wordLength = word.length();

            if (Math.abs(tokenLength - wordLength) == 1 || tokenLength - wordLength == 0) {
                minEditDistance = getMinEditDistance(datasetToken, word, tokenLength, wordLength);

                if (minEditDistance == 1) {
                    if (typeOfOperation.equals("insertion")) {
                        preprocessing.addToInsertionInfoMap(correctLetters, wrongLetters);
                    } else if (typeOfOperation.equals("deletion")) {
                        preprocessing.addToDeletionInfoMap(correctLetters, wrongLetters);
                    } else if (typeOfOperation.equals("substitution")) {
                        preprocessing.addToSubstitutionInfoMap(correctLetters, wrongLetters);
                    }
                    minEditDistanceIsOne = true;
//                    return minEditDistance;
                }
            }
        }

        return minEditDistanceIsOne;
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

            minEditDistance = deletionEditDistance;
        }
        // type is substitution
        else {
            typeOfOperation = "substitution";
            char currentLetter = token.charAt(tokenLength - 1);
            char previousLetter = word.charAt(wordLength - 1);

            correctLetters = String.valueOf(currentLetter);
            wrongLetters = String.valueOf(previousLetter);

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
