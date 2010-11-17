package npanday.executable;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import npanday.executable.CommandExecutor;


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
            cmdExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        }

        @Test
        public void testParamWithNoSpaces()
            throws ExecutionException
        {
            String path = parentPath + File.separator + "sampledirectory";

            params.clear();
            params.add(path);

            cmdExecutor.executeCommand( MKDIR, params );
            File dir = new File( path );

            assertTrue( dir.exists() );
        }

        @Test
        public void testParamWithSpaces()
            throws ExecutionException
        { 
            String path = parentPath + File.separator + "sample directory";

            params.clear(); 
            params.add(path);

            cmdExecutor.executeCommand( MKDIR, params );
            File dir = new File( path );

            assertTrue( dir.exists() );
         }

    }