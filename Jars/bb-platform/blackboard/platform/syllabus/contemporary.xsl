<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- 
     The / and MAIN templates are required to use the same template for 
     transforming an entire syllabus tree, or just the MAIN sub-node.
  -->
<xsl:template match="/">
  <xsl:apply-templates select="//MAIN"/>
</xsl:template>

<xsl:template match="MAIN">
	<style type="text/css">
	.syllabusH1-contemporary<xsl:value-of select="UUID" /> {
		font-family:Arial, Helvetica, sans-serif;
		font-size:1.3em;
		font-weight:bold;
		COLOR:<xsl:value-of select="HEADERCOLOR" />;   
	}
	
	.syllabusH2-contemporary<xsl:value-of select="UUID" /> {
			font-family:Arial, Helvetica, sans-serif;
			font-size:0.9em;
			font-weight:bold;
			text-transform:uppercase;   
			COLOR:<xsl:value-of select="HEADERCOLOR" />; 
			border-bottom-width:1px;
			border-bottom-style: solid;
			border-bottom-color:<xsl:value-of select="HEADERCOLOR" />;
			margin-bottom:1em;
			padding-top:1.3em;
	}
	
	.syllabusH3-contemporary<xsl:value-of select="UUID" /> {
			font-family:Arial, Helvetica, sans-serif;
			font-size:0.9em;
			font-weight:bold;
			COLOR:<xsl:value-of select="HEADERCOLOR" />;
		
	}
	
	.syllabusTopOuter-contemporary<xsl:value-of select="UUID" /> {
			border-width:1px;
			border-style: solid;
			border-color: <xsl:value-of select="HEADERCOLOR" />;
			padding: 0.3em 0.3em 0.3em 0.3em;
			background-color:<xsl:value-of select="FRAMECOLOR" />;
		
	}
	
	.syllabusTopInner-contemporary<xsl:value-of select="UUID" /> {
			border-width:1px;
			border-style: dashed;
			border-color: <xsl:value-of select="HEADERCOLOR" />;
			padding: 0.3em 0.3em 0.3em 0.3em;
			background-color:<xsl:value-of select="BGCOLOR" />;
			background-image:url(<xsl:value-of select="BGPATTERN" />);			
	}
	
	.lessonsH1-contemporary<xsl:value-of select="UUID" /> {
			font-family:Arial, Helvetica, sans-serif;
			font-size:0.9em;
			font-weight:bold;
			text-transform:uppercase;       
			border-bottom-width:1px;
			border-bottom-style: solid;
			border-bottom-color: <xsl:value-of select="HEADERCOLOR" />;
			margin-bottom:1em;
			padding-top:2em;
			COLOR:<xsl:value-of select="HEADERCOLOR" />;
	}
	
	.lessonsP-contemporary<xsl:value-of select="UUID" /> {
			padding: 0.3em 0.3em 0.3em 1.3em;
			COLOR:<xsl:value-of select="TEXTCOLOR" />;
		}
		
	.lessonsTitle-contemporary<xsl:value-of select="UUID" /> {
			font-family:Arial, Helvetica, sans-serif;
			font-size:0.9em;
			font-weight:bold;
		}
		</style>
		
		<table border="0" width="100%" cellpadding="6" cellspacing="0">
		<tr>
		<td>
		<div> <xsl:attribute name="class"><xsl:text>syllabusTopOuter-contemporary</xsl:text><xsl:value-of select="UUID" />
   </xsl:attribute>
		<div> <xsl:attribute name="class"><xsl:text>syllabusTopInner-contemporary</xsl:text><xsl:value-of select="UUID" />
   </xsl:attribute>
		
		<span> <xsl:attribute name="class"><xsl:text>syllabusH1-contemporary</xsl:text><xsl:value-of select="UUID" />
   </xsl:attribute><xsl:value-of select="TITLE"/></span>      
  <div> <xsl:attribute name="class"><xsl:text>syllabusH2-contemporary</xsl:text><xsl:value-of select="UUID" />
   </xsl:attribute><xsl:value-of select="DESCTEXT"/></div>
  <xsl:choose>
	<xsl:when test="string-length(DESCRIPTION) &gt; 0">
		<p> <xsl:attribute name="class"><xsl:text>lessonsP-contemporary</xsl:text><xsl:value-of select="UUID" />
   </xsl:attribute><xsl:value-of select="DESCRIPTION"/></p>
	</xsl:when>
	<xsl:otherwise>
		<br/><br/>
	</xsl:otherwise>	    
  </xsl:choose>  
  
  <div> <xsl:attribute name="class"><xsl:text>syllabusH2-contemporary</xsl:text><xsl:value-of select="UUID" />
   </xsl:attribute><xsl:value-of select="LEARNTEXT"/></div>
  <xsl:choose>
	<xsl:when test="string-length(LEARNINGOBJECTIVES) &gt; 0">
		<p> <xsl:attribute name="class"><xsl:text>lessonsP-contemporary</xsl:text><xsl:value-of select="UUID" />
   </xsl:attribute><xsl:value-of select="LEARNINGOBJECTIVES"/></p>
	</xsl:when>
	<xsl:otherwise>
		<br/><br/>
	</xsl:otherwise>	    
  </xsl:choose>      
  
  <div> <xsl:attribute name="class"><xsl:text>syllabusH2-contemporary</xsl:text><xsl:value-of select="UUID" />
   </xsl:attribute><xsl:value-of select="MATERIALTEXT"/></div>
  <xsl:choose>
	<xsl:when test="string-length(REQUIREDMATERIALS) &gt; 0">
		<p> <xsl:attribute name="class"><xsl:text>lessonsP-contemporary</xsl:text><xsl:value-of select="UUID" />
   </xsl:attribute><xsl:value-of select="REQUIREDMATERIALS"/></p>
	</xsl:when>
	<xsl:otherwise>
		<br/><br/>
	</xsl:otherwise>	    
  </xsl:choose>  
  
  </div>
  </div>
  

  <xsl:if test="count(//LESSON) &gt; 0">
    <div> <xsl:attribute name="class"><xsl:text>syllabusH2-contemporary</xsl:text><xsl:value-of select="UUID" />
   </xsl:attribute><xsl:value-of select="LESSONTEXT"/></div>
  </xsl:if>
  <div> <xsl:attribute name="class"><xsl:text>syllabusTopOuter-contemporary</xsl:text><xsl:value-of select="UUID" />
   </xsl:attribute>
  <div> <xsl:attribute name="class"><xsl:text>syllabusTopInner-contemporary</xsl:text><xsl:value-of select="UUID" />
   </xsl:attribute>
  <xsl:for-each select="//LESSON">
    <div> <xsl:attribute name="class"><xsl:text>lessonsTitle-contemporary</xsl:text><xsl:value-of select="..//UUID" />
   </xsl:attribute>
	<xsl:element name="font">
		<xsl:attribute name = "color" ><xsl:value-of select="LESSONTITLECOLOR"/></xsl:attribute>
		<xsl:value-of select="LESSONTITLE"/>
	</xsl:element>
    </div>
    <p> <xsl:attribute name="class"><xsl:text>lessonsP-contemporary</xsl:text><xsl:value-of select="..//UUID" />
   </xsl:attribute><xsl:value-of select="DESCRIPTION"/></p>
    <div> <xsl:attribute name="class"><xsl:text>syllabusH3-contemporary</xsl:text><xsl:value-of select="..//UUID" />
   </xsl:attribute><xsl:value-of select="LESSONDATE"/></div>         
    <div> <xsl:attribute name="class"><xsl:text>syllabusH3-contemporary</xsl:text><xsl:value-of select="..//UUID" />
   </xsl:attribute><xsl:value-of select="LESSONTIME"/></div> 
  </xsl:for-each>

  </div>
  </div>
  </td>
  </tr>
  </table>
</xsl:template>


</xsl:stylesheet>

		
