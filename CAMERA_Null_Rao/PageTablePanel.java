/*
 * FILE:	PageTablePanel.java
 * AUTHOR:	Karishma Rao
 * DATE:	February 5th, 2003
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

//CLASS FOR THE TLB PANEL
class PageTablePanel extends JPanel {

	//DECLARE THE COMPONENTS

	//BOOLEAN ARRAY OF 8 TO INDICATE WHETHER A ROW IS TO BE HIGHLIGHTED OR NOT
	boolean[] boolRows = new boolean[8];
	
	//STRING ARRAYS OF 8 TO HOLD THE VALUES OF THE FRAME NUMBER AND VALID BIT FOR EACH ROW
	String[] frameNum = new String[8];
	String[] validBit = new String[8];

	//DATA MEMBERS USED AS MEASUREMENTS FOR DRAWING THE ROWS
	private Dimension dP;
	private int dx, dy, offsetX, offsetY;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//CONSTRUCTOR	    
	public PageTablePanel(){

		//SET THE PROPERTIES OF THE PANEL
		setPreferredSize(new Dimension(340, 240));
		Border pageTableBorder= BorderFactory.createEtchedBorder();
		setBorder(BorderFactory.createTitledBorder(pageTableBorder, " Page Table ")); 

		//INITIALIZE THE STRING ARRAYS
		for (int i = 0; i < 8; i++){
			frameNum[i] = "-";
			validBit[i] = "0";
		}
	}//END CONSTRUCTOR

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//OVERRIDE THE paintComponent METHOD
	public void paintComponent(Graphics g){

		dP = this.getSize();             //GET THE DIMENSIONS OF THE PANEL
		dP.height -= 20;               
		dP.width -= 20;   
	
		//RESET THE BACKGROUND COLOR
		g.setColor(new Color(205, 205, 205));
		g.fillRect(0, 0, dP.width, dP.height);

		//EVALUATE THE HORIZONTAL PARAMETERS           
		dy = (int)(dP.height/9);        //8 ROWS AND THE HEADER
		int temp = dy*9;
		offsetY = 18;
		
		dx = (int) ((dP.width-20)/2);
		offsetX = 20;	     //USED TO HORIZONTALY CENTER THE DRAWING IN THE PANEL
		
		//DRAW THE 8 ROWS PLUS THE HEADER ROW
		g.setColor(Color.black);
		for (int i = 0; i < 10; i++)
			//HORIZONTAL LINES TO DRAW THE ROWS
			g.drawLine(offsetX, offsetY + dy*i, offsetX + 2*dx, offsetY + dy*i);

		//DRAW THE ROW CONTENTS
		g.drawString("Frame Number", offsetX + 33, offsetY+17);
		g.drawString("Valid Bit", offsetX + dx + 50, offsetY+17);
		
		for (int i = 0; i < 8; i++){	
			g.drawString(""+i, offsetX/2, offsetY + dy*(i+1) + 17);
			g.drawString(frameNum[i], offsetX+dx/2, offsetY + dy*(i+1) + 17);
			g.drawString(validBit[i], offsetX+3*dx/2, offsetY + dy*(i+1) + 17);
		}

		//DRAW THE 3 VERTICAL LINES
		g.drawLine(offsetX, offsetY, offsetX, offsetY + temp);
		g.drawLine(offsetX + dx, offsetY, offsetX + dx, offsetY + temp);
		g.drawLine(offsetX + 2*dx, offsetY, offsetX + 2*dx, offsetY + temp);	

		
		//HIGHLIGHT ROW IF REQUIRED
		for (int i = 1; i < 9; i++){
			if (boolRows[i-1]){
				g.setColor(Color.pink);
				g.fillRect(offsetX+1, offsetY+1 + dy*i, dx-1, dy-1);
				g.fillRect(offsetX+1+dx, offsetY+1+dy*i, dx-1, dy-1);
				
				g.setColor(Color.black);
				g.drawString(frameNum[i-1], offsetX+dx/2, offsetY + dy*i +17);
				g.drawString(validBit[i-1], offsetX+3*dx/2, offsetY + dy*i +17);
				
				break;
			}
		}
	
	}//END FUNCTION paintComponent

}//END CLASS PageTablePanel