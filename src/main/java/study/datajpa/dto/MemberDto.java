package study.datajpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import study.datajpa.entity.Team;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    private String username;
    private int age;
    private Team team;
}
