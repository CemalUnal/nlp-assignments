public class UnorderedWord {

    private String word;
    private String tag;
    private int position;

    public UnorderedWord(String word, String tag, int position) {
        this.word = word;
        this.tag = tag;
        this.position = position;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
