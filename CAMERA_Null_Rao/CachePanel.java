/**
 * FILE:	CachePanel.java
 * AUTHOR:	Karishma Rao
 * DATE:	February 16th, 2003
 */

/**
 * This class does not implement previous state storage of the cache blocks.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

//CLASS FOR THE CACHE PANEL
class CachePanel extends JPanel {

	//DECLARE THE COMPONENTS

	//BOOLEAN ARRAY OF 16 TO INDICATE WHETHER A CACHE BLOCK IS TO BE HIGHLIGHTED OR NOT
	boolean[] boolBlocks = new boolean[16];

	//BOOLEAN ARRAY OF 8 TO INDICATE WHETHER A WORD IN A BLOCK IS TO BE HIGHLIGHTED OR NOT
	boolean[] boolWords = new boolean[8];

	//BOOLEAN ARRAY OF 16 TO INDICATE WHETHER A TAG IN THE CACHE IS TO BE HIGHLIGHTED OR NOT
	boolean[] boolTags = new boolean[16];

	//STRING ARRAY OF 16 TO HOLD THE VALUE OF THE MEMORY BLOCK THAT IS IN A PARTICULAR CACHE BLOCK
	String[] stringBlocks = new String[16];

	//STRING ARRAY OF 16 TO HOLD THE VALUE OF THE TAG ASSOCIATED WITH A BLOCK
	String[] tag = new String[16];

	//DATA MEMBERS USED AS MEASUREMENTS FOR DRAWING THE LINES FOR THE CACHE BLOCKS AND WORDS
	private Dimension dC;
	private int dx, dy, offsetX, offsetY;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//CONSTRUCTOR	    
	public CachePanel(){

		//SET THE PROPERTIES OF THE PANEL
		setPreferredSize(new Dimension(250, 550));
		Border cacheBorder= BorderFactory.createEtchedBorder();
		setBorder(BorderFactory.createTitledBorder(cacheBorder, " Cache ")); 
		
		//INITIALIZE THE ARRAYS THAT SHOW THE BLOCK IN MEMORY AND THE TAG ASSOCIATED WITH EACH CACHE BLOCK
		for (int i = 0; i < 16; i++){		
			stringBlocks[i] = "";
			tag[i] = "";		
		}

	}//END CONSTRUCTOR

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//OVERRIDE THE paintComponent METHOD
	public void paintComponent(Graphics g){

		/* TO CALCULATE THE MEASUREMENTS FOR DRAWING THE BLOCKS AND WORDS,
		* THE DIMENSION OF THE PANEL IS RETRIEVED, THEN THE HEIGHT AND WIDTH ARE REDUCED BY 20 PIXELS EACH
		* TO ACCOUNT FOR THE BORDER. ALSO, THE WIDTH IS FURTHER REDUCED LATER TO ACCOUNT FOR BLOCK LABELS.
		* THE BLOCK, WORD AND TAG DIMENSIONS ARE THEN CALCULATED BASED ON THE AREA LEFT.
		*/
		dC = this.getSize();             //GET THE DIMENSIONS OF THE PANEL
		dC.height -= 20;               
		dC.width -= 20;   

		//EVALUATE THE HORIZONTAL PARAMETERS           
		dy = (int)(dC.height/16);        //16 BLOCK CACHE
		int temp = dy*16;
		offsetY = (int)((dC.height - temp)/2);   //USED TO VERTICALLY CENTER THE DRAWING IN THE PANEL

		//EVALUATE THE VERTICAL PARAMETERS
		//ACCOUNT FOR 36 PIXELS FOR THE BLOCK LABEL, 20 FOR THE TAG, THEN THE REST IS FOR THE 8 WORDS PER BLOCK
		dx = (int) ((dC.width-56)/8);    //8 WORDS PER BLOCK
		int temp1 = dx*8;
		offsetX = (int)((dC.width - 56 - temp1)/2);   //USED TO HORIZONTALLY CENTER THE DRAWING IN THE PANEL
		
		//DRAW THE 16 CACHE BLOCKS WITH A TAG PER BLOCK AND 8 WORDS PER BLOCK
		g.setColor(Color.black);
		for (int i = 0; i < 16; i++){

			//HORIZONTAL LINES TO DRAW THE TAGS AND BLOCKS
			g.drawLine(offsetX+36+10, offsetY+15+dy*i, dC.width+10-offsetX, offsetY+15+dy*i);

			//HORIZONTAL LINES TO DRAW THE TAGS
			g.drawLine(offsetX+36+10, offsetY+15+dy*i+dy/2, 56+10+offsetX, offsetY+15+dy*i+dy/2);

			//VERTICAL LINES TO DRAW THE TAGS
			g.drawLine(offsetX+36+10, offsetY+15+dy*i, offsetX+36+10, offsetY+15+dy*i+dy/2);
			
			//DRAW THE BLOCK LABELS    
			g.drawString("Blk"+i, offsetX+10, offsetY+dy*i+10+15);
		}//END FOR

		//LAST HORIZONTAL LINE STRETCHES FROM BEGINNING OF BLOCK TO END OF BLOCK
		g.drawLine(offsetX+56+10, offsetY+15+dy*16, dC.width+10-offsetX, offsetY+15+dy*16);
		
		for (int i = 0; i < 9; i++) {

			//VERTICAL LINES TO DIVIDE THE BLOCKS INTO WORDS
			g.drawLine(offsetX+56+10+dx*i, offsetY+15, offsetX+56+10+dx*i, dC.height+15-offsetY);
		}//END FOR

		//HIGHLIGHT BLOCK IF REQUIRED
		for (int i = 0; i < 16; i++){
			if (boolBlocks[i]){

				//HIGHLIGHT THE TAG
				g.setColor(Color.orange);
				g.fillRect(offsetX + 37 +10, offsetY + dy*i + 1+15, 19, dy/2 - 1);
				g.setColor(Color.yellow);

				//HIGHLIGHT THE BLOCK
				for (int j = 0; j < 8; j++){
					g.fillRect(57 + 10 + offsetX + dx*j, 15 + offsetY + dy*i + 1, dx - 1, dy - 1);

					//HIGHLIGHT THE WORD
					if (boolWords[j] == true){
						g.setColor(Color.red);
						g.fillRect(57 + offsetX + dx*j+10, offsetY + dy*i + 1+15, dx-1, dy-1);
						g.setColor(Color.yellow);
					}
				}
			}

			//IF THE CACHE ALREADY HAD DATA IN IT, THEN REWRITE IT ON THE SCREEN
				if (!stringBlocks[i].equals("")){
					g.setColor(Color.black);
					for (int j = 0; j < 8; j++){
						g.drawString("B"+stringBlocks[i],57+10+offsetX+dx*j, offsetY+dy*i+15+15);
						g.drawString("W"+j, 57+10+offsetX+dx*j+2, offsetY+dy*i+26+15);
					}
					g.drawString(tag[i], offsetX+44+10, offsetY+dy*i+15+15);
				}
			
		}
		
		
	}//END FUNCTION paintComponent

}//END CLASS CachePanel
