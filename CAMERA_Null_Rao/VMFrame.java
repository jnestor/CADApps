/*
 * FILE:	VMFrame.java
 * AUTHOR:	Karishma Rao
 * DATE:	February 16th, 2003
 */

/*
 * DESCRIPTION: Implements the interactive tutorial to demonstrate the working of Virtual Memory and Paging
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Vector;

//CLASS VMFrame IS THE MAIN FRAME OF THE APPLICATION
class VMFrame extends JFrame {

	//DECLARE ALL THE COMPONENTS
	
	//PANELS FOR DRAWING THE PHYSICAL MEMORY PANEL AND THE VIRTUAL MEMORY PANEL
	private PhysicalMemoryPanel physMemPanel;  
	private VirtualMemoryPanel virtMemPanel;
	private JPanel memPanel;
	
	//PANELS FOR DRAWING THE TLB AND PAGE TABLE
	private TLBPanel tlbPanel;
	private PageTablePanel pageTablePanel;

	//PANELS TO CONTAIN THE PROGRESS BAR AND NAVIGATION BUTTONS
	private JPanel bottomPanel;

	//PANELS TO CONTAIN THE ADDRESS REFERENCE STRING AND MEMORY ADDRESS BITS
	private JPanel pEastPanel, pAddRefStr, pAutoSelfGen, pBits, pBitsInVM, pBitsInPM;
	
	//PANELS FOR THE PAGE TABLE, TLB AND HITS AND MISSES COUNTS
	private JPanel pTables, pTLBHitsMisses, pPageHitsFaults;

	//NAVIGATION BUTTONS AND ADDRESS REFERENCE STRING GENERATION BUTTONS
	private JButton restart, next, back, quit, autoGen, selfGen;

	//LABELS FOR THE VARIOUS COMPONENTS
	private JLabel lTLBHits, lTLBMisses, lPageFaults, lPageHits, lProgress, lBitsInVM, lBitsInPM;

	//TEXTFIELDS FOR TLB AND PAGE HITS AND MISSES AND MEMORY ADDRESS BITS
	private JTextField tTLBHits, tTLBMisses, tPageFaults, tPageHits, tPageInVM, tOffsetInVM, tPageInPM, tOffsetInPM;

	//TEXTAREA FOR PROGRESS UPDATE
	private JTextArea tProgress;

	//SCROLLPANES FOR PROGRESS UPDATE TEXTAREA AND ADDRESS REFERENCE STRING LISTBOX
	private JScrollPane progressScroll, addRefStrScroll;

	//LISTBOX FOR ADDRESS REFERENCE STRING
	private JList addRefStrList;

	//BORDERS FOR THE VARIOUS PANELS
	private Border memBorder, bitsInVMBorder, bitsInPMBorder, bitsBorder, addRefStrBorder, tablesBorder;

	//TEMP ARRAY USED TO GENERATE THE STRING OF 256 POSSSIBLE MEMORY ADDRESSES
	private String[] tempAddress = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

	//ARRAY TO HOLD THE 256 POSSIBLE MEMORY ADDRESSES
	private String[] addresses = new String[256];	
	
	//VECTOR USED TO MAINTAIN THE ADDRESSES IN THE REFERENCE STRING LIST
	private Vector listData = new Vector();

	//INT USED TO TRACK STEPPING THROUGH THE TUTORIAL (WHEN THE Next AND Back BUTTONS ARE USED)
	private int moveStatus = 0;

	//INT USED TO COUNT THE NUMBER OF ADDRESS REFERENCE STRINGS IN THE LIST
	private int numStrings = 0;

	//INT USED TO INDICATE THE ADDRESS REFERENCE STRING IN QUESTION
	private int evaluateIndex = 0;

	//INTS TO KEEP COUNT OF TLB HITS AND MISSES, AND PAGE FAULTS AND HITS 
	private int tlbHits, tlbMisses, pageFaults, pageHits;
	private boolean boolTLBHit = false;
	private boolean boolPageHit = false;

	//INTS TO KEEP TRACK OF THE RELEVANT ENTRY IN TLB AND PAGE TABLE
	private int tlbEntry = -1;
	private int pageTableEntry = -1;
	
	//DATA MEMBERS TO KEEP TRACK OF EMPTY AND LRU MEMORY FRAMES
	private int emptyMemFrame = -1;
	private int lruMemFrame = -1;
	private int frameNum = -1;
	private boolean[] statusFrameEmpty = new boolean[4];
	private int[] statusFrameLRU = new int[4];
	private int statusMemFrameLRU = 0;		//INCREMENTED EACH TIME A FRAME IS ACCESSED
	
	//DATA MEMBERS TO KEEP TRACK OF EMPTY AND LRU TLB ROWS
	private int emptyTLBRow = -1;
	private int tlbRow = -1;
	private boolean[] statusTLBRowEmpty = new boolean[4];
	private int[] statusTLBRowLRU = new int[4];
	private int statusRowLRU = 0;		//INCREMENTED EACH TIME A TLB ROW IS ACCESSED
	
	//STRINGS USED TO CONVERT THE DECIMAL MEMORY ADDRESS VALUES INTO HEX AND BINARY
	private String hexAddress = new String();
	private String binAddressVM = new String();
	private String binAddressPM = new String();
	private int physAddress;

	//INTS USED TO MAINTAIN THE DECIMAL VALUE OF THE MEMORY ADDRESS IN QUESTION 
	private int intOffsetDec = 0;
	private int intVirtPageDec = 0;
	private int intPhysPageDec = 0;

	//STRINGS USED TO MAINTAIN THE MAIN MEMORY ADDRESS BITS
	private String virtPage = new String();
	private String offset = new String();
	private String physPage = new String();

	//STRINGS USED FOR CONVERSION OF ADDRESSES FROM DECIMAL - HEX - BIN
	private String virtPageDec = new String();
	private String physPageDec = new String();
	private String offsetDec = new String();

	//BOOLEAN USED TO KEEP TRACK OF WHETHER THE RESTART BUTTON WAS PRESSED - USED TO ENABLE Next BUTTON APPROPRIATELY
	private boolean reStarted = true;
	
	//BOOLEAN USED TO INDICATE WHETHER THE Next BUTTON WAS CLICKED OR THE Back BUTTON
	private boolean nextClicked = true;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//CONSTRUCTOR
	public VMFrame(){

		//SET PROPERTIES OF THE MAIN FRAME
		setTitle("Virtual Memory and Paging");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setIconImage(Toolkit.getDefaultToolkit().createImage(VMFrame.class.getResource("cam.gif")));

		//CREATE COMPONENTS AND SET THEIR PROPERTIES

		//NAVIGATION BUTTONS
		restart = new JButton("Restart");
		next = new JButton("Next");
		back = new JButton("Back");
		quit = new JButton("Quit");

		//TLB HITS AND MISSES AND PAGE HITS AND FAULTS INFO.
		lTLBHits = new JLabel("TLB Hits");
		lTLBMisses = new JLabel("TLB Misses");	
		tTLBHits = new JTextField(5);
		tTLBMisses = new JTextField(5);
		tTLBHits.setEditable(false);
		tTLBHits.setFont(new Font("Monospaced", Font.BOLD, 14));
		tTLBHits.setText("  0");
		tTLBMisses.setEditable(false);
		tTLBMisses.setFont(new Font("Monospaced", Font.BOLD, 14));
		tTLBMisses.setText("  0");
		
		lPageHits = new JLabel("Page Hits");
		lPageFaults = new JLabel("Page Faults");	
		tPageHits = new JTextField(5);
		tPageFaults = new JTextField(5);
		tPageHits.setEditable(false);
		tPageHits.setFont(new Font("Monospaced", Font.BOLD, 14));
		tPageHits.setText("  0");
		tPageFaults.setEditable(false);
		tPageFaults.setFont(new Font("Monospaced", Font.BOLD, 14));
		tPageFaults.setText("  0");

		//PROGRESS UPDATE AREA
		tProgress = new JTextArea(3, 45);
		tProgress.setEditable(false);
		tProgress.setLineWrap(true);
		tProgress.setWrapStyleWord(true);
		tProgress.setCaretPosition(0);
		tProgress.setFont(new Font("Serif", Font.BOLD + Font.ITALIC, 16));
		tProgress.setText("Welcome to Virtual Memory and Paging!\nThe system specs are as follows -"
						  + "\n  The virtual memory space for a process is 2^8 words\n  There are 16 Blocks in Main Memory\n  8 words per block"
						  +"\n  Page size is 32 words, so each page contains 4 blocks."
						  +"\n  So, there are 4 pages in physical memory and 8 virtual pages per process."
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
		lBitsInVM = new JLabel("     PAGE                   OFFSET");
		lBitsInPM = new JLabel("     PAGE                   OFFSET");

		tPageInVM = new JTextField(8);
		tPageInVM.setEditable(false);
		tOffsetInVM = new JTextField();
		tOffsetInVM.setEditable(false);
		
		tPageInPM = new JTextField(8);
		tPageInPM.setEditable(false);
		tOffsetInPM = new JTextField();
		tOffsetInPM.setEditable(false);		

		//SET THE FONT STYLES FOR THE BITS IN MAIN MEMORY ADDRESS
		tPageInVM.setFont(new Font("Monospaced", Font.BOLD, 14));
		tPageInPM.setFont(new Font("Monospaced", Font.BOLD, 14));
		tOffsetInVM.setFont(new Font("Monospaced", Font.BOLD, 14));
		tOffsetInPM.setFont(new Font("Monospaced", Font.BOLD, 14));

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
		virtMemPanel = new VirtualMemoryPanel();
		physMemPanel = new PhysicalMemoryPanel();
		memPanel = new JPanel();
		tlbPanel = new TLBPanel();
		pageTablePanel = new PageTablePanel();
		
		bottomPanel = new JPanel();	
		pTables = new JPanel();
		pTLBHitsMisses = new JPanel();
		pPageHitsFaults = new JPanel();	
		pAutoSelfGen = new JPanel();
		pAddRefStr = new JPanel();
		pEastPanel = new JPanel();
		pBits = new JPanel();
		pBitsInVM = new JPanel();
		pBitsInPM = new JPanel();

		//ADD COMPONENTS TO THE PANELS	
		
		//PANEL WITH THE PHYSICAL AND VIRTUAL MEMORY
		memPanel.setLayout(new BorderLayout());
		memPanel.add(physMemPanel, "Center");
		memPanel.add(virtMemPanel, "South");
		memBorder= BorderFactory.createEtchedBorder();
		memPanel.setBorder(BorderFactory.createTitledBorder(memBorder, "")); 

		//PANEL WITH PROGRESS UPDATE TEXT AREA AND NAVIGATION BUTTONS
		bottomPanel.add(lProgress);	
		bottomPanel.add(progressScroll);
		bottomPanel.add(restart);
		bottomPanel.add(next);	
		bottomPanel.add(back);
		bottomPanel.add(quit);

		//PANEL WITH TLB AND PAGE TABLE, AND HITS AND MISSES INFO.
		pTLBHitsMisses.add(lTLBHits);
		pTLBHitsMisses.add(tTLBHits);
		pTLBHitsMisses.add(lTLBMisses);
		pTLBHitsMisses.add(tTLBMisses);
		
		pPageHitsFaults.add(lPageHits);
		pPageHitsFaults.add(tPageHits);
		pPageHitsFaults.add(lPageFaults);
		pPageHitsFaults.add(tPageFaults);
		
		pTables.add(tlbPanel);
		pTables.add(pTLBHitsMisses);
		pTables.add(pageTablePanel);
		pTables.add(pPageHitsFaults);
		tablesBorder= BorderFactory.createEtchedBorder();
		pTables.setBorder(BorderFactory.createTitledBorder(tablesBorder, "")); 

		//PANEL WITH ADDRESS REFERENCE STRING AND STRING GENERATION BUTTONS
		pAutoSelfGen.setLayout(new GridLayout(2, 1));
		pAutoSelfGen.add(autoGen);
		pAutoSelfGen.add(selfGen);
		
		pAddRefStr.setLayout(new BorderLayout());
		pAddRefStr.setPreferredSize(new Dimension(120, 200));

		pAddRefStr.add(addRefStrScroll, "Center");
		pAddRefStr.add(pAutoSelfGen, "South");
		addRefStrBorder = BorderFactory.createEtchedBorder();
		pAddRefStr.setBorder(BorderFactory.createTitledBorder(addRefStrBorder, " Address Reference String ")); 

		//PANEL WITH THE MAIN MEMORY ADDRESS BITS INFO.
		pBitsInVM.setLayout(new BorderLayout());
		bitsInVMBorder= BorderFactory.createEtchedBorder();
		pBitsInVM.setBorder(BorderFactory.createTitledBorder(bitsInVMBorder, " Virtual Address Bits ")); 
		pBitsInVM.add(tPageInVM, "West");
		pBitsInVM.add(tOffsetInVM, "Center");	
		pBitsInVM.add(lBitsInVM, "South");

		pBitsInPM.setLayout(new BorderLayout());
		bitsInPMBorder= BorderFactory.createEtchedBorder();
		pBitsInPM.setBorder(BorderFactory.createTitledBorder(bitsInPMBorder, " Physical Address Bits ")); 
		pBitsInPM.add(tPageInPM, "West");
		pBitsInPM.add(tOffsetInPM, "Center");	
		pBitsInPM.add(lBitsInPM, "South");

		pBits.setLayout(new GridLayout(2, 1));
		bitsBorder= BorderFactory.createEtchedBorder();
		pBits.setBorder(BorderFactory.createTitledBorder(bitsBorder, ""));
		pBits.add(pBitsInVM);
		pBits.add(pBitsInPM);

		//PANEL CONTAINING THE ADDRESS REF. STRING PANEL AND BITS PANEL
		pEastPanel.setLayout(new BorderLayout());
		pEastPanel.setPreferredSize(new Dimension(210, 600));
		pEastPanel.add(pAddRefStr, "Center");
		pEastPanel.add(pBits, "South");
		
		//ADD COMPONENTS TO THE FRAME CONTAINER
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(memPanel, "West");
		c.add(pTables, "Center");
		c.add(pEastPanel, "East");
		c.add(bottomPanel, "South");
		
		//INITIALIZE ARRAYS THAT HOLD STATUS OF EMPTY AND LRU CACHE BLOCKS
		//INITIALIZE ARRAYS THAT HOLD STATUS OF EMPTY AND LRU TLB ROWS
		for (int i = 0; i < 4; i++){
			statusFrameEmpty[i] = true;
			statusFrameLRU[i] = 0;
			
			statusTLBRowEmpty[i] = true;
			statusTLBRowLRU[i] = 0;
		}
		
		statusMemFrameLRU = 0;
		statusRowLRU = 0;

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

		//UPDATE THE STATE OF THE MEMORY PANELS AND TLB
		for (int i = 0; i < 32; i++){
			physMemPanel.boolWords[i] = false;
		}
		for (int i = 0; i < 4; i++){
			physMemPanel.boolFrames[i] = false;
			statusFrameEmpty[i] = true;
			statusFrameLRU[i] = 0;
			
			tlbPanel.boolRows[i] = false;
			statusTLBRowEmpty[i] = true;
			statusTLBRowLRU[i] = 0;
			tlbPanel.virtPageNum[i] = "-";
			tlbPanel.physPageNum[i] = "-";
			
			for (int j = 0; j < 32; j++)
				physMemPanel.stringWords[i][j] = "";
		}
		
		for (int i = 0; i < 8; i++){
			virtMemPanel.boolPages[i] = false;			
			pageTablePanel.boolRows[i] = false;		
			pageTablePanel.frameNum[i] = "-";
			pageTablePanel.validBit[i] = "0";
		}

		//UPDATE THE HITS AND MISSES FIELDS
		tlbHits = 0;
		tlbMisses = 0;
		tTLBHits.setText("  0");
		tTLBMisses.setText("  0");
		
		pageHits = 0;
		pageFaults = 0;
		tPageHits.setText("  0");
		tPageFaults.setText("  0");
		
		boolTLBHit = false;
		boolPageHit = false;

		//UPDATE THE TLB AND PAGE TABLE INTS
		tlbEntry = -1;
		pageTableEntry = -1;
		
		//UPDATE INTS TRACKING EMPTY AND LRU FRAMES
		emptyMemFrame = -1;
		lruMemFrame = -1;
		frameNum = -1;
		statusMemFrameLRU = 0;
		
		//UPDATE INTS TRACKING EMPTY AND LRU TLB ROWS
		emptyTLBRow = -1;
		tlbRow = -1;
		statusRowLRU = 0;
		
		
		//REFRESH THE ADDRESS REFERENCE STRING TO NULL
		listData.removeAllElements();
		addRefStrList.setListData(listData);

		//UPDATE THE BITS IN MAIN MEMORY ADDRESS
		tPageInVM.setText("");
		tPageInPM.setText("");
		tOffsetInVM.setText("");
		tOffsetInPM.setText("");
		tPageInVM.setBackground(new Color(205, 205, 205));
		tPageInPM.setBackground(new Color(205, 205, 205));
		tOffsetInVM.setBackground(new Color(205, 205, 205));
		tOffsetInPM.setBackground(new Color(205, 205, 205));

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
	
	//FUNCTION TO SIMULATE THE NEXT MOVE ON THE FRAME
	public void step(){
		
		/** 
		 * EACH TIME THE NEXT BUTTON IS PRESSED, ALL THE STATES THAT OCURRED UPTO THE CURRENT STATE ARE EVALUATED.
		 * THERE IS A while STATEMENT THAT PERFORMS THIS FUNCTION AND CONTAINS A switch STATEMENT WITHIN 
		 * IT TO EVALUATE EACH STEP AS IT OCCURS.  
		 */
		
		/////////////////////////////// INITIALIZATION ////////////////////////////////////
		
		//UPDATE THE STATE OF THE MEMORY PANELS AND TLB
		for (int i = 0; i < 32; i++){
			physMemPanel.boolWords[i] = false;
		}
		for (int i = 0; i < 4; i++){
			physMemPanel.boolFrames[i] = false;
			statusFrameEmpty[i] = true;
			statusFrameLRU[i] = 0;
			
			tlbPanel.boolRows[i] = false;
			statusTLBRowEmpty[i] = true;
			statusTLBRowLRU[i] = 0;
			tlbPanel.virtPageNum[i] = "-";
			tlbPanel.physPageNum[i] = "-";
			
			for (int j = 0; j < 32; j++)
				physMemPanel.stringWords[i][j] = "";
		}
		
		for (int i = 0; i < 8; i++){
			virtMemPanel.boolPages[i] = false;			
			pageTablePanel.boolRows[i] = false;		
			pageTablePanel.frameNum[i] = "-";
			pageTablePanel.validBit[i] = "0";
		}

		//UPDATE THE HITS AND MISSES FIELDS
		tlbHits = 0;
		tlbMisses = 0;
		tTLBHits.setText("  0");
		tTLBMisses.setText("  0");
		
		pageHits = 0;
		pageFaults = 0;
		tPageHits.setText("  0");
		tPageFaults.setText("  0");
		
		boolTLBHit = false;
		boolPageHit = false;

		//UPDATE THE TLB AND PAGE TABLE INTS
		tlbEntry = -1;
		pageTableEntry = -1;
		
		//UPDATE INTS TRACKING EMPTY AND LRU FRAMES
		emptyMemFrame = -1;
		lruMemFrame = -1;
		frameNum = -1;
		statusMemFrameLRU = 0;
		
		//UPDATE INTS TRACKING EMPTY AND LRU TLB ROWS
		emptyTLBRow = -1;
		tlbRow = -1;
		statusRowLRU = 0;		

		//UPDATE THE BITS IN MAIN MEMORY ADDRESS
		tPageInVM.setText("");
		tPageInPM.setText("");
		tOffsetInVM.setText("");
		tOffsetInPM.setText("");
		tPageInVM.setBackground(new Color(205, 205, 205));
		tPageInPM.setBackground(new Color(205, 205, 205));
		tOffsetInVM.setBackground(new Color(205, 205, 205));
		tOffsetInPM.setBackground(new Color(205, 205, 205));
		
		//DISABLE ADDRESS GENERATION BUTTONS
		autoGen.setEnabled(false);
		selfGen.setEnabled(false);

		//RESET THE VALUE OF evaluateIndex
		evaluateIndex = 0;		
		
		/////////////////////////////// END INITIALIZATION ////////////////////////////////
		
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
		
		//CONTINUE TO EVALUATE EACH STATE TILL WE REACH THE CURRENT STATE
		while (tempState <= moveStatus){
			
			switch (tempState%7){
			
			case 1: //IF A NEW CYCLE IS BEGINNING, OBTAIN NEXT ADDRESS REFERENCE

				//OBTAIN THE ADDRESS REFERENCE STRING
				addRefStrList.setSelectedIndex(evaluateIndex);
			
				//ENSURE THAT THE LIST SCROLLS AND SELECTED INDEX IS VISIBLE
				//DUE TO REPAINTING CONSTRAINTS, ONLY DO THIS IN THE CURRENT STATE
				if (tempState == moveStatus)
					addRefStrList.ensureIndexIsVisible(evaluateIndex);

				//EVALUATE THE PAGE AND OFFSET VALUES
				hexAddress = (String)addRefStrList.getSelectedValue();
				int intAddress = Integer.parseInt(hexAddress, 16);
				binAddressVM = Integer.toBinaryString(intAddress);
			
				//USING CLASS INTEGER'S toBinaryString FUNCTION RETURNS A BINARY STRING WITHOUT LEADING 0'S
				//ENSURE THAT binAddressVM is 8 bits
			
				if (binAddressVM.length() < 8){
					int zeroes = 8 - binAddressVM.length();
					for (int i = 0; i < zeroes; i++)
						binAddressVM = '0'+binAddressVM;
				}
				virtPage = binAddressVM.substring(0, 3);
				offset = binAddressVM.substring(3);
			
				//CALCULATE (AS INTEGER VALUES) THE ACTUAL PAGE AND WORD IN QUESTION
				intVirtPageDec = Integer.parseInt(virtPage, 2);
				intOffsetDec = Integer.parseInt(offset, 2);

				//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
				if (tempState == moveStatus){
					tProgress.setText("The virtual memory address we want is obtained from the Address Reference String."
									  +" It is (in hexadecimal): " + hexAddress+".");
					tProgress.setCaretPosition(0);
				}
				
				break;
			
			case 2: //HIGHLIGHT THE BITS IN THE VIRTUAL ADDRESS 

				//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
				if (tempState == moveStatus){
					tProgress.setText("The hexadecimal address " + hexAddress 
									  + " evaluates to its binary equivalent " + binAddressVM+"."
									  + "\nHence the bits in the Virtual Address are divided into the following fields\n"
									  + virtPage + " --> Page,  "+ offset + " --> Offset."
									  + "\nThe above field values are used to access the required virtual page from the virtual memory space.");
					tProgress.setCaretPosition(0);
					
					//HIGHLIGHT THE BITS IN MAIN MEMORY ADDRESS IN GREEN
					tPageInVM.setBackground(Color.green);
					tOffsetInVM.setBackground(Color.green);
				}		
				
				tPageInVM.setText("  "+virtPage);				
				tOffsetInVM.setText("     "+offset);

				break;
		
			case 3: //SEARCH THE TLB FOR THE VIRTUAL PAGE NUMBER AND HIGHLIGHT THE RESULT, IF FOUND
					//UPDATE THE TLB HITS AND MISSES COUNTS AS APPROPRIATE
		
				//UNDO HIGHLIGHTS OF PREVIOUS STEP
				tPageInVM.setBackground(new Color(205, 205, 205));
				tOffsetInVM.setBackground(new Color(205, 205, 205));
			
				//RESET THE POINTER THAT TRACKS THE TLB ENTRY MATCH
				tlbEntry = -1;

				//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
				if (tempState == moveStatus){
					tProgress.setText("The bits in the Virtual Address indicating the virtual page are "+ virtPage 
									  + ".\nThe equivalent virtual page number in decimal, then, is "+ intVirtPageDec 
									  + " so, the Translation Lookaside Buffer, TLB, is searched for this page number.");
					tProgress.setCaretPosition(0);
				}
				
				//SEARCH THE TLB FOR THIS VIRTUAL PAGE NUMBER
				for (int i = 0; i < 4; i++){
					if (tlbPanel.virtPageNum[i].equals((""+intVirtPageDec))){
						tlbEntry = i;   //ROW IN TLB WITH THE PAGE-MAP PAIR
						break;
					}
				}
			
				//IF FOUND, HIGHLIGHT THE TLB ENTRY IN PINK AND UPDATE THE HITS/MISSES COUNT
				//TO CAUSE HIGHLIGHTING ON THE TLB, WE NEED TO MODIFY IT'S STATE, i.e. IT'S DATA MEMBERS
				if (tlbEntry >= 0){
					
					boolTLBHit = true;
					//GENERATE THE PHYSICAL ADDRESS BITS
					physPage = Integer.toBinaryString(Integer.parseInt(tlbPanel.physPageNum[tlbEntry], 10));

					if (physPage.length() == 1)
						physPage = '0'+physPage;
					
					binAddressPM = physPage+offset;
					physAddress = Integer.parseInt(binAddressPM, 2);
					
					//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
					if (tempState == moveStatus){					
						tProgress.append("\nThere was a TLB hit since a matching entry was found in the TLB for the virtual page number "+intVirtPageDec+"."
										 +"\nThe physical page frame number is retrieved from this entry. It is "+tlbPanel.physPageNum[tlbEntry]+"."
										 +"\nThus, the physical address bits are "+binAddressPM+"."
										 +"\nThe actual physical address is, therefore, "+physAddress);
						tProgress.setCaretPosition(0);
						
						//HIGHLIGHT THE PHYSICAL ADDRESS BITS
						tPageInPM.setBackground(Color.green);				
						tOffsetInPM.setBackground(Color.green);
					}
					
					//DISPLAY THE PHYSICAL ADDRESS BITS
					tPageInPM.setText("   "+physPage);
					tOffsetInPM.setText("     "+offset);
					
					//HIGHLIGHT THE TLB ENTRY
					tlbPanel.boolRows[tlbEntry] = true;
					
					//UPDATE THE LRU STATUS OF THE TLB ROW
					statusRowLRU++;
					statusTLBRowLRU[tlbEntry] = statusRowLRU;										
			
					//UPDATE THE TLB HITS AND MISSES COUNTS
					tlbHits++;
					tTLBHits.setText("  "+tlbHits);				
				}

				else { //THERE IS A TLB MISS
					
					boolTLBHit = false;
					
					//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
					if (tempState == moveStatus){
						tProgress.append("\nSince a matching entry was not found for the virtual page number, there was a TLB miss."
										 +"\nNow the entry at row "+intVirtPageDec+" in the page table is looked up.");
						tProgress.setCaretPosition(0);
					}
					tlbMisses++;
					tTLBMisses.setText("  "+tlbMisses);				
					
					//HIGHLIGHT THE PAGE TABLE ENTRY THAT IS NOW LOOKED UP
					pageTablePanel.boolRows[intVirtPageDec] = true;
					
					//RETRIEVE THE FRAME NUMBER ASSOCIATED WITH THIS VIRTUAL PAGE FROM THE PAGE TABLE
					if (pageTablePanel.validBit[intVirtPageDec].equals("1"))
						pageTableEntry = Integer.parseInt(pageTablePanel.frameNum[intVirtPageDec], 10); //ACTUAL FRAME NUMBER
					
					if (pageTableEntry >= 0){//THERE WAS A PAGE HIT
						
						boolPageHit = true;
						
						//GENERATE THE PHYSICAL ADDRESS BITS
						physPage = Integer.toBinaryString(pageTableEntry);
						
						if (physPage.length() == 1)
							physPage = '0'+physPage;
						
						binAddressPM = physPage+offset;					
						physAddress = Integer.parseInt(binAddressPM, 2);
						
						//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
						if (tempState == moveStatus){
							tProgress.append("\nSince the virtual page number had a valid entry in the page table, there was a page hit."
											 +"\nThe physical page number is "+pageTableEntry+"."
											 +"\nThus, the physical address bits are "+binAddressPM+"."
											 +"\nThe actual physical address is, therefore, "+physAddress+"."
											 +"\n\nNote that the TLB is now updated using the information found in the page table.");
							tProgress.setCaretPosition(0);
							
							//HIGHLIGHT THE PHYSICAL ADDRESS BITS
							tPageInPM.setBackground(Color.green);				
							tOffsetInPM.setBackground(Color.green);							
						}
						
						//DISPLAY THE PHYSICAL ADDRESS BITS
						tPageInPM.setText("   "+physPage);
						tOffsetInPM.setText("     "+offset);						
						
						//UPDATE THE PAGE HITS COUNT
						pageHits++;
						tPageHits.setText("  "+pageHits);
						
						//UPDATE THE TLB
						emptyTLBRow = getFirstEmptyTLBRow();
						if (!(emptyTLBRow == -1)){		//IF THERE IS AN EMPTY TLB ROW				
							//UPDATE THE TLB ROW
							tlbPanel.virtPageNum[emptyTLBRow] = ""+intVirtPageDec;
							tlbPanel.physPageNum[emptyTLBRow] = ""+pageTableEntry;
							
							//UPDATE THE EMPTY AND LRU STATUS OF THE TLB ROW
							statusTLBRowEmpty[emptyTLBRow] = false;
							statusRowLRU++;
							statusTLBRowLRU[emptyTLBRow] = statusRowLRU;	
							
							//GLOBAL VARIABLE THAT HOLDS THE CURRENT ROW IN THE TLB
							tlbEntry = emptyTLBRow;
						}
						else { //GET THE LRU TLB ROW AND REPLACE IT WITH THE NEW INFO
							tlbPanel.virtPageNum[getLRURow()] = ""+intVirtPageDec;
							tlbPanel.physPageNum[getLRURow()] = ""+pageTableEntry;
						
							//UPDATE THE LRU STATUS OF THE TLB ROW
							statusRowLRU++;
							statusTLBRowLRU[getLRURow()] = statusRowLRU;
							
							//GLOBAL VARIABLE THAT HOLDS THE CURRENT ROW IN THE TLB
							tlbEntry = getLRURow();
						}

					}
					else {//THERE WAS A PAGE FAULT	
						
						boolPageHit = false;
						
						//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
						if (tempState == moveStatus){
							tProgress.append("\nSince the page table entry for this virtual page number had a valid bit of 0, there was a page fault."
											 +"\nThe page must now be brought into physical memory from the hard disk.");
							tProgress.setCaretPosition(0);
						}
						
						//UPDATE THE PAGE FAULTS COUNT
						pageFaults++;
						tPageFaults.setText("  "+pageFaults);
					}
				}

				break;			
		
			case 4: //HIGHLIGHT THE PHYSICAL MEMORY FRAME WHERE THE PAGE EXISTS OR WILL EXIST

				//UNDO THE HIGHLIGHTS OF THE PREVIOUS STEP
				if (tlbEntry >= 0)
					tlbPanel.boolRows[tlbEntry] = false;
				pageTablePanel.boolRows[intVirtPageDec] = false;	
			
				tPageInPM.setBackground(new Color(205, 205, 205));				
				tOffsetInPM.setBackground(new Color(205, 205, 205));

				//IF THE PAGE IS IN MEMORY, HIGHLIGHT THAT MEMORY FRAME			
				//CASE 1: THERE WAS A TLB HIT
				if (boolTLBHit){
					frameNum = Integer.parseInt(tlbPanel.physPageNum[tlbEntry], 10);
					physMemPanel.boolFrames[frameNum] = true;
					physMemPanel.boolWords[intOffsetDec] = true;			
					
					//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
					if (tempState == moveStatus){
						tProgress.setText("As we saw in the previous step, the page frame number was retrieved from the TLB."
										  +"Highlighted is the page frame in physical memory with the data that we wanted."
										  +"Since the offset retrieved from the virtual address was "+intOffsetDec+", the word at this offset from the first address in the frame is the required data word.");
						tProgress.setCaretPosition(0);
					}
					
					//UPDATE THE LRU STATUS OF THE MEMORY FRAME
					statusMemFrameLRU++;
					statusFrameLRU[Integer.parseInt(tlbPanel.physPageNum[tlbEntry], 10)] = statusMemFrameLRU;

				}//END IF
			
				//CASE 2: THERE WAS A PAGE TABLE HIT
				else if (pageTablePanel.validBit[intVirtPageDec].equals("1")){
					frameNum = pageTableEntry;
					physMemPanel.boolFrames[frameNum] = true;
					physMemPanel.boolWords[intOffsetDec] = true;			
					
					//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
					if (tempState == moveStatus){
						tProgress.setText("As we saw in the previous step, the page frame number was retrieved from the Page Table."
										  +"\nHighlighted is the page frame in physical memory with the data that we wanted."
										  +"\nSince the offset retrieved from the virtual address was "+intOffsetDec+", the word at this offset from the first address in the frame is the required data word.");
						tProgress.setCaretPosition(0);
					}
					
					//UPDATE THE LRU STATUS OF THE MEMORY FRAME
					statusMemFrameLRU++;
					statusFrameLRU[pageTableEntry] = statusMemFrameLRU;

				}//END IF			

				//ELSE IF PAGE WAS NOT IN MEMORY - HIGHLIGHT THE FRAME INTO WHICH IT WILL BE BROUGHT
				//THIS FRAME MAY BE AN AVAILABLE EMPTY FRAME IN MEMORY, OR THE LEAST RECENTLY USED ONE
				else {
					
					//GET FIRST EMPTY MEMORY FRAME, IF AVAILABLE
					emptyMemFrame = getFirstEmptyMemFrame();
					
					//IF EMPTY MEMORY FRAME IS AVAILABLE, THIS IS WHERE THE PAGE WILL BE BROUGHT SO DISPLAY IT
					if (!(emptyMemFrame == -1)){
						//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
						if (tempState == moveStatus){
							tProgress.setText("Since the memory has empty space, the first available frame will be filled."
											  +"\nHighlighted is the memory frame into which the page will be brought.");
							tProgress.setCaretPosition(0);
						}
						
						//HIGHLIGHT THE MEMORY FRAME IN YELLOW
						physMemPanel.boolFrames[emptyMemFrame] = true;
						
						//STORE THE MEMORY FRAME NUMBER IN A GLOBAL INT	
						frameNum = emptyMemFrame;
					}

					//ELSE DISPLAY THE LRU MEMORY FRAME WHICH WILL BE REPLACED
					else {

						//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
						if (tempState == moveStatus){
							tProgress.setText("Since the memory is full, the least recently used memory frame will be replaced."
											  +"\nHighlighted is the victim memory frame which will be replaced.");
							tProgress.setCaretPosition(0);
						}

						lruMemFrame = getLRUMemFrame();

						//HIGHLIGHT THE MEMORY FRAME IN YELLOW
						physMemPanel.boolFrames[lruMemFrame] = true;
						
						//STORE THE MEMORY FRAME NUMBER IN A GLOBAL INT	
						frameNum = lruMemFrame;
					}				

				}//END ELSE			

				break;
		
			case 5: //HIGHLIGHT THE VIRTUAL PAGE WHICH WILL BE BROUGHT INTO MEMORY FROM DISK
			
				//UNDO THE HIGHLIGHTS OF THE PREVIOUS STEP
				if (frameNum >= 0)
					physMemPanel.boolFrames[frameNum] = false;
			
				//IF THIS IS THE CURRENT STATE, UPDATE PROGRESS STATUS
				if (tempState == moveStatus){
					tProgress.setText("Highlighted is the virtual page we need.");
					tProgress.setCaretPosition(0);
				}
			
				//HIGHLIGHT THE VIRTUAL PAGE
				virtMemPanel.boolPages[intVirtPageDec] = true;
			
				break;
		
			case 6: //BRING THE PAGE INTO MEMORY AND HIGHLIGHT THE FRAME WITH THE DATA IN IT
			
				//UNDO THE HIGHLIGHTS OF THE PREVIOUS STEP
				virtMemPanel.boolPages[intVirtPageDec] = false;
			
				//IF VIRTUAL PAGE ALREADY EXISTS IN THE MEMORY
				if (boolTLBHit || boolPageHit){
					//IF THIS IS THE CURRENT STATE, UPDATE PROGRESS STATUS
					if (tempState == moveStatus){
						tProgress.setText("As we saw earlier, the page already exists in frame "+frameNum+"."
										  +"\nHighlighted in black is the required word at offset "+intOffsetDec+".");
						tProgress.setCaretPosition(0);
					}
					
					//HIGHLIGHT THE MEMORY FRAME WITH THE DATA IN IT			
					physMemPanel.boolFrames[frameNum] = true;
					physMemPanel.boolWords[intOffsetDec] = true;
				}
				
				else { //PAGE IS BROUGHT IN FROM DISK
					//IF THIS IS THE CURRENT STATE, UPDATE PROGRESS STATUS
					if (tempState == moveStatus){
						tProgress.setText("The page is brought in from disk to memory into frame "+frameNum+"."
										+"\nHighlighted in black is the required word at offset "+intOffsetDec+".");
						tProgress.setCaretPosition(0);	
					}
					
					//VOID THE PAGE TABLE ENTRY FOR THE FRAME THAT IS ABOUT TO BE REPLACED
					if (lruMemFrame >= 0){
						String pageNum = physMemPanel.stringWords[lruMemFrame][0].substring(1, 2);
						pageTablePanel.frameNum[Integer.parseInt(pageNum)] = "-";
						pageTablePanel.validBit[Integer.parseInt(pageNum)] = "0";
						
						//IF THIS IS THE CURRENT STATE, UPDATE PROGRESS STATUS
						if (tempState == moveStatus){
							tProgress.append("\nNote that, since the previous page "+pageNum+" that was in the frame was replaced,"
											  +" the corresponding entry in the page table is invalidated.");
							tProgress.setCaretPosition(0);
						}
					}
			
					//HIGHLIGHT THE MEMORY FRAME WITH THE DATA IN IT			
					physMemPanel.boolFrames[frameNum] = true;
					physMemPanel.boolWords[intOffsetDec] = true;						
			
					for (int j = 0; j < 32; j++)
							physMemPanel.stringWords[frameNum][j] = "P"+intVirtPageDec+"Of"+j;
				
					//UPDATE THE EMPTY AND LRU STATUS OF THE MEMORY FRAME
					statusFrameEmpty[frameNum] = false;
					statusMemFrameLRU++;
					statusFrameLRU[frameNum] = statusMemFrameLRU;
			
					//UPDATE THE TLB AND PAGE TABLE AND HIGHLIGHT THEM	
					emptyTLBRow = getFirstEmptyTLBRow();
					if (!(emptyTLBRow == -1)){ //AN EMPTY TLB ROW IS AVAILABLE	
				
						tlbPanel.virtPageNum[emptyTLBRow] = ""+intVirtPageDec;
						tlbPanel.physPageNum[emptyTLBRow] = ""+frameNum;
						tlbPanel.boolRows[emptyTLBRow] = true;
				
						//UPDATE EMPTY AND LRU STATUS OF TLB ROW
						statusTLBRowEmpty[emptyTLBRow] = false;
						statusRowLRU++;
						statusTLBRowLRU[emptyTLBRow] = statusRowLRU;
			
						//GLOBAL VARIABLE TRACKING THE CURRENT TLB ROW
						tlbEntry = emptyTLBRow;
					}
					else { //GET THE LRU ROW AND REPLACE IT WITH THE NEW INFO
						tlbPanel.virtPageNum[getLRURow()] = ""+intVirtPageDec;
						tlbPanel.physPageNum[getLRURow()] = ""+frameNum;
						tlbPanel.boolRows[getLRURow()] = true;
						
						//GLOBAL VARIABLE TRACKING THE CURRENT TLB ROW
						tlbEntry = getLRURow();
				
						//UPDATE LRU STATUS OF TLB ROW
						statusRowLRU++;
						statusTLBRowLRU[getLRURow()] = statusRowLRU;
				
					}
			
					pageTablePanel.frameNum[intVirtPageDec] = ""+frameNum;
					pageTablePanel.validBit[intVirtPageDec] = "1";
					pageTablePanel.boolRows[intVirtPageDec] = true;
				
					//GENERATE THE PHYSICAL ADDRESS BITS
					physPage = Integer.toBinaryString(frameNum);
			
					if (physPage.length() == 1)
						physPage = '0'+physPage;
					binAddressPM = physPage+offset;
					physAddress = Integer.parseInt(binAddressPM, 2);
			
					//IF THIS IS THE CURRENT STATE, UPDATE PROGRESS STATUS
					if (tempState == moveStatus){
						tProgress.append("\nNote that the physical address was generated as the page was brought into memory."
										 +"\nLet's clearly understand the evaluation of the physical address:"
										 +"\nThe physical page frame number is "+frameNum+"."
										 +"\nThus, the page bits in the physical address bits are "+physPage+"."
										 +"\nThe word bits are the same as in the virtual address, "+offset+"."
										 +"\nThe actual physical address in decimal is, therefore, "+physAddress+".");
						tProgress.setCaretPosition(0);
						
						//HIGHLIGHT THE PHYSICAL ADDRESS BITS
						tPageInPM.setBackground(Color.green);				
						tOffsetInPM.setBackground(Color.green);						
					}
			
					//DISPLAY THE PHYSICAL ADDRESS BITS
					tPageInPM.setText("   "+physPage);
					tOffsetInPM.setText("     "+offset);
				
				}
				break;
			
			case 0: //LAST STEP IN CYCLE - CLEANUP STEP!

				//UNDO HIGHLIGHTS OF PREVIOUS STEP
				if (frameNum >= 0)
					physMemPanel.boolFrames[frameNum] = false;
				physMemPanel.boolWords[intOffsetDec] = false;
				if (tlbEntry >= 0)
					tlbPanel.boolRows[tlbEntry] = false;
				pageTablePanel.boolRows[intVirtPageDec] = false;
				
				tPageInPM.setBackground(new Color(205, 205, 205));				
				tOffsetInPM.setBackground(new Color(205, 205, 205));
				
				tPageInVM.setText("");
				tOffsetInVM.setText("");
				tPageInPM.setText("");
				tOffsetInPM.setText("");

				tPageInVM.setBackground(new Color(205, 205, 205));
				tOffsetInVM.setBackground(new Color(205, 205, 205));
				tPageInPM.setBackground(new Color(205, 205, 205));
				tOffsetInPM.setBackground(new Color(205, 205, 205));

				//CLEAR THE SELECTED ADDRESS REFERENCE STRING
				addRefStrList.clearSelection();

				//INCREMENT THE INDEX SO AS TO POINT TO THE NEXT ADDRESS REFERENCE STRING
				evaluateIndex++;

				//IF THE LAST ADDRESS REFERENCE STRING HAS BEEN REACHED, DO THE APPROPRIATE
				if (evaluateIndex == listData.size()){
					
					//IF THIS IS THE CURRENT STATE, UPDATE THE PROGRESS FIELD
					if (tempState == moveStatus){
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

				//ELSE AN ACCESS CYCLE HAS BEEN COMPLETED SO SHOW THE APPROPRIATE MESSAGE IN THE PROGRESS FIELD
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
				JOptionPane.showMessageDialog(null, "Uh Oh! There something wrong in switch-case!");
				break;
			
			}//END switch
			
			tempState++;
			
		}//END while			
		
		//CALL THE REPAINT METHOD
		repaint();
		
	}//END FUNCTION step
	
	////////////////////////////////////////////////////////////////////////////////////////
		
	//FUNCTION TO GENERATE A STRING OF 10 ADDRESS REFERENCES
	public void autoGenerateString(){
		int random;

		for (int i = 0; i < 10; i++){

			//RANDOMLY SELECT AN INDEX BETWEEN 0 AND 255
			random = (int)(Math.random() * 256);

			//ADD THE ADDRESS AT THIS INDEX IN ARRAY addresses TO THE LIST OF ADDRESS REFERENCE STRINGS
		//	listData[i] = addresses[random];
			listData.add(addresses[random]);
		}

		//ADD THIS LIST TO THE LISTBOX
		addRefStrList.setListData(listData);
		evaluateIndex = 0;
		
		//ENABLE THE Next BUTTON AND DISABLE Back BUTTON
	//	moveStatus = 0;
		next.setEnabled(true);
		back.setEnabled(false);

		//UPDATE THE PROGRESS FIELD
		tProgress.setText("We have automatically generated an address string of 10 addresses for you to work with."
						  +"\nPlease click on \"Next\" to continue.");

	}//END FUNCTION autoGenerateString
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//FUNCTION ALLOWING THE USER TO SELF GENERATE THE ADDRESS REFERENCE STRING
	public void selfGenerateString(){

		int option = 0;
		int index = 0;

		//INITIALIZE THE LIST OF ADDRESS REFERENCE STRINGS
	//	for (int i = 0; i < 10; i++)
	//		listData[i] = "";

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
			//		listData[index] = textfield.getText().toUpperCase();
					listData.add(textfield.getText().toUpperCase());
					index++;
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
				//	listData[index] = textfield.getText().toUpperCase();
					listData.add(textfield.getText().toUpperCase());
					index++;
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
		evaluateIndex = 0;

		//ENABLE Next BUTTON AND DISABLE Back BUTTON ONLY IF AT LEAST ONE VALID ENTRY WAS MADE 
		if (index > 0){
	//		moveStatus = 0;
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

	//FUNCTION TO RETURN THE INDEX OF THE FIRST EMPTY MEMORY FRAME, IF THERE IS ONE
	//OTHERWISE A VALUE OF -1 IS RETURNED

	public int getFirstEmptyMemFrame(){

		for (int i = 0; i < 4; i++){
			if (statusFrameEmpty[i] == true)
				return i;
		}
		return -1;

	}//END FUNCTION getFirstEmptyMemFrame

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//FUNCTION TO RETURN THE INDEX OF THE LEAST RECENTLY USED CACHE BLOCK

	public int getLRUMemFrame(){

		//INTEGER TO HOLD THE VALUE OF THE INDEX WITH THE LEAST LRU VALUE
		int minimum = 0;

		//COMPARE THE VALUES OF THE ARRAY statusFrameLRU AND RETURN THE INDEX WITH THE MINIMUM VALUE
		for (int i = 1; i < 4; i++){
			if (statusFrameLRU[minimum] > statusFrameLRU[i])
				minimum = i;
		}
		return minimum;

	}//END FUNCTION getLRUMemFrame

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//FUNCTION TO RETURN THE INDEX OF THE FIRST EMPTY TLB ROW, IF THERE IS ONE
	//OTHERWISE A VALUE OF -1 IS RETURNED

	public int getFirstEmptyTLBRow(){

		for (int i = 0; i < 4; i++){
			if (statusTLBRowEmpty[i] == true)
				return i;
		}
		return -1;

	}//END FUNCTION getFirstEmptyTLBRow

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//FUNCTION TO RETURN THE INDEX OF THE LEAST RECENTLY USED TLB ROW

	public int getLRURow(){

		//INTEGER TO HOLD THE VALUE OF THE INDEX WITH THE LEAST LRU VALUE
		int minimum = 0;

		//COMPARE THE VALUES OF THE ARRAY statusTLBRowLRU AND RETURN THE INDEX WITH THE MINIMUM VALUE
		for (int i = 1; i < 4; i++){
			if (statusTLBRowLRU[minimum] > statusTLBRowLRU[i])
				minimum = i;
		}
		return minimum;

	}//END FUNCTION getLRURow

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//FUNCTION TO CLOSE THIS FRAME
	public void removeInstance(){
		this.dispose();
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}//END CLASS VMFrame

