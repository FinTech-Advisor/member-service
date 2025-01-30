package org.advisor.member.repositories;


import org.advisor.member.entities.Authorities;
import org.advisor.member.entities.AuthoritiesId;
import org.advisor.member.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

public interface AuthoritiesRepository extends JpaRepository<Authorities, AuthoritiesId>, QuerydslPredicateExecutor<Authorities> {
    Optional<Authorities> findByMember(Member member);

    List<Authorities> findAllById(String id);

    void deleteAll(String id);
}