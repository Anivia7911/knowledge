package com.wch.common.trivial.enumdata;

import lombok.Getter;

/**
 * @author: Jie Bugui
 * @create: 2026-04-29 2:52
 */
@Getter
public enum FileTypeEnum {
    /**
     * JPEG
     */
    JPEG("JPEG", "FFD8FF"),

    /**
     * PNG
     */
    PNG("PNG", "89504E47"),

    /**
     * GIF
     */
    GIF("GIF", "47494638"),

    /**
     * TIFF
     */
    TIFF("TIFF", "49492A00"),

    /**
     * Windows bitmap
     */
    BMP("BMP", "424D"),

    /**
     * CAD
     */
    DWG("DWG", "41433130"),

    /**
     * Adobe photoshop
     */
    PSD("PSD", "38425053"),

    /**
     * Rich Text Format
     */
    RTF("RTF", "7B5C727466"),

    /**
     * XML
     */
    XML("XML", "3C3F786D6C"),

    /**
     * HTML
     */
    HTML("HTML", "68746D6C3E"),

    /**
     * Outlook Express
     */
    DBX("DBX", "CFAD12FEC5FD746F "),

    /**
     * Outlook
     */
    PST("PST", "2142444E"),

    /**
     * doc;xls;dot;ppt;xla;ppa;pps;pot;msi;sdw;db
     */
    OLE2("OLE2", "0xD0CF11E0A1B11AE1"),

    /**
     * Microsoft Word/Excel
     */
    XLS_DOC("XLS_DOC", "D0CF11E0"),

    /**
     * Microsoft Access
     */
    MDB("MDB", "5374616E64617264204A"),

    /**
     * Word Perfect
     */
    WPB("WPB", "FF575043"),

    /**
     * Postscript
     */
    EPS_PS("EPS_PS", "252150532D41646F6265"),

    /**
     * Adobe Acrobat
     */
    PDF("PDF", "255044462D312E"),

    /**
     * Windows Password
     */
    PWL("PWL", "E3828596"),

    /**
     * ZIP Archive
     */
    ZIP("ZIP", "504B0304"),

    /**
     * ARAR Archive
     */
    RAR("RAR", "52617221"),

    /**
     * WAVE
     */
    WAV("WAV", "57415645"),

    /**
     * AVI
     */
    AVI("AVI", "41564920"),

    /**
     * Real Audio
     */
    RAM("RAM", "2E7261FD"),

    /**
     * Real Media
     */
    RM("RM", "2E524D46"),

    /**
     * Quicktime
     */
    MOV("MOV", "6D6F6F76"),

    /**
     * Windows Media
     */
    ASF("ASF", "3026B2758E66CF11"),

    /**
     * MIDI
     */
    MID("MID", "4D546864"),

    /**
     * OTHER
     */
    OTHER("OTHER", "");

    private final String fileTypeName;
    private final String magicNumberCode;

    FileTypeEnum(String fileTypeName, String magicNumberCode) {
        this.fileTypeName = fileTypeName;
        this.magicNumberCode = magicNumberCode;
    }

    // 根据魔数反推文件类型（简化版）
    public static FileTypeEnum fromMagicNumber(String magic) {
        for (FileTypeEnum type : values()) {
            if (magic.startsWith(type.magicNumberCode)) {
                return type;
            }
        }
        return null;
    }
}

