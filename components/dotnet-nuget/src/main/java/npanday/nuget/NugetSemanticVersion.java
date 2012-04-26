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

package npanday.nuget;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Port of Nugets SemanticVersion as of version 1.7
 *
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 */
public class NugetSemanticVersion
    implements Comparable<NugetSemanticVersion>
{
    private static Pattern _semanticVersionRegex = Pattern.compile("^(\\d+(\\s*\\.\\s*\\d+){0,3})(-[a-z][0-9a-z-]*)?$", Pattern.CASE_INSENSITIVE);
    private static Pattern _strictSemanticVersionRegex = Pattern.compile("^(\\d+(\\.\\d+){2})(-[a-z][0-9a-z-]*)?$", Pattern.CASE_INSENSITIVE);
    private String _originalString;

    public NugetSemanticVersion( int major, int minor, int build, int revision )
    {
        this( new DotnetVersion( major, minor, build, revision ) );
    }

    public NugetSemanticVersion( int major, int minor, int build, String specialVersion )
    {
        this(new DotnetVersion(major, minor, build), specialVersion);
    }

    public NugetSemanticVersion( DotnetVersion version )
    {
        this( version, "" );
    }

    public NugetSemanticVersion( DotnetVersion version, String specialVersion )

    {
        this( version, specialVersion, null );
    }

    private NugetSemanticVersion( DotnetVersion version, String specialVersion, String originalString )
    {
        Preconditions.checkNotNull( version );

        this.version = version;
        this.specialVersion = specialVersion == null ? "" : specialVersion;
        _originalString = Strings.isNullOrEmpty( originalString ) ? version.toString() + (!Strings.isNullOrEmpty(
            specialVersion
        ) ? '-' + specialVersion : null) : originalString;
    }


    /**
     *  Gets the normalized version portion.
     */
    DotnetVersion version;

    /**
     *  Gets the optional special version.
     */
    String specialVersion;

    /**
     * Parses a version string using loose semantic versioning rules that allows 2-4 version components followed by an optional special version.
     */
    public static NugetSemanticVersion parse( String version )
    {
        NugetSemanticVersion semVer = tryParse( version );
        Preconditions.checkArgument( semVer != null, "Version '" + version + "' is invalid!" );
        return semVer;
    }

    /**
     * Parses a version string using loose semantic versioning rules that allows 2-4 version components followed by an optional special version.
     * @param version
     * @return
     */
    public static NugetSemanticVersion tryParse( String version )
    {
        return tryParseInternal( version, _semanticVersionRegex );
    }

    /**
     * Parses a version string using strict semantic versioning rules that allows exactly 3 components and an optional special version.
     */
    public static NugetSemanticVersion tryParseStrict( String version )
    {
        return tryParseInternal( version, _strictSemanticVersionRegex );
    }

    private static NugetSemanticVersion tryParseInternal( String version, Pattern regex )
    {
        if (Strings.isNullOrEmpty( version ))
        {
            return null;
        }

        Matcher match = regex.matcher( version.trim() );
        DotnetVersion versionValue;
        if (!match.matches() || null == (versionValue = DotnetVersion.parse( match.group( 1 ) )))
        {
            return null;
        }

        String release = match.group( 3 );
        if (release != null && release.startsWith( "-" ))
            release = release.substring( 1 );

        return new NugetSemanticVersion(versionValue, release, version.replace( " ", "" ));
    }

    @Override
    public String toString()
    {
        return _originalString;
    }

    public boolean equals(NugetSemanticVersion other)
    {
        return other != null &&
            version.equals( other.version ) &&
            specialVersion.equalsIgnoreCase( other.specialVersion );
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + this.version.hashCode();
        hash = 43 * hash + (this.specialVersion != null ? this.specialVersion.hashCode() : 0);
        return hash;
    }

    public int compareTo( NugetSemanticVersion other )
    {
        if (this == other)
        {
            return 1;
        }

        int result = version.compareTo(other.version );

        if (result != 0)
        {
            return result;
        }

        boolean empty = Strings.isNullOrEmpty( specialVersion );
        boolean otherEmpty = Strings.isNullOrEmpty(other.specialVersion );
        if (empty && otherEmpty)
        {
            return 0;
        }
        else if (empty)
        {
            return 1;
        }
        else if (otherEmpty)
        {
            return -1;
        }
        return specialVersion.compareToIgnoreCase( other.specialVersion );
    }
}
