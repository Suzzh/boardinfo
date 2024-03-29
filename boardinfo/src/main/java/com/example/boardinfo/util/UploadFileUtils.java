package com.example.boardinfo.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

public class UploadFileUtils {
	//로깅
	private static final Logger logger
	=LoggerFactory.getLogger(UploadFileUtils.class);
	
	public static String uploadFile(String uploadPath
			, String originalName, byte[] fileData) throws Exception {
		//uuid 발급
		UUID uid = UUID.randomUUID();
		String savedName = uid.toString() + "_" + originalName;
		//업로드할 디렉토리 생성
		String savedPath = calcPath(uploadPath);
		File target = new File(uploadPath+savedPath, savedName);
		//임시 디렉토리에 업로드된 파일을 지정된 디렉토리로 복사
		FileCopyUtils.copy(fileData, target);
		//파일의 확장자 검사(ex : a.jpg / aaa.bbb.ccc.jpg)
		String formatName = originalName.substring(originalName.lastIndexOf(".")+1);
		String uploadedFileName = null;
		//이미지 파일인 경우 : 썸네일(작은 이미지)을 생성
		if(MediaUtils.getMediaType(formatName) != null) {
			//썸네일 생성
			uploadedFileName = makeThumbnail(uploadPath, savedPath, savedName);
		}else {//이미지 파일이 아닌경우
			uploadedFileName = noImg(uploadPath, savedPath, savedName);
		}
		return uploadedFileName;
	}

	//이미지 파일이 아닌 파일처리 메소드
	public static String noImg(String uploadPath, String path
			, String fileName) throws Exception {
		//File.separator 는 이름 구분자로서 예를 들어 윈도우의 경우 upload\\test.txt
		//리눅스는 upload/text.txt라고 쓰는데, '\', '/'를 따로 신경 안써도  separator가
		//OS에 따라 자동으로 처리한다.
		String iconName = uploadPath + path + File.separator+fileName;
		// path값을 포함한 파일이름을 리턴, File.separatorChar는 File.separator와 같은 기능의
		// 이름구분자이지만 리턴값을 따로 정할 수 있다.
		return iconName.substring(uploadPath.length())
				.replace(File.separatorChar, '/');
	}

	private static String makeThumbnail(String uploadPath, String path, 
			String fileName) throws Exception {
		//원본 이미지를 읽기 위한 버퍼
		BufferedImage sourceImg = ImageIO.read(new File(uploadPath + path, fileName));
		//100픽셀 단위의 썸네일 생성
		BufferedImage destImg = Scalr.resize(sourceImg, Scalr.Method.AUTOMATIC
				, Scalr.Mode.FIT_TO_HEIGHT, 100);
		//썸네일의 이름
		String thumbnailName = uploadPath + path + File.separator + "s_" + fileName;
		File newFile = new File(thumbnailName);
		String formatName = fileName.substring(fileName.lastIndexOf(".") + 1);
		//썸네일 생성
		ImageIO.write(destImg, formatName.toUpperCase(), newFile);
		//썸네일 이름 리턴
		return thumbnailName.substring(
				uploadPath.length()).replace(File.separatorChar, '/');
	}

	//날짜 처리 (2023년폴더 / 00월폴더 / 00일 폴더 생성)
	public static String calcPath(String uploadPath) {
		Calendar cal = Calendar.getInstance();
		String yearPath = File.separator + cal.get(Calendar.YEAR);
		String monthPath = yearPath + File.separator + 
				new DecimalFormat("00").format(cal.get(Calendar.MONTH) + 1);
		String datePath = monthPath + File.separator + 
				new DecimalFormat("00").format(cal.get(Calendar.DATE));
		makeDir(uploadPath, yearPath, monthPath, datePath);
		logger.info(datePath);
		return datePath;
	}

	//디렉토리 생성 2023-04-17
	public static void makeDir(String uploadPath, String... paths) {
		// String... 은 가변사이즈 매개변수(배열의 요소가 몇개등 상관없이 처리)
		// 디렉토리가 존재하면 skip
		if (new File(paths[paths.length - 1]).exists()) {
			return;
		}
		for (String path : paths) {
			File dirPath = new File(uploadPath + path);
			if(!dirPath.exists()) {
				dirPath.mkdir();//디렉토리 생성
			}
		}
	}
}
