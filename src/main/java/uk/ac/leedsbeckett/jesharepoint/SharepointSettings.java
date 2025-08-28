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
package uk.ac.leedsbeckett.jesharepoint;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import org.apache.http.client.CookieStore;
import uk.ac.leedsbeckett.jesharepoint.odata.http.FilteredCookieStore;
import uk.ac.leedsbeckett.jesharepoint.odata.ODataSettings;
import uk.ac.leedsbeckett.jesharepoint.odata.TypeMap;


/**
 * This class needs to instantiated by the application and values loaded
 * into it before instantiating the Sharepoint class. It specifies the 
 * location of the Sharepoint site for example.
 * 
 * @author maber01
 */
public class SharepointSettings extends Properties implements ODataSettings
{
  private static final Logger logger = Logger.getLogger( SharepointSettings.class.getName() );
  Path data;
  TypeMap typeMap = new TypeMap( "uk.ac.leedsbeckett.jesharepoint.sptypes" );
  
  /**
   * This standard constructor is used to create and initialise the settings.
   * 
   * @param data The path of a java properties format text file containing settings.
   */
  public SharepointSettings( Path data )
  {
    this.data = data;
    try ( FileReader reader = new FileReader( this.data.toFile() ) )
    {
      this.load( reader );
    }
    catch ( IOException ex )
    {
      logger.log( Level.SEVERE, null, ex );
    }
  }
  
  @Override
  public boolean isAcceptAnySSLCertificate()
  {
    return "true".equalsIgnoreCase( getProperty( "AcceptAnySSLCertificate" ) );
  }
  
  /**
   * This is an application specific property which ought not be here.
   * @return  The required property.
   */
  public String getMembersGroupName()
  {
    return this.getProperty( "MembersGroupName" );    
  }
  
  /**
   * This is an application specific property which ought not be here.
   * @return  The required property.
   */
  public String getModuleGroupPrefix()
  {
    return this.getProperty( "ModuleGroupPrefix" );    
  }

  /**
   * Tells OData how to access the site via an HTTP proxy service.
   * 
   * @return  The required property.
   */
  @Override
  public String getHttpProxyUrl()
  {
    return this.getProperty( "HttpProxyUrl" );
  }
  
  /**
   * The base URI of the Sharepoint site.
   * 
   * @return  The required property.
   */
  @Override
  public String getServiceUri()
  {
    return this.getProperty( "ServiceUri" );
  }
  
  /** 
   * URI of the Odata v3 context information for the site.
   * 
   * @return  The required property.
   */
  @Override
  public String getContextInfoUri()
  {
    return this.getProperty( "ContextInfoUri" );
  }
  
  /**
   * The path of a file which will store cookies across multiple
   * HTTP requests.
   * 
   * @return  The required property.
   */
  @Override
  public String getCookieStorePath()
  {
    return this.getProperty( "CookieStorePath" );    
  }

  /**
   * The domain of cookies of interest so others can be discarded
   * @return  The required property.
   */
  @Override
  public String getCookieDomain()
  {
    return this.getProperty( "CookieDomain" );    
  }
  
  /**
   * User by the OData implementation to get an instantiated cookie
   * store that is properly configured.
   * 
   * @return  The required property.
   */
  @Override
  public CookieStore getCookieStore()
  {
    return new FilteredCookieStore( Paths.get( getCookieStorePath() ), getCookieDomain() );
  }

  /**
   * Get a properly configured type map which is used to map
   * OData type names onto Java classes and vice versa.
   * 
   * @return  The required property.
   */
  @Override
  public TypeMap getTypeMap()
  {
    return typeMap;
  }
}
