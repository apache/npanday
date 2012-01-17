package npanday;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Enumeration of all the valid target types for the .NET platform.
 *
 * @author Shane Isbell
 * @author <a href="mailto:me@lcorneliussen.de">Lars Corneliussen</a>
 */
public enum ArtifactType
{
    NULL( null, null, null ),

    DOTNET_MODULE("dotnet-module", "module", "netmodule"),

    /**
     * A dll-file compiled by any of the .NET compilers.
     */
    DOTNET_LIBRARY("dotnet-library", "library", "dll"),

    /**
     * Configuration file attachable to a library
     * artifact.
    */
    DOTNET_LIBRARY_CONFIG("dotnet-library-config", null, "dll.config"),

    /**
     * A exe-file compiled by any of the .NET compilers.
     */
    DOTNET_EXECUTABLE("dotnet-executable", "exe", "exe"),
    
    /**
     * Configuration file attachable to a executable
     * artifact.
    */
    DOTNET_EXECUTABLE_CONFIG("dotnet-executable-config", null, "exe.config"),

    /**
     * A library that is expected to be installed into the GAC
     * before it is used.
     */
    DOTNET_GAC("dotnet-gac", null, "dll"),
    
    /**
     * A pdb file containing debug symbols for either
     * a dll or executable.
     */
    DOTNET_SYMBOLS("dotnet-symbols", null, "pdb"),
    
    /**
     * A tlb-file that contains information about types
     * of a library that are accessible through COM.
     */
    DOTNET_OLE_TYPE_LIB("ole-type-library", null, "tlb"),
    
    /**
     * Contains the inline code documentation.
     */
    DOTNET_VSDOCS("dotnet-vsdocs", null, "xml"),
    
    /**
     * A maven plugin authored in .NET.
     */
    DOTNET_MAVEN_PLUGIN("dotnet-maven-plugin", "library", "dll"),

    /**
     * A compilation of libraries and their complementary 
     * files as debug symbols, docs or local satellite
     * assemblies.
     */
    DOTNET_ARCHIVE("dotnet-archive", null, "zip"),

    /**
     * A zip containing everything an application (or library) needs
     * to be run, including transitive dependencies and so on.
     */
    DOTNET_APPLICATION("dotnet-application", null, "app.zip", true),

    // We should reconsider those..
    
    // DOTNET_ASPX("dotnet-aspx", "library", "dll"),
    // DOTNET_("dotnet-gac_generic", "library", "dll"),
    // DOTNET_("dotnet-gac_msil", "library", "dll"),
    // DOTNET_("dotnet-gac_msil4", "library", "dll"),
    // DOTNET_("dotnet-gac_32", "library", "dll"),
    // DOTNET_("dotnet-gac_64", "library", "dll"),	
    // DOTNET_("dotnet-nar", "library", "nar"),
    // DOTNET_("dotnet-visual-studio-addin", "library", "dll"),
    
    /** 
     * Use DOTNET_MODULE instead
     */
    @Deprecated
    MODULE( "module", "module", "netmodule" ),
    
    /** 
     * Use DOTNET_LIBRARY instead
     */
    @Deprecated
    LIBRARY( "library", "library", "dll" ),
    
    /** 
     * Use DOTNET_EXECUTABLE instead
     */
    @Deprecated
    EXE( "exe", "exe", "exe" ),
    
    /** 
     * Use DOTNET_EXECUTABLE instead
     */
    @Deprecated
    WINEXE( "winexe", "winexe", "exe" ),
    
    /** 
     * Use DOTNET_EXECUTABLE_CONFIG instead
     */
    @Deprecated
    EXECONFIG( "exe.config", "null", "exe.config" ),
    
    /**
     * Use DOTNET_MAVEN_PLUGIN instead
     */
    @Deprecated
    NETPLUGIN( "netplugin", "library", "dll" ),
    
    VISUAL_STUDIO_ADDIN( "visual-studio-addin", "library", "dll" ),
    
    SHARP_DEVELOP_ADDIN( "sharp-develop-addin", "library", "dll" ),
    
    ASP ( "asp", "library", "dll" ),
    
    GAC ( "gac", null, "dll"),

    /**
     * Use DOTNET_GAC instead
     */
    @Deprecated
    GAC_GENERIC ("gac_generic", null, "dll"),
    
    GAC_MSIL ("gac_msil", null, "dll"),

    GAC_MSIL4 ("gac_msil4", null, "dll"),    
    
    GAC_32 ( "gac_32", null, "dll"),

    GAC_32_4 ( "gac_32_4", null, "dll"),

    GAC_64 ( "gac_64", null, "dll"),

    GAC_64_4 ( "gac_64_4", null, "dll"),

    COM_REFERENCE( "com_reference", null, "dll"),

    /* Azure support */
    AZURE_CLOUD_SERVICE ("azure-cloud-service", null, "cspkg"),
    AZURE_CLOUD_SERVICE_CONFIGURATION ("azure-cloud-service-configuration", null, "cscfg"),

    /* MSDeploy support */
    MSDEPLOY_PACKAGE ("msdeploy-package", null, "msdeploy.zip", true),

    // Silverlight support
    SILVERLIGHT_LIBRARY ("silverlight-library", "library", "dll"),
    SILVERLIGHT_APPLICATION ("silverlight-application", "library", "xap", true);

    /**
     * The extension used for the artifact(netmodule, dll, exe)
     */
    private String extension;

    /**
     * The packaging type (as given in the package tag within the pom.xml) of the artifact.
     */
    private String packagingType;

    /**
     * The target types (module, library, winexe, exe) for the .NET platform.
     */
    private String targetCompileType;

    /**
     * Whether to flag the artifact type as including its dependencies (do not pass them on transitively), or not (pass
     * them on transitively)
     */
    private boolean includesDependencies = false;

    /**
     * Constructor
     */
    ArtifactType( String packagingType, String targetCompileType, String extension )
    {
        this.packagingType = packagingType;
        this.targetCompileType = targetCompileType;
        this.extension = extension;
    }

    /**
     * Constructor
     */
    ArtifactType( String packagingType, String targetCompileType, String extension, boolean includesDependencies )
    {
        this.packagingType = packagingType;
        this.targetCompileType = targetCompileType;
        this.extension = extension;
        this.includesDependencies = includesDependencies;
    }

    /**
     * Returns extension used for the artifact(netmodule, dll, exe).
     *
     * @return Extension used for the artifact(netmodule, dll, exe).
     */
    public String getExtension()
    {
        return extension;
    }

    /**
     * Returns the packaging type (as given in the package tag within the pom.xml) of the artifact.
     *
     * @return the packaging type (as given in the package tag within the pom.xml) of the artifact.
     */
    public String getPackagingType()
    {
        return packagingType;
    }

    /**
     * Returns target types (module, library, winexe, exe) for the .NET platform.
     *
     * @return target types (module, library, winexe, exe) for the .NET platform.
     */
    public String getTargetCompileType()
    {
        return targetCompileType;
    }

    public boolean isIncludesDependencies()
    {
        return includesDependencies;
    }

    /**
     * Returns artifact type for the specified packaging name
     *
     * @param packagingName the package name (as given in the package tag within the pom.xml) of the artifact.
     * @return the artifact type for the specified packaging name
     */
    public static synchronized ArtifactType getArtifactTypeForPackagingName( String packagingName )
    {
        for( ArtifactType t : ArtifactType.values() )
        {
            if(packagingName.equals(t.getPackagingType()))
                return t;
        }
        return ArtifactType.NULL;
    }
}
