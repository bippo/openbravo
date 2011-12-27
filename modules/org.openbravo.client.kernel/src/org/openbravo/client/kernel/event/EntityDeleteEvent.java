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
package org.openbravo.client.kernel.event;

import javax.enterprise.context.ApplicationScoped;

/**
 * The event object send out when an entity gets deleted.
 * 
 * To receive this event, create a class with a method which has this signature:
 * 
 * public void onEvent(@Observes EntityDeleteEvent event) {
 * 
 * Note, the method name is unimportant, the @Observes EntityDeleteEvent specifies that this method
 * will be called before persisting a new instance.
 * 
 * @author mtaal
 */
@ApplicationScoped
public class EntityDeleteEvent extends EntityPersistenceEvent {
}
