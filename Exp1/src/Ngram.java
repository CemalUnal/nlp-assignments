import java.util.*;
import java.util.regex.Pattern;

public class Ngram {

    /**
     * Adds given word to given map. And it handles frequencies inside itself.
     *
     * @param map a map to add an element to it
     * @param word a word to add to map
     */
    public void addWordToMap(Map<String, Double> map,  String word) {
        if (map.containsKey(word)) {
            double currentFrequency = map.get(word);
            currentFrequency = currentFrequency + 1.0;
            map.put(word, currentFrequency);
        } else {
            map.put(word, 1.0);
        }
    }

    /**
     * Handles the logic for the stopping criteria for the e-mails.
     * It creates a random probability. With this random probability,
     * it checks the ProbabilityChart and gets appropriate word.
     *
     * @param probabilityChart probability chart(table) that contains all the
     *                         word probabilities for the related language model
     * @param endMarks list of all sentence ending punctuation marks
     * @return generated email using related probability chart
     */
    public String generateEmail(ProbabilityChart probabilityChart, List<String> endMarks) {
        Random random = new Random();
        StringJoiner joiner = new StringJoiner(" ");
        String[] tempArray = new String[0];

        while (tempArray.length < 30) { // to stop when the length of the sentence reaches 30
            double randomProbability = random.nextDouble();
            String word = probabilityChart.getWord(randomProbability);
            if (word != null) {
                String[] tempArray2 = word.split(" ");
                for (String singleToken : tempArray2) {
                    if (!(singleToken.equals("<s>") || singleToken.equals("</s>"))) {
                        joiner.add(singleToken);
                    }
                    for (String endMark : endMarks) {
                        if (Pattern.compile(endMark).matcher(singleToken).find()) {
                            return joiner.toString();
                        }
                    }
                    tempArray = joiner.toString().split(" ");
                }
            }
        }

        return joiner.toString();
    }

    /**
     * Calculates the smoothed bigram or smoothed trigram perplexity of the sentence.
     *
     * @param sentence  a sentence to calculate perplexity
     * @param logProbability log probability of the sentence.
     * @return smoothed bigram or smoothed trigram perplexity of the sentence
     */
    public double calculatePerplexity(String sentence, double logProbability) {
        String[] words = sentence.split(" ");

        double power = logProbability / words.length;

        return Math.pow(2, 0.0 - power);
    }
}