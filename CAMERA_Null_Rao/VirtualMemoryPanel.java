import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

//CLASS FOR THE VIRTUAL MEMORY PANEL
class VirtualMemoryPanel extends JPanel {

	//DECLARE THE COMPONENTS

	//BOOLEAN ARRAY OF 8 TO INDICATE WHETHER A PAGE IS TO BE HIGHLIGHTED OR NOT
	boolean[] boolPages = new boolean[8];

	//DATA MEMBERS USED AS MEASUREMENTS FOR DRAWING THE LINES FOR THE VIRTUAL MEMORY PAGES
	private Dimension dV;
	private int dx, dy, offsetX, offsetY;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//CONSTRUCTOR	    
	public VirtualMemoryPanel(){

		//SET THE PROPERTIES OF THE PANEL
		setPreferredSize(new Dimension(395, 200));
		Border virtualMemBorder= BorderFactory.createEtchedBorder();
		setBorder(BorderFactory.createTitledBorder(virtualMemBorder, " Virtual Memory ")); 
		
	}//END CONSTRUCTOR

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//OVERRIDE THE paintComponent METHOD
	public void paintComponent(Graphics g){

		dV = this.getSize();             //GET THE DIMENSIONS OF THE PANEL
		dV.height -= 20;               
		dV.width -= 20;   
	
		//RESET THE BACKGROUND COLOR
		g.setColor(new Color(205, 205, 205));
		g.fillRect(0, 0, dV.width, dV.height);

		//EVALUATE THE HORIZONTAL PARAMETERS           
		dy = (int)(dV.height/8);        //8 PAGE MEMORY
		int temp = dy*8;
		offsetY = 14;
		offsetX = (int) (dV.width/2 - 40);	     //USED TO HORIZONTALY CENTER THE DRAWING IN THE PANEL
		
		//DRAW THE 8 PAGES
		g.setColor(Color.black);
		for (int i = 0; i < 9; i++)
			//HORIZONTAL LINES TO DRAW THE PAGES
			g.drawLine(offsetX, offsetY + dy*i, offsetX + 80, offsetY + dy*i);

		for (int i = 0; i < 8; i++)
			//DRAW THE PAGE LABELS    
			g.drawString("Page "+i, offsetX+20, offsetY + dy*i + 18);		

		//DRAW THE 2 VERTICAL LINES
		g.drawLine(offsetX, offsetY, offsetX, offsetY + temp);
		g.drawLine(offsetX + 80, offsetY, offsetX + 80, offsetY + temp);	

		//HIGHLIGHT PAGE IF REQUIRED
		for (int i = 0; i < 8; i++){
			if (boolPages[i]){
				g.setColor(Color.yellow);
				g.fillRect(offsetX+1, offsetY+1 + dy*i, 79, dy - 1);

				//DRAW THE PAGE LABELS
				g.setColor(Color.black);
				g.drawString("Page "+i, offsetX+20, offsetY + dy*i +18);
			}
		}
	
	}//END FUNCTION paintComponent

}//END CLASS VirtualMemoryPanel
