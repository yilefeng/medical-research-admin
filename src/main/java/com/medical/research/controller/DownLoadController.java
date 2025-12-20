package com.medical.research.controller;

import com.medical.research.util.FileUtil;
import com.medical.research.util.PdfToImageUtil;
import com.medical.research.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/download")
@Tag(name = "下载", description = "下载PDF、roc图片")
public class DownLoadController {

    @Operation(summary = "下载png或pdf", description = "生成DeLong/AUC报告及ROC图")
    @GetMapping
    public void download(
            HttpServletResponse response,
            @Parameter(description = "path", required = true) String path,
            @Parameter(description = "open") @RequestParam(defaultValue = "true") Boolean open,
            @Parameter(description = "png") @RequestParam(defaultValue = "false") Boolean png)
            throws IOException {

        if (png) {
            String pathPng = path.replace(".pdf", ".png");
            path = PdfToImageUtil.convertAllPagesToSinglePng(path, pathPng, 300);
            log.info("png path: {}", path);
        }
        FileUtil.downLoadFile(response, Files.newInputStream(Paths.get(path)), path, open);
    }

}