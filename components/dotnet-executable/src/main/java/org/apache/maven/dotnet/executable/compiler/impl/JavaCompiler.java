package org.apache.maven.dotnet.executable.compiler.impl;

import org.apache.maven.dotnet.executable.ExecutionException;

import java.util.List;

public class JavaCompiler extends BaseCompiler
{
    public boolean failOnErrorOutput()
    {
        return false;
    }

    public List<String> getCommands()
        throws ExecutionException
    {
        return null;  
    }

    public void resetCommands( List<String> commands )
    {

    }
}
