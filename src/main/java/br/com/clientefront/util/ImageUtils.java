package br.com.clientefront.util;

import org.primefaces.model.UploadedFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

public class ImageUtils {

    public static String detectImageType(byte[] imageBytes) throws IOException {
        if (imageBytes.length < 8) {
            throw new IOException("Arquivo de imagem muito pequeno ou inválido");
        }

        byte[] pngSignature = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (Arrays.equals(Arrays.copyOfRange(imageBytes, 0, 8), pngSignature)) {
            return "image/png";
        }
        byte[] jpegSignature = {(byte) 0xFF, (byte) 0xD8};
        if (Arrays.equals(Arrays.copyOfRange(imageBytes, 0, 2), jpegSignature)) {
            return "image/jpeg";
        }

        throw new IOException("Formato de imagem não suportado (use PNG, JPG ou JPEG)");
    }

    public static String convertUploadedImageToBase64(UploadedFile uploadedFile){
        if (uploadedFile == null || uploadedFile.getSize() == 0) {
            return null;
        }
        byte[] fileContent = uploadedFile.getContents();
        String base64String = Base64.getEncoder().encodeToString(fileContent);
        String mimeType = uploadedFile.getContentType();
        return "data:" + mimeType + ";base64," + base64String;
    }
}