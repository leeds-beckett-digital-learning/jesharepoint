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
import java.util.List;
import uk.ac.leedsbeckett.jesharepoint.odata.Value;

/**
 * A generic that represents a collection of entities, also known as a feed
 * in the terminology of Atom XML.
 * 
 * @author maber01
 * @param <T> The type of entity contained.
 */
public class EntityCollection<T extends Entity> extends Value
{
  final ArrayList<T> entityList = new ArrayList<>();
  
  /**
   * Add an entity to the collection.
   * @param e The new entity.
   */
  public void add( T e )
  {
    entityList.add( e );
  }
  
  /**
   * Get the list of entities in the collection
   * @return A list of type T
   */
  public List<T> getEntities()
  {
    return entityList;
  }
}
