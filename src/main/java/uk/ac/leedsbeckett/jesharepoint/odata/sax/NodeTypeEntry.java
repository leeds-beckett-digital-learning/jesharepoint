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

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.XmlDocument;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.XmlNode;

/**
 *
 * @author maber01
 */
public class NodeTypeEntry
{
  public final Class<?> c;
  public final Constructor constructor;

  public NodeTypeEntry( Class<?> c )
  {
    this.c = c;
    Constructor cc = null;
    try
    {
      cc = c.getConstructor( XmlDocument.class, XmlNode.class, String.class, String.class, Attributes.class );
    }
    catch ( NoSuchMethodException | SecurityException ex )
    {
      Logger.getLogger( NodeTypeEntry.class.getName() ).log( Level.SEVERE, null, ex );
    }
    constructor = cc;
  }
  
}
