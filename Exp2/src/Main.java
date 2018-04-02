public class Main {

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        if (args.length != 2) {
            System.out.println("Please specify one input file and one output file. Example execution is --> Java Main yourInputFile yourOutputFile");
            return;
        }

        String inputFile = args[0];
        String resultsFile = args[1];

        FReader fileReader = new FReader();
        Viterbi viterbi = new Viterbi();

        DatasetOperations datasetOperations = new DatasetOperations();
        HiddenMarkovModel hmm = new HiddenMarkovModel();

        try {
            fileReader.getCorrectedDatasetLines(inputFile);

            fileReader.initializeWrongCorrectWordsMap();

            viterbi.implementViterbi(resultsFile);

            System.out.println("Execution is done. Estimated execution time is: " + (double) (System.nanoTime() - startTime) / 1000000000.0 + " seconds.");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
