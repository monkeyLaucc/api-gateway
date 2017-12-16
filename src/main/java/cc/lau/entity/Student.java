package cc.lau.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Student {
    private Integer id;
    private String name;

    public Student(String name) {
        this.name = name;
    }
}
