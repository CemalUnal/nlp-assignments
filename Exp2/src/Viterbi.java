import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Viterbi {

//    private static List<String> states = Arrays.asList("#", "NN", "VB");
//    private static List<String> observations = Arrays.asList("I", "go", "some times");
//    private static List<String> words = new ArrayList<>();
//    private static List<Double> startProbabilities = Arrays.asList( 0.3, 0.4, 0.3 );
//    private static double[][] transition_probability = { { 0.2, 0.2, 0.6 }, { 0.4, 0.1, 0.5 }, { 0.1, 0.8, 0.1 } };
//    private static double[][] emission_probability = { { 0.01, 0.02, 0.02 }, { 0.8, 0.01, 0.5 }, { 0.19, 0.97, 0.48 } };

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

    public String getPlainWord(String word) {
        String punctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

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

    private static class ViterbiNode {
        //        private List<Integer> viterbiPath;
        private String wordWithMaxProb;
        private String wrongWord;
        private double viterbiProbability;

        //        public TNode( List<Integer> viterbiPath, String wordWithMaxProb, double viterbiProbability) {
        ViterbiNode(String wordWithMaxProb, String wrongWord, double viterbiProbability) {
//            this.viterbiPath = new ArrayList<>(viterbiPath);
            this.wordWithMaxProb = wordWithMaxProb;
            this.wrongWord = wrongWord;
            this.viterbiProbability = viterbiProbability;
        }
    }

    private static List<ViterbiNode> viterbiNodeList = new ArrayList<>();

    private ViterbiNode getMaxViterbiNode(List<String> candidates, String wrongWord, boolean isInitial) {
        EditDistanceCalculator editDistanceCalculator = new EditDistanceCalculator();
        Preprocessing preprocessing = new Preprocessing();

        double maxProbability = 0.0;
        String maxProbWord = "";

        for (int i = 0; i < candidates.size(); i++) {
            // this method call is used to get the correct and wrong letters
            int minEditDistance = editDistanceCalculator.getMinEditDistance(candidates.get(i), wrongWord, candidates.get(i).length(), wrongWord.length());

            double currentStateProbability;

            if (minEditDistance == 1) {
                String correctLetters = editDistanceCalculator.getCorrectLetters();
                String wrongLetters = editDistanceCalculator.getWrongLetters();

                double emissionProbability = preprocessing.getEmissionProbability(correctLetters, wrongLetters);
                emissionProbability = Math.log(emissionProbability) / Math.log(2);
                double transitionProbability;

                if (isInitial) {
                    transitionProbability = preprocessing.getTransitionProbability("<s>", candidates.get(i));

                    transitionProbability = Math.log(transitionProbability) / Math.log(2);

                    currentStateProbability = transitionProbability + emissionProbability;
                    currentStateProbability = Math.pow(2, currentStateProbability);
                } else {
                    transitionProbability = preprocessing.getTransitionProbability(viterbiNodeList.get(viterbiNodeList.size() - 1).wordWithMaxProb, candidates.get(i));

                    transitionProbability = Math.log(transitionProbability) / Math.log(2);

                    currentStateProbability = transitionProbability + emissionProbability;

                    currentStateProbability = currentStateProbability + Math.log(viterbiNodeList.get(viterbiNodeList.size() - 1).viterbiProbability) / Math.log(2);
                    currentStateProbability = Math.pow(2, currentStateProbability);
                }
            } else {
                currentStateProbability = Double.MIN_VALUE;
            }

            if (currentStateProbability > maxProbability) {
                maxProbability = currentStateProbability;
                maxProbWord = candidates.get(i);
            }
        }

        return new ViterbiNode(maxProbWord, wrongWord, maxProbability);
    }

    public void implementViterbi(String outputFile) throws IOException {
        FWriter fileWriter = new FWriter();
        Preprocessing preprocessing = new Preprocessing();

        fileWriter.openFile(outputFile);
        List<String> wrongLines = preprocessing.getWrongLines();
        String regex = preprocessing.getRegex();

        Map<String, List<String>> wrongAndCorrectWordForms = preprocessing.getWrongAndCorrectWordForms();
        double correctGuessCount = 0.0;

        for (String line : wrongLines) {
            List<String> wrongViterbiWords = getWrongViterbiWords(line, regex);

            if (wrongViterbiWords != null && wrongViterbiWords.size() > 0) {
                List<String> candidates = null;

                String firstWord = wrongViterbiWords.get(0);

                if (wrongWordsWithCorrectVersion.containsKey(firstWord)) {
                    candidates = wrongAndCorrectWordForms.get(wrongViterbiWords.get(0));
                }

                //////////////////////////////////////// INITIAL PROBABILITIES ////////////////////////////////////

                double initialProbability = preprocessing.getTransitionProbability("<s>", firstWord);

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
                        double transitionProbability = preprocessing.getTransitionProbability(maxViterbiNode.wordWithMaxProb, currentViterbiWord);
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
                    if (viterbiNode.wrongWord != null) {
                        String correctVersion = wrongWordsWithCorrectVersion.get(viterbiNode.wrongWord);
                        if (correctVersion.equals(viterbiNode.wordWithMaxProb)) {
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
                    stringJoiner.add(correctViterbiWord.wordWithMaxProb);
                }

                fileWriter.write(String.format("%s%n%n%n", stringJoiner.toString()));
                fileWriter.write(String.format("---------------------------------------------------------------------------------%n%n%n"));

            }

            wrongWordsWithCorrectVersion.clear();
            viterbiNodeList.clear();
        }

        fileWriter.write(String.format("Accuracy is: %s percent.%n", (100.0 * correctGuessCount) / preprocessing.getTotalWrongWordCount()));
        fileWriter.closeFile();
    }
}