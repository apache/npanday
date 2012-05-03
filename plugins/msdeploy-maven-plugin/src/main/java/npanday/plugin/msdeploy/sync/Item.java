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

package npanday.plugin.msdeploy.sync;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class Item
{
    private Artifact artifact;

    private String groupId;

    private String artifactId;

    private String version;

    private String contentPath;

    private SyncEvent preSync, postSync;

    private Destination destination;

    public void contextualize( Artifact artifact, Destination destination ) throws MojoFailureException
    {
        this.artifact = artifact;

        if ( this.destination == null )
        {
            this.destination = destination;
        }

        if ( !artifact.isResolved() )
        {
            throw new MojoFailureException(
                "NPANDAY-154-000: The artifact should already have been resolved: " + artifact
            );
        }
    }

    public File getPackageSource()
    {
        return artifact.getFile();
    }


    @Override
    public String toString()
    {
        return "Item{" + "packageSource=" + groupId + ":" + artifactId + ":" + version + ", packageTarget="
            + getDestinationArgument() + '}';
    }

    static Joiner JOIN_ON_COMMA = Joiner.on( "," ).skipNulls();

    public String getDestinationArgument()
    {
        List<String> parts = Lists.newArrayList();

        if ( contentPath != null )
        {
            parts.add( "contentPath=" + contentPath );
        }

        if ( destination != null
            && !Strings.isNullOrEmpty(destination.getComputerName())
            && !destination.getLocal()) {

            parts.add( "computerName=" + destination.getComputerName() );

            if (!Strings.isNullOrEmpty( destination.getServerId())) {
                if ( destination.getUsername() != null )
                {
                    parts.add( "username=" + destination.getUsername() );
                }

                if ( destination.getPassword() != null )
                {
                    parts.add( "password=" + destination.getPassword() );
                }

                if ( destination.getAuthType() != null )
                {
                    parts.add( "authType=" + destination.getAuthType() );
                }
            }
        }

        return JOIN_ON_COMMA.join( parts );
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public void setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getContentPath()
    {
        return contentPath;
    }

    public void setContentPath( String contentPath )
    {
        this.contentPath = contentPath;
    }

    public SyncEvent getPreSync()
    {
        return preSync;
    }

    public void setPreSync( SyncEvent preSync )
    {
        this.preSync = preSync;
    }

    public SyncEvent getPostSync()
    {
        return postSync;
    }

    public void setPostSync( SyncEvent postSync )
    {
        this.postSync = postSync;
    }

    public Destination getDestination()
    {
        return destination;
    }

    public void setDestination( Destination destination )
    {
        this.destination = destination;
    }
}
