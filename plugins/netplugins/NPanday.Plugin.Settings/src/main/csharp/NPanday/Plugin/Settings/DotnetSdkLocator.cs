using Microsoft.Win32;

namespace NPanday.Plugin.Settings
{
    public class DotnetSdkLocator
    {
        RegistryKey Microsoft_NETFramework = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\.NETFramework");
        RegistryKey Microsoft_SDKs_NETFramework = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\SDKs\.NETFramework");
        RegistryKey Microsoft_SDKs_Windows_70 = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\Microsoft SDKs\Windows\v7.0");
        RegistryKey Microsoft_SDKs_Windows_70a = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\Microsoft SDKs\Windows\v7.0A");

        public string Find1_1()
        {
            return (string)Microsoft_NETFramework.GetValue("sdkInstallRootv1.1");
        }

        public string Find2_0()
        {
            return PathUtil.FirstExisting(
                registryFind(Microsoft_NETFramework, "sdkInstallRootv2.0"),
                registryFind(Microsoft_SDKs_NETFramework, "v2.0", "InstallationFolder")
                );
        }

        public string Find3_5()
        {
            return PathUtil.FirstExisting(
                registryFind(Microsoft_NETFramework, "sdkInstallRootv3.5"),
                // prefer 32 bit until its made explicit
                registryFind(Microsoft_SDKs_Windows_70, "WinSDKNetFx35Tools-x86", "InstallationFolder"),
                registryFind(Microsoft_SDKs_Windows_70a, "WinSDK-NetFx35Tools-x86", "InstallationFolder"),
                registryFind(Microsoft_SDKs_Windows_70, "WinSDKNetFx35Tools", "InstallationFolder"),
                registryFind(Microsoft_SDKs_Windows_70a, "WinSDK-NetFx35Tools", "InstallationFolder"),
                registryFind(Microsoft_SDKs_Windows_70, "WinSDKNetFx35Tools-x64", "InstallationFolder"),
                registryFind(Microsoft_SDKs_Windows_70a, "WinSDK-NetFx35Tools-x64", "InstallationFolder")
            );
        }

        public string Find4_0()
        {
            return PathUtil.FirstExisting(
                registryFind(Microsoft_NETFramework, "sdkInstallRootv4.0"), // does not exist, I thinkg (lcorneliussen)
                // prefer 32 bit until its made explicit
                registryFind(Microsoft_SDKs_Windows_70a, "WinSDK-NetFx40Tools", "InstallationFolder"),
                registryFind(Microsoft_SDKs_Windows_70a, "WinSDK-NetFx40Tools-x86", "InstallationFolder"),
                registryFind(Microsoft_SDKs_Windows_70a, "WinSDK-NetFx40Tools-x64", "InstallationFolder")
            );
        }

        private string registryFind(RegistryKey root, string valueKey)
        {
            return registryFind(root, null, valueKey);
        }

        private string registryFind(RegistryKey root, string subkey, string valueKey)
        {
            RegistryKey current = root;

            if (current == null)
                return null;

            if (subkey != null)
            {
                current = current.OpenSubKey(subkey);
                if (current == null)
                    return null;
            }

            return (string)current.GetValue(valueKey);
        }
    }
}
