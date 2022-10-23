package com.eleygi.crud.student.dao;

import com.eleygi.crud.student.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentDaoTest {

    private final String HASH_KEY = "id";
    private StudentDao dao;
    @Mock
    private DynamoDbTable<Student> studentTable;
    @Captor
    private ArgumentCaptor<Key> keyArgumentCaptor;

    @BeforeEach
    void setUp() {
        dao = new StudentDao(studentTable);
    }

    @Test
    void create() {
        Student student = new Student();

        dao.create(student);

        verify(studentTable).putItem(student);
        verifyNoMoreInteractions(studentTable);
    }

    @Test
    void get() {
        Student student = new Student();

        when(studentTable.getItem(any(Key.class))).thenReturn(student);

        Student result = dao.get(HASH_KEY);

        assertThat(result).isEqualTo(student);

        verify(studentTable).getItem(keyArgumentCaptor.capture());
        assertThat(keyArgumentCaptor.getValue().partitionKeyValue().s()).isEqualTo(HASH_KEY);
        verifyNoMoreInteractions(studentTable);
    }

    @Test
    void update() {
        Student student = new Student();

        dao.update(student);

        verify(studentTable).updateItem(student);
        verifyNoMoreInteractions(studentTable);
    }

    @Test
    void delete() {
        Student student = new Student();

        when(studentTable.deleteItem(any(Key.class))).thenReturn(student);

        dao.delete(HASH_KEY);

        verify(studentTable).deleteItem(keyArgumentCaptor.capture());
        assertThat(keyArgumentCaptor.getValue().partitionKeyValue().s()).isEqualTo(HASH_KEY);
        verifyNoMoreInteractions(studentTable);
    }
}