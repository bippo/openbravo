<#function getter p>
  <#if p.boolean>
    <#return "is" + p.getterSetterName?cap_first>
  <#else>
    <#return "get" + p.getterSetterName?cap_first>
  </#if>
</#function>

<#function theList entity>
  <#if entity.simpleClassName == "List">
    <#return "java.util.List">
  <#else>
    <#return "List">
  </#if>
</#function>
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
 * All portions are Copyright (C) 2008-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package ${entity.packageName};
<#list entity.javaImports as i>
${i}
</#list>
/**
 * Entity class for entity ${entity.name} (stored in table ${entity.tableName}).
 *
 * NOTE: This class should not be instantiated directly. To instantiate this
 * class the {@link org.openbravo.base.provider.OBProvider} should be used.
 */
public class ${entity.simpleClassName} extends BaseOBObject ${entity.implementsStatement} {
    private static final long serialVersionUID = 1L;
    public static final String TABLE_NAME = "${entity.tableName}";
    public static final String ENTITY_NAME = "${entity.name}";
    <#list entity.properties as p>
    public static final String PROPERTY_${p.name?upper_case} = "${p.name}";
    </#list>

    public ${entity.simpleClassName}() {
    <#list entity.properties as p>
        <#if p.hasDefaultValue()>
        setDefaultValue(PROPERTY_${p.name?upper_case}, ${p.formattedDefaultValue});
        </#if>
    </#list>
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    <#list entity.properties as p>
    <#if !p.oneToMany>
    <#if p.name?matches("Id")>
    @Override
    </#if>
    public ${p.shorterTypeName} ${getter(p)}() {
    <#if p.partOfCompositeId>
        return ((Id)getId()).«getter((Property)p)»();
    <#else>
        return (${p.shorterTypeName}) get(PROPERTY_${p.name?upper_case});
    </#if>
    }

    <#if p.name?matches("Id")>
    @Override
    </#if>
    public void set${p.getterSetterName?cap_first}(${p.shorterTypeName} ${p.javaName}) {
    <#if p.partOfCompositeId>
	    ((Id)getId()).set${p.getterSetterName?cap_first}(${p.javaName});
	<#else>
        set(PROPERTY_${p.name?upper_case}, ${p.javaName});
	</#if>
    }

    </#if>
	</#list>
	<#list entity.properties as p>
	<#if p.oneToMany>
    @SuppressWarnings("unchecked")
    public ${theList(entity)}<${p.shorterNameTargetEntity}> get${p.name?cap_first}() {
        return (${theList(entity)}<${p.shorterNameTargetEntity}>) get(PROPERTY_${p.name?upper_case});
    }

    public void set${p.getterSetterName?cap_first}(${theList(entity)}<${p.shorterNameTargetEntity}> ${p.name}) {
        set(PROPERTY_${p.name?upper_case}, ${p.name});
    }

    </#if>
    </#list>
    <#if entity.hasCompositeId()>
	public static class Id implements java.io.Serializable {
	    private static final long serialVersionUID = 1L;

		<#list entity.properties as p>
		<#if p.partOfCompositeId>
		<#if p.hasDefaultValue()>
		private ${p.typeName} ${p.javaName} = ${p.formattedDefaultValue};
		<#else>
		private ${p.typeName} ${p.javaName};
		</#if>
		</#if>
		</#list>

		<#list entity.properties as p>
		<#if p.partOfCompositeId>
		public ${p.typeName} «getter((Property)p)»() {
			return ${p.javaName};
		}
		
		public void set${p.getterSetterName?cap_first}(${p.typeName} ${p.javaName}) {
			this.${p.javaName} = ${p.javaName};
		}
		</#if>
		</#list>
		
	    public boolean equals(Object obj) {
			if (this == obj) {
    			return true;
			}
			if (!(obj instanceof Id)) {
				return false;
			}
			final Id otherId = (Id)obj;
		<#list entity.properties as p>
		<#if p.partOfCompositeId>
			if (!areEqual(«getter((Property)p)»(), otherId.«getter((Property)p)»())) {
				return false;
			} 
		</#if>
		</#list>
			return true;
		}

		// hashCode assumes that keys can not change!
    	public int hashCode() {
    		int result = 0;
    		<#list entity.properties as p>
    		<#if p.partOfCompositeId>
			if («getter((Property)p)»() != null) {
				result +=«getter((Property)p)»().hashCode(); 
			}
			</#if>
			</#list>
    		
    		if (result == 0) {
    			return super.hashCode();
    		}
    		return result;
    	}

		private boolean areEqual(Object v1, Object v2) {
			if (v1 == null || v2 == null) {
				return v1 == v2;
			}
			return v1.equals(v2);
		}		
	}
	</#if>
}
