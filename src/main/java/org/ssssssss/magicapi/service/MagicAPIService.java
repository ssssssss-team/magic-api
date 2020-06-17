package org.ssssssss.magicapi.service;

import org.ssssssss.magicapi.model.Page;
import org.ssssssss.magicapi.model.PageResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface MagicAPIService {

    default Object execute(String statementId) {
        return execute(statementId, Collections.emptyMap());
    }

    Object execute(String statementId, Map<String, Object> params);

    default <T> T queryForObject(String statementId, Class<T> clazz) {
        return queryForObject(statementId, Collections.emptyMap(), clazz);
    }

    <T> T queryForObject(String statementId, Map<String, Object> params, Class<T> clazz);

    default <T> List<T> queryForList(String statementId, Class<T> clazz) {
        return queryForList(statementId, Collections.emptyMap(), clazz);
    }

    <T> List<T> queryForList(String statementId, Map<String, Object> params, Class<T> clazz);

    default <T> PageResult<T> queryForPage(String statementId, Page page, Class<T> clazz) {
        return queryForPage(statementId, page, Collections.emptyMap(), clazz);
    }

    <T> PageResult<T> queryForPage(String statementId, Page page, Map<String, Object> params, Class<T> clazz);

    default int update(String statementId) {
        return update(statementId, Collections.emptyMap());
    }

    int update(String statementId, Map<String, Object> params);

}
