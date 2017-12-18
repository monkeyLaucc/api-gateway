package cc.lau.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;


@Data
@NoArgsConstructor
public class Student {
    private Integer id;
    @NotBlank
    private String name;

    public Student(String name) {
        this.name = name;
    }
}
