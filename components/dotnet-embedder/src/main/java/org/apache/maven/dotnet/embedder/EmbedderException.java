package org.apache.maven.dotnet.embedder;

public class EmbedderException extends Exception
{
    static final long serialVersionUID = -67348843971270983L;

    public EmbedderException()
    {
        super();
    }

    public EmbedderException( String message )
    {
        super( message );
    }

    public EmbedderException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public EmbedderException( Throwable cause )
    {
        super( cause );
    }
}
