public class ViterbiNode {
    private String wordWithMaxProb;
    private String wrongWord;
    private double viterbiProbability;

    public ViterbiNode(String wordWithMaxProb, String wrongWord, double viterbiProbability) {
        this.wordWithMaxProb = wordWithMaxProb;
        this.wrongWord = wrongWord;
        this.viterbiProbability = viterbiProbability;
    }

    /**
     * Returns wordWithMaxProb
     *
     * @return wordWithMaxProb
     *
     */
    public String getWordWithMaxProb() {
        return wordWithMaxProb;
    }

    /**
     * Returns wrongWord
     *
     * @return wrongWord
     *
     */
    public String getWrongWord() {
        return wrongWord;
    }

    /**
     * Returns viterbiProbability
     *
     * @return viterbiProbability
     *
     */
    public double getViterbiProbability() {
        return viterbiProbability;
    }
}
