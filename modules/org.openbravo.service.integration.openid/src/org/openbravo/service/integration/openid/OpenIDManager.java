/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.integration.openid;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openid4java.OpenIDException;
import org.openid4java.association.AssociationSessionType;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

/**
 * 
 * @author iperdomo
 */
@SuppressWarnings("rawtypes")
public class OpenIDManager implements OBSingleton {

  private static ConsumerManager manager;
  private static OpenIDManager instance;

  private static final Logger log = Logger.getLogger(OpenIDManager.class);

  private static Map<String, DiscoveryInformation> discoveryInformationMap;

  public static final String ATTRIBUTE_EMAIL = "email";
  public static final String ATTRIBUTE_FIRSTNAME = "firstName";
  public static final String ATTRIBUTE_LASTNAME = "lastName";

  public static final String GOOGLE_OPENID_DISCOVER_URL = "https://www.google.com/accounts/o8/id";

  public static synchronized OpenIDManager getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(OpenIDManager.class);
      discoveryInformationMap = new HashMap<String, DiscoveryInformation>();
      manager = new ConsumerManager();
      manager.setAssociations(new InMemoryConsumerAssociationStore());
      manager.setNonceVerifier(new InMemoryNonceVerifier(5000));
      manager.setMinAssocSessEnc(AssociationSessionType.DH_SHA256);
    }
    return instance;
  }

  public String authRequest(String discoverURL, HttpServletRequest httpReq,
      HttpServletResponse httpResp) throws IOException, ServletException {
    try {
      // configure the return_to URL where your application will receive
      // the authentication responses from the OpenID provider
      // String returnToUrl = "http://example.com/openid";
      String returnToUrl = httpReq.getRequestURL().toString() + "?is_return=true";

      DiscoveryInformation discovered;
      if (discoveryInformationMap.containsKey(discoverURL)) {
        discovered = discoveryInformationMap.get(discoverURL);
      } else {
        // perform discovery on the user-supplied identifier
        List discoveries = manager.discover(discoverURL);

        // attempt to associate with the OpenID provider
        // and retrieve one service endpoint for authentication
        discovered = manager.associate(discoveries);

        discoveryInformationMap.put(discoverURL, discovered);
      }

      // store the discovery information in the user's session
      httpReq.getSession().setAttribute("openid-disc", discovered);

      // obtain a AuthRequest message to be sent to the OpenID provider
      AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

      FetchRequest fetch = FetchRequest.createFetchRequest();
      fetch.addAttribute(ATTRIBUTE_EMAIL, "http://axschema.org/contact/email", true);
      fetch.addAttribute(ATTRIBUTE_FIRSTNAME, "http://axschema.org/namePerson/first", true);
      fetch.addAttribute(ATTRIBUTE_LASTNAME, "http://axschema.org/namePerson/last", true);

      authReq.addExtension(fetch);

      httpReq.setAttribute("message", authReq);

      httpResp.sendRedirect(authReq.getDestinationUrl(true));
    } catch (OpenIDException e) {
      log.error("Error trying to authenticate with Google Services: " + e.getMessage(), e);
      httpResp.sendRedirect(httpReq.getHeader("Referer"));
    }

    return null;
  }

  public User getUser(Identifier oid) throws OBException {
    User u = null;

    OBCriteria<OBSOIDUserIdentifier> userCriteria = OBDal.getInstance().createCriteria(
        OBSOIDUserIdentifier.class);
    userCriteria
        .add(Restrictions.eq(OBSOIDUserIdentifier.PROPERTY_OPENIDIDENTIFIER, oid.toString()));
    userCriteria.setFilterOnReadableClients(false);
    userCriteria.setFilterOnReadableOrganization(false);

    OBSOIDUserIdentifier userIdentifier = (OBSOIDUserIdentifier) userCriteria.uniqueResult();
    if (userIdentifier != null) {
      u = userIdentifier.getUserContact();
    }
    return u;

  }

  public void associateAccount(Identifier oid, HttpServletRequest req, HttpServletResponse resp)
      throws Exception {
    // Map<String, String> userAttributes = (LinkedHashMap<String, String>) req
    // .getAttribute("attributes");

    User user = OBDal.getInstance().get(User.class, OBContext.getOBContext().getUser().getId());
    // TODO: Ask the user if he wants to update the email account and uncomment this code
    // if (!userAttributes.get(ATTRIBUTE_EMAIL).equals(user.getEmail())) {
    // try {
    // user.setEmail(userAttributes.get(ATTRIBUTE_EMAIL));
    // OBDal.getInstance().save(user);
    // OBDal.getInstance().flush();
    // } catch (Exception e) {
    // log.error("Error trying to update email for user: " + user.getUsername(), e);
    // }
    // }

    OBCriteria<OBSOIDUserIdentifier> oidCriteria = OBDal.getInstance().createCriteria(
        OBSOIDUserIdentifier.class);
    oidCriteria
        .add(Restrictions.eq(OBSOIDUserIdentifier.PROPERTY_OPENIDIDENTIFIER, oid.toString()));

    if (oidCriteria.count() > 0) {
      log.warn("Account association already exists - OpenID identifier: " + oid.toString());
      return;
    }

    OBSOIDUserIdentifier userIdentifier = OBProvider.getInstance().get(OBSOIDUserIdentifier.class);
    userIdentifier.setUserContact(user);
    userIdentifier.setOpenIDIdentifier(oid.toString());
    userIdentifier.setClient(OBContext.getOBContext().getRole().getClient());
    userIdentifier.setOrganization(OBContext.getOBContext().getRole().getOrganization());

    OBDal.getInstance().save(userIdentifier);
    OBDal.getInstance().flush();
  }

  public Identifier getIdentifier(HttpServletRequest httpReq) throws OBException {
    try {
      // extract the parameters from the authentication response
      // (which comes in as a HTTP request from the OpenID provider)
      ParameterList response = new ParameterList(httpReq.getParameterMap());

      // retrieve the previously stored discovery information
      DiscoveryInformation discovered = (DiscoveryInformation) httpReq.getSession().getAttribute(
          "openid-disc");

      // extract the receiving URL from the HTTP request
      StringBuffer receivingURL = httpReq.getRequestURL();
      String queryString = httpReq.getQueryString();
      if (queryString != null && queryString.length() > 0)
        receivingURL.append("?").append(httpReq.getQueryString());

      // verify the response; ConsumerManager needs to be the same
      // (static) instance used to place the authentication request
      VerificationResult verification = manager.verify(receivingURL.toString(), response,
          discovered);

      // examine the verification result and extract the verified
      // identifier
      Identifier verified = verification.getVerifiedId();
      if (verified != null) {
        AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

        receiveAttributeExchange(httpReq, authSuccess);

        return verified; // success
      }
    } catch (OpenIDException e) {
      // present error to the user
      throw new OBException(e);
    }

    return null;
  }

  @SuppressWarnings({ "unchecked" })
  private void receiveAttributeExchange(HttpServletRequest httpReq, AuthSuccess authSuccess)
      throws MessageException {
    if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
      FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);

      List aliases = fetchResp.getAttributeAliases();
      Map attributes = new LinkedHashMap();
      for (Iterator iter = aliases.iterator(); iter.hasNext();) {
        String alias = (String) iter.next();
        List values = fetchResp.getAttributeValues(alias);
        if (values.size() > 0) {
          String[] arr = new String[values.size()];
          values.toArray(arr);
          attributes.put(alias, StringUtils.join(arr));
        }
      }
      httpReq.setAttribute("attributes", attributes);
    }
  }
}
