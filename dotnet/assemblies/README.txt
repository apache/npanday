Not using the profiles within the pom, since pom profile support for more than one profile is buggy.

TODO: Submit JIRA and test cases:  1) two profiles using only names causes NPE, 2) two profiles using name-value pairs will not
allow execution of first profile.