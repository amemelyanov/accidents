package ru.job4j.accidents.model;

import lombok.*;

/**
 * Модель данных тип инцидента
 *
 * @author Alexander Emelyanov
 * @version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AccidentType {

    /**
     * Идентификатор типа инцидента
     */
    @EqualsAndHashCode.Include
    private int id;

    /**
     * Наименование типа инцидента
     */
    private String name;
}