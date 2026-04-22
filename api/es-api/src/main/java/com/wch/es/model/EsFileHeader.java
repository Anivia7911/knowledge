package com.wch.es.model;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * @author: Jie Bugui
 * @create: 2025-04-22 16:43
 */
@Data
@Document(indexName = "EsFileHeader")
public class EsFileHeader {
    private String id;
    private String name;
}
