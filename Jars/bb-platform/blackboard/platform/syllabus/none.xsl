<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <xsl:apply-templates select="//MAIN"/>
</xsl:template>

<xsl:template match="MAIN">
<style type="text/css">
  .syllabusH1-none<xsl:value-of select="UUID" /> {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 1.3em;
    font-weight: bold;	
	color: <xsl:value-of select="HEADERCOLOR"/>;
  }

  .syllabusH2-none<xsl:value-of select="UUID" /> {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 0.9em;
    font-weight:bold;
    text-transform: uppercase;		
    border-bottom-width: 1px;
    border-bottom-style: solid;
    border-bottom-color: <xsl:value-of select="HEADERCOLOR" />;
	color: <xsl:value-of select="HEADERCOLOR"/>;
    margin-bottom: 1em;
    padding-top: 1.3em;
  }

  .syllabusH3-none<xsl:value-of select="UUID" /> {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 0.9em;
    font-weight: bold;
	color: <xsl:value-of select="HEADERCOLOR"/>;
  }

  .syllabusTopOuter-none<xsl:value-of select="UUID" /> {
    padding: 0.3em 0.3em 0.3em 0.3em;
	background-color:#FFFFFF;
  }

  .syllabusTopInner-none<xsl:value-of select="UUID" /> {
    padding: 0.3em 0.3em 0.3em 0.3em;    
	background-color:#FFFFFF;	
  }

  .lessonsH1-none<xsl:value-of select="UUID" /> {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 0.9em;
    font-weight: bold;
    text-transform: uppercase;		
    border-bottom-width: 1px;
    border-bottom-style: solid;
    border-bottom-color: <xsl:value-of select="HEADERCOLOR" />;
    margin-bottom: 1em;
    padding-top: 2em;
    color: <xsl:value-of select="HEADERCOLOR"/>;
  }
  .lessonsP-none<xsl:value-of select="UUID" />{
  	color: <xsl:value-of select="TEXTCOLOR"/>;
  }
  </style>
  
  <table border="0" width="100%" cellpadding="6" cellspacing="0">
  <tr>
  <td>

  <span><xsl:element name="font">
		<xsl:attribute name = "color" ><xsl:value-of select="HEADERCOLOR"/></xsl:attribute>
		<xsl:value-of select="TITLE"/>
	</xsl:element></span>
  <div><xsl:element name="font">
		<xsl:attribute name = "color" ><xsl:value-of select="HEADERCOLOR"/></xsl:attribute>
		<xsl:value-of select="DESCTEXT"/>
	</xsl:element></div>
  <xsl:choose>
	<xsl:when test="string-length(DESCRIPTION) &gt; 0">
		<p><xsl:element name="font">
		<xsl:attribute name = "color" ><xsl:value-of select="TEXTCOLOR"/></xsl:attribute>
		<xsl:value-of select="DESCRIPTION"/>
	</xsl:element></p>
	</xsl:when>
	<xsl:otherwise>
		<br/><br/>
	</xsl:otherwise>	    
  </xsl:choose>
  
  <div><xsl:element name="font">
		<xsl:attribute name = "color" ><xsl:value-of select="HEADERCOLOR"/></xsl:attribute>
		<xsl:value-of select="LEARNTEXT"/>
	</xsl:element></div>
  <xsl:choose>
	<xsl:when test="string-length(LEARNINGOBJECTIVES) &gt; 0">
		<p><xsl:element name="font">
		<xsl:attribute name = "color" ><xsl:value-of select="TEXTCOLOR"/></xsl:attribute>
		<xsl:value-of select="LEARNINGOBJECTIVES"/>
	</xsl:element></p>
	</xsl:when>
	<xsl:otherwise>
		<br/><br/>
	</xsl:otherwise>	    
  </xsl:choose>
  
  <div><xsl:element name="font">
		<xsl:attribute name = "color" ><xsl:value-of select="HEADERCOLOR"/></xsl:attribute>
		<xsl:value-of select="MATERIALTEXT"/>
	</xsl:element></div>
  <xsl:choose>
	<xsl:when test="string-length(REQUIREDMATERIALS) &gt; 0"> 
		<p><xsl:element name="font">
		<xsl:attribute name = "color" ><xsl:value-of select="TEXTCOLOR"/></xsl:attribute>
		<xsl:value-of select="REQUIREDMATERIALS"/>
	</xsl:element></p>
	</xsl:when>
	<xsl:otherwise>
		<br/><br/>
	</xsl:otherwise>	    
  </xsl:choose>       

  <xsl:if test="count(//LESSON) &gt; 0">
    <div><xsl:element name="font">
		<xsl:attribute name = "color" ><xsl:value-of select="HEADERCOLOR"/></xsl:attribute>
		<xsl:value-of select="LESSONTEXT"/>
	</xsl:element></div>
    
    <xsl:for-each select="//LESSON"> 
	<xsl:element name="font">
		<xsl:attribute name = "color" ><xsl:value-of select="LESSONTITLECOLOR"/></xsl:attribute>
		<xsl:value-of select="LESSONTITLE"/>
	</xsl:element>
        <p><xsl:value-of select="DESCRIPTION"/></p>
        <div><xsl:value-of select="LESSONDATE"/></div>         
        <div><xsl:value-of select="LESSONTIME"/></div> 
    </xsl:for-each>
    
  </xsl:if>

  </td>
  </tr>
  </table>
</xsl:template>


</xsl:stylesheet>

