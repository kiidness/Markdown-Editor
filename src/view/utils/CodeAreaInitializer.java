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
    private static final String BOLD_PATTERN = PatternFactory.generateMultilinedBalisePattern("[*_]{2}", "BOLD");
    private static final String ITALIC_PATTERN = PatternFactory.generateMultilinedBalisePattern("[*_]", "ITALIC");
    private static final String STRIKETHROUGH_PATTERN = PatternFactory.generateMultilinedBalisePattern("[~]{2}", "STRIKETHROUGH");
    private static final String TITLE1_PATTERN = PatternFactory.generateTitlePattern(1);
    private static final String TITLE2_PATTERN = PatternFactory.generateTitlePattern(2);
    private static final String TITLE3_PATTERN = PatternFactory.generateTitlePattern(3);
    private static final String TITLE4_PATTERN = PatternFactory.generateTitlePattern(4);
    private static final String TITLE5_PATTERN = PatternFactory.generateTitlePattern(5);
    private static final String TITLE6_PATTERN = PatternFactory.generateTitlePattern(6);
    private static final String BALISE_PATTERN = "<.*/?>";
    private static final String BLOCK_CODE_PATTERN = PatternFactory.generateMultilinedBalisePattern("[`]{3}", "BLOCKCODE");
    private static final String CODE_PATTERN = PatternFactory.generateMultilinedBalisePattern("[`]", "CODE");

    private static final String ITALICBOLD_PATTERN = PatternFactory.generateMultilinedBalisePattern("[*_]{3}", "ITALICBOLD");
    private static final String BOLDSTRIKETHROUGH_PATTERN = PatternFactory.generateMultilinedBalisePattern("([~]{2}[*_]{2})|([*_]{2}[~]{2})", "BOLDSTRIKETHROUGH");
    private static final Pattern PATTERN = Pattern.compile(
                    BOLDSTRIKETHROUGH_PATTERN
                    + "|" + ITALICBOLD_PATTERN
                    + "|" + BOLD_PATTERN
                    + "|" + ITALIC_PATTERN
                    + "|" + STRIKETHROUGH_PATTERN
                    + "|" + TITLE1_PATTERN
                    + "|" + TITLE2_PATTERN
                    + "|" + TITLE3_PATTERN
                    + "|" + TITLE4_PATTERN
                    + "|" + TITLE5_PATTERN
                    + "|" + TITLE6_PATTERN
                    + "|(?<BALISE>" + BALISE_PATTERN + ")"
                    + "|" + BLOCK_CODE_PATTERN
                    + "|" + CODE_PATTERN
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
        Matcher matcher = PATTERN.matcher(text); // Adding an espace to get arround with \ character
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
                                                        matcher.group("BLOCKCODE") != null ? "blockcode" :
                                                                matcher.group("CODE") != null ? "code" :
                    matcher.group("BALISE") != null ? "balise" :
                                                        null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start(styleClass.toUpperCase()) - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end(styleClass.toUpperCase()) - matcher.start(styleClass.toUpperCase()));
            lastKwEnd = matcher.end(styleClass.toUpperCase());
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
