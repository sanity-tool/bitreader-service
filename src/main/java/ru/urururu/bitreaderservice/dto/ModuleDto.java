package ru.urururu.bitreaderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Data
@AllArgsConstructor
public class ModuleDto {
    private Collection<FunctionDto> functions;
    private Collection<TypeDto> types;
}
