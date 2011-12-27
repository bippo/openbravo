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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_process;

import org.openbravo.base.secureApp.HttpSecureAppServlet;

public class Register extends HttpSecureAppServlet {

  /*
   * private static final long serialVersionUID = 1L;
   * 
   * public static final String PROTOCOL = "https"; public static final String HOST =
   * "butler.openbravo.com"; public static final int PORT = 443; public static final String PATH =
   * "/heartbeat-server/register";
   * 
   * @Override public void doPost(HttpServletRequest request, HttpServletResponse response) throws
   * ServletException, IOException { VariablesSecureApp vars = new VariablesSecureApp(request); try
   * { if (!HeartbeatProcess.isInternetAvailable(this)) { String message = Utility.messageBD(myPool,
   * "HB_INTERNET_UNAVAILABLE", vars.getLanguage()); log4j.error(message); advisePopUp(response,
   * "ERROR", "Registration", message); } } catch (ServletException e) { e.printStackTrace(); }
   * RegisterData[] data = RegisterData.select(this); if (data.length > 0) { RegisterData rd =
   * data[0]; if (rd.isregistrationactive == null || rd.isregistrationactive.equals("")) {
   * rd.isregistrationactive = "N"; RegisterData.updateIsRegistrationActive(this, "N"); } if
   * (rd.registrationId == null || rd.registrationId.equals("")) { String registrationId =
   * UUID.randomUUID().toString(); rd.registrationId = registrationId;
   * RegisterData.updateRegistrationId(this, registrationId); } String queryStr =
   * createQueryString(rd); String encodedQueryStr = HttpsUtils.encode(queryStr, "UTF-8"); String
   * result = null; String message = null; try { result = register(encodedQueryStr); if (result ==
   * null || result.equals(rd.registrationId)) { // TODO Something went wrong. Handle. }
   * 
   * if ((!rd.registrationId.equals("") && rd.registrationId != null) &&
   * (rd.isregistrationactive.equals("") || rd.isregistrationactive == null ||
   * rd.isregistrationactive .equals("N"))) message = Utility.messageBD(myPool, "REG_UNREGISTER",
   * vars .getLanguage()); else message = Utility.messageBD(myPool, "REG_SUCCESS", vars
   * .getLanguage());
   * 
   * adviseRegistrationConfirm(response, vars, "SUCCESS", "Registration", message); } catch
   * (IOException e) { if (e instanceof SSLHandshakeException) { message = Utility.messageBD(myPool,
   * "HB_SECURE_CONNECTION_ERROR", vars.getLanguage()); advisePopUp(response, "ERROR",
   * "Registration", message); } else { message = Utility.messageBD(myPool, "HB_SEND_ERROR", vars
   * .getLanguage()); advisePopUp(response, "ERROR", "Registration", message); } } catch
   * (GeneralSecurityException e) { message = Utility.messageBD(myPool, "HB_CERTIFICATE_ERROR",
   * vars.getLanguage()); advisePopUp(response, "ERROR", "Registration", message); } } }
   * 
   * public void adviseRegistrationConfirm(HttpServletResponse response, VariablesSecureApp vars,
   * String strTipo, String strTitulo, String strTexto) throws IOException { XmlDocument xmlDocument
   * = xmlEngine.readXmlTemplate( "org/openbravo/erpCommon/ad_forms/RegistrationConfirm")
   * .createXmlDocument();
   * 
   * xmlDocument.setParameter("ParamTipo", strTipo.toUpperCase());
   * xmlDocument.setParameter("ParamTitulo", strTitulo); xmlDocument.setParameter("ParamTexto",
   * strTexto);
   * 
   * xmlDocument.setParameter("result", strTexto); xmlDocument.setParameter("directory",
   * "var baseDirectory = \"" + strReplaceWith + "/\";\n"); xmlDocument.setParameter("language",
   * "defaultLang=\"" + vars.getLanguage() + "\";"); xmlDocument.setParameter("theme",
   * vars.getTheme());
   * 
   * response.setContentType("text/html; charset=UTF-8"); PrintWriter out = response.getWriter();
   * out.println(xmlDocument.print()); out.close(); }
   * 
   * public String register(String encodedQueryStr) throws GeneralSecurityException, IOException {
   * URL url = null; try { url = new URL(PROTOCOL, HOST, PORT, PATH); } catch (MalformedURLException
   * e) { e.printStackTrace(); // Won't happen. } log4j.info(("Sending registration info: '" +
   * encodedQueryStr + "'")); return HttpsUtils.sendSecure(url, encodedQueryStr,
   * HeartbeatProcess.CERT_ALIAS, "changeit"); }
   * 
   * private String createQueryString(RegisterData data) {
   * 
   * String registrationId = data.registrationId; String isregistrationactive =
   * data.isregistrationactive; // Contact Details String contactfirstname = data.contactfirstname;
   * String contactlastname = data.contactlastname; String contactjobtitle = data.contactjobtitle;
   * String contactphone = data.contactphone; String contactemail = data.contactemail; // Company
   * Details String companyname = data.companyname; String companywebsite = data.companywebsite;
   * String companycountry = data.companycountry; String companyindustry = data.companyindustry;
   * String companynumEmployees = data.companynumEmployees; String comments = data.comments; String
   * contactme = data.contactme; String wanttopartner = data.wanttopartner; String isevaluating =
   * data.isevaluating;
   * 
   * // Newsletter Subscription String vision = data.obVision; String development =
   * data.obDevelopment; String developmentAnnounce = data.obDevelopmentAnnounce; String commits =
   * data.obCommits;
   * 
   * // Building request StringBuilder sb = new StringBuilder(); sb.append("registrationId=" +
   * (registrationId == null ? "" : registrationId) + "&"); sb.append("isregistrationactive=" +
   * (isregistrationactive == null ? "" : isregistrationactive) + "&");
   * sb.append("contactfirstname=" + (contactfirstname == null ? "" : contactfirstname) + "&");
   * sb.append("contactlastname=" + (contactlastname == null ? "" : contactlastname) + "&");
   * sb.append("contactjobtitle=" + (contactjobtitle == null ? "" : contactjobtitle) + "&");
   * sb.append("contactphone=" + (contactphone == null ? "" : contactphone) + "&");
   * sb.append("contactemail=" + (contactemail == null ? "" : contactemail) + "&");
   * sb.append("companyname=" + (companyname == null ? "" : companyname) + "&");
   * sb.append("companywebsite=" + (companywebsite == null ? "" : companywebsite) + "&"); try {
   * sb.append("companycountry=" + (companycountry == null ? "" : PaisComboData.selectName( this,
   * companycountry) + "&")); } catch (Exception e) {
   * log4j.error("Error building registration request: " + e.getMessage());
   * sb.append("companycountry=" + (companycountry == null ? "" : companycountry + "&")); }
   * sb.append("companyindustry=" + (companyindustry == null ? "" : companyindustry) + "&");
   * sb.append("companynumEmployees=" + (companynumEmployees == null ? "" : companynumEmployees) +
   * "&"); sb.append("comments=" + (comments == null ? "" : comments) + "&"); sb.append("contactme="
   * + (contactme == null ? "" : contactme) + "&"); sb.append("wanttopartner=" + (wanttopartner ==
   * null ? "" : wanttopartner) + "&"); sb.append("isevaluating=" + (isevaluating == null ? "" :
   * isevaluating) + "&"); sb.append("vision=" + (vision == null ? "" : vision) + "&");
   * sb.append("development=" + (development == null ? "" : development) + "&");
   * sb.append("developmentannounce=" + (developmentAnnounce == null ? "" : developmentAnnounce) +
   * "&"); sb.append("commits=" + (commits == null ? "" : commits));
   * 
   * return sb.toString(); }
   */
}
