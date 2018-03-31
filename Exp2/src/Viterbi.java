import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Viterbi {

    private static List<String> wrongWords = new ArrayList<>();

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

    private List<String> getViterbiWords(String line, String regex) {
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

            while (matcher.find()) {
                wrongWord = matcher.group(5);
            }

            wrongWord = getPlainWord(wrongWord);
            words.add(wrongWord);
            wrongWords.add(wrongWord);
            temp = endIndexList.get(i) + 1;
        }

        // 1 ve 0 olarak algiliyordu size'i. neden oldugunu anlamadim
        if (endIndexList.size() != 0 && line.length() - 1 > endIndexList.get(endIndexList.size() - 1)) {
            String lastWordOfTheLine = line.substring(endIndexList.get(endIndexList.size() - 1) + 2, line.length());
            lastWordOfTheLine = getPlainWord(lastWordOfTheLine);
            words.add(lastWordOfTheLine);
        }

        return words;
    }

    private static List<String> states = Arrays.asList("#", "NN", "VB");
    private static List<String> observations = Arrays.asList("I", "go", "some times");
    private static List<String> words = new ArrayList<>();
    private static List<Double> startProbabilities = Arrays.asList( 0.3, 0.4, 0.3 );
    private static double[][] transition_probability = { { 0.2, 0.2, 0.6 }, { 0.4, 0.1, 0.5 }, { 0.1, 0.8, 0.1 } };
    private static double[][] emission_probability = { { 0.01, 0.02, 0.02 }, { 0.8, 0.01, 0.5 }, { 0.19, 0.97, 0.48 } };

    private static class TNode {
//        private List<Integer> viterbiPath;
        private String wordWithMaxProb;
        private double viterbiProbability;

//        public TNode( List<Integer> viterbiPath, String wordWithMaxProb, double viterbiProbability) {
        public TNode(String wordWithMaxProb, double viterbiProbability) {
//            this.viterbiPath = new ArrayList<>(viterbiPath);
            this.wordWithMaxProb = wordWithMaxProb;
            this.viterbiProbability = viterbiProbability;
        }
    }

    private TNode getMaxTNode(List<String> candidates, String wrongWord) {
        DatasetOperations datasetOperations = new DatasetOperations();
        HiddenMarkovModel hmm = new HiddenMarkovModel();
        double maxProbability = 0.0;
        String maxProbWord = "";

        for (int i = 0; i < candidates.size(); i++) {
            datasetOperations.getMinEditDistance(candidates.get(i), wrongWord, candidates.get(i).length(), wrongWord.length());

            String correctLetters = datasetOperations.getCorrectLetters();
            String wrongLetters = datasetOperations.getWrongLetters();

            double emissionProbability = hmm.getEmissionProbability(correctLetters, wrongLetters);
            double currentStateProbability = hmm.getTransitionProbability("<s>", candidates.get(i));

            currentStateProbability = currentStateProbability * emissionProbability;

            if (currentStateProbability > maxProbability) {
                maxProbability = currentStateProbability;
                maxProbWord = candidates.get(i);
            }
        }

        return new TNode(maxProbWord, maxProbability);
    }

    public void implementViterbi() {
        DatasetOperations datasetOperations = new DatasetOperations();
        FReader fR = new FReader();
        HiddenMarkovModel hmm = new HiddenMarkovModel();

        List<String> wrongLines = fR.getWrongLines();
        String regex = fR.getRegex();

        Map<String, List<String>> wrongAndCorrectWordForms = fR.getWrongAndCorrectWordForms();

//        for (Map.Entry<String, List<String>> entry: wrongAndCorrectWordForms.entrySet()) {
//            System.out.println(entry.getKey() + " - " + entry.getValue());
//        }

        for (String line : wrongLines) {
            List<String> viterbiWords = getViterbiWords(line, regex);

            List<String> states = new ArrayList<>();
            List<Double> initialProbabilities = new ArrayList<>();
            List<List<Double>> transitionProbabilities = new ArrayList<>();
            List<List<Double>> emissionProbabilities = new ArrayList<>();

            List<String> candidates = wrongAndCorrectWordForms.get(viterbiWords.get(0));

            //////////////////////////////////////// INITIAL PROBABILITIES ////////////////////////////////////
            String firstWord = viterbiWords.get(0);

            double initialProbability = hmm.getTransitionProbability("<s>", firstWord);

            List<TNode> tNodeList = new ArrayList<>();
            List<Integer> path = new ArrayList<>();

            // if the word is typed correct
            if (candidates == null) {
                initialProbabilities.add(initialProbability);
                path.add(0);
                tNodeList.add(new TNode(firstWord, initialProbabilities.get(0)));
            }
            // if the word is typed wrong
            // calculate the emission probabilities of each candidate
            else {
                tNodeList.add(getMaxTNode(candidates, firstWord));
//                initialProbabilities.add(maxProbability);
            }
            //////////////////////////////////////// INITIAL PROBABILITIES ////////////////////////////////////


            for (int i = 0; i < viterbiWords.size(); i++) {
                TNode maxTNode = tNodeList.get(i);
                String currentViterbiWord = viterbiWords.get(i);
                candidates = wrongAndCorrectWordForms.get(viterbiWords.get(i));

                // if the word is typed correct
                // calculate only transition probability
                if (candidates == null) {
                    double transitionProbability = hmm.getTransitionProbability(maxTNode.wordWithMaxProb, currentViterbiWord);
                    tNodeList.add(new TNode(currentViterbiWord, transitionProbability));
                }
                // if the word is typed wrong
                // calculate emission and transition probabilities of each candidate
                else {
                    tNodeList.add(getMaxTNode(candidates, viterbiWords.get(i)));
                }
//                List<TNode> tempTNodeList = new ArrayList<>();

            }

            for (TNode tNode : tNodeList) {
                System.out.println(tNode.wordWithMaxProb + " - " + tNode.viterbiProbability);
            }

//            for (String currentWrongWord : wrongWords) {
//                // previousWord : I
//                // viterbiWords : I wach it each night
//                // candidates : watch, each, wash
//                for (String previousWord : viterbiWords) {
////                    List<String> candidates = wrongAndCorrectWordForms.get(currentWrongWord);
//
//                    List<Double> tempTransitionProbabilities = new ArrayList<>();
//                    List<Double> tempEmissionProbabilities = new ArrayList<>();
//                    for (String candidate : candidates) {
//                        states.add(candidate);
//                        // transition probabilities
//                        tempTransitionProbabilities.add(hmm.getTransitionProbability(previousWord, candidate));
//
//                        // emission probabilities
//                        datasetOperations.getMinEditDistance(candidate, currentWrongWord, candidate.length(), currentWrongWord.length());
//                        String correctLetters = datasetOperations.getCorrectLetters();
//                        String wrongLetters = datasetOperations.getWrongLetters();
//
//                        tempEmissionProbabilities.add(hmm.getEmissionProbability(correctLetters, wrongLetters));
//                    }
//
//                    transitionProbabilities.add(tempTransitionProbabilities);
//                    emissionProbabilities.add(tempEmissionProbabilities);
//                }
//            }





            wrongWords.clear();
        }
    }
}