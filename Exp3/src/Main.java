import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        if (args.length != 4) {
            System.out.println("Please specify one input file and one output file. Example execution is --> Java Main yourTrainFile yourTestFile yourOutputFile");
            return;
        }

        String stopWordsFile = args[0];
        String trainFile = args[1];
        String testFile = args[2];
        String resultsFile = args[3];

        Preprocessing preprocessing = new Preprocessing();

        try {
            preprocessing.readStopWords(stopWordsFile);
            preprocessing.processDatasetLines(trainFile);

            Map<Integer, Map<List<UnorderedWord>, Double>> fOneFeatureVector = Preprocessing.getfOneFeatureVector();
            Map<Integer, Map<List<UnorderedWord>, Double>> fTwoFeatureVector = Preprocessing.getfTwoFeatureVector();
//            System.out.println(fOneFeatureVector.size());
//            System.out.println(fTwoFeatureVector.size());

            for (Map.Entry<Integer, Map<List<UnorderedWord>, Double>> entry : fOneFeatureVector.entrySet()) {
                System.out.println(entry.getKey());
                for (Map.Entry<List<UnorderedWord>, Double> item : entry.getValue().entrySet()) {
                    System.out.println(item);

//                    System.out.println(item.getValue());
                    for (UnorderedWord unorderedWord : item.getKey()) {
                        System.out.println(unorderedWord.getWord() + " --- " + unorderedWord.getPosition());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println(Preprocessing.fOneFeatureVector.size());
//        System.out.println(Preprocessing.fOneFeatureVector);

        System.out.println("Estimated execution time is: " + (double) (System.nanoTime() - startTime) / 1000000000.0 + " seconds.");
//        Stemmer stemmer = new Stemmer();
//
//        String testString = "goes";
//
//        for (int i = 0; i < testString.length(); i++)
//            stemmer.add(testString.charAt(i));
//
//        stemmer.stem();
//        String resultString = stemmer.toString();
//
//        System.out.print(resultString);
    }
}
