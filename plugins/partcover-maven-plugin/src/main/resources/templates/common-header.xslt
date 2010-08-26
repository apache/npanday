<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:msxml="urn:schemas-microsoft-com:xslt">

  <xsl:template name="reportHeader">
    <center>
      Tested On:
      <xsl:value-of select="//PartCoverReport[@version='4.0']/@date"/>
      Generated On:
      <xsl:value-of select="$generatedOn"/>
    </center>
    <hr size="1"/>
    <p>
      Current Code Coverage Percentage Baseline is:
      <b><xsl:value-of select="$coverageBaseline"/>%
      </b>
      . Code Coverage below this baseline is not acceptable.
    </p>

    <a href="#" onclick="toggleLegend();">View Legend</a>
    <div id="legend" style="display:none">
    <p>
      <table>
        <tr>
          <th class="TableHeader">Legend</th>
        </tr>
        <tr>
          <xsl:element name="td">
            <xsl:attribute name="class">
              <xsl:value-of select="$cov0style"/>
            </xsl:attribute>
            0%
          </xsl:element>
        </tr>
        <tr>
          <xsl:element name="td">
            <xsl:attribute name="class">
              <xsl:value-of select="$cov20style"/>
            </xsl:attribute>
            0-19%
          </xsl:element>
        </tr>
        <tr>
          <xsl:element name="td">
            <xsl:attribute name="class">
              <xsl:value-of select="$cov40style"/>
            </xsl:attribute>
            20-39%
          </xsl:element>
        </tr>
        <tr>
          <xsl:element name="td">
            <xsl:attribute name="class">
              <xsl:value-of select="$cov60style"/>
            </xsl:attribute>
            40-59%
          </xsl:element>
        </tr>
        <tr>
          <xsl:element name="td">
            <xsl:attribute name="class">
              <xsl:value-of select="$cov80style"/>
            </xsl:attribute>
            60%-79%
          </xsl:element>
        </tr>
        <tr>
          <xsl:element name="td">
            <xsl:attribute name="class">
              <xsl:value-of select="$cov100style"/>
            </xsl:attribute>
            80-100%
          </xsl:element>
        </tr>
      </table>
    </p>
    </div>
  </xsl:template>

</xsl:stylesheet>
