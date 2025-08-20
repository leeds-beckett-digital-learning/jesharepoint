/*
 * Copyright 2025 maber01.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.leedsbeckett.jesharepoint.odata.sax;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reflections.Reflections;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import uk.ac.leedsbeckett.jesharepoint.odata.TypeMap;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.ODataMProperties;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.ODataProperty;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.TotallyUnknownNode;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.UnknownAtomNode;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.UnknownODataMetadataNode;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.UnknownODataNode;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.XmlDocument;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.XmlNode;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.annotation.AtomElementMapping;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.annotation.ODataElementMapping;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.annotation.ODataMElementMapping;

/**
 * This is the SAX handler that is used to process all incoming XML from the
 * OData v3 service.
 * 
 * @author maber01
 */
public class ODataSaxHandler extends DefaultHandler
{
  private static final HashMap<FQName, NodeTypeEntry> nodeTypeMap = new HashMap<>();

  String contentType;
  TypeMap typeMap;

  StringBuilder log = new StringBuilder();
  int depth = 0;

  XmlDocument documentNode;
  XmlNode currentNode;
  
  /**
   * Instantiated by OData client for each incoming XML HTTP response.
   * 
   * @param contentType The content type as reported by HTTP response header.
   * @param typeMap The typemap to use.
   */
  public ODataSaxHandler( String contentType, TypeMap typeMap )
  {
    this.contentType = contentType;
    this.typeMap = typeMap;
    synchronized ( nodeTypeMap )
    {
      if ( nodeTypeMap.isEmpty() )
      {
        scan();
      }
    }
  }

  /**
   * Get the object that represents the root node.
   * 
   * @return The XmlDocument that represents the root node.
   */
  public XmlDocument getXmlDocument()
  {
    return documentNode;
  }

  /**
   * User in development process to output log in one string.
   * 
   * @return The log contents.
   */  
  public String getLog()
  {
    return log.toString();
  }

  /**
   * Process start of document.
   * 
   * @throws SAXException If there is invalid XML content.
   */
  @Override
  public void startDocument() throws SAXException
  {
    documentNode = new XmlDocument( null, null, null, null, null );
    documentNode.setTypeMap( typeMap );
    currentNode = documentNode;
  }

  /**
   * Process end of document.
   * 
   * @throws SAXException If there is invalid XML content.
   */
  @Override
  public void endDocument() throws SAXException
  {
  }

