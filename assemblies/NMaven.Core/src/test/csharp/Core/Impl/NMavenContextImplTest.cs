using NUnit.Framework;
using System;
using System.IO;
using NMaven.Core.Impl;
using NMaven.Model;
using Microsoft.Build.BuildEngine;
using System.Collections;
using System.Collections.Generic;

namespace NMaven.Core.Impl
{
 [TestFixture]
 public class TestNMavenContextImplTest
 {
 [Test]
	 public void TestMethod()
	 {
	 	IProjectGenerator projectGenerator = new ProjectGeneratorImpl();
	 	
	 	NMaven.Model.Model model = projectGenerator.CreatePomModelFor(@"..\..\..\NMaven.Plugin.Solution\pom.xml");
	 	Console.WriteLine(model.artifactId);
	 	
	 	IProjectReference projectReference = 
	 		projectGenerator.GenerateProjectFor(model,
	 	                                    new DirectoryInfo(@"C:\Documents and Settings\shane\nmaven-apache\SI_IDE\assemblies\NMaven.Plugin.Solution\src\main\csharp\"), 
	 	                                    model.artifactId + "T1", null);
	 	List<IProjectReference> projectReferences = new List<IProjectReference>();
	 	projectReferences.Add(projectReference);
	    
	 	IProjectReference testReference = projectGenerator.GenerateProjectFor(model,
	 	                                    new DirectoryInfo(@"C:\Documents and Settings\shane\nmaven-apache\SI_IDE\assemblies\NMaven.Core\src\test\csharp\"), 
	 	                                    model.artifactId + "-Test1", projectReferences);
	 	projectReferences.Add(testReference);
	 	
	 	
	 	projectGenerator.GenerateSolutionFor(new FileInfo(@"..\..\..\..\test.sln"), projectReferences);
	
	}
}
}
