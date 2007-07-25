package org.apache.maven.dotnet.repository;

/*
import javax.repository.PersistenceManagerFactory;
import javax.repository.JDOHelper;
import javax.repository.PersistenceManager;
import javax.repository.Transaction;
import java.util.Set;
import java.util.HashSet;
*/

public class JdoTest
{
    /*
    PersistenceManager pm;

    public void setUp()
    {
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory( "jpox.properties" );
        pm = pmf.getPersistenceManager();
    }

    public void testPopulate()
    {
        Transaction tx = pm.currentTransaction();
        try
        {
            tx.begin();

            Project project = new Project();
            project.setArtifactId( "NMaven.Pom.Model" );
            project.setGroupId( "NMaven.Model" );
            project.setVersion( "1.1" );
            ProjectDependency project2 = new ProjectDependency();
            project2.setArtifactId( "NMaven.Pom.Model2" );
            project2.setGroupId( "NMaven.Model2" );
            project2.setVersion( "1.2" );
            Set<ProjectDependency> set = new HashSet<ProjectDependency>();
            set.add( project2 );
            project.setProjectDependencies( set );
            pm.makePersistent( project );
           // pm.makePersistent( set ); 
            tx.commit();
        }
        finally
        {
            if ( tx.isActive() )
            {
                tx.rollback();
            }

            pm.close();
        }
    }
    */
}
