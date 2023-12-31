package FXPROJECT.CHECKPASS.domain.entity.users;

import FXPROJECT.CHECKPASS.domain.enums.Job;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * CHECKPASS-13
 * User Management System DB 구축
 */

@Getter
@Setter
@MappedSuperclass
public abstract class Users {

    @Id
    @Column(nullable = false)
    private Long userId;

    @OneToOne
    private Account account;

    @Column(nullable = false , length = 3)
    private int userAge;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String userCollege;

    @Column(nullable = false)
    private String userDepartment;

    @Column(nullable = false , length = 5)
    private String userName;

    @Column(nullable = false)
    @Enumerated
    private Job userJob;

}
