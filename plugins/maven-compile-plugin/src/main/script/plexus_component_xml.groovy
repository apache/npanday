import npanday.plugin.compile.CompileLifecycleMap
import npanday.lifecycle.LifecycleConfigurationGenerator

def plexus = new File(project.build.outputDirectory, 'META-INF/plexus')
plexus.mkdirs()

def componentsXmlFile = new File(plexus, 'components.xml');

LifecycleConfigurationGenerator
		.persistAllTypesAndLifecycles(CompileLifecycleMap, componentsXmlFile)