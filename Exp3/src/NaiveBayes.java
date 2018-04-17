import java.io.IOException;
import java.util.List;
import java.util.Map;

public class NaiveBayes {

    //    public int getNaiveBayesProbability(String outputFile) throws IOException {
    public static int getNaiveBayesProbability(String currentTestSetWordId, List<String> currentTestSetFeature, FWriter fileWriter) throws IOException {

        Map<String, Map<String, Double>> featureVectorTrainSet = Preprocessing.getFeatureVectorTrainSet();
//        Map<String, List<String>> featureVectorTestSet = Preprocessing.getFeatureVectorTestSet();

//        System.out.println(featureVectorTrainSet.size());
//        System.out.println(featureVectorTestSet.size());

        // is equal to N in the assignment sheet
        double allLexeltsInTrainingSet = Preprocessing.getAllLexeltsInTrainingSet();
//        List<String> ambiguousWords = Preprocessing.getAmbiguousWords();

//        for (Map.Entry<String, List<String>> testEntry : featureVectorTestSet.entrySet()) {
//            String currentTestSetWord = testEntry.getKey();
//            List<String> currentTestSetFeature = testEntry.getValue();

        double maxProbability = 0.0;
        String senseIdWithMaxProb = "";

        for (Map.Entry<String, Map<String, Double>> trainEntry : featureVectorTrainSet.entrySet()) {

            // C(si)
            double numberOfContextWordsBelongingToSenseId = getNumberOfContextWordsWithSpecificSense(trainEntry.getValue());
            double subProduct = 1.0;
            for (String currentFeatureItem : currentTestSetFeature) {

                // C(fj, si)
                double numberOfOccurrenceBelongingToSenseId;

                if (trainEntry.getValue().get(currentFeatureItem) != null) {
                    numberOfOccurrenceBelongingToSenseId = trainEntry.getValue().get(currentFeatureItem);
                } else {
                    numberOfOccurrenceBelongingToSenseId = 1.0;
                }


                // P(fj |w = si) = C(fj, si)/C(si)
                double stepOneProbability = numberOfOccurrenceBelongingToSenseId / numberOfContextWordsBelongingToSenseId;

                subProduct = subProduct * stepOneProbability;
            }

            // P(si) = C(si)/N
            double stepTwoProbability = numberOfContextWordsBelongingToSenseId / allLexeltsInTrainingSet;

            double finalProductOfProbabilities = subProduct * stepTwoProbability;

            if (finalProductOfProbabilities > maxProbability) {
                maxProbability = finalProductOfProbabilities;
                senseIdWithMaxProb = trainEntry.getKey();
            }
        }

        fileWriter.write(String.format("%s %s%n", currentTestSetWordId, senseIdWithMaxProb));
        return 0;
    }

    private static double getNumberOfContextWordsWithSpecificSense(Map<String, Double> map) {
        double contextWordCount = 0.0;

        for (Map.Entry<String, Double> item : map.entrySet()) {
            contextWordCount = contextWordCount + item.getValue();
        }

        return contextWordCount;
    }

//    private double getNaiveBayesProbability(Map<List<UnorderedWord>, Count> innerMap, String ambiguousWord,
//                                            double allLexeltsInTrainingSet, List<UnorderedWord> testFeatureList) {
//
//        // number of occurrences of fj in a context of sense si in the training corpus,
//        // C(fj, si)
//        double numberOfOccurrenceBelongingToSenseId = getNumberOfOccurrenceBelongingToSenseId(innerMap, ambiguousWord);
//
//        // the number of context words belonging to si in the training corpus
//        // C(si)
//        double numberOfContextWordsBelongingToSenseId = getNumberOfContextWordsWithSpecificSense(innerMap);
//
//        // P(fj |w = si) = C(fj, si)/C(si)
//        double stepOneProbability;
//
//        stepOneProbability = (numberOfOccurrenceBelongingToSenseId + 1) / (numberOfContextWordsBelongingToSenseId + testFeatureList.size());
//
//        // P(si) = C(si)/N
//        double stepTwoProbability = numberOfContextWordsBelongingToSenseId / allLexeltsInTrainingSet;
//
//        // P(fj |w = si) ∗ P(si)
//        return stepOneProbability * stepTwoProbability;
//    }
//
//    private double getNumberOfOccurrenceBelongingToSenseId(Map<List<UnorderedWord>, Count> map, String word) {
//        double numberOfOccurrence = 0.0;
//
//        for (Map.Entry<List<UnorderedWord>, Count> item : map.entrySet()) {
//            for (UnorderedWord unorderedWord : item.getKey()) {
//                if (unorderedWord.getWord().equalsIgnoreCase(word)) {
//                    numberOfOccurrence ++;
//                }
//            }
//        }
//
//        return numberOfOccurrence;
//    }
}
