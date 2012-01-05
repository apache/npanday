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


package npanday.executable.execution;


import npanday.executable.CommandExecutor
import npanday.executable.ExecutionException
import npanday.executable.execution.quoting.CustomSwitchAwareQuotingStrategy
import npanday.executable.execution.quoting.PlexusNativeQuotingStrategy
import org.codehaus.plexus.logging.Logger
import org.codehaus.plexus.logging.console.ConsoleLogger
import org.codehaus.plexus.util.Os
import org.junit.Test
import org.junit.internal.AssumptionViolatedException
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import static org.junit.Assert.*

@RunWith(value = Parameterized.class)
public class CommandExecutorTest
{
    private static final String MKDIR = "mkdir";

    private String parentPath;

    private List<String> params = new ArrayList<String>();

    private CommandExecutor cmd;
    private String cmdHint;

    @Parameters
    public static Collection<Object[]> data()
    {
        def osKey = isWindows() ? "win_" : "x_";
        Object[][] data = [
                [osKey + "unified_simple_quoting",
                        new UnifiedShellCommandExecutor(new PlexusNativeQuotingStrategy())],
                [osKey + "unified_custom_quoting",
                        new UnifiedShellCommandExecutor(new CustomSwitchAwareQuotingStrategy())]
        ];
        return Arrays.asList(data);
    }

    public CommandExecutorTest(String hint, CommandExecutor cmd)
    {
        cmdHint = hint;
        println "Executing with " + hint
        File f = new File("test");
        parentPath = System.getProperty("user.dir") + File.separator + "target" + File.separator +
                "test-resources";

        File parentPathFile = new File(parentPath);
        if ( !parentPathFile.exists() )
        {
            parentPathFile.mkdir();
        }

        this.cmd = cmd;
        cmd.setLogger(new ConsoleLogger(Logger.LEVEL_DEBUG, "Command Executor"));
    }

    @Test
    public void testSimpleCommandArg()
    throws ExecutionException
    {
        testArgExpansion(["x"], "x");
    }

    @Test
    public void testCommandArgWithSpaces()
    throws ExecutionException
    {
        testArgExpansion(["a b"], '"a b\"');
    }

    @Test
    public void testCommandArgWithEmbeddedSingleQuotes_middle()
    throws ExecutionException
    {
        testArgExpansion(["a ' b"], '"a \' b"');
    }

    @Test
    public void testCommandArgWithEmbeddedSingleQuotes_trailing()
    throws ExecutionException
    {
        testArgExpansion(["a '"], [
                         win_unified_simple_quoting: '"a \'"',
                         win_unified_custom_quoting: '"a \'"'
                         ]);
    }

    @Test
    public void testCommandArgWithEmbeddedSingleQuotes_leading()
    throws ExecutionException
    {
        testArgExpansion(["' a"], [
                         win_unified_simple_quoting: '"\' a"',
                         win_unified_custom_quoting: '"\' a"'
                         ]);
    }

    @Test
    public void testCommandArgWithEmbeddedSingleQuotes_surrounding()
    throws ExecutionException
    {
        testArgExpansion(["' a '"], [
                         win_unified_simple_quoting: '"\' a \'"',
                         win_unified_custom_quoting: '"\' a \'"'
                         ]);
    }

    @Test
    public void testCommandArgWithEmbeddedDoubleQuotes_middle()
    throws ExecutionException
    {
        testArgExpansion(['a " b'], [
                         win_unified_simple_quoting: '"a \\" b"',
                         win_unified_custom_quoting: '"a \\" b"'
                         ]);
    }

    @Test
    public void testCommandArgWithEmbeddedDoubleQuotes_trailing()
    throws ExecutionException
    {
        testArgExpansion(['a "'], [
                         win_unified_simple_quoting: '"a \\""',
                         win_unified_custom_quoting: '"a \\""'
                         ]);
    }

    @Test
    public void testCommandArgWithEmbeddedDoubleQuotes_leading()
    throws ExecutionException
    {
        testArgExpansion(['" a'], [
                         win_unified_simple_quoting: '"\\" a"',
                         win_unified_custom_quoting: '"\\" a"'
                         ]);
    }

    @Test
    public void testCommandArgWithEmbeddedDoubleQuotes_surrounding()
    throws ExecutionException
    {
        testArgExpansion(['" a "'], [
                         win_unified_simple_quoting: '" a "', // if it yet is quoted, it wont quote again
                         win_unified_custom_quoting: '"\\" a \\""' // but we want it escaped and quoted again
                         ]);
    }

    @Test
    public void testCommandArgSwitchWithSpaceInValue_slashColon()
    throws ExecutionException
    {
        testArgExpansion(['/test:a b'], [
                         win_unified_simple_quoting: '"/test:a b"',
                         win_unified_custom_quoting: '/test:"a b"'
                         ]);
    }

    @Test
    public void testCommandArgSwitchWithSpaceInValue_slashEquals()
    throws ExecutionException
    {
        testArgExpansion(['/test=a b'], [
                         win_unified_simple_quoting: '"/test=a b"',
                         win_unified_custom_quoting: '/test="a b"'
                         ]);
    }

