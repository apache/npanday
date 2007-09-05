BUG TITLE: Mono does not allow creation of a new app domain manager
DESCRIPTION: Mono does not support the use of the APPDOMAIN_MANAGER_ASM and APPDOMAIN_MANAGER_TYPE environment variables to plugin a new app domain manager.
IMPACT: This impact is: 1) developers can't write Maven plugins in .NET; and 2) NMaven plugins like the solution generator can't be executed in Mono runtime.

I. SETTING UP:

Prerequisites:
1) Windows XP
2) Microsoft SDK 2.0 Installed
3) Microsoft Visual Studio Installed
4) Mono Installed (tested with 1.2.3.1)

Set Environmental Variables for test:
NMAVEN_FRAMEWORK (Microsoft .NET Framework)
NMAVEN_SDK (Microsoft SDK 2.0)
NMAVEN_MONO (Mono bin directory)

NMAVEN_MONO also needs to be included within the path.

Samples of Environmental Variables
NMAVEN_FRAMEWORK=C:\WINDOWS\Microsoft.NET\Framework\v2.0.50727
NMAVEN_SDK="C:\Program Files\Microsoft.NET\SDK\v2.0\Bin"
NMAVEN_MONO="C:\Program Files\Mono-1.2.3.1\bin"

II. RUNNING THE TESTS:

The scripts should be built with Microsoft nmake (located within the SDK bin).

For Microsoft: goto the parent directory of the test distribution and from the commandline type:
	nmake -f Makefile.mak install
	nmake -f Makefile.mak test
At the end of the test, you should see something similar to:

-----Starting Plugin Loader-----
Assembly File = ..\NMaven.Test.Issue67.Application\NMaven.Test.Issue67.Application.dll
Loading Plugin: C:\Documents and Settings\shane\nmaven-apache\trunk\integration-tests\mono-bug\NMaven.Test.Issue67.Application
Creating Plugin Domain Manager
-----Ending Plugin Loader-----
-----Test OK-----

Now for Mono: from the commandline
	nmake -f Makefile.mak clean
	nmake -f Makefile.mak install vendor=MONO
	nmake -f Makefile.mak test vendor=MONO
 
At the end of the test, you should see something similar to:
----Starting Plugin Loader-----
Assembly File = ..\NMaven.Test.Issue67.Application\NMaven.Test.Issue67.Application.dll
Loading Plugin: C:\Documents and Settings\shane\nmaven-apache\trunk\integration-tests\mono-bug\NMaven.Test.Issue67.Application
-----Failed to find test app domain manager-----
-----Test Failed----

III. Test Details
NMaven.Test.Issue67.Runner: This class instance takes two args: 1) the vendor (MONO or Microsoft) and the startProcessAssembly, which references NMaven.Test.Issue67.Loader.exe. The vendor arg tells the Runner whether to start the Loader.exe under the Microsoft CLR or the Mono one. This class instance also sets the application manager information that the Loader.exe process will use by setting the APPDOMAIN_MANAGER_ASM and the APPDOMAIN_MANAGER_TYPE environmental variables. 

NMaven.Test.Issue67.Loader: This class attempts to get the correct instance of the AppDomainManager (TestAppDomainManager) and invoke a method on it.

The NMaven.Test.Issue67.Domain: Contains the TestAppDomainManager class.



