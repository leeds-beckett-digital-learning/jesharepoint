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

import java.util.ArrayList;
import uk.ac.leedsbeckett.jesharepoint.odata.Value;

/**
 * Represents a navigation property, also known as deferred property, also
 * known as a link. In
 * some cases this property only describes how to fetch the data but it is
 * also possible that it will contain a collection of entities.
 * 
 * @author maber01
 * @param <T> The type of entities that may or may not be nested inside.
 */
public class NavigationProperty<T> extends Value
{
  boolean deferred = true;
  final String deferredUri;
  final ArrayList<T> entityList = new ArrayList<>();

  /**
   * Instantiate with a specific URI.
   * @param deferredUri The URI where the property data may be found.
   */
  public NavigationProperty( String deferredUri )
  {
    this.deferredUri = deferredUri;
  }
  
  /**
   * Instantiate without a specific URI
   */
  public NavigationProperty()
  {
    this.deferredUri = null;
  }
  
  /**
   * Add an entity to the navigation property.
   * @param entity The new entity.
   */
  public void addEntity( T entity )
  {
    entityList.add( entity );
  }

  /**
   * Set the deferred status.
   * @param deferred Indicates if the data load is deferred (true) or not (false).
   */
  public void setDeferred( boolean deferred )
  {
    this.deferred = deferred;
  }
  
  /**
   * Is the data load deferred?
   * @return True if deferred.
   */
  public boolean isDeferred()
  {
    return deferred;
  }

  /**
   * Get the URI of it.
   * @return Get the URI where the data can be fetched.
   */
  public String getDeferredUri()
  {
    return deferredUri;
  }

  /**
   * Get the number of entities that have been loaded.
   * @return The size of the entity set if it was loaded.
   */
  public int size()
  {
    return entityList.size();
  }
  
  /**
   * Get a specific entity indexed from start of list.
   * 
   * @param n The zero based index.
   * @return The entity at given distance from start.
   */
  public T getEntity( int n )
  {
    return entityList.get( n );
  }
}
