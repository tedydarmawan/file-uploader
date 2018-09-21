package rsch.proj.mtx.controller;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import rsch.proj.mtx.exception.StorageFileNotFoundException;
import rsch.proj.mtx.service.StorageService;

@Controller
public class FileUploaderController {
	
	private final StorageService storageService;
	
	@Autowired
	public FileUploaderController(StorageService storageService) {
		this.storageService = storageService;
	}
	
	@GetMapping("/images")
	public ResponseEntity<List<String>> listUploadedFiles() {
		Stream<Path> images = storageService.loadAll();
		List<String> fileNames = images.map(path -> MvcUriComponentsBuilder
				.fromMethodName(FileUploaderController.class, "serveFile", path.getFileName().toString()).build().toString()).collect(Collectors.toList());
		return ResponseEntity.ok().body(fileNames);
			
	}
	
	@GetMapping("/images/{fileName:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
		Resource file = storageService.loadAsResource(fileName);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}
	
	@PostMapping("/upload")
	public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
		try {
			storageService.store(file);
			return ResponseEntity.status(HttpStatus.OK).body("You successfully uploaded " + file.getOriginalFilename() + "!");
		}catch(Exception e) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Fail to upload " + file.getOriginalFilename() + "!");
		}
	}
	
	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}
}
