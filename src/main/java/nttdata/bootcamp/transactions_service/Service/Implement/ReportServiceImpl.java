package nttdata.bootcamp.transactions_service.Service.Implement;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import nttdata.bootcamp.transactions_service.Entity.TransactionDocument;
import nttdata.bootcamp.transactions_service.Repository.TransactionRepository;
import nttdata.bootcamp.transactions_service.Service.ReportService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;


import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF report builder for the last 10 movements of a product using iText.
 */
@Service
@AllArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    private final TransactionRepository repository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Single<Resource> generateMovementsReport(String productId, String cardType) {
        return RxJava3Adapter.fluxToFlowable(
                        repository.findTop10ByProductIdOrderByTransactionDateDesc(productId))
                .toList()
                .map(transactions -> buildMovementsReport(transactions, productId, cardType));
    }

    /**
     * Renders a PDF table of movements (Spanish labels in the PDF content).
     */
    private Resource buildMovementsReport(
            List<TransactionDocument> transactions, String productId, String cardType) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            String cardLabel = cardType != null ? cardType : "Producto";
            addHeader(doc,
                    "Últimos 10 Movimientos — Tarjeta " + cardLabel,
                    "Producto ID: " + productId);

            PdfPTable table = createTable(6,
                    "ID Transacción", "Tipo", "Monto", "Comisión", "Descripción", "Fecha");

            for (TransactionDocument t : transactions) {
                table.addCell(nvl(t.getId()));
                table.addCell(nvl(t.getTransactionType()));
                table.addCell(t.getAmount() != null ? String.format("%.2f", t.getAmount()) : "0.00");
                table.addCell(t.getFee() != null ? String.format("%.2f", t.getFee()) : "0.00");
                table.addCell(nvl(t.getDescription()));
                table.addCell(t.getTransactionDate() != null ? FMT.format(t.getTransactionDate()) : "—");
            }

            if (transactions.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase(
                        "No se encontraron movimientos para este producto.",
                        FontFactory.getFont(FontFactory.HELVETICA, 10)));
                empty.setColspan(6);
                empty.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(empty);
            }

            doc.add(table);
            addFooter(doc, transactions.size());
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating movements report PDF", e);
        } finally {
            doc.close();
        }
        return new ByteArrayResource(out.toByteArray());
    }

    private void addHeader(Document doc, String title, String subtitle) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY);
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.GRAY);

        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(4f);
        doc.add(titlePara);

        Paragraph subPara = new Paragraph(subtitle, subFont);
        subPara.setAlignment(Element.ALIGN_CENTER);
        subPara.setSpacingAfter(16f);
        doc.add(subPara);

        doc.add(Chunk.NEWLINE);
    }

    private PdfPTable createTable(int cols, String... headers) throws DocumentException {
        PdfPTable table = new PdfPTable(cols);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8f);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new BaseColor(33, 97, 140));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6f);
            table.addCell(cell);
        }
        return table;
    }

    private void addFooter(Document doc, int count) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, BaseColor.GRAY);
        Paragraph footer = new Paragraph(
                "\nTotal de registros: " + count
                        + "   |   Generado: " + FMT.format(Instant.now()),
                footerFont);
        footer.setSpacingBefore(12f);
        footer.setAlignment(Element.ALIGN_RIGHT);
        doc.add(footer);
    }

    private String nvl(String val) {
        return val != null ? val : "—";
    }
}
