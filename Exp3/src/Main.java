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

        // TODO: HER BIR SENSE IDYE KARSILIK LIST TUTMAK YERINE MAP TUTMAK GEREKEBILIR.

        try {
            preprocessing.readStopWords(stopWordsFile);
            preprocessing.processDatasetLines(trainFile);

            Map<Integer, List<List<UnorderedWord>>> fOneFeatureVector = Preprocessing.getfOneFeatureVector();
            System.out.println(fOneFeatureVector.size());

//
//            for (Map.Entry<Integer, List<List<UnorderedWord>>> entry : fOneFeatureVector.entrySet()) {
//                System.out.println(entry.getKey());
//                for (List<UnorderedWord> item : entry.getValue()) {
//                    System.out.println(entry.getValue());
//
//                    for (UnorderedWord unorderedWord : item) {
//                        System.out.println(unorderedWord.getWord() + " --- " + unorderedWord.getPosition());
//                    }
//                }
//            }

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
