package ru.urururu.bitreaderservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

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
    private String opcode;
    private List<ValueRefDto> operands;
}
