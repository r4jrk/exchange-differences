package pl.net.brach;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.text.DecimalFormat;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.net.brach.commons.data.CurrencyRepository;
import pl.net.brach.commons.nbp.NbpClient;
import pl.net.brach.commons.nbp.NbpRate;
import pl.net.brach.commons.ui.Dialogs;
import pl.net.brach.commons.ui.R4TechBannerView;

public class MainWindowController implements Initializable {

    private static final List<String> DATA_FORMATS = Arrays.asList("dd-MM-yyyy", "dd/MM/yyyy", "ddMMyyyy", "dd.MM.yyyy",
            "yyyy-MM-dd", "yyyy/MM/dd", "yyyyMMdd", "yyyy.MM.dd");

    private static final Logger log = LoggerFactory.getLogger(MainWindowController.class);

    private final NbpClient nbpClient = new NbpClient();

    @FXML
    private Button bClose;
    @FXML
    private RadioButton rbPurchase;
    @FXML
    private TextField tbTransactionAmount;
    @FXML
    private DatePicker dpTransactionDate;
    @FXML
    private DatePicker dpInvoiceDate;
    @FXML
    private ComboBox<String> cbCurrencies;
    @FXML
    private StackPane bannerContainer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bannerContainer.getChildren().add(new R4TechBannerView());

        addCurrenciesToComboBox();

