import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    private static Map<String, EditDistanceDetails> editDetailsOfWrongWord = new HashMap<>();
    private static EditDistanceDetails edd;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please specify one input file and one output file. Example execution is --> Java Main yourInputFile yourOutputFile");
            return;
        }

        String inputFile = args[0];
        String resultsFile = args[1];

        FReader fileReader = new FReader();
        FWriter fileWriter = new FWriter();

        try {
            fileReader.read(inputFile);
            Map<String, List<String>> wrongAndCorrectWordForms = fileReader.getWrongAndCorrectWordForms();

            for (Map.Entry<String, List<String>> wrongWord : wrongAndCorrectWordForms.entrySet()) {
                for (String correctWord : wrongWord.getValue()) {
                    int minEditDistance = getMinEditDistance(correctWord, wrongWord.getKey(), correctWord.length(), wrongWord.getKey().length());
                    if (minEditDistance == 1) {
                        editDetailsOfWrongWord.put(wrongWord.getKey(), edd);
                    }
                }
            }

            for (Map.Entry<String, EditDistanceDetails> entry : editDetailsOfWrongWord.entrySet()) {
                System.out.println("Wrong Word: " + entry.getKey());
                System.out.println("TypeOfOperation: " + entry.getValue().getTypeOfOperation());
                System.out.println("CorrectionWord: " + entry.getValue().getCorrectionWord());
                System.out.println("CorrectLetter: " + entry.getValue().getCorrectLetter());
                System.out.println("ErrorLetter: " + entry.getValue().getErrorLetter());
                System.out.println("-----------------------------------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static int getMinEditDistance(String correctWord, String wrongWord, int lenOfCorrectWord, int lenOfWrongWord) {
        // If the correctWord is empty, the cost is equal to
        // the length of wrongWord
        if (lenOfCorrectWord == 0) return lenOfWrongWord;

        // If the wrongWord is empty, the cost is equal to
        // the length of the correctWord
        if (lenOfWrongWord == 0) return lenOfCorrectWord;

        // If last characters of two words are same, just ignore them
        if (correctWord.charAt(lenOfCorrectWord - 1) == wrongWord.charAt(lenOfWrongWord - 1))
            return getMinEditDistance(correctWord, wrongWord, lenOfCorrectWord - 1, lenOfWrongWord - 1);

        // If last characters are not same, calculate all three operation cost
        int insertionEditDistance = getMinEditDistance(correctWord, wrongWord, lenOfCorrectWord, lenOfWrongWord - 1);
        int deletionEditDistance = getMinEditDistance(correctWord, wrongWord, lenOfCorrectWord - 1, lenOfWrongWord);
        int substitutionEditDistance = getMinEditDistance(correctWord,  wrongWord, lenOfCorrectWord - 1, lenOfWrongWord - 1);

        // Pick the minimum distance

        int minEditDistance;

        if (insertionEditDistance <= deletionEditDistance && insertionEditDistance <= substitutionEditDistance) {
            String typeOfOperation = "insertion";
            char correctLetter = '-';
            char errorLetter = wrongWord.charAt(lenOfWrongWord - 1);

            edd = new EditDistanceDetails(correctWord, correctLetter,  errorLetter, "insertion");

            minEditDistance = insertionEditDistance;
        } else if (deletionEditDistance <= insertionEditDistance && deletionEditDistance <= substitutionEditDistance) {
            String typeOfOperation = "deletion";
            char correctLetter = correctWord.charAt(lenOfCorrectWord - 1);
            char errorLetter = '-';

            edd = new EditDistanceDetails(correctWord, correctLetter,  errorLetter, typeOfOperation);

            minEditDistance = deletionEditDistance;
        } else {
            editDetailsOfWrongWord.put(wrongWord, edd);
            String typeOfOperation = "substitution";
            char correctLetter = correctWord.charAt(lenOfCorrectWord - 1);
            char errorLetter = wrongWord.charAt(lenOfWrongWord - 1);

            edd = new EditDistanceDetails(correctWord, correctLetter,  errorLetter, typeOfOperation);

            minEditDistance = substitutionEditDistance;
        }

        return 1 + minEditDistance;
    }
}
