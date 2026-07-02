package pe.edu.vallegrande.student.domain.port.out;

import pe.edu.vallegrande.student.domain.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de SALIDA (driven port). La capa de aplicacion depende de esta interfaz;
 * la infraestructura (JPA, Mongo, memoria, etc.) la implementa.
 */
public interface StudentRepositoryPort {

    List<Student> findAll();

    Optional<Student> findByDni(String dni);

    boolean existsByDni(String dni);

    Student save(Student student);

    void deleteByDni(String dni);
}
