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
package org.apache.maven.dotnet.plugin.resolver;

/**
 * Provides access to net dependency information.
 *
 * @author Shane Isbell
 */
public class NetDependency
{

    private String version;

    private String artifactId;

    private String groupId;

    private String type;

    private  boolean isGacInstall;

    public boolean isGacInstall()
    {
        return isGacInstall;
    }

    public void setGacInstall( boolean gacInstall )
    {
        isGacInstall = gacInstall;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
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

        final NetDependency that = (NetDependency) o;

        if ( !artifactId.equals( that.artifactId ) )
        {
            return false;
        }
        if ( !groupId.equals( that.groupId ) )
        {
            return false;
        }
        if ( type != null ? !type.equals( that.type ) : that.type != null )
        {
            return false;
        }
        if ( !version.equals( that.version ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = version.hashCode();
        result = 29 * result + artifactId.hashCode();
        result = 29 * result + groupId.hashCode();
        result = 29 * result + ( type != null ? type.hashCode() : 0 );
        return result;
    }
}
