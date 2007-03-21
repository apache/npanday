package org.apache.maven.plugin.vstudio;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class VisualStudioUtil
{


    private static final String[] EMPTY_STRING_ARRAY = {};

    private static final String[] DEFAULT_INCLUDES = {"**/**"};

    private static final String[] DEFAULT_INCLUDES_CSHARP = {"**/*.cs"};

    public static VisualStudioFile getPom( MavenProject project )
    {

        String relativeToProjectRoot =
            toRelativeAndFixSeparator( project.getBasedir(), project.getFile().getAbsolutePath(), false );

        VisualStudioFile file =
            new VisualStudioFile( project.getFile(), BuildAction.Content, relativeToProjectRoot, SubType.Null );

        return file;
    }


    public static String toRelativeAndFixSeparator( File basedir, String absolutePath, boolean replaceSlashes )
    {
        String relative = "";

        System.out.println( "absolute path" + absolutePath );

        if ( absolutePath.equals( basedir.getAbsolutePath() ) )
        {
            System.out.println( "option 1" );
            //relative = ".";
        }
        else if ( absolutePath.startsWith( basedir.getAbsolutePath() ) )
        {
            System.out.println( "option 2" );
            relative = absolutePath.substring( basedir.getAbsolutePath().length() + 1 );
        }
        else
        {
            System.out.println( "option 3" );
            relative = absolutePath;
        }

        if ( relative.startsWith( "." ) )
        {
            relative = relative.substring( 2, relative.length() );
        }

        relative = StringUtils.replace( relative, "", "\\" ); //$NON-NLS-1$ //$NON-NLS-2$

        return relative;
    }

    public static VisualStudioFile[] getSourceFiles( File baseDir, List sourceFileRoots, Set includes, Set excludes )
    {

        ArrayList list = new ArrayList();

        for ( Iterator i = sourceFileRoots.iterator(); i.hasNext(); )
        {

            DirectoryScanner scanner = new DirectoryScanner();

            String root = (String) i.next();

            File rootDir = new File( root );

            if ( !rootDir.exists() || !rootDir.isDirectory() )
            {
                break;
            }

            scanner.setBasedir( root );

            //process the includes and excludes...
            if ( includes == null || includes.isEmpty() )
            {
                scanner.setIncludes( DEFAULT_INCLUDES_CSHARP );
            }
            else
            {
                scanner.setIncludes( getStringArrayFromSet( includes ) );
            }

            if ( excludes == null || excludes.isEmpty() )
            {
                scanner.setExcludes( getStringArrayFromSet( excludes ) );
            }
            else
            {
                scanner.addDefaultExcludes();
            }

            scanner.scan();

            List includedFiles = Arrays.asList( scanner.getIncludedFiles() );

            for ( Iterator j = includedFiles.iterator(); j.hasNext(); )
            {
                String relativePath = (String) j.next();

                File f = new File( rootDir, relativePath );

                String relativeToProjectRoot = toRelativeAndFixSeparator( baseDir, f.getAbsolutePath(), false );

                VisualStudioFile file = create( f, relativeToProjectRoot );

                if ( file != null )
                {
                    list.add( file );
                }
            }
        }

        return (VisualStudioFile[]) list.toArray( new VisualStudioFile[list.size()] );
    }

    /**
     * factory method
     * <p/>
     * This is a bit messy.....
     *
     * @param f
     * @param relativePath
     * @return
     */
    private static VisualStudioFile create( File f, String relativePath )
    {

        VisualStudioFile vsf = null;

        if ( lastNCharsEqual( f.getName(), 10, ".aspx.resx" ) )
        {
            //add .resx for aspx file
            vsf = new VisualStudioFile( f, BuildAction.EmbeddedResources, relativePath,
                                        new DependentUpon( FilenameUtils.removeExtension( f.getName() ) + ".cs" ) );
        }
        else if ( lastNCharsEqual( f.getName(), 8, ".aspx.cs" ) )
        {
            vsf = new VisualStudioFile( f, BuildAction.Compile, relativePath, SubType.AspxCodeBehind,
                                        new DependentUpon( FilenameUtils.removeExtension( f.getName() ) ) );
            //add normal .asax.cs file
        }
        else if ( lastNCharsEqual( f.getName(), 8, ".asax.cs" ) )
        {
            vsf = new VisualStudioFile( f, BuildAction.Compile, relativePath, SubType.Code,
                                        new DependentUpon( FilenameUtils.removeExtension( f.getName() ) ) );
            //add normal .asax file
        }
        else if ( lastNCharsEqual( f.getName(), 5, ".asax" ) )
        {
            vsf = new VisualStudioFile( f, BuildAction.Content, relativePath, SubType.Component );
            //add normal .cs file
        }
        else if ( lastNCharsEqual( f.getName(), 10, ".asax.resx" ) )
        {
            vsf = new VisualStudioFile( f, BuildAction.EmbeddedResources, relativePath,
                                        new DependentUpon( FilenameUtils.removeExtension( f.getName() ) + ".cs" ) );
            //add normal .cs file
        }
        else if ( lastNCharsEqual( f.getName(), 3, ".cs" ) && ( !lastNCharsEqual( f.getName(), 8, ".aspx.cs" ) ) )
        {
            vsf = new VisualStudioFile( f, BuildAction.Compile, relativePath, SubType.Code );
            //add normal .aspx file.
        }
        else if ( lastNCharsEqual( f.getName(), 5, ".aspx" ) )
        {
            vsf = new VisualStudioFile( f, BuildAction.Content, relativePath, SubType.Form );
        }

        return vsf;
    }

    private static boolean lastNCharsEqual( String s, int n, String equalTo )
    {
        if ( s.length() < n )
        {
            return false;
        }
        if ( StringUtils.right( s, n ).equals( equalTo ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private static String[] getStringArrayFromSet( Set setOfStrings )
    {
        return (String[]) setOfStrings.toArray( new String[setOfStrings.size()] );
    }


    public static VisualStudioFile[] getResourceFiles( File baseDir, List resourceFiles )
    {

        ArrayList list = new ArrayList();

        if ( resourceFiles == null || resourceFiles.size() == 0 )
        {
            return new VisualStudioFile[0];
        }

        for ( Iterator i = resourceFiles.iterator(); i.hasNext(); )
        {

            DirectoryScanner scanner = new DirectoryScanner();

            Resource r = (Resource) i.next();

            if ( r == null )
            {
                return new VisualStudioFile[0];
            }

            File rootDir = new File( r.getDirectory() );

            if ( !rootDir.exists() || !rootDir.isDirectory() )
            {
                break;
            }

            scanner.setBasedir( rootDir );

            if ( r.getIncludes() == null || r.getIncludes().isEmpty() )
            {
                scanner.setIncludes( DEFAULT_INCLUDES );
            }
            else
            {
                scanner.setIncludes( (String[]) r.getIncludes().toArray( new String[r.getIncludes().size()] ) );
            }

            if ( r.getExcludes() == null || r.getExcludes().isEmpty() )
            {
                scanner.setExcludes( EMPTY_STRING_ARRAY );
            }
            else
            {
                scanner.setExcludes( (String[]) r.getExcludes().toArray( new String[r.getExcludes().size()] ) );
            }

            scanner.addDefaultExcludes();
            scanner.scan();

            List includedFiles = Arrays.asList( scanner.getIncludedFiles() );

            for ( Iterator j = includedFiles.iterator(); j.hasNext(); )
            {
                String relativePath = (String) j.next();

                File f = new File( rootDir, relativePath );

                String relativeToProjectRoot = toRelativeAndFixSeparator( baseDir, f.getAbsolutePath(), false );

                VisualStudioFile file =
                    new VisualStudioFile( f, BuildAction.Content, relativeToProjectRoot, SubType.Null );

                list.add( file );
            }
        }

        return (VisualStudioFile[]) list.toArray( new VisualStudioFile[list.size()] );
    }

//  if ( resource.getIncludes() != null && !resource.getIncludes().isEmpty() )
//  {
//      scanner.setIncludes( (String[]) resource.getIncludes().toArray( EMPTY_STRING_ARRAY ) );
//  }
//  else
//  {
//      scanner.setIncludes( DEFAULT_INCLUDES );
//  }
//  if ( resource.getExcludes() != null && !resource.getExcludes().isEmpty() )
//  {
//      scanner.setExcludes( (String[]) resource.getExcludes().toArray( EMPTY_STRING_ARRAY ) );
//  }

    //String destination = name;

//  if ( targetPath != null )
//  {
//      destination = targetPath + "/" + name;
//  }
//
//  File source = new File( resource.getDirectory(), name );
//
//  File destinationFile = new File( outputDirectory, destination );
//
//  if ( !destinationFile.getParentFile().exists() )
//  {
//      destinationFile.getParentFile().mkdirs();
//  }
//
//  try
//  {
//      copyFile( source, destinationFile, resource.isFiltering() );
//  }
//  catch ( IOException e )
//  {
//      throw new MojoExecutionException( "Error copying resources", e );
//  }


}
