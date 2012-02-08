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
 * Some helper methods for classifying packaging types.
 *
 * @author <a href="mailto:me@lcorneliussen.de">Lars Corneliussen</a>
 */
public class ArtifactTypeHelper
{
    public static boolean isDotnetMavenPlugin(String packaging)
    {
        return isDotnetMavenPlugin( ArtifactType.getArtifactTypeForPackagingName( packaging ) );
    }

    public static boolean isDotnetMavenPlugin(ArtifactType packaging)
    {
        return packaging.equals( ArtifactType.DOTNET_MAVEN_PLUGIN )
                || packaging.equals( ArtifactType.NETPLUGIN );
    }

    public static boolean isDotnetExecutable(String packaging)
    {
        return isDotnetExecutable( ArtifactType.getArtifactTypeForPackagingName( packaging ) );
    }

    public static boolean isDotnetExecutable(ArtifactType packaging)
    {
        return packaging.equals( ArtifactType.DOTNET_EXECUTABLE )
                || packaging.equals( ArtifactType.EXE )
                || packaging.equals( ArtifactType.WINEXE );
    }

    public static boolean isDotnetLibrary(String packaging)
    {
        return isDotnetLibrary( ArtifactType.getArtifactTypeForPackagingName( packaging ) );
    }

    public static boolean isDotnetLibrary(ArtifactType packaging)
    {
        return packaging.equals( ArtifactType.DOTNET_LIBRARY )
                || packaging.equals( ArtifactType.COM_REFERENCE )
                || packaging.equals( ArtifactType.LIBRARY );
    }

    public static boolean isDotnetModule(String packaging)
    {
        return isDotnetModule( ArtifactType.getArtifactTypeForPackagingName( packaging ) );
    }

    public static boolean isDotnetModule(ArtifactType packaging)
    {
        return packaging.equals( ArtifactType.DOTNET_MODULE )
                || packaging.equals( ArtifactType.MODULE );
    }

    public static boolean isDotnetExecutableConfig(String packaging)
    {
        return isDotnetExecutableConfig( ArtifactType.getArtifactTypeForPackagingName( packaging ) );
    }

    public static boolean isDotnetExecutableConfig(ArtifactType packaging)
    {
        return packaging.equals( ArtifactType.DOTNET_EXECUTABLE_CONFIG )
                || packaging.equals( ArtifactType.EXECONFIG );
    }


    public static boolean isDotnetGenericGac(String packaging)
    {
        return isDotnetGenericGac( ArtifactType.getArtifactTypeForPackagingName( packaging ) );
    }

    public static boolean isDotnetGenericGac(ArtifactType packaging)
    {
        // the new gac should always be generic.
        // GAC_MSIL, GAC32 and GAC should be deprecated without
        // any replacements.
        return packaging.equals( ArtifactType.DOTNET_GAC )
                || packaging.equals( ArtifactType.GAC_GENERIC );
    }

    public static boolean isDotnetAnyGac(String packaging)
    {
        return isDotnetAnyGac( ArtifactType.getArtifactTypeForPackagingName( packaging ) );
    }

    public static boolean isDotnetAnyGac(ArtifactType packaging)
    {
        return packaging.equals( ArtifactType.DOTNET_GAC )
                || packaging.equals( ArtifactType.GAC_GENERIC )
                || packaging.equals( ArtifactType.GAC )
                || packaging.equals( ArtifactType.GAC_32)
                || packaging.equals( ArtifactType.GAC_32_4)
                || packaging.equals( ArtifactType.GAC_64)
                || packaging.equals( ArtifactType.GAC_64_4)
                || packaging.equals( ArtifactType.GAC_MSIL )
                || packaging.equals( ArtifactType.GAC_MSIL4 );
    }

    public static boolean isDotnet4Gac(String packaging)
    {
        return isDotnet4Gac( ArtifactType.getArtifactTypeForPackagingName( packaging ) );
    }

    public static boolean isDotnet4Gac(ArtifactType packaging)
    {
        return packaging.equals( ArtifactType.GAC_MSIL4 )
                || packaging.equals( ArtifactType.GAC_32_4)
                || packaging.equals( ArtifactType.GAC_64_4);
    }
}
