package view.utils;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CodeAreaInitializer {
    private static final String BOLD_PATTERN = "[*_]{2}[^\n]+[*_]{2}";
    private static final String ITALIC_PATTERN = "[*_][^\n]+[*_]";
    private static final String STRIKETHROUGH_PATTERN = "[~]{2}[^\\n]+[~]{2}";
    private static final String TITLE1_PATTERN = "((^#)|(\n#))\\h[^\n]+";
    private static final String TITLE2_PATTERN = "((^#{2})|(\n#{2}))\\h[^\n]+";
    private static final String TITLE3_PATTERN = "((^#{3})|(\n#{3}))\\h[^\n]+";
    private static final String TITLE4_PATTERN = "((^#{4})|(\n#{4}))\\h[^\n]+";
    private static final String TITLE5_PATTERN = "((^#{5})|(\n#{5}))\\h[^\n]+";
    private static final String TITLE6_PATTERN = "((^#{6})|(\n#{6}))\\h[^\n]+";
    private static final String BALISE_PATTERN = "<(.|\\R)*?/?>";

    private static final String ITALICBOLD_PATTERN = "[*_]{3}[^\n]+[*_]{3}";
    private static final String BOLDSTRIKETHROUGH_PATTERN= "([~]{2}[*_]{2})|([*_]{2}[~]{2})[^\n]+([~]{2}[*_]{2})|([*_]{2}[~]{2})";

    private static final Pattern PATTERN = Pattern.compile(
                    "(?<BOLDSTRIKETHROUGH>" + BOLDSTRIKETHROUGH_PATTERN + ")"
                    + "|(?<ITALICBOLD>" + ITALICBOLD_PATTERN + ")"
                    + "|(?<BOLD>" + BOLD_PATTERN + ")"
                    + "|(?<ITALIC>" + ITALIC_PATTERN + ")"
                    + "|(?<STRIKETHROUGH>" + STRIKETHROUGH_PATTERN + ")"
                    + "|(?<TITLE1>" + TITLE1_PATTERN + ")"
                    + "|(?<TITLE2>" + TITLE2_PATTERN + ")"
                    + "|(?<TITLE3>" + TITLE3_PATTERN + ")"
                    + "|(?<TITLE4>" + TITLE4_PATTERN + ")"
                    + "|(?<TITLE5>" + TITLE5_PATTERN + ")"
                    + "|(?<TITLE6>" + TITLE6_PATTERN + ")"
                    + "|(?<BALISE>" + BALISE_PATTERN + ")"
    );

    public static void initialize(CodeArea codeArea) {
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        Subscription cleanupWhenNoLongerNeedIt = codeArea
                .multiPlainChanges()
                // refresh rate
                .successionEnds(Duration.ofMillis(1))
                .subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));

    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("TITLE1") != null ? "title1" :
                            matcher.group("TITLE2") != null ? "title2" :
                                    matcher.group("TITLE3") != null ? "title3" :
                                            matcher.group("TITLE4") != null ? "title4" :
                                                    matcher.group("TITLE5") != null ? "title5" :
                                                            matcher.group("TITLE6") != null ? "title6" :
                    matcher.group("BOLDSTRIKETHROUGH") != null ? "boldstrikethrough" :
                        matcher.group("ITALICBOLD") != null ? "italicbold" :
                            matcher.group("ITALIC") != null ? "italic" :
                                    matcher.group("BOLD") != null ? "bold" :

                                                matcher.group("STRIKETHROUGH") != null ? "strikethrough" :
                                                        null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
