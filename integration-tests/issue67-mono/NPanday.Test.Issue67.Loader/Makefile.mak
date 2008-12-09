ASSEMBLY_NAME=NMaven.Test.Issue67.Loader

install : 
!IF "$(vendor)" == "MONO"	
	$(NMAVEN_MONO)\gmcs /target:exe /out:$(ASSEMBLY_NAME).exe	\
		/reference:..\NMaven.Test.Issue67.Domain\NMaven.Test.Issue67.Domain.dll	\
		/debug *.cs
!ELSE
	$(NMAVEN_FRAMEWORK)\csc /target:exe /out:$(ASSEMBLY_NAME).exe	\
		/reference:..\NMaven.Test.Issue67.Domain\NMaven.Test.Issue67.Domain.dll	\
		/debug *.cs
!ENDIF

clean :
	del $(ASSEMBLY_NAME).*
		