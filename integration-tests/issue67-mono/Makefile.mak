install :
	cd "NMAVEN.TEST.Issue67.Application"
	$(MAKE) /$(MAKEFLAGS) /F .\Makefile.mak vendor="$(vendor)"
	cd ".."

	cd "NMAVEN.TEST.Issue67.Domain"	
	$(MAKE) /$(MAKEFLAGS) /F .\Makefile.mak vendor="$(vendor)"
	cd ".."

	cd "NMAVEN.TEST.Issue67.Loader"	
	$(MAKE) /$(MAKEFLAGS) /F .\Makefile.mak vendor="$(vendor)"
	cd ".."

	cd "NMAVEN.TEST.Issue67.Runner"	
	$(MAKE) /$(MAKEFLAGS) /F .\Makefile.mak vendor="$(vendor)"
	cd ".."

clean :
	cd "NMAVEN.TEST.Issue67.Application"
	$(MAKE) /$(MAKEFLAGS) /F .\Makefile.mak clean vendor="$(vendor)"
	cd ".."

	cd "NMAVEN.TEST.Issue67.Domain"	
	$(MAKE) /$(MAKEFLAGS) /F .\Makefile.mak clean vendor="$(vendor)"
	cd ".."

	cd "NMAVEN.TEST.Issue67.Loader"	
	$(MAKE) /$(MAKEFLAGS) /F .\Makefile.mak clean vendor="$(vendor)"
	cd ".."

	cd "NMAVEN.TEST.Issue67.Runner"	
	$(MAKE) /$(MAKEFLAGS) /F .\Makefile.mak clean vendor="$(vendor)"
	cd ".."

test : 
	cd "NMAVEN.TEST.Issue67.Runner"	
	$(MAKE) /$(MAKEFLAGS) /F .\Makefile.mak test vendor="$(vendor)"
	cd ".."