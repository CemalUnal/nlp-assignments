import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NaiveBayes {

//    C(fj , si) is the
    private static Map<String, Integer> sensesWithMaxProbability = new HashMap<>();

    public int getNaiveBayesProbability() {

        Map<Integer, Map<List<UnorderedWord>, Double>> fOneFVTrainSet = Preprocessing.getfOneFeatureVectorTrainSet();
        Map<Integer, Map<List<UnorderedWord>, Double>> fTwoFVTrainSet = Preprocessing.getfTwoFeatureVectorTrainSet();

        Map<String, Map<List<UnorderedWord>, Double>> fOneFVTestSet = Preprocessing.getfOneFeatureVectorTestSet();
        Map<String, Map<List<UnorderedWord>, Double>> fTwoFVTestSet = Preprocessing.getfTwoFeatureVectorTestSet();

        // is equal to N in the assignment sheet
        double allInstancesInTrainingSet = Preprocessing.getAllInstancesInTrainingSet();

        for (Map.Entry<String, Map<List<UnorderedWord>, Double>> testEntry : fOneFVTestSet.entrySet()) {
            Map<List<UnorderedWord>, Double> innerTestMap = testEntry.getValue();

            for (Map.Entry<List<UnorderedWord>, Double> testItem : innerTestMap.entrySet()) {
                List<UnorderedWord> testFeatureList = testItem.getKey(); // FJ

                double maxProbability = 0.0;
                for (Map.Entry<Integer, Map<List<UnorderedWord>, Double>> trainEntry : fOneFVTrainSet.entrySet()) {
//                    System.out.println("-- " + trainEntry.getKey());
//                    System.out.println("-- " + trainEntry.getValue().get(testFeatureList));

                    // the number of context words belonging to si in the training corpus
                    // C(si)
                    double numberOfContextWordsBelongingToSenseId = getNumberOfContextWordsBelongingToSenseId(trainEntry.getValue());

                    // number of occurrences of fj in a context of sense si in the training corpus,
                    // C(fj, si)
                    Double numberOfTestFeatureInTrainFeature = trainEntry.getValue().get(testFeatureList);

                    // P(fj |w = si) = C(fj, si)/C(si)
                    double stepOneProbability;

                    stepOneProbability = (numberOfTestFeatureInTrainFeature + 1) / (numberOfContextWordsBelongingToSenseId + testFeatureList.size());

//                    if (numberOfTestFeatureInTrainFeature != null) {
//                        stepOneProbability = numberOfTestFeatureInTrainFeature / numberOfContextWordsBelongingToSenseId;
//                    } else {
//                        stepOneProbability = 1 / N;
//                    }

                    // P(si) = C(si)/N
                    double stepTwoProbability = numberOfContextWordsBelongingToSenseId / allInstancesInTrainingSet;

                    // P(fj |w = si) ∗ P(si)
                    double finalProbability = stepOneProbability * stepTwoProbability;

                    if (finalProbability > maxProbability)
                        maxProbability = finalProbability;
                }
            }
        }

        return 0;
    }

    private double getNumberOfContextWordsBelongingToSenseId(Map<List<UnorderedWord>, Double> map) {
        double result = 0.0;

        for (Map.Entry<List<UnorderedWord>, Double> item : map.entrySet()) {
            result = result + item.getValue();
        }

        return result;
    }
}
