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
package uk.ac.leedsbeckett.jesharepoint.odata.http;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

/**
 * A store that puts cookies into a simple text file and which filters out
 * cookies that don't match a specific domain. Session cookies are stored so
 * that multiple app runs can work like a single browser session.
 * 
 * @author maber01
 */
public class FilteredCookieStore extends BasicCookieStore
{
  private static final Logger logger = Logger.getLogger( FilteredCookieStore.class.getName() );
      
  private final Path storeLocation;
  private final String domain;
  private final HashSet<String> allowedCookieNames = new HashSet<>();
  private boolean changed = false;
  
  public FilteredCookieStore( Path storeLocation, String domain )
  {
    this.storeLocation = storeLocation;
    this.domain = domain;
    allowedCookieNames.add( "rtFa" );
    allowedCookieNames.add( "FedAuth" );
    load();
  }

  @Override
  public synchronized void addCookies( Cookie[] cookies )
  {
    for ( Cookie c : cookies )
//      if ( allowedCookieNames.contains( c.getName() ) &&
//           domain.equals( c.getDomain() )   )
      {
        super.addCookie( c );
        changed = true;
      }
    if ( changed )
      save();
  }

  @Override
  public synchronized void addCookie( Cookie cookie )
  {
//    if ( allowedCookieNames.contains( cookie.getName() ) &&
//         domain.equals( cookie.getDomain() )   )
    {
      super.addCookie( cookie );
      changed = true;
    }
    if ( changed )
      save();
  }

  @Override
  public List<Cookie> getCookies()
  {
    return super.getCookies();
  }

  
  
  private void loadCookie( String s )
  {
    int n = s.indexOf( '=' );
    if ( n < 0 ) return;
    BasicClientCookie cookie = new BasicClientCookie( 
            s.substring( 0, n ),
            s.substring( n+1  ) );
    cookie.setDomain( domain );
    super.addCookie( cookie );
  }
  
  private void load()
  {
    changed = false;
    if ( storeLocation == null )
      return;
    
    if ( !storeLocation.toFile().exists() )
      return;
    
    try ( Stream<String> stream = Files.lines( storeLocation ) )
    {
      stream.forEach( line -> this.loadCookie( line ) );
    }
    catch ( IOException ex )
    {
      logger.log( Level.SEVERE, null, ex );
    }
    for ( Cookie c : getCookies() )
      logger.info( "Cookie loaded " + c.getName() );
  }
  
  public void save()
  {
    changed = false;
    if ( storeLocation == null )
      return;
    try ( FileWriter writer = new FileWriter( storeLocation.toFile() ) )
    {
      for ( Cookie c : this.getCookies() )
      {
        writer.append( c.getName() ).append( "=" ).append( c.getValue() ).append( "\n" );
      }
    }
    catch ( IOException ex )
    {
      logger.log( Level.SEVERE, null, ex );
    }
  }
}
