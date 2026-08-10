package com.wch.common.trivial.enumdata;

import java.io.Serializable;

/**
 * @author: Jie Bugui
 * @create: 2026-04-29 2:44
 */
public enum FileSchemeEnum {

    LOCAL(1),//本地操作系统
    ALI_OSS(2);//阿里云服务oss

    private final Integer schemeCode;

    FileSchemeEnum(Integer schemeCode) {
        this.schemeCode = schemeCode;
    }

    public Serializable getValue() {
        return schemeCode;
    }
}
