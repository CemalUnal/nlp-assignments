import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bigram extends Ngram {

    /* Holds the Bigram counts. Bigram Language Model */
    private static Map<String, Double> bigramCountsMap = new HashMap<>();

    private static double unsmoothedBigramLogProbability = 0.0;
    private static double smoothedBigramLogProbability = 0.0;

    /**
     * Creates the bigram model.
     * Adds all bigrams in the training set to a hashmap by calling
     * the addWordToMap method of its parent class.
     *
     * @param trainingSet training set
     */
    public void createBigramModel(List<String> trainingSet) {
        for (String sentence : trainingSet) {
            String[] words = sentence.split("\\s+");
            for (int i = 1; i < words.length; i++) {
                String bigramToken = words[i - 1] + " " + words[i];
                addWordToMap(bigramCountsMap, bigramToken);
            }
        }
    }

    /**
     * Calculates the smoothed bigram probability of the sentence.
     *
     * @param sentence a sentence to calculate probability
     * @return smoothed bigram probability of the sentence
     */
    public double getProbabilityWithSmoothedBigram(String sentence) {
        double probability = 1.0;

        Map<String, Double> unigramCountsMap = Unigram.getUnigramCountsMap();

        String[] words = sentence.split("\\s+");
        for (int i = 1; i < words.length; i++) {
            String unigramToken = words[i - 1];
            String bigramToken = unigramToken + " " + words[i];

            if (bigramCountsMap.containsKey(bigramToken)) {
                double tempProbability = getProbabilityWithUnsmoothedBigram(bigramToken);
                probability = probability * tempProbability;
                smoothedBigramLogProbability = smoothedBigramLogProbability + tempProbability;
            } else {
                double tempProbability = 1.0;
                if (bigramCountsMap.containsKey(bigramToken) && unigramCountsMap.containsKey(unigramToken))
                    tempProbability = (bigramCountsMap.get(bigramToken) + 1) / (unigramCountsMap.get(unigramToken) + unigramCountsMap.size());
                probability = probability * tempProbability;
                smoothedBigramLogProbability = smoothedBigramLogProbability + Math.log(tempProbability) / Math.log(2);
            }
        }

        return probability;
    }

    /**
     * Calculates the unsmoothed bigram probability of the sentence.
     *
     * @param sentence a sentence to calculate probability
     * @return unsmoothed bigram probability of the sentence
     */
    public double getProbabilityWithUnsmoothedBigram(String sentence) {
        double probability = 1.0;

        Map<String, Double> unigramCountsMap = Unigram.getUnigramCountsMap();

        String[] words = sentence.split("\\s+");
        for (int i = 1; i < words.length; i++) {
            String unigramToken = words[i - 1];
            String bigramToken = unigramToken + " " + words[i];
            double tempProbability = 1.0;

            if (bigramCountsMap.containsKey(bigramToken) && unigramCountsMap.containsKey(unigramToken))
                tempProbability = bigramCountsMap.get(bigramToken) / unigramCountsMap.get(unigramToken);
            probability = probability * tempProbability;
            unsmoothedBigramLogProbability = unsmoothedBigramLogProbability + Math.log(tempProbability) / Math.log(2);
        }

        return probability;
    }

    /**
     * Creates a probability chart(table) that contains all the word
     * probabilities of unsmoothed bigram model. Than it calls the
     * generateEmail method of its parent class with this probability chart.
     *
     * @param endMarks list of all sentence ending punctuation marks
     * @return generated email using unsmoothed bigram
     */
    public String generateUnsmoothedBigramEmails(List<String> endMarks) {
        ProbabilityChart unsmoothedBigramChart = new ProbabilityChart();
        double sumOfProbabilities = 0.0;
        for (Map.Entry<String, Double> entry : bigramCountsMap.entrySet()) {
            sumOfProbabilities = sumOfProbabilities + getProbabilityWithUnsmoothedBigram(entry.getKey());
        }

        for (Map.Entry<String, Double> entry : bigramCountsMap.entrySet()) {
            double currentProbability = getProbabilityWithUnsmoothedBigram(entry.getKey());

            currentProbability = currentProbability / sumOfProbabilities;
            unsmoothedBigramChart.addToChart(entry.getKey(), currentProbability);
        }

        return generateEmail(unsmoothedBigramChart, endMarks);
    }

    /**
     * Creates a probability chart(table) that contains all the word
     * probabilities of smoothed bigram model. Than it calls the
     * generateEmail method of the parent class with this probability chart.
     *
     * @param endMarks list of all sentence ending punctuation marks
     * @return generated email using smoothed bigram
     */
    public String generateSmoothedBigramEmails(List<String> endMarks) {
        ProbabilityChart smoothedBigramChart = new ProbabilityChart();
        double sumOfProbabilities = 0.0;
        for (Map.Entry<String, Double> entry : bigramCountsMap.entrySet()) {
            sumOfProbabilities = sumOfProbabilities + getProbabilityWithSmoothedBigram(entry.getKey());
        }

        for (Map.Entry<String, Double> entry : bigramCountsMap.entrySet()) {
            double currentProbability = getProbabilityWithSmoothedBigram(entry.getKey());
            currentProbability = currentProbability / sumOfProbabilities;
            smoothedBigramChart.addToChart(entry.getKey(), currentProbability);
        }

        return generateEmail(smoothedBigramChart, endMarks);
    }

    /**
     * Returns the smoothed bigram perplexity of the sentence by calling
     * the calculatePerplexity method of its parent class.
     *
     * @param sentence a sentence to calculate perplexity
     * @return smoothed bigram perplexity of the sentence
     */
    public double calculatePerplexity(String sentence) {
        return calculatePerplexity(sentence, smoothedBigramLogProbability);
    }

    /**
     * Returns the bigram language model.
     *
     * @return bigramCountsMap
     */
    public static Map<String, Double> getBigramCountsMap() {
        return bigramCountsMap;
    }
}
