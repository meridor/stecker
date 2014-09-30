package ru.meridor.tools.plugin;

import ru.meridor.tools.plugin.impl.DefaultClassesScanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.jar.*;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public final class JarHelper {

    private static final String CLASS_EXTENSION = ".class";

    public static File createJarFile(File directory, String name, Manifest manifest, Class... classes) throws Exception {
        File outputFile = new File(directory, name);
        try (JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(outputFile), manifest)) {
            for (Class currentClass: classes){
                addClass(outputStream, currentClass);
            }
        }
        return outputFile;
    }

    private static void addClass(JarOutputStream outputStream, Class currentClass) throws Exception {
        String className = currentClass.getSimpleName();
        String packageName = currentClass.getPackage().getName();
        String resourceName = File.separator + currentClass.getCanonicalName().replace(".", File.separator) + CLASS_EXTENSION;
        File classFile = new File(currentClass.getResource(resourceName).toURI());
        try (FileInputStream fis = new FileInputStream(classFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            JarEntry entry = new JarEntry(packageName.replace(".", File.separator) + File.separator + className + CLASS_EXTENSION);
            outputStream.putNextEntry(entry);
            while((bytesRead = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    public static JarFile getMockedJarFile(Class... classes) {
        JarFile mockedJarFile = mock(JarFile.class);
        List<JarEntry> jarEntries = Arrays.asList(classes).stream()
                .map(JarHelper::classToJarEntry)
                .collect(Collectors.toList());
        doReturn(new JarFileEnumeration(jarEntries.toArray(new JarEntry[jarEntries.size()])))
                .when(mockedJarFile)
                .entries();
        return mockedJarFile;
    }

    private static class JarFileEnumeration implements Enumeration<JarEntry> {

        private final List<JarEntry> entries = new ArrayList<>();

        private int position = 0;

        private JarFileEnumeration(JarEntry[] entries) {
            this.entries.addAll(Arrays.asList(entries));
        }

        @Override
        public boolean hasMoreElements() {
            return position <= (entries.size() - 1);
        }

        @Override
        public JarEntry nextElement() {
            JarEntry nextEntry = entries.get(position);
            position++;
            return nextEntry;
        }
    }

    private static JarEntry classToJarEntry(Class processedClass) {
        String entryName = processedClass.getCanonicalName()
                .replace(DefaultClassesScanner.PACKAGE_SEPARATOR, DefaultClassesScanner.JAR_DIRECTORY_SEPARATOR)
                .concat(DefaultClassesScanner.CLASS_FILE_EXTENSION);
        return new JarEntry(entryName);
    }

    public static Manifest createManifest(Map<String, String> customFields) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        for (String key: customFields.keySet()){
            manifest.getMainAttributes().putValue(key, customFields.get(key));
        }
        return manifest;
    }

    private JarHelper(){}
}
