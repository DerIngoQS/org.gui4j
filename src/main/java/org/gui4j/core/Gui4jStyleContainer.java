package org.gui4j.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dom4j.Attribute;
import org.dom4j.LElement;
import org.gui4j.exception.ErrorTags;
import org.gui4j.exception.Gui4jUncheckedException;

public final class Gui4jStyleContainer implements Serializable, ErrorTags {
  private static final long serialVersionUID = 1L;
  private final Map<String, Map<String, Map<String, String>>> mStyleMap;
  private final Map<String, String> mStyleMapResourceName;
  private final String mConfigurationName;
  private final String NOTHING = "Nothing";
  private final String DEFAULT = "Default";

  public Gui4jStyleContainer(String configurationName) {
    mStyleMap = new HashMap<String, Map<String, Map<String, String>>>();
    mStyleMapResourceName = new HashMap<String, String>();
    mConfigurationName = configurationName;
  }

  /**
   * @param resourceName
   * @param styleName name of style
   * @param styleExtends name of style to be extended
   */
  public void createStyle(String resourceName, String styleName, String styleExtends) {
    Map<String, Map<String, String>> style = getStyle(styleName, false);
    if (style != null && !resourceName.equals(mStyleMapResourceName.get(styleName))) {
      Object[] args = {styleName};
      throw new Gui4jUncheckedException.ResourceError(
          mConfigurationName, -1, RESOURCE_ERROR_style_defined_twice, args);
    }

    style = new HashMap<String, Map<String, String>>();
    mStyleMap.put(styleName, style);
    mStyleMapResourceName.put(styleName, resourceName);

    if (styleExtends != null) {
      Map<String, Map<String, String>> styleE = getStyle(styleExtends, true);
      for (Iterator<Map.Entry<String, Map<String, String>>> it = styleE.entrySet().iterator();
          it.hasNext(); ) {
        Map.Entry<String, Map<String, String>> entry = it.next();
        String elementName = entry.getKey();
        Map<String, String> attributes = entry.getValue();
        style.put(elementName, attributes);
      }
    }
  }

  private Map<String, Map<String, String>> getStyle(String styleName, boolean checkExistent) {
    Map<String, Map<String, String>> style = mStyleMap.get(styleName);
    if (checkExistent && style == null) {
      Object[] args = {styleName};
      throw new Gui4jUncheckedException.ResourceError(
          mConfigurationName, -1, RESOURCE_ERROR_style_not_defined, args);
    }
    return style;
  }

  public void addAttributes(String elementName, String styleName, List<Attribute> attributes) {
    Map<String, Map<String, String>> style = getStyle(styleName, true);
    Map<String, String> attrs = style.get(elementName);
    if (attrs != null) {
      Map<String, String> oldAttrs = attrs;
      attrs = new HashMap<String, String>();
      style.put(elementName, attrs);
      for (Iterator<Map.Entry<String, String>> it = oldAttrs.entrySet().iterator();
          it.hasNext(); ) {
        Map.Entry<String, String> entry = it.next();
        String name = entry.getKey();
        String value = entry.getValue();
        attrs.put(name, value);
      }
    } else {
      attrs = new HashMap<String, String>();
      style.put(elementName, attrs);
    }

    for (Iterator<Attribute> it = attributes.iterator(); it.hasNext(); ) {
      Attribute attr = it.next();
      attrs.put(attr.getName(), attr.getValue());
    }
  }

  /**
   * Copies all defined styles of <code>gui4jStyleContainer</code> If such a style is already
   * defined, an exception is thrown.
   *
   * @param gui4jStyleContainer
   */
  public void extendBy(Gui4jStyleContainer gui4jStyleContainer) {
    for (Iterator<Map.Entry<String, Map<String, Map<String, String>>>> it =
            gui4jStyleContainer.mStyleMap.entrySet().iterator();
        it.hasNext(); ) {
      Map.Entry<String, Map<String, Map<String, String>>> entry = it.next();
      String styleName = entry.getKey();
      Map<String, Map<String, String>> style = entry.getValue();

      Map<String, Map<String, String>> thisStyle = getStyle(styleName, false);

      if (thisStyle != null
          && !mStyleMapResourceName
              .get(styleName)
              .equals(gui4jStyleContainer.mStyleMapResourceName.get(styleName))) {
        Object[] args = {styleName};
        throw new Gui4jUncheckedException.ResourceError(
            mConfigurationName, -1, RESOURCE_ERROR_style_defined_twice, args);
      }

      mStyleMap.put(styleName, style);
      mStyleMapResourceName.put(
          styleName, gui4jStyleContainer.mStyleMapResourceName.get(styleName));
    }
  }

  /**
   * Element <code>e</code> represents a <code>Gui4jComponent</code>. The method inserts all
   * attributes in that instance defined by this style which are not defined by the element
   *
   * @param e
   */
  public void extend(LElement e) {
    String styleName = e.attributeValue(Gui4jComponentFactory.FIELD_Style);
    if (styleName != null && styleName.equals(NOTHING)) {
      return;
    }
    String name = e.getName();
    Map<String, String> attrs = null;
    if (styleName != null) {
      Map<String, Map<String, String>> style = getStyle(styleName, true);
      attrs = style.get(name);
      if (attrs == null) {
        // maybe we should not throw an exception
        // there is no problem if there are no attributes for this element

        /*
        throw new UncheckedGui4jException(
        	mConfigurationSource.toString()
        		+ ": style with name "
        		+ styleName
        		+ " for element "
        		+ name
        		+ " not defined");
        */
      }
    } else {
      Map<String, Map<String, String>> style = getStyle(DEFAULT, false);
      if (style != null) {
        attrs = style.get(name);
      }
    }
    if (attrs != null) {
      Set<String> names = new HashSet<String>();
      List<Attribute> attrList = e.attributes();
      for (Iterator<Attribute> it = attrList.iterator(); it.hasNext(); ) {
        Attribute attr = it.next();
        names.add(attr.getName());
      }

      boolean modified = false;
      for (Iterator<Map.Entry<String, String>> it = attrs.entrySet().iterator(); it.hasNext(); ) {
        Map.Entry<String, String> entry = it.next();
        if (!names.contains(entry.getKey())) {
          e.addAttribute(entry.getKey(), entry.getValue());
          /*
          attrList.add(
          	new Attribute(
          		(String) entry.getKey(),
          		(String) entry.getValue()));
          		*/
          modified = true;
        }
      }

      if (modified) {
        // e.setAttributes(attrList);
      }
    }
    e.addAttribute(Gui4jComponentFactory.FIELD_Style, NOTHING);
  }
}
