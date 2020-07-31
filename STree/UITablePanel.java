
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author 15002
 */
public abstract class UITablePanel extends JPanel {

    private final JTextPane edgeNames = new JTextPane();
    private final JTextPane edgeDistances = new JTextPane();
    private final JPanel titlePanel = new JPanel();
    private final JPanel contentPanel = new JPanel();
    private final Timer tableThread = new Timer(5, (ActionEvent evt) -> {
        refreshTable();
    });

    public UITablePanel(String a, String b) {
        initWindow(a, b);
    }

    abstract void refreshTable();

    public void empty() {
        stop();
        edgeNames.setText("");
        edgeDistances.setText("");
        repaint();
    }

    public boolean refresh() throws InterruptedException {
        tableThread.setRepeats(false);
        tableThread.start();
        return true;
    }

    public void stop() {
        tableThread.stop();
    }

    public void highlight(Color c) {
        highlight(edgeNames, c);
        highlight(edgeDistances, c);
    }

    public void highlight(Color c, String s1, String s2) throws InterruptedException {
        while(tableThread.isRunning()){
            wait(10);
            System.out.println("Wait");
        }
            Document doc = edgeNames.getDocument();
            Document doc2 = edgeDistances.getDocument();
        try {
            highlight(edgeNames, c, s1);
            int index = doc.getText(0, doc.getLength()).indexOf(s1);
            int lineNum = getLineCount(doc.getText(0, doc.getLength()), index);
            int lineIndex = getLineIndex(doc2.getText(0, doc2.getLength()), lineNum);
            highlight(edgeDistances, c, lineIndex, lineIndex + s2.length());
            //highlight(edgeDistances,c,s2);
        } catch (Exception ex) {
            Logger.getLogger(UITablePanel.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(s1);
            System.out.println(s2);
            try {
                System.out.println(doc.getText(0, doc.getLength()));
            } catch (BadLocationException ex1) {
                Logger.getLogger(UITablePanel.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    private void highlight(JTextPane pane, Color c) {
        if (!pane.getText().isEmpty()) {
            Highlighter hilit = new DefaultHighlighter();
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(c);
            pane.setHighlighter(hilit);
            try {
                hilit.addHighlight(0, pane.getText().indexOf("\n"), painter);
            } catch (BadLocationException ex) {
                Logger.getLogger(UIPrimDisTable.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println(pane.getText().indexOf("\n"));
            }
        }
    }

    private void highlight(JTextPane pane, Color c, String s) throws BadLocationException {
        if (!pane.getText().isEmpty()) {
            Highlighter hilit = new DefaultHighlighter();
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(c);
            pane.setHighlighter(hilit);
            Document doc = pane.getDocument();
                int index = doc.getText(0, doc.getLength()).indexOf(s);
                hilit.addHighlight(index, index + s.length(), painter);
        }
    }

    private void highlight(JTextPane pane, Color c, int start, int end) {
        if (!pane.getText().isEmpty()) {
            Highlighter hilit = new DefaultHighlighter();
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(c);
            pane.setHighlighter(hilit);
            try {
                hilit.addHighlight(start, end, painter);
            } catch (BadLocationException ex) {
            }
        }
    }

    private int getLineCount(String s, int end) {
        int i = s.indexOf("\n");
        int last = s.lastIndexOf("\n");
        String sub = s.substring(0, end);
        int lineCount = 0;
        while (i < end && i < last && i >= 0) {
            lineCount++;
            i = sub.indexOf("\n", i + 1);
        }
        return lineCount;
    }

    private int getLineIndex(String s, int lineNum) {
        int lineCount = 0;
        int i = 0;
        while (lineCount < lineNum && i >= 0) {
            lineCount++;
            i = s.indexOf("\n", i + 1);

        }
        return i + 1;
    }

    protected void initWindow(String a, String b) {
        setPreferredSize(new Dimension(220, 600));
        DefaultCaret caret = (DefaultCaret) edgeNames.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        caret = (DefaultCaret) edgeDistances.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        edgeNames.setEditable(false);
        edgeDistances.setEditable(false);
        edgeNames.setBackground(new Color(238, 238, 238));
        edgeDistances.setBackground(new Color(238, 238, 238));
        JScrollPane scrollLeft = new JScrollPane(edgeNames, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane scrollRight = new JScrollPane(edgeDistances, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollLeft.getHorizontalScrollBar().setModel(scrollRight.getHorizontalScrollBar().getModel());
        scrollLeft.getVerticalScrollBar().setModel(scrollRight.getVerticalScrollBar().getModel());
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        setLayout(new BorderLayout());
        titlePanel.add(new JLabel(a, JLabel.CENTER));
        titlePanel.add(new JLabel(b, JLabel.CENTER));
        titlePanel.setPreferredSize(new Dimension(220, 32));
        contentPanel.add(scrollLeft);
        contentPanel.add(scrollRight);
        titlePanel.setLayout(new GridLayout(1, 2));
        contentPanel.setLayout(new GridLayout(1, 2));
        //contentPanel.setBorder(new EmptyBorder(0, 0, 59, 0));
        add(titlePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        JPanel ghost = new JPanel();
        ghost.setPreferredSize(new Dimension(400, 52));
        add(ghost, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    public void append(String a, String b) {
        try {
            StyledDocument nameDoc = (StyledDocument) edgeNames.getDocument();
            StyledDocument disDoc = (StyledDocument) edgeDistances.getDocument();
            nameDoc.insertString(nameDoc.getLength(), " " + a + " " + "\n", null);
            disDoc.insertString(disDoc.getLength(), " " + b + " " + "\n", null);
            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
            nameDoc.setParagraphAttributes(0, nameDoc.getLength(), center, false);
            disDoc.setParagraphAttributes(0, disDoc.getLength(), center, false);
        } catch (BadLocationException ex) {
            Logger.getLogger(UIPrimDisTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void append(String a, String b, Color c1, Color c2) {
        try {
            StyledDocument nameDoc = (StyledDocument) edgeNames.getDocument();
            StyledDocument disDoc = (StyledDocument) edgeDistances.getDocument();
            Style style = edgeNames.addStyle("I'm a Style", null);
            StyleConstants.setBold(style, true);
            int end = a.indexOf("-");
            nameDoc.insertString(nameDoc.getLength(), " " + a.substring(0, 1), null);
            StyleConstants.setForeground(style, c1);
            nameDoc.insertString(nameDoc.getLength(), " " + a.substring(1, end), style);
            nameDoc.insertString(nameDoc.getLength(), a.substring(end, end + 2), null);
            StyleConstants.setForeground(style, c2);
            nameDoc.insertString(nameDoc.getLength(), a.substring(end + 2, a.length() - 1), style);
            nameDoc.insertString(nameDoc.getLength(), a.substring(a.length() - 1, a.length()) + " " + "\n", null);
            disDoc.insertString(disDoc.getLength(), " " + b + " " + "\n", null);
            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
            nameDoc.setParagraphAttributes(0, nameDoc.getLength(), center, false);
            disDoc.setParagraphAttributes(0, disDoc.getLength(), center, false);
        } catch (BadLocationException ex) {
            Logger.getLogger(UIPrimDisTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
