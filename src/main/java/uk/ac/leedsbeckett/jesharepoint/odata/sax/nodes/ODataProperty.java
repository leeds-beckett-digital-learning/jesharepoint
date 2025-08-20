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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import uk.ac.leedsbeckett.jesharepoint.odata.Value;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.Complex;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.Entity;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.NavigationProperty;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.NS;

/**
 *
 * @author maber01
 */
public class ODataProperty extends ODataNode
{
  public String type;
  public String value;
  
  public Value odatavalue;
  
  public ODataProperty( XmlDocument document, XmlNode parent, String namespace, String name, Attributes attributes ) throws SAXException
  {
    super( document, parent, namespace, name, attributes );
    type = attributes.getValue( NS.ODATA_META, "type" );
    if ( type == null ) type = "Edm.String";
  }

  @Override
  public void characters( char[] ch, int start, int length )
  {
    value = new String( ch, start, length );
  }

  @Override
  public void end() throws SAXException
  {
    if ( document.getTypeMap().isSupportedPrimitive( type ) )
    {
      odatavalue = document.getTypeMap().getPrimitiveProperty( type, name, value );
      return;
    }
    
    // ToDo should be checking the scheme too
    Class<? extends Complex> complexClass = document.getTypeMap().getComplexClass( type );
    if ( complexClass == null )
      return;
    
    try
    {
      Constructor cons = complexClass.getConstructor();
      Complex complex = (Complex)cons.newInstance();
      List<ODataProperty> props = getChildren( ODataProperty.class );
      this.processProperties( complex, props );
      odatavalue = complex;
    }
    catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
    {
      Logger.getLogger( AtomEntry.class.getName() ).log( Level.SEVERE, null, ex );
      throw new SAXException( "Problem parsing XML to OData." );
    }
  }
}
