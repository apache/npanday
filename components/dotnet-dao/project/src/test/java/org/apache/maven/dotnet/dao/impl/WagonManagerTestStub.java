package org.apache.maven.dotnet.dao.impl;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
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
            File pomFile = artifact.getFile();
            try
            {
                FileOutputStream fos = new FileOutputStream( pomFile );
                FileInputStream fis = new FileInputStream( new File( basedir, "target/test-classes/" +
                    artifact.getGroupId() + "-" + artifact.getArtifactId() + "-" + artifact.getVersion() + ".xml" ) );
                IOUtil.copy( fis, fos );
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
