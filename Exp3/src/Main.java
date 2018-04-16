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
        String inputFileType = "train";

        Preprocessing preprocessing = new Preprocessing();
//        NaiveBayes naiveBayes = new NaiveBayes();

        try {
            FWriter fWriter = preprocessing.openOutputFile(resultsFile);

            preprocessing.readStopWords(stopWordsFile);

            preprocessing.processInputLines(trainFile, inputFileType, fWriter);

            inputFileType = "test";
            preprocessing.processInputLines(testFile, inputFileType, fWriter);

            fWriter.closeFile();

//            Map<String, Map<String, Double>> featureVectorTrain = Preprocessing.getFeatureVectorTrainSet();
//            Map<String, Map<String, Double>> featureVectorTest = Preprocessing.getFeatureVectorTrainSet();

//            System.out.println(fOneFeatureVectorTrain.size());
//            System.out.println(fOneFeatureVectorTest.size());

//            naiveBayes.getNaiveBayesProbability(resultsFile);

//            Map<Integer, Map<List<UnorderedWord>, Double>> fTwoFeatureVector = Preprocessing.getfTwoFeatureVectorTrainSet();

//            for (Map.Entry<String, Map<String, Double>> entry : featureVectorTrain.entrySet()) {
//                System.out.println(entry.getKey());
//                for (Map.Entry<String, Double> item : entry.getValue().entrySet()) {
//                    System.out.println(item.getKey() + " - " + item.getValue());
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
