ASSEMBLY_NAME=NMaven.Test.Issue67.Application

install : 
!IF "$(vendor)" == "MONO"	
	$(NMAVEN_MONO)\gmcs /target:library /out:$(ASSEMBLY_NAME).dll /keyfile:sample.snk /debug *.cs
	$(NMAVEN_MONO)\gacutil /f /i $(ASSEMBLY_NAME).dll 
!ELSE
	$(NMAVEN_FRAMEWORK)\csc /target:library /out:$(ASSEMBLY_NAME).dll /keyfile:sample.snk /debug *.cs
	$(NMAVEN_SDK)\gacutil /f /i $(ASSEMBLY_NAME).dll 
!ENDIF

clean :
	del $(ASSEMBLY_NAME).*
!IF "$(vendor)" == "MONO"	
	$(NMAVEN_MONO)\gacutil /u $(ASSEMBLY_NAME) 
!ELSE
	$(NMAVEN_SDK)\gacutil /u $(ASSEMBLY_NAME) 
!ENDIF