package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberJpaRepositoryTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

//        member1.setUsername("change user name");

        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        List<Member> members = memberJpaRepository.findAll();
        assertThat(members.size()).isEqualTo(2);

        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2);

        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);

        long afterDeleteCount = memberJpaRepository.count();
        assertThat(afterDeleteCount).isEqualTo(0);
    }

    @Test
    void findByUsernameAndAgeGreaterThen() throws Exception {
        //given
        Member member1 = new Member("member1", 100);
        Member member2 = new Member("member2", 200);
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);
        
        //when
        List<Member> findMembers = memberJpaRepository.findByUsernameAndAgeGreaterThen("member1", 99);

        //then
        assertThat(findMembers.get(0).getUsername()).isEqualTo("member1");
        assertThat(findMembers.get(0).getAge()).isEqualTo(100);
    }    
}