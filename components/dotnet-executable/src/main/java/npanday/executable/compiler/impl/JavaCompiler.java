package npanday.executable.compiler.impl;

import npanday.executable.ExecutionException;

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
