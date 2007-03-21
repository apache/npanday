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
package org.apache.maven.dotnet.artifact;

/**
 * Enumeration of all the valid target types (module, library, winexe, exe, nar) for the .NET platform.
 *
 * @author Shane Isbell
 */
public enum ArtifactType
{
    MODULE( "module", "netmodule" ),
    LIBRARY( "library", "dll" ),
    EXE( "exe", "exe" ),
    WINEXE( "winexe", "exe" ),
    NAR( "nar", "nar" ),
    EXECONFIG( "exe.config", "exe.config" ),
    NULL( "null", "null" );

    private String extension;

    private String artifactTypeName;

    /**
     * Constructor
     */
    ArtifactType( String artifactTypeName, String extension )
    {
        this.artifactTypeName = artifactTypeName;
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
     * Returns target types (module, library, winexe, exe) for the .NET platform.
     *
     * @return target types (module, library, winexe, exe) for the .NET platform.
     */
    public String getArtifactTypeName()
    {
        return artifactTypeName;
    }

    public static synchronized ArtifactType getArtifactTypeForName(String name)
    {
        if ( name.equals( ArtifactType.MODULE.getArtifactTypeName() ) )
        {
            return ArtifactType.MODULE;
        }
        else if ( name.equals( ArtifactType.LIBRARY.getArtifactTypeName() ) )
        {
            return ArtifactType.LIBRARY;
        }
        else if ( name.equals( ArtifactType.EXE.getArtifactTypeName() ) )
        {
            return ArtifactType.EXE;
        }
        else if ( name.equals( ArtifactType.WINEXE.getArtifactTypeName() ) )
        {
            return ArtifactType.WINEXE;
        }
        else if ( name.equals( ArtifactType.NAR.getArtifactTypeName() ) )
        {
           return ArtifactType.LIBRARY;
        }
        else if ( name.equals( ArtifactType.NAR.getArtifactTypeName() ) )
        {
           return ArtifactType.NAR;
        }
        else if ( name.equals( ArtifactType.EXECONFIG.getArtifactTypeName() ) )
        {
           return ArtifactType.EXECONFIG;
        }
        return ArtifactType.NULL;
    }

    public static synchronized ArtifactType getArtifactTypeForExtension(String extension)
    {
        if ( extension.equals( ArtifactType.MODULE.getExtension()) )
        {
            return ArtifactType.MODULE;
        }
        else if ( extension.equals( ArtifactType.LIBRARY.getExtension() ) )
        {
            return ArtifactType.LIBRARY;
        }
        else if ( extension.equals( ArtifactType.EXE.getExtension() ) )
        {
            return ArtifactType.EXE;
        }
        else if ( extension.equals( ArtifactType.WINEXE.getExtension() ) )
        {
            return ArtifactType.WINEXE;
        }
        else if ( extension.equals( ArtifactType.NAR.getExtension() ) )
        {
           return ArtifactType.LIBRARY;
        }
        else if ( extension.equals( ArtifactType.EXECONFIG.getExtension() ) )
        {
           return ArtifactType.EXECONFIG;
        }
        return ArtifactType.NULL;
    }
}
