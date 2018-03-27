import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        if (args.length != 2) {
            System.out.println("Please specify one input file and one output file. Example execution is --> Java Main yourInputFile yourOutputFile");
            return;
        }

        String inputFile = args[0];
        String resultsFile = args[1];

        FReader fileReader = new FReader();
        FWriter fileWriter = new FWriter();
        DatasetOperations datasetOperations = new DatasetOperations();
        HiddenMarkovModel hmm = new HiddenMarkovModel();

        try {
            List<String> correctedDatasetLines = fileReader.getCorrectedDatasetLines(inputFile);
            List<String> sentences = datasetOperations.addSentenceBoundary(correctedDatasetLines);

            hmm.createUnigramModel(sentences);
            hmm.createBigramModel(sentences);

            fileReader.initializeWrongCorrectWordsMap();
//            for (String sentence : sentences) {
//                System.out.println(sentence);
//            }

//            Map<String, List<String>> wrongAndCorrectWordForms = fileReader.getWrongAndCorrectWordForms();
//
//            System.out.println("wrongAndCorrectWordForms size: " + wrongAndCorrectWordForms.size());
//            System.out.println("unigram map size: " + hmm.getUnigramCountsMap().size());
//            for (String key : wrongAndCorrectWordForms.keySet()) {
//                datasetOperations.calculateMinEditDistance(key, hmm.getUnigramCountsMap());
//            }
//
            Map<List<String>, Double> cemal1 = hmm.getInsertionInfoMap();
            Map<List<String>, Double> cemal2 = hmm.getDeletionInfoMap();
            Map<List<String>, Double> cemal3 = hmm.getSubstitutionInfoMap();

            for (Map.Entry<List<String>, Double> cemal : cemal1.entrySet()) {
                System.out.println(cemal.getKey() + " - " + cemal.getValue());
            }
//////////////////////
//            for (Map.Entry<List<String>, Double> cemal : cemal2.entrySet()) {
//                System.out.println(cemal.getKey() + " - " + cemal.getValue());
//            }
//
//            for (Map.Entry<List<String>, Double> cemal : cemal3.entrySet()) {
//                System.out.println(cemal.getKey() + " - " + cemal.getValue());
//            }

            Map<String, EditDistanceDetails> editDetails = datasetOperations.getEditDetailsOfWrongWords();

            for (String key : editDetails.keySet()) {
                if (editDetails.get(key).getTypeOfOperation().equals("insertion")) {
                    System.out.println("WrongWord: " + key);
                    System.out.println(editDetails.get(key).getTypeOfOperation());
                    System.out.println("CorrectionWord: " + editDetails.get(key).getCorrectionWord());
                    System.out.println("CorrectLetters: " + editDetails.get(key).getCorrectLetters());
                    System.out.println("WrongLetters: " + editDetails.get(key).getWrongLetters());
                    System.out.println("---------------------------------------------------------------------------");
                }
            }

//            datasetOperations.deneme();
//            for (Map.Entry<String, List<String>> wrongWord : wrongAndCorrectWordForms.entrySet()) {
//                int minEditDistance = getMinEditDistance(correctWord, wrongWord.getKey());
//                if (minEditDistance == 1 && edd != null) {
//                    editDetailsOfWrongWords.put(wrongWord.getKey(), edd);
//                }
//            }

            System.out.println("Execution is done. Estimated execution time is: " + (double) (System.nanoTime() - startTime) / 1000000000.0 + " seconds.%n");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


//    private static int getMinEditDistance(String correctWord, String wrongWord, int lenOfCorrectWord, int lenOfWrongWord) {
//        // If the correctWord is empty, the cost is equal to
//        // the length of wrongWord
//        if (lenOfCorrectWord == 0) {
//            if (lenOfWrongWord - 1 == 0) {
//                String currentLetter = String.valueOf(wrongWord.charAt(lenOfWrongWord - 1));
//                String previousLetter = "-";
//
//                String correctLetters = String.valueOf(previousLetter);
//                String wrongLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
//
//                edd = new EditDistanceDetails(correctWord, correctLetters,  wrongLetters, "addition", 0.0);
//            }
//            return lenOfWrongWord;
//        }
//
//        // If the wrongWord is empty, the cost is equal to
//        // the length of the correctWord
//        if (lenOfWrongWord == 0) {
//            if (lenOfCorrectWord - 1 == 0) {
//                char currentLetter = correctWord.charAt(lenOfCorrectWord - 1);
//                char previousLetter = '-';
//
////            deleteIndex = lenOfCorrectWord - 1;
//                String correctLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
//                String wrongLetters = String.valueOf(currentLetter);
//                edd = new EditDistanceDetails(correctWord, correctLetters, wrongLetters, "deletion", 0.0);
//            }
//            return lenOfCorrectWord;
//        }
//
//        // If last characters of two words are same, just ignore them
//        if (correctWord.charAt(lenOfCorrectWord - 1) == wrongWord.charAt(lenOfWrongWord - 1)) {
////            flag = true;////////////////////////////////////////
//            return getMinEditDistance(correctWord, wrongWord, lenOfCorrectWord - 1, lenOfWrongWord - 1);
//        }
//        // If last characters are not same, calculate all three operation cost
//        int insertionEditDistance = getMinEditDistance(correctWord, wrongWord, lenOfCorrectWord, lenOfWrongWord - 1);
//        int deletionEditDistance = getMinEditDistance(correctWord, wrongWord, lenOfCorrectWord - 1, lenOfWrongWord);
//        int substitutionEditDistance = getMinEditDistance(correctWord,  wrongWord, lenOfCorrectWord - 1, lenOfWrongWord - 1);
//
//        // Pick the minimum distance
//
//        int minEditDistance;
//          1 correct , 2 wrong
//        // type is insertion
//        if (insertionEditDistance <= deletionEditDistance && insertionEditDistance <= substitutionEditDistance) {
//            String typeOfOperation = "insertion";
//            String currentLetter = String.valueOf(wrongWord.charAt(lenOfWrongWord - 1));
//            String previousLetter = "-";
//
//            if (lenOfWrongWord - 2 >= 0) {
//                previousLetter = String.valueOf(wrongWord.charAt(lenOfWrongWord - 2));
//            }
//
//            String correctLetters = String.valueOf(previousLetter);
//            String wrongLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
//
//            edd = new EditDistanceDetails(correctWord, correctLetters,  wrongLetters, typeOfOperation, 0.0);
//
//            minEditDistance = insertionEditDistance;
//        }
//        // type is deletion

//          1 correct , 2 wrong
//        else if (deletionEditDistance <= insertionEditDistance && deletionEditDistance <= substitutionEditDistance) {
//            String typeOfOperation = "deletion";
//
//            char currentLetter = correctWord.charAt(lenOfCorrectWord - 1);
//            char previousLetter = '-';
//
//            if (lenOfCorrectWord - 1 >= 0) {
//                previousLetter = wrongWord.charAt(lenOfWrongWord - 1);
//            }
//
//            String correctLetters = String.valueOf(previousLetter) + String.valueOf(currentLetter);
//            String wrongLetters = String.valueOf(currentLetter);
////            deleteIndex = lenOfCorrectWord - 1;
//
//            edd = new EditDistanceDetails(correctWord, correctLetters, wrongLetters, typeOfOperation, 0.0);
//
//            minEditDistance = deletionEditDistance;
//        }
//        // type is substitution
//        else {
//            String typeOfOperation = "substitution";
//            char currentLetter = correctWord.charAt(lenOfCorrectWord - 1);
//            char previousLetter = wrongWord.charAt(lenOfWrongWord - 1);
//
//            edd = new EditDistanceDetails(correctWord, String.valueOf(currentLetter),  String.valueOf(previousLetter), typeOfOperation, 0.0);
//
//            minEditDistance = substitutionEditDistance;
//        }
//
//        return (1 + minEditDistance);
//    }
}
