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
package org.apache.maven.dotnet.plugin.compile;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.AbstractMojo;

import org.apache.maven.project.MavenProject;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.vendor.VendorFactory;
import org.apache.maven.dotnet.executable.compiler.*;

import java.util.ArrayList;
import java.io.File;

/**
 * Maven Mojo for compiling Class files to the .NET Intermediate Language.
 * To use a specific vendor (MICROSOFT/MONO) or language, the compiler/language must be previously installed AND
 * configured through the plugin-compiler.xml file: otherwise the Mojo either will throw a MojoExecutionException
 * telling you that the platform is not supported (occurs if entry is not in plugin-compilers.xml, regardless of
 * whether the compiler/language is installed) or will attempt to execute the compiler and fail (occurs if entry is in
 * plugin-compilers.xml and the compiler/language is not installed).
 *
 * @author Shane Isbell
 * @goal compile
 * @phase compile
 * @description Maven Mojo for compiling Class files to the .NET Intermediate Language
 */
public final class CompilerMojo extends AbstractMojo {

    /**
     * @parameter expression="${settings.localRepository}"
     * @required
     */
    private File localRepository;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * Additional compiler commands
     *
     * @parameter expression = "${parameters}"
     */
    private ArrayList<String> parameters;

    /**
     * Delay-sign the assembly using only the public portion of the strong name key. (not currently supported)
     */
    private boolean delaysign;

    /**
     * Specify a strong name key file. (not currently supported)
     */
    private File keyfile;

    /**
     * Specifies a strong name key container. (not currently supported)
     */
    private String keycontainer;

    /**
     * Limit the platforms this code can run on. (not currently supported)
     *
     * @parameter expression = "${platform} default-value = "anycpu"
     */
    private String platform;

    /**
     * @parameter expression = "${frameworkVersion}" 
     */
    private String frameworkVersion;

    /**
     * The profile that the compiler should use to compile classes: FULL, COMPACT, (or a custom one specified in a
     * compiler-plugins.xml).
     *
     * @parameter expression = "${profile}" default-value = "FULL"
     */
    private String profile;

    /**
     * .NET Language. The default value is <code>C_SHARP</code>. Not case or white-space sensitive.
     *
     * @parameter expression="${language}" default-value = "C_SHARP"
     * @required
     */
    private String language;

    /**
     * The Vendor for the Compiler. Supports MONO and MICROSOFT: the default value is <code>MICROSOFT</code>. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * This over-rides the defaultAssemblyPath for the compiler plugin.
     *
     * @parameter expression = "${profileAssemblyPath}
     */
    private File profileAssemblyPath;

    /**
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * @component
     */
    private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

    /**
     * Compiles the class files.
     *
     * @throws MojoExecutionException thrown if MOJO is unable to compile the class files or if the environment is not
     *                                properly set.
     */
    public void execute() throws MojoExecutionException {
        if (profileAssemblyPath != null && !profileAssemblyPath.exists())
            throw new MojoExecutionException("NMAVEN-900-007: Profile Assembly Path does not exist: Path = " +
                    profileAssemblyPath.getAbsolutePath());

        //Requirement
        CompilerRequirement compilerRequirement = CompilerRequirement.Factory.createDefaultCompilerRequirement();
        compilerRequirement.setLanguage(language);
        compilerRequirement.setFrameworkVersion(frameworkVersion);
        compilerRequirement.setProfile(profile);
        compilerRequirement.setVendorVersion(vendorVersion);
        try {
            if(vendor != null) compilerRequirement.setVendor(VendorFactory.createVendorFromName(vendor));
        } catch (PlatformUnsupportedException e) {
            throw new MojoExecutionException("NMAVEN-900-000: Unknown Vendor: Vendor = " + vendor, e);
        }

        //Config
        CompilerConfig compilerConfig = (CompilerConfig) CompilerConfig.Factory.createDefaultExecutableConfig();
        compilerConfig.setLocalRepository(localRepository);
        if (parameters != null) compilerConfig.setCommands(parameters);
        String artifactTypeName = project.getArtifact().getType();

        if (artifactTypeName.equals(ArtifactType.MODULE.getArtifactTypeName())) {
            compilerConfig.setArtifactType(ArtifactType.MODULE);
        } else if (artifactTypeName.equals(ArtifactType.LIBRARY.getArtifactTypeName())) {
            compilerConfig.setArtifactType(ArtifactType.LIBRARY);
        } else if (artifactTypeName.equals(ArtifactType.EXE.getArtifactTypeName())) {
            compilerConfig.setArtifactType(ArtifactType.EXE);
        } else if (artifactTypeName.equals(ArtifactType.WINEXE.getArtifactTypeName())) {
            compilerConfig.setArtifactType(ArtifactType.WINEXE);
        } else if (artifactTypeName.equals(ArtifactType.NAR.getArtifactTypeName())) {
            compilerConfig.setArtifactType(ArtifactType.LIBRARY);
        } else {
            throw new MojoExecutionException("NMAVEN-900-001: Unrecognized artifact type: Language = " + language
                    + ", Vendor = " + vendor + ", ArtifactType = " + artifactTypeName);
        }

        try {
            CompilerExecutable compilerExecutable = netExecutableFactory.getCompilerExecutableFor(compilerRequirement,
                    compilerConfig, project, profileAssemblyPath);
            compilerExecutable.execute();
            project.getArtifact().setFile(compilerExecutable.getCompiledArtifact());
        } catch (PlatformUnsupportedException e) {
            throw new MojoExecutionException("NMAVEN-900-003: Unsupported Platform: Language = " + language
                    + ", Vendor = " + vendor + ", ArtifactType = " + artifactTypeName, e);
        } catch (ExecutionException e) {
            throw new MojoExecutionException("NMAVEN-900-004: Unable to Compile: Language = " + language
                    + ", Vendor = " + vendor + ", ArtifactType = " + artifactTypeName + ", Source Directory = "
                    + project.getBuild().getSourceDirectory(), e);
        }
    }
}

/*
        if (compilerVendor.equals(Vendor.MICROSOFT)) {
            File frameworkPath = new File("C:\\WINDOWS\\Microsoft.NET\\Framework\\v" + frameworkVersion);
            if (!frameworkPath.exists())
                throw new MojoExecutionException("NMAVEN-900-006: Could not find .NET framework: "
                        + ", Path = " + frameworkPath.getAbsolutePath());
            compilerConfig.setExecutionPath(frameworkPath.getAbsolutePath() + File.separator);
        }
*/