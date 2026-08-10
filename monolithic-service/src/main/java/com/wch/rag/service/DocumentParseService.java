package com.wch.rag.service;

import com.wch.file.model.po.FileBodyPO;
import com.wch.file.model.po.FileHeaderPO;
import com.wch.file.service.FileBodyService;
import com.wch.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

/**
 * 文档解析服务：将已上传的文件解析为纯文本，供 RAG 向量化使用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentParseService {

    private final FileService fileService;
    private final FileBodyService fileBodyService;

    private static final Set<String> TEXT_TYPES = Set.of(
            "txt", "md", "markdown", "json", "xml", "csv", "log", "java", "py", "js", "ts",
            "html", "css", "sql", "yaml", "yml", "properties", "sh", "bat", "ini", "conf"
    );

    /**
     * 按文件ID解析文本内容
     */
    public String parseByFileId(Long fileId) throws Exception {
        FileHeaderPO header = fileService.getFileInfo(String.valueOf(fileId));
        if (header == null) {
            throw new RuntimeException("文件不存在");
        }
        FileBodyPO body = fileBodyService.getById(header.getBodyId());
        if (body == null || body.getPath() == null || body.getPath().isBlank()) {
            throw new RuntimeException("文件内容不存在（可能是历史脏数据）");
        }
        String type = body.getType() == null ? extOf(header.getName()) : body.getType().toLowerCase();
        return parse(body.getPath(), type);
    }

    /**
     * 按文件路径与类型解析文本
     */
    public String parse(String path, String type) throws Exception {
        if (type == null) {
            type = "";
        }
        type = type.toLowerCase();
        return switch (type) {
            case "pdf" -> parsePdf(path);
            case "docx" -> parseDocx(path);
            case "doc" -> parseDoc(path);
            case "xlsx" -> parseXlsx(path);
            case "xls" -> parseXls(path);
            case "pptx" -> parsePptx(path);
            case "ppt" -> parsePpt(path);
            default -> {
                if (TEXT_TYPES.contains(type)) {
                    yield Files.readString(Paths.get(path), StandardCharsets.UTF_8);
                }
                throw new RuntimeException("暂不支持解析该文件类型: " + type);
            }
        };
    }

    /**
     * 判断文件类型是否可被解析为文本
     */
    public boolean isParseable(String type) {
        if (type == null) return false;
        String t = type.toLowerCase();
        return TEXT_TYPES.contains(t) || Set.of("pdf", "docx", "doc", "xlsx", "xls", "pptx", "ppt").contains(t);
    }

    private String parsePdf(String path) throws Exception {
        try (PDDocument doc = PDDocument.load(new java.io.File(path))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String parseDocx(String path) throws Exception {
        try (InputStream is = new FileInputStream(path);
             XWPFDocument doc = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private String parseDoc(String path) throws Exception {
        try (InputStream is = new FileInputStream(path);
             HWPFDocument doc = new HWPFDocument(is);
             WordExtractor extractor = new WordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private String parseXlsx(String path) throws Exception {
        try (InputStream is = new FileInputStream(path);
             XSSFWorkbook workbook = new XSSFWorkbook(is)) {
            return extractWorkbookText(workbook::getNumberOfSheets, workbook::getSheetAt);
        }
    }

    private String parseXls(String path) throws Exception {
        try (InputStream is = new FileInputStream(path);
             HSSFWorkbook workbook = new HSSFWorkbook(is)) {
            return extractWorkbookText(workbook::getNumberOfSheets, workbook::getSheetAt);
        }
    }

    private interface SheetProvider {
        int count();

        Sheet sheet(int index);
    }

    private String extractWorkbookText(java.util.function.IntSupplier countFn, java.util.function.IntFunction<Sheet> sheetFn) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < countFn.getAsInt(); i++) {
            Sheet sheet = sheetFn.apply(i);
            sb.append(sheet.getSheetName()).append('\n');
            for (Row row : sheet) {
                StringBuilder line = new StringBuilder();
                for (Cell cell : row) {
                    try {
                        line.append(cell.toString()).append('\t');
                    } catch (Exception ignore) {
                    }
                }
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    private String parsePptx(String path) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = new FileInputStream(path);
             XMLSlideShow ppt = new XMLSlideShow(is)) {
            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        sb.append(textShape.getText()).append('\n');
                    }
                }
            }
        }
        return sb.toString();
    }

    private String parsePpt(String path) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = new FileInputStream(path);
             HSLFSlideShow ppt = new HSLFSlideShow(is)) {
            for (var slide : ppt.getSlides()) {
                for (var shape : slide.getShapes()) {
                    if (shape instanceof HSLFTextShape textShape) {
                        sb.append(textShape.getText()).append('\n');
                    }
                }
            }
        }
        return sb.toString();
    }

    private String extOf(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
