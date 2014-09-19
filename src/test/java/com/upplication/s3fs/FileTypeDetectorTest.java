package com.upplication.s3fs;

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;
import com.upplication.s3fs.util.FileTypeDetector;
import org.apache.tika.Tika;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class FileTypeDetectorTest {
    private FileSystem fsMem;
    @Before
    public void cleanup() throws IOException {
        fsMem = MemoryFileSystemBuilder.newLinux().build("linux");
    }

    @After
    public void closeMemory() throws IOException{
        fsMem.close();
    }

    @Test
    public void fileTypeDetectorUseTike() throws IOException {

        Tika tika = spy(new Tika());
        FileTypeDetector detector = new FileTypeDetector(tika);
        Path path = fsMem.getPath("/file.html");
        Files.write(path, "<html><body>ey</body></html>".getBytes());
        detector.probeContentType(path);

        verify(tika).detect(any(InputStream.class), eq(path.getFileName().toString()));
    }

    @Test
    public void fileTypeDetectorDetectByServiceLocator() throws IOException {
        // act
        ServiceLoader<java.nio.file.spi.FileTypeDetector> loader = ServiceLoader
                .load(java.nio.file.spi.FileTypeDetector.class, ClassLoader.getSystemClassLoader());
        // assert
        boolean existsS3fsFileTypeDetector = false;
        for (java.nio.file.spi.FileTypeDetector installed : loader) {
            if (installed instanceof FileTypeDetector){
                existsS3fsFileTypeDetector = true;
            }
        }

        assertTrue(existsS3fsFileTypeDetector);
    }
}