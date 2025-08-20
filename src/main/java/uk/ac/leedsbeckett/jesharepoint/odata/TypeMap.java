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
package uk.ac.leedsbeckett.jesharepoint.odata;

import uk.ac.leedsbeckett.jesharepoint.odata.containers.UnknownEntity;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.Entity;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import org.reflections.Reflections;
import uk.ac.leedsbeckett.jesharepoint.odata.annotation.ODataMapping;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.Complex;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.Property;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.UnknownComplex;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.ValueWithProperties;
import uk.ac.leedsbeckett.jesharepoint.odata.properties.BooleanProperty;
import uk.ac.leedsbeckett.jesharepoint.odata.properties.Int32Property;
import uk.ac.leedsbeckett.jesharepoint.odata.properties.StringProperty;

/**
 * The TypeMap is instantiated and configured by the Sharepoint client as
 * a way to tell the OData client how to map Sharepoint's data types onto
 * Java classes and vice versa. The classes must be annotated with ODataMapping
 * and must (via parent classes) subclass ValueWithProperties which means
 * subclassing Entity or Complex.
 * 
 * @author maber01
 */
public class TypeMap
{
  final HashMap<String,Class<? extends ValueWithProperties>> map = new HashMap<>();
  final HashMap<Class<?>,String> reversemap = new HashMap<>();
  final String packagePrefix;
  
  /**
   * Instantiates a TypeMap which will immediately scan the package for
   * annotated classes.
   * 
   * @param packagePrefix The package prefix defines the package(s) that will be searched.
   */
  public TypeMap( String packagePrefix )
  {
    this.packagePrefix = packagePrefix;
    scan();    
  }

  public String getPackagePrefix()
  {
    return packagePrefix;
  }
  
  private void scan()
  {
    // Search only specified package
    Reflections reflections = new Reflections( packagePrefix );
    Set<Class<?>> types = reflections.getTypesAnnotatedWith(ODataMapping.class);
    for ( Class<?> c : types )
      addClass( c );
  }
  
  private void addClass( Class<?> c )
  {
    if ( ValueWithProperties.class.isAssignableFrom( c ))
    {
      ODataMapping mapping = c.getAnnotation(ODataMapping.class );
      if ( mapping != null )
      {
        map.put( mapping.value(), c.asSubclass( ValueWithProperties.class ) );
        reversemap.put( c, mapping.value() );
      }
    }
  }
  
  public boolean isSupportedPrimitive( String type )
  {
    return "Edm.String".equals( type ) || "Edm.Boolean".equals( type ) || "Edm.Int32".equals( type );
  }

  public Class<? extends Property> getPrimitivePropertyClass( String type )
  {
    if ( "Edm.Boolean".equals( type ) ) return BooleanProperty.class;
    if ( "Edm.Int32".equals( type ) ) return Int32Property.class;
    if ( "Edm.String".equals( type ) ) return StringProperty.class;
    return null;
  }
  
  public Property getPrimitiveProperty( String type, String name, String xmlvalue )
  {
    try
    {
      Class c = getPrimitivePropertyClass( type );
      if ( c == null ) return null;
      Constructor con = c.getConstructor();
      if ( con == null ) return null;
      Property p = (Property) con.newInstance();
      p.setName( name );
      p.setFromXmlRepresentation( xmlvalue );
      return p;
    }
    catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
    {
    }
    return null;
  }
  
  public String getType( Class c )
  {
    return reversemap.get( c );
  }
  
  public Class<? extends ValueWithProperties> getClass( String type )
  {
    return map.get( type );
  }
  
  public Class<? extends Entity> getEntityClass( String type )
  {
    Class<? extends Value> c = map.get( type );
    if ( c != null && Entity.class.isAssignableFrom( c ) )
      return (Class<? extends Entity>) c;
    return null;
  }
  
  public Class<? extends Complex> getComplexClass( String type )
  {
    Class<? extends Value> c = map.get( type );
    if ( c != null && Complex.class.isAssignableFrom( c ) )
      return (Class<? extends Complex>) c;
    return null;
  }
  
  public Class<? extends Entity> getUnknownEntityClass()
  {
    return UnknownEntity.class;
  }

  public Class<? extends Complex> getUnknownComplexClass()
  {
    return UnknownComplex.class;
  }
}
