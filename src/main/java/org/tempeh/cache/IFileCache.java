package org.tempeh.cache;

import java.io.IOException;
import java.io.InputStream;

public interface IFileCache {
	//return cached version if it exist, otherwise the original
	InputStream getFileInputStream(String fileUri) throws IOException;
	InputStream getFileInputStream(String url, String filename) throws IOException;
	String getCachedFilePath(String fileUri) throws IOException;
}
