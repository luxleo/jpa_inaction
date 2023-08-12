package jpabook.jpashop.repository.springdatajpa;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * BaseTimeEntity를 상속 받는 방식으로 생성,수정 (일자, 작성자)의 적용용례의 폭을 넓힘
 */
@MappedSuperclass @Getter
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity extends BaseTimeEntity{
    // 밑의 두개는 SpringBootApplication에 annotation, AuditorAware<T>해주어야한다. JpashopApplication참조
    @CreatedBy
    @Column(updatable = false)
    private String createdBy;
    @LastModifiedBy
    private String lastModifiedBy;

}
