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
package uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.ValueWithProperties;
import uk.ac.leedsbeckett.jesharepoint.odata.properties.BooleanProperty;
import uk.ac.leedsbeckett.jesharepoint.odata.properties.Int32Property;
import uk.ac.leedsbeckett.jesharepoint.odata.properties.StringProperty;

/**
 *
 * @author maber01
 */
public abstract class XmlNode
{
  public XmlDocument document;
  public XmlNode parent;
  public String namespace;
  public String name;
  public Attributes attributes;
  public ArrayList<XmlNode> children = new ArrayList<>();

  public XmlNode( XmlDocument document, XmlNode parent, String namespace, String name, Attributes attributes )
          throws SAXException
  {
    this.document   = document;
    this.parent     = parent;
    this.namespace  = namespace;
    this.name       = name;
    this.attributes = attributes;
    if ( parent != null )
      parent.addChild( this );
  }
  
  public void characters( char[] ch, int start, int length )
          throws SAXException
  {
    
  }
  
  public void end()
          throws SAXException
  {
  }
  
  public void addChild( XmlNode child )
  {
    children.add( child );
  }
  
  @SuppressWarnings( "unchecked" )
  public <T extends XmlNode> T getAncestor( Class<T> c )
  {
    XmlNode current = parent;
    while ( current != null )
    {
      if ( current.getClass().equals( c ) )
        return (T)current;
      current = current.parent;
    }
    return null;
  }
  
  @SuppressWarnings( "unchecked" )
  public <T extends XmlNode> List<T> getChildren( Class<T> c )
  {
    ArrayList<T> found = new ArrayList<>();
    for ( XmlNode node : children )
    {
      if ( node.getClass().equals( c ) )
        found.add( (T)node );
    }
    return found;
  }

  @SuppressWarnings( "unchecked" )
  public <T extends XmlNode> T getChild( Class<T> c )
  {
    ArrayList<T> found = new ArrayList<>();
    for ( XmlNode node : children )
    {
      if ( node.getClass().equals( c ) )
        found.add( (T)node );
    }
    if ( found.isEmpty() ) return null;
    return found.get( 0 );
  }
  
  public void processProperties( ValueWithProperties value, List<ODataProperty> props ) throws SAXException
  {
    Class c = value.getClass();
    for ( ODataProperty prop : props )
    {
      if ( prop.odatavalue == null )
        continue;
      
      try
      {
        Field f = c.getField( prop.name );
        if ( f != null )
        {
          if ( f.getType().isAssignableFrom( prop.odatavalue.getClass() ) )
          {
            f.set( value, prop.odatavalue );
          }
          // if target is a String...
          else if ( String.class.equals( f.getType() ) )
          {
            if ( !(prop.odatavalue instanceof StringProperty) )
              throw new SAXException( "Incompatible property type." );
            f.set( value, ((StringProperty)prop.odatavalue).getValue() );
          }
          else if ( int.class.equals( f.getType() ) )
          {
            if ( !(prop.odatavalue instanceof Int32Property) )
              throw new SAXException( "Incompatible property type." );
            f.set( value, ((Int32Property)prop.odatavalue).getValue() );
          }
          else if ( boolean.class.equals( f.getType() ) )
          {
            if ( !(prop.odatavalue instanceof BooleanProperty) )
              throw new SAXException( "Incompatible property type." );
            f.set( value, ((BooleanProperty)prop.odatavalue).getValue() );
          }
        }
      }
      catch ( NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex ) {}
    }    
  }
}
