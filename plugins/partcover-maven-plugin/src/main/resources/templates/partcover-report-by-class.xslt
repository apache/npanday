<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:msxml="urn:schemas-microsoft-com:xslt">
  <xsl:template name="reportByClass">
    <xsl:element name="div">
      <p>
        <h2>Class Level Coverage Details <a onclick="showAllAssemblyClasses(); return false;" href="#">reset filter</a></h2>
        <table id="class.coverage.info">
          <tr>
            <th class="TableHeader">Class Name</th>
            <!--<th class="TableHeader">Assembly</th>-->
            <th class="TableHeader">Coverage (%)</th>
          </tr>
          
          <xsl:for-each select="/PartCoverReport/Assembly">
            <xsl:variable name="current-asm" select="./@id"/>
            <xsl:element name="tbody">
              <xsl:attribute name="class">coverage-details</xsl:attribute>
              <xsl:attribute name="id">coverage-assembly-<xsl:value-of select="$current-asm"/></xsl:attribute>
              <xsl:for-each select="/PartCoverReport/Type[@asmref=$current-asm]">
                <xsl:element name="tr">
                  <xsl:element name="td">
                    <xsl:value-of select="@name"/>
                  </xsl:element>
                  <!--<xsl:element name="td">
                    <xsl:value-of select="//Assembly[@id=$current-asm]/@name"/>
                  </xsl:element>-->
                  <xsl:variable name="codeSize" select="sum(./Method/pt/@len)+sum(./Method[count(pt)=0]/@bodysize)"/>
                  <xsl:variable name="coveredCodeSize" select="sum(./Method/pt[@visit>0]/@len)"/>
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
                </xsl:element>
              </xsl:for-each>
            </xsl:element>
          </xsl:for-each>
        </table>
      </p>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
