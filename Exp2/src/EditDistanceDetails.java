public class EditDistanceDetails {

    private String correctionWord;
    private String correctLetters;
    private String wrongLetters;
//    private char currentLetter;
//    private char previousLetter;
    private String typeOfOperation;
    private double emissionProbability;

    public EditDistanceDetails(String correctionWord, String correctLetters, String wrongLetters, String typeOfOperation, double emissionProbability) {
        this.correctionWord = correctionWord;
        this.correctLetters = correctLetters;
        this.wrongLetters = wrongLetters;
        this.typeOfOperation = typeOfOperation;
        this.emissionProbability = emissionProbability;
    }

    public EditDistanceDetails() {}

    public String getCorrectionWord() {
        return correctionWord;
    }

    public void setCorrectionWord(String correctionWord) {
        this.correctionWord = correctionWord;
    }

//    public char getCurrentLetter() {
//        return currentLetter;
//    }
//
//    public void setCurrentLetter(char currentLetter) {
//        this.currentLetter = currentLetter;
//    }
//
//    public char getPreviousLetter() {
//        return previousLetter;
//    }
//
//    public void setPreviousLetter(char previousLetter) {
//        this.previousLetter = previousLetter;
//    }

    public String getTypeOfOperation() {
        return typeOfOperation;
    }

    public void setTypeOfOperation(String typeOfOperation) {
        this.typeOfOperation = typeOfOperation;
    }

    public double getEmissionProbability() {
        return emissionProbability;
    }

    public void setEmissionProbability(double emissionProbability) {
        this.emissionProbability = emissionProbability;
    }

    public String getCorrectLetters() {
        return correctLetters;
    }

    public void setCorrectLetters(String correctLetters) {
        this.correctLetters = correctLetters;
    }

    public String getWrongLetters() {
        return wrongLetters;
    }

    public void setWrongLetters(String wrongLetters) {
        this.wrongLetters = wrongLetters;
    }
}
