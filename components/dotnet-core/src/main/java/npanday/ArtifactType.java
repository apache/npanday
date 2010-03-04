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
package npanday;

/**
 * Enumeration of all the valid target types (module, library, winexe, exe, nar) for the .NET platform.
 *
 * @author Shane Isbell
 */
public enum ArtifactType
{
    MODULE( "module", "module", "netmodule" ),
    LIBRARY( "library", "library", "dll" ),
    EXE( "exe", "exe", "exe" ),
    WINEXE( "winexe", "winexe", "exe" ),
    NAR( "nar", "null", "nar" ),
    EXECONFIG( "exe.config", "null", "exe.config" ),
    NETPLUGIN( "netplugin", "library", "dll" ),
    VISUAL_STUDIO_ADDIN( "visual-studio-addin", "library", "dll" ),
    SHARP_DEVELOP_ADDIN( "sharp-develop-addin", "library", "dll" ),
    NULL( "null", "null", "null" ),     
    ASP ( "asp", "library", "zip" ),
    GAC ( "gac", "null", "dll"),
    GAC_GENERIC ("gac_generic", "null", "dll"),
    GAC_MSIL ("gac_msil", "null", "dll"),
    GAC_32 ( "gac32", "null", "dll");


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
     * Constructor
     */
    ArtifactType( String packagingType, String targetCompileType, String extension )
    {
        this.packagingType = packagingType;
        this.targetCompileType = targetCompileType;
        this.extension = extension;
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
