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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import uk.ac.leedsbeckett.jesharepoint.sptypes.SpFolder;
import uk.ac.leedsbeckett.jesharepoint.sptypes.SpGroup;
import uk.ac.leedsbeckett.jesharepoint.sptypes.SpRoleDefinition;
import uk.ac.leedsbeckett.jesharepoint.sptypes.SpUser;
import uk.ac.leedsbeckett.jesharepoint.odata.HttpRequestPrepper;
import uk.ac.leedsbeckett.jesharepoint.odata.ODataResponse;
import uk.ac.leedsbeckett.jesharepoint.odata.ODataService;
import uk.ac.leedsbeckett.jesharepoint.odata.Value;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.EntityCollection;
import uk.ac.leedsbeckett.jesharepoint.odata.properties.BooleanProperty;
import uk.ac.leedsbeckett.jesharepoint.odata.properties.StringProperty;
import uk.ac.leedsbeckett.jesharepoint.sptypes.SpContextWebInformation;

/**
 * This class is the entry point for applications. All supported Sharepoint
 * functionality is exposed here. Sharepoint data is represented by Java
 * classes in the sptypes package.
 * 
 * One instance of this class is required for each Sharepoint site that
 * must be accessed by a given security principal.
 * 
 * @author maber01
 */
public class Sharepoint implements HttpRequestPrepper
{
  private static final Logger logger = Logger.getLogger( Sharepoint.class.getName() );
  
  private final SharepointSettings settings;
  private ODataService oDataService;
  private SpContextWebInformation webInformation=null;

  SpRoleDefinition roleDefRead=null, roleDefEdit=null;
  
  public Sharepoint( SharepointSettings settings )
  {
    this.settings = settings;
    oDataService = new ODataService( settings );
    oDataService.setHttpRequestPrepper( this );
  }

  /**
   * This is a Sharepoint specific wrapper on the OData post method. It is needed because Sharepoint
   * adds an extra requirement to supply a request header with an up to date token in for all POST
   * method HTTP requests.
   * 
   * @param <T> A type which extends Value
   * @param expectedValueClass Tells the OData API what type of data is expected in the return Value.
   * @param target The URI of the endpoint
   * @param query An optional query string or null.
   * @param payload Data to be posted in the request entity in XML or Atom XML format.
   * @return The return value.
   * @throws UnsupportedEncodingException
   * @throws IOException 
   */
  private <T extends Value> ODataResponse<T> post( Class<T> expectedValueClass, String target, String query, String payload )
          throws UnsupportedEncodingException, IOException
  {
    if ( webInformation == null || (webInformation.FormDigestTimeoutSeconds - System.currentTimeMillis()) < 60000L )
    {
      ODataResponse<SpContextWebInformation> res = 
              oDataService.post( SpContextWebInformation.class, settings.getContextInfoUri(), null, null );
      if ( res != null && res.d != null )
        webInformation = res.getD();
    }
    
    return oDataService.post( expectedValueClass, target, query, payload );
  }
  
  /**
   * Use HTTP GET request to fetch the specified SpRoleDefinition
   * 
   * @param name The requested name.
   * @return The role definition found or null if not found.
   * @throws java.io.IOException Issue with the HTTP request/response.
   * @throws java.net.URISyntaxException Issue with the endpoint URI.
   */
  public SpRoleDefinition getRoleDefinition( String name ) throws IOException, URISyntaxException
  {
    String url = 
            settings.getServiceUri() +
            "roledefinitions/getbyname('" + name + "')";
    ODataResponse<SpRoleDefinition> response = oDataService.get( SpRoleDefinition.class, url, null );
    if ( response != null && response.d != null )
      return response.getD();
    return null;
  }

  /**
   * Gets a group with a given name.
   * 
   * @param groupName The name of the group in the sharepoint site
   * @return The group found or null if not found.
   * @throws IOException Issue with the HTTP request/response.
   * @throws URISyntaxException Issue with the endpoint URI.
   */
  public SpGroup getGroup( String groupName ) throws IOException, URISyntaxException
  {
    String url = 
            settings.getServiceUri() +
            "sitegroups/getbyname('" +
            groupName + 
            "')";
    ODataResponse<SpGroup> response = oDataService.get( SpGroup.class, url, null );
    if ( response != null && response.d != null )
      return response.getD();
    return null;
  }

