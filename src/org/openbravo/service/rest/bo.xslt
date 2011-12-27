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

<xsl:stylesheet version='1.0'
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:ob="http://www.openbravo.com">
	<!--
		Note if the namespace of openbravo changes then the namespace
		declaration above has to be changed
	--> 
	<xsl:template match="ob:Openbravo">
		<xsl:for-each select="*">
			<xsl:call-template name="handleEntity" />
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="Openbravo">
		<xsl:for-each select="*">
			<xsl:call-template name="handleEntity" />
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="handleEntity">
		<html>
			<body>
				<h1>
					<xsl:value-of select="name(.)" />
					(
					<xsl:value-of select="@identifier" />
					-
					<xsl:value-of select="@id" />
					)
				</h1>
				<table>
					<xsl:for-each select="*">
						<xsl:choose>
							<xsl:when test="count(*)>0">
								<xsl:call-template name="handleManyField" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template name="handleField" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</table>
			</body>
		</html>
	</xsl:template>
	<xsl:template name="handleField">
		<tr>
			<td width="200" style="background-color: lightgreen;vertical-align: top;">
				<xsl:value-of select="name(.)" />
			</td>
			<td style="padding-left: 10px">
				<xsl:choose>
					<xsl:when test="@id">
						<xsl:call-template name="handleReference" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates />
					</xsl:otherwise>
				</xsl:choose>
			</td>
		</tr>
	</xsl:template>
	<xsl:template name="handleManyField">
		<tr>
			<td width="200" style="background-color: lightgreen;vertical-align: top;">
				<xsl:value-of select="name(.)" />
			</td>
			<td style="padding-left: 10px">
				<xsl:for-each select="*">
					<h3>
						<xsl:value-of select="name(.)" />
						(
						<xsl:value-of select="@identifier" />
						-
						<xsl:value-of select="@id" />
						)
					</h3>
					<table>
						<xsl:for-each select="*">
							<xsl:choose>
								<xsl:when test="count(*)>0">
									<xsl:call-template name="handleManyField" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="handleField" />
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
					</table>
				</xsl:for-each>
			</td>
		</tr>
	</xsl:template>
	<xsl:template name="handleReference">
		<xsl:variable name="href">
			<xsl:value-of select="@entity-name" />
			/
			<xsl:value-of select="@id" />
		</xsl:variable>
		<a href="../{$href}?template=bo.xslt">
			<xsl:value-of select="@identifier" />
			(
			<xsl:value-of select="@id" />
			)
		</a>
		<a href="../{$href}"> (xml)</a>
	</xsl:template>
</xsl:stylesheet>