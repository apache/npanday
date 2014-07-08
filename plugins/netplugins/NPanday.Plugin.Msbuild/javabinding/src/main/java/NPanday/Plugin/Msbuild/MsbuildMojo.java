package NPanday.Plugin.Msbuild;

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

import java.io.File;
import java.io.IOException;
import java.util.*;

import npanday.LocalRepositoryUtil;
import npanday.PlatformUnsupportedException;
import npanday.msbuild.MsbuildException;
import npanday.msbuild.MsbuildInvocationParameters;
import npanday.msbuild.MsbuildInvoker;
import npanday.resolver.NPandayDependencyResolution;
import npanday.vendor.VendorRequirement;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

/**
 * @phase validate
 * @goal compile
 */
public class MsbuildMojo
    extends AbstractMojo
{
    /**
     * @parameter expression = "${project}"
     */
    private org.apache.maven.project.MavenProject project;

    /**
     * @parameter expression = "${reactorProjects}"
     */
    private List<org.apache.maven.project.MavenProject> reactorProjects;

    /**
     * @parameter expression = "${settings.localRepository}"
     */
    private String localRepository;

    /**
     * @parameter expression = "${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * @component
     */
    private MsbuildInvoker msbuildInvoker;

    /**
     * @parameter default-value=".references"
     */
    private File referencesDirectory;

    /**
     * @parameter expression="${msbuild.configuration}" default-value="Debug"
     */
    public String configuration;

    /**
     * @parameter 
     */
    private String platform;

    /**
     * @parameter default-value="true"
     */
    private boolean copyReferences = true;

    /**
     * @parameter default-value="false"
     */
    private boolean attachXBAP = false;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private NPandayDependencyResolution dependencyResolution;

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * @parameter expression="${msbuild.extraArguments}"
     * @deprecated use {@linkplain #extraArgs}. This does not support quoted strings containing spaces
     */
    public String extraArguments;

    /**
     * @parameter
     */
    private List<String> extraArgs;

    public final void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Collection<Artifact> requiredArtifacts;
        try
        {
            requiredArtifacts = dependencyResolution.require( project, LocalRepositoryUtil.create( localRepository ), "test" );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException(
                "NPANDAY-154-010: Could not satisfy required dependencies of scope test", e
            );
        }

        if ( copyReferences )
        {
            copyDependencies(requiredArtifacts);
        }

        File msbuildFile = null;
        for (String ext : new String[] { "csproj", "vbproj", "ccproj" }) {
            msbuildFile = new File(project.getBuild().getSourceDirectory(), project.getArtifactId() + "." + ext);
            if (msbuildFile.exists()) {
                break;
            }
        }
        if (msbuildFile == null) {
            throw new MojoFailureException("No MSBuild project file found in the current directory");
        }
        MsbuildInvocationParameters params = new MsbuildInvocationParameters(
                new VendorRequirement(vendor, vendorVersion, frameworkVersion), msbuildFile);

        // must use /v:q here, as /v:m and above report the csc command, that includes '/errorprompt', which
        // erroneously triggers the NPANDAY-063-001 error
        params.setVerbosity("q");

        // BuildingInsideVisualStudio is required to avoid building project references on framework 2.0
        params.setProperty("BuildProjectReferences", "false");
        params.setProperty("BuildingInsideVisualStudio", "true");

        if (configuration != null) {
            params.setProperty("Configuration", configuration);
        }

        if (extraArguments != null) {
            params.addExtraArguments(Arrays.asList(extraArguments.split(" ")));
        }
        if (extraArgs != null) {
            params.addExtraArguments(extraArgs);
        }

        try {
            msbuildInvoker.invoke(params);
        } catch (PlatformUnsupportedException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (MsbuildException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        // #12549 - add resources generated by MSBuild to the project
        String directory = getMSBuildOutputDirectory();

        addGeneratedResources(directory);

        addGeneratedSources(directory);

        if (attachXBAP) {
            attachXBAPArtifact(directory);
        }
    }

    private String getMSBuildOutputDirectory() {
        String directory = new File( project.getBasedir(), "obj" ).getAbsolutePath();
        if ( platform != null )
        {
            directory += "/" + platform;
        }
        directory += "/" + configuration;
        return directory;
    }

    private void attachXBAPArtifact(String directory) {
        projectHelper.attachArtifact(project, "xbap", new File(directory, project.getArtifactId() + ".xbap"));
        File manifest = new File(directory, project.getArtifactId() + ".exe.manifest");
        if (manifest.exists()) {
            getLog().info("Attaching manifest: " + manifest);
            projectHelper.attachArtifact(project, "exe.manifest", manifest);
        }
        else {
            getLog().debug("Manifest not found: " + manifest);
        }
    }

    private void addGeneratedSources(String directory) throws MojoExecutionException {
        if ( new File( directory ).exists() ) {
            // Pick up generated source patterns to compile
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( directory );
            scanner.setIncludes(new String[] { "**/*.g.cs", "**/*.g.vb" });
            scanner.scan();

            String[] files = scanner.getIncludedFiles();
            if ( files != null && files.length > 0 )
            {
                getLog().info( "Adding sources from " + directory + " generated by MSBuild" );
                File generatedSourcesDirectory = new File( project.getBuild().getDirectory(), "generated-sources/msbuild" );
                for ( String f : files )
                {
                    File src = new File( directory, f );
                    File dest = new File( generatedSourcesDirectory, f );
                    dest.getParentFile().mkdirs();
                    try {
                        FileUtils.copyFile(src, dest);
                    } catch (IOException e) {
                        throw new MojoExecutionException("Unable to copy source file: " + e.getLocalizedMessage(), e);
                    }
                }
                project.addCompileSourceRoot( generatedSourcesDirectory.getAbsolutePath() );
            }
        }
    }

    private void addGeneratedResources(String directory) {
        getLog().info( "Adding resources from " + directory + " generated by MSBuild" );
        Resource resource = new Resource();
        resource.setDirectory( directory );
        resource.addInclude( "**/*.resources" );
        project.addResource( resource );
    }

    private void copyDependencies(Collection<Artifact> requiredArtifacts) throws MojoExecutionException {
        Map<String,MavenProject> projects = new HashMap<String,MavenProject>();
        for ( MavenProject p : reactorProjects )
        {
            projects.put( ArtifactUtils.versionlessKey(p.getGroupId(), p.getArtifactId()), p );
        }
        getLog().info( "projects = " + projects.keySet() );

        for ( Object artifact : requiredArtifacts )
        {
            Artifact a = (Artifact) artifact;

            File targetDir;
            String vKey = ArtifactUtils.versionlessKey( a );
            if ( !projects.containsKey( vKey ) )
            {
                String path =
                    a.getGroupId() + "/" + a.getArtifactId() + "-" + a.getBaseVersion();
                targetDir = new File( referencesDirectory, path );
            }
            else
            {
                // Likely a project reference in MSBuild.
                // If the other project was not built with MSBuild, make sure the artifact is present where it will look for it
                // Note: deliberately limited for now - will only work with reactor projects and doesn't test what are references and what are not
                File binDir = new File( projects.get( vKey ).getBasedir(), "bin" );
                targetDir = new File( binDir, configuration );
            }
            File targetFile = new File( targetDir, a.getArtifactId() + "." + a.getArtifactHandler().getExtension() );

            getLog().info( "Copying reference " + vKey + " to " + targetFile );
            if ( !targetFile.exists() )
            {
                targetFile.getParentFile().mkdirs();

                try
                {
                    FileUtils.copyFile(a.getFile(), targetFile);
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException(
                        "Error copying reference from the local repository to .references: " + e.getMessage(), e );
                }
            }
        }
    }
}
