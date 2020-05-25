package placement.ui;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.Toolkit;
import java.text.*;
import java.util.Locale;

public class FormattedDocument extends PlainDocument {
    private Format format;
    private ParsePosition parsePos = new ParsePosition(0);

    public FormattedDocument(Format f) {
        format = f;
    }

    public Format getFormat() {
        return format;
    }

    public void insertString(int offs, String str, AttributeSet a)
        throws BadLocationException {

        String currentText = getText(0, getLength());
        String beforeOffset = currentText.substring(0, offs);
        String afterOffset = currentText.substring(offs, currentText.length());
        String proposedResult = beforeOffset + str + afterOffset;

        try {
	    parsePos.setIndex(0);
            format.parseObject(proposedResult,parsePos);
	    //now check for junk at the end of the field!
	    if (parsePos.getIndex() != proposedResult.length())
	      throw new ParseException("Couldn't parse end of field",parsePos.getIndex());
            super.insertString(offs, str, a);
        } catch (ParseException e) {
            Toolkit.getDefaultToolkit().beep();
            System.err.println("insertString: could not parse: "
                               + proposedResult);
        }
    }

    public void remove(int offs, int len) throws BadLocationException {
        String currentText = getText(0, getLength());
        String beforeOffset = currentText.substring(0, offs);
        String afterOffset = currentText.substring(len + offs,
                                                   currentText.length());
        String proposedResult = beforeOffset + afterOffset;

        try {
            if (proposedResult.length() != 0)
                format.parseObject(proposedResult);
            super.remove(offs, len);
        } catch (ParseException e) {
            Toolkit.getDefaultToolkit().beep();
            System.err.println("remove: could not parse: " + proposedResult);
        }
    }
}
