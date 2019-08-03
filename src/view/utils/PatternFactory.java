package view.utils;

public abstract class PatternFactory {
    public static String generateMultilinedBalisePattern (String baliseRegex, String groupName) {
        StringBuilder stringBuilder = new StringBuilder();


        stringBuilder.append("([^\\\\])(?<");
        stringBuilder.append(groupName);
        stringBuilder.append(">");
        stringBuilder.append(baliseRegex);
        stringBuilder.append("[^\n]+[^\\\\]");
        stringBuilder.append(baliseRegex);
        stringBuilder.append(")");
        /*

        stringBuilder.append("|(?<");
        stringBuilder.append(groupName);
        stringBuilder.append(">^(");
        stringBuilder.append(baliseRegex);
        stringBuilder.append(")[^\n]+[^\\\\](");
        stringBuilder.append(baliseRegex);
        stringBuilder.append("))");
        */
        /*
        stringBuilder.append("|(?<");
        stringBuilder.append(groupName);
        stringBuilder.append(">^(");
        stringBuilder.append(baliseRegex);
        stringBuilder.append(")([^\n]+[^\\\\](");
        stringBuilder.append(baliseRegex);
        stringBuilder.append(")$))|");


        stringBuilder.append("[^\\\\\\\\](?<");
        stringBuilder.append(groupName);
        stringBuilder.append(">((");
        stringBuilder.append(baliseRegex);
        stringBuilder.append(")[^\n]+[^\\\\](");
        stringBuilder.append(baliseRegex);
        stringBuilder.append(")))");
        */
        return stringBuilder.toString();
    }
}
