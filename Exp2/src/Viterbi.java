import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Viterbi {

    private static Map<String, String> wrongWordsWithCorrectVersion = new HashMap<>();

    private List<String> extractErrorTagFromLine(String line, String regex) {
        List<String> errorTags = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            errorTags.add(matcher.group());
        }

        return errorTags;
    }

    private String getPlainWord(String word) {
        String punctuation = "!\"#$%&'()*+,-./:;<=>@[\\]^_`{|}~";

        if (word.length() == 0 ) {
            return "";
        }

        // if the last char of the word is a punctuation mark,
        // then simply delete it.
        if (word.length() > 1 && punctuation.indexOf(word.charAt(word.length() - 1)) != -1) {
            word = word.substring(0, word.length() - 1);
        }

        // if the first char of the word is a punctuation mark,
        // then simply delete it.
        if (word.length() > 1 && punctuation.indexOf(word.charAt(0)) != -1) {
            word = word.substring(1);
        }

        // if the word is a punctuation mark
        if (punctuation.indexOf(word.charAt(word.length() - 1)) != -1) {
            return "";
        }

        return word;
    }

    private List<String> getWrongViterbiWords(String line, String regex) {
        List<Integer> beginIndexList = new ArrayList<>();
        List<Integer> endIndexList = new ArrayList<>();
        List<String> words = new ArrayList<>();

        List<String> errorTags = extractErrorTagFromLine(line, regex);

//        System.out.println(errorTags);
        // if the sentence does not contain any wrong typed word,
        // return only its contents
        if (errorTags.size() == 0) {
            String[] tempWords = line.split("\\s+");
            for (String tempWord : tempWords) {
                tempWord = getPlainWord(tempWord);

                if (!tempWord.equals("") && !tempWord.equals(" "))
                    words.add(tempWord);
            }

            return words;
        }

        for (String errTag : errorTags) {
            Pattern pattern = Pattern.compile(errTag);
            Matcher matcher = pattern.matcher(line);

            while (matcher.find()) {
                if (!beginIndexList.contains(matcher.start()) && !endIndexList.contains(matcher.end() - 1)) {
                    beginIndexList.add(matcher.start());
                    endIndexList.add(matcher.end() - 1);
                }
            }
        }

        beginIndexList.sort(Comparator.naturalOrder());
        endIndexList.sort(Comparator.naturalOrder());

        int temp = 0;
        for (int i = 0; i < beginIndexList.size(); i++) {
            String tempWords[] = line.substring(temp, beginIndexList.get(i)).split("\\s+");
            for (String tempWord : tempWords) {
                if (!tempWord.equals("") && !tempWord.equals(" ")) {
                    tempWord = getPlainWord(tempWord);
                    words.add(tempWord);
                }
            }

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(line.substring(beginIndexList.get(i), endIndexList.get(i) + 1));

            String wrongWord = null;
            String correctWord = null;

            while (matcher.find()) {
                correctWord = matcher.group(2);
                wrongWord = matcher.group(5);
            }

            wrongWord = getPlainWord(wrongWord);
            correctWord = getPlainWord(correctWord);

            if (!wrongWord.equals("") && !wrongWord.equals(" ")) {
                words.add(wrongWord);
                wrongWordsWithCorrectVersion.put(wrongWord, correctWord);
            }

            temp = endIndexList.get(i) + 1;
        }

        // 1 ve 0 olarak algiliyordu size'i. neden oldugunu anlamadim
        if (endIndexList.size() != 0 && line.length() - 1 > endIndexList.get(endIndexList.size() - 1)) {
            String lastWordOfTheLine = line.substring(endIndexList.get(endIndexList.size() - 1) + 2, line.length());
            lastWordOfTheLine = getPlainWord(lastWordOfTheLine);
            if (!lastWordOfTheLine.equals("") && !lastWordOfTheLine.equals(" "))
                words.add(lastWordOfTheLine);
        }

        return words;
    }

    private static List<ViterbiNode> viterbiNodeList = new ArrayList<>();

    private ViterbiNode getMaxViterbiNode(List<String> candidates, String wrongWord, boolean isInitial) {
        EditDistanceCalculator editDistanceCalculator = new EditDistanceCalculator();
        Preprocessing preprocessing = new Preprocessing();

        double maxProbability = 0.0;
        String maxProbWord = "";

        for (String candidate : candidates) {
            // this method call is used to get the correct and wrong letters
            int minEditDistance = editDistanceCalculator.getMinEditDistance(candidate, wrongWord, candidate.length(), wrongWord.length());

            double currentStateProbability;

            if (minEditDistance == 1) {
                String correctLetters = editDistanceCalculator.getCorrectLetters();
                String wrongLetters = editDistanceCalculator.getWrongLetters();

                double emissionProbability = editDistanceCalculator.getEmissionProbability(correctLetters, wrongLetters);
                emissionProbability = Math.log(emissionProbability) / Math.log(2);
                double transitionProbability;

                if (isInitial) {
                    transitionProbability = editDistanceCalculator.getTransitionProbability("<s>", candidate);

                    transitionProbability = Math.log(transitionProbability) / Math.log(2);

                    currentStateProbability = transitionProbability + emissionProbability;
                    currentStateProbability = Math.pow(2, currentStateProbability);
                } else {
                    transitionProbability = editDistanceCalculator.getTransitionProbability(viterbiNodeList.get(viterbiNodeList.size() - 1).getWordWithMaxProb(), candidate);

                    transitionProbability = Math.log(transitionProbability) / Math.log(2);

                    currentStateProbability = transitionProbability + emissionProbability;

                    currentStateProbability = currentStateProbability + Math.log(viterbiNodeList.get(viterbiNodeList.size() - 1).getViterbiProbability()) / Math.log(2);
                    currentStateProbability = Math.pow(2, currentStateProbability);
                }
            } else {
                currentStateProbability = Double.MIN_VALUE;
            }

            if (currentStateProbability > maxProbability) {
                maxProbability = currentStateProbability;
                maxProbWord = candidate;
            }
        }

        return new ViterbiNode(maxProbWord, wrongWord, maxProbability);
    }

    public void implementViterbi(String outputFile) throws IOException {
        FWriter fileWriter = new FWriter();
        Preprocessing preprocessing = new Preprocessing();
        EditDistanceCalculator editDistanceCalculator = new EditDistanceCalculator();

        fileWriter.openFile(outputFile);
        List<String> wrongLines = preprocessing.getWrongLines();
        String regex = preprocessing.getRegex();

        Map<String, List<String>> wrongAndCorrectWordForms = preprocessing.getWrongAndCorrectWordForms();
        double correctGuessCount = 0.0;

        for (String line : wrongLines) {
            List<String> wrongViterbiWords = getWrongViterbiWords(line, regex);

            // Ignore correct typed sentences
            if (!Pattern.compile(regex).matcher(line).find())
                continue;

            if (wrongViterbiWords != null && wrongViterbiWords.size() > 0) {
                List<String> candidates = null;

                String firstWord = wrongViterbiWords.get(0);

                if (wrongWordsWithCorrectVersion.containsKey(firstWord)) {
                    candidates = wrongAndCorrectWordForms.get(wrongViterbiWords.get(0));
                }

                //////////////////////////////////////// INITIAL PROBABILITIES ////////////////////////////////////

                double initialProbability = editDistanceCalculator.getTransitionProbability("<s>", firstWord);

                // if the word is typed correct
                if (candidates == null) {
//                    initialProbabilities.add(initialProbability);
//                    path.add(0);
                    viterbiNodeList.add(new ViterbiNode(firstWord, null, initialProbability));
                }
                // if the word is typed wrong
                // calculate the emission probabilities of each candidate
                else {
                    ViterbiNode viterbiNode = getMaxViterbiNode(candidates, firstWord, true);
                    viterbiNodeList.add(viterbiNode);
//                initialProbabilities.add(maxProbability);
                }
                //////////////////////////////////////// INITIAL PROBABILITIES ////////////////////////////////////

                for (int i = 1; i < wrongViterbiWords.size(); i++) {
                    ViterbiNode maxViterbiNode = viterbiNodeList.get(i - 1);
                    String currentViterbiWord = wrongViterbiWords.get(i);

                    candidates = null;

                    if (wrongWordsWithCorrectVersion.containsKey(wrongViterbiWords.get(i))) {
                        candidates = wrongAndCorrectWordForms.get(wrongViterbiWords.get(i));
                    }

//                    candidates = wrongAndCorrectWordForms.get(viterbiWords.get(i));

                    // if the word is typed correct
                    // calculate only transition probability
                    if (candidates == null) {
                        double transitionProbability = editDistanceCalculator.getTransitionProbability(maxViterbiNode.getWordWithMaxProb(), currentViterbiWord);
                        viterbiNodeList.add(new ViterbiNode(currentViterbiWord, null, transitionProbability));
                    }
                    // if the word is typed wrong
                    // calculate emission and transition probabilities of each candidate
                    else {
                        ViterbiNode viterbiNode = getMaxViterbiNode(candidates, wrongViterbiWords.get(i), false);
                        viterbiNodeList.add(viterbiNode);
                    }
                }

                for (ViterbiNode viterbiNode : viterbiNodeList) {
                    if (viterbiNode.getWrongWord() != null) {
                        String correctVersion = wrongWordsWithCorrectVersion.get(viterbiNode.getWrongWord());
                        if (correctVersion.equals(viterbiNode.getWordWithMaxProb())) {
                            correctGuessCount++;
                        }
                    }
//                    System.out.print(tNode.wordWithMaxProb + " ");
                }

                fileWriter.write(String.format("---------------------------   WRONG SENTENCE    ---------------------------------%n"));
                StringJoiner stringJoiner = new StringJoiner(" ");

                for (String viterbiWord : wrongViterbiWords) {
                    stringJoiner.add(viterbiWord);
                }

                fileWriter.write(String.format("%s%n%n", stringJoiner.toString()));


                fileWriter.write(String.format("--------------------------   VITERBI SENTENCE    --------------------------------%n"));
                stringJoiner = new StringJoiner(" ");

                for (ViterbiNode correctViterbiWord : viterbiNodeList) {
                    stringJoiner.add(correctViterbiWord.getWordWithMaxProb());
                }

                fileWriter.write(String.format("%s%n%n%n", stringJoiner.toString()));
                fileWriter.write(String.format("---------------------------------------------------------------------------------%n%n%n"));

            }

            wrongWordsWithCorrectVersion.clear();
            viterbiNodeList.clear();
        }

        fileWriter.write(String.format("Accuracy is: %s percent.%n", (100.0 * correctGuessCount) / preprocessing.getTotalWrongWordCount()));
        fileWriter.closeFile();

        System.out.println("Viterbi algorithm is implemented.");

        System.out.printf("%nAccuracy is: %s percent.%n", (100.0 * correctGuessCount) / preprocessing.getTotalWrongWordCount());
        System.out.printf("See the output file for corrected sentences.%n%n");
    }
}