/**
 * Title:        UIAnimated
 * Description:  Methods called by UIAnimationController.
 * Copyright:    Copyright (c) 2006
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 *
 */


public interface UIAnimated {
    /* from the new thread - use to call the algorithm code */
    public void runAnimation() throws InterruptedException;
    
    /* use to clean up when animation is terminated */
    public void stopAnimation();
}
