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
import uk.ac.leedsbeckett.jesharepoint.odata.containers.Entity;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.Metadata;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.NavigationProperty;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.annotation.AtomElementMapping;

/**
 *
 * @author maber01
 */
@AtomElementMapping( "entry" )
public class AtomEntry extends AtomNode
{
  Entity entity = null;
  
  public AtomEntry( XmlDocument document, XmlNode parent, String namespace, String name, Attributes attributes ) throws SAXException
  {
    super( document, parent, namespace, name, attributes );
  }

  @Override
  public void end() throws SAXException
  {
    AtomCategory category = this.getChild( AtomCategory.class );
    if ( category == null ) return;
    AtomId id = this.getChild( AtomId.class );

    // ToDo should be checking the scheme too
    Class<? extends Entity> entityClass = document.getTypeMap().getEntityClass( category.entityterm );
    if ( entityClass == null )
      entityClass = document.getTypeMap().getUnknownEntityClass();
    try
    {
      Constructor cons = entityClass.getConstructor();
      entity = (Entity) cons.newInstance();
      entity.__metadata = new Metadata();
      if ( id != null && id.atomid != null )
        entity.__metadata.id = id.atomid;

      List<AtomLink> links = this.getChildren( AtomLink.class );
      for ( AtomLink link : links )
      {
        if ( link.href != null && link.title != null && 
                ( "application/atom+xml;type=feed".equals( link.linkType ) || 
                  "application/atom+xml;type=entry".equals( link.linkType ) ) )
        {
          try
          {
            Field f = entityClass.getField( link.title );
            if ( !NavigationProperty.class.isAssignableFrom( f.getType() ) )
              throw new SAXException( "Incompatible with navigation property type." );
            Constructor pcons = f.getType().getConstructor( String.class );
            NavigationProperty<?> p = (NavigationProperty<?>) pcons.newInstance( link.href );
            f.set( entity, p );
            if ( link.inline != null )
            {
              p.setDeferred( false );
              Method adder = f.getType().getMethod( "addEntity", Object.class );
              if ( link.inline.inlineEntry != null && link.inline.inlineEntry.entity != null )
                adder.invoke( p, link.inline.inlineEntry.entity );
              if ( link.inline.inlineFeed != null && link.inline.inlineFeed.entities != null )
                for ( Entity e : link.inline.inlineFeed.entities )
                  adder.invoke( p, e );
            }
          }
          catch ( NoSuchFieldException ex ) {}
        }
      }

      AtomContent content = this.getChild( AtomContent.class );
      if ( content == null )
        return;
      ODataMProperties properties = content.getChild( ODataMProperties.class );
      if ( properties == null )
        return;
      List<ODataProperty> props = properties.getChildren( ODataProperty.class );
      this.processProperties( entity, props );
    }
    catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
    {
      Logger.getLogger( AtomEntry.class.getName() ).log( Level.SEVERE, null, ex );
      throw new SAXException( "Problem parsing XML to OData." );
    }
  }
  
  
}
