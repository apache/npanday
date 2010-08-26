<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:msxml="urn:schemas-microsoft-com:xslt">
  <xsl:param name="assemblyName"/>
  <xsl:param name="moduleCompany"/>
  <xsl:param name="generatedOn"/>
  <xsl:param name="coverageBaseline"/>
  <xsl:variable name="cov0style" select="'cov0style'"/>
  <xsl:variable name="cov20style" select="'cov20style'"/>
  <xsl:variable name="cov40style" select="'cov40style'"/>
  <xsl:variable name="cov60style" select="'cov60style'"/>
  <xsl:variable name="cov80style" select="'cov80style'"/>
  <xsl:variable name="cov100style" select="'cov100style'"/>

  <xsl:include href="common-header.xslt"/>
  <xsl:include href="partcover-report-by-assembly.xslt"/>
  <xsl:include href="partcover-report-by-class.xslt"/>
  <xsl:include href="common-footer.xslt"/>
  <xsl:output method="html" indent="yes"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>
          <xsl:value-of select="$assemblyName"/>
          Code Coverage Report
        </title>
        <link rel="stylesheet" type="text/css" href="coverage-report.css"/>
        <script type="text/javascript">
          /* <![CDATA[ */
          /*
            getElementsByClassName Developed by Robert Nyman, http://www.robertnyman.com
            Code/licensing: http://code.google.com/p/getelementsbyclassname/
          */
          var getElementsByClassName = function (className, tag, elm){
            if (document.getElementsByClassName) {
              getElementsByClassName = function (className, tag, elm) {
                elm = elm || document;
                var elements = elm.getElementsByClassName(className),
                  nodeName = (tag)? new RegExp("\\b" + tag + "\\b", "i") : null,
                  returnElements = [],
                  current;
                for(var i=0, il=elements.length; i<il; i+=1){
                  current = elements[i];
                  if(!nodeName || nodeName.test(current.nodeName)) {
                    returnElements.push(current);
                  }
                }
                return returnElements;
              };
            }
            else if (document.evaluate) {
              getElementsByClassName = function (className, tag, elm) {
                tag = tag || "*";
                elm = elm || document;
                var classes = className.split(" "),
                  classesToCheck = "",
                  xhtmlNamespace = "http://www.w3.org/1999/xhtml",
                  namespaceResolver = (document.documentElement.namespaceURI === xhtmlNamespace)? xhtmlNamespace : null,
                  returnElements = [],
                  elements,
                  node;
                for(var j=0, jl=classes.length; j<jl; j+=1){
                  classesToCheck += "[contains(concat(' ', @class, ' '), ' " + classes[j] + " ')]";
                }
                try   {
                  elements = document.evaluate(".//" + tag + classesToCheck, elm, namespaceResolver, 0, null);
                }
                catch (e) {
                  elements = document.evaluate(".//" + tag + classesToCheck, elm, null, 0, null);
                }
                while ((node = elements.iterateNext())) {
                  returnElements.push(node);
                }
                return returnElements;
              };
            }
            else {
              getElementsByClassName = function (className, tag, elm) {
                tag = tag || "*";
                elm = elm || document;
                var classes = className.split(" "),
                  classesToCheck = [],
                  elements = (tag === "*" && elm.all)? elm.all : elm.getElementsByTagName(tag),
                  current,
                  returnElements = [],
                  match;
                for(var k=0, kl=classes.length; k<kl; k+=1){
                  classesToCheck.push(new RegExp("(^|\\s)" + classes[k] + "(\\s|$)"));
                }
                for(var l=0, ll=elements.length; l<ll; l+=1){
                  current = elements[l];
                  match = false;
                  for(var m=0, ml=classesToCheck.length; m<ml; m+=1){
                    match = classesToCheck[m].test(current.className);
                    if (!match) {
                      break;
                    }
                  }
                  if (match) {
                    returnElements.push(current);
                  }
                }
                return returnElements;
              };
            }
            return getElementsByClassName(className, tag, elm);
          };
  
          function showAssemblyClasses(assemblyId) {
            // Hide all assembly details
            var details = getElementsByClassName("coverage-details");
            for(var i = 0; i < details.length; i++) {
              details[i].style.display = "none";
            }

            //Show selected assembly
            var elem = document.getElementById("coverage-assembly-" + assemblyId);
            elem.style.display = "";
          }

          function showAllAssemblyClasses() {
            var details = getElementsByClassName("coverage-details");
            for(var i = 0; i < details.length; i++) {
              details[i].style.display = "";
            }
          }
          
          function toggleLegend() {
            if (document.getElementById('legend').style.display == 'none') {  
              document.getElementById('legend').style.display = ''; 
            } else { 
              document.getElementById('legend').style.display = 'none'; 
            }
          }
          /* ]]> */
        </script>
      </head>

      <body>
        <h1>
          <center>
            <xsl:value-of select="$assemblyName"/>
            Code Coverage Report
          </center>
        </h1>
        <xsl:call-template name="reportHeader">
        </xsl:call-template>
        <hr size="1"></hr>
        <xsl:call-template name="reportByAssembly"/>
        <hr size="1"></hr>
        <xsl:call-template name="reportByClass"/>
        <hr size="1"></hr>
        <xsl:call-template name="reportFooter"/>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
