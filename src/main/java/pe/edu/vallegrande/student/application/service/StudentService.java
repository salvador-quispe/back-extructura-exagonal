package pe.edu.vallegrande.student.application.service;

import pe.edu.vallegrande.student.domain.model.Student;
import pe.edu.vallegrande.student.domain.port.in.StudentUseCase;
import pe.edu.vallegrande.student.domain.port.out.StudentRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Capa de APLICACION: orquesta el caso de uso. No conoce HTTP ni JPA,
 * solo depende de los puertos (in/out) del dominio.
 */
@Service
public class StudentService implements StudentUseCase {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepositoryPort repository;

    public StudentService(StudentRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<Student> findAll() {
        log.info("ACCION=INVOCAR recurso=student detalle=listado-completo");
        return repository.findAll();
    }

    @Override
    public Student findByDni(String dni) {
        log.info("ACCION=INVOCAR recurso=student dni={}", dni);
        return repository.findByDni(dni)
                .orElseThrow(() -> new NoSuchElementException("Student no encontrado con dni=" + dni));
    }

    @Override
    public Student create(Student student) {
        if (repository.existsByDni(student.getDni())) {
            throw new IllegalStateException("Ya existe un student con dni=" + student.getDni());
        }
        student.setDate(LocalDateTime.now());
        Student saved = repository.save(student);
        log.info("ACCION=REGISTRAR dni={} firstName={} lastName={} promotion={} fecha={}",
                saved.getDni(), saved.getFirstName(), saved.getLastName(), saved.getPromotion(), saved.getDate());
        return saved;
    }

    @Override
    public Student update(String dni, Student student) {
        Student existing = findByDni(dni);
        existing.setFirstName(student.getFirstName());
        existing.setLastName(student.getLastName());
        existing.setPromotion(student.getPromotion());
        existing.setDate(LocalDateTime.now());
        Student updated = repository.save(existing);
        log.info("ACCION=ACTUALIZAR dni={} firstName={} lastName={} promotion={} fecha={}",
                updated.getDni(), updated.getFirstName(), updated.getLastName(), updated.getPromotion(), updated.getDate());
        return updated;
    }

    @Override
    public void delete(String dni) {
        Student existing = findByDni(dni);
        repository.deleteByDni(dni);
        log.info("ACCION=ELIMINAR dni={} firstName={} lastName={}",
                existing.getDni(), existing.getFirstName(), existing.getLastName());
    }
}
