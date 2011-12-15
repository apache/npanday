package npanday.registry;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public interface WindowsRegistryAccessProvider
{
   /**
     * Tries to get a registry value from the Windows registry.
     * @param registryHKey
     * @param key
     * @param valueName
     * @return The value of the key or null.
     */
    String getValue( RegistryHKey registryHKey, String key, String valueName ) throws
        WindowsRegistryAccessException;

    public static enum RegistryHKey
    {
        HKLM( "HKEY_LOCAL_MACHINE", 0x80000001 ),
        HKCU( "HKEY_CURRENT_USER",0x80000002 );

        private String longName;
        private int hkey;

        RegistryHKey( String longName, int hkey )
        {
            this.longName = longName;
            this.hkey = hkey;
        }

        public String getLongName()
        {
            return longName;
        }

        public int getHKey()
        {
            return hkey;
        }

        public static RegistryHKey tryGetFromName(String name){
            checkArgument( name != null, "Name must not be null");

            if (name.equals("HKLM") || name.equals(HKLM.getLongName()))
                return HKLM;
            if (name.equals("HKCU") || name.equals(HKCU.getLongName()))
                return HKCU;

            return null;
        }
    }
}

