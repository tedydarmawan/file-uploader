package rsch.proj.mtx.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import rsch.proj.mtx.exception.StorageException;
import rsch.proj.mtx.exception.StorageFileNotFoundException;
import rsch.proj.mtx.properties.StorageProperties;

@Service
public class FileSystemStorageService implements StorageService{

	private final Path rootLocation;
	
	@Autowired
	public FileSystemStorageService(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
	}
	
	@Override
	public void init() {
		try {
			Files.createDirectories(rootLocation);
		}catch(IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
		
	}

	@Override
	public void store(MultipartFile file) {
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		try {
			if(file.isEmpty()) {
				throw new StorageException("Failed to store empty file " + fileName);
			}
			if(fileName.contains("..")) {
				throw new StorageException("Cannot store file with relative path outside current directory " + fileName);
			}
			try(InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, this.rootLocation.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
			}
		}catch(IOException e) {
			throw new StorageException("Failed to store file " + fileName, e);
		}
	}

	@Override
	public Stream<Path> loadAll() {
		try {
			return Files.walk(this.rootLocation, 1)
					.filter(path -> !path.equals(this.rootLocation))
					.map(this.rootLocation::relativize);
		}catch(IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}
	}

	@Override
	public Path load(String fileName) {
		return rootLocation.resolve(fileName);
	}

	@Override
	public Resource loadAsResource(String fileName) {
		try {
			Path file = load(fileName);
			Resource resource = new UrlResource(file.toUri());
			if(resource.exists() || resource.isReadable()) {
				return resource;
			}else {
				throw new StorageFileNotFoundException("Could not read file: " + fileName);
			}
		}catch(MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + fileName, e);
		}
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}

}
