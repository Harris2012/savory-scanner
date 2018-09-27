package cn.savory.scanner;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PackageScanner Tester.
 *
 * @author chen.tengfei
 * @version 1.0
 * @since <pre>10/18/2017</pre>
 */
public class PackageScannerTest {

    private static Logger logger = LoggerFactory.getLogger(PackageScannerTest.class);

    @Test
    public void testScan() throws Exception {
        PackageScanner packageScanner = new PackageScanner();
        List<String> classNames = packageScanner.doScan("cn.savory");

        Assert.assertNotNull(classNames);
        Assert.assertTrue(classNames.size() > 0);

        for (String name : classNames) {
            logger.info("find class [{}]", name);
        }
    }
} 
