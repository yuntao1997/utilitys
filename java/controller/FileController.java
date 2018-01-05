package com.qb.workstation.controller.system;

import com.qb.workstation.common.CommonConst;
import com.qb.workstation.dto.AbstractOutputDto;
import com.qb.workstation.dto.system.output.FileUploadOutputDto;
import com.qb.workstation.entity.FileInfo;
import com.qb.workstation.service.system.SystemService;
import com.qb.workstation.util.DataUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.jmimemagic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Api(description = "文件管理", tags = "系统管理")
@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private SystemService systemService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${com.qb.file.upload_dir.windows}")
    private String windowsUploadDir;

    @Value("${com.qb.file.upload_dir.linux}")
    private String linuxUploadDir;

    /**
     * 上传成功后返回上传的文件信息
     * @param files
     * @param request
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation(value = "文件上传",notes = "适用场景：文件上传。上传成功将返回一个文件ID，可通过此文件ID对文件进行预览等操作",tags = {"PC端","客户端","技师端"})
    public AbstractOutputDto<FileUploadOutputDto> upload(@ApiParam(name = "files", value = "文件集合") @RequestParam("files") MultipartFile[] files, HttpServletRequest request) {

        if(files == null || files.length == 0){
            return AbstractOutputDto.error("未上传文件，请确认");
        }

        List<File> storeFiles = new ArrayList<>(files.length);
        List<FileInfo> fileInfos = new ArrayList<>(files.length);

        StopWatch stopWatch = new StopWatch("FileController-Upload");
        stopWatch.start();
        try {
            logger.info("文件上传开始.......");
            String osName = System.getProperty("os.name");

            for (MultipartFile file : files) {
                String dir = null;
                File storeFile = null;
                if (osName.indexOf("Windows") != -1) {
                    dir = windowsUploadDir;
                }
                if (osName.indexOf("Linux") != -1) {
                    dir = linuxUploadDir;
                }
                String originalFilename = file.getOriginalFilename();
                String fileName = UUID.randomUUID().toString().replaceAll("-", "") + (originalFilename.indexOf(".") != -1 ? originalFilename.substring(originalFilename.indexOf(".") + 1) : "");
                storeFile = new File(dir + DataUtil.getCurrent().getUid() + "/" + fileName);
                if (!storeFile.exists()) {
                    new File(storeFile.getParent()).mkdirs();
                }

                Path path = storeFile.toPath();

                Files.write(path, file.getBytes());
                storeFiles.add(storeFile);

                FileInfo fileInfo = new FileInfo();
                fileInfo.setName(originalFilename);
                fileInfo.setRealPath(storeFile.getAbsolutePath());
                fileInfo.setFileSize(storeFile.length() * 0.01 / 1024);//保留两位小数
                fileInfo.setTimesOfUse(0);
                fileInfos.add(fileInfo);
            }

        } catch (MultipartException e) {
            logger.error("文件解析错误...", e);
        } catch (IOException e) {
            logger.error("文件上传失败", e);
        }
        stopWatch.stop();
        logger.error("文件上传结束，耗时：{}毫秒，共上传{}个文件...", stopWatch.getTotalTimeMillis(), files.length);

        systemService.saveFileInfos(fileInfos);

        FileUploadOutputDto fileUploadOutputDto = new FileUploadOutputDto();
        fileUploadOutputDto.setDetails(new ArrayList<>(fileInfos.size()));

        FileUploadOutputDto.Detail detail = null;
        for (FileInfo fileInfo : fileInfos) {
            detail = new FileUploadOutputDto.Detail();
            detail.setFileId(fileInfo.getId());
            detail.setFileName(fileInfo.getName());
            detail.setCanPreview(CommonConst.YES);
            if (CommonConst.YES == detail.getCanPreview()) {
                String previewUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/files/preview?fileId=" + fileInfo.getId();
                detail.setPreviewUrl(previewUrl);
            }
            fileUploadOutputDto.getDetails().add(detail);
        }

        return AbstractOutputDto.ok(fileUploadOutputDto);
    }

    @GetMapping("/preview")
    @ApiOperation(value = "文件预览",notes = "适用场景：文件预览",tags = {"PC端","客户端","技师端"})
    public void preview(@ApiParam(name = "fileId",value = "文件ID") @RequestParam("fileId") int fileId, HttpServletResponse response) {

        FileInfo fileInfo = systemService.getFileInfo(fileId);
        if (fileInfo == null) {
            return;
        }
        File file = new File(fileInfo.getRealPath());
        if (!file.exists()) {
            return;
        }
        Magic parser = new Magic() ;
        MagicMatch match = null;
        try {
            match = parser.getMagicMatch(Files.readAllBytes(Paths.get(fileInfo.getRealPath())) );
        } catch (Exception e) {
            logger.error("文件Mime类型获取错误",e);
            return;
        }
        String contentType = match.getMimeType();
        response.setContentType(contentType);
        FileInputStream fis = null;
        OutputStream os = null;
        try {

            fis = new FileInputStream(file);
            os = response.getOutputStream();
            int count = 0;
            byte[] buffer = new byte[1024 * 1024];
            while ((count = fis.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
