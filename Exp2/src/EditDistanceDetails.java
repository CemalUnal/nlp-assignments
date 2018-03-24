public class EditDistanceDetails {

    private String correctionWord;
    private char correctLetter;
    private char errorLetter;
    private String typeOfOperation;

    public EditDistanceDetails(String correctionWord, char correctLetter, char errorLetter, String typeOfOperation) {
        this.correctionWord = correctionWord;
        this.correctLetter = correctLetter;
        this.errorLetter = errorLetter;
        this.typeOfOperation = typeOfOperation;
    }

    public EditDistanceDetails() {}

    public String getCorrectionWord() {
        return correctionWord;
    }

    public void setCorrectionWord(String correctionWord) {
        this.correctionWord = correctionWord;
    }

    public char getCorrectLetter() {
        return correctLetter;
    }

    public void setCorrectLetters(char correctLetter) {
        this.correctLetter = correctLetter;
    }

    public char getErrorLetter() {
        return errorLetter;
    }

    public void setErrorLetters(char errorLetter) {
        this.errorLetter = errorLetter;
    }

    public String getTypeOfOperation() {
        return typeOfOperation;
    }

    public void setTypeOfOperation(String typeOfOperation) {
        this.typeOfOperation = typeOfOperation;
    }
}
