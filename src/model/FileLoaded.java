package model;

import model.utils.converters.MarkDownToHtmlConverter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileLoaded {
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public final String PROP_FILE_NAME = "fileName";
    private String fileName;
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) {
        var oldValue = this.fileName;
        this.fileName = fileName;
        support.firePropertyChange(PROP_FILE_NAME, oldValue, fileName);
    }

    public final String PROP_FILE_PATH = "filePath";
    private String filePath;
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) {
        var oldValue = this.filePath;
        this.filePath = filePath;
        support.firePropertyChange(PROP_FILE_PATH, oldValue, filePath);
    }

    public final String PROP_HTML_TEXT = "htmlText";
    private String htmlText;
    public String getHtmlText() { return htmlText; }
    public void setHtmlText(String htmlText) {
        var oldValue = this.htmlText;
        this.htmlText = htmlText;
        support.firePropertyChange(PROP_HTML_TEXT, oldValue, htmlText);
    }

    private String savedMarkDownText;

    public final String PROP_MARKDOWN_TEXT = "markdownText";
    private String markDownText;
    public String getMarkDownText() { return markDownText; }
    public void setMarkDownText(String markDownText) {
        var oldValue = this.markDownText;
        this.markDownText = markDownText;
        setHtmlText(MarkDownToHtmlConverter.convert(markDownText));
        updateFileName();
        support.firePropertyChange(PROP_MARKDOWN_TEXT, oldValue, markDownText);
    }

    public void updateHtmlText() {
        setHtmlText(MarkDownToHtmlConverter.convert(markDownText));
    }

    public FileLoaded(String fileName, String filePath, String markDownText) {
        setFileName(fileName);
        setFilePath(filePath);
        savedMarkDownText = markDownText;
        setMarkDownText(markDownText);
    }

    public FileLoaded(String fileName) {
        setFileName(fileName);
        setFilePath("");
        savedMarkDownText = "-1";
        setMarkDownText("");
    }

    public int setHighlight(int start, int end) {
        return surroundWith(start, end, "={2}", "==");
    }

    public int setCode(int start, int end) {
        return surroundWith(start, end, "[`]{3}", "```");
    }

    public int setBold(int start, int end) {
        return surroundWith(start, end, "[*_]{2}", "**");
    }

    public int setItalic(int start, int end) {
        return surroundWith(start, end, "[*_]", "*");
    }

    public int setStrikeThrough(int start, int end) {
        return surroundWith(start, end, "~{2}", "~~");
    }

    private int surroundWith(int start, int end, String mdBalisisRegex, String markDownBalisis) {
        int max;
        for (int i = 10; i >= 0; i--) {
            max = 1 + i;
            if (start - max < 0 || markDownText.length() < end + max)
                continue;

            var subSequence = markDownText.subSequence(start - max, end + max).toString();

            Pattern pattern = Pattern.compile(String.format("(%s)", mdBalisisRegex));
            Matcher matcher = pattern.matcher(markDownText);
            int numApparitions = 0;
            while (matcher.find())
                numApparitions++;

            if (numApparitions > 1 && (numApparitions / 2) % 2 == 1) {
                String tmp = getMarkDownText();
                setMarkDownText(String.format("%s%s%s",
                        markDownText.subSequence(0, start - max),
                        subSequence.replaceFirst(String.format("^(.{0,%d})%s(.+)%s(.{0,%d})$", i, mdBalisisRegex, mdBalisisRegex, i), "$1$2$3"),
                        markDownText.subSequence(end + max, markDownText.length())));
                return (getMarkDownText().length() - tmp.length()) / 2;
            }
        }

        setMarkDownText(String.format("%s%s%s%s%s",
                markDownText.subSequence(0, start),
                markDownBalisis,
                markDownText.subSequence(start,end),
                markDownBalisis,
                markDownText.subSequence(end, markDownText.length())));
        return markDownBalisis.length();
    }

    public void setTitle(int numberLine, int titleNumber) {
        if (titleNumber < 1 || titleNumber > 6) {
            throw new UnsupportedOperationException("Incorrect title number");
        }
        numberLine -= 1;
        var lines = markDownText.split("\n");
        var selectedLine = lines[numberLine];

        StringBuilder addedText = new StringBuilder();
        for (int i = 0; i < titleNumber; i++) {
            addedText.append("#");
        }
        addedText.append(" ");

        if (selectedLine.matches("^#{1,6} .*")) {
            lines[numberLine] = lines[numberLine].replaceFirst("^#{1,6} ", "");
        } else {
            lines[numberLine] = String.format("%s%s", addedText, selectedLine);
        }
        setMarkDownText(String.join("\n", lines));
    }

    public void setQuote(int numberLine) {
        numberLine -= 1;
        var lines = markDownText.split("\n");
        var selectedLine = lines[numberLine];

        if (selectedLine.matches("^> .*")) {
            lines[numberLine] = lines[numberLine].replaceFirst("^> ", "");
        } else {
            lines[numberLine] = String.format("> %s", selectedLine);
        }
        setMarkDownText(String.join("\n", lines));
    }

    public void setImage(int caretPosition, String text, String path) {
        var firstPart = getMarkDownText().substring(0, caretPosition);
        var secondPart = getMarkDownText().substring(caretPosition);
        setMarkDownText(String.format("%s![%s](%s)%s", firstPart, text, path, secondPart));
    }

    public void setLink(int startPosition, int endPosition, String text, String link) {
        var firstPart = getMarkDownText().substring(0, startPosition);
        var secondPart = getMarkDownText().substring(endPosition);
        setMarkDownText(String.format("%s[%s](%s)%s", firstPart, text, link, secondPart));
    }

    public void updateSavedText() {
        savedMarkDownText = markDownText;
        updateFileName();
    }

    private void updateFileName() {
        if (isFileSaved()) {
            setFileName(fileName.replaceAll("\\*$",""));
        } else if (!fileName.endsWith("*")){
            setFileName(String.format("%s*", fileName));
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public boolean isFileSaved() {
        return markDownText.equals(savedMarkDownText);
    }

    public void addLink(int caretPosition) {
    }

    public int addOlListLine(int numberLine) {
        numberLine -= 1;
        var lines = markDownText.split("\n");
        var selectedLine = lines[numberLine];
        int caretDifference;

        // Check if ol line already present in previous line
        var currentValue = 1;
        if (numberLine > 0) {
            var pattern = Pattern.compile("(\\d+). [^\\n]*");
            var matcher = pattern.matcher(lines[numberLine - 1]);
            if (matcher.find()) {
                var group1 = matcher.group(1);
                currentValue = Integer.parseInt(group1) + 1;
            }
        }

        if (selectedLine.matches("\\d+. [^n]*")) {
            lines[numberLine] = lines[numberLine].replaceFirst("\\d+. ", "");
            caretDifference = -3;
        } else {
            var addedText = new StringBuilder();
            addedText.append(currentValue);
            addedText.append(". ");
            lines[numberLine] = String.format("%s%s", addedText, selectedLine);
            caretDifference = 3;
        }
        setMarkDownText(String.join("\n", lines));

        return caretDifference;
    }

    public int addUlListLine(int numberLine) {
        numberLine -= 1;
        var lines = markDownText.split("\n");
        var selectedLine = lines[numberLine];
        int caretDifference;

        if (selectedLine.matches("^(-|\\*) .*")) {
            lines[numberLine] = lines[numberLine].replaceFirst("^(-|\\*) ", "");
            caretDifference = -2;
        } else {
            var addedText = "- ";
            lines[numberLine] = String.format("%s%s", addedText, selectedLine);
            caretDifference = 2;
        }
        setMarkDownText(String.join("\n", lines));

        return caretDifference;
    }
}
