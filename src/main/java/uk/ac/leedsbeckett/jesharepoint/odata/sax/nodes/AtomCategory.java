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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.NS;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.annotation.AtomElementMapping;

/**
 *
 * @author maber01
 */
@AtomElementMapping( "category" )
public class AtomCategory extends AtomNode
{
  String entityscheme;
  String entityterm;
  
  public AtomCategory( XmlDocument document, XmlNode parent, String namespace, String name, Attributes attributes ) throws SAXException
  {
    super( document, parent, namespace, name, attributes );
    if ( !(parent instanceof AtomEntry) )
      throw new SAXException( "Atom category element must be inside entry element." );
    entityscheme = attributes.getValue( "scheme" );
    entityterm   = attributes.getValue( "term" );
  } 
}
