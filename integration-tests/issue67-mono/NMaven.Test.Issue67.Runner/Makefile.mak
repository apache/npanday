ASSEMBLY_NAME=NMaven.Test.Issue67.Runner

install : 
!IF "$(vendor)" == "MONO"	
	$(NMAVEN_MONO)\gmcs /target:exe /out:$(ASSEMBLY_NAME).exe	\
		/debug *.cs
!ELSE
	$(NMAVEN_FRAMEWORK)\csc /target:exe /out:$(ASSEMBLY_NAME).exe	\
		/debug *.cs
!ENDIF

clean :
	del $(ASSEMBLY_NAME).*

test :
	$(ASSEMBLY_NAME).exe 	\
		startProcessAssembly=..\NMaven.Test.Issue67.Loader\NMaven.Test.Issue67.Loader.exe	\
		assemblyFile=..\NMaven.Test.Issue67.Application\NMaven.Test.Issue67.Application.dll	\
		vendor=$(vendor)