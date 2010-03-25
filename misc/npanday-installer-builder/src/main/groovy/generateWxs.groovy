version = project.version
index = version.indexOf( '-' )
def productVersion = version
if ( index >= 0 ) {
  productVersion = version.substring( 0, index )
}

def repositoryComponentIds = []
def repositoryBasedir = new File(project.build.directory + "/repository/releases");
def generateGuid = { "{"+java.util.UUID.randomUUID().toString().toUpperCase() + "}" }

def addinArtifacts = [ [groupId:"npanday.visualstudio", artifactId:"NPanday.VisualStudio.Addin"]]
def visualStudioVersions = ['2005', '2008']

outputFile = new File(project.build.directory, "npanday.wxs")
outputFile.getParentFile().mkdirs()
def writer = outputFile.withWriter("UTF-8") { writer ->
  def builder = new groovy.xml.MarkupBuilder( writer )
  builder.Wix (xmlns:"http://schemas.microsoft.com/wix/2006/wi", 'xmlns:util':'http://schemas.microsoft.com/wix/UtilExtension') {
    Product( Id:"{9BB7FC88-853C-406E-92C0-A617ACD3E3B1}", Codepage:"1252", Language:"1033", Manufacturer:project.organization.name,
             Name:"NPanday " + version, UpgradeCode:"{A9239AE6-C0D5-41A2-A779-F427B2A32F3E}", Version:productVersion) {
      Package(Id:"*", InstallerVersion:"200", Compressed:"yes", Description:project.description, Manufacturer:project.organization.name)
      Media(Id:"1", Cabinet:"NPanday.cab", EmbedCab:"yes")

      // TODO: make configurable in installer?
      SetDirectory(Id:"REPOSITORYDIR",Value:"\$(env.UserProfile)\\.m2\\repository")

      Directory(Id:"TARGETDIR", Name:"SourceDir") {
        Directory(Id:"REPOSITORYDIR",Name:"REPOSITORYDIR") {
          traverse = { dir, id ->
            dir.eachFile { f ->
              if ( f.isDirectory() ) {
                nextId = id + "_" + f.name.replace('-','')
                Directory(Id:nextId, Name:f.name) {
                  traverse( f, nextId )
                }
              }
              else if ( ! ( f.name =~ /maven-metadata(-central)?.xml*/ ) ) {
                def componentId = "repository_" + f.name.replace('-', '_')
                repositoryComponentIds << componentId
                Component(Id:componentId,Guid:generateGuid()) {
                  File(Name:f.name, DiskId:"1", Source:f.absolutePath)
                }
              }
            }
          }
          traverse(repositoryBasedir, "__dir")
        }
        Directory(Id:"ProgramFilesFolder", Name:"PFiles") {
          Directory(Id:"NPandayDir", Name:"NPanday") {
            Directory(Id:"BinDir", Name:"bin") {
              addinArtifacts.each { artifact ->
                Component(Id:"${artifact.artifactId}.dll", Guid:generateGuid()) {
                  def groupPath = artifact.groupId.replace('.','/')
                  // use a pattern as we might have a timestamped snapshot to match
                  def p = ~/${artifact.artifactId}-.*\.dll$/
                  def dir = new File(repositoryBasedir, "${groupPath}/${artifact.artifactId}/${version}")
                  dir.eachFileMatch(p) { file ->
                    File(Name:"${artifact.artifactId}.dll", DiskId:"1", Source:file.absolutePath)
                  }
                }
              }
            }
          }
        }
        Directory(Id:"PersonalFolder", Name:"MyDocuments") {
          visualStudioVersions.each { vs ->
            // TODO: make conditional on VS installed
            //      would check HKCR\VisualStudio.DTE.8.0, HKCR\VisualStudio.DTE.9.0 in the registry respectively
            Directory(Id:"VS${vs}Folder", Name:"Visual Studio ${vs}") {
              Directory(Id:"VS${vs}Addin", Name:"Addins") {
                Component(Id:"VS${vs}AddinDescriptor", Guid:generateGuid()) {
                  RemoveFolder(Id:"remove_VS${vs}Addin",On:"uninstall")
                  RemoveFolder(Id:"remove_VS${vs}Folder",On:"uninstall",Directory:"VS${vs}Folder")
                  RegistryKey( Root:"HKCU", Key:"Software\\NPanday\\VS${vs}AddinDescriptor") {
                    RegistryValue( KeyPath: "yes", Type:"string", Value:"" )
                  }
                  File(Id:"VS${vs}_file", Name:"NPanday.VisualStudio.Addin", DiskId:"1",
                       Source:"${project.basedir}/src/main/wix/NPanday.VisualStudio.Addin")
                  'util:XmlFile'(Id:"VS${vs}XmlModifyAssembly", Action:"setValue",
                                 ElementPath:"/Extensibility/Addin/Assembly",
                                 File:"[VS${vs}Addin]\\NPanday.VisualStudio.Addin",
                                 Value:"[BinDir]NPanday.VisualStudio.Addin.dll")
                  'util:XmlFile'(Id:"VS${vs}XmlModifyDescription", Action:"setValue",
                                 ElementPath:"/Extensibility/Addin/Description",
                                 File:"[VS${vs}Addin]\\NPanday.VisualStudio.Addin",
                                 Value:"${project.description}")
                }
              }
            }
          }
        }
      }
      Feature(Id:"NPandayRepository", Title:"NPanday Repository Content", Level:"1") {
        for ( id in repositoryComponentIds ) {
          ComponentRef(Id:id)
        }
      }
      Feature(Id:"NPandayAddIn", Title:"NPanday Visual Studio Add-in", Level: "1") {
        addinArtifacts.each { artifact ->
          ComponentRef(Id:"${artifact.artifactId}.dll")
        }
        visualStudioVersions.each { vs ->
          ComponentRef(Id:"VS${vs}AddinDescriptor")
        }
      }
    }
  }
}

