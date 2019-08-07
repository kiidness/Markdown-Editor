package view.utils;

public abstract class PatternFactory {
    public static String generateMultilinedBalisePattern (String baliseRegex, String groupName) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("(\\A|[^\\\\])(?<");
        stringBuilder.append(groupName);
        stringBuilder.append(">");
        stringBuilder.append(baliseRegex);
        stringBuilder.append("((?!");
        stringBuilder.append(baliseRegex);
        stringBuilder.append(")(.|[\n]))*[^\\\\]");
        stringBuilder.append(baliseRegex);
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    public static String generateTitlePattern (int titleNumber) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("(?<TITLE");
        stringBuilder.append(titleNumber);
        stringBuilder.append(">((^#{");
        stringBuilder.append(titleNumber);
        stringBuilder.append("})|(\n#{");
        stringBuilder.append(titleNumber);
        stringBuilder.append("}))\\h[^\n]+)");

        return stringBuilder.toString();
    }
}
