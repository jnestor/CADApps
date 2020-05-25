package placement.moves;import java.awt.Color;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class PMoveStatus {
  private final String name;
  private Color moveColor;

  private PMoveStatus(String n, Color c) { name = n; moveColor = c; }

  public String toString() { return name; }
  public Color getColor() { return moveColor; }

  public static final PMoveStatus MOVE_PENDING =
      new PMoveStatus("Pending", Color.orange);
  public static final PMoveStatus MOVE_APPLIED =
      new PMoveStatus("Applied", Color.gray);
  public static final PMoveStatus REJECT_PENDING =
      new PMoveStatus("Rejected", Color.red);
  public static final PMoveStatus ACCEPT_PENDING =
      new PMoveStatus("Accepted", Color.green);
  public static final PMoveStatus MOVE_COMPLETE =
      new PMoveStatus("Complete", Color.yellow);
}
