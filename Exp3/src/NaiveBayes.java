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
        Preprocessing preprocessing = new Preprocessing();

        Map<String, Map<String, Double>> featureVectorTrainSet = Preprocessing.getFeatureVectorTrainSet();
        Map<String, List<String>> wordsWithSenseIds = Preprocessing.getWordsWithSenseIds();

        String currentTestSetWord = preprocessing.getWordFromWordId(currentTestSetWordId);

        List<String> currentPossibleSenseIds = wordsWithSenseIds.get(currentTestSetWord);

        // numberOfAllWordsInTrainingSet is equal to N in the assignment sheet
        double numberOfAllWordsInTrainingSet = Preprocessing.getNumberOfAllWordsInTrainingSet();

        double maxProbability = 0.0;
        String senseIdWithMaxProb = "";

        for (String senseId : currentPossibleSenseIds) {
            Map<String, Double> currentFeatureVector = featureVectorTrainSet.get(senseId);

            if (currentFeatureVector != null) {
                // C(si)
                double numberOfContextWordsBelongingToSenseId = getNumberOfContextWordsWithSpecificSense(currentFeatureVector);
                double subProduct = 1.0;

                // P(si) = C(si)/N
                double probabilityOfCurrentSense = numberOfContextWordsBelongingToSenseId / numberOfAllWordsInTrainingSet;
                for (String currentTestFeatureWord : currentTestSetFeature) {

                    // C(fj, si)
                    double numberOfTestFeatureWordOccurrenceBelongingToSenseId;

                    if (currentFeatureVector.get(currentTestFeatureWord) != null) {
                        numberOfTestFeatureWordOccurrenceBelongingToSenseId = currentFeatureVector.get(currentTestFeatureWord);
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
                    senseIdWithMaxProb = senseId;
                }
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