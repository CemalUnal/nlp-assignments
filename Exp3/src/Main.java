import java.io.IOException;

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

        try {
            FWriter fWriter = preprocessing.openOutputFile(resultsFile);

            preprocessing.readStopWords(stopWordsFile);

            preprocessing.processInputLines(trainFile, inputFileType, fWriter);

            inputFileType = "test";
            preprocessing.processInputLines(testFile, inputFileType, fWriter);

            fWriter.closeFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
