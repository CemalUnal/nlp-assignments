import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegexMap {

    private static Map<String, String> regexMap = new HashMap<>();

    public void addRegex(String name, String value) {
        regexMap.put(name, value);
    }

    public String getRegex(String name) {
        return regexMap.get(name);
    }

    public Map<String, String> getRegexMap() {
        return regexMap;
    }

    /**
     * Initializes the regexMap with some regex names
     * and their string values.
     */
    public void initializeRegexMap() {
        addRegex("ALLEN_REGEX", "allen-p/_sent_mail/");
        addRegex("DATE_REGEX", "Date: [a-zA-Z]+, \\d{1,2} [a-zA-Z]+ \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}");
        addRegex("FROM_REGEX", "From: [a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
        addRegex("TO_REGEX", "To: [a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
        addRegex("LESS_GREATER_THAN_REGEX", "<[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+>");
        addRegex("CC_REGEX", "cc:");
        addRegex("SUBJECT_REGEX", "Subject:[a-zA-Z0-9_ ]");
        addRegex("MIME_REGEX", "Mime-Version:");
        addRegex("CTYPE_REGEX", "Content-Type:");
        addRegex("CTENC_REGEX", "Content-Transfer-Encoding:");
        addRegex("XFROM_REGEX", "X-From:");
        addRegex("XTO_REGEX", "X-To:");
        addRegex("XCC_REGEX", "X-cc:");
        addRegex("XBCC_REGEX", "X-bcc:");
        addRegex("XFOLDER_REGEX", "X-Folder:");
        addRegex("XORIGIN_REGEX", "X-Origin:");
        addRegex("XFILENAME_REGEX", "X-FileName:");
        addRegex("DASH_REGEX", "-{5}+");
    }

    /**
     * Checks whether the line of the emails.csv file contains
     * any regex that is in the regexMap.
     *
     * @param line a line from the input file
     * @return true if line contains any regex, otherwise returns false.
     */
    public boolean containsRegex(String line) {
        for (Map.Entry<String, String> entry : getRegexMap().entrySet()) {
            if (Pattern.compile(entry.getValue()).matcher(line).find()) {
                return true;
            }
        }

        return false;
    }

}