package org.advisor.member.repositories;

import org.advisor.member.entities.Authorities;
import org.advisor.member.entities.AuthoritiesId;
import org.advisor.member.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface AuthoritiesRepository extends JpaRepository<Authorities, AuthoritiesId>, QuerydslPredicateExecutor<Authorities> {
    Optional<Authorities> findByMember(Member member);

    List<Authorities> findAllByMember_Id(String memberId);

    void deleteAllByMember_Id(String memberId);

    // 여러 개의 Authorities를 한 번에 저장하는 메서드
    <S extends Authorities> List<S> saveAll(Iterable<S> entities);
}
