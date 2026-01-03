package GP2.utils;

import java.util.ArrayList;
import java.util.List;
import javax.json.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class fileUtils {

    // Search for files matching a pattern in a directory
    public static List<String> searchFilesWithPattern(Path rootDir, String pattern) throws IOException {
        List<String> matchedFiles = new ArrayList<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);

        FileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) throws IOException {
                if (matcher.matches(file.getFileName())) {
                    matchedFiles.add(file.getFileName().toString());
                }
                return FileVisitResult.CONTINUE;
            }
        };

        Files.walkFileTree(rootDir, fileVisitor);
        return matchedFiles;
    }

    // Get list of files to delete based on a pattern
    public static List<String> getFilesToDelete(String directoryPath, String pattern) {
        try {
            Path path = FileSystems.getDefault().getPath(directoryPath);
            return searchFilesWithPattern(path, pattern);
        } catch (IOException e) {
            System.err.println("Exception::" + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Delete specified files in a folder
    public static boolean deleteFiles(String folderPath, List<String> filesToDelete) {
        File directory = new File(folderPath);
        File[] files = directory.listFiles((dir, name) -> filesToDelete.contains(name));

        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    System.err.println("Can't remove " + file.getAbsolutePath());
                } else {
                    System.out.println(file + " deleted");
                }
            }
        }
        return true;
    }

    // Delete files matching a pattern in a directory
    public static void deleteFilesByPattern(String directoryPath, String pattern) {
        List<String> filesToDelete = getFilesToDelete(directoryPath, pattern);
        deleteFiles(directoryPath, filesToDelete);
    }

    // Get a file by name, throw exception if not found
    public static File getFile(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        if (file.exists()) {
            return file;
        } else {
            throw new FileNotFoundException("File " + fileName + " does not exist.");
        }
    }

    // Check if a directory is empty
    public static boolean isDirectoryEmpty(Path directoryPath) throws IOException {
        if (Files.isDirectory(directoryPath)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directoryPath)) {
                return !directoryStream.iterator().hasNext();
            }
        }
        return false;
    }

    // Recursively delete a directory
    public static boolean deleteDirectoryRecursively(File directory) {
        File[] contents = directory.listFiles();
        if (contents != null) {
            for (File file : contents) {
                deleteDirectoryRecursively(file);
            }
        }
        return directory.delete();
    }

    // Get a FileWriter for a file
    public static FileWriter createFileWriter(String fileName) {
        try {
            String directory = Utils.m_settings.getDirToUse();
            File file = new File(directory, fileName);
            FileOutputStream fileOutputStream = FileUtils.openOutputStream(file);
            return new FileWriter(fileOutputStream.getFD());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Get a FileReader for a file
    public static FileReader createFileReader(String fileName) {
        try {
            String directory = Utils.m_settings.getDirToUse();
            File file = new File(directory, fileName);
            FileInputStream fileInputStream = FileUtils.openInputStream(file);
            return new FileReader(fileInputStream.getFD());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    // Write a JSON object to a file
    public static void writeJsonToFile(String fileName, JsonObject jsonObject) {
        try (FileWriter file = createFileWriter(fileName)) {
            JSONObject json = new JSONObject(jsonObject);
            file.write(json.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}