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
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * Port of Nugets VersionSpec as of version 1.7
 *
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 */
public class NugetVersionSpec
{
    NugetSemanticVersion MinVersion, MaxVersion;

    boolean IsMinInclusive, IsMaxInclusive;

    @Override
    public String toString()
    {
        if ( MinVersion != null && IsMinInclusive && MaxVersion == null && !IsMaxInclusive )
        {
            return MinVersion.toString();
        }

        if ( MinVersion != null && MaxVersion != null && MinVersion == MaxVersion && IsMinInclusive && IsMaxInclusive )
        {
            return "[" + MinVersion + "]";
        }

        StringBuilder versionBuilder = new StringBuilder();
        versionBuilder.append( IsMinInclusive ? '[' : '(' );
        versionBuilder.append( MinVersion + ", " + MaxVersion );
        versionBuilder.append( IsMaxInclusive ? ']' : ')' );

        return versionBuilder.toString();
    }

    static Splitter VERSIONS_IN_RANGE = Splitter.on( "," ).omitEmptyStrings().trimResults();

    public static NugetVersionSpec parse (String value){
        NugetVersionSpec spec = tryParse( value );
        Preconditions.checkArgument( spec != null, "Version spec '" + value + "' is invalid!");
        return spec;
    }

    public static NugetVersionSpec tryParse( String value )
    {
        Preconditions.checkNotNull( value );

        value = value.trim();
        NugetVersionSpec versionSpec = new NugetVersionSpec();

        // First, try to parse it as a plain version string
        NugetSemanticVersion version = NugetSemanticVersion.tryParse( value );
        if ( version != null )
        {
            // A plain version is treated as an inclusive minimum range
            versionSpec.IsMinInclusive = true;
            versionSpec.MinVersion = version;
            return versionSpec;
        }

        // Fail early if the string is too short to be valid
        if ( value.length() < 3 )
        {
            return null;
        }

        // The first character must be [ ot (
        switch ( value.charAt( 0 ) )
        {
            case '[':
                versionSpec.IsMinInclusive = true;
                break;
            case '(':
                versionSpec.IsMinInclusive = false;
                break;
            default:
                return null;
        }

        // The last character must be ] ot )
        switch ( value.charAt( value.length() - 1 ) )
        {
            case ']':
                versionSpec.IsMaxInclusive = true;
                break;
            case ')':
                versionSpec.IsMaxInclusive = false;
                break;
            default:
                return null;
        }

        // Get rid of the two brackets
        value = value.substring( 1, value.length() - 1 );

        // Split by comma, and make sure we don't get more than two pieces
        String[] parts = Iterables.toArray( VERSIONS_IN_RANGE.split( value ), String.class );
        if ( parts.length > 2 || parts.length == 0 )
        {
            return null;
        }

        // If there is only one piece, we use it for both min and max
        String minVersionString = parts[0];
        String maxVersionString = ( parts.length == 2 ) ? parts[1] : parts[0];

        NugetSemanticVersion minVersion = NugetSemanticVersion.tryParse( minVersionString );
        NugetSemanticVersion maxVersion = NugetSemanticVersion.tryParse( maxVersionString );

        if ( minVersion == null || maxVersion == null )
        {
            return null;
        }

        versionSpec.MinVersion = minVersion;
        versionSpec.MaxVersion = maxVersion;

        return versionSpec;
    }

    public boolean isSatisfiedBy(NugetSemanticVersion version){
        boolean condition = true;
        if (MinVersion != null)
        {
            if (IsMinInclusive)
            {
                condition = condition && version.compareTo( MinVersion ) >= 0;
            }
            else
            {
                condition = condition && version.compareTo( MinVersion) > 0;
            }
        }

        if (MaxVersion != null)
        {
            if (IsMaxInclusive)
            {
                condition = condition && version.compareTo( MaxVersion) <= 0;
            }
            else
            {
                condition = condition && version.compareTo( MaxVersion) < 0;
            }
        }

        return condition;
    }
}
