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
package npanday.executable;

import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.ArrayList;


public class CommandExecutorTest
{
    private static final String MKDIR = "mkdir";

    private String parentPath;

    private List<String> params = new ArrayList<String>();

    private CommandExecutor cmdExecutor;

    public CommandExecutorTest()
    {
        File f = new File( "test" );
        parentPath = System.getProperty( "user.dir" ) + File.separator + "target" + File.separator + "test-resources";

        File parentPathFile = new File( parentPath );
        if ( !parentPathFile.exists() )
        {
            parentPathFile.mkdir();
        }

        cmdExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
    }

    @Test
    public void testParamWithNoSpaces()
        throws ExecutionException
    {
        String path = parentPath + File.separator + "sampledirectory";

        params.clear();
        params.add( path );

        cmdExecutor.executeCommand( MKDIR, params );
        File dir = new File( path );

        assertTrue( dir.exists() );

        if ( dir.exists() )
        {
            dir.delete();
        }
    }

    @Test
    public void testParamWithSpaces()
        throws ExecutionException
    {
        String path = parentPath + File.separator + "sample directory";

        params.clear();
        params.add( path );

        cmdExecutor.executeCommand( MKDIR, params );
        File dir = new File( path );

        assertTrue( dir.exists() );

        if ( dir.exists() )
        {
            dir.delete();
        }
    }

    @Test
    /**
     test is related to NPANDAY-366
     */
    public void testTooLongCommandName()
        throws ExecutionException
    {
        params.clear();

        cmdExecutor.setLogger( new ConsoleLogger( 0, null ) );

        try
        {
            cmdExecutor.executeCommand( repeat( 'x', 260 ), params, null, false );
            fail( "Expected the command to fail!" );
        }
        catch ( ExecutionException e )
        {
            System.out.println( cmdExecutor.toString() );
            // the message is language-specific, but better to ensure an error than
            // ignoring the test
            // assertEquals( "The input line is too long.", cmdExecutor.getStandardError() );
            assertEquals( 1, cmdExecutor.getResult() );
        }
    }

    @Test
    /**
     test is related to NPANDAY-366
     */
    public void testTooLongCommandName_withSpace()
        throws ExecutionException
    {
        params.clear();

        cmdExecutor.setLogger( new ConsoleLogger( 0, null ) );

        try
        {
            cmdExecutor.executeCommand( "echo " + repeat( 'x', 255 ), params, null, false );
            fail( "Expected the command to fail!" );
        }
        catch ( ExecutionException e )
        {
            System.out.println( cmdExecutor.toString() );
            // the message is language-specific, but better to ensure an error than
            // ignoring the test
            // assertEquals( "The input line is too long.", cmdExecutor.getStandardError() );
            assertEquals( 1, cmdExecutor.getResult() );
        }
    }


    @Test
    /**
     test is related to NPANDAY-366
     */
    public void testLongCommand()
        throws ExecutionException
    {
        params.clear();

        cmdExecutor.setLogger( new ConsoleLogger( 0, null ) );
        params.add( repeat( 'a', 260 ) );

        cmdExecutor.executeCommand( "echo", params, null, false );
        System.out.println( cmdExecutor.toString() );

        assertEquals( repeat( 'a', 260 ), cmdExecutor.getStandardOut() );
    }

    private static String repeat( char c, int i )
    {
        String tst = "";
        for ( int j = 0; j < i; j++ )
        {
            tst = tst + c;
        }
        return tst;
    }
}