    @Test
    public void testCommandArgSwitchWithSpaceInValue_minusColon()
    throws ExecutionException
    {
        testArgExpansion(['-test:a b'], [
                         win_unified_simple_quoting: '"-test:a b"',
                         win_unified_custom_quoting: '-test:"a b"'
                         ]);
    }

    @Test
    public void testCommandArgSwitchWithSpaceInValue_minusEquals()
    throws ExecutionException
    {
        testArgExpansion(['-test=a b'], [
                         win_unified_simple_quoting: '"-test=a b"',
                         win_unified_custom_quoting: '-test="a b"'
                         ]);
    }

    @Test
    public void testCommandArgSwitchWithSpaceInValue_prequotedDouble()
    throws ExecutionException
    {
        testArgExpansion(['/test:"a b"'], [
                         win_unified_simple_quoting: '"/test:\\"a b\\""',
                         win_unified_custom_quoting: '/test:"\\"a b\\""'
                         ]);
    }

    @Test
    public void testCommandArgSwitchWithSpaceInValue_prequotedSingle()
    throws ExecutionException
    {
        testArgExpansion(["/test:'a b'"], [
                         win_unified_simple_quoting: '"/test:\'a b\'"',
                         win_unified_custom_quoting: '/test:"\'a b\'"'
                         ]);
    }

    @Test
    public void testCscDefineSwitch()
    throws ExecutionException
    {
        def raw = '/define:"CONFIG="Debug",DEBUG=-1,TRACE=-1,_MyType="Windows",PLATFORM="AnyCPU"'
        def quoted = '/define:"\\"CONFIG=\\"Debug\\",DEBUG=-1,TRACE=-1,_MyType=\\"Windows\\",PLATFORM=\\"AnyCPU\\""'
        testArgExpansion([raw], [win_unified_custom_quoting: quoted]);
    }

    private def testArgExpansion(ArrayList<String> args, String expected)
    {
        cmd.executeCommand("echo", args)
        assert cmd.result == 0
        assert cmd.standardOut == expected
    }

    private def testArgExpansion(ArrayList<String> args, Map<String, String> expectedPerHint)
    {
        if (!isWindows()){

        }

        if ( !expectedPerHint.containsKey(cmdHint) )
        {
            cmd.executeCommand("echo", args)
            throw new AssumptionViolatedException("Quoting behaviour undefined for '" + cmdHint + "'.\n"
                                                          + args + " -> " + cmd.standardOut)
        }

        testArgExpansion(args, expectedPerHint[cmdHint])
    }

    @Test
    public void testErrorWithReturnValue()
    throws ExecutionException
    {
        // hopefully an executable named asdfasdf doesnt exist
        try
        {
            cmd.executeCommand("asdfasdf", [])
            fail("expected command to fail")
        }
        catch (ExecutionException)
        {

        }
        println "Result is $cmd.result"
        assert cmd.result != 0
    }

    @Test
    public void testParamWithNoSpaces()
    throws ExecutionException
    {
        String path = parentPath + File.separator + "sampledirectory";

        File dir = new File(path)
        if ( dir.exists() ) dir.deleteDir()

        params.clear();
        params.add(path);

        cmd.executeCommand(MKDIR, params);

        assertTrue(dir.exists());

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
        params.add(path);

        cmd.executeCommand(MKDIR, params);
        File dir = new File(path);

        assertTrue(dir.exists());

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
        // we are only interested in exectuing this test
        // on Windows, to catch the "Command line to long" issue for cmd.exe.
        if ( !isWindows() ) return;

        params.clear();

        cmd.setLogger(new ConsoleLogger(0, null));

        try
        {
            cmd.executeCommand(repeat('x', 260), params, null, false);
            fail("Expected the command to fail!");
        }
        catch (ExecutionException e)
        {
            System.out.println(cmd.toString());
            // the message is language-specific, but better to ensure an error than
            // ignoring the test
            // assertEquals( "The input line is too long.", cmdExecutor.getStandardError() );
            assertEquals(1, cmd.getResult());
        }
    }

    @Test
    /**
     test is related to NPANDAY-366
     */
    public void testTooLongCommandName_withSpace()
    throws ExecutionException
    {
        // we are only interested in exectuing this test
        // on Windows, to catch the "Command line to long" issue for cmd.exe.
        if ( !isWindows() ) return;

        params.clear();

        cmd.setLogger(new ConsoleLogger(0, null));

        try
        {
            cmd.executeCommand("echo " + repeat('x', 255), params, null, false);
            fail("Expected the command to fail!");
        }
        catch (ExecutionException e)
        {
            System.out.println(cmd.toString());
            // the message is language-specific, but better to ensure an error than
            // ignoring the test
            // assertEquals( "The input line is too long.", cmdExecutor.getStandardError() );
            assertEquals(1, cmd.getResult());
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

        cmd.setLogger(new ConsoleLogger(0, null));
        params.add(repeat('a', 260));

        cmd.executeCommand("echo", params, null, false);
        System.out.println(cmd.toString());

        assertEquals(repeat('a', 260), cmd.getStandardOut());
    }

    private static String repeat(String c, int i)
    {
        String tst = "";
        for ( int j = 0; j < i; j++ )
        {
            tst = tst + c;
        }
        return tst;
    }

    /**
     * Simple check if the test is executed on Windows...
     */
    private static boolean isWindows()
    {
       return Os.isFamily(Os.FAMILY_WINDOWS);
    }
}