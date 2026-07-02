package pe.edu.vallegrande.student.infrastructure.adapter.out.persistence;

import pe.edu.vallegrande.student.domain.model.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentPersistenceMapper {

    public StudentEntity toEntity(Student student) {
        StudentEntity entity = new StudentEntity();
        entity.setId(student.getId());
        entity.setDni(student.getDni());
        entity.setFirstName(student.getFirstName());
        entity.setLastName(student.getLastName());
        entity.setPromotion(student.getPromotion());
        entity.setDate(student.getDate());
        return entity;
    }

    public Student toDomain(StudentEntity entity) {
        return new Student(
                entity.getId(),
                entity.getDni(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPromotion(),
                entity.getDate()
        );
    }
}
