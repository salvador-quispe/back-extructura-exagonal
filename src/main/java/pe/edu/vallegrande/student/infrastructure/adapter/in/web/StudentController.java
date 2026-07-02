package pe.edu.vallegrande.student.infrastructure.adapter.in.web;

import pe.edu.vallegrande.student.domain.model.Student;
import pe.edu.vallegrande.student.domain.port.in.StudentUseCase;
import pe.edu.vallegrande.student.infrastructure.adapter.in.web.dto.StudentRequest;
import pe.edu.vallegrande.student.infrastructure.adapter.in.web.dto.StudentResponse;
import pe.edu.vallegrande.student.infrastructure.adapter.in.web.mapper.StudentWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Adaptador de ENTRADA (REST). Solo traduce HTTP <-> dominio, sin logica de negocio.
 *
 * Endpoints:
 *   GET    /v1/api/student        -> lista todos
 *   GET    /v1/api/student/{dni}  -> obtiene uno
 *   POST   /v1/api/student        -> crea
 *   PUT    /v1/api/student/{dni}  -> actualiza
 *   DELETE /v1/api/student/{dni}  -> elimina
 *
 * Si cambias el recurso, renombra esta clase y actualiza la RUTA_BASE.
 */
@RestController
@RequestMapping("/v1/api/student")
public class StudentController {

    private final StudentUseCase studentUseCase;
    private final StudentWebMapper mapper;

    public StudentController(StudentUseCase studentUseCase, StudentWebMapper mapper) {
        this.studentUseCase = studentUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public List<StudentResponse> findAll() {
        return studentUseCase.findAll().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{dni}")
    public StudentResponse findByDni(@PathVariable String dni) {
        Student student = studentUseCase.findByDni(dni);
        return mapper.toResponse(student);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(@Valid @RequestBody StudentRequest request) {
        Student created = studentUseCase.create(mapper.toDomain(request));
        return mapper.toResponse(created);
    }

    @PutMapping("/{dni}")
    public StudentResponse update(@PathVariable String dni, @Valid @RequestBody StudentRequest request) {
        Student updated = studentUseCase.update(dni, mapper.toDomain(request));
        return mapper.toResponse(updated);
    }

    @DeleteMapping("/{dni}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String dni) {
        studentUseCase.delete(dni);
    }
}
