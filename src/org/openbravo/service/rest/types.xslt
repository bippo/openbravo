
	<!--
		*************************************************************************
		* The contents of this file are subject to the Openbravo Public
		License * Version 1.1 (the "License"), being the Mozilla Public
		License * Version 1.1 with a permitted attribution clause; you may not
		use this * file except in compliance with the License. You may obtain
		a copy of * the License at http://www.openbravo.com/legal/license.html
		* Software distributed under the License is distributed on an "AS IS"
		* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
		the * License for the specific language governing rights and
		limitations * under the License. * The Original Code is Openbravo ERP.
		* The Initial Developer of the Original Code is Openbravo SLU * All
		portions are Copyright (C) 2008 Openbravo SLU * All Rights Reserved. *
		Contributor(s): ______________________________________.
		************************************************************************
	-->
<xsl:stylesheet version='1.0'
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

	<xsl:param name="url" />

	<xsl:template match="Types">
		<html>
			<body>
				<h1>Types List</h1>
				<table>
					<xsl:apply-templates />
				</table>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="Type">
		<xsl:variable name="href">
			<xsl:value-of select="@entityName" />
		</xsl:variable>
		<tr>
			<td>
				<xsl:element name="a">
					<xsl:attribute name="href"><xsl:value-of
						select="$href" />?template=bolist.xslt</xsl:attribute>
					<xsl:value-of select="$href" />
				</xsl:element>
			</td>
			<td style="padding-left:20px">
				<a href="{$url}{$href}">xml</a>
			</td>
		</tr>
	</xsl:template>
</xsl:stylesheet> 
