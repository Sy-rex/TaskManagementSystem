package com.sobolev.spring.taskmanagementsystem.service;

import com.sobolev.spring.taskmanagementsystem.dto.CommentDTO;
import com.sobolev.spring.taskmanagementsystem.dto.CreateTaskRequest;
import com.sobolev.spring.taskmanagementsystem.dto.TaskDTO;
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
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final ModelMapper modelMapper;

    public Optional<Task> findById(Integer id) {
        return taskRepository.findById(id);
    }

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
                .orElseThrow(() -> new ResourceNotFoundException("Author not found: " + authorName));

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

            throw new ResourceNotFoundException("Assignees not found: " + String.join(", ", notFoundAssignees));
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

        // Сохраняем все связи исполнителей с задачей
        taskAssigneeRepository.saveAll(taskAssignees);

        // Обновляем список исполнителей в задаче и сохраняем задачу
        task.setAssignees(taskAssignees);
        taskRepository.save(task);

        // Возвращаем DTO задачи
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

    // Добавление комментария

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

    public List<CommentDTO> getTaskComments(Integer taskId) {
        return taskCommentRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(this::convertToCommentDTO)
                .toList();
    }

    private void checkAssigneePermissions(Task task, User user) {
        boolean isAssignee = task.getAssignees().stream()
                .anyMatch(ta -> ta.getUser().getId().equals(user.getId()));

        if (!isAssignee) {
            throw new AccessDeniedException("User is not assigned to this task");
        }
    }

    // Проверка прав на комментирование
    private void checkCommentPermissions(Task task, User user) {
        boolean isAuthor = task.getAuthor().getId().equals(user.getId());
        boolean isAssignee = task.getAssignees().stream()
                .anyMatch(ta -> ta.getUser().getId().equals(user.getId()));

        if (!isAuthor && !isAssignee) {
            throw new AccessDeniedException("User has no permissions to comment this task");
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
