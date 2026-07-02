package pe.edu.vallegrande.student.infrastructure.adapter.out.persistence;

import pe.edu.vallegrande.student.domain.model.Student;
import pe.edu.vallegrande.student.domain.port.out.StudentRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de SALIDA: implementa el puerto usando JPA/H2/MySQL.
 * Si cambias de motor de persistencia (Mongo, memoria, etc.) solo tocas esta clase.
 */
@Component
public class StudentRepositoryAdapter implements StudentRepositoryPort {

    private final StudentJpaRepository jpaRepository;
    private final StudentPersistenceMapper mapper;

    public StudentRepositoryAdapter(StudentJpaRepository jpaRepository, StudentPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Student> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Student> findByDni(String dni) {
        return jpaRepository.findByDni(dni).map(mapper::toDomain);
    }

    @Override
    public boolean existsByDni(String dni) {
        return jpaRepository.existsByDni(dni);
    }

    @Override
    public Student save(Student student) {
        StudentEntity entity = mapper.toEntity(student);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public void deleteByDni(String dni) {
        jpaRepository.deleteByDni(dni);
    }
}
