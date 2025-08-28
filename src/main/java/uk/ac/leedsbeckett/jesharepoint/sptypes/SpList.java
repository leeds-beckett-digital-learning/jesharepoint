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
package uk.ac.leedsbeckett.jesharepoint.sptypes;

import uk.ac.leedsbeckett.jesharepoint.odata.containers.Entity;
import uk.ac.leedsbeckett.jesharepoint.odata.annotation.ODataMapping;

/**
 *
 * @author maber01
 */
@ODataMapping( "SP.List" )
public class SpList extends Entity
{
  public String Id;
  public String Title;
  //public NavigationProperty Items;
}
