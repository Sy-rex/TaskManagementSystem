package com.sobolev.spring.taskmanagementsystem.service;

import com.sobolev.spring.taskmanagementsystem.dto.CommentDTO;
import com.sobolev.spring.taskmanagementsystem.dto.TaskDTO;
import com.sobolev.spring.taskmanagementsystem.exception.AccessDeniedException;
import com.sobolev.spring.taskmanagementsystem.exception.ResourceNotFoundException;
import com.sobolev.spring.taskmanagementsystem.model.Task;
import com.sobolev.spring.taskmanagementsystem.model.TaskComment;
import com.sobolev.spring.taskmanagementsystem.model.User;
import com.sobolev.spring.taskmanagementsystem.repository.TaskAssigneeRepository;
import com.sobolev.spring.taskmanagementsystem.repository.TaskCommentRepository;
import com.sobolev.spring.taskmanagementsystem.repository.TaskRepository;
import com.sobolev.spring.taskmanagementsystem.util.TaskPriority;
import com.sobolev.spring.taskmanagementsystem.util.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final ModelMapper modelMapper;

    public Optional<Task> findById(Integer id) {
        return taskRepository.findById(id);
    }

    public List<Task> findAllTasksByAuthor(User author) {
        return taskRepository.findAllByAuthor(author);
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
        dto.setStatus(task.getStatus().name()); // Enum будет автоматически конвертирован через конвертер
        dto.setPriority(task.getPriority().name());
        dto.setAuthorName(task.getAuthor().getUsername());

        // Используем безопасный null-check
        dto.setAssignees(task.getAssignees().stream()
                .map(ta -> ta.getUser() != null ? ta.getUser().getUsername() : "Unknown")
                .collect(Collectors.toList()));

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
