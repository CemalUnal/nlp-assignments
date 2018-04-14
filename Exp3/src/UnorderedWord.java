public class UnorderedWord {

    private String word;
    private String tag;
    private int position;

    public UnorderedWord(String word, String tag, int position) {
        this.word = word;
        this.tag = tag;
        this.position = position;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (!(obj instanceof UnorderedWord)) return false;

        if (obj == this) return true;

        return (this.getWord().equals(((UnorderedWord) obj).getWord()));
    }

    @Override
    public int hashCode() {
        return (word != null) ? (word.hashCode()) : 0;
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
