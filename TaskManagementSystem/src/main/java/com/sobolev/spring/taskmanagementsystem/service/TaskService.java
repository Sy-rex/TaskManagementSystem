package com.sobolev.spring.taskmanagementsystem.service;

import com.sobolev.spring.taskmanagementsystem.dto.CommentDTO;
import com.sobolev.spring.taskmanagementsystem.dto.CreateTaskRequest;
import com.sobolev.spring.taskmanagementsystem.dto.TaskDTO;
import com.sobolev.spring.taskmanagementsystem.dto.UpdateTaskRequest;
import com.sobolev.spring.taskmanagementsystem.exception.AccessDeniedException;
import com.sobolev.spring.taskmanagementsystem.exception.ResourceNotFoundException;
import com.sobolev.spring.taskmanagementsystem.model.Task;
import com.sobolev.spring.taskmanagementsystem.model.TaskAssignee;
import com.sobolev.spring.taskmanagementsystem.model.TaskComment;
import com.sobolev.spring.taskmanagementsystem.model.User;
import com.sobolev.spring.taskmanagementsystem.model.embedded.TaskAssigneeId;
import com.sobolev.spring.taskmanagementsystem.repository.TaskAssigneeRepository;
import com.sobolev.spring.taskmanagementsystem.repository.TaskCommentRepository;
import com.sobolev.spring.taskmanagementsystem.repository.TaskRepository;
import com.sobolev.spring.taskmanagementsystem.repository.UserRepository;
import com.sobolev.spring.taskmanagementsystem.util.TaskPriority;
import com.sobolev.spring.taskmanagementsystem.util.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskCommentRepository taskCommentRepository;


    public List<TaskDTO> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream().map(this::convertToDTO).toList();
    }

    public TaskDTO getTaskById(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача с ID " + taskId + " не найдена"));

        return convertToDTO(task);
    }


    @Transactional
    public TaskDTO createTask(CreateTaskRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authorName = authentication.getName();

        // Находим автора задачи
        User author = userRepository.findByUsername(authorName)
                .orElseThrow(() -> new ResourceNotFoundException("Автор задания не найден: " + authorName));

        // Получаем исполнителей по именам
        List<User> assignees = userRepository.findByUsernameIn(request.getAssigneeNames());

        // Проверяем, что все исполнители найдены
        if (assignees.size() != request.getAssigneeNames().size()) {
            List<String> foundAssignees = assignees.stream()
                    .map(User::getUsername)
                    .toList();

            List<String> notFoundAssignees = request.getAssigneeNames().stream()
                    .filter(name -> !foundAssignees.contains(name))
                    .toList();

            throw new ResourceNotFoundException("Исполнитель не найден: " + String.join(", ", notFoundAssignees));
        }

        // Создаем и сохраняем задачу
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.valueOf(request.getStatus()));
        task.setPriority(TaskPriority.valueOf(request.getPriority()));
        task.setAuthor(author);

        task = taskRepository.save(task);

        Integer taskId = task.getId(); // Получаем ID задачи после сохранения

        // Создаем связи TaskAssignee и сохраняем их
        List<TaskAssignee> taskAssignees = new ArrayList<>();

        for (User assignee : assignees) {
            TaskAssignee taskAssignee = new TaskAssignee();
            taskAssignee.setId(new TaskAssigneeId(taskId, assignee.getId()));
            taskAssignee.setTask(task);
            taskAssignee.setUser(assignee);
            taskAssignee.setAssignedAt(new Date());

            taskAssignees.add(taskAssignee);
        }

        taskAssigneeRepository.saveAll(taskAssignees);

        task.setAssignees(taskAssignees);
        taskRepository.save(task);

        return convertToDTO(task);
    }




    public List<TaskDTO> getTasksByAssignee(User user) {
        List<Task> tasks = taskRepository.findTasksByAssigneeWithDetails(user);
        return tasks.stream().map(this::convertToDTO).toList();
    }

    public TaskDTO getTaskWithPermissionsCheck(Integer taskId, User user) {
        Task task = taskRepository.findByIdWithAssignees(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задание не найдено"));

        boolean isAuthor = task.getAuthor().getId().equals(user.getId());
        boolean isAssignee = task.getAssignees().stream()
                .anyMatch(ta -> ta.getUser().getId().equals(user.getId()));

        if (!isAuthor && !isAssignee) {
            throw new AccessDeniedException("Доступ запрещен");
        }

        return convertToDTO(task);
    }

    // Обновление статуса задачи
    @Transactional
    public TaskDTO updateTaskStatus(Integer taskId, TaskStatus newStatus, User currentUser) {
        Task task = taskRepository.findByIdWithAssignees(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена"));

        checkAssigneePermissions(task, currentUser);

        task.setStatus(newStatus);
        task.setUpdatedAt(new Date());

        return convertToDTO(taskRepository.save(task));
    }

    // Добавление комментария для Пользователя
    @Transactional
    public CommentDTO addComment(Integer taskId, String commentText, User user) {
        if (user == null) {
            throw new AccessDeniedException("Пользователь не аутентифицирован");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена"));

        checkCommentPermissions(task, user);

        TaskComment comment = TaskComment.builder()
                .commentText(commentText)
                .task(task)
                .user(user)
                .createdAt(new Date())
                .build();

        TaskComment savedComment = taskCommentRepository.save(comment);
        return convertToCommentDTO(savedComment);
    }

    // Добавление комментария для админа
    @Transactional
    public CommentDTO addComment(Integer taskId, String commentText) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = authentication.getName();

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Администратор не найден: " + adminUsername));

        // Проверяем, существует ли задача
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задание не найдено с id:" + taskId));

        // Создаем комментарий
        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setUser(admin);
        comment.setCommentText(commentText);
        comment.setCreatedAt(new Date());

        TaskComment savedComment = taskCommentRepository.save(comment);

        return convertToCommentDTO(savedComment);
    }

    // Обновление задания
    @Transactional
    public TaskDTO updateTask(Integer taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задание не найдено с id: " + taskId));

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            task.setStatus(TaskStatus.valueOf(request.getStatus()));
        }

        if (request.getPriority() != null) {
            task.setPriority(TaskPriority.valueOf(request.getPriority()));
        }

        // Обрабатываем обновление исполнителей, если они заданы
        if (request.getAssigneeNames() != null) {
            List<User> assignees = userRepository.findByUsernameIn(request.getAssigneeNames());

            // Проверяем, что все исполнители найдены
            if (assignees.size() != request.getAssigneeNames().size()) {
                List<String> foundAssignees = assignees.stream()
                        .map(User::getUsername)
                        .toList();

                List<String> notFoundAssignees = request.getAssigneeNames().stream()
                        .filter(name -> !foundAssignees.contains(name))
                        .toList();

                throw new ResourceNotFoundException("Исполнитель не найден: " + String.join(", ", notFoundAssignees));
            }

            // Удаляем существующих исполнителей задачи
            taskAssigneeRepository.deleteByTaskId(task.getId());

            // Создаем и сохраняем новые связи исполнителей
            List<TaskAssignee> taskAssignees = new ArrayList<>();

            for (User assignee : assignees) {
                TaskAssignee taskAssignee = new TaskAssignee();
                taskAssignee.setId(new TaskAssigneeId(task.getId(), assignee.getId()));
                taskAssignee.setTask(task);
                taskAssignee.setUser(assignee);
                taskAssignee.setAssignedAt(new Date());

                taskAssignees.add(taskAssignee);
            }

            taskAssigneeRepository.saveAll(taskAssignees);
            task.setAssignees(taskAssignees);
        }

        task = taskRepository.save(task);

        return convertToDTO(task);
    }

    // Удаление задания
    @Transactional
    public void deleteTask(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задание не найдено с id: " + taskId));

        // Удаляем всех исполнителей задачи
        taskAssigneeRepository.deleteByTaskId(taskId);

        // Удаляем комментарии, связанные с задачей
        taskCommentRepository.deleteByTaskId(taskId);

        // Удаляем задачу
        taskRepository.delete(task);
    }

    // Изменение статуса задачи
    @Transactional
    public TaskDTO updateTaskStatus(Integer taskId, String status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задание не найдено с id: " + taskId));

        TaskStatus taskStatus;
        try {
            taskStatus = TaskStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Неправильное значение статуса: " + status);
        }

        task.setStatus(taskStatus);
        task = taskRepository.save(task);

        return convertToDTO(task);
    }


    // Изменение приоритета задания
    @Transactional
    public TaskDTO updateTaskPriority(Integer taskId, String priority) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задание не найдено с id: " + taskId));

        TaskPriority taskPriority;
        try {
            taskPriority = TaskPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Неправильное значение приоритета: " + priority);
        }

        task.setPriority(taskPriority);
        task = taskRepository.save(task);

        return convertToDTO(task);
    }

    // Назначить исполнителя для задачи
    @Transactional
    public TaskDTO assignUserToTask(Integer taskId, Integer userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задание не найдено с id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с id: " + userId));

        // Проверить, назначен ли уже пользователь
        boolean isAlreadyAssigned = taskAssigneeRepository.existsById(new TaskAssigneeId(taskId, userId));
        if (isAlreadyAssigned) {
            throw new IllegalArgumentException("Пользователь с id: " + userId + " уже работает над этой задачей" + taskId);
        }

        TaskAssignee taskAssignee = new TaskAssignee();
        taskAssignee.setId(new TaskAssigneeId(taskId, userId));
        taskAssignee.setTask(task);
        taskAssignee.setUser(user);
        taskAssignee.setAssignedAt(new Date());

        taskAssigneeRepository.save(taskAssignee);

        task = taskRepository.save(task);

        return convertToDTO(task);
    }

    // Проверка имеет ли доступ к заданию
    private void checkAssigneePermissions(Task task, User user) {
        boolean isAssignee = task.getAssignees().stream()
                .anyMatch(ta -> ta.getUser().getId().equals(user.getId()));

        if (!isAssignee) {
            throw new AccessDeniedException("Пользователь не имеет доступа к заданию");
        }
    }

    private void checkCommentPermissions(Task task, User user) {
        boolean isAuthor = task.getAuthor().getId().equals(user.getId());
        boolean isAssignee = task.getAssignees().stream()
                .anyMatch(ta -> ta.getUser().getId().equals(user.getId()));

        if (!isAuthor && !isAssignee) {
            throw new AccessDeniedException("Пользователь не может комментировать это задание");
        }
    }

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus().name());
        dto.setPriority(task.getPriority().name());
        dto.setAuthorName(task.getAuthor().getUsername());

        dto.setAssignees(task.getAssignees().stream()
                .map(ta -> ta.getUser() != null ? ta.getUser().getUsername() : "Unknown")
                .toList());

        // Загружаем комментарии, отсортированные по дате
        List<CommentDTO> comments = taskCommentRepository.findByTaskIdOrderByCreatedAtDesc(task.getId())
                .stream()
                .map(this::convertToCommentDTO)
                .toList();

        dto.setComments(comments);

        return dto;
    }

    private CommentDTO convertToCommentDTO(TaskComment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .commentText(comment.getCommentText())
                .authorName(comment.getUser().getUsername())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
