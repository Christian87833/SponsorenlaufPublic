package ch.steinerschule_stgallen.billing;

import ch.steinerschule_stgallen.model.Main;
import ch.steinerschule_stgallen.model.Model;
import ch.steinerschule_stgallen.model.Sponsor;
import ch.steinerschule_stgallen.model.Sponsorship;
import ch.steinerschule_stgallen.util.Currency;
import ch.steinerschule_stgallen.util.EmailType;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.*;

import okhttp3.Request;

import java.io.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Takes care of sending actual emails via graph api by microsoft
 */
public class EmailService {

    final java.util.List<String> scopes = List.of("https://graph.microsoft.com/.default");

    private final GraphServiceClient<Request> graphClient;


    public EmailService() throws NoKeysException{
        //File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        //System.out.println(jarFile.getAbsolutePath());
        //File file = new File(jarFile.getParentFile().getParent(), "Keys.json");

        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("and nowhhhh?");

        /**
         * Secret Keys in here, do not share!
         */
        String clientId;
        String clientSecret;
        String tenant;
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("Keys.json");
            // Read JSON file as JsonNode
            JsonNode rootNode = objectMapper.readTree(in);

            // Retrieve value associated with the key
            JsonNode valueNodeClient = rootNode.get("clientId");
            JsonNode valueNodeClientSecret = rootNode.get("clientSecret");
            JsonNode valueNodeTenant = rootNode.get("tenant");

            if (valueNodeClient != null && valueNodeClient.isTextual() &&
                    valueNodeClientSecret != null && valueNodeClientSecret.isTextual() &&
                    valueNodeTenant != null && valueNodeTenant.isTextual()) {
                clientId = valueNodeClient.asText();
                clientSecret = valueNodeClientSecret.asText();
                tenant = valueNodeTenant.asText();
            } else {
                System.out.println("Keys not found or value is not a string");
                throw new NoKeysException("No keys found to establish E-Mail Server trust");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new NoKeysException("Error reading the keys to establish E-Mail Server trust", e);
        }
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenant)
                .build();

        TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(scopes, clientSecretCredential);
        graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(tokenCredentialAuthProvider)
                        .buildClient();

    }



    /**
     * Sends an email based on a html template  to the sponsor
     * @param sponsor to receive email
     * @param type  determines template to be used
     * @param model needed for reference
     * @return whether email was sent or not
     */
    public boolean sendEmail(Sponsor sponsor, EmailType type, Model model) {
        try {
            String template = "";
            String bodyContent = "";
            String subject = "";
            LinkedList<Attachment> attachmentsList = new LinkedList<Attachment>();;
            switch (type) {
                case BILL -> {
                    template = "emailTemplate.html";
                    subject = "Ihre Rechnung für den Sponsorenlauf";
                    bodyContent = getBillHTMLString(template, sponsor, model);
                    attachmentsList = attachmentsListBill(sponsor, type, model);
                }
                case REMINDER -> {
                    template = "reminderEmailTemplate.html";
                    subject = "Erinnerung an Ihre Rechnung";
                    bodyContent = getBillHTMLString(template, sponsor, model);
                    attachmentsList = attachmentsListBill(sponsor, type, model);
                }
                case THANK_YOU -> {
                    template = "thanksTemplate.html";
                    subject = "Vielen Dank!";
                    bodyContent = getThankYouHTMLString(template, sponsor, model);
                    attachmentsList = attachmentsListThanks();
                }
            }

            Message message = new Message();
            message.subject = subject;
            ItemBody body = new ItemBody();
            body.contentType = BodyType.HTML;
            body.content = bodyContent;
            message.body = body;
            LinkedList<Recipient> toRecipientsList = new LinkedList<Recipient>();
            Recipient toRecipients = new Recipient();
            EmailAddress emailAddress = new EmailAddress();
            emailAddress.address = sponsor.getEmail();
            toRecipients.emailAddress = emailAddress;
            toRecipientsList.add(toRecipients);
            message.toRecipients = toRecipientsList;

            AttachmentCollectionResponse attachmentCollectionResponse = new AttachmentCollectionResponse();
            attachmentCollectionResponse.value = attachmentsList;
            message.attachments = new AttachmentCollectionPage(attachmentCollectionResponse, null);

            boolean saveToSentItems = true;

            graphClient.users("sponsorenlauf@steinerschule-stgallen.ch")
                .sendMail(UserSendMailParameterSet
                    .newBuilder()
                    .withMessage(message)
                    .withSaveToSentItems(saveToSentItems)
                    .build())
                .buildRequest().post();

            return true;

        } catch (DocumentException | IOException | ClientException e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Creates an attachment list for graphics and PDF to be attached to email
     * @param sponsor for which to get the QR Codes
     * @param type determines template to generate correct PDF attachment
     * @param model needed for reference
     * @return List of Attachments containing the Logo and potentially QR codes for EUR and CHF
     * @throws DocumentException if the template is not found
     * @throws IOException if the Logo can not be read
     */
    private LinkedList<Attachment> attachmentsListBill(Sponsor sponsor, EmailType type, Model model) throws DocumentException, IOException {
        //PDF bytes
        byte[] fileBytes = PDFCreator.createPDFFromTemplate(sponsor, type, model);// PDFCreator.getBillAsByteArray(sponsor);
        // Create an instance of MessageAttachment and specify the file details
        FileAttachment pdfBill = new FileAttachment();
        pdfBill.name = "Rechnung.pdf"; // Set the name of the attachment
        pdfBill.contentId = "Rechnung";
        pdfBill.contentType = "application/pdf"; // Set the content type of the attachment
        pdfBill.contentBytes = fileBytes; // Set the content of the attachment as byte array
        pdfBill.oDataType = "#microsoft.graph.fileAttachment";


        InputStream fi = this.getClass().getClassLoader().getResourceAsStream("Logo_Farbig.png") ;
        byte[] imageByteArrayLogo = new byte[0];
        if (fi != null) {
            imageByteArrayLogo = fi.readAllBytes();
        }
        LinkedList<Attachment> attachmentsList = new LinkedList<Attachment>();

        FileAttachment logo = new FileAttachment();
        logo.name = "Logo.png"; // Set the name of the attachment
        logo.contentId = "logo";
        logo.contentType = "image/png"; // Set the content type of the attachment
        logo.contentBytes = imageByteArrayLogo; // Set the content of the attachment as byte array
        logo.oDataType = "#microsoft.graph.fileAttachment";
        attachmentsList.add(logo);

        QRBillCreator qrBillCreator = new QRBillCreator();
        if(sponsor.getTotal(Currency.EUR) > 0.001) {
            byte[] imageByteArrayEUR = qrBillCreator.pngBytes(sponsor.getTotal(Currency.EUR), Currency.EUR, sponsor);
            FileAttachment qrpng = new FileAttachment();
            qrpng.name = "qrcode.png"; // Set the name of the attachment
            qrpng.contentId = "qrcodeEUR";
            qrpng.contentType = "image/png"; // Set the content type of the attachment
            qrpng.contentBytes = imageByteArrayEUR; // Set the content of the attachment as byte array
            qrpng.oDataType = "#microsoft.graph.fileAttachment";
            attachmentsList.add(qrpng);
        }
        if(sponsor.getTotal(Currency.CHF) > 0.001) {
            byte[] imageByteArrayCHF = qrBillCreator.pngBytes(sponsor.getTotal(Currency.CHF), Currency.CHF, sponsor);
            FileAttachment qrpng = new FileAttachment();
            qrpng.name = "qrcode.png"; // Set the name of the attachment
            qrpng.contentId = "qrcodeCHF";
            qrpng.contentType = "image/png"; // Set the content type of the attachment
            qrpng.contentBytes = imageByteArrayCHF; // Set the content of the attachment as byte array
            qrpng.oDataType = "#microsoft.graph.fileAttachment";
            attachmentsList.add(qrpng);
        }

        attachmentsList.add(pdfBill);
        return attachmentsList;
    }

    /**
     * Creates an attachment list only consisting of the logo
     * @return List of Attachments containing the Logo
     * @throws IOException if the Logo can not be read
     */
    private LinkedList<Attachment> attachmentsListThanks() throws IOException {
        InputStream fi = this.getClass().getClassLoader().getResourceAsStream("Logo_Farbig.png") ;
        byte[] imageByteArrayLogo = new byte[0];
        if (fi != null) {
            imageByteArrayLogo = fi.readAllBytes();
        }
        LinkedList<Attachment> attachmentsList = new LinkedList<Attachment>();

        FileAttachment logo = new FileAttachment();
        logo.name = "Logo.png"; // Set the name of the attachment
        logo.contentId = "logo";
        logo.contentType = "image/png"; // Set the content type of the attachment
        logo.contentBytes = imageByteArrayLogo; // Set the content of the attachment as byte array
        logo.oDataType = "#microsoft.graph.fileAttachment";
        attachmentsList.add(logo);
        return attachmentsList;
    }

    /**
     * Replaces all placeholders in the given HTML String with the sponsors information
     * @param template String of HTML template to replace Info in
     * @param sponsor to get Information for placeholders. (Title, name, ect.)
     * @return the HTML String with replaced Info
     */
    private String getBillHTMLString(String template, Sponsor sponsor, Model model) {
        BufferedReader reader;

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(template);
        reader = new BufferedReader(new InputStreamReader(inputStream));

        String         line;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");


        while(true) {
            try {
                if ((line = reader.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
        temp = temp.replace("[billNum]", String.valueOf(sponsor.getId()));

        StringBuilder sb = new StringBuilder();
        for(Sponsorship s : sponsor.getSponsorships()) {
            sb.append("<tr> <td>");
            sb.append(s.getDetailedString(model));
            sb.append("</td> <td>");
            if(s.getCurrency() == Currency.EUR)
                sb.append("EUR ");
            else
                sb.append("CHF ");
            sb.append(s.getTotal());
            sb.append("</td></tr>");
        }
        temp = temp.replace("[entries]", sb.toString());
        DecimalFormat df = new DecimalFormat("0.00");

        boolean bothCurr = sponsor.getTotal(Currency.CHF) > 0.001 && sponsor.getTotal(Currency.EUR) > 0.001;
        String qrCodeWidth = "";
        if(bothCurr) {
            temp = temp.replace("[css]", "49%");
            temp = temp.replace("[css2]", "left");
            qrCodeWidth = "100%";   //inverse to have smaller qr when only one qr is dosplayed
        } else {
            temp = temp.replace("[css]", "100%");
            temp = temp.replace("[css2]", "none");
            qrCodeWidth = "50%";    //inverse to have smaller qr when only one qr is dosplayed
        }

        String totals = "";
        String qrcodes = "";
        if(sponsor.getTotal(Currency.EUR) > 0.001) {
            totals += "<p><strong>Ges. Summe EUR: </strong>" + df.format(sponsor.getTotal(Currency.EUR)) + "</p>";
            qrcodes += "<div class=\"column\"> <figure><img src=\"cid:qrcodeEUR\" alt=\"Betrag EUR\" " +
                    "style=\"width:"+qrCodeWidth+"\"><figcaption>" + df.format(sponsor.getTotal(Currency.EUR)) +" EUR</figcaption></figure> </div>";
        }
        if(sponsor.getTotal(Currency.CHF) > 0.001) {
            totals += "<p><strong>Ges. Summe CHF: </strong>" + df.format(sponsor.getTotal(Currency.CHF)) + "</p>";
            qrcodes += "<div class=\"column\"> <figure><img src=\"cid:qrcodeCHF\" alt=\"Betrag CHF\" " +
                    "style=\"width:"+qrCodeWidth+"\"><figcaption>CHF " + df.format(sponsor.getTotal(Currency.CHF)) + "</figcaption></figure> </div>";
        }

        temp = temp.replace("[total]", totals);
        temp = temp.replace("[qrcodes]", qrcodes);
        return temp;
    }

    /**
     * Replaces all placeholders in the given HTML String with the sponsors information
     * @param template String of HTML template to replace Info in
     * @param sponsor to get Information for placeholders. (Title, name, ect.)
     * @param model needed for reference
     * @return the HTML String with replaced Info
     */
    private String getThankYouHTMLString(String template, Sponsor sponsor, Model model) {
        BufferedReader reader;
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(template);
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String         line = null;
            StringBuilder  stringBuilder = new StringBuilder();
            String         ls = System.getProperty("line.separator");

            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            DecimalFormat df = new DecimalFormat("0.00");
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
            temp = temp.replace("[laps]", String.valueOf(model.allLaps()));
            temp = temp.replace("[franks]", df.format(model.calculateGrandTotal(Currency.CHF)));

            return temp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
