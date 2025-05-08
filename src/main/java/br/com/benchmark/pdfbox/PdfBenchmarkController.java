package br.com.benchmark.pdfbox;

import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pdf")
public class PdfBenchmarkController {

    private final PdfBenchmarkService service;

    public PdfBenchmarkController(PdfBenchmarkService service) {
        this.service = service;
    }

    @PostMapping("/text")
    public String generateText(@RequestBody Map<String, String> data) {
        service.generateTextPdf(data);
        return "PDF com texto gerado!";
    }

    @PostMapping("/template")
    public String fillTemplate(@RequestBody Map<String, String> data) {
        service.fillTemplatePdf(data);
        return "PDF com template preenchido!";
    }

    @GetMapping("/multipage")
    public String multipageExtrato() {
        service.generateMultiPageWithTable();
        return "PDF multipage gerado!";
    }

    @GetMapping("/merge")
    public String mergeByCpf(@RequestParam String cpf) {
        service.mergeTemplateAndTermoByCpf(cpf);
        return "PDFs mesclados para o CPF!";
    }
}
