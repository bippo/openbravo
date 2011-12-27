/*
 ************************************************************************************
 * Copyright (C) 2008-2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.openbravo.base.secureApp;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.WindowTreeData;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationType;

class OrgTreeNode implements Serializable {
  private static final long serialVersionUID = 1L;
  private String id;
  private String parentId;
  private String value;
  private String isReady;
  private OrganizationType orgType;
  private String serializedOrgTypeId;

  /**
   * Creates a node from data related to it
   * 
   * @param nodeData
   *          info for the node
   */
  public OrgTreeNode(WindowTreeData nodeData) {
    id = nodeData.id;
    parentId = nodeData.parentId;
    value = nodeData.name;
    isReady = nodeData.isready;
    orgType = OBDal.getInstance().get(OrganizationType.class, nodeData.adOrgtypeId);
  }

  /**
   * Creates a node from the DAL object using the organization's identifier
   * 
   * @param AD_Org_ID
   *          Organization's identifier
   */
  public OrgTreeNode(String AD_Org_ID) {
    final Organization org = OBDal.getInstance().get(Organization.class, AD_Org_ID);
    id = AD_Org_ID;
    value = org.getName();
    isReady = org.isReady().toString();
    orgType = org.getOrganizationType();
  }

  // The orgtreenode is serialized in a persistent http session by tomcat
  // the Openbravo BusinessObjects which have been read from the db can
  // only be deserialized if they are connected to the db because some
  // of the references maybe a cglib proxy
  private void writeObject(ObjectOutputStream out) throws IOException {
    serializedOrgTypeId = orgType.getId();
    orgType = null;
    out.defaultWriteObject();
  }

  /**
   * Creates a tree from data and returns the root node
   * 
   * @param data
   *          information to generete the tree
   * @return Node[]: Complete tree's nodes
   */
  public static List<OrgTreeNode> createTree(WindowTreeData[] data) {
    final List<OrgTreeNode> nodes = new ArrayList<OrgTreeNode>();

    for (int i = 0; i < data.length; i++)
      nodes.add(new OrgTreeNode(data[i]));

    return nodes;
  }

  public String getParentId() {
    return parentId;
  }

  public String getId() {
    return id;
  }

  public String getValue() {
    return value;
  }

  public String getIsReady() {
    return isReady;
  }

  public void setIsReady(String isReady) {
    this.isReady = isReady;
  }

  public OrganizationType getOrgType() {
    return orgType;
  }

  public void setOrgType(OrganizationType orgType) {
    this.orgType = orgType;
  }

  public boolean equals(String s) {
    return id.equals(s);
  }
}
