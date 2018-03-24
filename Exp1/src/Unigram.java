import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Unigram extends Ngram {

    /* Holds the Unigram counts. Unigram Language Model */
    private static Map<String, Double> unigramCountsMap = new HashMap<>();

    private static double totalCount = 1.0;
    private static double unsmoothedUnigramLogProbability = 0.0;
    private static double smoothedUnigramLogProbability = 0.0;

    /**
     * Creates the unigram model.
     * Adds all unigrams in the training set to a hashmap by calling
     * the addWordToMap method of its parent class.
     *
     * @param trainingSet training set
     */
    public void createUnigramModel(List<String> trainingSet) {
        for (String sentence : trainingSet) {
            String[] words = sentence.split("\\s+");
            for (String word : words) {
                addWordToMap(unigramCountsMap, word);
                totalCount++;
            }
        }
    }

    /**
     * Calculates the smoothed unigram probability of the sentence.
     *
     * @param sentence a sentence to calculate probability
     * @return smoothed unigram probability of the sentence
     */
    public double getProbabilityWithSmoothedUnigram(String sentence) {
        double probability = 1.0;
        String[] words = sentence.split(" ");

        for (String word : words) {
            if (unigramCountsMap.containsKey(word)) {
                double tempProbability = getProbabilityWithUnsmoothedUnigram(word);
                probability = probability * tempProbability;
                smoothedUnigramLogProbability = smoothedUnigramLogProbability + tempProbability;
            } else {
                double tempProbability = 1.0 / (totalCount + unigramCountsMap.size());
                probability = probability * tempProbability;
                smoothedUnigramLogProbability = smoothedUnigramLogProbability + (Math.log(tempProbability) / Math.log(2));
            }
        }

        return probability;
    }

    /**
     * Calculates the unsmoothed unigram probability of the sentence.
     *
     * @param sentence a sentence to calculate probability
     * @return unsmoothed unigram probability of the sentence
     */
    public double getProbabilityWithUnsmoothedUnigram(String sentence) {
        double probability = 1.0;
        String[] words = sentence.split(" ");

        for (String word : words) {
            double tempProbability = unigramCountsMap.get(word) / totalCount;
            probability = probability * tempProbability;
            unsmoothedUnigramLogProbability = unsmoothedUnigramLogProbability + Math.log(tempProbability) / Math.log(2);
        }
        return probability;
    }

    /**
     * Creates a probability chart(table) that contains all the word
     * probabilities of unsmoothed unigram model. Than it calls the
     * generateEmail method of its parent class with this probability chart.
     *
     * @param endMarks list of all sentence ending punctuation marks
     * @return generated email using unsmoothed unigram
     */
    public String generateUnsmoothedUnigramEmails(List<String> endMarks) {
        ProbabilityChart unsmoothedUnigramChart = new ProbabilityChart();
        double sumOfProbabilities = 0.0;
        for (Map.Entry<String, Double> entry : unigramCountsMap.entrySet()) {
            sumOfProbabilities = sumOfProbabilities + getProbabilityWithUnsmoothedUnigram(entry.getKey());
        }

        for (Map.Entry<String, Double> entry : unigramCountsMap.entrySet()) {
            double currentProbability = getProbabilityWithUnsmoothedUnigram(entry.getKey());
            currentProbability = currentProbability / sumOfProbabilities;
            unsmoothedUnigramChart.addToChart(entry.getKey(), currentProbability);
        }

        return generateEmail(unsmoothedUnigramChart, endMarks);
    }

    /**
     * Creates a probability chart(table) that contains all the word
     * probabilities of smoothed unigram model. Than it calls the
     * generateEmail method of the parent class with this probability chart.
     *
     * @param endMarks list of all sentence ending punctuation marks
     * @return generated email using smoothed unigram
     */
    public String generateSmoothedUnigramEmails(List<String> endMarks) {
        ProbabilityChart smoothedUnigramChart = new ProbabilityChart();
        double sumOfProbabilities = 0.0;
        for (Map.Entry<String, Double> entry : unigramCountsMap.entrySet()) {
            sumOfProbabilities = sumOfProbabilities + getProbabilityWithSmoothedUnigram(entry.getKey());
        }

        for (Map.Entry<String, Double> entry : unigramCountsMap.entrySet()) {
            double currentProbability = getProbabilityWithSmoothedUnigram(entry.getKey());
            currentProbability = currentProbability / sumOfProbabilities;
            smoothedUnigramChart.addToChart(entry.getKey(), currentProbability);
        }

        return generateEmail(smoothedUnigramChart, endMarks);
    }

    /**
     * Returns the unigram language model.
     *
     * @return unigramCountsMap
     */
    public static Map<String, Double> getUnigramCountsMap() {
        return unigramCountsMap;
    }

}
