import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        if (args.length != 3) {
            System.out.println("Please specify one input file and one output file. Example execution is --> Java Main yourTrainFile yourTestFile yourOutputFile");
            return;
        }

        String stopWordsFile = System.getProperty("user.dir") + "/src/stopwords.txt";
        String trainFile = args[0];
        String testFile = args[1];
        String resultsFile = args[2];
        String inputFileType = "train";

        Preprocessing preprocessing = new Preprocessing();

        //TODO: !!!!!!!!!!!!!!!!!!!!!!!!! test set icin stem almak gerekebilir !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

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
    }
}
