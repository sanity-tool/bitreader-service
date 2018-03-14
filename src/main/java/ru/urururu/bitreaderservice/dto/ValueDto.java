package ru.urururu.bitreaderservice.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Data
@Builder
public class ValueDto {
    private String kind;
    private String name;
    private int typeId;
    private Long intValue;
    private Double fpValue;
}
