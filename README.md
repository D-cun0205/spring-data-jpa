## Spring Data JPA

### 메서드 이름으로 생성

이름으로 유추해서 쿼리를 생성해주는 방법인데 파라미터가 2개 이상 넘어가면
메서드 명이 너무 길어지므로 단순하지 않을 때 다른 방법(@Query) 사용

```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
}
```

### @Query & 파라미터 바인딩 사용

```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);
}
```

### 페이징, 정렬

* Page 
  * 데이터 및 데이터 수, 페이지 등 확인할 수 있는 function 제공
* Slice
  * Page 와 비슷하며 전체를 조회하지 않고 Slice 를 기준으로 자른 만큼만 조회하여 제공
* List type 으로 리턴 받을 수 있음, 페이지 function 을 사용하지 못할 뿐

```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    Page<Member> findByAge(int age, Pageable pageable);
}
```

Use

```java
public class Test {
    void paging() {
        PageRequest request = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        int age = 10;
        Page<Member> page = memberRepository.findByAge(age, request);
        List<Member> members = page.getContent();
    }
}
```

#### @Query - countQuery property

조인으로 인해서 쿼리가 복잡할 때 countQuery 를 설정하여 별개로 가져오도록 해야된다
Page 를 통해서 그냥 가져오게되면 복잡한 쿼리의 경우 그 복잡성을 가진 상태로 카운트를 하게 됨

```java
public class Test {
    @Query(
            value = "select m from Member m left join m.team t",
            countQuery = "select count(m) from Member m"
    )
    Page<Member> findByAge(int age, Pageable pageable);    
}
```

#### Page 객체 Dto 로 변환해서 API 에 바로 반환하기

```java
public class Test {
    public void pageToDto() {
        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getUsername(), m.getAge(), m.getTeam()));
    }
}
```

### 벌크성 수정 쿼리

* executeUPdate() -> repository 메서드에 @Modifying 사용
* JPQL 설명할 때 벌크연산 후 영속성 컨텍스트를 flush(), clear() 호출하여 준속성으로 변경하는 작업 필요
* 위 방법을 Spring Data JPA 에서는 @Modifying(clearAutomatically = true) 설정으로 해결 가능  


```java
public class 벌크_수정 {
    @Modifying
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);
}
```

### @EntityGraph

```java
public class fetch_join {
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @Override
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findAll();

    @EntityGraph(attributePaths = ("team"))
    List<Member> findEntityGraphByUsername(@Param("username") String username);
}
```

### JPA Hint & Lock

* SQL 힌트가 아니며 JPA 구현체(하이버네이트)에게 전달해주는 힌트
* Hint
  * 힌트_사용 클래스에 설정된 대로 해놓으면 변경 감지를 안하게 되므로 업데이트를 진행하지 않음


```java
public class 힌트_사용 {
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username); 
}
```

* Lock: @Lock
  * select ~ for update 쿼리 생성
  * 엄청 깊은 내용을 다루고 있으며 필요한 경우 책 참조
  * 최대한 안쓰는 방법 을 권장, 실시간 트래픽이 많은 경우 사용하지 말것


### 사용자 정의 리포지토리 구현

* 중요 *** 사용 규칙으로 JpaRepository 를 상속받고 있는 인터페이스 이름 + Impl 로 생성 *** 
* 위 와 같이 생성해야 Spring Data Jpa 가 메서드를 찾아서 실행해줌
* custom interface 생성
* interface 구현
* 기존에 사용하던 인터페이스 repository 에 custom interface 를 extends
* 생성한 메서드 호출해서 사용


```java
public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}

@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<Member> findMemberCustom() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }
}

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom { }
```

Use

```java
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;
    
    public void useCustomJPQLMethod() {
        List<Member> findMembers = memberRepository.findMemberCustom();
    }
}
```

### Auditing

순수 JPA 사용 방법

```java
@MappedSuperclass
public class JpaBaseEntity {

    @Column(updatable = false)
    private LocalDateTime createDate;

    private LocalDateTime updateDate;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createDate = now;
        updateDate = now;
    }

    @PreUpdate
    public void preUpdate() {
        updateDate = LocalDateTime.now();
    }
}

public class Member extends JpaBaseEntity { }
```

Spring Data JPA 가 지원하는 방법

* 아래 코드를 보면 auditor 값으로 UUID 를 넣고 있음
* Spring Security 를 사용하면 세션이나 토큰 값 체크해서 사용자 아이디를 넣어주도록 변경
* 시간, 등록 or 수정자 를 별개로 나눠야할 때 시간 클래스를 따로 만들어서 등록 or 수정자 클래스가 상속받도록 구조 변경


```java
@EnableJpaAuditing
@SpringBootApplication
public class DataJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataJpaApplication.class, args);
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of(UUID.randomUUID().toString());
    }
}

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(updatable = false)
    private String createBy;

    @LastModifiedBy
    private String lastModifiedBy;
}

public class Member extends BaseEntity { }
```

### Web 확장 - 도메인 클래스 컨버터

* findMember(), findMember2() 두 가지 방법으로 사용 가능
* 도메인 클래스 컨버터가 중간에 동작해서 회원 엔티티 객체를 반환
* 단순 조회용으로 사용하고 트랜잭션 없는 범위에서 조회하여 변경해도 DB 반영되지 않음
* 그러나 사용을 권하지 않고 이런식으로 사용할 수 있다 정도로 참고

```java
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        String username = memberRepository.findById(id).get().getUsername();
        return username;
    }

    @GetMapping("/members/{id}")
    public String findMember2(@PathVariable("id") Member member) {
        String username = memberRepository.findById(id).get().getUsername();
        return username;
    }
}
```

### Web 확장 - 페이징과 정렬

* 클라이언트에게 파라미터로 Pageable 을 받을 수 있음, Pageable: 인터페이스, PageRequest: 구현체
* 요청 파라미터(Pageable)
  * 예시 /members?page=0&size=3&sort=id,desc&sort=username,desc
  * page: 현재 페이지, 0 부터 시작(default: 20)
  * size: 한 페이지에 노출할 데이터 수(default: 2000)
  * sort: 정렬 조건 정의, asc 생략 가능
* page, size 수 변경
  * 전체: application.yaml -> data.web.pageable.default-page-size or max-page-size
  * 개별: 매개변수 Pageable 앞에 @PageableDefault(size = ?, sort = "") 적용
* Pageable 을 접두사(@Qualifier) 를 사용하여 매개변수 여러개로 받을 수 있음
* 

```java
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/members")
    public Page<Member> list(Pageable pageable) {
        return memberRepository.findAll(pageable);;
    }
}
```