package model.utils.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public abstract class MarkDownToHtmlConverter {
    private static List<String> openWeakBaliseStack;
    private static List<String> openStrongBaliseStack;
    private static boolean hasComputedOneSingleLinedBalise, mayBeTable, mustAppendPreviousLine, mustIgnoreCurrentLine;
    private static String previousLine;

    private static String currentStyle = "";
    public static void setCurrentStyle(String styleText) {
        currentStyle = styleText;
    }

    public static String convert(String markDownText) {
        var convertedText = new StringBuilder();
        convertedText.append("<html>\n<header>\n<style>");
        convertedText.append(currentStyle);
        convertedText.append("\n</style>\n</header>\n<body>\n");
        convertedText.append(getConvertedBodyHtml(markDownText));
        convertedText.append("\n</body>\n</html>");
        return convertedText.toString();
    }

    public static String getConvertedBodyHtml(String markDownText) {
        openWeakBaliseStack = new ArrayList<>();
        openStrongBaliseStack = new ArrayList<>();
        previousLine = "";
        mayBeTable = false;

        var convertedText = new StringBuilder();
        for (var line : markDownText.split("\n")) {
            hasComputedOneSingleLinedBalise = true;
            mustAppendPreviousLine = false;
            mustIgnoreCurrentLine = false;

            line = computeMultiLinedStrongBalise(line);
            line = computeSingleLinedBalise(line);

            if (!hasComputedOneSingleLinedBalise && !mayBeTable) {
                line = computeParagraphBalise(line);
                if (getStrongBaliseIndex("pre") == -1) {
                    line = line + "</br>";
                }
            } else if (line.trim().isEmpty()) {
                line = closeAllCurrentWeakBalise(line);
            }

            if (mustAppendPreviousLine) {
                previousLine = previousLine + "\n";
                convertedText.append(previousLine);
            }

            previousLine = line;
            if (!mayBeTable && !mustIgnoreCurrentLine) {
                line = line + "\n";
                convertedText.append(line);
            }
        }
        if (mayBeTable) {
            convertedText.append(previousLine + "\n");
        }
        convertedText.append(closeAllCurrentWeakBalise(""));
        convertedText.append(closeAllCurrentStrongBalise(""));
        return convertedText.toString();
    }

    private static String computeMultiLinedStrongBalise(String line) {
        var isCodeDefinedBeforeLine = getStrongBaliseIndex("code") != -1;
        var isPreDefinedBeforeLine = getStrongBaliseIndex("pre") != -1;

        line = computeStrongBalise(line, "[`]{3}", "pre");
        line = computeStrongBalise(line, "[`]", "code");

        // code
        line = computeLtGtSigns(line, isCodeDefinedBeforeLine, isPreDefinedBeforeLine);

        if (getStrongBaliseIndex("code") != -1 || getStrongBaliseIndex("pre") != -1)  {
            return line;
        }

        // bold
        line = computeStrongBalise(line, "[*_]{2}", "strong");

        // italic
        line = computeStrongBalise(line, "[*_]", "em");

        // strikethrough
        line = computeStrongBalise(line, "[~]{2}", "strike");

        // mark
        line = computeStrongBalise(line, "==", "mark");

        line = line.replaceAll("\\\\([*_~=`])", "$1");

        return line;
    }

    private static String computeSingleLinedBalise(String line) {
        if (getStrongBaliseIndex("code") != -1)  {
            return line;
        }

        // Empty line
        if (line.trim().isEmpty()) {
            return "</br>";
        }

        // Line
        if (line.matches("^===+$|^---+$|^[*][*][*]+$")) {
            return "<hr>";
        }

        // Image
        var pattern = Pattern.compile("(.*)!\\[(.*)\\]\\((.*) \"(.*)\"\\)(.*)");
        var matcher = pattern.matcher(line);
        while (matcher.matches()) {
            line = String.format("%s<img alt=\"%s\" src=\"%s\" title=\"%s\"/>%s", matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
            matcher = pattern.matcher(line);
        }

        pattern = Pattern.compile("(.*)!\\[(.*)\\]\\((.*)\\)(.*)");
        matcher = pattern.matcher(line);
        while (matcher.matches()) {
            line = String.format("%s<img alt=\"%s\" src=\"%s\"/>%s", matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
            matcher = pattern.matcher(line);
        }

        // Link
        pattern = Pattern.compile("(.*)\\[(.+)\\]\\((.+) \"(.*)\"\\)(.*)");
        matcher = pattern.matcher(line);
        while (matcher.matches()) {
            line = String.format("%s<a href=\"%s\" title=\"%s\">%s</a>%s", matcher.group(1), matcher.group(3), matcher.group(4), matcher.group(2), matcher.group(5));
            matcher = pattern.matcher(line);
        }

        pattern = Pattern.compile("(.*)\\[(.+)\\]\\((.+)\\)(.*)");
        matcher = pattern.matcher(line);
        while (matcher.matches()) {
            line = String.format("%s<a href=\"%s\">%s</a>%s", matcher.group(1), matcher.group(3), matcher.group(2), matcher.group(4));
            matcher = pattern.matcher(line);
        }

        // Titles
        if (line.matches("^[#]{1,6}[\\s].+")) {
            var titleNum = line.replaceFirst("(^[#]{1,6})[\\s].+","$1").chars().count();
            if (titleNum < 7) {
                line = String.format("<h%d>%s</h%d>", titleNum, line.replaceFirst("^([#]){1,6}[\\s]", ""), titleNum);
                return closeAllCurrentWeakBalise(line);
            }
        }

        // Quotes
        if (line.matches("^[>][\\s].+")) {
            line = String.format("<blockquote>%s</blockquote>", line.replaceFirst("^>[\\s]", ""));
            return closeAllCurrentWeakBalise(line);
        }

        // Table
        if (mayBeTable) {
            if (line.matches("^[|]?(\\s*)[-]+(\\s*)([|](\\s*)[-]+)+(\\s*)[|]?$")) {
                previousLine.replaceAll("^[|]|[|]$", "");
                var partsHeader = previousLine.split("[|]");
                var sb = new StringBuilder();

                sb.append("<tr>\n");
                for (var part : partsHeader) {
                    sb.append("<th>");
                    sb.append(part);
                    sb.append("</th>\n");
                }
                sb.append("</tr>");
                previousLine = sb.toString();
                previousLine = openWeakBalise(previousLine, "table", true);

                mustAppendPreviousLine = true;
                mustIgnoreCurrentLine = true;
                mayBeTable = false;
                return line;
            } else if (line.matches("^[|]?.+([|].+)+[|]?$")) {
                mustAppendPreviousLine = true;
                mayBeTable = true;
            } else {
                mustAppendPreviousLine = true;
                mayBeTable = false;
            }
        } else if (line.matches("^[|]?.+([|].+)+[|]?$")) {
            if (getWeakBaliseIndex("table") == -1) {
                mayBeTable = true;
            } else {
                line = computeTableLine(line);
                return line;
            }
        }
        line = closeWeakBalise(line, "table");

        // Unordered list
        var balise = "ul";
        if (line.matches("^[-*][\\s].+")) {
            line = String.format("<li>%s</li>", line.replaceFirst("^(-|\\*)[\\s]", ""));
            if (getWeakBaliseIndex(balise) < 0) {
                line = openWeakBalise(line, balise, true);
            }
            return line;
        }
        line = closeWeakBalise(line, balise);

        // Ordered list
        balise = "ol";
        if (line.matches("^\\d[.][\\s].+")) {
            line = String.format("<li>%s</li>", line.replaceFirst("^\\d[.][\\s]", ""));
            if (getWeakBaliseIndex(balise) < 0) {
                line = openWeakBalise(line, balise, true);
            }
            return line;
        }
        line = closeWeakBalise(line, balise);

        hasComputedOneSingleLinedBalise = false;
        return line;
    }

    private static String computeTableLine(String line) {
        var sb = new StringBuilder();
        line = line.replaceAll("^[|]|[|]$", "");

        var parts = line.split("[|]");
        sb.append("<tr>\n");
        for (var part : parts) {
            sb.append("<td>");
            sb.append(part);
            sb.append("</td>\n");
        }
        sb.append("</tr>");

        line = sb.toString();

        return line;
    }

    private static String computeParagraphBalise(String line) {
        var balise = "p";
        var baliseIndex = getWeakBaliseIndex(balise);
        if (baliseIndex == -1) {
            openWeakBaliseStack.add(balise);
            return String.format("<%s>%s", balise, line);
        }
        return line;
    }

    private static String computeStrongBalise(String line, String mdRegex, String balise) {
        line = line + " ";
        var allBalise = balise.split(",");
        String part, tmpPart;


        if (line.matches(String.format(".*%s.*", mdRegex))) {
            line = " " + line;

            var pattern = Pattern.compile(String.format("([^\\\\])(%s)", mdRegex));
            var matcher = pattern.matcher(line);
            while (matcher.find()) {
                line = matcher.replaceFirst(String.format("$1%s", computeNextBalise(allBalise)));
                matcher = pattern.matcher(line);
            }
            line = line.replaceAll("^ ", "");
        }
        line = line.replaceAll(" $", "");
        return line;
    }

    private static String computeNextBalise(String[] allBalise) {
        int index;
        int baliseIndex = getStrongBaliseIndex(allBalise[0]);
        String currentBalise;
        StringBuilder stringBuilder = new StringBuilder();

        if (baliseIndex == -1) {
            for (index = 0; index < allBalise.length; index++) {
                currentBalise = allBalise[index];
                openStrongBaliseStack.add(currentBalise);
                stringBuilder.append(String.format("<%s>", currentBalise));
            }
        } else {
            for (index =  allBalise.length - 1; index >= 0; index--) {
                currentBalise = allBalise[index];
                baliseIndex = getStrongBaliseIndex(allBalise[index]);
                openStrongBaliseStack.remove(baliseIndex);
                stringBuilder.append(String.format("</%s>", currentBalise));
            }
        }
        return stringBuilder.toString();
    }

    private static String closeAllCurrentWeakBalise(String line) {
        var stringBuilder = new StringBuilder();
        for(var balise: openWeakBaliseStack) {
            stringBuilder.append(String.format("</%s>", balise));
            //openWeakBaliseStack.remove(balise);
        }
        stringBuilder.append(line);
        return stringBuilder.toString();
    }

    private static String closeAllCurrentStrongBalise(String line) {
        var stringBuilder = new StringBuilder();

        for(var balise: openStrongBaliseStack) {
            stringBuilder.append(String.format("</%s>", balise));
            //openStrongBaliseStack.remove(balise);
        }
        stringBuilder.append(line);
        return stringBuilder.toString();
    }

    private static String openWeakBalise(String line, String balise, Boolean closeAllCurrentWeakBalise) {
        line = String.format("<%s>\n%s", balise, line);
        if (closeAllCurrentWeakBalise) {
            line = closeAllCurrentWeakBalise(line);
        }
        openWeakBaliseStack.add(balise);
        return line;
    }

    private static String closeWeakBalise(String line, String balise) {
        var baliseIndex = getWeakBaliseIndex(balise);
        if (baliseIndex != -1) {
            line = String.format("</%s>%s", balise, line);
            openWeakBaliseStack.remove(baliseIndex);
        }
        return line;
    }

    private static String computeLtGtSigns(String line, boolean isCodeDefinedBeforeLine, boolean isPreDefinedBeforeLine) {
        String[] balises = {"pre", "code"};
        String[] parts;
        line = " " + line + " ";

        for (var balise : balises) {
            StringBuilder stringBuilder = new StringBuilder();
            if ((!isCodeDefinedBeforeLine && balise.equals("code")) | (!isPreDefinedBeforeLine && balise.equals("pre"))) {
                parts = line.split(String.format("<%s>", balise));

                parts[0] = parts[0].replaceAll("\\\\>", "&gt");
                parts[0] = parts[0].replaceAll("\\\\<", "&lt");
                stringBuilder.append(parts[0]);

                for (int i = 1; i < parts.length; i++) {
                    stringBuilder.append(String.format("<%s>", balise));

                    var subparts = parts[i].split(String.format("</%s>", balise));
                    subparts[0] = subparts[0].replaceAll(">", "&gt");
                    subparts[0] = subparts[0].replaceAll("<", "&lt");
                    stringBuilder.append(subparts[0]);

                    if (subparts.length >= 2) {
                        subparts[1] = subparts[1].replaceAll("\\\\>", "&gt");
                        subparts[1] = subparts[1].replaceAll("\\\\<", "&lt");
                        stringBuilder.append(String.format("</%s>", balise));
                        stringBuilder.append(subparts[1]);
                    }
                }
            } else {
                parts = line.split(String.format("</%s>", balise));

                parts[0] = parts[0].replaceAll(">", "&gt");
                parts[0] = parts[0].replaceAll("<", "&lt");
                stringBuilder.append(parts[0]);

                for (int i = 1; i < parts.length; i++) {
                    stringBuilder.append(String.format("</%s>", balise));

                    var subparts = parts[i].split(String.format("<%s>", balise));

                    subparts[0] = subparts[0].replaceAll("\\\\>", "&gt");
                    subparts[0] = subparts[0].replaceAll("\\\\<", "&lt");

                    if (subparts.length >= 2) {
                        subparts[1] = subparts[1].replaceAll(">", "&gt");
                        subparts[1] = subparts[1].replaceAll("<", "&lt");

                        stringBuilder.append(String.format("<%s>", balise));
                        stringBuilder.append(subparts[1]);
                    } else {
                        stringBuilder.append(subparts[0]);
                    }
                }
            }
            line = stringBuilder.toString();
        }

        if (line.length() <= 2) {
            return line.substring(1, 1);
        }
        return line.substring(1, line.length() - 1);
    }

    private static int getWeakBaliseIndex(String balise) {
        return IntStream.range(0, openWeakBaliseStack.size())
                .filter(index -> openWeakBaliseStack.get(index).equals(balise))
                .findFirst()
                .orElse(-1);
    }

    private static int getStrongBaliseIndex(String balise) {
        return IntStream.range(0, openStrongBaliseStack.size())
                .filter(index -> openStrongBaliseStack.get(index).equals(balise))
                .findFirst()
                .orElse(-1);
    }
}

