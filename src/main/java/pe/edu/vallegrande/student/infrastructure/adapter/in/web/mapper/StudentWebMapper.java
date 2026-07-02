package pe.edu.vallegrande.student.infrastructure.adapter.in.web.mapper;

import pe.edu.vallegrande.student.domain.model.Student;
import pe.edu.vallegrande.student.infrastructure.adapter.in.web.dto.StudentRequest;
import pe.edu.vallegrande.student.infrastructure.adapter.in.web.dto.StudentResponse;
import org.springframework.stereotype.Component;

@Component
public class StudentWebMapper {

    public Student toDomain(StudentRequest request) {
        Student student = new Student();
        student.setDni(request.getDni());
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setPromotion(request.getPromotion());
        return student;
    }

    public StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getDni(),
                student.getFirstName(),
                student.getLastName(),
                student.getPromotion(),
                student.getDate()
        );
    }
}
