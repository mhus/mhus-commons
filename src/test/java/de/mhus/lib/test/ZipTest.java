package de.mhus.lib.test;

import de.mhus.commons.errors.InternalRuntimeException;
import de.mhus.commons.io.Zip;
import de.mhus.commons.tools.MFile;
import de.mhus.commons.tools.MString;
import de.mhus.lib.test.util.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@Slf4j
public class ZipTest extends TestCase {

    @Test
    public void testThrowsError() {

        var rootDir = new File("target/test-zip");
        MFile.deleteDir(rootDir);
        rootDir.mkdirs();

        var src = new File(rootDir, "src");
        src.mkdirs();

        var dst = new File(rootDir, "dst");
        dst.mkdirs();

        var zip = Zip.builder().src(src).dst(dst).throwException(true).build();
        assertThat(catchThrowableOfType(() -> zip.zip(), InternalRuntimeException.class)).isNotNull();
        System.out.println(zip.getErrors());
        assertThat(zip.getErrors()).isNotEmpty();

    }

    @Test
    public void testError() {

        var rootDir = new File("target/test-zip");
        MFile.deleteDir(rootDir);
        rootDir.mkdirs();

        var src = new File(rootDir, "src");
        src.mkdirs();

        var dst = new File(rootDir, "dst");
        dst.mkdirs();

        var zip = Zip.builder().src(src).dst(dst).throwException(false).build();
        assertThat(catchThrowableOfType(() -> zip.zip(), InternalRuntimeException.class)).isNull();
        System.out.println(zip.getErrors());
        assertThat(zip.getErrors()).isNotEmpty();

    }

    @Test
    public void testGZipSingleFile() {
        // prepare root dir
        var rootDir = new File("target/test-zip");
        MFile.deleteDir(rootDir);
        rootDir.mkdirs();

        // create a file
        var src = new File(rootDir, "src");
        src.mkdirs();
        var file = new File(src, "test.txt");
        MFile.writeFile(file, "content-" + UUID.randomUUID());
        var zipStructure = dirToString(src);

        // zip the file
        var zip = new File(rootDir, "test.gz");
        var zipErrors = Zip.builder().src(file).dst(zip).build().gzip().getErrors();
        assertThat(zipErrors).isEmpty();

        // unzip the file
        var target = new File(rootDir, "target/test.gz");
        var unzipErrors = Zip.builder().src(zip).dst(target).build().ungzip().getErrors();
        assertThat(unzipErrors).isEmpty();

        // compare the structures
        System.out.println("zipStructure: " + zipStructure);
        var targetStructure = dirToString(target.getParentFile());
        assertThat(targetStructure).isEqualTo(zipStructure);

    }

    @Test
    public void testZipSingleFile() {
        // prepare root dir
        var rootDir = new File("target/test-zip");
        MFile.deleteDir(rootDir);
        rootDir.mkdirs();

        // create a file
        var src = new File(rootDir, "src");
        src.mkdirs();
        var file = new File(src, "test.txt");
        MFile.writeFile(file, "content-" + UUID.randomUUID());
        var zipStructure = dirToString(src);

        // zip the file
        var zip = new File(rootDir, "test.zip");
        var zipErrors = Zip.builder().src(file).dst(zip).build().zip().getErrors();
        assertThat(zipErrors).isEmpty();

        // unzip the file
        var target = new File(rootDir, "target");
        var unzipErrors = Zip.builder().src(zip).dst(target).build().unzip().getErrors();
        assertThat(unzipErrors).isEmpty();

        // compare the structures
        System.out.println("zipStructure: " + zipStructure);
        var targetStructure = dirToString(target);
        assertThat(targetStructure).isEqualTo(zipStructure);

    }

    @Test
    public void testZipFileToFile() {
        // prepare root dir
        var rootDir = new File("target/test-zip");
        MFile.deleteDir(rootDir);
        rootDir.mkdirs();

        // create a directory structure
        var src = new File(rootDir, "src");
        for (int i = 0; i < 20; i++) {
            var dir = new File(src, "d-" + UUID.randomUUID());
            for (int d = 0; d < i % 3; d++) {
                dir = new File(dir, "d-" + UUID.randomUUID()); // deep structures
            }
            dir.mkdirs();
            for (int j = 0; j < 10; j++) {
                var file = new File(dir, "f-" + UUID.randomUUID() + ".txt");
                MFile.writeFile(file, "content-" + UUID.randomUUID());
            }
        }

        // zip the structure
        var zip = new File(rootDir, "test.zip");
        var zipErrors = Zip.builder().src(src).dst(zip).build().zip().getErrors();
        assertThat(zipErrors).isEmpty();
        var zipStructure = dirToString(src);

        // unzip the structure with file target
        var target = new File(rootDir, "target");
        var unzipErrors = Zip.builder().src(zip).dst(target).build().unzip().getErrors();
        assertThat(unzipErrors).isEmpty();

        // compare the structures
        var targetStructure = dirToString(target);
        assertThat(targetStructure).isEqualTo(zipStructure);

    }

