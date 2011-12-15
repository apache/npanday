package npanday.vendor;

import java.io.Serializable;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Specifies the requirements for a vendor to be used.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class VendorRequirement
{
    Vendor vendor;

    String vendorVersion;

    String frameworkVersion;

    public VendorRequirement( String vendorName, String vendorVersion, String frameworkVersion )
    {
        setVendor( vendorName );
        setVendorVersion( vendorVersion );
        setFrameworkVersion( frameworkVersion );
    }

    public VendorRequirement( Vendor vendor, String vendorVersion, String frameworkVersion )
    {
        setVendor( vendor );
        setVendorVersion( vendorVersion );
        setFrameworkVersion( frameworkVersion );
    }

    /**
     * @see npanday.vendor.VendorInfo#getVendor()
     */
    public Vendor getVendor()
    {
        return vendor;
    }

    /**
     * @see npanday.vendor.VendorInfo#getVendor()
     */
    public void setVendor( Vendor vendor )
    {
        this.vendor = vendor;
    }

    /**
     * @see npanday.vendor.VendorInfo#getVendor()
     */
    public void setVendor( String vendorName )
    {
        if (!isNullOrEmpty(vendorName))
        {
            setVendor( VendorFactory.createVendorFromName( vendorName ) );
        }
    }

    /**
     * @see npanday.vendor.VendorInfo#getVendorVersion()
     */
    public String getVendorVersion()
    {
        return vendorVersion;
    }

    /**
     * @see npanday.vendor.VendorInfo#getVendorVersion()
     */
    public void setVendorVersion( String vendorVersion )
    {
        if ( "".equals( vendorVersion ) )
        {
            throw new IllegalArgumentException(
                "Given frameworkVersion must be a value or null; empty is not allowed." );
        }

        this.vendorVersion = vendorVersion;
    }

    /**
     * @see npanday.vendor.VendorInfo#getFrameworkVersion()
     */
    public String getFrameworkVersion()
    {
        return frameworkVersion;
    }

    /**
     * @see npanday.vendor.VendorInfo#getFrameworkVersion()
     */
    public void setFrameworkVersion( String frameworkVersion )
    {
        if ( "".equals( frameworkVersion ) )
        {
            throw new IllegalArgumentException(
                "Given frameworkVersion must be a value or null; empty is not allowed." );
        }

        this.frameworkVersion = frameworkVersion;
    }

    /**
     * Determines, if the requirement is completed, hence, if vendor,
     * vendorVersion and frameworkVersion is filled in.
     */
    public boolean isComplete()
    {
        return vendor != null && vendorVersion != null && frameworkVersion != null;
    }

    public String toString()
    {
        return "[" + getClass().getSimpleName() + " for vendor " + visibleNullString( vendor ) + " version "
            + visibleNullString(getVendorVersion()) + ", Framework Version = "
            + visibleNullString(getFrameworkVersion()) + "]";
    }

    private Serializable visibleNullString( Object obj )
    {
        return ( obj == null ? "NULL" : obj.toString() );
    }
}
