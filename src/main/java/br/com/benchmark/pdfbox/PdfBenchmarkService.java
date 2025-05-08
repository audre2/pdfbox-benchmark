package br.com.benchmark.pdfbox;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.Random;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.line.LineStyle;

@Service
public class PdfBenchmarkService {

	public void generateTextPdf(Map<String, String> data) {
		String cpf = data.get("cpf");
		String filePath = "output/text-" + cpf + ".pdf";

		try (PDDocument doc = new PDDocument()) {
			PDPage page = new PDPage();
			doc.addPage(page);
			PDPageContentStream stream = new PDPageContentStream(doc, page);

			PDFont font = PDType1Font.HELVETICA_BOLD;
			stream.beginText();
			stream.setFont(font, 16);
			stream.setLeading(20f);
			stream.newLineAtOffset(50, 700);

			stream.showText("Relatório de Atividades");
			stream.newLine();
			stream.setFont(PDType1Font.HELVETICA, 12);
			stream.showText("Nome: " + data.get("nome"));
			stream.newLine();
			stream.showText("CPF: " + cpf);
			stream.newLine();
			stream.showText("Data: " + data.get("data"));
			stream.newLine();
			stream.showText("Status: " + data.get("status"));
			stream.endText();
			stream.close();

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

		try (PDDocument doc = PDDocument.load(new File(templatePath))) {
			PDPage page = doc.getPage(0);
			PDPageContentStream stream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND,
					true);

			PDFont font = PDType1Font.HELVETICA;
			stream.beginText();
			stream.setFont(font, 12);
			stream.newLineAtOffset(100, 700);
			stream.showText(data.get("nome"));
			stream.endText();

			stream.beginText();
			stream.setFont(font, 12);
			stream.newLineAtOffset(100, 670);
			stream.showText(cpf);
			stream.endText();

			stream.beginText();
			stream.setFont(font, 12);
			stream.newLineAtOffset(180, 640);
			stream.showText(data.get("dataNascimento"));
			stream.endText();

			byte[] imageBytes = Base64.getDecoder().decode(Files.readString(Paths.get(signaturePath)));
			PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, imageBytes, "assinatura");
			stream.drawImage(pdImage, 40, 510);

			stream.close();
			doc.save(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void generateMultiPageWithTable() {
	    String filePath = "output/multipage-" + new Random().nextInt(999999) + ".pdf";

	    try (PDDocument doc = new PDDocument()) {
	        doc.setAllSecurityToBeRemoved(true);

	        PDPage page = new PDPage(PDRectangle.A4);
	        doc.addPage(page);

	        float margin = 40;
	        float yStart = page.getMediaBox().getHeight() - margin;

	        try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
	            contentStream.beginText();
	            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
	            contentStream.newLineAtOffset(margin, yStart);
	            contentStream.showText("Extrato de Transações");
	            contentStream.endText();
	        }

	        float tableY = yStart - 30;
	        float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
	        float bottomMargin = 40;

	        BaseTable table = new BaseTable(tableY, yStart, bottomMargin, tableWidth, margin, doc, page, true, true);

	        Row<PDPage> header = table.createRow(20);

	        Cell<PDPage> h1 = header.createCell(20, "Data");
	        h1.setFont(PDType1Font.HELVETICA_BOLD);
	        h1.setBorderStyle(new LineStyle(Color.BLACK, 0.1f));

	        Cell<PDPage> h2 = header.createCell(50, "Descrição");
	        h2.setFont(PDType1Font.HELVETICA_BOLD);
	        h2.setBorderStyle(new LineStyle(Color.BLACK, 0.1f));

	        Cell<PDPage> h3 = header.createCell(30, "Valor (R$)");
	        h3.setFont(PDType1Font.HELVETICA_BOLD);
	        h3.setAlign(HorizontalAlignment.RIGHT);
	        h3.setBorderStyle(new LineStyle(Color.BLACK, 0.1f));

	        double total = 0;

	        for (int i = 1; i <= 100; i++) {
	            String data = "2024-01-" + String.format("%02d", (i % 30) + 1);
	            String desc = "Serviço " + i;
	            double valor = Math.round((50 + Math.random() * 450) * 100.0) / 100.0;
	            total += valor;
	            String valorStr = "R$ " + String.format("%.2f", valor).replace('.', ',');

	            Row<PDPage> row = table.createRow(18);

	            Cell<PDPage> c1 = row.createCell(20, data);
	            c1.setBorderStyle(new LineStyle(Color.BLACK, 0.1f));

	            Cell<PDPage> c2 = row.createCell(50, desc);
	            c2.setBorderStyle(new LineStyle(Color.BLACK, 0.1f));

	            Cell<PDPage> c3 = row.createCell(30, valorStr);
	            c3.setAlign(HorizontalAlignment.RIGHT);
	            c3.setBorderStyle(new LineStyle(Color.BLACK, 0.1f));
	        }

	        Row<PDPage> totalRow = table.createRow(20);

	        Cell<PDPage> t1 = totalRow.createCell(20, "");
	        t1.setBorderStyle(new LineStyle(Color.BLACK, 0.1f));

	        Cell<PDPage> t2 = totalRow.createCell(50, "Total");
	        t2.setFont(PDType1Font.HELVETICA_BOLD);
	        t2.setBorderStyle(new LineStyle(Color.BLACK, 0.1f));

	        Cell<PDPage> t3 = totalRow.createCell(30, "R$ " + String.format("%.2f", total).replace('.', ','));
	        t3.setFont(PDType1Font.HELVETICA_BOLD);
	        t3.setAlign(HorizontalAlignment.RIGHT);
	        t3.setBorderStyle(new LineStyle(Color.BLACK, 0.1f));

	        table.draw();
	        doc.save(filePath);

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	public void mergeTemplateAndTermoByCpf(String cpf) {
		String inputTemplate = "output/template-output-" + cpf + ".pdf";
		String fallbackTemplate = "src/main/resources/template.pdf";
		String inputTermo = "src/main/resources/termo.pdf";
		String outputPath = "output/merged-" + cpf + ".pdf";

		try (PDDocument merged = new PDDocument();
				PDDocument doc1 = PDDocument
						.load(new File(Files.exists(Paths.get(inputTemplate)) ? inputTemplate : fallbackTemplate));
				PDDocument doc2 = PDDocument.load(new File(inputTermo))) {

			for (PDPage page : doc1.getPages()) {
				merged.addPage(page);
			}
			for (PDPage page : doc2.getPages()) {
				merged.addPage(page);
			}
			merged.save(outputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
