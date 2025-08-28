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

/**
 *
 * @author maber01
 */
public class FQName
{
  String ns;
  String name;

  public FQName( String ns, String name )
  {
    this.ns = ns;
    this.name = name;
  }

  @Override
  public int hashCode()
  {
    return ns.hashCode() ^ name.hashCode();
  }

  @Override
  public String toString()
  {
    return ns + " " + name;
  }

  @Override
  public boolean equals( Object obj )
  {
    if ( !(this.getClass().equals( obj.getClass() ) ) )
      return false;
    FQName other = (FQName)obj;
    return name.equals( other.name ) && ns.equals( other.ns );
  }  
}
