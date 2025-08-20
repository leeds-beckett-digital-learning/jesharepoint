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

/**
 * Represents a response from an OData service that optionally contains
 * a value. Uses Java generics so the application doesn't need to test
 * the type of the returned value.
 * 
 * @author maber01
 * @param <T> A type that subclasses Value.
 */
public class ODataResponse<T extends Value>
{
  public T d = null;

  public final Class valueClass;
  public final Class subClass;

  /**
   * Instantiates this class.
   */
  public ODataResponse()
  {
    valueClass=null;
    subClass=null;
  }
  
  /**
   * Instantiates with expected types recorded
   * @param valueClass The type of the expected value
   * @param subClass Usually null but if valueClass is collection, the type of objects in the collection.
   */
  public ODataResponse( Class valueClass, Class subClass )
  {
    this.valueClass = valueClass;
    this.subClass = subClass;
  }
  
  /**
   * Get the contained value.
   * 
   * @return The value or null if there is none.
   */
  public T getD()
  {
    return d;
  }

  /**
   * Set the contained value.
   * 
   * @param d The value to use.
   */
  public void setD( T d )
  {
    this.d = d;
  }
}
