package org.dom4j.io;

import java.io.StringReader;
import org.dom4j.DocumentException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class LNSAXReader extends SAXReader {
  private static final String FEATURE_DISALLOW_DOCTYPE_DECL =
      "http://apache.org/xml/features/disallow-doctype-decl";
  private static final String FEATURE_EXTERNAL_GENERAL_ENTITIES =
      "http://xml.org/sax/features/external-general-entities";
  private static final String FEATURE_EXTERNAL_PARAMETER_ENTITIES =
      "http://xml.org/sax/features/external-parameter-entities";
  private static final String FEATURE_LOAD_EXTERNAL_DTD =
      "http://apache.org/xml/features/nonvalidating/load-external-dtd";
  private static final EntityResolver NO_EXTERNAL_ENTITIES_RESOLVER =
      (publicId, systemId) -> new InputSource(new StringReader(""));

  public LNSAXReader() {
    super();
    hardenXmlParser();
  }

  /**
   * @param validating
   */
  public LNSAXReader(boolean validating) {
    super(validating);
    hardenXmlParser();
  }

  /**
   * @param xmlReader
   */
  public LNSAXReader(XMLReader xmlReader) {
    super(xmlReader);
    hardenXmlParser();
  }

  /**
   * @param xmlReader
   * @param validating
   */
  public LNSAXReader(XMLReader xmlReader, boolean validating) {
    super(xmlReader, validating);
    hardenXmlParser();
  }

  /**
   * @param xmlReaderClassName
   * @throws org.xml.sax.SAXException
   */
  public LNSAXReader(String xmlReaderClassName) throws SAXException {
    super(xmlReaderClassName);
    hardenXmlParser();
  }

  /**
   * @param xmlReaderClassName
   * @param validating
   * @throws org.xml.sax.SAXException
   */
  public LNSAXReader(String xmlReaderClassName, boolean validating) throws SAXException {
    super(xmlReaderClassName, validating);
    hardenXmlParser();
  }

  /* (non-Javadoc)
   * @see org.dom4j.io.SAXReader#createContentHandler(org.xml.sax.XMLReader)
   */
  protected SAXContentHandler createContentHandler(XMLReader reader) {
    return new LNSAXContentHandler(new LDocumentFactory(), getDispatchHandler());
  }

  protected void configureReader(XMLReader reader, DefaultHandler handler) throws DocumentException {
    super.configureReader(reader, handler);
    reader.setEntityResolver(NO_EXTERNAL_ENTITIES_RESOLVER);
    setSecureFeature(reader, FEATURE_DISALLOW_DOCTYPE_DECL, true);
    setSecureFeature(reader, FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
    setSecureFeature(reader, FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);
    setSecureFeature(reader, FEATURE_LOAD_EXTERNAL_DTD, false);
  }

  private void hardenXmlParser() {
    setIncludeExternalDTDDeclarations(false);
    setEntityResolver(NO_EXTERNAL_ENTITIES_RESOLVER);
    setSecureFeature(this, FEATURE_DISALLOW_DOCTYPE_DECL, true);
    setSecureFeature(this, FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
    setSecureFeature(this, FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);
    setSecureFeature(this, FEATURE_LOAD_EXTERNAL_DTD, false);
  }

  private void setSecureFeature(SAXReader reader, String feature, boolean value) {
    try {
      reader.setFeature(feature, value);
    } catch (SAXException ex) {
      // Ignore unsupported parser features; supported ones are still enforced.
    }
  }

  private void setSecureFeature(XMLReader reader, String feature, boolean value) {
    try {
      reader.setFeature(feature, value);
    } catch (SAXException ex) {
      // Ignore unsupported parser features; supported ones are still enforced.
    }
  }
}
