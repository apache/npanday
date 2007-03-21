import junit.framework.TestCase;
import org.apache.maven.dotnet.embedder.MavenEmbedderService;
import org.apache.maven.dotnet.embedder.MavenExecutionRequest;
import org.apache.maven.dotnet.embedder.impl.MavenEmbedderServiceImpl;
import org.apache.maven.dotnet.embedder.impl.MavenExecutionRequestImpl;

public class ServiceTest
    extends TestCase
{
    public void testA()
    {
        MavenEmbedderServiceImpl service = new MavenEmbedderServiceImpl();
        service.initialize();
        MavenExecutionRequest request = new MavenExecutionRequestImpl();
        //service.execute( request );
    }
}
