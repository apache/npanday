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
 * Provides access to the major, minor, build and revision version information.
 *
 * @author Shane Isbell
 */
public class Version
{
    private int major;

    private int minor;

    private int build;

    private int revision;

    /**
     * Default constructor
     */
    public Version()
    {
    }

    public int getMajor()
    {
        return major;
    }

    public void setMajor( int major )
    {
        this.major = major;
    }

    public int getMinor()
    {
        return minor;
    }

    public void setMinor( int minor )
    {
        this.minor = minor;
    }

    public int getBuild()
    {
        return build;
    }

    public void setBuild( int build )
    {
        this.build = build;
    }

    public int getRevision()
    {
        return revision;
    }

    public void setRevision( int revision )
    {
        this.revision = revision;
    }

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        final Version version = (Version) o;

        if ( build != version.build )
        {
            return false;
        }
        if ( major != version.major )
        {
            return false;
        }
        if ( minor != version.minor )
        {
            return false;
        }
        if ( revision != version.revision )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = major;
        result = 29 * result + minor;
        result = 29 * result + build;
        result = 29 * result + revision;
        return result;
    }

}
