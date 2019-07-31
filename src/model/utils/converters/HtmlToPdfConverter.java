package model.utils.converters;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.File;
import java.io.IOException;

public abstract class HtmlToPdfConverter {
    public static void convertAndSave(String htmlText, File destination) throws IOException {
        PdfWriter writer = new PdfWriter(destination);

        var converterProperties = new ConverterProperties();
        HtmlConverter.convertToPdf(htmlText, writer);
    }

    public static void convertAndSave(String htmlText, String destination) throws IOException {
        PdfWriter writer = new PdfWriter(destination);

        var converterProperties = new ConverterProperties();
        HtmlConverter.convertToPdf(htmlText, writer);
    }
}
