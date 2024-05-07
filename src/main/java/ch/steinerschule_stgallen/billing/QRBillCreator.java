package ch.steinerschule_stgallen.billing;

import ch.steinerschule_stgallen.model.Sponsor;
import ch.steinerschule_stgallen.model.Sponsorship;
import ch.steinerschule_stgallen.util.Currency;
import net.codecrete.qrbill.generator.*;

/**
 * Creates QR codes for swiss banking apps to fill in information for transaction
 */
public class QRBillCreator {

    private final Address creditor;

    /**
     * Create QR codes for Rudolf Steiner Schule as creditor
     */
    public QRBillCreator() {
        this.creditor = new Address();
        creditor.setName("Rudolf Steiner Schule St.Gallen");
        creditor.setAddressLine1("Rorschacher Strasse 312");
        creditor.setAddressLine2("9016 St. Gallen");
        creditor.setCountryCode("CH");
    }

    /**
     * Generates QR Code in A4 format as PDF byte array
     * @param amount of transaction
     * @param curr currency of transaction
     * @param sponsor for debtor information
     * @return byte array of qr pdf A4
     */
    public byte[] pdfBytes(double amount, Currency curr, Sponsor sponsor) {

        // Setup bill
        Bill bill = new Bill();
        bill.setAccount("CH6509000000900058775");
        bill.setAmountFromDouble(amount);
        bill.setCurrency(curr.toString());

        // Set creditor
        bill.setCreditor(creditor);

        // more bill data
        bill.setUnstructuredMessage("SL 2023, Nr: " + sponsor.getId());

        // Set debtor
        Address debtor = new Address();
        debtor.setName(sponsor.getName() + " " + sponsor.getSurname());
        if(sponsor.getAddress() == null || sponsor.getAddress().isEmpty()) {
            debtor.setAddressLine1("-");
        } else {
            debtor.setAddressLine1(sponsor.getAddress());
        }
        if(sponsor.getPlz() == null || sponsor.getPlz().isEmpty()) {
            debtor.setAddressLine2("-");
        } else {
            debtor.setAddressLine2(sponsor.getPlz() + " " + sponsor.getCity());
        }


        if (sponsor.getLand() == null) {
            debtor.setCountryCode("CH");
        } else {
            String land = sponsor.getLand().toLowerCase().strip();
            if (land.equals("deutschland") || land.equals("dt") || land.equals("germany") || land.equals("d")
                    || land.equals("g") || land.equals("ger"))
                debtor.setCountryCode("DE");
            else
                debtor.setCountryCode("CH");
        }
        bill.setDebtor(debtor);

        // Set output format
        BillFormat format = bill.getFormat();
        format.setGraphicsFormat(GraphicsFormat.PDF);
        format.setOutputSize(OutputSize.A4_PORTRAIT_SHEET);
        format.setLanguage(Language.DE);

        // Generate QR bill
        return QRBill.generate(bill);
    }

    /**
     * Generates QR Code as png byte array
     * @param amount of transaction
     * @param curr currency of transaction
     * @param sponsor for debtor information
     * @return byte array of qr png
     */
    public byte[] pngBytes(double amount, Currency curr, Sponsor sponsor) {

        // Setup bill
        Bill bill = new Bill();
        bill.setAccount("CH6509000000900058775");
        bill.setAmountFromDouble(amount);
        bill.setCurrency(curr.toString());

        // Set creditor
        bill.setCreditor(creditor);

        // more bill data
        bill.setUnstructuredMessage("SL 2023, Nr: " + sponsor.getId());

        // Set debtor
        Address debtor = new Address();
        debtor.setName(sponsor.getName() + " " + sponsor.getSurname());
        if(sponsor.getAddress() == null || sponsor.getAddress().isEmpty()) {
            debtor.setAddressLine1("-");
        } else {
            debtor.setAddressLine1(sponsor.getAddress());
        }
        if(sponsor.getPlz() == null || sponsor.getPlz().isEmpty()) {
            debtor.setAddressLine2("-");
        } else {
            debtor.setAddressLine2(sponsor.getPlz() + " " + sponsor.getCity());
        }


        if (sponsor.getLand() == null) {
            debtor.setCountryCode("CH");
        } else {
            String land = sponsor.getLand().toLowerCase().strip();
            if (land.equals("deutschland") || land.equals("dt") || land.equals("germany") || land.equals("d")
                    || land.equals("g") || land.equals("ger"))
                debtor.setCountryCode("DE");
            else
                debtor.setCountryCode("CH");
        }
        bill.setDebtor(debtor);

        // Set output format
        BillFormat format = bill.getFormat();
        format.setGraphicsFormat(GraphicsFormat.PNG);
        format.setOutputSize(OutputSize.QR_CODE_WITH_QUIET_ZONE);
        format.setLanguage(Language.DE);

        // Generate QR bill
        return QRBill.generate(bill);
    }
}
