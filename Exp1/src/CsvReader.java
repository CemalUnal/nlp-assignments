import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CsvReader {

    public CsvReader() {  }

    private RegexMap regexMap = new RegexMap();
    private List<String> emails = new ArrayList<>();
    private String fileExtension;

    /**
     * Determines all tokens for a line of the input file.
     *
     * @param line a line from the input file
     * @return list of all tokens in a line including punctuation marks
     */

    private ArrayList<String> separatePunctuations (String line) {
        ArrayList<String> allTokens = new ArrayList<>();

        Pattern pattern = Pattern.compile("(\\w+)|[^\\s]"); // this regex is used to separate punctuation marks
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            allTokens.add(matcher.group());
        }

        return allTokens;
    }

    /**
     * Determines all tokens for an input file.
     *
     * @param filePath path of the input file
     * @return list of all tokens in input file including punctuation marks
     * @throws Exception when the input file is not found in the given path or
     *                   an error occurs while reading the file
     */
    public List<String> read(String filePath) throws Exception {
        Path file = Paths.get(filePath);
        if (!Files.exists(file)) {
            throw new FileNotFoundException(filePath);
        }

        regexMap.initializeRegexMap();

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        List<String> allTokens = null;
        boolean endOfEmailIsReached = false;
        StringBuilder emailBuilder = new StringBuilder();

        fileExtension = getFileExtension(new File(filePath));

        if (fileExtension.equalsIgnoreCase("csv")) {
            reader.readLine();
        }
        while ((line = reader.readLine()) != null) {
            if (!regexMap.containsRegex(line)) {
                ArrayList<String> tempTokens;
                line = line.replaceAll("\\s*\"*$", "");
                tempTokens = separatePunctuations(line);

                if (allTokens == null) {
                    allTokens = tempTokens;
                } else {
                    allTokens.addAll(tempTokens);
                }
                if (endOfEmailIsReached) {
                    emails.add(emailBuilder.toString());
                    emailBuilder.setLength(0);
                    endOfEmailIsReached = false;
                } else {
                    if (!line.equals(""))
                        emailBuilder.append(line);
                }
            } else {
                if (Pattern.compile(regexMap.getRegex("XFILENAME_REGEX")).matcher(line).find()) {
                    endOfEmailIsReached = true;
                }
            }
        }

        return allTokens;
    }

    /**
     * Determines the extension of a given file.
     *
     * @param file a file
     * @return extension of the file
     */
    private String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }

    /**
     * Returns the fileExtension variable
     *
     * @return fileExtension variable
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * Returns the all emails that are in the input file.
     *
     * @return all emails that are in the input file
     */
    public List<String> getEmails() {
        return emails;
    }
}