/*
Copyright 2015 Borys Omelayenko

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package name.borys.website;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test and an illustration how to use this library.
 *
 * @author Borys Omelayenko
 *
 */
public class RunTest {

    @Test
    public void testMain() throws Exception {

        File srcDir = new File("src/test/resources/content");
        File dstDir = new File("target/test/www");
        File expectedDir = new File("src/test/resources/expected");
        Run.main(new String[]{"-c", srcDir.getPath(), "-d", dstDir.getPath()});
        Collection<File> expectedFiles = FileUtils.listFiles(expectedDir, null, true);
        Collection<File> dstFiles = FileUtils.listFiles(dstDir, null, true);
        Assert.assertEquals(expectedFiles.size(), dstFiles.size());

        Iterator<File> dstIterator = dstFiles.iterator();
        for (File expectedFile : expectedFiles) {
            Assert.assertArrayEquals(
                    FileUtils.readLines(expectedFile, CharEncoding.UTF_8).toArray(),
                    FileUtils.readLines(dstIterator.next(), CharEncoding.UTF_8).toArray());
        }
    }
}