  /**
   * Get the group expanded to include not just a link to the contained
   * users but the user data itself. Makes use of the $expand Odata query
   * parameter. More efficient than requesting the users separately.
   * 
   * @param groupName The name of the required group
   * @return The group found, including users or null if not found.
   * @throws IOException Issue with the HTTP request/response.
   * @throws URISyntaxException Issue with the endpoint URI.
   */
  public SpGroup getGroupWithUsers( String groupName ) throws IOException, URISyntaxException
  {
    String url = 
            settings.getServiceUri() +
            "sitegroups/getbyname('" +
            groupName + 
            "')?$expand=Users";
    ODataResponse<SpGroup> response = oDataService.get( SpGroup.class, url, null );
    if ( response.d != null )
      return response.getD();
    return null;
  }

  /**
   * Get a named group but if it does not yet exist creates it.
   * 
   * @param groupName The name of the required group.
   * @return The group found or created.
   * @throws IOException Issue with the HTTP request/response.
   * @throws URISyntaxException Issue with the endpoint URI.
   */
  public SpGroup getOrCreateGroup( String groupName ) throws IOException, URISyntaxException
  {
    SpGroup g = getGroup( groupName );
    if ( g!= null ) return g;
    String url = settings.getServiceUri() + "sitegroups";
    g = new SpGroup();
    g.Title = groupName;
    String[] pnames = {"Title"};
    String payload = oDataService.entityToPayload( g, pnames );
    ODataResponse<SpGroup> response = post(
                SpGroup.class,
                url, 
                null,
                payload );
    if ( response != null && response.d != null )
      return response.getD();
    return null;
  }

  /**
   * Get the users that belong to a given group.
   * 
   * @param group A group previously fetched from Sharepoint
   * @return A collection of users from the group which could be empty.
   * @throws IOException Issue with the HTTP request/response.
   * @throws URISyntaxException Issue with the endpoint URI.
   */
  public EntityCollection<SpUser> getGroupMembers( SpGroup group ) throws IOException, URISyntaxException
  {
    String url = settings.getServiceUri() + "sitegroups/getbyid(" + group.Id + ")/users";
    EntityCollection<SpUser> collection = new EntityCollection<>();
    ODataResponse<EntityCollection<SpUser>> response;
    response = oDataService.getEC( SpUser.class, url, null );
    if ( response != null && response.d != null )
      return response.getD();
    return null;
  }

  /**
   * Add a person to a given group
   * 
   * @param group The group to which a user must be added.
   * @param email The email address of the user.
   * @return Returns the user object for the email provided.
   * @throws IOException Issue with the HTTP request/response.
   * @throws URISyntaxException Issue with the endpoint URI.
   */  
  public SpUser createGroupUser( SpGroup group, String email  ) throws IOException, URISyntaxException
  {
    String loginName = "i:0#.f|membership|" + email;
    String url = settings.getServiceUri() + "sitegroups/getbyid(" + group.Id + ")/users";
    SpUser user = new SpUser();
    user.LoginName = loginName;
    String[] pnames = {"LoginName"};
    String payload = oDataService.entityToPayload( user, pnames );
    ODataResponse<SpUser> response = post( SpUser.class, url, null, payload );
    return response.d;
  }
  

  /**
   * Get a folder within the Sharepoint site from a URL that is relative to
   * the base URL of the site. If the folder is not found it is created and
   * returned.
   * 
   * @param serverRelativeUrl The relative URL
   * @return A folder object that was found or created.
   * @throws IOException Issue with the HTTP request/response.
   * @throws URISyntaxException Issue with the endpoint URI.
   */
  public SpFolder getOrCreateFolder( String serverRelativeUrl ) throws IOException, URISyntaxException
  {
    String url = 
            settings.getServiceUri() +
            "getfolderbyserverrelativeurl('" +
            serverRelativeUrl + 
            "')";
    ODataResponse<SpFolder> getresponse = oDataService.get( SpFolder.class, url, null );
    if ( getresponse != null && getresponse.getD() != null )
      return getresponse.getD();
    
    int n = serverRelativeUrl.lastIndexOf( "/" );
    String name = serverRelativeUrl.substring( n+1 );
    String parent = serverRelativeUrl.substring( 0, n );
    String posturl = 
            settings.getServiceUri() +
            "getfolderbyserverrelativeurl('" +
            parent + 
            "')/Folders";
    SpFolder folder = new SpFolder();
    folder.ServerRelativeUrl = name;
    String[] pnames = {"ServerRelativeUrl"};
    String payload = oDataService.entityToPayload( folder, pnames );    
    ODataResponse<SpFolder> postresponse = post( SpFolder.class, posturl, null, payload );
    return postresponse.getD();
  }

