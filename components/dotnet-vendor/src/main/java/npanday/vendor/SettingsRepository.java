package npanday.vendor;

import npanday.model.settings.DefaultSetup;
import npanday.model.settings.Vendor;
import npanday.registry.Repository;

import java.util.List;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public interface SettingsRepository
    extends Repository
{
    List<Vendor> getVendors();
    DefaultSetup getDefaultSetup();
    boolean isEmpty();

    int getContentVersion();
}
