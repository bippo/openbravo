package org.openbravo.client.kernel;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.AuxiliaryInput;
import org.openbravo.model.ad.ui.Tab;

@ApplicationScoped
public class SetContextInfoActionHandler extends BaseActionHandler {

  private static final Logger log = Logger.getLogger(SetContextInfoActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {

    RequestContext rc = RequestContext.get();

    // TODO Auto-generated method stub
    try {
      OBContext.setAdminMode();

      JSONObject p = new JSONObject(content);

      String tabId = p.getString("_tabId");
      Tab tab = OBDal.getInstance().get(Tab.class, tabId);
      String windowId = tab.getWindow().getId();

      System.out.println("window: " + windowId);

      JSONArray names = p.names();
      for (int i = 0; i < names.length(); i++) {
        String name = names.getString(i);
        String value = p.getString(name);

        rc.setSessionAttribute((windowId + "|" + name).toUpperCase(), value);
        System.out.println((windowId + "|" + name).toUpperCase() + ":" + value);
        System.out.println(name + ": " + value);
      }

      // Auxiliary inputs

      OBCriteria<AuxiliaryInput> qInputs = OBDal.getInstance().createCriteria(AuxiliaryInput.class);
      qInputs.add(Restrictions.eq(AuxiliaryInput.PROPERTY_TAB, tab));
      for (AuxiliaryInput input : qInputs.list()) {

      }

    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }

    System.out.println("set context info");
    return new JSONObject();
  }

}
