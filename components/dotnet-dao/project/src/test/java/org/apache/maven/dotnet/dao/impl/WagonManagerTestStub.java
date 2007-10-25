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
package org.apache.maven.dotnet.dao.impl;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.repository.RepositoryPermissions;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.PlexusContainer;

import java.util.List;
import java.util.Collection;
import java.io.File;
import java.io.IOException;

public class WagonManagerTestStub
    implements WagonManager
{

    private File basedir;

    public Wagon getWagon( String string )
        throws UnsupportedProtocolException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Wagon getWagon( Repository repository )
        throws UnsupportedProtocolException, WagonConfigurationException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void getArtifact( Artifact artifact, List list )
        throws TransferFailedException, ResourceDoesNotExistException
    {
        if ( artifact.getType().equals( "pom" ) )
        {
            try
            {
                FileUtils.copyFile(
                    new File( basedir, "target/remote-test-repo/" + new DefaultRepositoryLayout().pathOf( artifact ) ),
                    artifact.getFile() );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

    public void getArtifact( Artifact artifact, ArtifactRepository artifactRepository )
        throws TransferFailedException, ResourceDoesNotExistException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void putArtifact( File file, Artifact artifact, ArtifactRepository artifactRepository )
        throws TransferFailedException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void putArtifactMetadata( File file, ArtifactMetadata artifactMetadata,
                                     ArtifactRepository artifactRepository )
        throws TransferFailedException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void getArtifactMetadata( ArtifactMetadata artifactMetadata, ArtifactRepository artifactRepository,
                                     File file, String string )
        throws TransferFailedException, ResourceDoesNotExistException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setOnline( boolean b )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isOnline()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addProxy( String string, String string1, int i, String string2, String string3, String string4 )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addAuthenticationInfo( String string, String string1, String string2, String string3, String string4 )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addMirror( String string, String string1, String string2 )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setDownloadMonitor( TransferListener transferListener )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addPermissionInfo( String string, String string1, String string2 )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ProxyInfo getProxy( String string )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AuthenticationInfo getAuthenticationInfo( String string )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addConfiguration( String string, Xpp3Dom xpp3Dom )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setInteractive( boolean b )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void registerWagons( Collection collection, PlexusContainer plexusContainer )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void findAndRegisterWagons( PlexusContainer plexusContainer )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setDefaultRepositoryPermissions( RepositoryPermissions repositoryPermissions )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void setBaseDir( File basedir )
    {
        this.basedir = basedir;
    }
}