  /** 
   * Gets a field of type boolean from the item object that lies behind
   * a folder.
   * 
   * @param propertyName The name of the property.
   * @param folder The folder.
   * @return The value of the property which could be null.
   * @throws IOException Issue with the HTTP request/response.
   * @throws URISyntaxException Issue with the endpoint URI.
   */
  public Boolean getFolderItemBooleanProperty( String propertyName, SpFolder folder ) throws IOException, URISyntaxException
  {
    String url = folder.__metadata.id + "/listitemallfields/" + propertyName;
    ODataResponse<BooleanProperty> getresponse = oDataService.get( BooleanProperty.class, url, null );
    return getresponse.getD().getValue();
  }

  /**
   * Set whether the folder has unique role assignments or inherits them
   * from its parent folder. If changing from not unique to unique no role
   * assignments will be assigned and only site owners will be able to work with
   * the folder. Does not propagate changes to subfolders or contained files.
   * 
   * @param folder The folder to set.
   * @param unique The desired setting - true for unique, false for inherited.
   * @return Returns true if a non-null response was given.
   * @throws IOException Issue with the HTTP request/response.
   * @throws URISyntaxException Issue with the endpoint URI.
   */
  public boolean setFolderUniqueRoleAssignments( SpFolder folder, boolean unique ) throws IOException, URISyntaxException
  {
    String url;
    if ( unique )
      url = folder.__metadata.id + "/listitemallfields/breakroleinheritance(copyroleassignments=false,clearsubscopes=false)";
    else
      url = folder.__metadata.id + "/listitemallfields/resetroleinheritance";
    ODataResponse<StringProperty> postresponse = post( StringProperty.class, url, null, null );
    return postresponse!=null;
  }

  /**
   * Loads two 'standard' role definitions.
   * 
   * @throws IOException Issue with the HTTP request/response.
   * @throws URISyntaxException Issue with the endpoint URI.
   */
  private void getRoleDefinitions() throws IOException, URISyntaxException
  {
    if ( roleDefRead == null )
      roleDefRead = getRoleDefinition( "Read" );    
    if ( roleDefEdit == null )
      roleDefEdit = getRoleDefinition( "Edit" );    
  }

  /**
   * Adds/removes the standard VIEW/EDIT role assignments to a folder for a
   * specific principal (user or group).
   * 
   * @param folder The specific folder.
   * @param principalId The specific principal.
   * @param access Either view or edit as required.
   * @throws IOException Issue with the HTTP request/response.
   * @throws URISyntaxException Issue with the endpoint URI.
   */
  public void setFolderRoleAssignments( SpFolder folder, int principalId, AccessRoleEnum access )
          throws IOException, URISyntaxException
  {
    logger.info( "Set role on " + folder.ServerRelativeUrl + " to " + principalId + " " + access.name() );
    getRoleDefinitions();
    
    SpRoleDefinition wanted;
    switch ( access )
    {
      case VIEW:
        wanted = roleDefRead;
        break;
      case EDIT:
        wanted = roleDefEdit;
        break;
      default:
        wanted = null;
    }
            
    String url = folder.__metadata.id + 
                 "/listitemallfields/roleassignments/getbyprincipalid(" + 
                 principalId + 
                 ")/roledefinitionbindings";
    ODataResponse<EntityCollection<SpRoleDefinition>> response = oDataService.getEC( SpRoleDefinition.class, url, null );
    boolean foundWanted=false;
    if ( response.getD() != null )
    {
      for ( SpRoleDefinition found : response.getD().getEntities() )
      {
        if ( found.Id != roleDefRead.Id && found.Id != roleDefEdit.Id )
          continue;
        if ( wanted == null || found.Id != wanted.Id )
        {
          String removeurl = folder.__metadata.id + 
                 "/listitemallfields/roleassignments/removeroleassignment(principalid=" + 
                 principalId + 
                 ",roledefid=" + found.Id + ")";
          post( StringProperty.class, removeurl, null, null );
        }
        if ( wanted != null && found.Id == wanted.Id )
          foundWanted = true;
      }
    }
    if ( !foundWanted )
    {
      String addurl = folder.__metadata.id + 
             "/listitemallfields/roleassignments/addroleassignment(principalid=" + 
             principalId + 
             ",roledefid=" + wanted.Id + ")";
      post( StringProperty.class, addurl, null, null );
    }
  }

  /**
   * A method that allows this class to inject request headers before the
   * OData implementation submits the request to the server. Used to add the
   * Sharepoint specific X-RequestDigest header to POST method requests.
   * 
   * @param request The Apache HTTP client request to prep.
   */
  @Override
  public void prepRequest( HttpRequestBase request )
  {
    if ( request instanceof HttpPost && 
         webInformation != null && 
         webInformation.FormDigestValue != null )
      request.addHeader( "X-RequestDigest", webInformation.FormDigestValue );
  }
}
