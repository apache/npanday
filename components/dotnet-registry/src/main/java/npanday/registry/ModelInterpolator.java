package npanday.registry;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.interpolation.InterpolationException;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public interface ModelInterpolator
{
    <T> T interpolate( T model, MavenProject project )
        throws InterpolationException;
}
