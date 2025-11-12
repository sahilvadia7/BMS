package com.bms.loan.service.impl.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class OcrService {

    private final String tessDataPath;
    private final int ocrEngineMode;
    private final int pageSegMode;
    private final String language;

    private static final int TARGET_DPI = 250; // 250 is a good compromise
    private static final int MIN_THREADS = 4;  // ensure parallelism even for small page counts


    public OcrService() {
        Tesseract tesseract = new Tesseract();
        // Path where tesseract trained data (.traineddata) files are located
        // On Windows: point to tessdata folder in your project
        try {
            File tessDataFolder = new ClassPathResource("tessdata").getFile();
            tessDataPath = tessDataFolder.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Could not load tessdata folder", e);
        }

        language = "eng+hin";
        ocrEngineMode = 3; // OEM_DEFAULT
        pageSegMode = 6;   // Assume a uniform block of text

        tesseract.setDatapath(tessDataPath);
        tesseract.setLanguage(language);
        tesseract.setOcrEngineMode(ocrEngineMode);
        // auto layout analysis
        tesseract.setPageSegMode(pageSegMode);

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


    private String extractTextFromScannedPdf(PDDocument document) throws Exception {
        PDFRenderer renderer = new PDFRenderer(document);
        int pageCount = document.getNumberOfPages();

        int threads = Math.max(MIN_THREADS, Math.min(pageCount, Runtime.getRuntime().availableProcessors()));
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < pageCount; i++) {
            final int pageIndex = i;
            futures.add(executor.submit(() -> {
                long pageStart = System.currentTimeMillis();
                try {
                    // Render smaller DPI -> big speed improvement
                    BufferedImage image = renderer.renderImageWithDPI(pageIndex, TARGET_DPI, ImageType.RGB);

                    // Quick scale-down if extremely large (keeps OCR accuracy but reduces processing)
                    image = downscaleIfNeeded(image, 2000);

                    BufferedImage processed = preprocessImage(image);

                    // Create per-thread Tesseract instance (thread-safe)
                    Tesseract localTess = createTesseractInstance();
                    // prefer only English for speed; if you need Hindi, switch to "eng+hin"
                    localTess.setLanguage("eng");
                    localTess.setPageSegMode(1); // auto layout

                    return localTess.doOCR(processed);
                } catch (TesseractException te) {
                    te.printStackTrace();
                    return "";
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }));
        }

        // collect results with a timeout to avoid indefinite blocking in edge cases
        StringBuilder extracted = new StringBuilder();
        for (Future<String> f : futures) {
            try {
                // adjust timeout as appropriate; here 60s per page as a safe upper bound
                extracted.append(f.get(60, TimeUnit.SECONDS)).append("\n");
            } catch (TimeoutException te) {
                f.cancel(true);
                System.err.println("OCR task timed out and was cancelled");
            } catch (ExecutionException ee) {
                System.err.println("OCR task failed: " + ee.getMessage());
            }
        }
        // graceful shutdown
        executor.shutdown();
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
        return cleanText(extracted.toString());
    }

    private Tesseract createTesseractInstance() {
        Tesseract local = new Tesseract();
        local.setDatapath(tessDataPath);
        local.setOcrEngineMode(ocrEngineMode);
        local.setLanguage(language);
        local.setPageSegMode(pageSegMode);
        return local;
    }

    private BufferedImage downscaleIfNeeded(BufferedImage src, int maxWidth) {
        int w = src.getWidth();
        if (w <= maxWidth) return src;

        int h = src.getHeight();
        double ratio = (double) maxWidth / w;
        int newW = maxWidth;
        int newH = (int) (h * ratio);

        BufferedImage scaled = new BufferedImage(newW, newH, src.getType());
        java.awt.Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, newW, newH, null);
        g.dispose();
        return scaled;
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
