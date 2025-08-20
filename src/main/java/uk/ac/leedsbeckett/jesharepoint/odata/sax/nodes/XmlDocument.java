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

import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import uk.ac.leedsbeckett.jesharepoint.odata.TypeMap;
import uk.ac.leedsbeckett.jesharepoint.odata.Value;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.Entity;

/**
 *
 * @author maber01
 */
public class XmlDocument extends XmlNode
{
  TypeMap typeMap;
  
  public XmlDocument( XmlDocument document, XmlNode parent, String namespace, String name, Attributes attributes ) throws SAXException
  {
    super( document, parent, namespace, name, attributes );
  }

  public TypeMap getTypeMap()
  {
    return typeMap;
  }

  public void setTypeMap( TypeMap typeMap )
  {
    this.typeMap = typeMap;
  }
  
  public boolean isCollection()
  {
    return !children.isEmpty() && children.get( 0 ) instanceof AtomFeed;
  }
  
  public Value getODataValue()
  {
    if ( children.isEmpty() ) return null;
    XmlNode node = children.get( 0 );
    if ( node instanceof AtomEntry )
      return ((AtomEntry)node).entity;
    if ( node instanceof ODataProperty )
      return ((ODataProperty)node).odatavalue;
    return null;
  }

  public List<Entity> getODataValues()
  {
    if ( children.isEmpty() ) return null;
    XmlNode node = children.get( 0 );
    if ( !(node instanceof AtomFeed) )
      return null;
    AtomFeed feed = (AtomFeed)node;
    return feed.entities;
  }
}
