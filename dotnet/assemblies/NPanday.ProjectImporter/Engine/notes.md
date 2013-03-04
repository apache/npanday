# Project Importer Notes

The following notes are intended to partially document the current design,
and primarily assist in future refactoring and cleanup efforts.

## POM Converters

Currently, there is a functional class, PomConverter, which is responsible
for allocating the appropriate type of IPomConverter instance and executing
its conversion methods, depending on the type of project.

The PomConverter class handles making the parent project.

The following converters exist:

 * NormalPomConverter - converts everything
 * WebPomConverter - seems to be for Web sites, skips many things and adds ASPX
 * WebWithVbOrCsProjectFilePomConverter - adds ASPX to NormalPomConverter

The following are converted by the NormalPomConverter:

 * POM "header" - parent, ID and packaging
 * SCM (though seems redundant as each project has a parent that declares this)
 * compile plugin and options
 * msbuild (if project is WPF)
 * test plugin (if test project - that is, has a NUnit dependency)
 * web references (even if not a "web" project)
 * COM references
 * key file
 * embedded resources
 * interproject references
 * project references
 * references as dependencies (that are not in the RSP files already)

### Refactoring opportunities

The parent POM generation can be turned into a new project converter.

All converters can be made to work from the single abstract converter, with
functionality pushed up.

The types can be turned into filters - single conversion stream, but
additional elements process the model as it progresses.

Add a fallback project type for converting unrecognised projects to a basic POM.

The duplication of handling SCM URLs should be rationalised. In addition, the
API refers to an "SCM tag", but it is constantly treated as an Subversion
URL. A richer SCM structure should be possible to support different
connection/viewer elements, as well as other SCM systems.

AddPluginConfiguration in the AbstractPomConverter should rename the
overloaded methods to be clearer what types of arguments are passed. There
also appears to be some hardcoded configuration options in the one that takes
properties - this appears to be a shortcut for certain types that could be
clearer.

## Project Type Importers

The project type importers determine the structure of the project, and create
POMs as appropriate. The following formats are supported:

 * "Abnormal" - represents a project structure that is not supported, for
   example if the project is outside of the solution directory
 * Flat Multi Module - multiple projects where one is in the same directory
   as the solution
 * Flat Single Module - single project in the same directory as the solution
 * Single Module - single project in a subdirectory of the solution
 * Multi Module - multiple projects in subdirectories of the solution

All derive from the AbstractProjectAlgorithm.

The inconsistency in naming should be corrected (importer on the interface,
but the types are just "Project", and the base class is an
"ProjectAlgorithm").

## Project Digesters

Has an interface for digesting a project, and a base class that provides a
utility method.

Two implementations:

 * Normal project - almost everything
 * Web project - web sites only

The "normal" digester performs the following steps:

 * read existing POM
 * add inter project references
 * add build properties
 * add build items - including resolving references

The web digester has a little duplication, but mostly omits the unnecessary
pieces.

Like with the converters, the web project need not be as unique and this
might be better dealt with as a filter over a single digest process. It
should at least factor out the common code.

In addition, the existing POM file is read. This is used both for reimporting
(see below), and for retaining dependencies on a full import. Ideally, this
would only be read when needed, not in the digest process.

The BaseProjectDigesterAlgorithm contains a method GetAssemblyName that seems
to also digest the whole project for a single value. This could be slowing
the process down and refactored to avoid the need.

AddDependency looks for duplicates keyed only on group and artefact ID - if
there are any with different types and/or classifiers (which is valid), they
may not be added.

## Parser

This only contains a solution parser, which should be removed in favour of
EnvDTE. However, we don't want automation in the project importer library, so
we should instead pass that information in to the importer (as there is very
little actual data used from the solutions).

## Validator

This does a small amount of validation, but primarily determines what project
type importer to use.

This should be folded into the project type importer structure.

This also contains the project type enumerations.

## Verifier

Despite the name, this seems to only be for re-syncing an existing POM when
re-importing.

Should rename appropriately.

This could potentially be merged with the VS operations that modify the POM
on the fly so that they behave the same way and there is less logic in the
addin.

Note that this contains a form and some message boxes to ask questions if the
project structure changes. These should be moved into the add-in. These could
potentially be passed in as change request listeners, or the API split to
determine the changes, then apply them and let the add in determine what to
ask in the middle.

## Utility classes

A note on NPanday.Utils is worth making here. There's an odd split between
utilities operating on Model.Pom - several copies exist in the POM converters
that are also in the utilities. Ideally, the utilities library would be split
into functional groups with POM operations added to Model.Pom directly (or a
companion assembly), and moved out of the project importer.

Some of the methods in here, such as HasPlugin, read the POM each time they
are called. This could lead to many POM reads for a single import / addin
operation that is a performance concern.

# Required test cases

The following are the test cases we need to exercise. Some are already
present - they haven't been matched up.

## Project Importer

 * ASP.NET project with resources (currently triggers msbuild)
 * WPF project with C# XAML file (triggers msbuild)
 * WPF project with VB XAML file (triggers msbuild)
 * C# project
 * VB project
 * Project with signing
 * Project with unit tests
 * Project with inter-project references
 * Project with project references
 * Project with embedded resources
 * Project with COM references
 * Project with Web References
 * Web site project (WebPomConverter)
 * Web application project (WebWithVbOrCsProjectFilePomConverter)
 * Providing an SCM URL
 * Flat single project structure
 * Flat multi project structure
 * Layered single project structure
 * Layered multi project structure
 * Unsupported project structures
 * Nested solutions (multiple levels of depth)
 * Azure project with a worker role
 * Azure project with a web role
 * Azure project with a web and worker role
 * Azure project with a web role and a regular library project
 * Other azure project structures (see tutorials on windowsazure.com)
 * Import a project that already exists with dependencies present

## Project Synchronization

The project sync API should be tested for reimporting existing projects with
certain characteristics.

## Reference Resolution

The reference resolution now mostly matches that of MSBuild, after first
checking the local Maven repository.

These lookups should just be done to validate the artifact exists somewhere.
They should not be put into the POM in either case as that reduced
portability.

Ideally, such dependencies could be marked as `provided` instead, and
NPanday's own resolution should be able to lookup the correct framework
library locations.

There remains some difference in the resolution process done on import (based
on the MSBuild project alone), and that in the ReferenceAdded handler in
Connect.cs (which has access to the actual project including the reference
path which points at the Reference Assembly). Potential improvements here:
 - abstract out the reference resolution into a common library
 - allow passing in the reference paths to the import so that a project
   import gets consistent behaviour within the addin (mostly making the prior
   lookups redundant here, but useful in the Maven plugins)
 - persist configuration of the project import so that add reference knows to
   use the framework libraries, etc. for lookup.

