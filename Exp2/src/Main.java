public class Main {

    public static void main(String[] args) {

        for (String arg : args) {
            System.out.println(arg);
        }
        if (args.length != 2) {
            System.out.println("Please specify one input file and one output file. Example execution is --> Java Main yourInputFile yourOutputFile");
            return;
        }

        String inputFile = args[0];
        String resultsFile = args[1];

        FReader fileReader = new FReader();
        FWriter fileWriter = new FWriter();

        try {
            fileReader.read(inputFile);
//            fileReader.printMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
