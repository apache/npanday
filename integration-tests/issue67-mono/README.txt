BUG TITLE: Mono does not allow creation of a new app domain manager
DESCRIPTION: Mono does not support the use of the APPDOMAIN_MANAGER_ASM and APPDOMAIN_MANAGER_TYPE environment variables to plugin a new app domain manager.
IMPACT: This impact is: 1) developers can't write Maven plugins in .NET; and 2) NPanday plugins like the solution generator can't be executed in Mono runtime.

I. SETTING UP:

Prerequisites:
1) Windows XP
2) Microsoft SDK 2.0 Installed
3) Microsoft Visual Studio Installed
4) Mono Installed (tested with 1.2.3.1)

Set Environmental Variables for test:
NPANDAY_FRAMEWORK (Microsoft .NET Framework)
NPANDAY_SDK (Microsoft SDK 2.0)
NPANDAY_MONO (Mono bin directory)

NPANDAY_MONO also needs to be included within the path.

Samples of Environmental Variables
NPANDAY_FRAMEWORK=C:\WINDOWS\Microsoft.NET\Framework\v2.0.50727
NPANDAY_SDK="C:\Program Files\Microsoft.NET\SDK\v2.0\Bin"
NPANDAY_MONO="C:\Program Files\Mono-1.2.3.1\bin"

II. RUNNING THE TESTS:

The scripts should be built with Microsoft nmake (located within the SDK bin).

For Microsoft: goto the parent directory of the test distribution and from the commandline type:
	nmake -f Makefile.mak install
	nmake -f Makefile.mak test
At the end of the test, you should see something similar to:

-----Starting Plugin Loader-----
Assembly File = ..\NPanday.Test.Issue67.Application\NPanday.Test.Issue67.Application.dll
Loading Plugin: C:\Documents and Settings\shane\npanday-apache\trunk\integration-tests\mono-bug\NPanday.Test.Issue67.Application
Creating Plugin Domain Manager
-----Ending Plugin Loader-----
-----Test OK-----

Now for Mono: from the commandline
	nmake -f Makefile.mak clean
	nmake -f Makefile.mak install vendor=MONO
	nmake -f Makefile.mak test vendor=MONO
 
At the end of the test, you should see something similar to:
----Starting Plugin Loader-----
Assembly File = ..\NPanday.Test.Issue67.Application\NPanday.Test.Issue67.Application.dll
Loading Plugin: C:\Documents and Settings\shane\npanday-apache\trunk\integration-tests\mono-bug\NPanday.Test.Issue67.Application
-----Failed to find test app domain manager-----
-----Test Failed----

III. Test Details
NPanday.Test.Issue67.Runner: This class instance takes two args: 1) the vendor (MONO or Microsoft) and the startProcessAssembly, which references NPanday.Test.Issue67.Loader.exe. The vendor arg tells the Runner whether to start the Loader.exe under the Microsoft CLR or the Mono one. This class instance also sets the application manager information that the Loader.exe process will use by setting the APPDOMAIN_MANAGER_ASM and the APPDOMAIN_MANAGER_TYPE environmental variables. 

NPanday.Test.Issue67.Loader: This class attempts to get the correct instance of the AppDomainManager (TestAppDomainManager) and invoke a method on it.

The NPanday.Test.Issue67.Domain: Contains the TestAppDomainManager class.



