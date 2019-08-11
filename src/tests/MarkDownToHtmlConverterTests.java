package tests;

import model.utils.converters.MarkDownToHtmlConverter;
import org.junit.jupiter.api.Test;

public class MarkDownToHtmlConverterTests {
    public MarkDownToHtmlConverterTests() {
        MarkDownToHtmlConverter.setCurrentStyle("");
    }

    @Test
    public final void testSingleInlinedCode() {
        String markdown = "`test`";
        String html = "<code>test</code>";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testDoubleInlinedCode() {
        String markdown = "`test`, `test2`";
        String html = "<code>test</code>, <code>test2</code>";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testDoubleInlinedCodeWithTextAround() {
        String markdown = "This is a test : `test`, `test2` <= this is a test.";
        String html = "This is a test : <code>test</code>, <code>test2</code> <= this is a test.";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testBlockCode() {
        String markdown = "```<a>Test</a>\n<p>test 2</p>```";
        String html = "<pre>&lt;a&gt;Test&lt;/a&gt;\n&lt;p&gt;test 2&lt;/p&gt;</pre>";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testBold() {
        String markdown = "This is a **test**";
        String html = "This is a <strong>test</strong>";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testMultilinedBold() {
        String markdown = "**This is a\ntest**";
        String html = "<strong>This is a</br>\ntest</strong>";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testItalic() {
        String markdown = "This is a *test*.";
        String html = "This is a <em>test</em>.";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testItalicAndBold() {
        String markdown = "This is a ***test***.";
        String html = "This is a <strong><em>test</strong></em>.";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testHighlight() {
        String markdown = "This is a ==test==.";
        String html = "<p>This is a <mark>test</mark>.</br>\n</p>";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testStrikeThrough() {
        String markdown = "This is a ~~test~~.";
        String html = "This is a <strike>test</strike>.";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testTitle1() {
        String markdown = "# Title test";
        String html = "<h1>Title test</h1>";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testTitle6() {
        String markdown = "###### Title test";
        String html = "<h6>Title test</h6>";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testQuote() {
        String markdown = "> Quote test";
        String html = "<blockquote>Quote test</blockquote>";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testUlList() {
        String markdown = "- test\n- test 2";
        String html = "<ul>\n<li>test</li>\n<li>test 2</li>\n</ul>";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testOlList() {
        String markdown = "1. test\n2. test 2";
        String html = "<ol>\n<li>test</li>\n<li>test 2</li>\n</ol>";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }

    @Test
    public final void testTable() {
        String markdown = "Test 1|Test 2\n-|-\ntest 1|test 2\n";
        String html = "<table>\n<tr>\n<th>Test 1</th>\n<th>Test 2</th>\n</tr>\n<tr>\n<td>test 1</td>\n<td>test 2</td>\n</tr>\n</table>";

        var result = MarkDownToHtmlConverter.getConvertedBodyHtml(markdown);

        assert(result.contains(html));
    }
}
