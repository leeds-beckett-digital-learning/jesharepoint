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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.Entity;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.EntityCollection;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.Property;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.ValueWithProperties;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.ODataSaxHandler;
import uk.ac.leedsbeckett.jesharepoint.odata.sax.nodes.XmlDocument;

/**
 * The entry point for OData operations. One instance represents one security
 * principal's session with one Odata service provider 
 * (e.g. one Sharepoint site.)
 * 
 * @author maber01
 */
public class ODataService
{
  private static final Logger logger = Logger.getLogger(ODataService.class.getName() );
  
  ODataSettings settings;
  TypeMap typeMap;
  protected HttpRoutePlanner routePlanner = null;
  protected HttpClientBuilder clientBuilder;
  CookieStore cookieStore;

  SAXParserFactory spf;
  SAXParser saxParser;
  HttpRequestPrepper prep = null;
  
  /**
   * Instantiates ODataService with given settings. The settings provides
   * information such as the base URI of service endpoints.
   * 
   * @param settings The configured settings.
   */
  public ODataService( ODataSettings settings )
  {
    this.settings = settings;
    cookieStore = settings.getCookieStore();
    typeMap = settings.getTypeMap();
    recreateClientBuilder();

    spf = SAXParserFactory.newInstance();
    spf.setNamespaceAware(true);
    try    
    {
      saxParser = spf.newSAXParser();
    }
    catch ( ParserConfigurationException | SAXException ex )
    {
      Logger.getLogger(ODataService.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }

  /**
   * A hook so HTTP request headers can be customised. Created so that
   * special Sharepoint headers can be injected.
   * 
   * @param prep The Apache HTTP client request object.
   */
  public void setHttpRequestPrepper( HttpRequestPrepper prep )
  {
    this.prep = prep;
  }

  /**
   * Creates an Apache HTTP client builder with properly configured
   * cookie store and proxy server.
   */
  private void recreateClientBuilder()
  {
    clientBuilder = HttpClients.custom();
    String httpsproxyurl = settings.getHttpProxyUrl();
    if ( StringUtils.isBlank( httpsproxyurl ) )
    {
      routePlanner = null;
    }
    else
    {
      HttpHost host = HttpHost.create( httpsproxyurl );
      routePlanner = new DefaultProxyRoutePlanner( host );
      clientBuilder.setRoutePlanner( routePlanner );
    }
    clientBuilder.setDefaultCookieStore( cookieStore );
  }

  /**
   * Takes a relative URL of the form found in odata links and converts to
   * a full URL suitable for Apache HTTP client.
   * 
   * @param target The relative URI.
   * @param query An optional query string or null.
   * @return The full URL.
   * @throws UnsupportedEncodingException If the inputs are invalid.
   */
  private String toFullUrl( String target, String query ) throws UnsupportedEncodingException
  {
    StringBuilder sb = new StringBuilder();
    sb.append( encodeODataUrl( target ) );
    if ( query != null )
    {
      sb.append( "?" );
      sb.append( encodeODataUrl( query ) );
    }
    return sb.toString();
  }

  /**
   * Use the HTTP GET method to fetch an OData entity from an endpoint.
   * 
   * @param <T> A type that is a subclass of Value.
   * @param expectedValueClass The expected return type
   * @param target The URI
   * @param query Optional query string
   * @return An ODataResponse containing the value if it was found.
   * @throws UnsupportedEncodingException Issue with the URI to the endpoint.
   * @throws IOException Issue with the HTTP request/response.
   */
  public <T extends Value> ODataResponse<T> get( Class<T> expectedValueClass, String target, String query )
          throws UnsupportedEncodingException, IOException
  {
    final HttpGet request = new HttpGet( toFullUrl( target, query ) );
    request.addHeader( "Accept", "application/atom+xml" );
    return executexml( expectedValueClass, request );
  }

  /**
   * Use the HTTP GET method to fetch an OData collection of entities from an endpoint.
   * 
   * @param <T> A type that is a subclass of Value.
   * @param expectedValueClass The expected type of the entities
   * @param target The URI
   * @param query Optional query string
   * @return An ODataResponse containing the collection if it was found.
   * @throws UnsupportedEncodingException Issue with the URI to the endpoint.
   * @throws IOException Issue with the HTTP request/response.
   */
  public <T extends Entity> ODataResponse<EntityCollection<T>> getEC( Class<T> expectedValueClass, String target, String query )
          throws UnsupportedEncodingException, IOException
  {
    final HttpGet request = new HttpGet( toFullUrl( target, query ) );
    request.addHeader( "Accept", "application/atom+xml" );
    return executeEC( expectedValueClass, request );
  }

  private static final String ATOM_TEMPLATE_MAIN = 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<entry xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\"\n" +
            "       xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\"\n" + 
            "       xmlns=\"http://www.w3.org/2005/Atom\">\n" +
            "  <category term=\"{0}\" scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"/>\n" +
            "  <content type=\"application/xml\">\n" +
            "{1}" +
            "  </content>\n" +
            "</entry>";
  private static final String ATOM_TEMPLATE_PROP = "    <m:properties><d:{0}>{1}</d:{0}></m:properties>\n";

  /**
   * Converts an entity to an XML string ready to be posted in an HTTP request.
   * 
   * @param entity The entity to represent.
   * @param propertyNames Which properties to include.
   * @return XML representation of the entity.
   */
  public String entityToPayload( Entity entity, String[] propertyNames )
  {
    Class eclass = entity.getClass();
    String type = typeMap.getType( eclass );
    // ToDo throw exception here
    if ( type == null ) return null;
    StringBuilder sb = new StringBuilder();
    for ( String pname : propertyNames )
    {
      try
      {
        Field field = eclass.getField( pname );
        Object obj = field.get( entity );
        // ToDo generalise this to work with other types of fields
        // This will only work with String, int, boolean properties
        if ( obj != null )
          sb.append( MessageFormat.format( ATOM_TEMPLATE_PROP, pname, obj.toString() ) );
      }
      catch ( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex )
      {
      }
    }
    return MessageFormat.format( ATOM_TEMPLATE_MAIN, type, sb.toString() );
  }
  
  /**
   * Posts data to an OData endpoint and fetches the result.
   * 
   * @param <T> A type that subclasses Value
   * @param expectedValueClass The expected value class.
   * @param target The endpoint
   * @param query A query string or null
   * @param payload Optional payload in XML format
   * @return A response that contains the value or is empty
   * @throws UnsupportedEncodingException Issue with the URI to the endpoint.
   * @throws IOException Issue with the HTTP request/response.
   */
  public <T extends Value> ODataResponse<T> post( Class<T> expectedValueClass, String target, String query, String payload )
          throws UnsupportedEncodingException, IOException
  {
    final HttpPost request = new HttpPost( toFullUrl( target, query ) );
    request.addHeader( "Accept", "application/atom+xml" );
    //request.addHeader( "Content-Type", "application/json; odata=verbose" );
    request.addHeader( "Content-Type", "application/atom+xml" );
    request.setEntity( new StringEntity( payload==null?"":payload ) );
    return executexml( expectedValueClass, request );
  }

  /**
   * Used to access an endpoint when an entity collection is expected.
   * 
   * @param <T> Type that is subclass of Entity.
   * @param expectedValueClass The expected return class
   * @param request The Apache HTTP client request
   * @return A response that contains the collection if found.
   * @throws IOException Issue with the HTTP request/response.
   */
  public <T extends Entity> ODataResponse<EntityCollection<T>> executeEC( Class<T> expectedValueClass, HttpRequestBase request ) 
          throws IOException
  {
    ODataResponse<EntityCollection<T>> response = new ODataResponse<>( EntityCollection.class, expectedValueClass );
    executexml( response, request );
    return response;
  }
  
  /**
   * Used to access an endpoint when an entity collection is NOT expected.
   * 
   * @param <T> Type that is subclass of Value.
   * @param expectedValueClass The expected return class
   * @param request The Apache HTTP client request
   * @return A response that contains the value if found.
   * @throws IOException Issue with the HTTP request/response.
   */
  private <T extends Value> ODataResponse<T> executexml( Class<T> expectedValueClass, HttpRequestBase request ) 
          throws IOException
  {
    ODataResponse<T> response = new ODataResponse<>( expectedValueClass, null );
    executexml( response, request );
    return response;
  }
  
  /**
   * The method that does all the work. Used for all methods - GET, POST etc.
   * and for all expected types of response - entities, entity collections, 
   * properties etc.
   * 
   * @param <T> A type that subclasses Value
   * @param odataresponse The expected class of the return value.
   * @param request An Apache HTTP client request.
   * @throws IOException Issue with the HTTP request/response.
   */
  private <T extends Value> void executexml( ODataResponse<T> odataresponse, HttpRequestBase request ) 
          throws IOException
  {
    if ( prep != null )
      prep.prepRequest( request );
    
    try (
            CloseableHttpClient client = clientBuilder.build();
            CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)
        )
    {
      int status = response.getStatusLine().getStatusCode();      
      if ( (status/100) != 2 && status !=404 )
      {
        String error = EntityUtils.toString( response.getEntity() );
        logger.severe( error );
        throw new IOException( "Problem fetching data. status = " + status );
      }
      if ( (status/100) == 2 )
      {
        try
        {
          InputSource inputSource = new InputSource( response.getEntity().getContent() );
          XMLReader xmlReader = saxParser.getXMLReader();
          Header h = response.getFirstHeader( "Content-Type" );
          ODataSaxHandler handler = new ODataSaxHandler( h==null?null:h.getValue(), typeMap );
          xmlReader.setContentHandler( handler );
          xmlReader.parse( inputSource );
          //logger.info( handler.getLog() );
          XmlDocument doc = handler.getXmlDocument();
          if ( EntityCollection.class.isAssignableFrom( odataresponse.valueClass ) )
          {
            if ( !doc.isCollection() )
              throw new IOException( "Expected entity collection but didn't get one." );
            try
            {
              Constructor collectioncon = odataresponse.valueClass.getConstructor();
              EntityCollection ec = (EntityCollection) collectioncon.newInstance();
              typeSafeSetDCollection( odataresponse, ec, doc );
            }
            catch ( NoSuchMethodException | SecurityException | InstantiationException | 
                    IllegalAccessException | IllegalArgumentException | InvocationTargetException ex ) 
            {
              Logger.getLogger(ODataService.class.getName() ).log( Level.SEVERE, null, ex );
            }
          }
          else if ( ValueWithProperties.class.isAssignableFrom( odataresponse.valueClass ) || 
                               Property.class.isAssignableFrom( odataresponse.valueClass )    )
          {
            if ( doc.isCollection() )
              throw new IOException( "Didn't expect entity collection but got one." );
            Value v = doc.getODataValue();
            if ( !odataresponse.valueClass.isAssignableFrom( v.getClass() ) )
              throw new IOException( "Wrong data type in response." );
            typeSafeSetDValue( odataresponse, doc );
          }
          else
          {
            throw new IOException( "Expected data type unknown." );
          }
        }
        catch ( SAXException ex )
        {
          logger.log( Level.SEVERE, null, ex );
          throw new IOException( "Unable to parse XML response.", ex );
        }
      }
    }
  }
  
  @SuppressWarnings( "unchecked" )
  private <T extends Value> void typeSafeSetDValue( ODataResponse<T> odataresponse, XmlDocument doc )
  {
    Value v = doc.getODataValue();
    if ( odataresponse.valueClass.isInstance( v ) )
      odataresponse.setD( (T)v );
  }
  
  @SuppressWarnings( "unchecked" )
  private <T extends Value> void typeSafeSetDCollection( ODataResponse<T> odataresponse, EntityCollection ec, XmlDocument doc )
  {
    List<Entity> v = doc.getODataValues();
    for ( Entity e : v )
        ec.add( e );
    odataresponse.setD( (T)ec );
  }
  
  /**
   * Encodes parts of URL that need encoding which avoiding encoding stuff
   * that shouldn't be encoded.
   * 
   * @param s The input URL
   * @return The encoded version.
   * @throws UnsupportedEncodingException 
   */
  private static String encodeODataUrl( String s ) throws UnsupportedEncodingException
  {
    StringBuilder sb = new StringBuilder( s.length() * 3 / 2 );
    for ( int i=0; i<s.length(); i++ )
    {
      if ( s.charAt( i ) == ' ' )
        sb.append( "%20" );
      else
        sb.append( s.charAt( i) );
    }
    return sb.toString();
  }  
}
