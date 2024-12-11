package com.example.ezlawbackend.LawyerAuth.repository;

import com.example.ezlawbackend.LawyerAuth.model.Lawyer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LawyerRepository extends MongoRepository<Lawyer, String> {
    Lawyer findByLawyerEmail(String email);
}