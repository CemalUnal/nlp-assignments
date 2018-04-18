import java.io.IOException;
import java.util.List;
import java.util.Map;

public class NaiveBayes {

    /**
     * Calculates naive bayes probabilities for each sense
     * with the given context in the test set.
     * And selects the sense id that has the maximum probability
     *
     * @param currentTestSetWordId current word id in the test set
     * @param currentTestSetFeature current feature set of the current word
     * @param fileWriter FWriter instance
     */
    public static void getNaiveBayesProbability(String currentTestSetWordId, List<String> currentTestSetFeature, FWriter fileWriter) throws IOException {

        Map<String, Map<String, Double>> featureVectorTrainSet = Preprocessing.getFeatureVectorTrainSet();

        // allLexeltsInTrainingSet is equal to N in the assignment sheet
        double allLexeltsInTrainingSet = Preprocessing.getAllLexeltsInTrainingSet();

        double maxProbability = 0.0;
        String senseIdWithMaxProb = "";

        for (Map.Entry<String, Map<String, Double>> trainEntry : featureVectorTrainSet.entrySet()) {

            // C(si)
            double numberOfContextWordsBelongingToSenseId = getNumberOfContextWordsWithSpecificSense(trainEntry.getValue());
            double subProduct = 1.0;

            // P(si) = C(si)/N
            double probabilityOfCurrentSense = numberOfContextWordsBelongingToSenseId / allLexeltsInTrainingSet;
            for (String currentTestFeatureWord : currentTestSetFeature) {

                // C(fj, si)
                double numberOfTestFeatureWordOccurrenceBelongingToSenseId;

                if (trainEntry.getValue().get(currentTestFeatureWord) != null) {
                    numberOfTestFeatureWordOccurrenceBelongingToSenseId = trainEntry.getValue().get(currentTestFeatureWord);
                } else {
                    numberOfTestFeatureWordOccurrenceBelongingToSenseId = 1.0;
                }

                // P(fj |w = si) = C(fj, si)/C(si)
                double conditionalProbabilityOfCurrentFeatureWordBelongingToSenseId =
                        numberOfTestFeatureWordOccurrenceBelongingToSenseId / numberOfContextWordsBelongingToSenseId;

                subProduct = subProduct * conditionalProbabilityOfCurrentFeatureWordBelongingToSenseId;
            }

            double finalProductOfProbabilities = subProduct * probabilityOfCurrentSense;

            if (finalProductOfProbabilities > maxProbability) {
                maxProbability = finalProductOfProbabilities;
                senseIdWithMaxProb = trainEntry.getKey();
            }
        }

        fileWriter.write(String.format("%s %s%n", currentTestSetWordId, senseIdWithMaxProb));
    }

    /**
     * Returns the number of context words belonging to current sense(Si)
     * in the training corpus.
     *
     * @param map word and wordtag-position frequencies
     *
     * @return the number of context words belonging to current sense(Si) in the training corpus
     */
    private static double getNumberOfContextWordsWithSpecificSense(Map<String, Double> map) {
        double contextWordCount = 0.0;

        for (Map.Entry<String, Double> item : map.entrySet()) {
            contextWordCount = contextWordCount + item.getValue();
        }

        return contextWordCount;
    }
}
