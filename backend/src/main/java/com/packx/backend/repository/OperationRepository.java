package com.packx.backend.repository;

import com.packx.backend.model.Operation;
import com.packx.backend.model.OperationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OperationRepository extends MongoRepository<Operation, String> {

    List<Operation> findAllByOrderByCreatedAtDesc();

    List<Operation> findByOperationType(OperationType operationType);

    long countByOperationType(@Param("operationType") OperationType operationType);
}
