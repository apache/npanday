package npanday.registry.impl;

import hidden.org.codehaus.plexus.interpolation.AbstractValueSource;
import npanday.registry.WindowsRegistryAccessException;
import npanday.registry.WindowsRegistryAccessProvider;
import org.codehaus.plexus.interpolation.ValueSource;

/**
 * Tries to find registry settings for all expressions starting with HKLM or HKCU.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class WindowsRegistryValueSource
    extends AbstractValueSource
    implements ValueSource
{
   private WindowsRegistryAccessProvider registry;

    public WindowsRegistryValueSource( WindowsRegistryAccessProvider registry )
    {
        super( true );
        this.registry = registry;
    }

    public Object getValue( String expression )
    {
        int indexOfBackslash = expression.indexOf( "\\" );
        if (indexOfBackslash == -1)
            return null;

        String hkeyExpression = expression.substring( 0, indexOfBackslash );

        final WindowsRegistryAccessProvider.RegistryHKey registryHKey = WindowsRegistryAccessProvider.RegistryHKey
            .tryGetFromName(
                hkeyExpression
            );

        if (registryHKey == null)
            return null;

        if (expression.length() < indexOfBackslash || !expression.contains( "@" ))
            return null;

        String keyAndValueNamePart = expression.substring( indexOfBackslash +1 );
        int indexOfAt = keyAndValueNamePart.indexOf( '@' );

        String key = keyAndValueNamePart.substring( 0, indexOfAt );
        String valueName = keyAndValueNamePart.substring( indexOfAt+1 );

        try
        {
            final String value = registry.getValue( registryHKey, key, valueName );
            addFeedback( "NPANDAY-118-001: Retrieved the registry value for " + expression + ": " + value);
            return value == null ? "" : value;
        }
        catch ( WindowsRegistryAccessException e )
        {
            addFeedback( "NPANDAY-118-000: Could not retrieve the registry value for " + expression, e );
            return "";
        }
    }
}

