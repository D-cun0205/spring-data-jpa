package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.iterable;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

//        member1.setUsername("change user name");

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        List<Member> members = memberRepository.findAll();
        assertThat(members.size()).isEqualTo(2);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long afterDeleteCount = memberRepository.count();
        assertThat(afterDeleteCount).isEqualTo(0);
    }

    @Test
    void findByUsernameAndAgeGreaterThen() throws Exception {
        //given
        Member member1 = new Member("member1", 100);
        Member member2 = new Member("member2", 200);
        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<Member> findMembers = memberRepository.findByUsernameAndAgeGreaterThan("member1", 99);

        //then
        assertThat(findMembers.get(0).getUsername()).isEqualTo("member1");
        assertThat(findMembers.get(0).getAge()).isEqualTo(100);
    }

    @Test
    void findMemberTest() throws Exception {
        //given
        Member member1 = new Member("member1", 100);
        memberRepository.save(member1);


        //when
        List<Member> findMember1 = memberRepository.findMember("member1", 100);

        //then
        assertThat(findMember1.get(0).getUsername()).isEqualTo("member1");
    }

    @Test
    void findMemberDto() throws Exception {
        //given
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member = new Member("sanghun", 20, team);
        memberRepository.save(member);

        //when
        List<MemberDto> memberDto = memberRepository.findMemberDto();

        //then
        assertThat(memberDto.get(0).getUsername()).isEqualTo("sanghun");
        assertThat(memberDto.get(0).getAge()).isEqualTo(20);
        assertThat(memberDto.get(0).getTeam().getName()).isEqualTo("teamA");

    }

    @Test
    void findByNamesTest() throws Exception {
        //given
        Member member1 = new Member("member1", 100);
        Member member2 = new Member("member2", 200);
        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<Member> byNames =
                memberRepository.findByNames(Arrays.asList(member1.getUsername(), member2.getUsername()));

        //then
        assertThat(byNames.get(0).getUsername()).isEqualTo("member1");
        assertThat(byNames.get(1).getUsername()).isEqualTo("member2");
    }

    @Test
    void resultType() throws Exception {
        //given
        Member member1 = new Member("member1", 100);
        Member member2 = new Member("member2", 200);
        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
//        List<Member> listTypeMember = memberRepository.findListTypeMember();
//        Member objectTypeMember = memberRepository.findObjectTypeMember();
//        Optional<List<Member>> optionalListTypeMember = memberRepository.findOptionalListTypeMember();
//        Optional<Member> optionalObjectTypeMember = memberRepository.findOptionalObjectTypeMember();

        //then
    }

    @Test
    void paging() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));

        int age = 10;

        PageRequest request = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> page = memberRepository.findByAge(age, request);
        List<Member> members = page.getContent();
        long totalCount = page.getTotalElements();
        int numberOfElements = page.getNumberOfElements();

        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getUsername(), m.getAge(), m.getTeam()));
        List<MemberDto> returnApiMemberDto = toMap.getContent();
    }

    @Test
    void entityGraphTest() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));

        List<Member> all = memberRepository.findAll();
        for (Member member : all) {
            System.out.println(member.getUsername());
        }
    }

    @Test
    void customSqlTest() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));

        List<Member> findMembers = memberRepository.findMemberCustom();
        for (Member member : findMembers) {
            System.out.println("member: " + member.getUsername());
        }
    }

    @Test
    void jpaEventBaseEntity() throws Exception {
        //given
        Member member = new Member("member1");
        memberRepository.save(member);

        Thread.sleep(1000);
        member.setUsername("member2");

        em.flush();
        em.clear();

        //when
        Member findMember = memberRepository.findById(member.getId()).get();

        //then
    }
}