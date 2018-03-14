package ru.urururu.bitreaderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Data
@AllArgsConstructor
public class GlobalInitializerDto {
    private final ValueRefDto ref;
    private final ValueRefDto initializer;
}
