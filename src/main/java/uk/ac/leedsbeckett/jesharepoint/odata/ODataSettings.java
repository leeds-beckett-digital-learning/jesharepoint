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

import org.apache.http.client.CookieStore;

/**
 * This interface tells the Sharepoint code what information the OData
 * implementation needs to be given to get started.
 * 
 * @author maber01
 */
public interface ODataSettings
{
  /**
   * HTTP Proxy location or null for direct access
   * @return The required property.
   */
  public String getHttpProxyUrl();
  
  /**
   * The service URI of the OData v3 service
   * @return The required property. 
   */
  public String getServiceUri();
  
  /**
   * The service's context information URI.
   * @return The required property. 
   */
  public String getContextInfoUri();
  
  /**
   * Location of the cookie store file
   * @return The required property. 
   */
  public String getCookieStorePath();
  
  /**
   * Cookie domain of interest
   * @return The required property. 
   */
  public String getCookieDomain();
  
  /** The fully configured and loaded cookie store to use.
   * 
   * @return The required property. 
   */
  public CookieStore getCookieStore();
  
  /**
   * A type map that tells the OData code how to map service
   * type names to Java classes.
   * 
   * @return The required property. 
   */
  public TypeMap getTypeMap();
}
