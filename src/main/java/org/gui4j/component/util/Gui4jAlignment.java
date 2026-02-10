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
    mHorizontalAlign.put(LEFT, Integer.valueOf(SwingConstants.LEFT));
    mHorizontalAlign.put(CENTER, Integer.valueOf(SwingConstants.CENTER));
    mHorizontalAlign.put(RIGHT, Integer.valueOf(SwingConstants.RIGHT));
    mHorizontalAlign.put(LEADING, Integer.valueOf(SwingConstants.LEADING));
    mHorizontalAlign.put(TRAILING, Integer.valueOf(SwingConstants.TRAILING));

    mVerticalAlign.put(TOP, Integer.valueOf(SwingConstants.TOP));
    mVerticalAlign.put(CENTER, Integer.valueOf(SwingConstants.CENTER));
    mVerticalAlign.put(BOTTOM, Integer.valueOf(SwingConstants.BOTTOM));
  }
}
