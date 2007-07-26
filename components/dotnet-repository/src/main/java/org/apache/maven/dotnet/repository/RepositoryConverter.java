package org.apache.maven.dotnet.repository;

import org.openrdf.repository.Repository;

import java.io.File;
import java.io.IOException;

public interface RepositoryConverter
{
    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = RepositoryConverter.class.getName();

    void convert( Repository repository, File mavenRepository) throws IOException;
}
