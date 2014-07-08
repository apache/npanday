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

package npanday.resolver.resolvers;

import npanday.ArtifactTypeHelper;
import npanday.executable.ExecutableRequirement;
import npanday.executable.ExecutionResult;
import npanday.executable.NetExecutable;
import npanday.executable.NetExecutableFactory;
import npanday.resolver.ArtifactResolvingContributor;
import npanday.resolver.NPandayResolutionCache;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.Os;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 * @plexus.component role="npanday.resolver.ArtifactResolvingContributor" role-hint="com"
 */
public class ComReferenceResolver
    extends AbstractLogEnabled
    implements ArtifactResolvingContributor
{
    /** @plexus.requirement */
    NPandayResolutionCache cache;

    /** @plexus.requirement */
    protected NetExecutableFactory netExecutableFactory;

    public void contribute(Artifact artifact, ArtifactRepository localRepository, List remoteRepositories,
                           Set<Artifact> additionalDependenciesCollector, ArtifactFilter filter)
	{
		// NO-OP
	}
	
    public void tryResolve(Artifact artifact, Set<Artifact> additionalDependenciesCollector, ArtifactFilter filter)
    {
        // resolve com reference
        // flow:
        // 1. generate the interop dll in temp folder and resolve to that path during dependency resolution
        // 2. cut and paste the dll to buildDirectory and update the paths once we grab the reference of
        // MavenProject (CompilerContext.java)
        if ( ArtifactTypeHelper.isComReference(artifact.getType()) )
        {
            if (cache.applyTo(artifact)){
                return;
            }

            String tokenId = artifact.getClassifier();
            String interopPath;
            try
            {
                interopPath = generateInteropDll( artifact.getArtifactId(), tokenId );
            }
            catch ( IOException e )
            {
                getLogger().error(
                    "NPANDAY-150-002: Error creating interop dll for " + artifact.getId()
                );
                return;
            }

            File f = new File( interopPath );

            if ( !f.exists() )
            {
                getLogger().error(
                    "NPANDAY-150-001: Dependency com_reference File not found:" + interopPath + " for " + artifact.getId()
                );
                return;
            }

            artifact.setFile( f );
            artifact.setResolved( true );
            cache.put(artifact);
        }
    }

    private String generateInteropDll( String name, String classifier )
        throws IOException
    {

        File tmpDir;
        String comReferenceAbsolutePath = "";
        try
        {
            tmpDir = getTempDirectory();
        }
        catch ( IOException e )
        {
            throw new IOException( "Unable to create temporary directory" );
        }

        try
        {
            comReferenceAbsolutePath = resolveComReferencePath( name, classifier );
        }
        catch ( Exception e )
        {
            throw new IOException( e.getMessage() );
        }

        String interopAbsolutePath = tmpDir.getAbsolutePath() + File.separator + "Interop." + name + ".dll";
        List<String> params = getInteropParameters( interopAbsolutePath, comReferenceAbsolutePath, name );

        try
        {
            final NetExecutable executable = netExecutableFactory.getExecutable(
                    new ExecutableRequirement( "MICROSOFT", null, null, "TLBIMP" ), params, null
            );
            executable.execute();
        }
        catch ( Exception e )
        {
            throw new IOException( e.getMessage() );
        }

        return interopAbsolutePath;
    }

    private File getTempDirectory()
        throws IOException
    {
        File tempFile = File.createTempFile( "interop-dll-", "" );
        File tmpDir = new File( tempFile.getParentFile(), tempFile.getName() );
        tempFile.delete();
        tmpDir.mkdir();
        return tmpDir;
    }

    private List<String> getInteropParameters( String interopAbsolutePath, String comRerefenceAbsolutePath,
                                               String namespace )
    {
        List<String> parameters = new ArrayList<String>();
        parameters.add( comRerefenceAbsolutePath );
        parameters.add( "/out:" + interopAbsolutePath );
        parameters.add( "/namespace:" + namespace );
        try
        {
            // beginning code for checking of strong name key or signing of projects
            String key = "";
            String keyfile = "";
            String currentWorkingDir = System.getProperty( "user.dir" );

            // Check if Parent pom
            List<String> modules = readPomAttribute( currentWorkingDir + File.separator + "pom.xml", "module" );
            if ( !modules.isEmpty() )
            {
                // check if there is a matching dependency with the namespace
                for ( String child : modules )
                {
                    // check each module pom file if there is existing keyfile
                    String tempDir = currentWorkingDir + File.separator + child;
                    try
                    {
                        List<String> keyfiles = readPomAttribute( tempDir + "\\pom.xml", "keyfile" );
                        if ( keyfiles.get( 0 ) != null )
                        {
                            // PROBLEM WITH MULTIMODULES
                            boolean hasComRef = false;
                            List<String> dependencies = readPomAttribute( tempDir + "\\pom.xml", "groupId" );
                            for ( String item : dependencies )
                            {
                                if ( item.equals( namespace ) )
                                {
                                    hasComRef = true;
                                    break;
                                }
                            }
                            if ( hasComRef )
                            {
                                key = keyfiles.get( 0 );
                                currentWorkingDir = tempDir;
                            }
                        }
                    }
                    catch ( Exception e )
                    {
                    }

                }
            }
            else
            {
                // not a parent pom, so read project pom file for keyfile value

                List<String> keyfiles = readPomAttribute( currentWorkingDir + File.separator + "pom.xml", "keyfile" );
                key = keyfiles.get( 0 );
            }
            if ( key != "" )
            {
                keyfile = currentWorkingDir + File.separator + key;
                parameters.add( "/keyfile:" + keyfile );
            }
            // end code for checking of strong name key or signing of projects
        }
        catch ( Exception ex )
        {
        }

        return parameters;
    }

    private List<String> readPomAttribute( String pomFileLoc, String tag )
    {
        List<String> attributes = new ArrayList<String>();

        try
        {
            File file = new File( pomFileLoc );

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse( file );
            doc.getDocumentElement().normalize();

            NodeList nodeLst = doc.getElementsByTagName( tag );

            for ( int s = 0; s < nodeLst.getLength(); s++ )
            {
                Node currentNode = nodeLst.item( s );

                NodeList childrenList = currentNode.getChildNodes();

                for ( int i = 0; i < childrenList.getLength(); i++ )
                {
                    Node child = childrenList.item( i );
                    attributes.add( child.getNodeValue() );
                }
            }
        }
        catch ( Exception e )
        {

        }

        return attributes;
    }

    private String resolveComReferencePath( String name, String classifier )
        throws Exception
    {
        String[] classTokens = classifier.split( "}" );

        classTokens[1] = classTokens[1].replace( "-", "\\" );

        String newClassifier = classTokens[0] + "}" + classTokens[1];

        String registryPath = "HKEY_CLASSES_ROOT\\TypeLib\\" + newClassifier;
        registryPath += Os.isArch( "x86" ) ? "\\win32\\" : "\\win64\\";
        int lineNoOfPath = 1;

        List<String> parameters = new ArrayList<String>();
        parameters.add( "query" );
        parameters.add( registryPath );
        parameters.add( "/ve" );

        ExecutionResult res;
        try
        {
            final NetExecutable executable = netExecutableFactory.getExecutable(
                    new ExecutableRequirement( "MICROSOFT", null, null, "REG" ), parameters, null
            );
            res = executable.execute();
        }
        catch ( Exception e )
        {
            throw new Exception( "Cannot find information of [" + name
                                     + "] ActiveX component in your system, you need to install this component first to continue." );
        }

        String out = res.getStandardOut();

        String tokens[] = out.split( "\n" );

        String lineResult = "";
        String[] result;
        if ( tokens.length >= lineNoOfPath - 1 )
        {
            lineResult = tokens[lineNoOfPath - 1];
        }

        result = lineResult.split( "REG_SZ" );

        if ( result.length > 1 )
        {
            return result[1].trim();
        }

        return null;
    }
}
