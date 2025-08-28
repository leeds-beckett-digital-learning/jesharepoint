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
package uk.ac.leedsbeckett.jesharepoint.odata.containers;

import uk.ac.leedsbeckett.jesharepoint.odata.Value;

/**
 * An abstract class which is parent of primitive properties.
 * @author maber01
 */
public abstract class Property extends Value
{
  public String name;

  /**
   * Get the name of the property.
   * @return The name.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Set the name of the property.
   * @param name The new name value.
   */
  public void setName( String name )
  {
    this.name = name;
  }
  
  /**
   * Get the class of the value. This is overriden in subclasses.
   * @return A class representing an OData primitive.
   */
  public static Class getValueClass()
  {
    return void.class;
  }
  
  /**
   * Subclass implements this.
   * @param s A string that comes directly from the XML.
   */
  public abstract void setFromXmlRepresentation( String s );
  
  /**
   * Subclass implements this.
   * @return A string that can be output in XML to represent this property.
   */
  public abstract String getXmlRepresentation();
}
