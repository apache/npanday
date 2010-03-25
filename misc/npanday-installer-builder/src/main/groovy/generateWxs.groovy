v = project.version
index = v.indexOf( '-' )
def productVersion = v
if ( index >= 0 ) {
  productVersion = v.substring( 0, index )
}

def repositoryComponentIds = []
def repositoryBasedir = new File(project.build.directory + "/repository/releases");

outputFile = new File(project.build.directory, 'npanday.wxs')
outputFile.getParentFile().mkdirs()
def writer = outputFile.withWriter('UTF-8') { writer ->
  def builder = new groovy.xml.MarkupBuilder( writer )
  builder.Wix (xmlns:"http://schemas.microsoft.com/wix/2006/wi") {
    Product( Id:"{9BB7FC88-853C-406E-92C0-A617ACD3E3B1}", Codepage:"1252", Language:"1033", Manufacturer:project.organization.name,
             Name:"NPanday " + v, UpgradeCode:"{A9239AE6-C0D5-41A2-A779-F427B2A32F3E}", Version:productVersion) {
      Package(Id:"*", InstallerVersion:"200", Compressed:"yes", Description:project.description, Manufacturer:project.organization.name)
      Media(Id:"1", Cabinet:"NPanday.cab", EmbedCab:"yes")

      // TODO: make configurable in installer?
      SetDirectory(Id:"REPOSITORYDIR",Value:"\$(env.UserProfile)\\.m2\\repository")

      Directory(Id:"TARGETDIR", Name:"SourceDir") {
        Directory(Id:'ProgramFilesFolder', Name:'PFiles') {
          Directory(Id:"NPandayDir", Name:"NPanday") {
          }
        }
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
                Component(Id:componentId,Guid:"{"+java.util.UUID.randomUUID().toString().toUpperCase() + "}") {
                  File(Name:f.name, DiskId:"1", Source:f.absolutePath)
                }
              }
            }
          }
          traverse(repositoryBasedir, "__dir")
        }
      }
      Feature(Id:"NPandayRepository", Title:"NPanday Repository Content", Level:"1") {
        for ( id in repositoryComponentIds ) {
          ComponentRef(Id:id)
        }
      }
    }
  }
}

