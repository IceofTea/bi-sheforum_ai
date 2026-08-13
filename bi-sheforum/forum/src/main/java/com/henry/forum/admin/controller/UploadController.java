package com.henry.forum.admin.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.util.UploadUtil;

@RestController
@CrossOrigin(origins = {"*"})
public class UploadController {
	@Value("${spring.servlet.multipart.location}")
	private String path;
	//上传文件
	@PostMapping("/upload")
	public APIResult upload(MultipartFile file) {
		String fileName;
		try {
			fileName = UploadUtil.save(file,path);
			return APIResult.ok(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return APIResult.notFound("mzd");

	}
	@PostMapping("/upload/name")
	public APIResult upname(MultipartFile file) {
		String fileName;
		try {
			fileName = UploadUtil.savename(file,path);
			return APIResult.ok(fileName);
		} catch (IOException e) {
			e.printStackTrace();
			return APIResult.notFound(e.getMessage());
		}


	}
	//读取文件内容
	@GetMapping("/upload")
	public APIResult getfile(String file) throws IOException {

		String fileName = path + file;
		String s = Files.readString(Paths.get(fileName));
		return APIResult.ok(s);

	}
	//删除上传
	@DeleteMapping("/upload")
	public APIResult deletefile(String file) throws IOException {

		String fileName = path + file;
		String str = fileName;
		str = str.replace("\\", "\\");
		UploadUtil.deleteAll(Path.of(str));
		return APIResult.ok("删除成功");

	}
	//转成utf-8格式
	@GetMapping("/upload/switch")
	public APIResult switchfile(String file) throws IOException {

		String fileName = path + file;
		String rfileName = path +'r'+ file;
		String str = "";
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "gb2312"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rfileName), "utf-8"));
			while ((str = br.readLine()) != null) {
				bw.write(str);
				bw.newLine();
			}
			bw.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return APIResult.ok(str);

	}

}
