package placement.moves;
import placement.*;
import placement.ui.*;

/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */
/** PMove objects represent different types of moves - each move type
    is a different subclass that must perform the move.  Each subtype is a
    singleton object that is re-used on later move applications.  To get
    a move, use the static method PMove.selectMove().
    <br><br>
    A move goes through several steps:
    <ol>
    <LI>setup - assigns a move module, computes an offset if appropriate,
    but does not apply.
    <LI>apply - apply the move
    <LI>accept - mark move as accepted
    <LI>reject - mark move as rejected
    <LI>complete - mark move as completed (after this it is ignored)
    </ol>
    <br><br>
    Each move has a selection weight selectionWt; the sum of all selectionWts must be 1.0
    <br><br>
    This class also supports range limited given an initial window, intial
    temperature and updated temperature during cooling.
   */

public abstract class PMove {


    private PMoveStatus moveStatus = PMoveStatus.MOVE_COMPLETE; // initialize as pending when we use selectMove
    private double acceptProbability = 0.0;
    private double acceptRandom = 0.0;
    protected int deltaCost = 0;
    protected PModule moveModule;
    private double selectionWt = 0.0;
    protected int offset = 0;
    
    /** array of move instances - each move has a selectionWt; the sum of all
	weights must be 1.0.  Moves are initialized with some default
	weights - these can be changed with setSelectionWt()   */
    // alternative: sum weights and normalize on each try - then, weights don't
    // need to sum up!
    
    private static final PMove [] moveArray = {
	new PMoveV(0.35), new PMoveH(0.35), new PMoveRotate(0.1),
	new PMoveFlipH(0.1), new PMoveFlipV(0.1)
	    };
    
    private static PMoveHistory moveHistory = new PMoveHistory();

    public static PMoveHistory getHistory() { return moveHistory; }

    public static void resetHistory(int cost) { moveHistory.reset(cost); }
    
    abstract public UIMove getUIMove();
    
    /** select and apply a move by walking through the moveArray and
	MoveWeights array.  Note if a selectionWt entry is 0.0
	that move will not be selected, which is what we want */
    public static PMove selectMove(PModule pm) {
	PMove selMove;
	double r = Math.random();
	double sp = 0.0;
	for (int i=0; i< moveArray.length; i++) {
	    selMove = moveArray[i];
	    sp += selMove.selectionWt;
	    if (r < sp) {
		selMove.setup(pm);
		return selMove;
	    }
	}
	// if we get here it's an error, but salvage anyway
	selMove = moveArray[moveArray.length - 1];
	selMove.setup(pm);
	return selMove;
    }

  /** used by subclass to add "rejected" string when necessary */
  protected String moveString(String mstr) {
    mstr += " " + moveModule.getName();
    if (moveStatus == PMoveStatus.ACCEPT_PENDING || moveStatus == PMoveStatus.REJECT_PENDING)
      mstr += " dCost=" + deltaCost;
    return mstr + " [" + moveStatus.toString() + "]";
  }

  /** initialize a move - called for all subclasses */
  protected PMove(double sw) {
    moveStatus = PMoveStatus.MOVE_COMPLETE;
    selectionWt = sw;
  }

  public int getOffset() { return offset; }

  public int getDeltaCost() { return deltaCost; }

  public PModule getMoveModule() { return moveModule; }

  public PMoveStatus getMoveStatus() { return moveStatus; }

    public double getAcceptProbability() { return acceptProbability; }

    public double getAcceptRandom() { return acceptRandom; }

  /** initialize move before applying - override to provide specifics
      (move direction, move etc. */
  void setup(PModule pm) {
    moveModule = pm;
    moveStatus = PMoveStatus.MOVE_PENDING;
  }

  /** apply a move - override to do the move, but call super.apply() to
      update moveStatus */
  public void apply() {
    if (moveStatus != PMoveStatus.MOVE_PENDING)
      System.out.println("PMove.apply(): must only apply NEW moves!");
    else moveStatus = PMoveStatus.MOVE_APPLIED;
  }


    /** Downhill-only accept decision */

    public boolean greedyAccept() {
	if (moveStatus != PMoveStatus.MOVE_APPLIED) {  
	    System.out.println("PMove.greedyAccept(): can't accept move unless applied first!");
	    return false;  // really ought to throw an exception here!
	}
	if (deltaCost < 0) {
	    accept();
	    return true;
	} else {
	    reject();
	    return false;
	}
    }

  /** Use Metropolis algorithm to accept/reject a move.  It should not be necessary to override this */
    public boolean metropolisAccept(double temp) {
	if (moveStatus != PMoveStatus.MOVE_APPLIED) {  
	    System.out.println("PMove.metropolisAccept(): can't accept move unless applied first!");
	    return false;  // really ought to throw an exception here!
	}
	if (deltaCost < 0 || temp == Double.POSITIVE_INFINITY) {
	    acceptProbability = 1.0;
	    acceptRandom = 1.0;
	    accept();
	    return true;
	} else {
	    acceptProbability = Math.exp( (double)(-deltaCost) / temp );
	    if (acceptProbability > 1.0) System.out.println("AWK! accept probability > 1!");
	    acceptRandom = Math.random();
	    if (acceptRandom < acceptProbability) {
		accept();
		return true;
	    }
	}
	reject();
	return false;
    }

    public void accept() {
	if (moveStatus != PMoveStatus.MOVE_APPLIED)
	    System.out.println("PMove.accept(): can't reject move unless applied first!");
	else {
	    moveStatus = PMoveStatus.ACCEPT_PENDING;
	    moveHistory.add(new PMoveHistoryPoint(deltaCost,acceptProbability,acceptRandom));
	}
    }

    public void reject() {
	if (moveStatus != PMoveStatus.MOVE_APPLIED)
	    System.out.println("PMove.reject(): can't reject move unless applied first!");
	else {
	    moveStatus = PMoveStatus.REJECT_PENDING;
	    moveHistory.add(new PMoveHistoryPoint(deltaCost,acceptProbability,acceptRandom));
	}
    }
    
  /** mark a move as completed - must be overridden to reverse rejected
   *  moves (after undo, overriding method should call super.complete()
   *  to update state) */
  public void complete() {
    if (moveStatus != PMoveStatus.ACCEPT_PENDING && moveStatus != PMoveStatus.REJECT_PENDING)
      System.out.println("PMove.complete(): accept/reject not pending!");
    else {
	moveStatus = PMoveStatus.MOVE_COMPLETE;
	acceptProbability = -1.0;
	acceptRandom = -1.0;
    }
  }

  // windowing & range-limiting stuff

  private static int windowX = 0;
  private static int windowY = 0;
  private static double logT0 = 0.0;
  private static double windowScale = 1.0;

  public static void setWindow(int wx, int wy) {
    windowX = wx; windowY = wy;
  }

  public static void setT0(double t) {
    logT0 = Math.log(t);
    windowScale = 1.0;
  }

  public static void setT(double t) {
    if (t == Double.POSITIVE_INFINITY) windowScale = 1.0;
    else windowScale = Math.log(t) / logT0;
  }

  public static int genX() {
    if (windowScale <= 0) return 1;
    else return (int)(windowX * windowScale);
  }

  public static int genY() {
    if (windowScale <= 0) return 1;
    else return (int)(windowY * windowScale);
  }
}



