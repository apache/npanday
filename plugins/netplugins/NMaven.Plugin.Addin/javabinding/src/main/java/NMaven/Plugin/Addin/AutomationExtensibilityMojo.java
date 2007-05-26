package NMaven.Plugin.Addin;

import org.apache.maven.dotnet.plugin.FieldAnnotation;

/**
 * @phase package
 * @goal package
 */
public class AutomationExtensibilityMojo
    extends org.apache.maven.dotnet.plugin.AbstractMojo
{
       /**
        * @parameter expression = "${settings.localRepository}"
        */
        @FieldAnnotation()
        public java.lang.String repository;

       /**
        * @parameter expression = "${project}"
        */
        @FieldAnnotation()
        public org.apache.maven.project.MavenProject mavenProject;

       /**
        * @parameter expression = "${project}"
        */
        private org.apache.maven.project.MavenProject project;

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
            return "NMaven.Plugin.Addin";
        }

        public String getMojoGroupId()
        {
            return "NMaven.Plugins";
        }

        public String getClassName()
        {
            return "NMaven.Plugin.Addin.AutomationExtensibilityMojo";
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
            return project;
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
