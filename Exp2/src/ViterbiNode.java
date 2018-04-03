public class ViterbiNode {
    //        private List<Integer> viterbiPath;
    private String wordWithMaxProb;
    private String wrongWord;
    private double viterbiProbability;

    public ViterbiNode(String wordWithMaxProb, String wrongWord, double viterbiProbability) {
//        this.viterbiPath = new ArrayList<>(viterbiPath);
        this.wordWithMaxProb = wordWithMaxProb;
        this.wrongWord = wrongWord;
        this.viterbiProbability = viterbiProbability;
    }

    public String getWordWithMaxProb() {
        return wordWithMaxProb;
    }

    public String getWrongWord() {
        return wrongWord;
    }

    public double getViterbiProbability() {
        return viterbiProbability;
    }
}
