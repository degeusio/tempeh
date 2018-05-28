package org.tempeh.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.google.common.io.Files;
import com.google.common.io.Resources;

public class LocalFileCache implements IFileCache{

	//private static final String LOCAL_DIR = "schemas";
	private boolean updateFile = false;
	private String cacheDir = "cache";
	
	public LocalFileCache(String cacheDir, boolean updateFile){
		this.updateFile = updateFile;
		this.cacheDir = cacheDir;
	}
	
	public LocalFileCache(boolean updateFile){
		this.updateFile = updateFile;
	}
	
	public LocalFileCache(String cacheDir){
		this.cacheDir = cacheDir;
	}
	
	public String getCachedFilePath(String fileUri) throws IOException{
		File file = new File(cacheDir + "/" + getFilePath(fileUri));
		if(file.exists())
			return file.getAbsolutePath();
		else{
			//save locally first
			if(!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			
			Resources.asByteSource(new URL(fileUri)).copyTo(Files.asByteSink(file));
			return file.getAbsolutePath();
		}
	}
	
	private String getFilePath(String fileuri){
		try{
			URI uri = new URI(fileuri);
			return uri.getAuthority() + uri.getPath();
		}
		catch(URISyntaxException e){
			return null;
		}
	}
	
	public InputStream getFileInputStream(String fileUri) throws IOException {
		File file = new File(cacheDir + "/" + getFilePath(fileUri));
		if(!updateFile && file.exists())
			return new FileInputStream(file);
		else{
			//save locally first
			if(!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			
			Resources.asByteSource(new URL(fileUri)).copyTo(Files.asByteSink(file));
			return new FileInputStream(file);
		}
	}
	
	public InputStream getFileInputStream(String url, String filename) throws IOException{
		File file = new File(cacheDir + "/" + filename);
		if(!updateFile && file.exists())
			return new FileInputStream(file);
		else{
			//save locally first
			if(!file.getParentFile().exists())
				file.getParentFile().mkdirs();

			Resources.asByteSource(new URL(url)).copyTo(Files.asByteSink(file));
			return new FileInputStream(file);
		}
	}
	
}
