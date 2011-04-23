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

package npanday.plugin.compile;

import npanday.ArtifactTypeHelper;
import org.apache.maven.artifact.Artifact;
import npanday.PlatformUnsupportedException;
import npanday.executable.ExecutionException;
import npanday.executable.compiler.CompilerConfig;
import npanday.executable.compiler.CompilerExecutable;
import npanday.executable.compiler.CompilerRequirement;
import npanday.registry.impl.StandardRepositoryLoader;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.impl.SettingsRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.List;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.*;
import java.util.Hashtable;
import java.io.*;

/**
 * Abstract Class for compile mojos for both test-compile and compile.
 *
 * @author Shane Isbell, Leopoldo Lee Agdeppa III
 */
public abstract class AbstractCompilerMojo
        extends AbstractMojo
{

    /**
     * @parameter expression="${npanday.settings}" default-value="${user.home}/.m2"
     */
    private String settingsPath;

    /**
     * Skips compiling of unit tests
     *
     * @parameter expression = "${skipTestCompile}" default-value = "false"
     */
    protected boolean skipTestCompile;


    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    /**
     * The location of the local Maven repository.
     *
     * @parameter expression="${settings.localRepository}"
     */
    protected File localRepository;

    /**
     * Additional compiler commands
     *
     * @parameter expression = "${parameters}"
     */
    protected ArrayList<String> parameters;


    /**
     * Additional compiler commands for test classes
     *
     * @parameter expression = "${testParameters}"
     */
    protected ArrayList<String> testParameters;


    /**
     * Specify a strong name key file.
     *
     * @parameter expression = "${keyfile}"
     */
    protected File keyfile;

    /**
     * Specify a strong name key file.
     *
     * @parameter expression = "${testKeyfile}"
     */
    protected File testKeyfile;


    /**
     * The starup object class
     *
     * @parameter expression = "${main}"
     */
    protected String main;


    /**
     * The starup object class
     *
     * @parameter expression = "${testMain}"
     */
    protected String testMain;


    /**
     * define
     *
     * @parameter expression = "${define}"
     */
    protected String define;


    /**
     * define
     *
     * @parameter expression = "${testDefine}"
     */
    protected String testDefine;

    /**
     * Specifies a strong name key container. (not currently supported)
     *
     * @parameter expression = "${keycontainer}"
     */
    protected String keycontainer;


    /**
     * Specifies a strong name key container. (not currently supported)
     *
     * @parameter expression = "${testKeycontainer}"
     */
    protected String testKeycontainer;

    /**
     * Limit the platforms this code can run on. (not currently supported)
     *
     * @parameter expression = "${platform} default-value = "anycpu"
     */
    protected String platform;

    /**
     * Limit the platforms this code can run on. (not currently supported)
     *
     * @parameter expression = "${testPlatform} default-value = "anycpu"
     */
    protected String testPlatform;

    /**
     * The framework version to compile under: 1.1, 2.0, 3.0
     *
     * @parameter expression = "${frameworkVersion}"
     */
    protected String frameworkVersion;


    /**
     * The framework version to compile the test classes: 1.1, 2.0, 3.0
     *
     * @parameter expression = "${testFrameworkVersion}"
     */
    protected String testFrameworkVersion;

    /**
     * The profile that the compiler should use to compile classes: FULL, COMPACT, (or a custom one specified in a
     * compiler-plugins.xml).
     *
     * @parameter expression = "${profile}" default-value = "FULL"
     */
    protected String profile;


    /**
     * The profile that the compiler should use to compile classes: FULL, COMPACT, (or a custom one specified in a
     * compiler-plugins.xml).
     *
     * @parameter expression = "${testProfile}" default-value = "FULL"
     */
    protected String testProfile;


    /**
     * .NET Language. The default value is <code>C_SHARP</code>. Not case or white-space sensitive.
     *
     * @parameter expression="${language}" default-value = "C_SHARP"
     * @required
     */
    protected String language;


    /**
     * .NET Language. The default value is <code>C_SHARP</code>. Not case or white-space sensitive.
     *
     * @parameter expression="${testLanguage}"
     */
    protected String testLanguage;


    /**
     * Returns the rootnamespace of the project. Used by VB project only.
     *
     * @parameter expression="${rootNamespace}"
     */
    protected String rootNamespace;


    /**
     * Returns the rootnamespace of the project. Used by VB project only.
     *
     * @parameter expression="${testRootNamespace}" default-value="${project.groupId}.${project.artifactId}"
     */
    protected String testRootNamespace;


    /**
     * The Vendor for the Compiler. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    protected String vendor;

    /**
     * The Vendor for the Compiler. Supports MONO and MICROSOFT: the default value is <code>MICROSOFT</code>. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${testVendor}"
     */
    protected String testVendor;


    /**
     * This over-rides the defaultAssemblyPath for the compiler plugin.
     *
     * @parameter expression = "${profileAssemblyPath}
     */
    protected File profileAssemblyPath;


    /**
     * This over-rides the defaultAssemblyPath for the compiler plugin.
     *
     * @parameter expression = "${testProfileAssemblyPath}
     */
    protected File testProfileAssemblyPath;

    /**
     * @parameter expression = "${vendorVersion}"
     */
    protected String vendorVersion;

    /**
     * @parameter expression = "${testVendorVersion}"
     */
    protected String testVendorVersion;


    /**
     * @parameter expression = "${isDebug}" default-value="false"
     */
    protected boolean isDebug;

    /**
     * @component
     */
    protected npanday.executable.NetExecutableFactory netExecutableFactory;

    /**
     * @parameter expression="${project.file}"
     * @required
     * @readonly
     */
    protected File pomFile;

    /**
     * Delay-sign the assembly using only the public portion of the strong name key
     *
     * @parameter
     */
    protected boolean delaysign;


    /**
     * Delay-sign the assembly using only the public portion of the strong name key
     *
     * @parameter
     */
    protected boolean testDelaysign;

    /**
     * Link the specified modules into this assembly
     *
     * @parameter expression="${addmodules}"
     */
    protected String[] addModules;

    /**
     * Link the specified modules into this assembly
     *
     * @parameter expression="${testAddmodules}"
     */
    protected String[] testAddModules;

    /**
     * Specify a Win32 resource file (.res)
     *
     * @parameter expression = "${win32res}"
     */
    protected String win32Res;

    /**
     * Specify a Win32 resource file (.res)
     *
     * @parameter expression = "${testWin32res}"
     */
    protected String testWin32Res;

    /**
     * Remove integer checks.
     *
     * @parameter
     */
    protected boolean removeintchecks;


    /**
     * Remove integer checks.
     *
     * @parameter
     */
    protected boolean testRemoveintchecks;

    /**
     * Specifies a Win32 icon file (.ico) for the default Win32 resources.
     *
     * @parameter expression = "${win32icon}"
     */
    protected String win32Icon;

    /**
     * Specifies a Win32 icon file (.ico) for the default Win32 resources.
     *
     * @parameter expression = "${testWin32icon}"
     */
    protected String testWin32Icon;

    /**
     * Declare global Imports for namespaces in referenced metadata files.
     *
     * @parameter expression = "${imports}"
     */
    protected String[] imports;

    /**
     * Declare global Imports for namespaces in referenced metadata files.
     *
     * @parameter expression = "${testImports}"
     */
    protected String[] testImports;

    /**
     * Included Source Codes
     *
     * @parameter expression = "${includeSources}"
     */
    protected File[] includeSources;

    /**
     * Included Source Codes
     *
     * @parameter expression = "${testIncludeSources}"
     */
    protected File[] testIncludeSources;

    /**
     * Embed the specified resource
     *
     * @parameter expression = "${resource}"
     */
    protected String resource;
    
    /**
     * Embed the specified resource
     *
     * @parameter expression = "${embeddedResources}"
     */
    protected ArrayList<String> embeddedResources;    

    /**
     * Embed the specified resource
     *
     * @parameter expression = "${testResource}"
     */
    protected String testResource;

    /**
     * Link the specified resource to this assembly
     *
     * @parameter expression = "${linkresource}"
     */
    protected String linkResource;

    /**
     * Link the specified resource to this assembly
     *
     * @parameter expression = "${testLinkresource}"
     */
    protected String testLinkResource;


    /**
     * Require explicit declaration of variables.
     *
     * @parameter
     */
    protected boolean optionexplicit;
    
    /**
     * Require explicit declaration of variables.
     *
     * @parameter
     */
    protected boolean testOptionexplicit;

    /**
     * Enforce strict language semantics / Warn when strict language semantics are not respected.
     *
     * @parameter expression = "${optionstrict}"
     */
    protected String optionStrict;

    /**
     * Enforce strict language semantics / Warn when strict language semantics are not respected.
     *
     * @parameter expression = "${testOptionstrict}"
     */
    protected String testOptionStrict;

    /**
     * Enable optimizations.
     *
     * @parameter
     */
    protected boolean optimize;

    /**
     * Enable optimizations.
     *
     * @parameter
     */
    protected boolean testOptimize;

    /**
     * Specifies binary or text style string comparisons
     *
     * @parameter expression = "${optioncompare}"
     */
    protected String optionCompare;


    /**
     * Specifies binary or text style string comparisons
     *
     * @parameter expression = "${testOptioncompare}"
     */
    protected String testOptionCompare;

    /**
     * Generate overflow checks
     *
     * @parameter
     */
    protected boolean checked;


    /**
     * Generate overflow checks
     *
     * @parameter
     */
    protected boolean testChecked;

    /**
     * Allow 'unsafe' code
     *     
     * @parameter
     */
    protected boolean unsafe;


    /**
     * Allow 'unsafe' code
     *
     * @parameter
     */
    protected boolean testUnsafe;

    /**
     * Do not auto include CSC.RSP/VBC.RSP file
     *
     * @parameter
     */
    protected boolean noconfig;


    /**
     * Do not auto include CSC.RSP/VBC.RSP file
     *
     * @parameter
     */
    protected boolean testNoconfig;

    /**
     * Base address for the library to be built
     *
     * @parameter expression = "${baseaddress}"
     */
    protected String baseAddress;

    /**
     * Base address for the library to be built
     *
     * @parameter expression = "${testBaseaddress}"
     */
    protected String testBaseAddress;

    /**
     * Create a 'Bug Report' file.
     *
     * @parameter expression = "${bugreport}"
     */
    protected String bugReport;

    /**
     * Create a 'Bug Report' file.
     *
     * @parameter expression = "${testBugreport}"
     */
    protected String testBugReport;

    /**
     * Specify the codepage to use when opening source files
     *
     * @parameter expression = "${codepage}"
     */
    protected String codePage;

    /**
     * Specify the codepage to use when opening source files
     *
     * @parameter expression = "${testCodepage}"
     */
    protected String testCodePage;

    /**
     * Output compiler messages in UTF-8 encoding
     *
     * @parameter
     */
    protected boolean utf8output;

    /**
     * Output compiler messages in UTF-8 encoding
     *
     * @parameter
     */
    protected boolean testUtf8output;

    /**
     * Specify debug information file name (default: output file name with .pdb extension)
     *
     * @parameter expression = "${pdb}"
     */
    protected String pdb;

    /**
     * Specify debug information file name (default: output file name with .pdb extension)
     *
     * @parameter expression = "${testPdb}"
     */
    protected String testPdb;

    /**
     * Specify how to handle internal compiler errors: prompt, send, queue, or none. The default is queue.
     *
     * @parameter expression = "${errorreport}"
     */
    protected String errorReport;

    /**
     * Specify how to handle internal compiler errors: prompt, send, queue, or none. The default is queue.
     *
     * @parameter expression = "${testErrorreport}"
     */
    protected String testErrorReport;

    /**
     * Name of the assembly which this module will be a part of
     *
     * @parameter expression = "${moduleassemblyname}"
     */
    protected String moduleAssemblyName;

    /**
     * Name of the assembly which this module will be a part of
     *
     * @parameter expression = "${testModuleassemblyname}"
     */
    protected String testModuleAssemblyName;

    /**
     * Specify additional directories to search in for references
     *
     * @parameter expression = "${libs}"
     */
    protected String[] libs;


    /**
     * Specify additional directories to search in for references
     *
     * @parameter expression = "${testLibs}"
     */
    protected String[] testLibs;


    /**
     * The artifact acts as an Integration test project
     *
     * @parameter
     */
    protected boolean integrationTest;



    /**
     * The directory for the compilated web application
     *
     * @parameter  expression = "${outputDirectory}"
     */
    protected File outputDirectory;
	
    /**
     * warn
     *
     * @parameter expression = "${warn}"
     */
    protected Integer warn;
    
    /**
     * testWarn
     *
     * @parameter expression = "${testWarn}"
     */
    protected Integer testWarn;
    
    /**
     * nowarn
     *
     * @parameter expression = "${nowarn}"
     */
    protected String nowarn;

 
    /**
     * testNowarn
     *
     * @parameter expression = "${testNowarn}"
     */
    protected String testNowarn;

    /**
     * @component
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * Compiles the class files.
     *
     * @throws MojoExecutionException thrown if MOJO is unable to compile the class files or if the environment is not
     *                                properly set.
     */


    public void execute() throws MojoExecutionException
    {
        execute(false);
    }

    private List<String> readPomAttribute(String pomFileLoc, String tag)
    {
        List<String> attributes=new ArrayList<String>();

        try 
        {
            File file = new File(pomFileLoc);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeLst = doc.getElementsByTagName(tag);

            for (int s = 0; s < nodeLst.getLength(); s++) 
            {
                Node currentNode = nodeLst.item(s);

                NodeList childrenList = currentNode.getChildNodes();

                for (int i = 0; i < childrenList.getLength(); i++)
                {
                    Node child = childrenList.item(i);
                    attributes.add(child.getNodeValue());
                }
            }
        } 
        catch (Exception e) 
        {
            System.out.println("[ERROR] readPomAttribute encountered error, there is a problem with the parsing of the pomfile");
        }

        return attributes;
    }

    private String getContents(File aFile) 
    {
        StringBuilder contents = new StringBuilder();

        try 
        {
          BufferedReader input =  new BufferedReader(new FileReader(aFile));
          try 
          {
            String line = null; //not declared within while loop

            while (( line = input.readLine()) != null)
            {
              contents.append(line);
              contents.append(System.getProperty("line.separator"));
            }
          }
          finally 
          {
            input.close();
          }
        }
        catch (Exception ex)
        {
          System.out.println("[ERROR] Could not get the contents of the given file: "+aFile);
        }

        return contents.toString();
    }

    private void setContents(File aFile, String aContents) throws Exception
    {
        if (aFile == null) 
        {
          throw new Exception("File should not be null.");
        }
        if (!aFile.exists()) {
          throw new Exception ("File does not exist: " + aFile);
        }
        if (!aFile.isFile()) {
          throw new Exception("Should not be a directory: " + aFile);
        }
        if (!aFile.canWrite()) {
          throw new Exception("File cannot be written: " + aFile);
        }

        //use buffering
        Writer output = new BufferedWriter(new FileWriter(aFile));
        try 
        {
          //FileWriter always assumes default encoding is OK!
          output.write( aContents );
        }
        finally {
          output.close();
        }
    }

    private void updateProjectVersion(String assemblyInfoFile, String ver, String dirtyVersion)
    {
        try
        {
            //returns if assemblyInfoFile does not exist
            if(!FileUtils.fileExists(assemblyInfoFile))
            {
                return;
			}
			String contents = getContents(new File(assemblyInfoFile));
			
			//add build number from Hudson (if any)
			String buildNumber = System.getenv("BUILD_NUMBER");// just for Hudson
			String fullVer = buildNumber != null && buildNumber.length() != 0 ? ver + "." + buildNumber : ver;
			
			//check for assembly version
			Boolean atrIsCSharp = assemblyInfoFile.indexOf(".cs") != -1;
			Pattern atrPattern = Pattern.compile("AssemblyVersion\\(.*\\)");
			Matcher m = atrPattern.matcher(contents);
			if(m.find()) //we have some record already, so change it
			{
				m.reset();
				contents = m.replaceAll("AssemblyVersion(\""+fullVer+"\")");
			}
			else //add new
			{
				if (atrIsCSharp)
					contents += "[assembly: AssemblyVersion(\""+fullVer+"\")]";
				else
					contents += "<Assembly: AssemblyVersion(\""+fullVer+"\")>";
			}
			
			
			//check for assembly file version
			atrPattern = Pattern.compile("AssemblyFileVersion\\(.*\\)");
			m = atrPattern.matcher(contents);
			if(m.find()) //we have some record already, so change it
			{
				m.reset();
				contents = m.replaceAll("AssemblyFileVersion(\""+fullVer+"\")");
			}
			else //add new
			{
				if (atrIsCSharp)
					contents += "[assembly: AssemblyFileVersion(\""+fullVer+"\")]";
				else
					contents += "<Assembly: AssemblyFileVersion(\""+fullVer+"\")>";
			}
			
			//check for assembly informational version
			atrPattern = Pattern.compile("AssemblyInformationalVersion\\(.*\\)");
			m = atrPattern.matcher(contents);
			if(m.find()) //we have some record already, so change it
			{
				m.reset();
				contents = m.replaceAll("AssemblyInformationalVersion(\""+dirtyVersion+"\")");
			}
			else //add new
			{
				if (atrIsCSharp)
					contents += "[assembly: AssemblyInformationalVersion(\""+dirtyVersion+"\")]";
				else
					contents += "<Assembly: AssemblyInformationalVersion(\""+dirtyVersion+"\")>";
			}
			/*String checkVersion = "AssemblyFileVersion(\""+ver;
			//modify AssemblyFileInfo if version is different in the pom.
			if(contents.lastIndexOf(checkVersion)==-1)
			{
				try
				{
					contents = contents.substring(0,contents.indexOf("[assembly: AssemblyFileVersion(")) 
					+"[assembly: AssemblyFileVersion(\""+ver+"\")]";
					setContents(new File(assemblyInfoFile),contents);
				}
				// thrown exception if the project type is vb
				catch(Exception e)
				{
					if(contents.lastIndexOf(checkVersion)==-1)
					{
						contents = contents.substring(0,contents.indexOf("<Assembly: AssemblyFileVersion(")) 
						+"<Assembly: AssemblyFileVersion(\""+ver+"\")>";
						setContents(new File(assemblyInfoFile),contents);
					}
				}
			}*/
			
			setContents(new File(assemblyInfoFile),contents);
		}
		catch(Exception e)
		{
		//setContents(new File(assemblyInfoFile),contents);
			System.out.println("[Error] Problem with updating Project File Version");
			e.printStackTrace();
		}
	}
	
	private boolean hasArtifactVersion(File aFile) 
	{
		StringBuilder contents = new StringBuilder();
		boolean hasVersion = false;
		int verCtr=0;
		int buildFlag=0;
		try 
		{
		  BufferedReader input =  new BufferedReader(new FileReader(aFile));
		  try 
		  {
			String line = null; //not declared within while loop
			
			while (( line = input.readLine()) != null)
			{
			  if(line.indexOf("<version>")!=-1)
			  {
				verCtr++;
			  }
			  if(line.indexOf("<build>")!=-1)
			  {
				break;
			  }
			}
			
			if(verCtr==2)
			{
				hasVersion = true;
			}
		  }
		  finally 
		  {
			input.close();
		  }
		}
		catch (Exception ex)
		{
		  
		}
    
		return hasVersion;
	}
	
	private String filterVersion(String version)
	{
		StringBuffer newVersion = new StringBuffer();
		char[] ver = version.toCharArray();
		for(char c : ver)
		{
			if(c!= '.' && !new Character(c).isDigit(c))
			{
				break;
			}
			else
			{
				newVersion.append(c);
			}
		}
		return newVersion.toString();
	}
    
	private void updateAssemblyInfoVersion()
	{
		try
		{
			String currentWorkingDir = System.getProperty("user.dir");
			String ver = "";
			List<String> modules = readPomAttribute(currentWorkingDir+File.separator+"pom.xml","module");
			
			
			//child pom or flat single module without parent tags
			if(modules.size()==0)
			{	
				//get versions in specific project path if executed with CURRENT project option
				List<String> versions = readPomAttribute(currentWorkingDir+File.separator+"pom.xml","version");
				ver = versions.get(0);
				String durtyVer = ver;
				
				//added checking for flat single modules with parent pom
				List<String> parent = readPomAttribute(currentWorkingDir+File.separator+"pom.xml","parent");
				List<String> files = FileUtils.getFiles(new File(currentWorkingDir),"*.csproj,*.sln,*.vbproj","",false);
								
				if(hasArtifactVersion(new File(currentWorkingDir+File.separator+"pom.xml")))
				{
					ver = versions.get(1);
					durtyVer = ver;
					ver = filterVersion(ver);
				}
				else
				{
					ver = filterVersion(ver);
				}
					
				String assemblyInfoFile = currentWorkingDir+File.separator+"Properties"+File.separator+"AssemblyInfo.cs";
				
				if(!FileUtils.fileExists(assemblyInfoFile))
				{
					assemblyInfoFile = currentWorkingDir+File.separator+"My Project"+File.separator+"AssemblyInfo.vb";
				}
				updateProjectVersion(assemblyInfoFile,ver, durtyVer);				
			}
			//parent pom
			else
			{
				if(!modules.isEmpty())
				{
					//check if there is a matching dependency with the namespace
					for(String child : modules)
					{
						String childDir = currentWorkingDir+File.separator+child;
						
						//get versions in parent path if executed with ALL project option
						List<String> versions = readPomAttribute(childDir+File.separator+"pom.xml","version");
						ver = versions.get(0);
						String durtyVer = ver;
				
						//checking if artifact has its own version
						if(hasArtifactVersion(new File(childDir+File.separator+"pom.xml")))
						{
							ver = versions.get(1);
							durtyVer = ver;
							ver = filterVersion(ver);
						}
						else
						{
							ver = filterVersion(ver);
						}
							
						try
						{
							String assemblyInfoFile = childDir+File.separator+"Properties"+File.separator+"AssemblyInfo.cs";
							
							if(!FileUtils.fileExists(assemblyInfoFile))
							{
								assemblyInfoFile = childDir+File.separator+"My Project"+File.separator+"AssemblyInfo.vb";
							}
							updateProjectVersion(assemblyInfoFile,ver, durtyVer);		
						}
						catch(Exception e)
						{
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("[ERROR]UpdateAssemblyInfo -- Encountered a Problem during updateAssemblyInfoVersion");
		}
		
		
	}
	
    protected void execute(boolean test)
            throws MojoExecutionException
    {
        long startTime = System.currentTimeMillis();

		//Modifies the AssemblyInfo.cs files to match the version of the pom
		try
		{
			updateAssemblyInfoVersion();
		}
		catch(Exception e)
		{
		
		}
		
        populateSettingsRepository();
        
		if (localRepository == null)
        {
            
			localRepository = new File(System.getProperty("user.home"), ".m2/repository");
        }
       
        initializeDefaults();
  
        try
        {
            CompilerExecutable compilerExecutable = netExecutableFactory.getCompilerExecutableFor(getCompilerRequirement(),
                    getCompilerConfig(),
                    project,
                    profileAssemblyPath);
            if (!test)
            {

                Boolean sourceFilesUpToDate = (Boolean) super.getPluginContext().get("SOURCE_FILES_UP_TO_DATE");
                if (((sourceFilesUpToDate == null) || sourceFilesUpToDate) &&
                        System.getProperty("forceCompile") == null && compilerExecutable.getCompiledArtifact() != null &&
                        compilerExecutable.getCompiledArtifact().exists())
                {
                    if (isUpToDateWithPomAndSettingsAndDependencies(compilerExecutable.getCompiledArtifact()))
                    {
                        getLog().info("NPANDAY-900-003: Nothing to compile - all classes are up-to-date");
                        project.getArtifact().setFile(compilerExecutable.getCompiledArtifact());
                        return;
                    }
                }
            }


//            FileUtils.mkdir("target");
            FileUtils.mkdir(project.getBuild().getDirectory());


            long startTimeCompile = System.currentTimeMillis();
            compilerExecutable.execute();
            long endTimeCompile = System.currentTimeMillis();

            getLog().info("NPANDAY-900-004: Compile Time = " + (endTimeCompile - startTimeCompile) + " ms");


            if (!test)
            {
                project.getArtifact().setFile(compilerExecutable.getCompiledArtifact());
            }

        }
        catch (PlatformUnsupportedException e)
        {
            throw new MojoExecutionException("NPANDAY-900-005: Unsupported Platform: Language = " + language +
                    ", Vendor = " + vendor + ", ArtifactType = " + project.getArtifact().getType(), e);
        }
        catch (ExecutionException e)
        {
            throw new MojoExecutionException("NPANDAY-900-006: Unable to Compile: Language = " + language +
                    ", Vendor = " + vendor + ", ArtifactType = " + project.getArtifact().getType() + ", Source Directory = " +
                    project.getBuild().getSourceDirectory(), e);
        }
        long endTime = System.currentTimeMillis();
        getLog().info("Mojo Execution Time = " + (endTime - startTime));
    }


    protected abstract void initializeDefaults() throws MojoExecutionException;

    protected abstract ArrayList<String> getParameters();

    protected abstract CompilerRequirement getCompilerRequirement() throws MojoExecutionException;

    protected abstract CompilerConfig getCompilerConfig() throws MojoExecutionException;


    protected String listToCommaDelimitedString(String[] list)
    {
        StringBuffer sb = new StringBuffer();
        boolean flag = false;

        if (list == null || list.length == 0) return "";

        for (String item : list)
        {
            sb.append(flag == true ? "," : "").append(item.trim());

            if (!flag)
            {
                flag = true;
            }
        }
        return sb.toString();

    }


    protected boolean isUpToDateWithPomAndSettingsAndDependencies(File targetFile)
    {
        File settingsFile = new File( settingsPath, "npanday-settings.xml" );
        Artifact latestDependencyModification =
                this.getLatestDependencyModification(project.getDependencyArtifacts());

        //TODO: Different parameters from the command line should also cause an update
        //TODO: Change in resource should cause an update
        if (targetFile.lastModified() < pomFile.lastModified())
        {
            getLog().info("NPANDAY-900-007: Project pom has changed. Forcing a recompile.");
            return false;
        }
        else if (settingsFile.exists() && targetFile.lastModified() < settingsFile.lastModified())
        {
            getLog().info("NPANDAY-900-008:Project settings has changed. Forcing a recompile.");
            return false;
        }
        else if (latestDependencyModification != null &&
                targetFile.lastModified() < latestDependencyModification.getFile().lastModified())
        {
            getLog().info(
                    "NPANDAY-900-009: Detected change in module dependency. Forcing a recompile: Changed Artifact = " +
                            latestDependencyModification);
            return false;
        }
        return true;
    }


    protected Artifact getLatestDependencyModification(Set<Artifact> artifacts)
    {
        if (artifacts == null)
        {
            return null;
        }
        Artifact lastModArtifact = null;
        for (Artifact artifact : artifacts)
        {
            if (lastModArtifact == null && !ArtifactTypeHelper.isDotnetAnyGac( artifact.getType() ))
            {
                lastModArtifact = artifact;
            }
            else if (!ArtifactTypeHelper.isDotnetAnyGac( artifact.getType() ) &&
                    artifact.getFile().lastModified() > lastModArtifact.getFile().lastModified())
            {
                lastModArtifact = artifact;
            }
        }
        return lastModArtifact;
    }


    protected void populateSettingsRepository()
    {
        File settingsFile = new File( settingsPath, "npanday-settings.xml" );
        
        if (!settingsFile.exists())
        {
            return;
        }

        try
        {
            SettingsRepository settingsRepository = ( SettingsRepository) repositoryRegistry.find( "npanday-settings" );

            if ( settingsRepository != null )
            {
                repositoryRegistry.removeRepository( "npanday-settings" );
            }
            try
            {
                StandardRepositoryLoader repoLoader = new StandardRepositoryLoader();
                repoLoader.setRepositoryRegistry( repositoryRegistry );
                settingsRepository = (SettingsRepository) repoLoader.loadRepository( settingsFile.getAbsolutePath(), SettingsRepository.class.getName(), new Hashtable() );
                repositoryRegistry.addRepository( "npanday-settings", settingsRepository );
            }
            catch ( IOException e )
            {
                getLog().error( e.getMessage(), e );
            }
        }
        catch ( Exception ex )
        {
            getLog().error( ex.getMessage(), ex );
        }
    }


}
