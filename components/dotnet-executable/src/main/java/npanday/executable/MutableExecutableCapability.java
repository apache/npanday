package npanday.executable;

import npanday.vendor.VendorInfo;

import java.util.List;

/**
 * Holds the configured executable capability.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
// TODO: Refactor to be based on the configured plugins
public class MutableExecutableCapability
    implements ExecutableCapability
{
    protected VendorInfo vendorInfo;

    protected String operatingSystem;

    private String architecture;

    protected String pluginClassName;

    private String executable;

    protected String identifier;

    private CommandCapability commandCapability;

    private List<String> frameworkVersions;

    private String profile;

    private String netDependencyId;

    private List<String> probingPaths;

    public String getProfile()
    {
        return profile;
    }

    public void setProfile( String profile )
    {
        this.profile = profile;
    }

    public List<String> getFrameworkVersions()
    {
        return frameworkVersions;
    }

    public void setFrameworkVersions( List<String> frameworkVersions )
    {
        this.frameworkVersions = frameworkVersions;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier( String identifier )
    {
        this.identifier = identifier;
    }

    public String getExecutableName()
    {
        return executable;
    }

    public void setExecutableName( String executableName )
    {
        this.executable = executableName;
    }

    public VendorInfo getVendorInfo()
    {
        return vendorInfo;
    }

    public void setVendorInfo( VendorInfo vendorInfo )
    {
        this.vendorInfo = vendorInfo;
    }

    public String getOperatingSystem()
    {
        return operatingSystem;
    }

    public void setOperatingSystem( String operatingSystem )
    {
        this.operatingSystem = operatingSystem;
    }

    public String getArchitecture()
    {
        return architecture;
    }

    public void setArchitecture( String architecture )
    {
        this.architecture = architecture;
    }

    public String getPluginClassName()
    {
        return pluginClassName;
    }

    public void setPluginClassName( String pluginClassName )
    {
        this.pluginClassName = pluginClassName;
    }

    public CommandCapability getCommandCapability()
    {
        return commandCapability;
    }

    public void setCommandCapability( CommandCapability commandCapability )
    {
        this.commandCapability = commandCapability;
    }

    public String getNetDependencyId()
    {
        return netDependencyId;
    }

    public void setNetDependencyId( String executableLocation )
    {
        this.netDependencyId = executableLocation;
    }

    public List<String> getProbingPaths()
    {
        return probingPaths;
    }

    public void setProbingPaths( List<String> probingPaths )
    {
        this.probingPaths = probingPaths;
    }
}
