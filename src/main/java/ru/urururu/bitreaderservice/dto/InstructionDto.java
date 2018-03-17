package ru.urururu.bitreaderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Data
@AllArgsConstructor
public class InstructionDto {
    private final String kind;
    private int typeId;
    private final List<ValueRefDto> operands;
    private final Integer sourceRef;
}
