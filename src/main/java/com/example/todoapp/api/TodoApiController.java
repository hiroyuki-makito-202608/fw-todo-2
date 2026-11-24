package com.example.todoapp.api;

import java.util.List;
import java.net.URI;

import com.example.todoapp.Todo;
import com.example.todoapp.TodoService;
import jakarta.validation.Valid;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class TodoApiController {
    private final TodoService todoService;

    public TodoApiController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/api/todos")
    public List<TodoDto> todos(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String order) {
        return todoService.search(keyword, category, order)
                .stream()
                .map(TodoDto::from)
                .toList();
    }

    @GetMapping("/api/todos/{id}")
    public ResponseEntity<?> todo(@PathVariable Long id) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            return notFound(id);
        }
        return ResponseEntity.ok(TodoDto.from(todo));
    }

    @PostMapping("/api/todos")
    public ResponseEntity<TodoDto> create(@Valid @RequestBody TodoRequest request) {
        Todo todo = toTodo(request);
        if (todo.getCompleted() == null) {
            todo.setCompleted(false);
        }
        todoService.create(todo);
        Todo created = todoService.findById(todo.getId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(todo.getId())
                .toUri();
        return ResponseEntity.created(location).body(TodoDto.from(created));
    }

    @PutMapping("/api/todos/{id}")
    public ResponseEntity<?> replace(@PathVariable Long id, @Valid @RequestBody TodoRequest request) {
        if (todoService.findById(id) == null) {
            return notFound(id);
        }
        Todo todo = toTodo(request);
        todo.setId(id);
        todoService.update(todo);
        return ResponseEntity.ok(TodoDto.from(todoService.findById(id)));
    }

    @DeleteMapping("/api/todos/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (todoService.findById(id) == null) {
            return notFound(id);
        }
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<ProblemDetail> notFound(Long id) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, "Todo with id " + id + " was not found.");
        problem.setTitle("Todo not found");
        problem.setInstance(URI.create("/api/todos/" + id));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BadRequestResponse> validationError(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<FieldErrorResponse> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::fieldErrorResponse)
                .toList();
        String instance = request.getRequestURI();
        if (request.getQueryString() != null) {
            instance += "?" + request.getQueryString();
        }
        return ResponseEntity.badRequest().body(new BadRequestResponse(
                "about:blank", "Bad Request", 400, "入力に誤りがあります", instance, errors));
    }

    private FieldErrorResponse fieldErrorResponse(FieldError error) {
        return new FieldErrorResponse(error.getField(), error.getDefaultMessage());
    }

    private Todo toTodo(TodoRequest request) {
        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        todo.setDetail(request.getDetail());
        todo.setCategory(request.getCategory());
        todo.setPriority(request.getPriority());
        todo.setDueDate(request.getDueDate());
        todo.setCompleted(request.getCompleted());
        return todo;
    }

    public record FieldErrorResponse(String field, String message) { }

    public record BadRequestResponse(String type, String title, int status, String detail,
            String instance, List<FieldErrorResponse> errors) { }
}
