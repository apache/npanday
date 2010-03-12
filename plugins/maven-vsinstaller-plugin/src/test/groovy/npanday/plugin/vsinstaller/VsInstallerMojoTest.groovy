package npanday.plugin.vsinstaller

import javax.swing.filechooser.FileSystemView;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VsInstallerMojoTest{
	String oldUserHome
	File tempDir
	File myDocumentsDir
	VsInstallerMojo mojo
	
	@Before
	final void setup(){
		oldUserHome = System.properties["user.home"]
		tempDir = new File(System.properties["java.io.tmpdir"], this.class.name)
		myDocumentsDir = new File(tempDir, "My Documents")
		
		tempDir.mkdirs()
		System.properties["user.home"] = tempDir.getAbsolutePath()
		mojo = new VsInstallerMojo()
		
		def fw = [getDefaultDirectory:{myDocumentsDir}] as FileSystemView	
		mojo.filesystemView = fw
	}
	
	@After
	final void teardown(){
		tempDir.deleteDir()
		mojo = null
	}
	
	@Test
	final void testCollectDefaultVSAddinDirectoriesWithMissingAddinDirectory(){
		mojo.collectDefaultVSAddinDirectories()
		List results = mojo.vsAddinDirectories
		
		Assert.assertEquals(0, results.size());
	}
	
	@Test
	final void testcollectDefaultVSAddinDirectories(){
		new File(myDocumentsDir, "Visual Studio 2005").mkdirs()
		new File(myDocumentsDir, "Visual Studio 2008").mkdirs()
		
		mojo.collectDefaultVSAddinDirectories()
		List results = mojo.vsAddinDirectories
		
		Assert.assertEquals(2, results.size());
	}
	
	@Test
	final void testCollectDefaultVSHomeDirectoriesWithAdditionalPaths(){
		mojo.vsAddinDirectories.add new File(tempDir, "Addins")
		mojo.collectDefaultVSAddinDirectories()
		List results = mojo.vsAddinDirectories
		
		Assert.assertEquals(1, results.size());
	}
	
	@Test
	final void testCollectDefaultVSHomeDirectoriesWithDifferentLocations(){
		File germanMyDocumentsDir = new File(tempDir, "Eigene Dateien")
		def fw = [getDefaultDirectory:{germanMyDocumentsDir}] as FileSystemView	
		mojo.filesystemView = fw
		
		new File(germanMyDocumentsDir, "Visual Studio 2008").mkdirs()
		
		mojo.collectDefaultVSAddinDirectories()
		List results = mojo.vsAddinDirectories
		
		Assert.assertEquals(1, results.size());
	}
	
	@Test
	final void testWritePlugin(){
		mojo.vsAddinDirectories.add new File(tempDir, "Addins")
		mojo.collectDefaultVSAddinDirectories()
		List results = mojo.vsAddinDirectories
		
		mojo.localRepository = tempDir.getPath()+"/m2/repo"
		
		results.each{ mojo.writePlugin it  }
		
		Assert.assertTrue new File(tempDir, "Addins/NPanday.VisualStudio.AddIn").exists()
	}
	
	@Test
	final void testWritePluginIn2008GermanDir(){
		File germanMyDocumentsDir = new File(tempDir, "Eigene Dateien")
		def fw = [getDefaultDirectory:{germanMyDocumentsDir}] as FileSystemView	
		mojo.filesystemView = fw
		
		def v2008 = new File(germanMyDocumentsDir, "Visual Studio 2008")
		v2008.mkdirs()
		
		mojo.collectDefaultVSAddinDirectories()
		List results = mojo.vsAddinDirectories
		
		mojo.localRepository = tempDir.getPath()+"/m2/repo"
		
		results.each{ mojo.writePlugin it }
		
		Assert.assertTrue new File(v2008, "Addins/NPanday.VisualStudio.AddIn").exists()
	}
}
