package placement;
import java.util.StringTokenizer;
import java.io.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class SCLayout extends PLayout {


	public SCLayout(String fname) {
                super(fname);
	}

        public SCLayout(BufferedReader in) throws IOException {
                super(in);
        }


  private int rowCount;
  private int rowHeight;
  private int channelHeight;

  public int getRowCount() { return rowCount; }

  public void setRowCount (int rc) { rowCount = rc; }

  public int getRowHeight() { return rowHeight; }

  public int getChannelHeight() { return channelHeight; }

  public int rowToY(int r) { return(r*(rowHeight + channelHeight)); }

  public void readLayout(BufferedReader in) throws IOException {
    String line;
      while ((line = in.readLine()) != null) {
        StringTokenizer t = new StringTokenizer(line);
        String kw = t.nextToken();
        if (kw.equals("rowheight")) rowHeight = Integer.parseInt(t.nextToken());
        else if (kw.equals("rowcount")) rowCount = Integer.parseInt(t.nextToken());
        else if (kw.equals("channelheight")) channelHeight = Integer.parseInt(t.nextToken());
        else if (kw.equals("scmodule")) addModule(SCModule.parseModule(t));
        else if (kw.equals("terminal")) {
          PTerminal.parseTerminal(t, this);
        }
        else if (kw.equals("net")) PNet.parseNet(t, this);
        else System.out.println("Unrecognized object: " + kw);
      }
  }
}
