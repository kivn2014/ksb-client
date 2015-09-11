package com.ksb.web.openapi.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;

import com.ksb.openapi.error.BaseSupportException;
import com.ksb.openapi.util.DateUtil;
import com.ksb.web.openapi.entity.OrderSaveEntity;
import com.ksb.web.openapi.mobile.controller.AppApiController;

public class FileUploadUtil{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2661377607233269636L;
	
	private String webServerFilePath = "/data/pic.3gongli.com/img";
	private String imgSuffix = ".jpg";

	public void uploadImage(HttpServletRequest req, HttpServletResponse resp,String filePath,String imgSuffix,OrderSaveEntity osn) {

		this.webServerFilePath = filePath;
		this.imgSuffix = imgSuffix;
		
		if (!ServletFileUpload.isMultipartContent(req)) {
			//log.warn("只能是 multipart/form-data 类型数据");
			throw new BaseSupportException("数据格式异常");
		}

		try {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload fileUpload = new ServletFileUpload(factory);
			factory.setSizeThreshold(80 * 1024);
			List items = fileUpload.parseRequest(req);
			Iterator iterator = items.iterator();
			while (iterator.hasNext()) {
				FileItem item = (FileItem) iterator.next();
				if (!item.isFormField()) {
					
					/*获取上传的原始文件名称*/
					String orginFileName = item.getName();
					
					/*验证文件后缀(仅支持jpg文件)*/
					if(!orginFileName.endsWith(imgSuffix)){
						throw new BaseSupportException("仅支持jpg文件");
					}
					
					String path = FileUtil.getImagePath();
					String imgPath = this.getImgPath(path);
					String imgName = this.getImgName(path);
					
					osn.setImg_id(imgName);
					
					String file = this.getWholeImgPath(imgPath, imgName);
					
					/*文件写入到磁盘*/
					this.write2Image(item, file);
				} else {
					String key = item.getFieldName();
					String value = item.getString();
					System.out.println("fileName:" + key + ",value=" + value);
					
					if ("md5_key".equalsIgnoreCase(key)) {
						osn.setMd5_key(value);;
					} else if("id".equalsIgnoreCase(key)) {
						osn.setWb_id(value);
					} else if("cid".equalsIgnoreCase(key)){
						osn.setCid(value);
					}else if("sp_fee".equalsIgnoreCase(key)){
						osn.setPay_sp_fee(value);
					}else if("buyer_fee".equalsIgnoreCase(key)){
						osn.setFetch_buyer_fee(value);
					}
				}
			}
		} catch (BaseSupportException ex) {
			throw ex;
		} catch (Exception e) {
			throw new BaseSupportException("图片解析异常,请重新上传: "+e.getMessage());
		}

		
	}
	/**
	 * 201310/22/1/random
	 * 
	 * @param path
	 * @return www.xxx.com/201310/22/1
	 */
	private String getImgPath(String path) {
		String imgPath = path.substring(0, path.lastIndexOf(File.separator));
//		String serverPath = AppApiController.IMG_PATH;
		String serverPath = webServerFilePath;
		StringBuilder result = new StringBuilder();
		result.append(serverPath).append(File.separator).append(imgPath);
		return result.toString();
	}

	/**
	 * 201310/22/1/random
	 * 
	 * @param path
	 * @return 201310_22_1_random
	 */
	private String getImgName(String path) {
		return StringUtils.replace(path, File.separator, "_");// 201310_22_1_random
	}
	private void write2Image(FileItem item, String filePath) {
		try {
			BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
			InputStream in = item.getInputStream();
			int length = 0;
			byte[] buf = new byte[1024];
			System.out.println("获取上传文件的大小：" + item.getSize());
			// in.read(buf) 每次读到的数据存放在 buf 数组中
			while ((length = in.read(buf)) != -1) {
				outStream.write(buf, 0, length);
			}
			in.close();
			outStream.flush();
			outStream.close();
		} catch (Exception e) {
			System.out.println("生成上传图片异常 "+e);
//			throw new AppException(AppExceptionEnum.PRASE_IMAGE_ERROR);
		}
	}
	/**
	 * 获得图片完整路径
	 * 
	 * @param imgPath
	 *            www.xxx.com/201310/22/1
	 * @param imgName
	 *            201310_22_1_random
	 * @return www.xxx.com/201310/22/1/201310_22_1_random.jpg
	 */
	private String getWholeImgPath(String imgPath, String imgName) {
		File file = new File(imgPath);
		if (!file.exists()) {
			synchronized (this.getClass()) {
				if (!file.exists()) {
					boolean cr = file.mkdirs();
					//System.out.println(file.getPath()+"==创建结果："+cr);
				}
			}
		}
		StringBuilder path = new StringBuilder();
		path.append(imgPath).append(File.separator).append(imgName).append(imgSuffix);
//		path.append(imgPath).append(File.separator).append(imgName).append(".jpg");
		return path.toString();
	}

	/**
	 * 201310_22_1_random
	 * 
	 * @param imageId
	 * @return
	 */
	private String getImgPathByImageId(String imageId) {
		String path = webServerFilePath;
		if (StringUtils.isEmpty(imageId)) {
			System.out.println("imageId参数不能为空");
			//throw new AppException(AppExceptionEnum.PARAM_FORMAT_EXP);
		}

		String imgName = StringUtils.replace(imageId, "_", File.separator);
		String resultPath = imgName.substring(0, imgName.lastIndexOf(File.separator));
		StringBuilder imgPath = new StringBuilder(path);
		imgPath.append(File.separator).append(resultPath).append(File.separator).append(imageId);
		if (!imageId.endsWith(imgSuffix)) {
			imgPath.append(imgSuffix);
		}
		return imgPath.toString();
	}

	// md5验证
	public boolean checkImageMD5(String imageId, String md5Str) {
		// String path = getServletContext().getRealPath("/upload");
		// String filePath = path + File.separator + imageId + IMAGE_SUFFIX;
		String filePath = this.getImgPathByImageId(imageId);
		return FileUtil.checkImageFile(filePath, md5Str);
	}
//	// 获取生成图片路径
//	@SuppressWarnings("unused")
//	private String getFilePath() {
//		String currentTime = DateUtil.convertDateToString(new Date(), "yyMMddHHmmss");
//		int rand = (int) ((99999 - 10000 + 1) * Math.random() + 10000);
//		String fileName = currentTime + rand;
//		String path = this.getServletContext().getRealPath("/upload");
//		// String imageDir = AppPropertiesUtil.readValue(path, "IMAGE_DIR");
//		// imageDir = path + imageDir;
//
//		String imageName = fileName + imgSuffix;
//		return path + File.separator + imageName;
//	}	
}
