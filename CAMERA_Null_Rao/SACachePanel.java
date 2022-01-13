/**
 * FILE: SACachePanel.java
 * AUTHOR: Karishma Rao
 * DATE: February 16th, 2003
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

//CLASS FOR THE CACHE PANEL
class SACachePanel extends JPanel {

	//DECLARE THE COMPONENTS

	//BOOLEAN ARRAYS TO INDICATE WHETHER A CACHE BLOCK IS TO BE HIGHLIGHTED OR NOT
	boolean[][] boolBlocks2Way = new boolean[8][2];
	boolean[][] boolBlocks4Way = new boolean[4][4];

	//BOOLEAN ARRAY TO INDICATE WHETHER A WORD IN A BLOCK IS TO BE HIGHLIGHTED OR NOT
	boolean[] boolWords = new boolean[8];

	//STRING ARRAYS TO HOLD THE VALUE OF THE MEMORY BLOCK THAT IS IN A PARTICULAR CACHE BLOCK
	String[][] stringBlocks2Way = new String[8][2];
	String[][] stringBlocks4Way = new String[4][4];

	//STRING ARRAYS TO HOLD THE VALUE OF THE TAG ASSOCIATED WITH A BLOCK
	String[][] tag2Way = new String[8][2];
	String[][]tag4Way = new String[4][4];
	
	//INT TO INDICATE THE CHOICE OF NUMBER OF WAYS IN CACHE: 0->2-WAY; 1->4-WAY
	int numWays;

	//DATA MEMBERS USED AS MEASUREMENTS FOR DRAWING THE LINES FOR THE CACHE BLOCKS AND WORDS
	private Dimension dC;
	private int dx, dy, offsetX, offsetY;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//CONSTRUCTOR	    
	public SACachePanel(){

		//SET THE PROPERTIES OF THE PANEL
		setPreferredSize(new Dimension(250, 550));
		Border cacheBorder= BorderFactory.createEtchedBorder();
		setBorder(BorderFactory.createTitledBorder(cacheBorder, " Set Associative Cache "));  
		
		//INITIALIZE THE ARRAYS THAT SHOW THE BLOCK IN MEMORY AND THE TAG ASSOCIATED WITH EACH CACHE BLOCK
		for (int i = 0; i < 8; i++){
			for (int j = 0; j < 2; j++){
				stringBlocks2Way[i][j] = "";
				tag2Way[i][j] = "";
			}
		}
		
		for (int i = 0; i < 4; i++){
			for (int j = 0; j < 4; j++){
				stringBlocks4Way[i][j] = "";
				tag4Way[i][j] = "";
			}
		}

	}//END CONSTRUCTOR

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//OVERRIDE THE paintComponent METHOD
	public void paintComponent(Graphics g){
		
		/* TO CALCULATE THE MEASUREMENTS FOR DRAWING THE BLOCKS AND WORDS,
		* THE DIMENSION OF THE PANEL IS RETRIEVED, THEN THE HEIGHT AND WIDTH ARE REDUCED BY 20 PIXELS EACH
		* TO ACCOUNT FOR THE BORDER. ALSO, THE WIDTH IS FURTHER REDUCED LATER TO ACCOUNT FOR SET AND BLOCK LABELS.
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
		//ACCOUNT FOR 36 PIXELS FOR THE BLOCK AND SET LABEL, 24 FOR THE TAG, THEN THE REST IS FOR THE 8 WORDS PER BLOCK
		dx = (int) ((dC.width-60)/8);    //8 WORDS PER BLOCK
		int temp1 = dx*8;
		offsetX = (int)((dC.width - 60 - temp1)/2);   //USED TO HORIZONTALLY CENTER THE DRAWING IN THE PANEL
		
		//DRAW THE 16 CACHE BLOCKS WITH A TAG PER BLOCK AND 8 WORDS PER BLOCK
		g.setColor(Color.black);
		for (int i = 0; i < 16; i++){
			
			//HORIZONTAL LINES TO DRAW THE TAGS AND BLOCKS
			g.drawLine(offsetX+36+10, offsetY+15+dy*i, dC.width+10-offsetX, offsetY+15+dy*i);
			
			//HORIZONTAL LINES TO DRAW THE TAGS
			g.drawLine(offsetX+36+10, offsetY+15+dy*i+dy/2, 60+10+offsetX, offsetY+15+dy*i+dy/2);

			//VERTICAL LINES TO DRAW THE TAGS
			g.drawLine(offsetX+36+10, offsetY+15+dy*i, offsetX+36+10, offsetY+15+dy*i+dy/2);	
			
		}//END FOR	
		
		//LAST HORIZONTAL LINE IN SET STRETCHES FROM BEGINNING OF BLOCK TO END OF BLOCK
		g.drawLine(offsetX+60+10, offsetY+15+dy*16, dC.width+10-offsetX, offsetY+15+dy*16);				
		
		for (int i = 0; i < 9; i++) {

			//VERTICAL LINES TO DIVIDE THE BLOCKS INTO WORDS
			g.drawLine(offsetX+60+10+dx*i, offsetY+15, offsetX+60+10+dx*i, dC.height+15-offsetY);
		}//END FOR		
	
		if (numWays == 0){ //NUMBER OF WAYS IS 2
			
			//HIGHLIGHT BLOCK IF REQUIRED
			for (int i = 0; i < 8; i++){
				for (int k = 0; k < 2; k++){
					if (boolBlocks2Way[i][k]){

						//HIGHLIGHT THE TAG
						g.setColor(Color.orange);
						g.fillRect(offsetX + 37 +10, offsetY + dy*(2*i +k) + 1+15, 23, dy/2 - 1);
						g.setColor(Color.yellow);

						//HIGHLIGHT THE BLOCK
						for (int j = 0; j < 8; j++){
							g.fillRect(61 + 10 + offsetX + dx*j, 15 + offsetY + dy*(2*i+k) + 1, dx - 1, dy - 1);

							//HIGHLIGHT THE WORD
							if (boolWords[j] == true){
								g.setColor(Color.red);
								g.fillRect(61 + offsetX + dx*j+10, offsetY + dy*(2*i+k) + 1+15, dx-1, dy-1);
								g.setColor(Color.yellow);
							}
						}
					}
						//IF THE CACHE ALREADY HAD DATA IN IT, THEN REWRITE IT ON THE SCREEN
						if (!stringBlocks2Way[i][k].equals("")){
							g.setColor(Color.black);
							for (int j = 0; j < 8; j++){
								g.drawString("B"+stringBlocks2Way[i][k],61+10+offsetX+dx*j, offsetY+dy*(2*i+k)+15+15);
								g.drawString("W"+j, 61+10+offsetX+dx*j+2, offsetY+dy*(2*i+k)+26+15);
							}
							g.drawString(tag2Way[i][k], offsetX+50, offsetY+dy*(2*i+k)+14+15);
						}
					
				}
			}			
			g.setColor(Color.black);
			//INDICATE THE 2 BLOCKS THAT BELONG TO A SET
			for (int i = 1; i < 58; i+=8){
				g.drawLine(offsetX+41, offsetY+i*dy/4+5, offsetX+43, offsetY+i*dy/4+5);
				g.drawLine(offsetX+41, offsetY+i*dy/4+5, offsetX+41, offsetY+(i+6)*dy/4+10);
				g.drawLine(offsetX+41, offsetY+(i+6)*dy/4+10, offsetX+43, offsetY+(i+6)*dy/4+10);
			}
			
			for (int i = 0; i < 16; i++){
				//DRAW THE SET AND BLOCK LABELS    
				g.drawString("Set"+(i/2), offsetX+10, offsetY+dy*i+10+15);
				g.drawString("Blk"+(i%2), offsetX+10, offsetY+dy*i+22+15);	
			}
			
		}//END IF NUMBER OF WAYS = 2
		
		if (numWays == 1){ //NUMBER OF WAYS IS 4
			
			//HIGHLIGHT BLOCK IF REQUIRED
			for (int i = 0; i < 4; i++){
				for (int k = 0; k < 4; k++){
					if (boolBlocks4Way[i][k]){

						//HIGHLIGHT THE TAG
						g.setColor(Color.orange);
						g.fillRect(offsetX + 37 +10, offsetY + dy*(4*i +k) + 1+15, 23, dy/2 - 1);
						g.setColor(Color.yellow);

						//HIGHLIGHT THE BLOCK
						for (int j = 0; j < 8; j++){
							g.fillRect(61 + 10 + offsetX + dx*j, 15 + offsetY + dy*(4*i+k) + 1, dx - 1, dy - 1);

							//HIGHLIGHT THE WORD
							if (boolWords[j] == true){
								g.setColor(Color.red);
								g.fillRect(61 + offsetX + dx*j+10, offsetY + dy*(4*i+k) + 1+15, dx-1, dy-1);
								g.setColor(Color.yellow);
							}
						}
					}
					//IF THE CACHE ALREADY HAD DATA IN IT, THEN REWRITE IT ON THE SCREEN
					if (!stringBlocks4Way[i][k].equals("")){
						g.setColor(Color.black);
						for (int j = 0; j < 8; j++){
							g.drawString("B"+stringBlocks4Way[i][k],61+10+offsetX+dx*j, offsetY+dy*(4*i+k)+15+15);
							g.drawString("W"+j, 61+10+offsetX+dx*j+2, offsetY+dy*(4*i+k)+26+15);
						}
						g.drawString(tag4Way[i][k], offsetX+48, offsetY+dy*(4*i+k)+15+15);
					}
					
				}
			}		
			
			g.setColor(Color.black);
			//INDICATING THE 4 BLOCKS THAT BELONG TO A SET
			for (int i = 1; i < 50; i+=16){
				g.drawLine(offsetX+41, offsetY+i*dy/4+5, offsetX+43, offsetY+i*dy/4+5);
				g.drawLine(offsetX+41, offsetY+i*dy/4+5, offsetX+41, offsetY+(i+14)*dy/4+10);
				g.drawLine(offsetX+41, offsetY+(i+14)*dy/4+10, offsetX+43, offsetY+(i+14)*dy/4+10);
			}
			
			for (int i = 0; i < 16; i++){
				//DRAW THE SET AND BLOCK LABELS    
				g.drawString("Set"+(i/4), offsetX+10, offsetY+dy*i+10+15);
				g.drawString("Blk"+(i%4), offsetX+10, offsetY+dy*i+22+15);	
			}		
			
		}//END IF NUMBER OF WAYS = 4			
		
	}//END FUNCTION paintComponent

}//END CLASS SACachePanel
