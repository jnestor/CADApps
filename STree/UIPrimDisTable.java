/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author 15002
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JTextPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class UIPrimDisTable extends UITablePanel {

    private final ConcurrentSkipListSet<STEdge> edgeTable;
    private final STGraph graphData;
    private final CopyOnWriteArrayList<STNode> nodes;
    private final Timer tableThread = new Timer(5, (ActionEvent evt) -> {
        refreshTable();
    });

    public UIPrimDisTable(STGraph tree) {
        super("Candidates", "Length     ");
        graphData = tree;
        nodes = graphData.getNodes();
        edgeTable = new ConcurrentSkipListSet<STEdge>();
    }

    @Override
    public void refreshTable() {
        empty();
        if (!nodes.isEmpty()) {
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).isVisited()) {
                    for (int j = 0; j < nodes.size(); j++) {
                        if (j != i && !(nodes.get(i).isVisited() && nodes.get(j).isVisited())) {
                            STEdge edge = new STEdge(nodes.get(i), nodes.get(j), false);
                            if (!edgeTable.contains(edge)) {
                                edgeTable.add(edge);
                            }
                        }
                    }
                }
            }
        }
        while (!edgeTable.isEmpty()) {
            STEdge edge = edgeTable.pollFirst();
            append(edge.toTableString(), Integer.toString(edge.length()),new Color (0, 138, 36),Color.blue);
        }
    }

    @Override
    public void empty() {
        edgeTable.clear();
        super.empty();
    }


    public void stop() {
        tableThread.stop();
    }

    public void highlight() {
        highlight(Color.YELLOW);
    }

}
