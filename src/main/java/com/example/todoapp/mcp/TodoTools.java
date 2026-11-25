package com.example.todoapp.mcp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.todoapp.Todo;
import com.example.todoapp.TodoService;
import com.example.todoapp.api.TodoDto;
import com.example.todoapp.api.HolidayClient;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class TodoTools {
    private static final Set<String> CATEGORIES = Set.of("デザイン", "マーケティング", "プログラミング", "資格", "就職活動");
    private final TodoService todoService;
    private final HolidayClient holidayClient;

    public TodoTools(TodoService todoService, HolidayClient holidayClient) {
        this.todoService = todoService;
        this.holidayClient = holidayClient;
    }

    @McpTool(name = "list_todos", description = "やることの一覧を返す（期間・ジャンルで絞れる）")
    public List<TodoDto> listTodos(
            @McpToolParam(description = "キーワード", required = false) String keyword,
            @McpToolParam(description = "ジャンル", required = false) String category,
            @McpToolParam(description = "開始日（yyyy-MM-dd）", required = false) String from,
            @McpToolParam(description = "終了日（yyyy-MM-dd）", required = false) String to) {
        LocalDate fromDate = from == null ? null : LocalDate.parse(from);
        LocalDate toDate = to == null ? null : LocalDate.parse(to);

        return todoService.search(keyword, category, "asc", fromDate, toDate)
                .stream()
                .map(TodoDto::from)
                .toList();
    }

    @McpTool(name = "get_todo", description = "やることを1件返す")
    public TodoDto getTodo(
            @McpToolParam(description = "やることのID", required = true) Long id) {
        Todo todo = todoService.findById(id);
        return todo == null ? null : TodoDto.from(todo);
    }

    @McpTool(name = "add_todo", description = "やることを1件足す")
    public TodoDto addTodo(
            @McpToolParam(description = "やることのタイトル", required = true) String title,
            @McpToolParam(description = "ジャンル（デザイン・マーケティング・プログラミング・資格・就職活動）", required = true) String category,
            @McpToolParam(description = "優先度（1=高、2=中、3=低）", required = true) Integer priority,
            @McpToolParam(description = "期限（yyyy-MM-dd）", required = false) String dueDate,
            @McpToolParam(description = "詳細", required = false) String detail) {
        validateText("タイトル", title);
        validateText("ジャンル", category);
        if (detail != null) validateText("詳細", detail);
        validateCategory(category);
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setCategory(category);
        todo.setPriority(priority);
        todo.setDueDate(dueDate == null ? null : LocalDate.parse(dueDate));
        todo.setDetail(detail);
        todo.setCompleted(false);
        todoService.create(todo);
        return TodoDto.from(todo);
    }

    @McpTool(name = "update_todo", description = "やることを1件直す（期限を変えるのもこれ）")
    public TodoDto updateTodo(
            @McpToolParam(description = "やることのID", required = true) Long id,
            @McpToolParam(description = "新しいタイトル", required = false) String title,
            @McpToolParam(description = "新しい詳細", required = false) String detail,
            @McpToolParam(description = "新しいジャンル（デザイン・マーケティング・プログラミング・資格・就職活動）", required = false) String category,
            @McpToolParam(description = "新しい優先度（1=高、2=中、3=低）", required = false) Integer priority,
            @McpToolParam(description = "新しい期限（yyyy-MM-dd）", required = false) String dueDate) {
        Todo todo = requireTodo(id);
        if (title != null) {
            validateText("タイトル", title);
            todo.setTitle(title);
        }
        if (detail != null) {
            validateText("詳細", detail);
            todo.setDetail(detail);
        }
        if (category != null) {
            validateText("ジャンル", category);
            validateCategory(category);
            todo.setCategory(category);
        }
        if (priority != null) todo.setPriority(priority);
        if (dueDate != null) todo.setDueDate(LocalDate.parse(dueDate));
        todoService.updateProvidedFields(todo, title != null, detail != null, category != null,
                priority != null, dueDate != null);
        return TodoDto.from(todo);
    }

    @McpTool(name = "complete_todo", description = "やることを完了にする")
    public TodoDto completeTodo(
            @McpToolParam(description = "やることのID", required = true) Long id) {
        Todo todo = requireTodo(id);
        todo.setCompleted(true);
        todoService.setCompleted(id, true);
        return TodoDto.from(todo);
    }

    @McpTool(name = "delete_todo", description = "やることを1件消す")
    public String deleteTodo(
            @McpToolParam(description = "やることのID", required = true) Long id) {
        requireTodo(id);
        todoService.delete(id);
        return "やることを削除しました（ID: " + id + "）";
    }

    @McpTool(name = "find_free_days", description = "期間の中で、期限のやることが無く、土日でも祝日でもない「空いている日」を返す。やることの期限を動かす先を決めるのに使う")
    public List<LocalDate> findFreeDays(
            @McpToolParam(description = "期間の開始日（yyyy-MM-dd）", required = true) String from,
            @McpToolParam(description = "期間の終了日（yyyy-MM-dd）", required = true) String to) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("終了日は開始日以降の日付にしてください");
        }

        Set<LocalDate> occupiedDates = todoService.search(null, null, "asc", fromDate, toDate).stream()
                .map(Todo::getDueDate)
                .filter(date -> date != null)
                .collect(Collectors.toSet());
        Map<String, String> holidays = holidayClient.getHolidays(fromDate, toDate).holidays();
        Set<LocalDate> holidayDates = holidays.keySet().stream()
                .map(LocalDate::parse)
                .collect(Collectors.toSet());

        List<LocalDate> freeDays = new ArrayList<>();
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            if (occupiedDates.contains(date)) continue;
            if (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) continue;
            if (holidayDates.contains(date)) continue;
            freeDays.add(date);
        }
        return freeDays;
    }

    private Todo requireTodo(Long id) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            throw new IllegalArgumentException("指定されたIDのやることが見つかりません: " + id);
        }
        return todo;
    }

    private void validateCategory(String category) {
        if (!CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("ジャンルはデザイン・マーケティング・プログラミング・資格・就職活動から選んでください");
        }
    }

    private void validateText(String fieldName, String value) {
        if (value.indexOf(0xFFFD) >= 0) {
            throw new IllegalArgumentException(fieldName + "に文字コード変換エラーを示す文字が含まれています。日本語をそのまま再入力してください");
        }
    }
}
