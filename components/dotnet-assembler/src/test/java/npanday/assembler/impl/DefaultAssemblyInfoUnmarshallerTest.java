package npanday.assembler.impl;

import junit.framework.TestCase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import npanday.assembler.AssemblyInfo;

public class DefaultAssemblyInfoUnmarshallerTest
    extends TestCase
{
    private static String basedir = System.getProperty( "basedir" );

    public void testUnmarshall()
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(
                "src/test/resources/AssemblyInfo.cs" );
        }
        catch ( FileNotFoundException e )
        {
            fail("Could not find test file");
        }

        DefaultAssemblyInfoMarshaller um = new DefaultAssemblyInfoMarshaller();
        try
        {
            AssemblyInfo assemblyInfo = um.unmarshall( fis );
            assertEquals( "Incorrect Assembly Version", "1.0.0", assemblyInfo.getVersion());
        }
        catch ( IOException e )
        {
            fail("Problem iwht reading the assembly info input");
        }
    }
}
