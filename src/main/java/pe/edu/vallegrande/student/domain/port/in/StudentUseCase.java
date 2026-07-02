package pe.edu.vallegrande.student.domain.port.in;

import pe.edu.vallegrande.student.domain.model.Student;

import java.util.List;

/**
 * Puerto de ENTRADA (driving port). Los adaptadores de entrada (REST, CLI, etc.)
 * dependen solo de esta interfaz, nunca de la implementacion.
 */
public interface StudentUseCase {

    List<Student> findAll();

    Student findByDni(String dni);

    Student create(Student student);

    Student update(String dni, Student student);

    void delete(String dni);
}