  /**
   * Process the start of an XML element.
   * 
   * @param namespace The namespace
   * @param localName The element name
   * @param qName The element name possibly with prefix.
   * @param attributes Attributes from the element.
   * @throws SAXException If there is invalid XML content.
   */
  @Override
  public void startElement( String namespace, String localName, String qName, Attributes attributes ) throws SAXException
  {
    FQName fqname = new FQName( namespace, localName );
    XmlNode parentNode = currentNode;
    NodeTypeEntry entry=null;

    depth++;
    logIndent();
    log.append( "Start Element " );
    log.append( namespace );
    log.append( " " );
    log.append( localName );
    log.append( "\n" );
    depth += 3;
    for ( int i = 0; i < attributes.getLength(); i++ )
    {
      logIndent();
      log.append( "Attribute " );
      log.append( attributes.getURI( i ) );
      log.append( " " );
      log.append( attributes.getLocalName( i ) );
      log.append( " " );
      log.append( attributes.getValue( i ) );
      log.append( "\n" );
    }
    depth -= 3;

    try
    {
      entry = nodeTypeMap.get( fqname );
      if ( entry != null )
        currentNode = (XmlNode)entry.constructor.newInstance( documentNode, parentNode, namespace, localName, attributes );
      else
      {
        if ( null == namespace )
          currentNode = new TotallyUnknownNode( documentNode, parentNode, namespace, localName, attributes );
        else switch ( namespace )
        {
          case NS.ATOM:
            currentNode = new UnknownAtomNode( documentNode, parentNode, namespace, localName, attributes );
            break;
          case NS.ODATA_META:          
            currentNode = new UnknownODataMetadataNode( documentNode, parentNode, namespace, localName, attributes );
            break;
          case NS.ODATA:
            if ( parentNode instanceof ODataMProperties || parentNode instanceof ODataProperty )
              currentNode = new ODataProperty( documentNode, parentNode, namespace, localName, attributes );
            else
            {
              String odatatype = attributes.getValue( NS.ODATA_META, "type" );
              Class<?> dc = null;
              if ( odatatype != null )
                dc = typeMap.getClass( odatatype );
              if ( dc != null || typeMap.isSupportedPrimitive( odatatype ) )
                currentNode = new ODataProperty( documentNode, parentNode, namespace, localName, attributes );
              else
              {
                String odatanull = attributes.getValue( NS.ODATA_META, "null" );
                if ( odatanull != null )
                  currentNode = new ODataProperty( documentNode, parentNode, namespace, localName, attributes );
                else
                  currentNode = new UnknownODataNode( documentNode, parentNode, namespace, localName, attributes );
              }
            }
            break;
          default:
            currentNode = new TotallyUnknownNode( documentNode, parentNode, namespace, localName, attributes );
            break;
        }          
      }
    }
    catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | 
            InstantiationException ex )
    {
      Logger.getLogger( ODataSaxHandler.class.getName() ).log( Level.SEVERE, null, ex );
      throw new SAXException( "Fault parsing XML", ex );
    }
  }

  /**
   * End of an element.
   * @param uri Namespace of element
   * @param localName Name of element
   * @param qName Name of element possibly with prefix.
   * @throws SAXException If there is invalid XML content.
   */
  @Override
  public void endElement( String uri, String localName, String qName ) throws SAXException
  {
    depth--;
    currentNode.end();
    currentNode=currentNode.parent;
  }

  /**
   * Process text data.
   * @param ch Array of chars.
   * @param start Index in array where characters start.
   * @param length The number of incoming chars.
   * @throws SAXException If there is invalid XML content.
   */
  @Override
  public void characters( char[] ch, int start, int length ) throws SAXException
  {
    String s = new String( ch, start, length );
    if ( !s.isBlank() )
    {
      depth++;
      logIndent();
      log.append( "CHARS: " );
      log.append( s );
      log.append( "\n" );
      depth--;
    }
    
    currentNode.characters( ch, start, length );
  }

  /**
   * Indent the line of text in the log with spaces.
   */
  private void logIndent()
  {
    for ( int i = 0; i < depth; i++ )
      log.append( "  " );
  }

  /**
   * Scan the node classes, look at annotations and build map.
   */
  private static void scan()
  {
    Reflections reflections = new Reflections( "uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes" );
    Set<Class<?>> types;
    types = reflections.getTypesAnnotatedWith( AtomElementMapping.class );
    for ( Class<?> c : types ) addClass( c );
    types = reflections.getTypesAnnotatedWith( ODataMElementMapping.class );
    for ( Class<?> c : types ) addClass( c );
    types = reflections.getTypesAnnotatedWith( ODataElementMapping.class );
    for ( Class<?> c : types ) addClass( c );
  }
  
  /**
   * Add a class to the map.
   * 
   * @param c The class to add to the maps.
   */
  private static void addClass( Class<?> c )
  {
    if ( XmlNode.class.isAssignableFrom( c ) )
    {
      AtomElementMapping amapping = c.getAnnotation( AtomElementMapping.class );
      if ( amapping != null )
        nodeTypeMap.put( new FQName(       NS.ATOM,   amapping.value() ), new NodeTypeEntry( c ) );
      ODataMElementMapping odmmapping = c.getAnnotation( ODataMElementMapping.class );
      if ( odmmapping != null )
        nodeTypeMap.put( new FQName( NS.ODATA_META, odmmapping.value() ), new NodeTypeEntry( c ) );
      ODataElementMapping odmapping = c.getAnnotation( ODataElementMapping.class );
      if ( odmapping != null )
        nodeTypeMap.put( new FQName( NS.ODATA_META, odmapping.value() ), new NodeTypeEntry( c ) );
    }
  }

}
