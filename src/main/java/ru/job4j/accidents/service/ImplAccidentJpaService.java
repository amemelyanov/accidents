package ru.job4j.accidents.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.job4j.accidents.dto.FileDto;
import ru.job4j.accidents.model.*;
import ru.job4j.accidents.repository.AccidentRepository;
import ru.job4j.accidents.repository.AccidentTypeRepository;
import ru.job4j.accidents.repository.RuleRepository;
import ru.job4j.accidents.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса по работе с инцидентами
 *
 * @see ru.job4j.accidents.service.AccidentService
 * @author Alexander Emelyanov
 * @version 1.0
 */
@AllArgsConstructor
@Service
public class ImplAccidentJpaService implements AccidentService {

    /**
     * Объект для доступа к методам AccidentRepository
     */
    private final AccidentRepository accidentRepository;

    /**
     * Объект для доступа к методам AccidentTypeRepository
     */
    private final AccidentTypeRepository accidentTypeRepository;

    /**
     * Объект для доступа к методам RuleRepository
     */
    private final RuleRepository ruleRepository;

    /**
     * Объект для доступа к методам FileService
     */
    private final FileService fileService;
    private final UserRepository userRepository;

    /**
     * Возвращает список всех инцидентов.
     *
     * @return список всех инцидентов
     */
    @Override
    public List<Accident> findAll() {
        List<Accident> result = new ArrayList<>();
        accidentRepository.findAll().forEach(result::add);
        return result.stream()
                .sorted(Comparator.comparing(Accident::getId))
                .collect(Collectors.toList());
    }

    /**
     * Выполняет сохранение инцидента. При успешном сохранении возвращает
     * сохраненный инцидент.
     *
     * @param accident сохраняемый инцидент
     * @param image    объект dto файла фотографии
     */
    @Override
    public Accident create(Accident accident, FileDto image) {
        saveNewFile(accident, image);
        return accidentRepository.save(accident);
    }

    /**
     * Выполняет обновление инцидента.
     *
     * @param accident обновляемый инцидент
     * @param image    объект dto файла фотографии
     * @return инцидент при успешном обновлении
     */
    @Override
    public Accident update(Accident accident, FileDto image) {
        int oldFileId = accident.getFileId();
        saveNewFile(accident, image);
        Accident savedAccident = accidentRepository.save(accident);
        fileService.deleteById(oldFileId);
        return savedAccident;
    }

    /**
     * Выполняет сохранение файла фотографии
     * и установку поля fileId у accident.
     *
     * @param accident инцидент
     * @param image    объект dto файла фотографии
     */
    private void saveNewFile(Accident accident, FileDto image) {
        File file = fileService.save(image);
        accident.setFileId(file.getId());
    }

    /**
     * Выполняет выбор методов класса для сохранения или обновления инцидента.
     *
     * @param accident сохраняемый инцидент
     * @param ids      массив идентификаторов статей
     * @param image    объект dto файла фотографии
     * @return инцидент при успешном сохранении или обновлении
     * @exception NoSuchElementException если тип инцидента не найден в репозитории
     */
    @Override
    public Accident createOrUpdateAccident(Accident accident, String[] ids, FileDto image) {
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        int typeId = accident.getType().getId();
        AccidentType type = accidentTypeRepository.findById(typeId).orElseThrow(
                () -> new NoSuchElementException(String.format(
                        "Тип нарушения с id = %d не найдена", typeId))
        );
        Set<Rule> rules = findRulesByIds(ids);

        accident.setUser(user);
        accident.setType(type);
        accident.setRules(rules);
        if (accident.getId() == 0) {
            accident.setStatus(Status.NEW);
            accident = create(accident, image);
        } else {
            accident = update(accident, image);
        }
        return accident;
    }

    /**
     * Выполняет поиск инцидента по идентификатору. При успешном нахождении возвращает
     * инцидент, иначе выбрасывает исключение.
     *
     * @param id идентификатор инцидента
     * @return инцидент при успешном нахождении
     * @throws NoSuchElementException если инцидент не найден
     */
    @Override
    public Accident findById(int id) {
        return accidentRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException(
                        String.format("Инцидент c id = %d не найден", id))
        );
    }

    /**
     * Возвращает список всех типов инцидентов.
     *
     * @return список всех типов инцидентов
     */
    @Override
    public List<AccidentType> findAllAccidentTypes() {
        List<AccidentType> result = new ArrayList<>();
        accidentTypeRepository.findAll().forEach(result::add);
        return result;
    }

    /**
     * Возвращает список всех статей инцидентов.
     *
     * @return список всех статей инцидентов
     */
    @Override
    public List<Rule> findAllAccidentRules() {
        List<Rule> result = new ArrayList<>();
        ruleRepository.findAll().forEach(result::add);
        return result;
    }

    /**
     * Возвращает правило по идентификатору.
     *
     * @param id идентификатор правила
     * @return правило
     */
    @Override
    public Rule findRuleById(int id) {
        return ruleRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException(
                        String.format("Правило c id = %d не найдена", id))
        );
    }

    /**
     * Возвращает список всех правил инцидента
     * по списку идентификаторов.
     *
     * @param ids список идентификаторов инцидента
     * @return список всех статей инцидента
     */
    @Override
    public Set<Rule> findRulesByIds(String[] ids) {
        return Arrays.stream(ids).map(Integer::parseInt)
                .map(this::findRuleById).collect(Collectors.toSet());
    }

    /**
     * Выполняет поиск инцидента по идентификатору.
     *
     * @param id идентификатор задачи
     * @return задачу при успешном нахождении
     */
    @Override
    public Accident findAccidentById(int id) {
        return accidentRepository.findAccidentById(id);
    }

    /**
     * Выполняет удаление инцидента по идентификатору.
     *
     * @param id идентификатор задачи
     */
    @Override
    public void deleteById(int id) {
        Accident accidentFromDB = findById(id);
        accidentRepository.deleteById(id);
        fileService.deleteById(accidentFromDB.getFileId());
    }
}
