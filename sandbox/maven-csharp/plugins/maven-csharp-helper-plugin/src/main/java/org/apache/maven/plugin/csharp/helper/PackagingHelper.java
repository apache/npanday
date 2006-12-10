package org.apache.maven.plugin.csharp.helper;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

public class PackagingHelper {

	
	public static String getExtension(String packaging) throws MojoExecutionException {
		
		String extension = getExtensionForPackaging( packaging );
			
		if(StringUtils.isEmpty( extension) ) 
			throw new MojoExecutionException("Unknown packaging type:" + packaging );
		
		return extension;
	}
	
	
	private static String getExtensionForPackaging(String packaging){
		
		String extension = null;
		
		if( packaging.toLowerCase().equals( "dotnet-library" ) ) extension = "dll";
		else if( packaging.toLowerCase().equals( "dotnet-exe" ) ) extension =  "exe";
		else if( packaging.toLowerCase().equals( "dotnet-winexe" ) ) extension =  "exe";
		else if( packaging.toLowerCase().equals( "dotnet-module" ) ) extension =  "dll";
		else if( packaging.toLowerCase().equals( "dotnet-webapp" ) ) extension =  "dll";
		
		return extension;
	}
	
	
	private static String getPackagingForExtension(String extension){
		
		String packaging = null;
		
		if( extension.toLowerCase().equals( "dll" ) ) packaging = "dotnet-library" ;
		else if( extension.toLowerCase().equals( "exe") ) packaging =  "dotnet-exe" ;
		else if( extension.toLowerCase().equals( "exe" ) ) packaging = "dotnet-winexe" ;
		else if( extension.toLowerCase().equals( "dll") ) packaging = "dotnet-module"  ;
		else if( extension.toLowerCase().equals( "dll") ) packaging =  "dotnet-webapp" ;
		
		return packaging;
	}
	
	public static boolean isDotnetPackaging(String packaging){
		
		String extension = getExtensionForPackaging( packaging );
		
		if(StringUtils.isEmpty( extension ) ){
			return false;
		}else
			return true;
	}
}
