package jpabook.jpashop.repository.queryDsl.cond;

import lombok.Builder;
import lombok.Data;

@Data
public class QMemberSearchCond {
    private String memberName;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
    @Builder
    public QMemberSearchCond(String memberName, String teamName, Integer ageGoe, Integer ageLoe) {
        this.memberName = memberName;
        this.teamName = teamName;
        this.ageGoe = ageGoe;
        this.ageLoe = ageLoe;
    }
}
