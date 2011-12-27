package org.openbravo.client.myob.test;

import java.util.List;

import org.openbravo.client.myob.MyOpenbravoComponent;
import org.openbravo.test.base.BaseTest;

public class MyOpenbravoTest extends BaseTest {
  private static final String WIDGET_MOVED = "WIDGET_MOVED";
  private static final String WIDGET_ADDED = "WIDGET_ADDED";
  private static final String WIDGET_REMOVED = "WIDGET_REMOVED";
  private static final String PUBLISH_CHANGES = "PUBLISH_CHANGES";

  public static final String TITLE = "title";
  private static final String COLNUM = "colNum";
  private static final String ROWNUM = "rowNum";
  private static final String HEIGHT = "height";
  protected static final String PARAMETERS = "parameters";
  private static final String DBINSTANCEID = "dbInstanceId";

  @Override
  protected void setUp() throws Exception {
    super.setUp();

  }

  public void testMyOpenbravoComponent() throws Exception {
    setTestAdminContext();
    final MyOpenbravoComponent component = new MyOpenbravoComponent();

    System.err.println("Available Widget Classes for System Administrator");
    List<String> availableWidgetClasses = component.getAvailableWidgetClasses();
    for (String widgetClass : availableWidgetClasses) {
      System.err.println(widgetClass);
    }

    System.err.println("Available Widget Instances for System Administrator");
    List<String> availableWidgetInstances = component.getWidgetInstanceDefinitions();
    for (String widgetClass : availableWidgetInstances) {
      System.err.println(widgetClass);
    }

  }

  /**
   * Testing of MyOpenbravoHandler is done using my-openbravo-test.js javascript.
   */

  // public void testMyOpenbravoHandler() {
  // setSystemAdministratorContext();
  // MyOpenbravoComponent component = new MyOpenbravoComponent();
  //
  // try {
  // JSONObject content = buildContent(false, WIDGET_ADDED, null, null, component);
  // JSONArray widgets = content.getJSONArray("widgets");
  // String initialWidgets = widgets.toString();
  // // Add a new widget instance to the user
  // WidgetClass widgetClass = OBDal.getInstance().get(WidgetClass.class,
  // "409D7D27FC2949ACAE0E3F5298A0B3BA");
  // Long maxRowNumCol0 = getMaxRowNum(widgets, 0L);
  // Long maxRowNumCol1 = getMaxRowNum(widgets, 1L);
  // JSONObject newWidget = newWidgetInstance(widgetClass, 1L, maxRowNumCol1 + 1,
  // new ArrayList<ParameterValue>());
  // widgets.put(newWidget.toString());
  //
  // } catch (JSONException e) {
  // }
  //
  // }
  //
  // private JSONObject buildContent(boolean isAdminMode, String strEventType, String strLevel,
  // String strLevelValue, MyOpenbravoComponent component) throws JSONException {
  //
  // JSONObject content = new JSONObject();
  // content.put("eventType", strEventType);
  //
  // JSONObject context = new JSONObject();
  // context.put("adminMode", isAdminMode);
  // if (isAdminMode) {
  // context.put("availableAtLevel", strLevel);
  // context.put("availableAtLevelValue", strLevelValue);
  // }
  // content.put("context", context);
  //
  // JSONArray widgets = new JSONArray();
  // if (!isAdminMode) {
  // widgets = new JSONArray(component.getWidgetInstanceDefinitions());
  // } else {
  // widgets = new JSONArray(MyOBUtils.getDefaultWidgetInstances(strLevel,
  // new String[] { strLevelValue }));
  // }
  // content.put("widgets", widgets);
  //
  // return null;
  // }
  //
  // private Long getMaxRowNum(JSONArray initialWidgets, long colNum) throws JSONException {
  // Long maxRowNum = 0L;
  // for (int i = 0; i < initialWidgets.length(); i++) {
  // JSONObject widgetDefinition = new JSONObject(initialWidgets.getString(i));
  // if (colNum == widgetDefinition.getLong(COLNUM)
  // && maxRowNum < widgetDefinition.getLong(ROWNUM)) {
  // maxRowNum = widgetDefinition.getLong(ROWNUM);
  // }
  // }
  // return maxRowNum;
  // }
  //
  // private JSONObject newWidgetInstance(WidgetClass widgetClass, Long colNum, Long rowNum,
  // List<ParameterValue> parameterValues) throws JSONException {
  // JSONObject jsonObject = new JSONObject();
  // jsonObject.put(MyOpenbravoWidgetComponent.CLASSNAMEPARAMETER, MyOBUtils.getWidgetProvider(
  // widgetClass).getClientSideWidgetClassName());
  // jsonObject.put(DBINSTANCEID, "");
  // jsonObject.put(TITLE, MyOBUtils.getWidgetTitle(widgetClass));
  // jsonObject.put(COLNUM, colNum);
  // jsonObject.put(ROWNUM, rowNum);
  // jsonObject.put(HEIGHT, widgetClass.getHeight());
  // final JSONObject widgetParameters = new JSONObject();
  // for (ParameterValue parameterValue : parameterValues) {
  // if (parameterValue.getValueDate() != null) {
  // widgetParameters
  // .put(parameterValue.getParameter().getName(), parameterValue.getValueDate());
  // } else if (parameterValue.getValueNumber() != null) {
  // widgetParameters.put(parameterValue.getParameter().getName(), parameterValue
  // .getValueNumber());
  // } else if (parameterValue.getValueString() != null) {
  // widgetParameters.put(parameterValue.getParameter().getName(), parameterValue
  // .getValueString());
  // }
  // }
  // jsonObject.put(PARAMETERS, widgetParameters);
  //
  // return jsonObject;
  // }
}
