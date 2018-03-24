import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProbabilityChart {

    /* Every probability defined between a head and a tail. */
    /* I created a nested class to keep both head and tail in the same object instance. */
    private class HeadAndTail {
        private double head;
        private double tail;

        HeadAndTail(double head, double tail) {
            this.head = head;
            this.tail = tail;
        }

        public double getHead() {
            return head;
        }

        public double getTail() {
            return tail;
        }
    }

    private Map<String, HeadAndTail> chart = new HashMap<>();

    private double previousProbability = 0.0;

    /**
     * Adds a probability to probability chart(table).
     *
     * @param word a sentence to calculate probability
     * @param probability a sentence to calculate probability
     */
    public void addToChart(String word, double probability) {
        double headIndex = previousProbability;
        double tailIndex = headIndex + probability;
        previousProbability = tailIndex;

        chart.put(word, new HeadAndTail(headIndex, tailIndex));
    }

    /**
     * It checks the ProbabilityChart and gets appropriate word
     * for the random probability.
     *
     * @param probability a random probability
     * @return a word with random probability
     */
    public String getWord(double probability) {
        String randomWord;
        for (Map.Entry<String, HeadAndTail> entry : chart.entrySet()) {

            int comparisonResult = Double.compare(entry.getValue().getTail(), probability);
            if (comparisonResult > 0 || comparisonResult == 0) {
                randomWord = entry.getKey();
                return randomWord;
            }
        }
        return null;
    }

    public void print() {
        for (Map.Entry<String, HeadAndTail> entry : chart.entrySet()) {
            System.out.println(entry.getKey() + " - Head: " + entry.getValue().getHead() + ", Tail: " + entry.getValue().getTail());
        }
    }
}
