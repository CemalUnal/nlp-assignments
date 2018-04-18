import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FWriter {

    private BufferedWriter writer;

    /**
     * Opens the given output file.
     *
     * @param file output file
     * @throws IOException when an error occurs while creating the FileWriter
     */
    public void openFile(String file) throws IOException {
        writer = new BufferedWriter(new FileWriter(file));
    }

    /**
     * Writes a message to the output file.
     *
     * @param message output file
     */
    public void write(String message) {
        try {
            writer.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the output file.
     */
    public void closeFile() {
        try {
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred while closing the file. Message is: " + e.getMessage());
        }
    }

}