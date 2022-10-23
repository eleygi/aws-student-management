package com.eleygi.crud.student.dao;

import com.eleygi.crud.student.model.Student;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class DaoFactory {

    private final UnaryOperator<String> propertyProvider;
    private final Supplier<DynamoDbClientBuilder> dynamoDbClientBuilderSupplier;

    public static DaoFactory create(){
        UnaryOperator<String> propertyProvider = System::getProperty;
        return new DaoFactory(propertyProvider, DynamoDbClient::builder);
    }

    public DaoFactory(UnaryOperator<String> propertyProvider, Supplier<DynamoDbClientBuilder> dynamoDbClientBuilderSupplier) {
        super();
        this.propertyProvider = propertyProvider;
        this.dynamoDbClientBuilderSupplier = dynamoDbClientBuilderSupplier;
    }

    public IDao<Student> createStudentDaoInstance(){
        String tableName = propertyProvider.apply("STUDENT_TABLE_NAME");
        String awsRegion = propertyProvider.apply("AWS_REGION");

        DynamoDbClient dynamoDbClient = dynamoDbClientBuilderSupplier.get().region(Region.of(awsRegion)).build();
        DynamoDbEnhancedClient dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
        DynamoDbTable<Student> studentDynamoDbTable = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(Student.class));
        return new StudentDao(studentDynamoDbTable);
    }
}
