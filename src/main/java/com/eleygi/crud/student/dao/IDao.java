package com.eleygi.crud.student.dao;

public interface IDao<T> {

    void create(T item);

    T get(String id);

    void update(T item);

    void delete(String id);
}
