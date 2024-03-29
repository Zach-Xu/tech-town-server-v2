package com.tech.repo;

import com.tech.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    Long countByUser_Id(Long userId);
}
