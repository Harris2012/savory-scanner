package cn.savory.scanner;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Find all classes in a package.
 */
public class PackageScanner {

    private Logger logger = LoggerFactory.getLogger(PackageScanner.class);

    public List<String> doScan(String basePackage) throws IOException {

        return doScan(basePackage, getClass().getClassLoader());
    }

    public List<String> doScan(String basePackage, ClassLoader classLoader) throws IOException {

        List<String> classNames = new ArrayList<>();

        if (StringUtil.isEmpty(basePackage)) {
            logger.debug("basePackages is empty.");
            return classNames;
        }

        // replace dots with splashes
        String splashPath = StringUtil.dotToSplash(basePackage);

        // get file path
        URL url = classLoader.getResource(splashPath);
        if (url == null) {
            logger.debug("url is null, basePackage[{}] maybe is error", basePackage);
            return classNames;
        }

        File file = StringUtil.toFile(url);
        if (file == null) {
            logger.warn("file is null, please check basePackage[{}] or URL[{}]", basePackage, url);
            return classNames;
        }

        // Get classes in that package.
        // If the web server unzips the jar file, then the classes will exist in the form of
        // normal file in the directory.
        // If the web server does not unzip the jar file, then classes will exist in jar file.
        List<String> names; // contains the name of the class file. e.g., Apple.class will be stored as "Apple"
        if (isJarFile(file.getName())) {
            // jar file
            names = readFromJarFile(file, splashPath);
        } else {
            // directory
            names = readFromDirectory(file, splashPath);
        }

        for (String name : names) {
            if (isClassFile(name)) {
                name = StringUtil.trimExtension(name);
                name = StringUtil.splashToDot(name);
                classNames.add(name);
            }
        }

        return classNames;
    }

    private List<String> readFromJarFile(File file, String splashedPackageName) throws IOException {
        JarInputStream jarIn = new JarInputStream(new FileInputStream(file));
        JarEntry entry = jarIn.getNextJarEntry();

        List<String> nameList = new ArrayList<>();
        while (null != entry) {
            String name = entry.getName();
            if (name.startsWith(splashedPackageName) && isClassFile(name)) {
                nameList.add(name);
            }

            entry = jarIn.getNextJarEntry();
        }

        return nameList;
    }

    private List<String> readFromDirectory(File file, String splashedPackageName) {

        File[] files = file.listFiles();
        if (files == null) {
            return Lists.newArrayList();
        }

        List<String> nameList = new ArrayList<>();
        for (File subFile : files) {
            if (subFile.isDirectory()) {
                List<String> subDirectoryList = readFromDirectory(subFile, splashedPackageName + "/" + subFile.getName());
                if (subDirectoryList != null) {
                    nameList.addAll(subDirectoryList);
                }
            } else if (isClassFile(subFile.getName())) {
                nameList.add(splashedPackageName + "/" + subFile.getName());
            }
        }

        return nameList;
    }

    private boolean isClassFile(String name) {
        return name.endsWith(".class");
    }

    private boolean isJarFile(String name) {
        return name.endsWith(".jar");
    }
}