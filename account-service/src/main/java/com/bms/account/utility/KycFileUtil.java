package com.bms.account.utility;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class KycFileUtil {

    @Value("${kyc.upload.dir}")
    private String uploadDir;

    /**
     * Save MultipartFile to local directory
     */
    // Keep only this in KycFileUtil
    public String saveFile(MultipartFile file, String cifNumber) throws IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String originalFileName = file.getOriginalFilename();
        String extension = "";

        // 1️ Try to get extension from original filename
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } else {
            // 2️ Fallback: try to detect MIME type from file bytes
            String mimeType = file.getContentType(); // e.g., application/pdf, image/jpeg
            if (mimeType != null) {
                if (mimeType.equals("application/pdf")) extension = ".pdf";
                else if (mimeType.equals("image/jpeg")) extension = ".jpg";
                else if (mimeType.equals("image/png")) extension = ".png";
                // add other types if needed
            }
        }

        // 3️ Default extension if still unknown
        if (extension.isEmpty()) extension = ".bin";

        // 4️ Build safe filename
        String safeFileName = cifNumber + "_file_" + System.currentTimeMillis() + extension;
        File dest = new File(dir, safeFileName);
        file.transferTo(dest);

        log.info("Saved KYC file to: {}", dest.getAbsolutePath());
        return dest.getAbsolutePath();
    }


}
