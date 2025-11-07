package com.bms.loan.service.impl.ocr;

import net.sourceforge.tess4j.Tesseract;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class OcrService {

    private final Tesseract tesseract;

    public OcrService() {
        tesseract = new Tesseract();
        // Path where tesseract trained data (.traineddata) files are located
        // On Windows: point to tessdata folder in your project
        try {
            File tessDataFolder = new ClassPathResource("tessdata").getFile();
            tesseract.setDatapath(tessDataFolder.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not load tessdata folder", e);
        }
        tesseract.setLanguage("eng+hin"); // use English
        tesseract.setOcrEngineMode(3);
        // auto layout analysis
        tesseract.setPageSegMode(6);
    }

    /**
     * Extract text from any type of PDF (text or image)
     */
    public String   extractTextFromPdf(MultipartFile file) throws Exception {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Check if PDF already has text
            if (text != null && text.trim().length() > 25) {
                return cleanText(text); // Text-based PDF
            } else {
                // No text probably image PDF → use OCR
                return extractTextFromScannedPdf(document);
            }
        }
    }


    /**
     * Convert each page of image PDF to image → OCR extract text
     */
    private String extractTextFromScannedPdf(PDDocument document) throws Exception {
        PDFRenderer renderer = new PDFRenderer(document);
        StringBuilder extracted = new StringBuilder();

        for (int i = 0; i < document.getNumberOfPages(); i++) {
            BufferedImage image = renderer.renderImageWithDPI(i, 400, ImageType.RGB);
            BufferedImage processed = preprocessImage(image);

            String pageText = tesseract.doOCR(processed);
            extracted.append(pageText).append("\n");
        }
        return cleanText(extracted.toString());
    }

    // Basic image preprocessing: grayscale + contrast + threshold
    private BufferedImage preprocessImage(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();

        // Convert to grayscale
        BufferedImage gray = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();

        // Apply simple binary threshold (brightness adjustment)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = gray.getRGB(x, y) & 0xFF;
                // Enhance contrast slightly
                int adjusted = Math.min(255, Math.max(0, (int)((rgb - 128) * 1.5 + 128)));
                gray.setRGB(x, y, (adjusted << 16) | (adjusted << 8) | adjusted);
            }
        }
        return gray;
    }

    private String cleanText(String text) {
        return text.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

}
