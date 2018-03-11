package ru.urururu.bitreaderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Data
@AllArgsConstructor
public class FunctionDto {
    private final Collection<BlockDto> blocks;
    private final int entryBlockIndex;
}
