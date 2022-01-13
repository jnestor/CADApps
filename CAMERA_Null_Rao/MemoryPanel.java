/**
 * FILE:	MemoryPanel.java
 * AUTHOR:	Karishma Rao
 * DATE:	December 2nd, 2002
 */


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

//CLASS FOR THE MEMORY PANEL
class MemoryPanel extends JPanel{

	//BOOLEAN ARRAY OF 32 TO INDICATE WHETHER A MEMORY BLOCK IS TO BE HIGHLIGHTED OR NOT
	boolean[] boolBlocks = new boolean[32];

	//BOOLEAN ARRAY OF 8 TO INDICATE WHETHER A MEMORY WORD IS TO BE HIGHLIGHTED OR NOT
	boolean[] boolWords = new boolean[8];

	//DATA MEMBERS USED AS MEASUREMENTS TO DRAW THE MEMORY BLOCKS AND WORDS
	Dimension dM;
	int dxM, dyM, offsetXM, offsetYM;

	//STRING ARRAY OF HEX VALUES USED TO LABEL THE MEMORY BLOCKS IN THE PANEL
	String[] memLabel = {"00", "08", "10", "18", "20", "28", "30", "38", "40", "48", "50", "58", "60",
						 "68", "70", "78", "80", "88", "90", "98", "A0", "A8", "B0", "B8", 
						 "C0", "C8","D0", "D8", "E0", "E8", "F0", "F8"};

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//CONSTRUCTOR
	public MemoryPanel(){

		//SET THE PROPERTIES OF THE PANEL
		Border memBorder = BorderFactory.createEtchedBorder();
		setBorder(BorderFactory.createTitledBorder(memBorder, " Memory ")); 

	}//END CONSTRUCTOR	

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//OVERRIDE THE paintComponent METHOD
	public void paintComponent(Graphics g){
		
		/* TO CALCULATE THE MEASUREMENTS FOR DRAWING THE BLOCKS AND WORDS,
		* THE DIMENSION OF THE PANEL IS RETRIEVED, THEN THE HEIGHT AND WIDTH ARE REDUCED BY 20 PIXELS EACH
		* TO ACCOUNT FOR THE BORDER. ALSO, THE HEIGHT IS FURTHER REDUCED TO ACCOUNT FOR THE WORD LABELS
		* AND THE WIDTH IS FURTHER REDUCED LATER TO ACCOUNT FOR BLOCK LABELS.
		* THE BLOCK AND WORD DIMENSIONS ARE THEN CALCULATED BASED ON THE AREA LEFT.
		*/

		//15 PIXELS FOR WORD LABELS AND 20 FOR BLOCK LABELS
		dM = this.getSize();
		dM.height -= 20;
		dyM = (int)((dM.height-15)/32);        //32 BLOCK MEMORY
		int temp2 = dyM*32;
		offsetYM = (int)((dM.height - temp2 - 15)/2);   //USED TO VERTICALLY CENTER THE DRAWING IN THE PANEL

		dM.width -= 20;
		dxM = (int) ((dM.width- 20)/8);       //8 WORDS PER BLOCK
		int temp3 = dxM*8;
		offsetXM = (int)((dM.width - temp3 - 20)/2);   //USED TO HORIZONTALLY CENTER THE DRAWING IN THE PANEL

		//RESET THE BACKGROUND COLOR
		g.setColor(new Color(205, 205, 205));
		g.fillRect(0, 0, dM.width, dM.height);

		//DRAW THE 32 MEMORY BLOCKS WITH 8 WORDS PER BLOCK
		g.setColor(Color.black);
		for (int i = 0; i < 33; i++){

			//HORIZONTAL LINES TO DRAW THE BLOCKS
			g.drawLine(offsetXM + 25, offsetYM + dyM*i+ 30, offsetXM + 25 + dxM*8, offsetYM + dyM*i + 30);
		}//END FOR

		for (int i = 0; i < 9; i++) {

			//DRAW THE WORD LABELS
			g.drawString("+"+i, offsetXM + 15 + dxM/2 + dxM*i, 31);   

			//VERTICAL LINES TO DIVIDE THE BLOCKS INTO WORDS
			g.drawLine(offsetXM + dxM*i + 25, offsetYM + 30, offsetXM + dxM*i + 25, dM.height - offsetYM -1 + 15);
		}//END FOR

		//BLOCK LABELS
		for (int i = 0; i < 32; i++)

			//DRAW THE BLOCK LABELS
			g.drawString(memLabel[i], offsetXM + 6, offsetYM + dyM*(i+1) + dyM/2 + 19);

		//INSERT INITIAL DATA IN MAIN MEMORY - Bi Wj
		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 8; j++) {

				//DRAW THE BLOCK AND WORD VALUES INSIDE THE WORDS
				g.drawString("B"+(i)+" W"+(j), offsetXM + dxM*j + 28, offsetYM + dyM*i + 45);
			}//END FOR
		}//END FOR

		//HIGHLIGHT A BLOCK, IF REQUIRED
		for (int j = 0; j < 32; j++){
			if (boolBlocks[j]){
				g.setColor(Color.blue);
				for (int i = 0; i < 8; i++){
					g.fillRect(offsetXM + dxM*i + 25, offsetYM + dyM*(j+1) + 14, dxM, dyM);

					//HIGHLIGHT THE REQUIRED WORD IN BLACK
					if (boolWords[i]){
						g.setColor(Color.black);
						g.fillRect(offsetXM + dxM*i + 25, offsetYM + dyM*(j+1) + 14, dxM, dyM);
						g.setColor(Color.blue);
					}
				}
				//WRITE THE DATA IN HIGHLIGHTED MEMORY BLOCK IN WHITE
				g.setColor(Color.white);
				for (int i = 0; i < 8; i++)
					g.drawString("B"+j+" W"+i, offsetXM + 20 + dxM*i + 8,
								 offsetYM + 15 + dyM*(j+1) + 13);
			}
		}

	}//END FUNCTION paintComponent

}//END CLASS MemoryPanel

