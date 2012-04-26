/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package npanday.plugin.libraryimporter;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="me@lcorneliussen.de">Lars Corneliussen, Faktum Software</a>
 */
public class LibImporterPathUtils
{
    public static Map<String, File> getLibDirectories( File packageDir )
    {
        File libDir = new File( packageDir, "lib" );
        Map<String, File> libDirs = Maps.newHashMap();
        if ( getLibraries( libDir ).size() > 0 )
        {
            libDirs.put("lib", libDir );
        }

        for ( File framework : libDir.listFiles() )
        {
            if ( !framework.isDirectory() )
            {
                continue;
            }

            if ( getLibraries( framework ).size() > 0 )
            {
                libDirs.put( framework.getName(), framework );
            }

            for ( File platform : framework.listFiles() )
            {
                if ( !platform.isDirectory() )
                {
                    continue;
                }

                if ( getLibraries( platform ).size() > 0 )
                {
                    libDirs.put(framework.getName() + "/" + platform.getName(), platform );
                }

            }
        }

        return libDirs;
    }

    public static List<File> getLibraries( File libFolder )
    {
        return Lists.newArrayList(
            libFolder.listFiles(
                new FileFilter()
                {
                    public boolean accept( File f )
                    {
                        return isDll( f ) || isExe(f);
                    }
                }
            )
        );
    }

    public static String removeFileExtension( String fileName )
    {
        return fileName.replaceFirst("[.][^.]+$", "");
    }

    public static boolean isDll( File file )
    {
        return file.getName().endsWith( ".dll" );
    }

    public static boolean isExe( File file )
    {
        return file.getName().endsWith( ".exe" );
    }

    public static File getLibDirectory( File packageDir, String defaultLibDir )
    {
        return new File( new File( packageDir, "lib" ), defaultLibDir);
    }

    public static List<String> getFileNames( Iterable<File> list )
    {
        Iterable<String> names = Iterables.transform(
            list, new Function<File, String>()
        {
            public String apply( @Nullable File file )
            {
                return file.getName();
            }
        }
        );

        return Lists.newArrayList( names );
    }
}
