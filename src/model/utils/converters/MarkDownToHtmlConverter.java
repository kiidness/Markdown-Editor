package model.utils.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public abstract class MarkDownToHtmlConverter {
    private static List<String> openWeakBalisisStack;
    private static List<String> openStrongBalisisStack;
    private static boolean hasComputedOneSingleLinedBalisis;

    private static String currentStyle = "";
    public static void setCurrentStyle(String styleText) {
        currentStyle = styleText;
    }

    public static String convert(String markDownText) {
        openWeakBalisisStack = new ArrayList<>();
        openStrongBalisisStack = new ArrayList<>();

        var convertedText = new StringBuilder();
        convertedText.append("<html>\n<header>\n<style>");
        convertedText.append(currentStyle);
        convertedText.append("\n</style>\n</header>\n<body>\n");
        convertedText.append(getConvertedBodyHtml(markDownText));
        convertedText.append("\n</body>\n</html>");
        return convertedText.toString();
    }

    private static String getConvertedBodyHtml(String markDownText) {
        var convertedText = new StringBuilder();
        for (var line : markDownText.split("\n")) {
            hasComputedOneSingleLinedBalisis = true;

            line = computeSingleLinedBalisis(line);
            line = computeMultiLinedStrongBalisis(line);
            if (!hasComputedOneSingleLinedBalisis && getStrongBalisisIndex("code") == -1) {
                line = computeParagraphBalisis(line);
                line = line + "</br>";
            }
            line = line + "\n";
            convertedText.append(line);
        }
        convertedText.append(closeAllCurrentWeakBalisis(""));
        convertedText.append(closeAllCurrentStrongBalisis(""));
        return convertedText.toString();
    }

    private static String computeMultiLinedStrongBalisis(String line) {
        // code
        line = computeStrongBalisis(line, "([`]{3}|[`])", "code,xmp");

        if (getStrongBalisisIndex("code") != -1)  {
            return line;
        }

        // bold
        line = computeStrongBalisis(line, "[*_]{2}", "strong");

        // italic
        line = computeStrongBalisis(line, "[*_]", "em");

        // strikethrough
        line = computeStrongBalisis(line, "[~]{2}", "strike");

        // mark
        line = computeStrongBalisis(line, "==", "mark");

        line = line.replaceAll("\\\\([*_~=`])", "$1");

        return line;
    }

    private static String computeSingleLinedBalisis(String line) {
        if (getStrongBalisisIndex("code") != -1)  {
            return line;
        }

        // Empty line
        if (line.trim().isEmpty()) {
            return "</br>";
        }

        // Line
        if (line.matches("^===+$|^---+$")) {
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
                return closeAllCurrentWeakBalisis(line);
            }
        }

        // Quotes
        if (line.matches("^[>][\\s].+")) {
            line = String.format("<blockquote>%s</blockquote>", line.replaceFirst("^>[\\s]", ""));
            return closeAllCurrentWeakBalisis(line);
        }

        // Unordered list
        var balisis = "ul";
        if (line.matches("^[-*][\\s].+")) {
            line = String.format("<li>%s</li>", line.replaceFirst("^(-|\\*)[\\s]", ""));
            if (getWeakBalisisIndex(balisis) < 0) {
                line = openWeakBalisis(line, balisis, true);
            }
            return line;
        }
        line = closeWeakBalisis(line, balisis);

        // Ordered list
        balisis = "ol";
        if (line.matches("^\\d[.][\\s].+")) {
            line = String.format("<li>%s</li>", line.replaceFirst("^\\d[.][\\s]", ""));
            if (getWeakBalisisIndex(balisis) < 0) {
                line = openWeakBalisis(line, balisis, true);
            }
            return line;
        }
        line = closeWeakBalisis(line, balisis);

        hasComputedOneSingleLinedBalisis = false;
        return line;
    }

    private static String computeParagraphBalisis(String line) {
        var balisis = "p";
        var balisisIndex = getWeakBalisisIndex(balisis);
        if (balisisIndex == -1) {
            openWeakBalisisStack.add(balisis);
            return String.format("<%s>%s", balisis, line);
        }
        return line;
    }

    private static String computeStrongBalisis(String line, String mdRegex, String balisis) {
        line = line + " ";
        var allBalisis = balisis.split(",");
        String part;


        if (line.matches(String.format(".*%s.*", mdRegex))) {
            var stringBuilder = new StringBuilder();
            line = " " + line;

            var parts = line.split(String.format("[^\\\\]%s", mdRegex));
            var numberOfParts = parts.length;
            var lastPartIndex = numberOfParts - 1;

            if (numberOfParts == 0) {
                stringBuilder.append(computeNextBalisis(allBalisis));
            }
            if (parts[0].startsWith(" ")) {
                parts[0] = parts[0].substring(1);
            }

            for(int i = 0; i < numberOfParts; i++) {
                part = parts[i];
                stringBuilder.append(part);
                if (i != lastPartIndex) {
                    stringBuilder.append(computeNextBalisis(allBalisis));
                }
            }
            return stringBuilder.toString();
        }
        line = line.replaceAll(" $", "");
        return line;
    }

    private static String computeNextBalisis(String[] allBalisis) {
        int index;
        int balisisIndex = getStrongBalisisIndex(allBalisis[0]);
        String currentBalisis;
        StringBuilder stringBuilder = new StringBuilder();

        if (balisisIndex == -1) {
            for (index = 0; index < allBalisis.length; index++) {
                currentBalisis = allBalisis[index];
                openStrongBalisisStack.add(currentBalisis);
                stringBuilder.append(String.format("<%s>", currentBalisis));
            }
        } else {
            for (index =  allBalisis.length - 1; index >= 0; index--) {
                currentBalisis = allBalisis[index];
                balisisIndex = getStrongBalisisIndex(allBalisis[index]);
                openStrongBalisisStack.remove(balisisIndex);
                stringBuilder.append(String.format("</%s>", currentBalisis));
            }
        }
        return stringBuilder.toString();
    }

    private static String closeAllCurrentWeakBalisis(String line) {
        var stringBuilder = new StringBuilder();
        for(var balisis: openWeakBalisisStack) {
            stringBuilder.append(String.format("</%s>", balisis));
        }
        stringBuilder.append(line);
        return stringBuilder.toString();
    }

    private static String closeAllCurrentStrongBalisis(String line) {
        var stringBuilder = new StringBuilder();

        var indexCode = getStrongBalisisIndex("code");
        if (indexCode != -1) {
            var index = getStrongBalisisIndex("xmp");
            if (index != -1) {
                stringBuilder.append("</xmp>");
                openStrongBalisisStack.remove(index);
            }
            stringBuilder.append("</code>");
            openStrongBalisisStack.remove(indexCode);
        }

        for(var balisis: openStrongBalisisStack) {
            stringBuilder.append(String.format("</%s>", balisis));
        }
        stringBuilder.append(line);
        return stringBuilder.toString();
    }

    private static String openWeakBalisis(String line, String balisis, Boolean closeAllCurrentWeakBalisis) {
        line = String.format("<%s>\n%s", balisis, line);
        if (closeAllCurrentWeakBalisis) {
            line = closeAllCurrentWeakBalisis(line);
        }
        openWeakBalisisStack.add(balisis);
        return line;
    }

    private static String closeWeakBalisis(String line, String balisis) {
        var balisisIndex = getWeakBalisisIndex(balisis);
        if (balisisIndex != -1) {
            line = String.format("</%s>%s", balisis, line);
            openWeakBalisisStack.remove(balisisIndex);
        }
        return line;
    }

    private static int getWeakBalisisIndex(String balisis) {
        return IntStream.range(0, openWeakBalisisStack.size())
                .filter(index -> openWeakBalisisStack.get(index).equals(balisis))
                .findFirst()
                .orElse(-1);
    }

    private static int getStrongBalisisIndex(String balisis) {
        return IntStream.range(0, openStrongBalisisStack.size())
                .filter(index -> openStrongBalisisStack.get(index).equals(balisis))
                .findFirst()
                .orElse(-1);
    }
}

