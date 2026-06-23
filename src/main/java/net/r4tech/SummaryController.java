package net.r4tech;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.OrientationRequested;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.r4tech.commons.ui.Dialogs;

public class SummaryController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(SummaryController.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH);

    @FXML
    private AnchorPane summaryInterior;
    @FXML
    private Button bClose;
    @FXML
    private Button bPrint;
    @FXML
    private Button bCopy;
    @FXML
    private Label invoiceTableNumber;
    @FXML
    private Label invoiceRateDate;
    @FXML
    private Label invoiceAmount;
    @FXML
    private Label invoiceRate;
    @FXML
    private Label invoiceCalculatedAmount;
    @FXML
    private Label transactionTableNumber;
    @FXML
    private Label transactionRateDate;
    @FXML
    private Label transactionAmount;
    @FXML
    private Label transactionRate;
    @FXML
    private Label transactionCalculatedAmount;
    @FXML
    private Label exchangeDifferencesComment;
    @FXML
    private Label exchangeDifferenceAmount;

    public String currency;

    @Override
    public void initialize(URL url, ResourceBundle rb) { }

    void generateSummary(String[] args) {
        invoiceTableNumber.setText(args[1]);
        invoiceRateDate.setText(LocalDate.parse(args[2]).format(DATE_FORMAT));
        invoiceAmount.setText(args[0]);
        invoiceRate.setText(args[3]);
        invoiceCalculatedAmount.setText(args[4]);

        transactionTableNumber.setText(args[5]);
        transactionRateDate.setText(LocalDate.parse(args[6]).format(DATE_FORMAT));
        transactionAmount.setText(args[0]);
        transactionRate.setText(args[7]);
        transactionCalculatedAmount.setText(args[8]);

        exchangeDifferencesComment.setText(args[10]);
        exchangeDifferenceAmount.setText(args[11]);

        currency = args[12];
    }

    @FXML
    private void saveToFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Różnice kursowe - zapisz podsumowanie");
        fileChooser.setInitialDirectory(new File("C:/"));
        fileChooser.setInitialFileName("podsumowanie");

        ObservableList<FileChooser.ExtensionFilter> extensionsFilter = fileChooser.getExtensionFilters();
        extensionsFilter.add(new FileChooser.ExtensionFilter("Plik PNG", ".png"));
        extensionsFilter.add(new FileChooser.ExtensionFilter("Plik TXT", ".txt"));

        Stage stage = new Stage();

        File fileChosen = fileChooser.showSaveDialog(stage);

        if (fileChosen != null) {
            Optional<String> extension = Optional.of(fileChosen.getName())
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(fileChosen.getName().lastIndexOf(".") + 1));

            if (extension.isPresent()) {
                if (extension.get().equals("png")) {
                    saveAsImage(fileChosen);
                } else {
                    saveAsText(fileChosen);
                }
            } else {
                System.out.println("Bład podczas zapisywania pliku...");
            }
        }
    }

    private void saveAsImage(File file) throws IOException {
        WritableImage image = summaryInterior.snapshot(new SnapshotParameters(), null);
        File imageFile = new File(file.getAbsolutePath());
        ImageIO.write((SwingFXUtils.fromFXImage(image, null)), "png", imageFile);
    }

    private void saveAsText(File file) throws IOException {
        ArrayList<String> fileContents = generateLabel(false);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String textLine : fileContents) {
                writer.write(textLine + System.lineSeparator());
            }
        }
    }

    @FXML
    private void copyImageToClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        Image image = summaryInterior.snapshot(new SnapshotParameters(), null);
        content.putImage(image);
        clipboard.setContent(content);
    }

    /**
     * Returns an ArrayList of Strings that can be used later to be printed/copied/saved
     * @param isToBePrinted determining whether the label is to be prepared for printing
     *                      or being copied to clipboard/saved to file
     * @return the list of
     */
    private ArrayList<String> generateLabel(boolean isToBePrinted) {
        ArrayList<String> stringArrayList = new ArrayList<>();

        String finalLineBegginning = "  ";

        if (isToBePrinted) {
            stringArrayList.add(" ----------------------------");
            stringArrayList.add("       Różnice kursowe");
            stringArrayList.add(" ----------------------------");
            stringArrayList.add("           Faktura");
            stringArrayList.add("     Kurs 1 " + currency + " = " + invoiceRate.getText());
            stringArrayList.add("    wg tab.: " + invoiceTableNumber.getText());
            stringArrayList.add("       z dn. " + invoiceRateDate.getText());
            stringArrayList.add("            - - -");
            stringArrayList.add("   " + invoiceAmount.getText() + " * " + invoiceRate.getText());
            stringArrayList.add("   = " + invoiceCalculatedAmount.getText());
            stringArrayList.add("           Zapłata");
            stringArrayList.add("     Kurs 1 " + currency + " = " + transactionRate.getText());
            stringArrayList.add("    wg tab.: " + transactionTableNumber.getText());
            stringArrayList.add("       z dn. " + transactionRateDate.getText());
            stringArrayList.add("            - - -");
            stringArrayList.add("   " + transactionAmount.getText() + " * " + transactionRate.getText());
            stringArrayList.add("   = " + transactionCalculatedAmount.getText());
            stringArrayList.add(" ----------------------------");
        } else {
            stringArrayList.add("--------------------------------------");
            stringArrayList.add("            Różnice kursowe");
            stringArrayList.add("--------------------------------------");
            stringArrayList.add("                Faktura");
            stringArrayList.add("    Numer tabeli: " + invoiceTableNumber.getText());
            stringArrayList.add("    Data kursu: " + invoiceRateDate.getText());
            stringArrayList.add("    Kwota w walucie: " + invoiceAmount.getText());
            stringArrayList.add("    Kurs: " + invoiceRate.getText());
            stringArrayList.add("    Przeliczona kwota: " + invoiceCalculatedAmount.getText());
            stringArrayList.add("-------------------------------------");
            stringArrayList.add("               Zapłata");
            stringArrayList.add("    Numer tabeli: " + transactionTableNumber.getText());
            stringArrayList.add("    Data kursu: " + transactionRateDate.getText());
            stringArrayList.add("    Kwota w walucie: " + transactionAmount.getText());
            stringArrayList.add("    Kurs: " + transactionRate.getText());
            stringArrayList.add("    Przeliczona kwota: " + transactionCalculatedAmount.getText());
            stringArrayList.add("-------------------------------------");

            finalLineBegginning = finalLineBegginning + "  ";
        }

        if (exchangeDifferencesComment.getText().charAt(0) == 'K') { //Cost
            stringArrayList.add(finalLineBegginning + exchangeDifferencesComment.getText().substring(0, 24));
            stringArrayList.add(finalLineBegginning + exchangeDifferencesComment.getText().substring(25)
                    + exchangeDifferenceAmount.getText());
        } else { //Income
            stringArrayList.add(finalLineBegginning + exchangeDifferencesComment.getText().substring(0, 27));
            stringArrayList.add(finalLineBegginning + exchangeDifferencesComment.getText().substring(28)
                    + exchangeDifferenceAmount.getText());
        }

        return stringArrayList;
    }

    @FXML
    private void printLabel() throws PrinterException {
        ArrayList<String> stringArrayListToPrint = generateLabel(true);

        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        pras.add(OrientationRequested.PORTRAIT);
        pras.add(new MediaPrintableArea(0, 0, LabelPrint.PRINT_PAGE_HEIGHT, LabelPrint.PRINT_PAGE_WIDTH, MediaPrintableArea.MM));
        pras.add(new JobName(ExchangeDifferences.R4_TECH_TITLE + " - Dokument", null));

        PrinterJob printerJob = PrinterJob.getPrinterJob();

        PrintService defaultService = printerJob.getPrintService(); // null when no printer is installed
        String defaultName = defaultService != null ? defaultService.getName() : "";
        PrintService printService = defaultName.equals(ExchangeDifferences.PRIMARY_PRINTER_NAME)
                ? getPrintService(ExchangeDifferences.PRIMARY_PRINTER_NAME)
                : getPrintService(ExchangeDifferences.SECONDARY_PRINTER_NAME);

        if (printService == null) {
            log.warn("Nie znaleziono drukarki etykiet ({} ani {})",
                    ExchangeDifferences.PRIMARY_PRINTER_NAME, ExchangeDifferences.SECONDARY_PRINTER_NAME);
            Dialogs.error("Nie znaleziono drukarki",
                    "Nie znaleziono drukarki etykiet: " + ExchangeDifferences.PRIMARY_PRINTER_NAME
                            + " ani " + ExchangeDifferences.SECONDARY_PRINTER_NAME + ".");
            return;
        }
        printerJob.setPrintService(printService);

        // stringArrayListToPrint is split to two lists, to generate two labels to be printed.
        // Anyone knows how to fix that and just let the printing handle it?
        // Reversed order (stringArrayListToPrintPageSecond) as first so the labels came out from the printer nicely
        List<String> stringArrayListToPrintPageSecond = stringArrayListToPrint.subList(10, 20);
        stringArrayListToPrintPageSecond.add("WORKAROUND STRING FOR CORRECT PRINTING");

        printerJob.setPrintable(new LabelPrint(new ArrayList(stringArrayListToPrintPageSecond)));
        printerJob.print(pras);

        List<String> stringArrayListToPrintPageFirst = stringArrayListToPrint.subList(0, 10);
        stringArrayListToPrintPageFirst.add("WORKAROUND STRING FOR CORRECT PRINTING");

        printerJob.setPrintable(new LabelPrint(new ArrayList(stringArrayListToPrintPageFirst)));
        printerJob.print(pras);
    }

    private static PrintService getPrintService(String printerName) {
        PrintService printService = null;
        PrintService[] printServices = PrinterJob.lookupPrintServices();

        for (PrintService service : printServices) {
            if (service.getName().equals(printerName)) {
                printService = service;
            }
        }
        return printService;
    }

    @FXML
    private void tooltipCopied() {
        Point2D p = bCopy.localToScene(15, 15);

        final Tooltip customTooltip = new Tooltip();
        customTooltip.setText("Skopiowano jako obrazek");

        bCopy.setTooltip(customTooltip);
        customTooltip.setAutoHide(true);

        customTooltip.show(bCopy, p.getX()
                + bCopy.getScene().getX() + bCopy.getScene().getWindow().getX(), p.getY()
                + bCopy.getScene().getY() + bCopy.getScene().getWindow().getY());
    }

    @FXML
    private void tooltipPrinting() {
        Point2D p = bPrint.localToScene(15, 15);

        final Tooltip customTooltip = new Tooltip();
        customTooltip.setText("Wysłano do druku");

        bPrint.setTooltip(customTooltip);
        customTooltip.setAutoHide(true);

        customTooltip.show(bPrint, p.getX()
                + bPrint.getScene().getX() + bPrint.getScene().getWindow().getX(), p.getY()
                + bPrint.getScene().getY() + bPrint.getScene().getWindow().getY());
    }

    @FXML
    private void closeClicked() {
        Stage stage = (Stage) bClose.getScene().getWindow();
        stage.close();
    }
}
