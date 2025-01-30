package org.advisor.member.repositories;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.advisor.member.entities.Member;
import org.advisor.member.entities.QMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member> {

    @EntityGraph("authorities")
    Optional<Member> findByEmail(String email);
    Optional<Member> findById(String id);
    List<Member> findByName(String name);
    List<Member> findByNameContaining(String name);
    List<Member> findByEmailContaining(String email);
    List<Member> findByNameAndEmail(String name, String email);
    default boolean exists(String email) {
        QMember member = QMember.member;

        return exists(member.email.eq(email));
    }

    boolean existsByEmail(@NotBlank(message = "Email cannot be blank") @Email(message = "Email should be valid") String email);
}
