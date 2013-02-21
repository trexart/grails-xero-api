package org.grails.plugins.xeroapi;

import org.scribe.model.*;
import org.scribe.builder.api.*;

public class XeroPublicApi extends DefaultApi10a
{
  private static final String AUTHORIZATION_URL = "https://api.xero.com/oauth/Authorize?oauth_token=%s";
  
  @Override
  public String getAccessTokenEndpoint()
  {
    return "https://api.xero.com/oauth/AccessToken"; 
  }

  @Override
  public String getRequestTokenEndpoint()
  {
    return "https://api.xero.com/oauth/RequestToken";
  }
  
  @Override
  public String getAuthorizationUrl(Token requestToken)
  {
    return String.format(AUTHORIZATION_URL, requestToken.getToken());
  }
}