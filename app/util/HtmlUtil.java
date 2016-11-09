package util;

import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Source;

/**
 * Created by Tobias Waltl on 29.08.2015.
 */
public class HtmlUtil {
    /**
     * Converts a position within a plain text into the corresponding position within the given HTML
     *
     * @param html     The HTML representation of the text
     * @param plainPos The position within the plain text
     * @return The HTML position of the given plain text position for the given HTML. -1 for invalid plainPos.
     */
    public static int convertPlainPosToHtmlPos(String html, String plainText, int plainPos) {
        int htmlPos = 0;
        int tagLength, specialCharLength;

        // Eliminate line separators
        int toSubtract = 0;
        int i = 0;
        String subString = plainText.substring(0, plainPos);
        while (i > -1) {
            i = subString.indexOf("\r\n");
            if (i > -1) {
                toSubtract += 2;
                subString = subString.substring(i + 2);
            }
        }
        plainPos -= toSubtract;
        while (htmlPos <= plainPos) {
            if (htmlPos == plainPos && htmlPos == html.length())
                // We are at the correct position which is the end of the given html
                return htmlPos;

            if (html.charAt(htmlPos) == '<') {
                // We are at the beginning of an HTML tag
                tagLength = html.indexOf(">", htmlPos + 1) - htmlPos + 1;
                htmlPos += tagLength;
                plainPos += tagLength;
            } else if (html.charAt(htmlPos) == '&' && html.substring(htmlPos + 1, htmlPos + 7).contains(";")) {
                // This is most probably a special character encoded in its HTML code
                specialCharLength = html.indexOf(";", htmlPos + 1) + 1 - htmlPos;
                htmlPos += specialCharLength;
                plainPos += specialCharLength - 1; // One char is consumed for the special char
            } else {
                // We are in plain text
                if (htmlPos == plainPos)
                    // we have reached the correct position within the given HTML
                    return htmlPos;
                else htmlPos++;
            }
        }

        return -1;
    }

    /**
     * Converts the given coveredText into plaintext
     *
     * @param content The coveredText to be converted
     * @return The plaintext representation of the given coveredText.
     */
    public static String convertToPlaintext(String content) {
        // some pre-processing for the Jericho renderer
        String result = content.replace(" <", "_<"); // Replace a space before a tag with a dummy char to prevent it
        // from getting ignored by Jericho

        // Convert to plain text
        Renderer renderer = new Source(result).getRenderer();
        renderer.setMaxLineLength(3000);
        renderer.setBlockIndentSize(0);
        renderer.setListIndentSize(0);
        result = renderer.toString();
        //result = result.replace('_', ' '); // After extracting the plaintext replace the dummy char with the space again
        return result;
    }

    /**
     * Determines in a very naive manner whether the given coveredText is in HTML format
     * @param content   The coveredText to be analyzed
     * @return  True, if the coveredText is in HTML format. False otherwise.
     */
    public static boolean isHtml (String content) {
        return content.contains("<") && content.contains(">");
    }

    /**
     *
     * @param content   The text for which XML chars shall be escaped
     * @return  The given String with all XML chars escaped. Null if coveredText is null.
     */
    public static String escapeXmlCharacters (String content) {
        if (content == null)
            return null;
        return content.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}