        dpInvoiceDate.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent keyEvent) -> {
            if (keyEvent.getCode() == KeyCode.DOWN) {
                dpInvoiceDate.setValue(dpInvoiceDate.getValue().minusDays(1));
                keyEvent.consume();
            }
            if (keyEvent.getCode() == KeyCode.UP) {
                dpInvoiceDate.setValue(dpInvoiceDate.getValue().plusDays(1));
                keyEvent.consume();
            }
        });

        dpTransactionDate.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent keyEvent) -> {
            if (keyEvent.getCode() == KeyCode.DOWN) {
                dpTransactionDate.setValue(dpTransactionDate.getValue().minusDays(1));
                keyEvent.consume();
            }
            if (keyEvent.getCode() == KeyCode.UP) {
                dpTransactionDate.setValue(dpTransactionDate.getValue().plusDays(1));
                keyEvent.consume();
            }
        });

        tbTransactionAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*,")) {
                tbTransactionAmount.setText(newValue.replaceAll("[^\\d,]", ""));
            }
        });

        dpTransactionDate.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 10) {
                // Cap at a full date (dd-MM-yyyy) instead of wiping the whole field.
                dpTransactionDate.getEditor().setText(newValue.substring(0, 10));
            }
        });

        dpInvoiceDate.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 10) {
                dpInvoiceDate.getEditor().setText(newValue.substring(0, 10));
            }
        });

        modifyDatePickers();
    }

    public void modifyDatePickers() {
        dpTransactionDate.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    for (String pattern : DATA_FORMATS) {
                        try {
                            if (date.isAfter(LocalDate.now())) {
                                return DateTimeFormatter.ofPattern(pattern).format(LocalDate.now());
                            } else {
                                return DateTimeFormatter.ofPattern(pattern).format(date);
                            }
                        } catch (DateTimeException dte) {
                            System.out.println("Format Error");
                        }
                    }
                }
                return "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    for (String pattern : DATA_FORMATS) {
                        try {
                            return LocalDate.parse(string, DateTimeFormatter.ofPattern(pattern));
                        } catch (DateTimeParseException ignored) { }
                    }
                    System.out.println("Parse Error");
                }
                return null;
            }
        });

        dpInvoiceDate.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    for (String pattern : DATA_FORMATS) {
                        try {
                            if (date.isAfter(LocalDate.now())) {
                                return DateTimeFormatter.ofPattern(pattern).format(LocalDate.now());
                            } else {
                                return DateTimeFormatter.ofPattern(pattern).format(date);
                            }
                        } catch (DateTimeException dte) {
                            System.out.println("Format Error");
                        }
                    }
                }
                return "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    for (String pattern : DATA_FORMATS) {
                        try {
                            return LocalDate.parse(string, DateTimeFormatter.ofPattern(pattern));
                        } catch (DateTimeParseException ignored) { }
                    }
                    System.out.println("Parse Error");
                }
                return null;
            }
        });
    }

    public void addCurrenciesToComboBox() {
        CurrencyRepository currencyRepository = new CurrencyRepository();

        cbCurrencies.getItems().clear();
        cbCurrencies.getItems().addAll(currencyRepository.getCurrencies());
        cbCurrencies.getSelectionModel().selectFirst();
    }

    @FXML
    private void currencyChosen() { cbCurrencies.getSelectionModel().select(cbCurrencies.getSelectionModel().getSelectedItem()); }

    private LocalDate extractDate(DatePicker datePicker) {
        LocalDate dInvoiceDate = null;
        for (String pattern : DATA_FORMATS) {
            try {
                dInvoiceDate = LocalDate.parse(datePicker.getEditor().getText(), DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignored) { }
        }
        return dInvoiceDate;
    }

    @FXML
    private void okClicked() {
        //Get user input data
        if (dpTransactionDate.getEditor().getText().equals("")) {
            System.out.println("No data was provided. Aborting...");
            return;
        }

        // Capture inputs on the FX thread; only the two network calls run in the background.
        final LocalDate dInvoiceDate = extractDate(dpInvoiceDate);
        final LocalDate dTransactionDate = extractDate(dpTransactionDate);
        final String currency = cbCurrencies.getValue();

        Task<NbpRate[]> fetchRates = new Task<>() {
            @Override
            protected NbpRate[] call() throws Exception {
                NbpRate invoiceRate = nbpClient.fetchRatePrecedingDate(currency, dInvoiceDate);
                NbpRate transactionRate = nbpClient.fetchRatePrecedingDate(currency, dTransactionDate);
                return new NbpRate[]{invoiceRate, transactionRate};
            }
        };

        fetchRates.setOnSucceeded(event -> {
            NbpRate[] rates = fetchRates.getValue();
            showResult(rates[0], rates[1], currency);
        });
        fetchRates.setOnFailed(event -> {
            log.error("Nie udało się pobrać kursów z NBP", fetchRates.getException());
            Dialogs.error("Nie udało się pobrać kursów z NBP",
                    "Sprawdź połączenie z internetem i spróbuj ponownie.");
        });

        Thread worker = new Thread(fetchRates, "nbp-fetch");
        worker.setDaemon(true);
        worker.start();
    }

    /** Runs on the FX thread (Task onSucceeded): builds the summary args and shows the summary. */
    private void showResult(NbpRate invoice, NbpRate transaction, String currency) {
        String invoiceRate = invoice.midPlain();
        String transactionRate = transaction.midPlain();

        double calculatedInvoiceAmount = calculateAmount(invoiceRate);
        double calculatedTransactionAmount = calculateAmount(transactionRate);
        double exchangeRatesDifference = calculatedInvoiceAmount - calculatedTransactionAmount;

        String[] args = new String[13];
        DecimalFormat format = new DecimalFormat("###,##0.00");

        args[0] = format.format(Double.parseDouble(tbTransactionAmount.getText().replace(",", "."))) + " " + currency;
        args[1] = invoice.tableNumber();
        args[2] = invoice.effectiveDate().toString();
        args[3] = invoiceRate.replace(".", ",");
        args[4] = format.format(calculatedInvoiceAmount) + " zł";
        args[5] = transaction.tableNumber();
        args[6] = transaction.effectiveDate().toString();
        args[7] = transactionRate.replace(".", ",");
        args[8] = format.format(calculatedTransactionAmount) + " zł";
        args[9] = String.format("%.2f", exchangeRatesDifference);
        args[10] = getCommentary(exchangeRatesDifference);
        args[11] = format.format(Math.abs(calculatedInvoiceAmount - calculatedTransactionAmount)) + " zł";
        args[12] = currency;

        try {
            ExchangeDifferences.displaySummary(args);
        } catch (IOException e) {
            log.error("Nie udało się otworzyć podsumowania", e);
            Dialogs.error("Błąd", "Nie udało się otworzyć okna podsumowania.");
        }
    }

    private double calculateAmount(String sRate) {
        return Math.round((Double.parseDouble(tbTransactionAmount.getText().replace(",", "."))
                * Double.parseDouble(sRate)) * 100.00) / 100.00;
    }

    private String getCommentary(double exchangeRatesDifference) {
        if (rbPurchase.isSelected()) {
            if (exchangeRatesDifference <= 0) {
                return "Koszt z różnic kursowych w zakupie: ";
            } else {
                return "Przychód z różnic kursowych w zakupie: ";
            }
        } else {
            if (exchangeRatesDifference <= 0) {
                return "Przychód z różnic kursowych w sprzedaży: ";

            } else {
                return "Koszt z różnic kursowych w sprzedaży: ";
            }
        }
    }

    @FXML
    private void closeClicked() {
        Stage stage = (Stage) bClose.getScene().getWindow();
        stage.close();
    }
}
