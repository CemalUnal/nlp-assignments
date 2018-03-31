public class EditDistanceDetails {

    private String correctWord;
    private String wrongWord;
    private String correctLetters;
    private String wrongLetters;
//    private char currentLetter;
//    private char previousLetter;
//    private String typeOfOperation;
//    private double emissionProbability;

    public EditDistanceDetails(String correctWord, String correctLetters, String wrongWord, String wrongLetters) {
        this.correctWord = correctWord;
        this.correctLetters = correctLetters;
        this.wrongWord = wrongWord;
        this.wrongLetters = wrongLetters;
//        this.typeOfOperation = typeOfOperation;
//        this.emissionProbability = emissionProbability;
    }

    public EditDistanceDetails() {}

    public String getCorrectWord() {
        return correctWord;
    }

    public void setCorrectWord(String correctWord) {
        this.correctWord = correctWord;
    }

    public String getCorrectLetters() {
        return correctLetters;
    }

    public void setCorrectLetters(String correctLetters) {
        this.correctLetters = correctLetters;
    }

    public String getWrongWord() {
        return wrongWord;
    }

    public void setWrongWord(String wrongWord) {
        this.wrongWord = wrongWord;
    }

    public String getWrongLetters() {
        return wrongLetters;
    }

    public void setWrongLetters(String wrongLetters) {
        this.wrongLetters = wrongLetters;
    }
}
