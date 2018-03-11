package ru.urururu.bitreaderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Data
@AllArgsConstructor
public class TypeDto {
    private final String kind;
    private final String name;
    private final Long len;
    private final List<Integer> types;
}
