package ru.urururu.bitreaderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Data
@Builder
public class InstructionDto {
    private String kind;
    private int typeId;
    private List<ValueRefDto> operands;
    private Integer sourceRef;
    private String predicate;
    private List<ValueRefDto> incomingValues;
    private List<ValueRefDto> incomingBlocks;
}
