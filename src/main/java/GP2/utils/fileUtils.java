package GP2.utils;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;

import org.apache.commons.io.FileUtils ;

import java.io.FileOutputStream ;
import java.io.FileInputStream ;
import java.io.FileReader;

public class fileUtils {
    public static List<String> searchWithWc(Path rootDir, String pattern) throws IOException {
	    List<String> matchesList = new ArrayList<String>();

        FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) throws IOException {
                FileSystem fs = FileSystems.getDefault();
                PathMatcher matcher = fs.getPathMatcher(pattern);
                Path name = file.getFileName();
                if (matcher.matches(name)) {
					//System.out.println( "matches \t\t" + name.toString() );
                    matchesList.add(name.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(rootDir, matcherVisitor);
        return matchesList;
    }

	public static List<String> getFilesToDelete(String path, String pattern)
	{
		List<String> actual = null ;
		try {
			Path filesPath = FileSystems.getDefault().getPath(path);
			return searchWithWc(filesPath , pattern);
		} catch (Exception e) {
			System.err.println("Exception::" + e.getMessage()) ;
			return actual ;
		}
	}

	public static boolean deleteAFile (String folder, List<String> files2d)
	{
		final File dir = new File(folder) ;
		final File[] list = dir.listFiles( new FilenameFilter() {
			@Override
			public boolean accept( final File dir, final String name ) {
				return files2d.contains(name) ;
			}
		} );
		for ( final File file : list ) {
			if ( !file.delete() ) {
				System.err.println( "Can't remove " + file.getAbsolutePath() );
			} else
				System.out.println(file + " deleted");
		}
		return true ;
	}

	public static void deleteFile (String path, String pattern)
	{
		List<String> f = getFilesToDelete(path, pattern) ;
		deleteAFile(path, f) ;
	}

	public static File getFile(String fileName)
	throws FileNotFoundException
	{
		File aFile = new File(fileName);
		if (aFile.exists()) return aFile;
		else throw new FileNotFoundException("File  " + fileName + " does not exist.");
	}

	public static boolean isEmpty(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
				return !directory.iterator().hasNext();
			}
		}
		return false;
	}

	public static boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}	

	public static FileWriter getFileWriter(String fName) {
		FileWriter fWriter = null ;

		try {
			String outFilename = fName;
			String dirToUse = Utils.m_settings.getDirToUse() ;
			File f = new File(dirToUse, outFilename);
			FileOutputStream foS = FileUtils.openOutputStream(f) ;
			FileWriter fw = new FileWriter(foS.getFD()) ;
			fWriter = fw;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fWriter ;
	}

	public static FileReader getFileReader(String fName) {
		FileReader fReader = null ;
		try {
			String inFilename = fName;
			String dirToUse = Utils.m_settings.getDirToUse() ;
            File f = new File(dirToUse, inFilename);
            FileInputStream fiS = FileUtils.openInputStream(f) ;
            FileReader fr = new FileReader(fiS.getFD()) ;
            fReader = fr ;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
		return fReader ;
	}

}