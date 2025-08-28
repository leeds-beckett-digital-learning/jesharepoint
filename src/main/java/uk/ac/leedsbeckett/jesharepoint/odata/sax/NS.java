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
public class NS
{
  // Atom elements wrap one or a collection of odata entities
  // or describe them in various ways
  public static final String ATOM = "http://www.w3.org/2005/Atom";
  
  // Elements that describe standard OData stuff
  public static final String  ODATA_META = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
  
  // Elements that describe application specific (e.g. sharepoint) OData
  public static final String  ODATA = "http://schemas.microsoft.com/ado/2007/08/dataservices";
}