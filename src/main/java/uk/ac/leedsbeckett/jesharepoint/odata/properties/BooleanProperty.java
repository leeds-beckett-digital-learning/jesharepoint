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
package uk.ac.leedsbeckett.jesharepoint.odata.properties;

import uk.ac.leedsbeckett.jesharepoint.odata.containers.Property;

/**
 * Represents a boolean primitive.
 * 
 * @author maber01
 */
public class BooleanProperty extends Property
{
  boolean value;

  /**
   * Getter for the value.
   * @return The value.
   */
  public boolean getValue()
  {
    return value;
  }

  /**
   * Setter of the value;
   * @param value The new value.
   */
  public void setValue( boolean value )
  {
    this.value = value;
  }

  /**
   * Use a string from out of the XML to set the value.
   * @param s The XML representation.
   */
  @Override
  public void setFromXmlRepresentation( String s )
  {
    setValue( "true".equalsIgnoreCase( s ) );
  }

  /**
   * Get the value in the necessary form for XML.
   * @return The value in XML format.
   */
  @Override
  public String getXmlRepresentation()
  {
    return getValue()?"true":"false";
  }
}
