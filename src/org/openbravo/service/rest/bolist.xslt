<!-- 
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
-->
<xsl:stylesheet version = '1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
     
<xsl:template match="/">
<xsl:for-each select="*"> 
<html>
	<body> 
     <h1>OpenBravo Business Object List</h1>
     <table>
     <xsl:for-each select="*">
     	<xsl:call-template name="handleEntity"/>
     </xsl:for-each>
     </table>
	</body>
</html>
</xsl:for-each>
</xsl:template>

<xsl:template name="handleEntity">
     <xsl:variable name="href"><xsl:value-of select="name(.)"/>/<xsl:value-of select="@id"/></xsl:variable>
     <tr><td>
     <a href="{$href}?template=bo.xslt"><xsl:value-of select="@identifier"/> (<xsl:value-of select="@id"/>)</a>
     </td><td style="padding-left:20px">
     <a href="{$href}">xml</a>
     </td></tr>
</xsl:template>
</xsl:stylesheet> 