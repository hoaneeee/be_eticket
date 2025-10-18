package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.service.QrService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QrServiceImpl implements QrService {

    @Override
    public String createPng(String content, String name, int size) {
        try {
            // Ensure the directory exists
            Path directoryPath = new File("uploads/qr").toPath();
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // Create the QR Code
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.MARGIN, 1);  // Set margin for QR code
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hintMap);

            // Create the image
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    img.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF); // Black & White pixels
                }
            }

            // Save the QR code image to the specified file path
            String filePath = "uploads/qr/%s.png".formatted(name);
            File outputFile = new File(filePath);
            ImageIO.write(img, "PNG", outputFile); // Save the image in PNG format

            return filePath;  // Return the relative path of the saved image

        } catch (IOException e) {
            throw new RuntimeException("Error writing the image file: " + e.getMessage(), e);
        } catch (com.google.zxing.WriterException e) {
            throw new RuntimeException("Error generating the QR code: " + e.getMessage(), e);
        }
    }
}
