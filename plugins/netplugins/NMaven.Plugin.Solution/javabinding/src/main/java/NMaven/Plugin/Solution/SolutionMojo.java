package NMaven.Plugin.Solution;

import org.apache.maven.dotnet.plugin.FieldAnnotation;

/**
 * @phase Package
 * @goal Solution
 */
public class SolutionMojo
    extends org.apache.maven.dotnet.plugin.AbstractMojo
{

       /**
        * @parameter expression = "${settings.localRepository}"
        */
        @FieldAnnotation()
        public java.lang.String localRepo;

       /**
        * @parameter expression = "${basedir}"
        */
        @FieldAnnotation()
        public java.lang.String basedir;

       /**
        * @parameter expression = "${project}"
        */
        @FieldAnnotation()
        public org.apache.maven.project.MavenProject mavenProject;

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
        private org.apache.maven.dotnet.executable.NetExecutableFactory netExecutableFactory;

       /**
        * @component
        */
        private org.apache.maven.dotnet.plugin.PluginContext pluginContext;

        public String getMojoArtifactId()
        {
            return "NMaven.Plugin.Solution";
        }

        public String getMojoGroupId()
        {
            return "NMaven.Plugins";
        }

        public String getClassName()
        {
            return "NMaven.Plugin.Solution.SolutionMojo";
        }

        public org.apache.maven.dotnet.plugin.PluginContext getNetPluginContext()
        {
            return pluginContext;
        }

        public org.apache.maven.dotnet.executable.NetExecutableFactory getNetExecutableFactory()
        {
            return netExecutableFactory;
        }

        public org.apache.maven.project.MavenProject getMavenProject()
        {
            return mavenProject;
        }

        public String getLocalRepository()
        {
            return localRepository;
        }

        public String getVendorVersion()
        {
            return vendorVersion;
        }

        public String getVendor()
        {
            return vendor;
        }

        public String getFrameworkVersion()
        {
            return frameworkVersion;
        }

}
