
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
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
public class UISteinerBOITable extends UITablePanel {

    private STBOI boiData;
    

    public UISteinerBOITable(STBOI data) {
        super("Candidates", "Gain");
        boiData = data;
    }

    @Override
    public void refreshTable() {
            empty();
            ArrayList<STNEPair> arr=new ArrayList<STNEPair>();
            for(STNEPair a :boiData.getMods()){
                arr.add(a);
            }
            Collections.sort(arr);
            for (STNEPair a : arr) {
                append(a.toTableString(), Integer.toString(a.getGain()));
            }
            repaint();
    }
    
    
    

}
