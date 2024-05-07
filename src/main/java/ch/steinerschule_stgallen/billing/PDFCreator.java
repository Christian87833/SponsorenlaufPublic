package ch.steinerschule_stgallen.billing;

import ch.steinerschule_stgallen.model.Model;
import ch.steinerschule_stgallen.model.Sponsor;
import ch.steinerschule_stgallen.model.Sponsorship;
import ch.steinerschule_stgallen.util.Currency;
import ch.steinerschule_stgallen.util.EmailType;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.html2pdf.HtmlConverter;



import java.io.*;
import java.text.DecimalFormat;

/**
 * Houses all methods to create PDF attachments and Lists, and functions to stitch together PDFs like Bills and QR Codes
 */
public class PDFCreator {

    /**
     * Creates a PDF containing a single list in table style
     * @param header of table
     * @param lines content of table
     * @param columnWidths in float notation
     * @param targetDir to save the list to
     * @param aditionalText to be written under the table (e.g. further information)
     */
    public static void createList(String header, String[] lines, float[] columnWidths, File targetDir, String aditionalText) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20); // Adjust the margins as needed
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            PdfPTable table = new PdfPTable(lines[0].split("\t").length);
            table.setWidths(columnWidths);

            // Add table headers
            Font headerFont = FontFactory.getFont(FontFactory.COURIER, 10, Font.BOLD, BaseColor.BLACK);
            Font dataFont = FontFactory.getFont(FontFactory.COURIER, 10, BaseColor.BLACK);

            String[] headers = header.split("\t");
            for (String h : headers) {
                PdfPCell cell = new PdfPCell();
                cell.setPhrase(new Phrase(h, headerFont));
                table.addCell(cell);
            }

            // Add table data
            for (String line : lines) {
                String[] rowData = line.split("\t");
                for (String data : rowData) {
                    table.addCell(new Phrase(data, dataFont));
                }
            }

            document.add(table);

            if(!aditionalText.trim().isEmpty()) {
                Paragraph freeText = new Paragraph(aditionalText);
                document.add(freeText);
            }
            document.close();

            FileOutputStream fileOutputStream = new FileOutputStream(targetDir);
            fileOutputStream.write(outputStream.toByteArray());
            fileOutputStream.close();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException(e);
        }

    }



    /**
     * Takes any PDF and stamps the Logo of the Rudolf Steiner Schule in the top left corner
     * @param inputPDF to be stamped
     * @return a stamped version of the original pdf
     * @throws IOException if stamp pdf cannot be read
     * @throws DocumentException if stamp pdf cannot be read
     */
    public static byte[] stampPDFwithHead(byte[] inputPDF) throws IOException, DocumentException {
        // Opening reader on content PDF and creating stamper
        PdfReader contentReader = new PdfReader(inputPDF);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(contentReader, outStream);

        // Opening template pdf
        PdfReader templateReader = new PdfReader("template.pdf");
        PdfImportedPage templatePage = stamper.getImportedPage(templateReader, 1);

        // Looping through pages
        for (int i = 1; i <= contentReader.getNumberOfPages(); i++)
        {
            // Retrieve content page where to apply template
            PdfContentByte contentPage = stamper.getUnderContent(i);

            // Apply template to PDF content
            contentPage.addTemplate(templatePage, 0, 0);
        }

        stamper.close();
        templateReader.close();
        contentReader.close();
        return outStream.toByteArray();

    }

    /**
     * Crates QR Codes for sponsors and stitches them together with a given bill
     * @param res the existing pdf as byte array to attach the qr codes to
     * @param sponsor the sponsor for which to generate the qr codes
     * @return  the byte array of a pdf consisting of the original pdf plus qr codes
     * @throws IOException if templates cannot be accessed properly
     * @throws DocumentException if templates cannot be accessed properly
     */
    public static byte[] attachQRBills(byte[] res, Sponsor sponsor) throws IOException, DocumentException {

        double totalFrank = sponsor.getTotal(Currency.CHF);
        double totalEur = sponsor.getTotal(Currency.EUR);

        boolean printChf = totalFrank > 0.009;
        boolean printEur = totalEur > 0.009;

        //stitch email and qr codes together
        Document document = new Document();

        ByteArrayOutputStream testStream = new ByteArrayOutputStream();
        PdfCopy copy = new PdfCopy(document, testStream);
        document.open();
        PdfReader reader;
        int n;
        reader = new PdfReader(res);
        n = reader.getNumberOfPages();
        for (int page = 0; page < n; ) {
            copy.addPage(copy.getImportedPage(reader, ++page));
        }
        copy.freeReader(reader);
        reader.close();

        if(printEur) {
            QRBillCreator qr = new QRBillCreator();
            byte[] pdfBytes = qr.pdfBytes(sponsor.getTotal(Currency.EUR), Currency.EUR, sponsor);
            reader = new PdfReader(pdfBytes);
            copy.addPage(copy.getImportedPage(reader, 1));
            copy.freeReader(reader);
            reader.close();
        }
        if(printChf) {
            QRBillCreator qr = new QRBillCreator();
            byte[] pdfBytes = qr.pdfBytes(sponsor.getTotal(Currency.CHF), Currency.CHF, sponsor);
            reader = new PdfReader(pdfBytes);
            copy.addPage(copy.getImportedPage(reader, 1));
            copy.freeReader(reader);
            reader.close();
        }

        document.close();

        return testStream.toByteArray();
    }

    /**
     * Takes HTML String and turns it into a well formatted PDF
     * @param htmlString to use for PDF format
     * @return a byte array of the resulting pdf
     * @throws IOException if resources cannot be accessed properly
     * @throws DocumentException if resources cannot be accessed properly
     */
    public static byte[] convertHTMLtoPDF(String htmlString) throws IOException, DocumentException {

        htmlString ="<br><br><br><br>" + htmlString;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(htmlString, outputStream);
        byte[] res = outputStream.toByteArray();
        res = stampPDFwithHead(res);

        return res;
    }

    /**
     * Takes a HTML template, replaces all placeholders with info from the sponsor and transforms the HTML to PDF
     * @param sponsor to use info from for placeholders
     * @param type determines template to use
     * @param model for reference
     * @return the byte array of resulting pdf
     */
    public static byte[] createPDFFromTemplate(Sponsor sponsor, EmailType type, Model model) {
        BufferedReader reader;
        String bodyContent;
        String template;
        switch (type) {
            case BILL -> {
                template = "emailTemplatePDF.html";
            }
            case REMINDER -> {
                template = "reminderEmailTemplatePDF.html";
            }
            case THANK_YOU -> {
                template = "thanksTemplatePDF.html";
            }
            default -> {
                template = "";
            }
        }
        try {
            InputStream inputStream = PDFCreator.class.getClassLoader().getResourceAsStream(template);
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");


            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            String temp = stringBuilder.toString();
            String title;
            if(sponsor.getTitle().trim().equalsIgnoreCase("Herr")) {
                title = "r Herr";
            } else if (sponsor.getTitle().trim().equalsIgnoreCase("Frau")) {
                title = " Frau";
            } else {
                title = "/r " + sponsor.getTitle();
            }
            temp = temp.replace("[title]", title);
            temp = temp.replace("[name]", sponsor.getSurname());

            if (type != EmailType.THANK_YOU) {
                StringBuilder sb = new StringBuilder();
                for (Sponsorship s : sponsor.getSponsorships()) {
                    sb.append("<tr> <td>");
                    sb.append(s.getDetailedString(model));
                    sb.append("</td> <td>");
                    if (s.getCurrency() == Currency.EUR)
                        sb.append("EUR ");
                    else
                        sb.append("CHF ");
                    sb.append(s.getTotal());
                    sb.append("</td></tr>");
                }
                temp = temp.replace("[entries]", sb.toString());
                temp = temp.replace("[billNum]", String.valueOf(sponsor.getId()));

                DecimalFormat df = new DecimalFormat("0.00");

                String totals = "";
                if (sponsor.getTotal(Currency.EUR) > 0.001) {
                    totals += "<p><strong>Ges. Summe EUR: </strong>" + df.format(sponsor.getTotal(Currency.EUR)) + "</p>";
                }
                if (sponsor.getTotal(Currency.CHF) > 0.001) {
                    totals += "<p><strong>Ges. Summe CHF: </strong>" + df.format(sponsor.getTotal(Currency.CHF)) + "</p>";
                }

                temp = temp.replace("[total]", totals);

            } else {
                //it is thank you letter
                DecimalFormat df = new DecimalFormat("0.00");
                temp = temp.replace("[laps]", String.valueOf(model.allLaps()));
                temp = temp.replace("[franks]", df.format(model.calculateGrandTotal(Currency.CHF)));

            }
            bodyContent = temp;
            byte[] pdf = convertHTMLtoPDF(bodyContent);

            if(type != EmailType.THANK_YOU) {
                pdf = attachQRBills(pdf, sponsor);
            }
            return pdf;
        } catch (IOException | DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Takes PDF in byte Array form and writes it to disk
     * @param pdfBytes of pdf to write to disk
     * @param targetDir location to write to
     * @param sponsor determines name
     * @param addOn to add after name, should contain file type (e.g. "_rechnung.pdf")
     */
    public static void writeBytesToPDFFile(byte[] pdfBytes, File targetDir, Sponsor sponsor, String addOn) {
        try{
            File targetFile = new File(targetDir, sponsor.getBillName() + addOn);
            FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
            fileOutputStream.write(pdfBytes);
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
