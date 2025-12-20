package com.medical.research.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfToImageUtil {

    /**
     * 将PDF转换为PNG图片
     * @param pdfPath PDF文件路径
     * @param outputPath 输出图片路径（不含扩展名）
     * @param dpi 图片分辨率
     * @return 生成的图片路径列表
     */
    public static List<String> convertPdfToPng(String pdfPath, String outputPath, int dpi) throws IOException {
        List<String> imagePaths = new ArrayList<>();

        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(i, dpi);
                String imagePath = outputPath + "_page_" + (i + 1) + ".png";
                ImageIO.write(image, "PNG", new File(imagePath));
                imagePaths.add(imagePath);
            }
        }

        return imagePaths;
    }

    public static String convertAllPagesToSinglePng(String pdfPath, String outputPath, int dpi) throws IOException {
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            List<BufferedImage> images = new ArrayList<>();

            // 渲染所有页面
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(i, dpi);
                images.add(image);
            }

            // 计算合并后图片的尺寸
            int totalHeight = images.stream().mapToInt(BufferedImage::getHeight).sum();
            int maxWidth = images.stream().mapToInt(BufferedImage::getWidth).max().orElse(0);

            // 创建新的大图片
            BufferedImage combinedImage = new BufferedImage(maxWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = combinedImage.createGraphics();

            // 填充白色背景
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, maxWidth, totalHeight);

            // 绘制所有页面图片
            int currentY = 0;
            for (BufferedImage image : images) {
                g2d.drawImage(image, 0, currentY, null);
                currentY += image.getHeight();
            }

            g2d.dispose();

            // 保存图片
            ImageIO.write(combinedImage, "PNG", new File(outputPath));
            return outputPath;
        }
    }
}
