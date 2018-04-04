public class Main {

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        if (args.length != 2) {
            System.out.println("Please specify one input file and one output file. Example execution is --> Java Main yourInputFile yourOutputFile");
            return;
        }

        String inputFile = args[0];
        String resultsFile = args[1];

        Preprocessing preprocessing = new Preprocessing();
        Viterbi viterbi = new Viterbi();

        try {
            System.out.printf("Preprocessing started.%n%n");

            preprocessing.processDatasetLines(inputFile);
            preprocessing.initializeWrongCorrectWordsMap();

            System.out.println("Preprocessing is done. Implementing viterbi...");

            viterbi.implementViterbi(resultsFile);

            System.out.println("Estimated execution time is: " + (double) (System.nanoTime() - startTime) / 1000000000.0 + " seconds.");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}