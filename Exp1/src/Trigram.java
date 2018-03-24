import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trigram extends Ngram {

    /* Holds the Trigram counts. Trigram Language Model */
    private static Map<String, Double> trigramCountsMap = new HashMap<>();

    private static double unsmoothedTrigramLogProbability = 0.0;
    private static double smoothedTrigramLogProbability = 0.0;

    /**
     * Creates the trigram model.
     * Adds all trigrams in the training set to a hashmap by calling
     * the addWordToMap method of its parent class.
     *
     * @param trainingSet training set
     */
    public void createTrigramModel(List<String> trainingSet) {
        for (String sentence : trainingSet) {
            String[] words = sentence.split("\\s+");

            String trigramToken = words[0] + " " + words[0] + " " + words[1];
            addWordToMap(trigramCountsMap, trigramToken);

            for (int i = 2; i < words.length; i++) {
                trigramToken = words[i - 2] + " " + words[i - 1] + " " + words[i];
                addWordToMap(trigramCountsMap, trigramToken);
            }
        }
    }

    /**
     * Calculates the smoothed trigram probability of the sentence.
     *
     * @param sentence a sentence to calculate probability
     * @return smoothed trigram probability of the sentence
     */
    public double getProbabilityWithSmoothedTrigram(String sentence) {
        double probability = 1.0;
        Map<String, Double> bigramCountsMap = Bigram.getBigramCountsMap();

        String[] words = sentence.split("\\s+");

        for (int i = 2; i < words.length; i++) {
            String trigramToken = words[i - 2] + " " + words[i - 1] + " " + words[i];
            String bigramToken = words[i - 2] + " " + words[i - 1];

            if (trigramCountsMap.containsKey(trigramToken) && bigramCountsMap.containsKey(bigramToken)) {
                double tempProbability = getProbabilityWithUnsmoothedTrigram(trigramToken);
                probability = probability * tempProbability;
                smoothedTrigramLogProbability = smoothedTrigramLogProbability + tempProbability;
            } else if (!trigramCountsMap.containsKey(trigramToken) && bigramCountsMap.containsKey(bigramToken)) {
                double tempProbability = 1.0 / (bigramCountsMap.get(bigramToken) + bigramCountsMap.size());
                probability = probability * tempProbability;
                smoothedTrigramLogProbability = smoothedTrigramLogProbability + Math.log(tempProbability) / Math.log(2);
            }

        }

        return probability;
    }

    /**
     * Calculates the unsmoothed trigram probability of the sentence.
     *
     * @param sentence a sentence to calculate probability
     * @return unsmoothed trigram probability of the sentence
     */
    public double getProbabilityWithUnsmoothedTrigram(String sentence) {
        double probability = 1.0;
        Map<String, Double> bigramCountsMap = Bigram.getBigramCountsMap();

        String[] words = sentence.split("\\s+");

        for (int i = 2; i < words.length; i++) {
            String trigramToken = words[i - 2] + " " + words[i - 1] + " " + words[i];
            String bigramToken = words[i - 2] + " " + words[i - 1];

            if (trigramCountsMap.containsKey(trigramToken) && bigramCountsMap.containsKey(bigramToken)) {
                double tempProbability = trigramCountsMap.get(trigramToken) / bigramCountsMap.get(bigramToken);
                probability = probability * tempProbability;
                unsmoothedTrigramLogProbability = unsmoothedTrigramLogProbability + Math.log(tempProbability) / Math.log(2);
            }
        }

        return probability;
    }

    /**
     * Creates a probability chart(table) that contains all the word
     * probabilities of unsmoothed trigram model. Than it calls the
     * generateEmail method of its parent class with this probability chart.
     *
     * @param endMarks list of all sentence ending punctuation marks
     * @return generated email using unsmoothed trigram
     */
    public String generateUnsmoothedTrigramEmails(List<String> endMarks) {
        ProbabilityChart unsmoothedTrigramChart = new ProbabilityChart();
        double sumOfProbabilities = 0.0;
        for (Map.Entry<String, Double> entry : trigramCountsMap.entrySet()) {
            sumOfProbabilities = sumOfProbabilities + getProbabilityWithUnsmoothedTrigram(entry.getKey());
        }

        for (Map.Entry<String, Double> entry : trigramCountsMap.entrySet()) {
            double currentProbability = getProbabilityWithUnsmoothedTrigram(entry.getKey());
            currentProbability = currentProbability / sumOfProbabilities;
            unsmoothedTrigramChart.addToChart(entry.getKey(), currentProbability);
        }

        return generateEmail(unsmoothedTrigramChart, endMarks);
    }

    /**
     * Creates a probability chart(table) that contains all the word
     * probabilities of smoothed trigram model. Than it calls the
     * generateEmail method of its parent class with this probability chart.
     *
     * @param endMarks list of all sentence ending punctuation marks
     * @return generated email using smoothed trigram
     */
    public String generateSmoothedTrigramEmails(List<String> endMarks) {
        ProbabilityChart smoothedTrigramChart = new ProbabilityChart();
        double sumOfProbabilities = 0.0;
        for (Map.Entry<String, Double> entry : trigramCountsMap.entrySet()) {
            sumOfProbabilities = sumOfProbabilities + getProbabilityWithSmoothedTrigram(entry.getKey());
        }

        for (Map.Entry<String, Double> entry : trigramCountsMap.entrySet()) {
            double currentProbability = getProbabilityWithSmoothedTrigram(entry.getKey());
            currentProbability = currentProbability / sumOfProbabilities;
            smoothedTrigramChart.addToChart(entry.getKey(), currentProbability);
        }

        return generateEmail(smoothedTrigramChart, endMarks);
    }

    /**
     * Calculates the smoothed trigram perplexity of the sentence by calling
     * the calculatePerplexity method of its parent class.
     *
     * @param sentence  a sentence to calculate perplexity
     * @return smoothed trigram perplexity of the sentence
     */
    public double calculatePerplexity(String sentence) {
        return calculatePerplexity(sentence, smoothedTrigramLogProbability);
    }
}
