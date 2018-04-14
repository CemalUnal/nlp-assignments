public class AmbiguousWord {

    private String wordId;
    private int senseId;
    private String word;

    public AmbiguousWord(String wordId, int senseId, String word) {
        this.wordId = wordId;
        this.senseId = senseId;
        this.word = word;
    }

    public String getWordId() {
        return wordId;
    }

    public void setWordId(String wordId) {
        this.wordId = wordId;
    }

    public int getSenseId() {
        return senseId;
    }

    public void setSenseId(int senseId) {
        this.senseId = senseId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
