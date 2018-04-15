import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NaiveBayes {

    private static Map<String, Integer> sensesWithMaxProbability = new HashMap<>();

    public int getNaiveBayesProbability() {

        Map<Integer, Map<List<UnorderedWord>, Count>> fOneFVTrainSet = Preprocessing.getfOneFeatureVectorTrainSet();
        Map<Integer, Map<List<UnorderedWord>, Count>> fTwoFVTrainSet = Preprocessing.getfTwoFeatureVectorTrainSet();

        Map<String, Map<List<UnorderedWord>, Count>> fOneFVTestSet = Preprocessing.getfOneFeatureVectorTestSet();
        Map<String, Map<List<UnorderedWord>, Count>> fTwoFVTestSet = Preprocessing.getfTwoFeatureVectorTestSet();

        // is equal to N in the assignment sheet
        double allLexeltsInTrainingSet = Preprocessing.getAllLexeltsInTrainingSet();
        List<String> ambiguousWords = Preprocessing.getAmbiguousWords();

        for (String ambiguousWord : ambiguousWords) {

            for (Map.Entry<String, Map<List<UnorderedWord>, Count>> testEntry : fOneFVTestSet.entrySet()) {
                Map<List<UnorderedWord>, Count> innerTestMap = testEntry.getValue();

                for (Map.Entry<List<UnorderedWord>, Count> testItem : innerTestMap.entrySet()) {
                    List<UnorderedWord> testFeatureList = testItem.getKey(); // FJ

                    double maxProbability = 0.0;
                    double finalProbability = 0.0;
                    String senseIdWithMaxProb = "";
                    for (Map.Entry<Integer, Map<List<UnorderedWord>, Count>> trainEntry : fOneFVTrainSet.entrySet()) {

                        finalProbability = getNaiveBayesProbability(trainEntry.getValue(), ambiguousWord,
                                allLexeltsInTrainingSet, testFeatureList);

                        if (finalProbability > maxProbability) {
                            senseIdWithMaxProb = trainEntry.getKey().toString();
                            maxProbability = finalProbability;
                        }

//                        System.out.println(senseIdWithMaxProb + " - " + ambiguousWord);
//                    for (Map.Entry<List<UnorderedWord>, Count> featureMapEntry : trainEntry.getValue().entrySet()) {
//                        featureMapEntry.getKey();
//
//                        for (UnorderedWord word : featureMapEntry.getKey()) {
//
//
//                        }
//                    }
                    }
                }
            }
        }
        return 0;
    }

    private double getNaiveBayesProbability(Map<List<UnorderedWord>, Count> innerMap, String ambiguousWord,
                                            double allLexeltsInTrainingSet, List<UnorderedWord> testFeatureList) {

        // number of occurrences of fj in a context of sense si in the training corpus,
        // C(fj, si)
        double numberOfOccurrenceBelongingToSenseId = getNumberOfOccurrenceBelongingToSenseId(innerMap, ambiguousWord);

        // the number of context words belonging to si in the training corpus
        // C(si)
        double numberOfContextWordsBelongingToSenseId = getNumberOfContextWordsWithSpecificSense(innerMap);

        // P(fj |w = si) = C(fj, si)/C(si)
        double stepOneProbability;

        stepOneProbability = (numberOfOccurrenceBelongingToSenseId + 1) / (numberOfContextWordsBelongingToSenseId + testFeatureList.size());

        // P(si) = C(si)/N
        double stepTwoProbability = numberOfContextWordsBelongingToSenseId / allLexeltsInTrainingSet;

        // P(fj |w = si) ∗ P(si)
        return stepOneProbability * stepTwoProbability;
    }

    private double getNumberOfOccurrenceBelongingToSenseId(Map<List<UnorderedWord>, Count> map, String word) {
        double numberOfOccurrence = 0.0;

        for (Map.Entry<List<UnorderedWord>, Count> item : map.entrySet()) {
            for (UnorderedWord unorderedWord : item.getKey()) {
                if (unorderedWord.getWord().equalsIgnoreCase(word)) {
                    numberOfOccurrence ++;
                }
            }
        }

        return numberOfOccurrence;
    }

    private double getNumberOfContextWordsWithSpecificSense(Map<List<UnorderedWord>, Count> map) {
        double contextWordCount = 0.0;

        for (Map.Entry<List<UnorderedWord>, Count> item : map.entrySet()) {
            contextWordCount = contextWordCount + item.getValue().getContextWordsCount();
        }

        return contextWordCount;
    }
}
