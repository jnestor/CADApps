/**
 * FILE: DMCFrame.java
 * AUTHOR: Karishma Rao
 * DATE: February 16th, 2003
 */

/**
 * DESCRIPTION: Implements the interactive tutorial to demonstrate the working of Direct Cache Mapping
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Vector;

//CLASS DMCFrame IS THE MAIN FRAME OF THE APPLICATION
class DMCFrame extends JFrame {

	//DECLARE ALL THE COMPONENTS

	//PANEL FOR DRAWING THE CACHE 
	private CachePanel cachePanel;  

	//PANEL FOR DRAWING THE MEMORY       
	private MemoryPanel memoryPanel;

	//PANELS TO CONTAIN THE PROGRESS BAR, NAVIGATION BUTTONS, CACHE AND CACHE HITS AND MISSES FIELDS
	private JPanel bottomPanel, cache, cacheHitsMisses;

	//PANELS TO CONTAIN THE ADDRESS REFERENCE STRING AND MAIN MEMORY ADDRESS BITS
	private JPanel pEastPanel, pAddRefStr, pAutoSelfGen, pBitsInMM1, pBitsInMM2, pBitsInMM;

	//NAVIGATION BUTTONS AND ADDRESS REFERENCE STRING GENERATION BUTTONS
	private JButton restart, next, back, quit, autoGen, selfGen;

	//LABELS FOR THE VARIOUS COMPONENTS
	private JLabel lCacheHits, lCacheMisses, lProgress, lBits1, lBits2;

	//TEXTFIELDS FOR CACHE HITS AND MISSES AND MAIN MEMORY ADDRESS BITS
	private JTextField tCacheHits, tCacheMisses, tTag, tBlock1, tBlock2, tWord1, tWord2;

	//TEXTAREA FOR PROGRESS UPDATE
	private JTextArea tProgress;

	//SCROLLPANES FOR PROGRESS UPDATE TEXTAREA AND ADDRESS REFERENCE STRING LISTBOX
	private JScrollPane progressScroll, addRefStrScroll;

	//LISTBOX FOR ADDRESS REFERENCE STRING
	private JList addRefStrList;

	//BORDERS FOR THE VARIOUS PANELS
	private Border cacheHMBorder, bitsInMM1Border, bitsInMM2Border, bitsInMMBorder, addRefStrBorder;

	//TEMP ARRAY USED TO GENERATE THE STRING OF 256 POSSSIBLE MEMORY ADDRESSES
	String[] tempAddress = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

	//ARRAY TO HOLD THE 256 POSSIBLE MEMORY ADDRESSES
	private String[] addresses = new String[256];

	//VECTOR USED TO MAINTAIN THE ADDRESSES IN THE REFERENCE STRING LIST
	private Vector listData = new Vector();

	//INT USED TO TRACK STEPPING THROUGH THE TUTORIAL (WHEN THE Next AND back BUTTONS ARE USED)
	private int moveStatus = 0;

	//INTS USED TO INDICATE THE ADDRESS REFERENCE STRING IN QUESTION
	private int evaluateIndex = 0;

	//INTS TO KEEP COUNT OF THE NUMBER OF CACHE HITS AND MISSES
	private int cacheHits, cacheMisses;

	//STRINGS USED TO CONVERT THE DECIMAL MEMORY ADDRESS VALUES INTO HEX AND BINARY
	private String hexAddress = new String();
	private String binAddress = new String();

	//INTS USED TO MAINTAIN THE DECIMAL VALUE OF THE MEMORY ADDRESS IN QUESTION - IN MAIN MEMORY AS WELL AS IN CACHE
	private int intBlockDec = 0;
	private int intWordDec = 0;
	private int intBlockDecMem = 0;

	//STRINGS USED TO MAINTAIN THE MAIN MEMORY ADDRESS BITS
	private String tag = new String();
	private String block = new String();
	private String word = new String();
	private String blockMem = new String();

	//STRINGS USED FOR CONVERSION OF ADDRESSES FROM DECIMAL - HEX - BIN
	private String blockDec = new String();
	private String wordDec = new String();
	
	//BOOLEAN USED TO KEEP TRACK OF WHETHER THE RESTART BUTTON WAS PRESSED - USED TO ENABLE Next BUTTON APPROPRIATELY
	private boolean reStarted = true;
	
	//BOOLEAN USED TO INDICATE WHETHER THE Next BUTTON WAS CLICKED OR THE Back BUTTON
	private boolean nextClicked = true;	

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//CONSTRUCTOR
	public DMCFrame(){

		//SET PROPERTIES OF THE MAIN FRAME
		setTitle("Direct Mapped Cache");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setIconImage(Toolkit.getDefaultToolkit().createImage(DMCFrame.class.getResource("cam.gif")));
	
		//CREATE COMPONENTS AND SET THEIR PROPERTIES

		//NAVIGATION BUTTONS
		restart = new JButton("Restart");
		next = new JButton("Next");
		back = new JButton("Back");
		quit = new JButton("Quit");

		//CACHE HITS AND MISSES INFO.
		lCacheHits = new JLabel("Cache Hits");
		lCacheMisses = new JLabel("Cache Misses");	
		tCacheHits = new JTextField(5);
		tCacheMisses = new JTextField(5);
		tCacheHits.setEditable(false);
		tCacheHits.setFont(new Font("Monospaced", Font.BOLD, 14));
		tCacheHits.setText("  0");
		tCacheMisses.setEditable(false);
		tCacheMisses.setFont(new Font("Monospaced", Font.BOLD, 14));
		tCacheMisses.setText("  0");

		//PROGRESS UPDATE AREA
		tProgress = new JTextArea(3, 45);
		tProgress.setEditable(false);
		tProgress.setLineWrap(true);
		tProgress.setWrapStyleWord(true);
		tProgress.setCaretPosition(0);
		tProgress.setFont(new Font("Serif", Font.BOLD + Font.ITALIC, 16));
		tProgress.setText("Welcome to Direct Mapped Cache!\nThe system specs are as follows -"
						  + "\n  16 Blocks in Cache\n  32 Blocks in Main Memory\n  8 Words per Block"
						  +"\nPlease generate the Address Reference String."
						  +"\nThen click on \"Next\" to continue.");
		progressScroll = new JScrollPane();
		progressScroll.getViewport().add(tProgress);
		progressScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		lProgress = new JLabel("PROGRESS UPDATE");

		//ADDRESS REFERENCE STRING
		addRefStrList = new JList();
		addRefStrList.setEnabled(false);
		addRefStrScroll = new JScrollPane();
		addRefStrScroll.getViewport().setView(addRefStrList);
		addRefStrScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		addRefStrScroll.setPreferredSize(new Dimension(140, 300));

		//BUTTONS USED TO ADDRESS GENERATION
		autoGen = new JButton("Auto Generate Add. Ref. Str.");
		selfGen = new JButton("Self Generate Add. Ref. Str.");

		//BITS IN MAIN MEMORY ADDRESS
		lBits1 = new JLabel("  TAG            BLOCK              WORD");
		lBits2 = new JLabel("                 BLOCK                  WORD");

		tTag = new JTextField(4);
		tTag.setEditable(false);
		tBlock1 = new JTextField(5);
		tBlock1.setEditable(false);	
		tBlock2 = new JTextField(8);
		tBlock2.setEditable(false);
		tWord1 = new JTextField(6);
		tWord1.setEditable(false);
		tWord2 = new JTextField(6);
		tWord2.setEditable(false);

		//SET THE FONT STYLES FOR THE BITS IN MAIN MEMORY ADDRESS
		tTag.setFont(new Font("Monospaced", Font.BOLD, 14));
		tBlock1.setFont(new Font("Monospaced", Font.BOLD, 14));
		tWord1.setFont(new Font("Monospaced", Font.BOLD, 14));
		tBlock2.setFont(new Font("Monospaced", Font.BOLD, 14));
		tWord2.setFont(new Font("Monospaced", Font.BOLD, 14));


		//REGISTER LISTENERS	
		restart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reStart();
			}
		});

		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextClicked = true;	
				step();
			}
		});
		
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextClicked = false;
				step();		
			}
		});
		
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int confirmQuit = JOptionPane.showConfirmDialog(null, "Really Quit?", "Quit Confirmation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				switch (confirmQuit){
					case JOptionPane.YES_OPTION: removeInstance();
					case JOptionPane.NO_OPTION: break;
				}
			}
		});
		

		autoGen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				autoGenerateString();
			}
		});

		selfGen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selfGenerateString();
			}
		});

		//DISABLE NAVIGATION BUTTONS FOR NOW
		next.setEnabled(false);
		back.setEnabled(false);

		//CREATE PANELS
		cachePanel = new CachePanel();
		memoryPanel = new MemoryPanel();
		bottomPanel = new JPanel();
		cache = new JPanel();
		cacheHitsMisses = new JPanel();
		pAutoSelfGen = new JPanel();
		pAddRefStr = new JPanel();
		pEastPanel = new JPanel();
		pBitsInMM = new JPanel();
		pBitsInMM1 = new JPanel();
		pBitsInMM2 = new JPanel();

		//ADD COMPONENTS TO THE PANELS

		//PANEL WITH PROGRESS UPDATE TEXT AREA AND NAVIGATION BUTTONS
		bottomPanel.add(lProgress);	
		bottomPanel.add(progressScroll);
		bottomPanel.add(restart);
		bottomPanel.add(next);	
		bottomPanel.add(back);
		bottomPanel.add(quit);

		//PANEL WITH CACHE BLOCKS, HITS AND MISSES INFO.
		cacheHitsMisses.add(lCacheHits);
		cacheHitsMisses.add(tCacheHits);
		cacheHitsMisses.add(lCacheMisses);
		cacheHitsMisses.add(tCacheMisses);
		cacheHMBorder= BorderFactory.createEtchedBorder();
		cacheHitsMisses.setBorder(BorderFactory.createTitledBorder(cacheHMBorder, "")); 

		cache.setLayout(new BorderLayout());
		cache.add(cachePanel, "Center");
		cache.add(cacheHitsMisses, "South");

		//PANEL WITH ADDRESS REFERENCE STRING AND STRING GENERATION BUTTONS
		pAutoSelfGen.setLayout(new GridLayout(2, 1));
		pAutoSelfGen.add(autoGen);
		pAutoSelfGen.add(selfGen);
		
		pAddRefStr.setLayout(new BorderLayout());
		pAddRefStr.setPreferredSize(new Dimension(160, 400));

		pAddRefStr.add(addRefStrScroll, "Center");
		pAddRefStr.add(pAutoSelfGen, "South");
		addRefStrBorder= BorderFactory.createEtchedBorder();
		pAddRefStr.setBorder(BorderFactory.createTitledBorder(addRefStrBorder, " Address Reference String ")); 

		//PANEL WITH THE MAIN MEMORY ADDRESS BITS INFO.
		pBitsInMM1.setLayout(new BorderLayout());
		bitsInMM1Border= BorderFactory.createEtchedBorder();
		pBitsInMM1.setBorder(BorderFactory.createTitledBorder(bitsInMM1Border, " Main Memory Address ")); 
		pBitsInMM1.add(tTag, "West");
		pBitsInMM1.add(tBlock1, "Center");
		pBitsInMM1.add(tWord1, "East");	
		pBitsInMM1.add(lBits1, "South");

		pBitsInMM2.setLayout(new BorderLayout());
		bitsInMM2Border= BorderFactory.createEtchedBorder();
		pBitsInMM2.setBorder(BorderFactory.createTitledBorder(bitsInMM2Border, " Memory Block and Word Bits ")); 
		pBitsInMM2.add(tBlock2, "Center");
		pBitsInMM2.add(tWord2, "East");	
		pBitsInMM2.add(lBits2, "South");

		pBitsInMM.setLayout(new GridLayout(2, 1));
		bitsInMMBorder= BorderFactory.createEtchedBorder();
		pBitsInMM.setBorder(BorderFactory.createTitledBorder(bitsInMMBorder, ""));
		pBitsInMM.add(pBitsInMM1);
		pBitsInMM.add(pBitsInMM2);

		//PANEL CONTAINING THE ADDRESS REF. STRING PANEL AND BITS IN MM PANEL
		pEastPanel.setLayout(new BorderLayout());
		pEastPanel.setPreferredSize(new Dimension(210, 600));
		pEastPanel.add(pAddRefStr, "Center");
		pEastPanel.add(pBitsInMM, "South");
		
		//ADD COMPONENTS TO THE FRAME CONTAINER
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(cache, "West");
		c.add(memoryPanel, "Center");
		c.add(pEastPanel, "East");
		c.add(bottomPanel, "South");

		/*
		* CALL THE FUNCTION TO GENERATE THE ARRAY addresses, WHICH CONTAINS ALL THE POSSIBLE MEMORY ADDRESSES
		* THIS ARRAY WILL BE USEFUL IN THE AUTO GENERATION OF ADDRESSES AS WELL AS FOR VALIDATION OF 
		* ADDRESS STRINGS INPUT BY THE USER IF HE/SHE CHOOSES SELF GENERATION.
		*/
		createAddresses();

		pack();
	}//END CONSTRUCTOR

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	* FUNCTION TO RESTART THE APPLICATION
	* BRINGS THE FRAME TO ITS ORIGINAL STARTING CONFIGURATION
	*/
	public void reStart(){

		//UNDO THE HIGHLIGHTS, IF ANY, OF THE PREVIOUS STEPS

		//UPDATE THE STATE OF THE CACHE AND MEMORY
		for (int i = 0; i < 16; i++){		
			cachePanel.boolBlocks[i] = false;
			cachePanel.stringBlocks[i] = "";
			cachePanel.tag[i] = "";						
		}		
		
		for (int i = 0; i < 8; i++){
			cachePanel.boolWords[i] = false;
			memoryPanel.boolWords[i] = false;
		}
		for (int i = 0; i < 32; i++)
			memoryPanel.boolBlocks[i] = false;

		//UPDATE THE CACHE HITS AND MISSES FIELDS
		cacheHits = 0;
		cacheMisses = 0;
		tCacheHits.setText("  0");
		tCacheMisses.setText("  0");

		//REFRESH THE ADDRESS REFERENCE STRING TO NULL
		listData.removeAllElements();
		addRefStrList.setListData(listData);

		//UPDATE THE BITS IN MAIN MEMORY ADDRESS
		tTag.setText("");
		tBlock1.setText("");
		tBlock2.setText("");
		tWord1.setText("");
		tWord2.setText("");
		tTag.setBackground(new Color(205, 205, 205));
		tBlock1.setBackground(new Color(205, 205, 205));
		tWord1.setBackground(new Color(205, 205, 205));
		tBlock2.setBackground(new Color(205, 205, 205));
		tWord2.setBackground(new Color(205, 205, 205));

		//UPDATE THE PROGRESS FIELD 
		tProgress.setText("Let's start over.\nPlease generate the Address Reference String."
						  +"\nThen click on \"Next\" to continue.");

		//DISABLE THE NEXT AND BACK BUTTONS
		next.setEnabled(false);
		back.setEnabled(false);
		
		//ENABLE ADDRESS GENERATION BUTTONS
		autoGen.setEnabled(true);
		selfGen.setEnabled(true);

		//RESET THE VALUES OF moveStatus AND evaluateIndex AND reStarted
		moveStatus = 0;
		evaluateIndex = 0;
		reStarted = true;

		//CALL THE REPAINT METHOD
		repaint();

	}//END FUNCTION reStart

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//FUNCTION TO SIMULATE A STEP IN THE SIMULATION ON THE SCREEN
	public void step(){
		
		/** 
		 * EACH TIME THE NEXT BUTTON IS PRESSED, ALL THE STATES THAT OCURRED UPTO THE CURRENT STATE ARE EVALUATED.
		 * THERE IS A while STATEMENT THAT PERFORMS THIS FUNCTION AND CONTAINS A switch STATEMENT WITHIN 
		 * IT TO EVALUATE EACH STEP AS IT OCCURS.  
		 */
		
		/////////////////////////////// INITIALIZATION ////////////////////////////////////
		
		//UPDATE THE STATE OF THE CACHE AND MEMORY
		for (int i = 0; i < 16; i++){		
			cachePanel.boolBlocks[i] = false;
			cachePanel.stringBlocks[i] = "";
			cachePanel.tag[i] = "";	
		}		
		
		for (int i = 0; i < 8; i++){
			cachePanel.boolWords[i] = false;
			memoryPanel.boolWords[i] = false;
		}
		for (int i = 0; i < 32; i++)
			memoryPanel.boolBlocks[i] = false;

		//UPDATE THE BITS IN MAIN MEMORY ADDRESS
		tTag.setText("");
		tBlock1.setText("");
		tBlock2.setText("");
		tWord1.setText("");
		tWord2.setText("");
		tTag.setBackground(new Color(205, 205, 205));
		tBlock1.setBackground(new Color(205, 205, 205));
		tWord1.setBackground(new Color(205, 205, 205));
		tBlock2.setBackground(new Color(205, 205, 205));
		tWord2.setBackground(new Color(205, 205, 205));
		
		//UPDATE THE CACHE HITS AND MISSES FIELDS
		cacheHits = 0;
		cacheMisses = 0;
		tCacheHits.setText("  "+cacheHits);
		tCacheMisses.setText("  "+cacheMisses);

		//RESET THE VALUE OF evaluateIndex	
		evaluateIndex = 0;
		
		//DISABLE ADDRESS GENERATION BUTTONS
		autoGen.setEnabled(false);
		selfGen.setEnabled(false);
		
		//IF Next WAS CLICKED, INCREMENT moveStatus	
		if (nextClicked)
			moveStatus++;
		else{
			//DECREMENT moveStatus AND ENABLE NEXT SINCE IT MIGHT BE DISABLED
			moveStatus--;
			next.setEnabled(true);
		}
		
		//IF NO MORE back MOVES CAN BE MADE, DISABLE back BUTTON
		if (moveStatus == 0){
			back.setEnabled(false);
			tProgress.setText("You cannot go back any further."
								  +"\nPlease click on \"Next\" or \"Restart\" to continue.");
				tProgress.setCaretPosition(0);
			
			//CLEAR THE SELECTED ADDRESS REFERENCE STRING
			addRefStrList.clearSelection();
		}
		else
			//ENABLE back BUTTON ONCE THE FIRST MOVE IS MADE		
			back.setEnabled(true);		
		
		//INITIALIZE THE VARIABLE THAT KEEPS TRACK OF THE STATE WE ARE CURRENTLY EVALUATING.
		int tempState = 1;
		
		////////////////////////// END INITIALIZATION //////////////////////////////////
		
		//CONTINUE TO EVALUATE EACH STATE TILL WE REACH THE CURRENT STATE
		while (tempState <= moveStatus){
			
			switch (tempState%6){
			
			case 1: //IF A NEW CYCLE IS BEGINNING, OBTAIN NEXT ADDRESS REFERENCE
								
				//HIGHLIGHT THE CURRENT ADDRESS REFERENCE STRING 
				addRefStrList.setSelectedIndex(evaluateIndex);
				
				//ENSURE THAT THE LIST SCROLLS AND SELECTED INDEX IS VISIBLE
				//DUE TO REPAINTING CONSTRAINTS, ONLY DO THIS IN THE CURRENT STATE
				if (tempState == moveStatus)
					addRefStrList.ensureIndexIsVisible(evaluateIndex);

				//EVALUATE THE TAG, BLOCK AND WORD
				hexAddress = (String)addRefStrList.getSelectedValue();				
				int intAddress = Integer.parseInt(hexAddress, 16);
				binAddress = Integer.toBinaryString(intAddress);
				
				//USING CLASS INTEGER'S parseInt FUNCTION RETURNS A BINARY STRING WITHOUT LEADING 0'S
				//ENSURE THAT binAddress is 8 bits
				
				if (binAddress.length() < 8){
					int zeroes = 8 - binAddress.length();
					for (int i = 0; i < zeroes; i++)
						binAddress = '0'+binAddress;
				}

				tag = binAddress.substring(0, 1);
				block = binAddress.substring(1, 5);
				word = binAddress.substring(5);
				blockMem = binAddress.substring(0, 5);
				
				//CALCULATE THE ACTUAL CACHE AND MEMORY BLOCKS AND WORDS IN QUESTION
				intBlockDec = Integer.parseInt(block, 2);
				intWordDec = Integer.parseInt(word, 2);
				intBlockDecMem = Integer.parseInt(blockMem, 2);
				
				//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD 
				if (tempState == moveStatus){
					tProgress.setText("The memory address we want is obtained from the Address Reference String."
									  +"\nIt is (in hexadecimal): " + hexAddress+".");
					tProgress.setCaretPosition(0);
				}
				
				break;
				
			case 2: //EVALUATE THE BITS IN MAIN MEMORY ADDRESS AND HIGHLIGHT THEM		 

				//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD AND THE ADDRESS BITS
				if (tempState == moveStatus){
					tProgress.setText("The hexadecimal address " + hexAddress 
									  + " evaluates to its binary equivalent " + binAddress+"."
									  + "\nHence the bits in the Main Memory Address are divided into the following fields\n"
									  + tag + " --> Tag,  "+ block + " --> Block,  " + word + " --> Word."
									  + "\nThe above field values are used to access the required cache block."
									  + "\n\nFrom the 8 bit binary address, "+binAddress+", evaluated above "
									  + "we can also retrieve the following data:"
									  +"\nThe leftmost 5 bits, "+tag+block+", and the rightmost 3 bits, "+word
									  +", indicate the actual memory block and word in question, respectively.");

					tProgress.setCaretPosition(0);				

					//HIGHLIGHT THE BITS IN MAIN MEMORY ADDRESS IN GREEN
					tTag.setBackground(Color.green);					
					tBlock1.setBackground(Color.green);					
					tWord1.setBackground(Color.green);					
					tBlock2.setBackground(Color.green);					
					tWord2.setBackground(Color.green);					
				}//END if
				
				tTag.setText(" "+tag);
				tBlock1.setText("    "+block);
				tWord1.setText(" "+word);
				tBlock2.setText("      "+tag+block);
				tWord2.setText(" "+word);
				
				break;

			case 3: //EVALUATE THE CACHE BLOCK IN QUESTION AND HIGHLIGHT IT
			
				//UNDO HIGHLIGHTS OF PREVIOUS STEP			
				tTag.setBackground(new Color(205, 205, 205));
				tBlock1.setBackground(new Color(205, 205, 205));
				tWord1.setBackground(new Color(205, 205, 205));
				tBlock2.setBackground(new Color(205, 205, 205));
				tWord2.setBackground(new Color(205, 205, 205));

				//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
				if (tempState == moveStatus){
					tProgress.setText("The bits in the Main Memory Address indicating the cache block are " 
									  + block + ".\nThe equivalent block number in decimal is "+ intBlockDec 
									  + ", so the block "+intBlockDec+" is searched in the cache.");
					tProgress.setCaretPosition(0);
				}//END IF

				//HIGHLIGHT THE CACHE BLOCK IN YELLOW
				//TO CAUSE HIGHLIGHTING ON THE CACHE, WE NEED TO MODIFY IT'S STATE, i.e. IT'S DATA MEMBERS
				cachePanel.boolBlocks[intBlockDec] = true;
				
				//UPDATE THE CACHE HITS AND MISSES COUNTS
				if (cachePanel.stringBlocks[intBlockDec].equals("")){
					cacheMisses++;
					tCacheMisses.setText("  "+cacheMisses);
					
					if (tempState == moveStatus){
						tProgress.append("\nSince the required cache block is empty, there is a cache miss.");
						tProgress.setCaretPosition(0);
					}
				}
				else if (cachePanel.stringBlocks[intBlockDec].equals(""+intBlockDecMem)){
					cacheHits++;
					tCacheHits.setText("  "+cacheHits);
					
					if (tempState == moveStatus){
						tProgress.append("\nSince the required cache block contains the memory block, there is a cache hit.");
						tProgress.setCaretPosition(0);
					}
				}
				else {
					cacheMisses++;
					tCacheMisses.setText("  "+cacheMisses);
					
					if (tempState == moveStatus){
						tProgress.append("\nSince the required cache block does not contain the memory block, there is a cache miss.");
						tProgress.setCaretPosition(0);					
					}
				}
				
				break;

			case 4: //EVALUATE THE MEMORY BLOCK IN QUESTION AND HIGHLIGHT IT

				//UNDO THE HIGHLIGHTS OF THE PREVIOUS STEP
				cachePanel.boolBlocks[intBlockDec] = false;

				/*
				* NOW, THERE ARE 3 WAYS TO GO FROM HERE
				* 1. IF THE CACHE BLOCK WAS EMPTY, SIMPLY BRING THE MEMORY BLOCK INTO CACHE
				* 2. IF THE REQUIRED MEMORY BLOCK IS ALREADY IN CACHE, DO NOTHING
				* 3. IF THE CACHE HAS ANOTHER MEMORY BLOCK IN IT, REPLACE IT WITH THE REQUIRED MEMORY BLOCK
				*/

				//IF THE CACHE BLOCK IS EMPTY THEN BRING THE MEMORY BLOCK INTO CACHE			
				if (cachePanel.stringBlocks[intBlockDec].equals("")){

					//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
					if (tempState == moveStatus){
						tProgress.setText("Since the 5 leftmost bits are " + tag + block 
										  +", the memory block we need (in decimal) is "+intBlockDecMem+"."
										  +"\nAs we saw in the previous step, the required block of the cache is empty "
										  +"so we need to bring in the block from memory.");
						tProgress.setCaretPosition(0);
					}

					//SET THE MEMORY STATE SO AS TO HIGHLIGHT THE REQUIRED MEMORY BLOCK
					memoryPanel.boolBlocks[intBlockDecMem] = true;

					//SET THE MEMORY STATE SO AS TO HIGHLIGHT THE REQUIRED WORD
					memoryPanel.boolWords[intWordDec] = true;

				}//END IF

				//IF MEMORY BLOCK IS ALREADY IN CACHE THEN DO NOTHING	
				else if (cachePanel.stringBlocks[intBlockDec].equals(""+intBlockDecMem)){
					
					//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
					if (tempState == moveStatus){
						tProgress.setText("Since the 5 leftmost bits are "+tag+block
										  +", the memory block we need (in decimal) is "+intBlockDecMem+"."
										  +"\nAs saw in the previous step, "
										  +"the block of the cache already contains this required block "
										  + "so we can now access it from cache as needed.");
						tProgress.setCaretPosition(0);
					}

					//SET THE MEMORY STATE SO AS TO HIGHLIGHT THE REQUIRED MEMORY BLOCK
					memoryPanel.boolBlocks[intBlockDecMem] = true;

					//SET THE MEMORY STATE SO AS TO HIGHLIGHT THE REQUIRED WORD
					memoryPanel.boolWords[intWordDec] = true;

				}//END ELSE IF

				//IF THE CACHE BLOCK IS FILLED WITH ANOTHER MEMORY BLOCK
				else {
					
					//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
					if (tempState == moveStatus){
						tProgress.setText("Since the 5 leftmost bits are "+tag+block
										  +", the memory block we need (in decimal) is "+intBlockDecMem+"."
										  +"\nAs we saw in the previous step, the required block of the cache is filled, "
										  +"but with another memory block."
										  +"\nSo we need to bring in the block from memory and "
										  +"replace the existing cache block.");
						tProgress.setCaretPosition(0);
					}

					//SET THE MEMORY STATE SO AS TO HIGHLIGHT THE REQUIRED MEMORY BLOCK IN BLUE
					memoryPanel.boolBlocks[intBlockDecMem] = true;

					//SET THE MEMORY STATE SO AS TO HIGHLIGHT THE REQUIRED WORD IN BLACK
					memoryPanel.boolWords[intWordDec] = true;

				}//END ELSE
				
				break;

			case 5: //HIGHLIGHT THE CACHE BLOCK WITH THE MEMORY BLOCK NOW IN IT

				//UNDO HIGHLIGHTS OF PREVIOUS STEP
				memoryPanel.boolBlocks[intBlockDecMem] = false;
				memoryPanel.boolWords[intWordDec] = false;
				
				//UPDATE THE CACHE ARRAYS KEEPING TRACK OF MEMORY BLOCKS AND TAGS
				cachePanel.stringBlocks[intBlockDec] = ""+intBlockDecMem;
				cachePanel.tag[intBlockDec] = tag;		
				
				//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
				if (tempState == moveStatus){
					tProgress.setText("Now that the required memory block is in cache we note the following 3 things:"
									  +"\n1. The cache block has a tag associated with it and the tag, "
									  +"as specified by the Tag bit in the Main Memory Address, has a value of "+tag
									  +".\n2. The word bits, as specified in the Main Memory Address, are "+word
									  +" which indicate the word at offset "+wordDec+" in the block."
									  +"\n3.If the cache was originally empty or contained a memory block other than the one"
									  +" we required, the count of Cache Misses was incremented."
									  +"\nIf the cache already had the required memory block in it, "
									  +"then the count of Cache Hits was incremented.");
					tProgress.setCaretPosition(0);
				}

				//HIGHLIGHT THE CACHE BLOCK IN YELLOW
				cachePanel.boolBlocks[intBlockDec] = true;
				cachePanel.boolWords[intWordDec] = true;
				cachePanel.boolTags[intBlockDec] = true;
				
				break;
				
			case 0: //CLEANUP STEP

				//UNDO HIGHLIGHTS OF PREVIOUS STEP
				cachePanel.boolBlocks[intBlockDec] = false;
				cachePanel.boolWords[intWordDec] = false;
				cachePanel.boolTags[intBlockDec] = false;

				tTag.setText("");
				tBlock1.setText("");
				tWord1.setText("");
				tBlock2.setText("");
				tWord2.setText("");

				tTag.setBackground(new Color(205, 205, 205));
				tBlock1.setBackground(new Color(205, 205, 205));
				tWord1.setBackground(new Color(205, 205, 205));
				tBlock2.setBackground(new Color(205, 205, 205));
				tWord2.setBackground(new Color(205, 205, 205));

				//CLEAR THE SELECTED ADDRESS REFERENCE STRING
				addRefStrList.clearSelection();

				//INCREMENT THE INDEX SO AS TO POINT TO THE NEXT ADDRESS REFERENCE STRING
				evaluateIndex++;

				//IF THE LAST ADDRESS REFERENCE STRING HAS BEEN REACHED, DO THE APPROPRIATE
				if (evaluateIndex == listData.size()){
					
					//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
					if(tempState == moveStatus){
						tProgress.setText("This completes the runthrough."
										  +"\nPlease click on \"Restart\", generate the Address Reference String "
										  +"OR click \"Quit\" to finish.");
						tProgress.setCaretPosition(0);
					}
					
					next.setEnabled(false);
					
					//ENABLE ADDRESS GENERATION BUTTONS
					autoGen.setEnabled(true);
					selfGen.setEnabled(true);
					
					reStarted = false;					
				}
				
				else{
					//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
					if (tempState == moveStatus){
						tProgress.setText("This completes an access cycle.");
						tProgress.setCaretPosition(0);		
					}
					
					//CLEAR THE SELECTION IN THE ADDRESS REFERENCE STRING
					addRefStrList.clearSelection();
				}
				
				break;
				
			default: 
				JOptionPane.showMessageDialog(null, "Uh Oh, there's a problem in switch-case!");
				break;			
			
			}//END switch
			
			tempState++;
			
		}//END while
		
		//CALL THE REPAINT METHOD
		repaint();

	}//END FUNCTION step

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	* FUNCTION TO CREATE ARRAY addresses
	* THIS ARRAY IS USED FOR AUTO GENERATION OF ADDRESSES AND FOR VALIDATION WHEN THE USER CHOOSES SELF GENERATION
	*/
	public void createAddresses(){

		int index = -1;
		for (int i = 0; i < 16; i++){
			for (int j = 0; j < 16; j++){
				index++;
				addresses[index] = tempAddress[i] + tempAddress[j];
			}
		}
	}//END FUNCTION createAddresses

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//FUNCTION TO GENERATE A STRING OF 10 ADDRESS REFERENCES
	public void autoGenerateString(){
		int random;

		for (int i = 0; i < 10; i++){

			//RANDOMLY SELECT AN INDEX BETWEEN 0 AND 255
			random = (int)(Math.random() * 256);

			//ADD THE ADDRESS AT THIS INDEX IN ARRAY addresses TO THE LIST OF ADDRESS REFERENCE STRINGS
			listData.add(addresses[random]);
		}

		//ADD THIS LIST TO THE LISTBOX
		addRefStrList.setListData(listData);
		
		//ENABLE THE Next BUTTON AND DISABLE back BUTTON
		next.setEnabled(true);
		back.setEnabled(false);

		//UPDATE THE PROGRESS FIELD
		tProgress.setText("We have automatically generated an address string of 10 addresses for you to work with."
						  +"\nClick on \"Next\" to continue.");
		
	}//END FUNCTION autoGenerateString

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//FUNCTION ALLOWING THE USER TO SELF GENERATE THE ADDRESS REFERENCE STRING
	public void selfGenerateString(){

		int option = 0;
		int index = 0;

		//SET THE VALUES OF THE CUSTOM ARGUMENTS USED IN THE UPCOMING CALL TO THE METHOD JOptionPane.showOptionDialog
		JTextField textfield = new JTextField();
		Object[] array = {"Enter String", textfield};
		Object[] options = {"Continue", "Done"};

		//ALLOW THE USER TO INPUT UPTO 10 ADDRESSES
		while ((option == 0) && index < 10){

			//CALL THE showOptionDialog METHOD USING THE ABOVE CUSTOM ARGUMENTS
			option = JOptionPane.showOptionDialog(this, array, "Self Generate", JOptionPane.YES_NO_OPTION, 
												  JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

			//IF THE "CONTINUE" BUTTON WAS CLICKED THEN VALIDATE INPUT 
			if (option == 0){
				//THEN VALIDATE INPUT
				if ((validateInput(textfield.getText())) || (validateInput(textfield.getText().toUpperCase()))){
					//IF THE INPUT IS VALID, ADD IT TO THE LIST OF ADDRESS REFERENCE STRINGS
					index++;
					listData.add(textfield.getText().toUpperCase());
				}
				else 
					//IF THE INPUT IS INVALID, NOTIFY USER AND PROMPT AGAIN
					JOptionPane.showMessageDialog(this, "Invalid Input. Please try again.", "Invalid Input", 
												  JOptionPane.ERROR_MESSAGE);

				//RESET THE TEXTFIELD
				textfield.setText("");
			}
			//ELSE IF DONE BUTTON WAS CLICKED ON INPUT, VALIDATE INPUT
			else if ((option == 1) && ((!textfield.getText().equals("")))){

				//IF INPUT WAS VALID, ADD TO ADDRESS REFERENCE STRING AND QUIT THE DIALOG BOX
				if ((validateInput(textfield.getText())) || (validateInput(textfield.getText().toUpperCase()))){
					index++;
					listData.add(textfield.getText().toUpperCase());
					tProgress.setText("You have generated an Address Reference String of "+index+" address."
									  +"\nPlease click on \"Next\" to continue.");
					tProgress.setCaretPosition(0);
				}
				else{
					//IF INPUT WAS INVALID, NOTIFY USER AND QUIT THE DIALOG BOX
					JOptionPane.showMessageDialog(this, "Invalid Input. Quitting without saving last entry.", 
												  "Invalid Input", JOptionPane.ERROR_MESSAGE);
					tProgress.setText("You have generated an Address Reference String of "+index+" address."
									  +"\nPlease click on \"Next\" to continue.");
					tProgress.setCaretPosition(0);
				}

			}
		}
		//PUT THE STRING OF INPUTS INTO THE ADDRESS REFERENCE STRING LISTBOX
		addRefStrList.setListData(listData);

		//ENABLE Next BUTTON AND DISABLE back BUTTON ONLY IF AT LEAST ONE VALID ENTRY WAS MADE 
		if (index > 0){
			next.setEnabled(true);
			back.setEnabled(false);
		}

	}//END FUNCTION selfGenerateString

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//FUNCTION TO VALIDATE THE INPUT BY THE USER - CALLED FROM FUNCTION selfGenerateString
	public boolean validateInput(String test){
		for (int i = 0; i < 256; i++){

			//CHECK IF INPUT VALUE EXISTS IN ARRAY addresses - RETURN TRUE OR FALSE ACCORDINGLY
			if (addresses[i].equals(test))
				return true;
		}
		return false;
	}//END FUNCTION validateInput

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//FUNCTION TO CLOSE THIS FRAME
	public void removeInstance(){
		this.dispose();
	}
	
}//END CLASS DMCFrame

