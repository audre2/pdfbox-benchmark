package br.com.benchmark.pdfbox;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.Random;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

@Service
public class PdfBenchmarkService {

    private final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private final PDType1Font FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    public void generateTextPdf(Map<String, String> data) {
        String cpf = data.get("cpf");
        String filePath = "output/text-" + cpf + ".pdf";

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
                stream.beginText();
                stream.setFont(FONT_BOLD, 16);
                stream.setLeading(20f);
                stream.newLineAtOffset(50, 700);

                stream.showText("Relatório de Atividades");
                stream.newLine();
                stream.setFont(FONT_REGULAR, 12);
                stream.showText("Nome: " + data.get("nome"));
                stream.newLine();
                stream.showText("CPF: " + cpf);
                stream.newLine();
                stream.showText("Data: " + data.get("data"));
                stream.newLine();
                stream.showText("Status: " + data.get("status"));
                stream.endText();
            }

            doc.save(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fillTemplatePdf(Map<String, String> data) {
        String cpf = data.get("cpf");
        String filePath = "output/template-output-" + cpf + ".pdf";
        String templatePath = "src/main/resources/template.pdf";
        String signaturePath = "src/main/resources/assinatura-base64.txt";

        try (PDDocument doc = Loader.loadPDF(new File(templatePath))) {
            PDPage page = doc.getPage(0);

            try (PDPageContentStream stream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
                stream.beginText();
                stream.setFont(FONT_REGULAR, 12);
                stream.newLineAtOffset(100, 700);
                stream.showText(data.get("nome"));
                stream.endText();

                stream.beginText();
                stream.setFont(FONT_REGULAR, 12);
                stream.newLineAtOffset(100, 670);
                stream.showText(cpf);
                stream.endText();

                stream.beginText();
                stream.setFont(FONT_REGULAR, 12);
                stream.newLineAtOffset(180, 640);
                stream.showText(data.get("dataNascimento"));
                stream.endText();

                byte[] imageBytes = Base64.getDecoder().decode(Files.readString(Paths.get(signaturePath)));
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, imageBytes, "assinatura");
                stream.drawImage(pdImage, 40, 510);
            }

            doc.save(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateMultiPageWithTable() {
        String filePath = "output/multipage-" + new Random().nextInt(999999) + ".pdf";

        try (PDDocument doc = new PDDocument()) {
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            float margin = 40;
            float yStart = PDRectangle.A4.getHeight() - margin;
            float tableWidth = PDRectangle.A4.getWidth() - 2 * margin;
            float rowHeight = 20;
            float cellPadding = 4;

            float[] colWidths = {100, 300, 100};

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(doc, page);
            float yPosition = yStart;

            // Título
            stream.beginText();
            stream.setFont(fontBold, 14);
            stream.newLineAtOffset(margin, yPosition);
            stream.showText("Extrato de Transações");
            stream.endText();
            yPosition -= 30;

            // Cabeçalho
            String[] headers = {"Data", "Descrição", "Valor (R$)"};
            drawRow(stream, margin, yPosition, rowHeight, colWidths, headers, cellPadding, fontBold);
            yPosition -= rowHeight;

            double total = 0;

            for (int i = 1; i <= 200; i++) {
                String data = "2024-01-" + String.format("%02d", (i % 30) + 1);
                String desc = "Serviço " + i;
                double valor = Math.round((50 + Math.random() * 450) * 100.0) / 100.0;
                total += valor;
                String valorStr = "R$ " + String.format("%.2f", valor).replace('.', ',');

                String[] row = {data, desc, valorStr};

                if (yPosition - rowHeight < margin) {
                    stream.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    stream = new PDPageContentStream(doc, page);
                    yPosition = yStart;
                }

                drawRow(stream, margin, yPosition, rowHeight, colWidths, row, cellPadding, fontRegular);
                yPosition -= rowHeight;
            }

            // Total
            if (yPosition - rowHeight < margin) {
                stream.close();
                page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                stream = new PDPageContentStream(doc, page);
                yPosition = yStart;
            }

            String[] totalRow = {"", "Total", "R$ " + String.format("%.2f", total).replace('.', ',')};
            drawRow(stream, margin, yPosition, rowHeight, colWidths, totalRow, cellPadding, fontBold);

            stream.close();
            doc.save(filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawRow(PDPageContentStream stream, float x, float y, float height, float[] colWidths, String[] texts, float padding, PDType1Font font) throws IOException {
        float cellX = x;

        for (int i = 0; i < texts.length; i++) {
            // Draw border
            stream.setStrokingColor(Color.BLACK);
            stream.addRect(cellX, y - height, colWidths[i], height);
            stream.stroke();

            // Write text
            stream.beginText();
            stream.setFont(font, 11); // <- ESSENCIAL para evitar o erro
            stream.newLineAtOffset(cellX + padding, y - height + padding + 3);
            stream.showText(texts[i]);
            stream.endText();

            cellX += colWidths[i];
        }
    }

    public void mergeTemplateAndTermoByCpf(String cpf) {
        String inputTemplate = "output/template-output-" + cpf + ".pdf";
        String fallbackTemplate = "src/main/resources/template.pdf";
        String inputTermo = "src/main/resources/termo.pdf";
        String outputPath = "output/merged-" + cpf + ".pdf";

        try (PDDocument merged = new PDDocument();
             PDDocument doc1 = Loader.loadPDF(new File(Files.exists(Paths.get(inputTemplate)) ? inputTemplate : fallbackTemplate));
             PDDocument doc2 = Loader.loadPDF(new File(inputTermo))) {

            doc1.getPages().forEach(merged::addPage);
            doc2.getPages().forEach(merged::addPage);
            merged.save(outputPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
