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

package npanday.executable.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 */
public class VersionComparer
{
    public static boolean isVendorVersionMissmatch( String capability, String requirement )
    {
        return capability != null && !requirement.equals( capability );
    }

    public static boolean isFrameworkVersionMissmatch( List<String> capability, final String requirement )
    {
        if ( capability != null && capability.size() > 0 )
        {
            if ( !Iterables.any(
                capability, new Predicate<String>()
            {
                public boolean apply( @Nullable String frameworkVersion )
                {
                    return normalize(requirement).equals( normalize(frameworkVersion) );
                }
            }
            ) )
            {
                return true;
            }
        }

        return false;
    }

    private static String normalize( String version )
    {
        if ( version.equals( "1.0.3705") )
        {
            return "1.0";
        }

        if ( version.equals( "1.1.4322") )
        {
            return "1.1";
        }

        if ( version.equals( "2.0.50727") )
        {
            return "2.0";
        }

        if ( version.equals( "v4.0.30319") )
        {
            return "4.0";
        }

        return version;
    }
}