    @Test
    public void testZipFileToStream() throws IOException {
        // prepare root dir
        var rootDir = new File("target/test-zip");
        MFile.deleteDir(rootDir);
        rootDir.mkdirs();

        // create a directory structure
        var src = new File(rootDir, "src");
        for (int i = 0; i < 20; i++) {
            var dir = new File(src, "d-" + UUID.randomUUID());
            for (int d = 0; d < i % 3; d++) {
                dir = new File(dir, "d-" + UUID.randomUUID()); // deep structures
            }
            dir.mkdirs();
            for (int j = 0; j < 10; j++) {
                var file = new File(dir, "f-" + UUID.randomUUID() + ".txt");
                MFile.writeFile(file, "content-" + UUID.randomUUID());
            }
        }

        // zip the structure
        var zip = new File(rootDir, "test.zip");
        var zipErrors = Zip.builder().src(src).dst(zip).build().zip().getErrors();
        assertThat(zipErrors).isEmpty();
        var zipStructure = dirToString(src);

        // unzip the structure with stream source
        var target = new File(rootDir, "target");
        var zipStream = new FileInputStream(zip);
        var unzipErrors = Zip.builder().srcStream(zipStream).dst(target).build().unzip().getErrors();
        zipStream.close();
        assertThat(unzipErrors).isEmpty();

        // compare the structures
        var targetStructure = dirToString(target);
        assertThat(targetStructure).isEqualTo(zipStructure);
    }

    @Test
    public void testZipStreamToFile() throws IOException {
        // prepare root dir
        var rootDir = new File("target/test-zip");
        MFile.deleteDir(rootDir);
        rootDir.mkdirs();

        // create a directory structure
        var src = new File(rootDir, "src");
        for (int i = 0; i < 20; i++) {
            var dir = new File(src, "d-" + UUID.randomUUID());
            for (int d = 0; d < i % 3; d++) {
                dir = new File(dir, "d-" + UUID.randomUUID()); // deep structures
            }
            dir.mkdirs();
            for (int j = 0; j < 10; j++) {
                var file = new File(dir, "f-" + UUID.randomUUID() + ".txt");
                MFile.writeFile(file, "content-" + UUID.randomUUID());
            }
        }

        // zip the structure
        var zip = new File(rootDir, "test.zip");
        var zipStream = new FileOutputStream(zip);
        var zipErrors = Zip.builder().src(src).dstStream(zipStream).build().zip().getErrors();
        zipStream.close();
        assertThat(zipErrors).isEmpty();
        var zipStructure = dirToString(src);

        // unzip the structure with file target
        var target = new File(rootDir, "target");
        var unzipErrors = Zip.builder().src(zip).dst(target).build().unzip().getErrors();
        assertThat(unzipErrors).isEmpty();

        // compare the structures
        var targetStructure = dirToString(target);
        assertThat(targetStructure).isEqualTo(zipStructure);

    }

    private String dirToString(File src) {
        var sb = new StringBuilder();
        dirToString(src, sb);
        return sb.toString();
    }

    private void dirToString(File src, StringBuilder sb) {
        for (File file : Arrays.stream(src.listFiles()).sorted(Comparator.comparing(File::getName))
                .toArray(File[]::new)) {
            if (file.getName().startsWith("."))
                continue;
            if (file.isDirectory()) {
                sb.append(cutFileName(file.getAbsolutePath())).append("\n");
                dirToString(file, sb);
            } else {
                sb.append(cutFileName(file.getAbsolutePath())).append("\n");
                sb.append("  ").append(MFile.readFile(file)).append("\n");
            }
        }
    }

    private String cutFileName(String path) {
        path = path.replace('\\', '/');
        return MString.afterIndex(MString.afterIndex(path, "/zip-test/"), '/');
    }

}
