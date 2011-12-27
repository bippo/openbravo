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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.provider.OBProvider;

import com.googlecode.jslint4java.Issue;
import com.googlecode.jslint4java.JSLint;
import com.googlecode.jslint4java.JSLintBuilder;
import com.googlecode.jslint4java.JSLintResult;
import com.googlecode.jslint4java.Option;

/**
 * In its base form calls {@link Component#generate()}, more advanced features will postprocess the
 * result from the component.
 * 
 * @author mtaal
 */
public class JSLintChecker {

  private static JSLintChecker instance;

  public static synchronized JSLintChecker getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(JSLintChecker.class);
    }
    return instance;
  }

  public static synchronized void setInstance(JSLintChecker instance) {
    JSLintChecker.instance = instance;
  }

  public String check(String componentIdentifier, String javascript) {
    long t1 = System.currentTimeMillis();
    JSLint jsLint = new JSLintBuilder().fromDefault();
    jsLint.addOption(Option.BROWSER); // browser globals are predefined
    jsLint.addOption(Option.EVIL); // allow eval()
    jsLint.addOption(Option.CONTINUE); // allow continue statement
    final JSLintResult lintResult = jsLint.lint(componentIdentifier, javascript);
    final List<Issue> issues = purgeIssuesList(lintResult.getIssues());

    if (issues.isEmpty()) {
      return null;
    }
    final StringBuilder sb = new StringBuilder();
    if (issues.size() > 0) {
      sb.append(">>>>>>> Issues found in javascript <<<<<<<<<\n");
      for (Issue issue : issues) {
        sb.append(componentIdentifier + ":" + issue.getLine() + ":" + issue.getCharacter() + ": "
            + issue.getReason());
        if (issue.getEvidence() != null) {
          sb.append(" >> offending code: " + issue.getEvidence());
        }
        sb.append("\n");
      }
      sb.append(">>>>>>> Issues (" + (System.currentTimeMillis() - t1) + "ms)  <<<<<<<<<\n");
    }
    return sb.toString();
  }

  private List<Issue> purgeIssuesList(List<Issue> issues) {
    // FreeMarker escapes '>' to '\>' this doesn't affect the resulting value of the
    // string. Tested: Chrome, IE, Opera, FF
    // https://sourceforge.net/tracker/index.php?func=detail&aid=3303868&group_id=794&atid=100794
    List<Issue> purgedList = new ArrayList<Issue>();
    for (Issue issue : issues) {
      if (issue.getReason().contains("Too many errors")) {
        continue;
      }

      if (issue.getReason().equals("Unexpected '\\'.") && issue.getEvidence().contains("\\>")) {
        continue;
      }

      purgedList.add(issue);
    }
    return purgedList;
  }
}
