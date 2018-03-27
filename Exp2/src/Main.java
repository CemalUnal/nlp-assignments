import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
//    private static
    private static EditDistanceDetails edd;
    private static Map<String, EditDistanceDetails> editDetailsOfWrongWords = new HashMap<>();

    private static List<String> correctedDataset;
    private static List<String> sentences;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please specify one input file and one output file. Example execution is --> Java Main yourInputFile yourOutputFile");
            return;
        }

        String inputFile = args[0];
        String resultsFile = args[1];

        FReader fileReader = new FReader();
        FWriter fileWriter = new FWriter();
        HiddenMarkovModel hmm = new HiddenMarkovModel();

        List<String> endMarks = new ArrayList<>();
        endMarks = initializeSentenceEndingMarkList(endMarks);

        try {
            correctedDataset = fileReader.read(inputFile);
            sentences = addSentenceBoundary(correctedDataset, endMarks);

//            System.out.println(correctedDataset.size());

            hmm.createUnigramModel(sentences);
            hmm.createBigramModel(sentences);
//            hmm.printMap();

            Map<String, List<String>> wrongAndCorrectWordForms = fileReader.getWrongAndCorrectWordForms();
//            Map<String, List<String>> correctAndWrongWordForms = fileReader.getCorrectAndWrongWordForms();

            for (Map.Entry<String, List<String>> wrongWord : wrongAndCorrectWordForms.entrySet()) {
                for (String correctWord : wrongWord.getValue()) {
                    int minEditDistance = getMinEditDistance(correctWord, wrongWord.getKey(), correctWord.length(), wrongWord.getKey().length());
                    if (minEditDistance == 1 && edd != null) {
                        editDetailsOfWrongWords.put(wrongWord.getKey(), edd);
                    }
                }
            }

            HiddenMarkovModel.getEmissionProbability("a", "b");
            for (Map.Entry<String, List<String>> entry: wrongAndCorrectWordForms.entrySet()) {
                System.out.println("Wrong word is : " + entry.getKey());
                System.out.println("Correct word list :");
                for (String correctWord : entry.getValue()) {
                    System.out.println(correctWord);
                }
            }
//
//            for (Map.Entry<String, List<String>> entry : correctAndWrongWordForms.entrySet()) {
//                System.out.println("Correct word is : " + entry.getKey());
//                System.out.println("Wrong word list :");
//                for (String wrongWord : entry.getValue()) {
//                    System.out.println(wrongWord);
//                }
//            }
//            for (Map.Entry<String, EditDistanceDetails> entry : editDetailsOfWrongWords.entrySet()) {
//                System.out.println("Wrong Word: " + entry.getKey());
//                System.out.println("TypeOfOperation: " + entry.getValue().getTypeOfOperation());
//                System.out.println("CorrectionWord: " + entry.getValue().getCorrectionWord());
//                System.out.println("CorrectLetters: " + entry.getValue().getCorrectLetters());
//                System.out.println("WrongLetters: " + entry.getValue().getWrongLetters());
//                System.out.println("-----------------------------------------------------------");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates a List that contains sentence ending marks as regex.
     *
     * @param endMarkList
     * @return list of sentence ending marks
     */
    private static List<String> initializeSentenceEndingMarkList(List<String> endMarkList) {
        endMarkList.add("[.]+");
        endMarkList.add("[!]+");
        endMarkList.add("[?]+");

        return endMarkList;
    }

    /**
     * Creates a sentence with sentence boundaries "<s>" and "</s>".
     *
     * @param allTokens all tokens that are in the input file
     * @param endMarkList list of sentence ending marks
     * @return a complete sentence with boundaries
     */
    private static List<String> addSentenceBoundary(List<String> allTokens, List<String> endMarkList) {
        String punctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
        StringJoiner stringJoiner = new StringJoiner(" ");
        List<String> sentences = new ArrayList<>();

        for (String token : allTokens) {

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

            // if the token is a punctuation mark or empty string
            if (punctuation.indexOf(token.charAt(token.length() - 1)) == -1 ) {
                stringJoiner.add(token);
            }

            for (String endMark : endMarkList) {
                if (Pattern.compile(endMark).matcher(token).find()) {
                    stringJoiner.add("</s>");
//                    sentences.add("<s>" + " " + stringJoiner.toString().toLowerCase());
                    sentences.add("<s>" + " " + stringJoiner.toString());
                    stringJoiner = new StringJoiner(" ");
                }
            }
        }
        return sentences;
    }

    private static int getMinEditDistance(String correctWord, String wrongWord, int lenOfCorrectWord, int lenOfWrongWord) {
        // If the correctWord is empty, the cost is equal to
        // the length of wrongWord
        if (lenOfCorrectWord == 0) {
            if (lenOfWrongWord - 1 == 0) {
                String currentLetter = String.valueOf(wrongWord.charAt(lenOfWrongWord - 1));
                String previousLetter = "-";

                String correctLetters = String.valueOf(previousLetter);
                String wrongLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);

                edd = new EditDistanceDetails(correctWord, correctLetters,  wrongLetters, "addition", 0.0);
            }
            return lenOfWrongWord;
        }

        // If the wrongWord is empty, the cost is equal to
        // the length of the correctWord
        if (lenOfWrongWord == 0) {
            if (lenOfCorrectWord - 1 == 0) {
                char currentLetter = correctWord.charAt(lenOfCorrectWord - 1);
                char previousLetter = '-';

//            deleteIndex = lenOfCorrectWord - 1;
                String correctLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
                String wrongLetters = String.valueOf(currentLetter);
                edd = new EditDistanceDetails(correctWord, correctLetters, wrongLetters, "deletion", 0.0);
            }
            return lenOfCorrectWord;
        }

        // If last characters of two words are same, just ignore them
        if (correctWord.charAt(lenOfCorrectWord - 1) == wrongWord.charAt(lenOfWrongWord - 1)) {
//            flag = true;////////////////////////////////////////
            return getMinEditDistance(correctWord, wrongWord, lenOfCorrectWord - 1, lenOfWrongWord - 1);
        }
        // If last characters are not same, calculate all three operation cost
        int insertionEditDistance = getMinEditDistance(correctWord, wrongWord, lenOfCorrectWord, lenOfWrongWord - 1);
        int deletionEditDistance = getMinEditDistance(correctWord, wrongWord, lenOfCorrectWord - 1, lenOfWrongWord);
        int substitutionEditDistance = getMinEditDistance(correctWord,  wrongWord, lenOfCorrectWord - 1, lenOfWrongWord - 1);

        // Pick the minimum distance

        int minEditDistance;

        // type is insertion
        if (insertionEditDistance <= deletionEditDistance && insertionEditDistance <= substitutionEditDistance) {
            String typeOfOperation = "insertion";
            String currentLetter = String.valueOf(wrongWord.charAt(lenOfWrongWord - 1));
            String previousLetter = "-";

            if (lenOfWrongWord - 2 >= 0) {
                previousLetter = String.valueOf(wrongWord.charAt(lenOfWrongWord - 2));
            }

            String correctLetters = String.valueOf(previousLetter);
            String wrongLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);

            edd = new EditDistanceDetails(correctWord, correctLetters,  wrongLetters, typeOfOperation, 0.0);

            minEditDistance = insertionEditDistance;
        }
        // type is deletion
        else if (deletionEditDistance <= insertionEditDistance && deletionEditDistance <= substitutionEditDistance) {
            String typeOfOperation = "deletion";

            char currentLetter = correctWord.charAt(lenOfCorrectWord - 1);
            char previousLetter = '-';

            if (lenOfCorrectWord - 1 >= 0) {
                previousLetter = wrongWord.charAt(lenOfWrongWord - 1);
            }

            String correctLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
            String wrongLetters = String.valueOf(currentLetter);
//            deleteIndex = lenOfCorrectWord - 1;

            edd = new EditDistanceDetails(correctWord, correctLetters, wrongLetters, typeOfOperation, 0.0);

            minEditDistance = deletionEditDistance;
        }
        // type is substitution
        else {
            String typeOfOperation = "substitution";
            char currentLetter = correctWord.charAt(lenOfCorrectWord - 1);
            char previousLetter = wrongWord.charAt(lenOfWrongWord - 1);

            edd = new EditDistanceDetails(correctWord, String.valueOf(currentLetter),  String.valueOf(previousLetter), typeOfOperation, 0.0);

            minEditDistance = substitutionEditDistance;
        }

        return (1 + minEditDistance);
    }

    // TODO: BUNU BASKA BIR CLASS'A AL.
    public static Map<String, EditDistanceDetails> getEditDetailsOfWrongWords() {
        return editDetailsOfWrongWords;
    }

    // TODO: BUNU BASKA BIR CLASS'A AL. YA DA BUNU KULLANAN METHODU BURAYA TASI.
    public static List<String> getCorrectedDataset() {
        return correctedDataset;
    }

    // TODO: BUNU BASKA BIR CLASS'A AL. YA DA BUNU KULLANAN METHODU BURAYA TASI.
    public static List<String> getSentences() {
        return sentences;
    }
}
