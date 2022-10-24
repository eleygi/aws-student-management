package com.eleygi.crud.student.dao;

import com.eleygi.crud.student.model.Student;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

public class StudentDao implements IDao<Student> {

    private static final String DDB_TABLE_NAME = System.getProperty("STUDENT_TABLE_NAME");
    private static final String AWS_REGION = System.getProperty("AWS_REGION");
    private final DynamoDbTable<Student> studentTable;

//    static{
//        DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.of(AWS_REGION)).credentialsProvider(EnvironmentVariableCredentialsProvider.create()).build();
//        DynamoDbEnhancedClient dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
//        studentTable = dynamoDbEnhancedClient.table(DDB_TABLE_NAME, TableSchema.fromBean(Student.class));
//    }
    public StudentDao(DynamoDbTable<Student> studentTable) {
        this.studentTable = studentTable;
    }

    @Override
    public void create(Student item) {
        studentTable.putItem(item);
    }

    @Override
    public Student get(String id) {
        return studentTable.getItem(Key.builder().partitionValue(id).build());
    }

    @Override
    public void update(Student item) {
        studentTable.updateItem(item);
    }

    @Override
    public void delete(String id) {
        studentTable.deleteItem(Key.builder().partitionValue(id).build());
    }
}
