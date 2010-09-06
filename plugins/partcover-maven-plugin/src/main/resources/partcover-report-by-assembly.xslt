<?xml version="1.0" encoding="utf-8"?>
<!-- Obtained from http://www.jroller.com/DhavalDalal/entry/incorporating_partcover_for_nant_builds -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:msxml="urn:schemas-microsoft-com:xslt">

  <xsl:template name="reportByAssembly">
    <p>
      <h2>Assembly Level Coverage Summary</h2>
      <table id="assembly.coverage.info">
        <tr>
          <th class="TableHeader">Assembly Name</th>
          <th class="TableHeader">Domain</th>
          <th class="TableHeader">Coverage (%)</th>
        </tr>

        <xsl:variable name="unique-asms" select="/PartCoverReport[@version='4.0']/Type[not(@asmref=following::Type/@asmref)]"/>

        <xsl:for-each select="$unique-asms">
          <xsl:variable name="current-asm" select="./@asmref"/>
          <tr id="data">
            <xsl:element name="td">
              <xsl:attribute name="class">assembly</xsl:attribute>
              <xsl:element name="a">
                <xsl:attribute name="href">javascript:void(0)</xsl:attribute>
                <xsl:attribute name="onclick">showAssemblyClasses('<xsl:value-of select="$current-asm"/>')
                </xsl:attribute>
                <xsl:value-of select="//Assembly[@id=$current-asm]/@name"/>
              </xsl:element>
            </xsl:element>

            <xsl:element name="td">
              <xsl:value-of select="//Assembly[@id=$current-asm]/@domain"/>
            </xsl:element>

            <xsl:variable name="codeSize" select="sum(//Type[@asmref=$current-asm]/Method/pt/@len)+sum(//Type[@asmref=$current-asm]/Method[count(pt)=0]/@bodysize)"/>
            <xsl:variable name="coveredCodeSize" select="sum(//Type[@asmref=$current-asm]/Method/pt[@visit>0]/@len)"/>

            <xsl:element name="td">
              <xsl:if test="$codeSize=0">
                <xsl:attribute name="class"><xsl:value-of select="$cov0style"/></xsl:attribute>
                0%
              </xsl:if>

              <xsl:if test="$codeSize &gt; 0">
                <xsl:variable name="coverage" select="round(100 * $coveredCodeSize div $codeSize)"/>
                
                <xsl:if test="$coverage &gt;=  0 and $coverage &lt; 20"><xsl:attribute name="class"><xsl:value-of select="$cov20style"/></xsl:attribute></xsl:if>
                <xsl:if test="$coverage &gt;= 20 and $coverage &lt; 40"><xsl:attribute name="class"><xsl:value-of select="$cov40style"/></xsl:attribute></xsl:if>
                <xsl:if test="$coverage &gt;= 40 and $coverage &lt; 60"><xsl:attribute name="class"><xsl:value-of select="$cov60style"/></xsl:attribute></xsl:if>
                <xsl:if test="$coverage &gt;= 60 and $coverage &lt; 80"><xsl:attribute name="class"><xsl:value-of select="$cov80style"/></xsl:attribute></xsl:if>
                <xsl:if test="$coverage &gt;= 80"><xsl:attribute name="class"><xsl:value-of select="$cov100style"/></xsl:attribute></xsl:if>
                <xsl:value-of select="$coverage"/>%
              </xsl:if>
            </xsl:element>
          </tr>
        </xsl:for-each>
      </table>
    </p>
  </xsl:template>
</xsl:stylesheet>
