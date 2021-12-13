package org.eclipse.dash.licenses.cli;

import javax.inject.Named;
import java.io.InputStream;
import java.io.Reader;

public interface DependencyListReaderFactory {
    @Named("npm")
    IDependencyListReader createNpmLockFileReader(InputStream input);

    @Named("npm-yarn")
    IDependencyListReader createYarnLockFileReader(Reader reader);

    @Named("flat-file")
    IDependencyListReader createFlatFileReader(Reader reader);

    @Named("golang")
    IDependencyListReader createGoSumFileReader(Reader reader);
}
