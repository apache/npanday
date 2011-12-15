package npanday.plugin.aspnet;

import org.apache.maven.plugin.assembly.AssemblerConfigurationSource;
import org.apache.maven.plugin.assembly.InvalidAssemblerConfigurationException;
import org.apache.maven.plugin.assembly.interpolation.AssemblyInterpolator;
import org.apache.maven.plugin.assembly.io.AssemblyReadException;
import org.apache.maven.plugin.assembly.io.DefaultAssemblyReader;
import org.apache.maven.plugin.assembly.model.Assembly;

import java.io.File;
import java.io.Reader;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class MixinAsssemblyReader extends DefaultAssemblyReader
{
    private String[] componentDescriptors;

    /**
     * Add the contents of all included components to main assembly
     *
     * @throws org.apache.maven.plugin.assembly.io.AssemblyReadException
     *
     * @throws org.apache.maven.plugin.MojoFailureException
     *
     * @throws org.apache.maven.plugin.MojoExecutionException
     *
     */
    @Override
    protected void mergeComponentsWithMainAssembly( Assembly assembly, File assemblyDir,
                                                    AssemblerConfigurationSource configSource )
        throws AssemblyReadException
    {
        appendComponentDescriptors( assembly );

        super.mergeComponentsWithMainAssembly( assembly, assemblyDir,
                                               configSource );    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Add component descriptors from other sources.
     */
    protected void appendComponentDescriptors( Assembly assembly )
    {
        for (String componentDescriptor : componentDescriptors){
            getLogger().debug( "NPANDAY-110-001: Mixing in component descriptor '" + componentDescriptor + "' to assembly with id '" + assembly.getId() + "'.");
            assembly.addComponentDescriptor( componentDescriptor );
        }
    }

    public void setComponentDescriptors( String[] componentDescriptors )
    {
        this.componentDescriptors = componentDescriptors;
    }
}
