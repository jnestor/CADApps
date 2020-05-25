package placement.anneal;

public interface PAnnealInterface {
  /** interesting event: move selected but not applied yet */
  public void showSelectMove() throws InterruptedException ;

  /** interesting event: move applied and about to be accepted */
  public void showAcceptMove() throws InterruptedException ;

  /** interesting event: move applied and about to be rejected */
  public void showRejectMove() throws InterruptedException ;

  /** interesting event: move complete */
  public void showCompleteMove() throws InterruptedException ;

  /** interesting event: end of temperature, about to update */
  public void showUpdateTemperature() throws InterruptedException ;
}
