package org.gui4j.component.util;

import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingConstants;

public class Gui4jAlignment {
  public static final Map mHorizontalAlign = new HashMap();
  public static final Map mVerticalAlign = new HashMap();

  public static final String LEFT = "left";
  public static final String CENTER = "center";
  public static final String RIGHT = "right";
  public static final String LEADING = "leading";
  public static final String TRAILING = "trailing";

  public static final String TOP = "top";
  public static final String BOTTOM = "bottom";

  static {
    mHorizontalAlign.put(LEFT, new Integer(SwingConstants.LEFT));
    mHorizontalAlign.put(CENTER, new Integer(SwingConstants.CENTER));
    mHorizontalAlign.put(RIGHT, new Integer(SwingConstants.RIGHT));
    mHorizontalAlign.put(LEADING, new Integer(SwingConstants.LEADING));
    mHorizontalAlign.put(TRAILING, new Integer(SwingConstants.TRAILING));

    mVerticalAlign.put(TOP, new Integer(SwingConstants.TOP));
    mVerticalAlign.put(CENTER, new Integer(SwingConstants.CENTER));
    mVerticalAlign.put(BOTTOM, new Integer(SwingConstants.BOTTOM));
  }
}
