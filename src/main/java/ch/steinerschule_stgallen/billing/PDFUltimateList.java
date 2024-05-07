package ch.steinerschule_stgallen.billing;

import ch.steinerschule_stgallen.model.Model;
import ch.steinerschule_stgallen.model.Sponsor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.splitting.ISplitCharacters;
import com.itextpdf.text.*;


import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class PDFUltimateList {


    public static void createUltimateList(List<Sponsor> sponsors, File targetDir, Model model) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            OutputStream result = new FileOutputStream(targetDir);
            PdfWriter writer = new PdfWriter(result);
            PdfDocument pdfDocument = new PdfDocument(writer);
            pdfDocument.setDefaultPageSize(PageSize.A4.rotate());
            Document doc = new Document(pdfDocument);

            Table table = new Table(10);

            // Add table headers
            Font headerFont = FontFactory.getFont(FontFactory.COURIER, 10, Font.BOLD, BaseColor.BLACK);
            Font dataFont = FontFactory.getFont(FontFactory.COURIER, 10, BaseColor.BLACK);

            String[] headers = {"Titel", "Name", "Vorname", "Email Rechnung Erhalten", "Bezahlstatus"};
            ISplitCharacters noSplit = (text, glyphPos) -> false;

            table.addHeaderCell(new Cell().add(new Paragraph("Titel")).setVerticalAlignment(VerticalAlignment.BOTTOM));
            table.addHeaderCell(new Cell().add(new Paragraph("Name")).setVerticalAlignment(VerticalAlignment.BOTTOM));
            table.addHeaderCell(new Cell().add(new Paragraph("Vorname")).setVerticalAlignment(VerticalAlignment.BOTTOM));
            table.addHeaderCell(new Cell().add(new Paragraph("Email")).setVerticalAlignment(VerticalAlignment.BOTTOM));

            table.addHeaderCell(new Cell().add(new Paragraph("Bezahlt").setRotationAngle(Math.PI / 2).setSplitCharacters(noSplit)).setVerticalAlignment(VerticalAlignment.BOTTOM));
            table.addHeaderCell(new Cell().add(new Paragraph("Email Rechnung").setRotationAngle(Math.PI / 2).setSplitCharacters(noSplit)).setVerticalAlignment(VerticalAlignment.BOTTOM));
            table.addHeaderCell(new Cell().add(new Paragraph("Email Erinnerung").setRotationAngle(Math.PI / 2).setSplitCharacters(noSplit)).setVerticalAlignment(VerticalAlignment.BOTTOM));
            table.addHeaderCell(new Cell().add(new Paragraph("Email Dank").setRotationAngle(Math.PI / 2).setSplitCharacters(noSplit)).setVerticalAlignment(VerticalAlignment.BOTTOM));
            table.addHeaderCell(new Cell().add(new Paragraph("PDF Rechnung").setRotationAngle(Math.PI / 2).setSplitCharacters(noSplit)).setVerticalAlignment(VerticalAlignment.BOTTOM));
            table.addHeaderCell(new Cell().add(new Paragraph("Sponsor Nummer").setRotationAngle(Math.PI / 2).setSplitCharacters(noSplit)).setVerticalAlignment(VerticalAlignment.BOTTOM));


            for(Sponsor sponsor : sponsors) {
                table.addCell(sponsor.getTitle());
                table.addCell(sponsor.getSurname());
                table.addCell(sponsor.getName());
                table.addCell(sponsor.getEmail());
                table.addCell(sponsor.isPayed() ? "1" : "0");
                table.addCell(sponsor.isEmailSent() ? "1" : "0");
                table.addCell(sponsor.isReminded() ? "1" : "0");
                table.addCell(sponsor.isSentThankYou() ? "1" : "0");
                table.addCell(sponsor.isCreatedPDF() ? "1" : "0");
                table.addCell(String.valueOf(sponsor.getId()));


                String sponsorships = sponsor.getSponsorships().stream()
                        .map(sponsorship -> sponsorship.getDetailedString(model))
                        .collect(Collectors.joining("\n"));

                table.addCell(new Cell(sponsor.getSponsorships().size(),10).add(new Paragraph(sponsorships)));
            }
            doc.add(table);


            doc.close();

           // FileOutputStream fileOutputStream = new FileOutputStream(targetDir);
           // fileOutputStream.write(outputStream.toByteArray());
           // fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
