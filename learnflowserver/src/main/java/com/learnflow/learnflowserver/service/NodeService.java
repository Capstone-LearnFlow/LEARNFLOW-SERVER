package com.learnflow.learnflowserver.service;

import com.learnflow.learnflowserver.domain.Assignment;
import com.learnflow.learnflowserver.domain.Evidence;
import com.learnflow.learnflowserver.domain.Node;
import com.learnflow.learnflowserver.domain.StudentAssignment;
import com.learnflow.learnflowserver.domain.common.enums.CreatedBy;
import com.learnflow.learnflowserver.domain.common.enums.NodeType;
import com.learnflow.learnflowserver.domain.common.enums.StudentStatus;
import com.learnflow.learnflowserver.domain.common.enums.TargetType;
import com.learnflow.learnflowserver.dto.request.*;
import com.learnflow.learnflowserver.dto.response.*;
import com.learnflow.learnflowserver.dto.response.ai.StudentResponseWithAiResponse;
import com.learnflow.learnflowserver.repository.AssignmentRepository;
import com.learnflow.learnflowserver.repository.EvidenceRepository;
import com.learnflow.learnflowserver.repository.NodeRepository;
import com.learnflow.learnflowserver.repository.StudentAssignmentRepository;
import com.learnflow.learnflowserver.service.ai.AiClient;
import com.learnflow.learnflowserver.service.ai.AiReviewService;
import com.learnflow.learnflowserver.service.ai.AsyncAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NodeService {

    private final NodeRepository nodeRepository;
    private final EvidenceRepository evidenceRepository;
    private final StudentAssignmentRepository studentAssignmentRepository;
    private final AiClient aiClient;
    private final AssignmentRepository assignmentRepository;
    private final AiReviewService aiReviewService;
    private final AsyncAiService asyncAiService;

//    @Transactional
//    public NodeResponse createMainNode(Long studentAssignmentId, NodeCreateRequest request) {
//        // 학생-과제 연결 정보 조회
//        StudentAssignment studentAssignment = studentAssignmentRepository.findById(studentAssignmentId)
//                .orElseThrow(() -> new IllegalArgumentException("학생-과제 연결 정보를 찾을 수 없습니다."));
//
//        Assignment assignment = studentAssignment.getAssignment(); // 이 부분이 실제 코드와 다를 수 있음
//        if (assignment == null) {
//            throw new IllegalArgumentException("과제 정보를 찾을 수 없습니다.");
//        }
//        String title = assignment.getTopic();
//
//        // AI를 통한 요약 생성 (메인 노드의 내용과 근거 목록)
//        List<String> contentToSummarize = new ArrayList<>();
//        contentToSummarize.add(request.getContent());
//        request.getEvidences().forEach(evidence -> contentToSummarize.add(evidence.getContent()));
//
//        List<String> summaries = aiClient.getSummaries(contentToSummarize);
//
//        // 메인 노드 생성 및 저장
//        Node node = Node.builder()
//                .studentAssignment(studentAssignment)
//                .content(request.getContent())
//                .summary(summaries.get(0))  // 첫 번째 요약은 메인 노드의 요약
//                .type(NodeType.CLAIM)
//                .createdBy(CreatedBy.STUDENT)
//                .isHidden(false)
//                .build();
//
//        Node savedNode = nodeRepository.save(node);
//
//        // 근거 생성 및 저장
//        List<Evidence> evidences = new ArrayList<>();
//        for (int i = 0; i < request.getEvidences().size(); i++) {
//            EvidenceCreateRequest evidenceRequest = request.getEvidences().get(i);
//            String summary = summaries.get(i + 1);  // 첫 번째 요약은 메인 노드의 요약이므로 i+1
//
//            Evidence evidence = Evidence.builder()
//                    .node(savedNode)
//                    .content(evidenceRequest.getContent())
//                    .summary(summary)
//                    .source(evidenceRequest.getSource())
//                    .url(evidenceRequest.getUrl())
//                    .createdBy(CreatedBy.STUDENT)
//                    .build();
//            savedNode.addEvidence(evidence);
//            evidences.add(evidenceRepository.save(evidence));
//        }
//
//        // 응답 생성
//        List<EvidenceResponse> evidenceResponses = evidences.stream()
//                .map(EvidenceResponse::from)
//                .collect(Collectors.toList());
//
//        return NodeResponse.of(savedNode, title, evidenceResponses);
//    }
    @Transactional
    public NodeResponse createMainNode(Long studentAssignmentId, NodeCreateRequest request) {
        System.out.println("=== 학생 메인 노드 생성 시작 ===");
        System.out.println("Thread: " + Thread.currentThread().getName());

        // 학생 노드 생성
        StudentAssignment studentAssignment = studentAssignmentRepository.findById(studentAssignmentId)
                .orElseThrow(() -> new IllegalArgumentException("학생-과제 연결 정보를 찾을 수 없습니다."));

        Assignment assignment = studentAssignment.getAssignment();
        if (assignment == null) {
            throw new IllegalArgumentException("과제 정보를 찾을 수 없습니다.");
        }
        String title = assignment.getTopic();

        // AI를 통한 요약 생성 (메인 노드의 내용과 근거 목록)
        List<String> contentToSummarize = new ArrayList<>();
        contentToSummarize.add(request.getContent());
        request.getEvidences().forEach(evidence -> contentToSummarize.add(evidence.getContent()));

        List<String> summaries = aiClient.getSummaries(contentToSummarize);

        // 메인 노드 생성 및 저장
        Node node = Node.builder()
                .studentAssignment(studentAssignment)
                .content(request.getContent())
                .summary(summaries.get(0))
                .type(NodeType.CLAIM)
                .createdBy(CreatedBy.STUDENT)
                .isHidden(false)
                .build();

        Node savedNode = nodeRepository.save(node);

        // 근거 생성 및 저장
        List<Evidence> evidences = new ArrayList<>();
        for (int i = 0; i < request.getEvidences().size(); i++) {
            EvidenceCreateRequest evidenceRequest = request.getEvidences().get(i);
            String summary = summaries.get(i + 1);

            Evidence evidence = Evidence.builder()
                    .node(savedNode)
                    .content(evidenceRequest.getContent())
                    .summary(summary)
                    .source(evidenceRequest.getSource())
                    .url(evidenceRequest.getUrl())
                    .createdBy(CreatedBy.STUDENT)
                    .build();
            savedNode.addEvidence(evidence);
            evidences.add(evidenceRepository.save(evidence));
        }

        // 응답 생성
        List<EvidenceResponse> evidenceResponses = evidences.stream()
                .map(EvidenceResponse::from)
                .collect(Collectors.toList());

        NodeResponse response = NodeResponse.of(savedNode, title, evidenceResponses);

        System.out.println("=== 학생 메인 노드 생성 완료 ===");
        System.out.println("Node ID: " + savedNode.getId());

        // 트랜잭션 커밋 후 비동기 AI 호출 등록
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                System.out.println("=== 트랜잭션 커밋 후 비동기 AI 요청 시작 ===");
                asyncAiService.generateAiResponseAsync(studentAssignmentId);
                System.out.println("=== 비동기 AI 요청 등록 완료 ===");
            }
        });

        return response;
    }

    public NodeDetailResponse getNodeDetail(Long nodeId) {
        // 노드 조회
        Node node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 노드를 찾을 수 없습니다."));

        StudentAssignment studentAssignment = node.getStudentAssignment();
        if (studentAssignment == null) {
            throw new IllegalStateException("노드에 연결된 학생-과제 정보가 없습니다.");
        }
        Assignment assignment = studentAssignment.getAssignment(); // 이 부분이 실제 코드와 다를 수 있음
        if (assignment == null) {
            throw new IllegalArgumentException("과제 정보를 찾을 수 없습니다.");
        }
        String title = assignment.getTopic();

        // 노드에 연결된 근거들 찾기
        List<Evidence> evidences = node.getEvidences();

        // 근거 목록 변환
        List<EvidenceResponse> evidenceResponses = evidences.stream()
                .map(EvidenceResponse::from)
                .collect(Collectors.toList());

        // 노드 상세 정보 응답 생성
        return NodeDetailResponse.of(node, title, evidenceResponses);
    }

    public NodeTreeResponse getNodeTree(Long studentAssignmentId) {
        StudentAssignment studentAssignment = studentAssignmentRepository.findById(studentAssignmentId)
                .orElseThrow(() -> new IllegalArgumentException("학생-과제 연결 정보를 찾을 수 없습니다."));

        Assignment assignment = studentAssignment.getAssignment();

        // 메인 노드 찾기 (hidden이 false이고 parent가 null인 CLAIM 노드)
        List<Node> allNodes = nodeRepository.findByStudentAssignmentAndIsHiddenFalse(studentAssignment);

        NodeTreeResponse subjectNode = NodeTreeResponse.builder()
                .id(0L) // 주제 노드는 항상 ID 0
                .content(assignment.getTopic()) // 과제의 주제
                .summary(null)
                .type(NodeType.SUBJECT)
                .createdBy(CreatedBy.TEACHER)
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .evidences(new ArrayList<>()) // 주제 노드는 evidence 없음
                .children(buildSubjectChildren(allNodes)) // 학생의 CLAIM 노드들이 children
                .triggeredByEvidenceId(null)
                .build();

        return subjectNode;
    }

    private List<NodeTreeResponse> buildSubjectChildren(List<Node> allNodes) {
        // 메인 노드들 찾기 (parent가 null이고 type이 CLAIM인 노드들)
        return allNodes.stream()
                .filter(node -> node.getParent() == null && node.getType() == NodeType.CLAIM)
                .map(claimNode -> buildNodeTree(claimNode, allNodes))
                .collect(Collectors.toList());
    }

    private NodeTreeResponse buildNodeTree(Node node, List<Node> allNodes) {
        // 현재 노드의 근거들 조회
        List<EvidenceResponse> evidences = node.getEvidences().stream()
                .map(EvidenceResponse::from)
                .collect(Collectors.toList());

        // 자식 노드들 찾기 (현재 노드를 parent로 가지는 노드들)
        List<NodeTreeResponse> children = allNodes.stream()
                .filter(n -> n.getParent() != null && n.getParent().getId().equals(node.getId()))
                .map(childNode -> buildNodeTree(childNode, allNodes))
                .collect(Collectors.toList());

        return NodeTreeResponse.builder()
                .id(node.getId())
                .content(node.getContent())
                .summary(node.getSummary())
                .type(node.getType())
                .createdBy(node.getCreatedBy())
                .createdAt(node.getCreatedAt())
                .updatedAt(node.getUpdatedAt())
                .evidences(evidences)
                .children(children)
                .triggeredByEvidenceId(node.getTriggeredByEvidence() != null ?
                        node.getTriggeredByEvidence().getId() : null)
                .build();
    }

//    @Transactional
//    public NodeResponse createStudentResponse(Long studentAssignmentId, StudentResponseRequest request) {
//        StudentAssignment studentAssignment = studentAssignmentRepository.findById(studentAssignmentId)
//                .orElseThrow(() -> new IllegalArgumentException("학생-과제 연결 정보를 찾을 수 없습니다."));
//
//        Assignment assignment = studentAssignment.getAssignment();
//        String title = assignment != null ? assignment.getTopic() : "";
//
//        if (request.getTargetType() == TargetType.NODE) {
//            // 질문 노드에 대한 답변
//            return createAnswerNode(studentAssignment, request.getTargetId(), request.getContent(), title);
//        } else {
//            // 근거에 대한 재반박
//            return createCounterClaimNode(studentAssignment, request.getTargetId(), request.getContent(),
//                    request.getEvidences(), title);
//        }
//    }
    @Transactional
    public NodeResponse createStudentResponse(Long studentAssignmentId, StudentResponseRequest request) {
        System.out.println("=== 학생 응답 노드 생성 시작 ===");
        System.out.println("Thread: " + Thread.currentThread().getName());

        StudentAssignment studentAssignment = studentAssignmentRepository.findById(studentAssignmentId)
                .orElseThrow(() -> new IllegalArgumentException("학생-과제 연결 정보를 찾을 수 없습니다."));

        Assignment assignment = studentAssignment.getAssignment();
        String title = assignment != null ? assignment.getTopic() : "";

        NodeResponse studentResponse;
        if (request.getTargetType() == TargetType.NODE) {
            // 질문 노드에 대한 답변
            studentResponse = createAnswerNode(studentAssignment, request.getTargetId(), request.getContent(), title);
        } else {
            // 근거에 대한 재반박
            studentResponse = createCounterClaimNode(studentAssignment, request.getTargetId(), request.getContent(),
                    request.getEvidences(), title);
        }

        System.out.println("=== 학생 응답 노드 생성 완료 ===");
        System.out.println("Node ID: " + studentResponse.getNodeId());

        // 트랜잭션 커밋 후 비동기 AI 호출 등록
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                System.out.println("=== 트랜잭션 커밋 후 비동기 AI 요청 시작 ===");
                asyncAiService.generateAiResponseAsync(studentAssignmentId);
                System.out.println("=== 비동기 AI 요청 등록 완료 ===");
            }
        });

        return studentResponse;
    }

    private NodeResponse createAnswerNode(StudentAssignment studentAssignment, Long questionNodeId,
                                          String content, String title) {
        // 질문 노드 조회
        Node questionNode = nodeRepository.findById(questionNodeId)
                .orElseThrow(() -> new IllegalArgumentException("질문 노드를 찾을 수 없습니다."));

        if (questionNode.getType() != NodeType.QUESTION) {
            throw new IllegalArgumentException("질문 노드가 아닙니다.");
        }

        // AI 요약 생성
        List<String> summaries = aiClient.getSummaries(List.of(content));

        // 답변 노드 생성
        Node answerNode = Node.builder()
                .studentAssignment(studentAssignment)
                .content(content)
                .summary(summaries.get(0))
                .type(NodeType.ANSWER)
                .createdBy(CreatedBy.STUDENT)
                .parent(questionNode) // 질문 노드를 부모로 설정
                .isHidden(false)
                .build();

        Node savedAnswerNode = nodeRepository.save(answerNode);
        return NodeResponse.of(savedAnswerNode, title, new ArrayList<>());
    }

    private NodeResponse createCounterClaimNode(StudentAssignment studentAssignment, Long evidenceId,
                                                String content, List<EvidenceCreateRequest> evidenceRequests,
                                                String title) {
        // 대상 근거 조회
        Evidence targetEvidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("대상 근거를 찾을 수 없습니다."));

        // AI 요약 생성
        List<String> contentToSummarize = new ArrayList<>();
        contentToSummarize.add(content);
        if (evidenceRequests != null) {
            evidenceRequests.forEach(req -> contentToSummarize.add(req.getContent()));
        }

        List<String> summaries = aiClient.getSummaries(contentToSummarize);

        // 재반박 노드 생성
        Node counterClaimNode = Node.builder()
                .studentAssignment(studentAssignment)
                .content(content)
                .summary(summaries.get(0))
                .type(NodeType.CLAIM)
                .createdBy(CreatedBy.STUDENT)
                .parent(targetEvidence.getNode()) // 근거가 속한 노드를 부모로 설정
                .triggeredByEvidence(targetEvidence) // 트리거한 근거 설정
                .isHidden(false)
                .build();

        Node savedCounterClaimNode = nodeRepository.save(counterClaimNode);

        // 근거들 생성
        List<Evidence> evidences = new ArrayList<>();
        if (evidenceRequests != null) {
            for (int i = 0; i < evidenceRequests.size(); i++) {
                EvidenceCreateRequest evidenceRequest = evidenceRequests.get(i);
                String summary = summaries.get(i + 1);

                Evidence evidence = Evidence.builder()
                        .node(savedCounterClaimNode)
                        .content(evidenceRequest.getContent())
                        .summary(summary)
                        .source(evidenceRequest.getSource())
                        .url(evidenceRequest.getUrl())
                        .createdBy(CreatedBy.STUDENT)
                        .build();
                savedCounterClaimNode.addEvidence(evidence);
                evidences.add(evidenceRepository.save(evidence));
            }
        }

        List<EvidenceResponse> evidenceResponses = evidences.stream()
                .map(EvidenceResponse::from)
                .collect(Collectors.toList());

        return NodeResponse.of(savedCounterClaimNode, title, evidenceResponses);
    }

    private String calculateDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "0분";

        Duration duration = Duration.between(start, end);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        if (days > 0) {
            return String.format("%d일 %d시간 %d분", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d시간 %d분", hours, minutes);
        } else {
            return String.format("%d분", minutes);
        }
    }

    // 과제별 전체 학생 활동 요약 조회
    public Map<String, Object> getTreeLogsSummary(Long assignmentId, String sort, int page, int size) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다."));

        // 과제에 할당된 모든 학생들 조회
        List<StudentAssignment> studentAssignments = studentAssignmentRepository
                .findByAssignmentId(assignmentId);

        // 각 학생의 활동 요약 생성
        List<StudentActivitySummary> studentSummaries = studentAssignments.stream()
                .map(this::createStudentActivitySummary)
                .collect(Collectors.toList());

        // 정렬
        switch (sort) {
            case "student_name":
                studentSummaries.sort(Comparator.comparing(StudentActivitySummary::getStudentName));
                break;
            case "activity_count":
                studentSummaries.sort(Comparator.comparing(StudentActivitySummary::getNodeCount).reversed());
                break;
            default: // "recent"
                studentSummaries.sort(Comparator.comparing(StudentActivitySummary::getLastActivityAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        }

        // 페이징
        int start = page * size;
        int end = Math.min(start + size, studentSummaries.size());
        List<StudentActivitySummary> pagedSummaries = start < studentSummaries.size() ?
                studentSummaries.subList(start, end) : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("assignment", Map.of(
                "id", assignment.getId(),
                "title", assignment.getTopic(),
                "totalStudents", studentAssignments.size(),
                "submittedStudents", (int) studentAssignments.stream()
                        .filter(sa -> sa.getStatus() != StudentStatus.NOT_STARTED).count(),
                "completionRate", calculateCompletionRate(studentAssignments)
        ));
        result.put("students", pagedSummaries);
        result.put("pagination", Map.of(
                "currentPage", page,
                "totalPages", (int) Math.ceil((double) studentSummaries.size() / size),
                "totalElements", studentSummaries.size(),
                "size", size
        ));

        return result;
    }

    private StudentActivitySummary createStudentActivitySummary(StudentAssignment studentAssignment) {
        List<Node> nodes = nodeRepository.findByStudentAssignmentOrderByCreatedAtAsc(studentAssignment);

        long nodeCount = nodes.size();
        int evidenceCount = nodes.stream()
                .mapToInt(node -> node.getEvidences().size())
                .sum();
        long aiInteractions = nodes.stream()
                .filter(node -> node.getCreatedBy() == CreatedBy.AI)
                .count();

        return StudentActivitySummary.builder()
                .studentId(studentAssignment.getStudent().getId())
                .studentName(studentAssignment.getStudent().getName())
                .studentNumber(studentAssignment.getStudent().getNumber())
                .nodeCount((int) nodeCount)
                .evidenceCount(evidenceCount)
                .aiInteractions((int) aiInteractions)
                .startedAt(nodes.isEmpty() ? null : nodes.get(0).getCreatedAt())
                .lastActivityAt(nodes.isEmpty() ? null : nodes.get(nodes.size() - 1).getCreatedAt())
                .status(studentAssignment.getStatus())
                .build();
    }

    private double calculateCompletionRate(List<StudentAssignment> studentAssignments) {
        if (studentAssignments.isEmpty()) return 0.0;

        long completed = studentAssignments.stream()
                .filter(sa -> sa.getStatus() == StudentStatus.COMPLETED)
                .count();
        return (double) completed / studentAssignments.size() * 100.0;
    }

    public StudentTreeLogResponse getStudentTreeLogs(Long assignmentId, Long studentId) {
        StudentAssignment studentAssignment = studentAssignmentRepository
                .findByStudentIdAndAssignmentId(studentId, assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생의 과제를 찾을 수 없습니다."));

        // 숨김 포함 모든 노드를 생성 시간순으로 조회 (선생님은 모든 이력을 볼 수 있음)
        List<Node> allNodes = nodeRepository.findByStudentAssignmentOrderByCreatedAtAsc(studentAssignment);

        // 노드를 ActivityLog로 변환
        List<ActivityLogResponse> activities = allNodes.stream()
                .map(node -> convertNodeToActivityLogWithStatus(node))
                .collect(Collectors.toList());

        // 통계 계산 (현재 보이는 노드들만으로 계산)
        List<Node> visibleNodes = allNodes.stream()
                .filter(node -> !node.isHidden())
                .collect(Collectors.toList());
        ActivityStatistics statistics = calculateActivityStatistics(visibleNodes);

        return StudentTreeLogResponse.builder()
                .assignment(AssignmentInfo.from(studentAssignment.getAssignment()))
                .student(StudentInfo.from(studentAssignment.getStudent()))
                .activities(activities)
                .statistics(statistics)
                .build();
    }

    private ActivityLogResponse convertNodeToActivityLogWithStatus(Node node) {
        // 근거들도 함께 포함
        List<EvidenceLogInfo> evidences = node.getEvidences().stream()
                .map(EvidenceLogInfo::from)
                .collect(Collectors.toList());

        return ActivityLogResponse.builder()
                .timestamp(node.getCreatedAt())
                .actionBy(node.getCreatedBy())
                .node(NodeLogInfo.fromWithStatus(node)) // 숨김 상태 포함
                .evidences(evidences)
                .isHidden(node.isHidden()) // 수정으로 인해 숨김 처리되었는지 표시
                .build();
    }

    private ActivityLogResponse convertNodeToActivityLog(Node node) {
        // 근거들도 함께 포함
        List<EvidenceLogInfo> evidences = node.getEvidences().stream()
                .map(EvidenceLogInfo::from)
                .collect(Collectors.toList());

        return ActivityLogResponse.builder()
                .timestamp(node.getCreatedAt())
                .actionBy(node.getCreatedBy())
                .node(NodeLogInfo.from(node))
                .evidences(evidences)
                .build();
    }

    private ActivityStatistics calculateActivityStatistics(List<Node> nodes) {
        long studentNodes = nodes.stream()
                .filter(node -> node.getCreatedBy() == CreatedBy.STUDENT)
                .count();

        long aiNodes = nodes.stream()
                .filter(node -> node.getCreatedBy() == CreatedBy.AI)
                .count();

        int totalEvidences = nodes.stream()
                .mapToInt(node -> node.getEvidences().size())
                .sum();

        int studentEvidences = nodes.stream()
                .filter(node -> node.getCreatedBy() == CreatedBy.STUDENT)
                .mapToInt(node -> node.getEvidences().size())
                .sum();

        LocalDateTime startedAt = nodes.isEmpty() ? null : nodes.get(0).getCreatedAt();
        LocalDateTime lastActivityAt = nodes.isEmpty() ? null :
                nodes.get(nodes.size() - 1).getCreatedAt();

        return ActivityStatistics.builder()
                .totalNodes((int) (studentNodes + aiNodes))
                .studentNodes((int) studentNodes)
                .aiNodes((int) aiNodes)
                .aiInteractions((int) aiNodes) // AI 노드 수 = AI 상호작용 수
                .totalEvidences(totalEvidences)
                .studentEvidences(studentEvidences)
                .aiEvidences(totalEvidences - studentEvidences)
                .startedAt(startedAt)
                .lastActivityAt(lastActivityAt)
                .totalDuration(calculateDuration(startedAt, lastActivityAt))
                .build();
    }

    @Transactional
    public NodeResponse updateNode(Long studentAssignmentId, Long nodeId, NodeUpdateRequest request) {
        // 1. 기존 노드 조회 및 권한 확인
        Node existingNode = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 노드를 찾을 수 없습니다."));

        // 학생이 만든 노드인지 확인
        if (existingNode.getCreatedBy() != CreatedBy.STUDENT) {
            throw new IllegalArgumentException("학생이 생성한 노드만 수정할 수 있습니다.");
        }

        // 해당 학생의 노드인지 확인
        if (!existingNode.getStudentAssignment().getId().equals(studentAssignmentId)) {
            throw new IllegalArgumentException("다른 학생의 노드는 수정할 수 없습니다.");
        }

        // 2. 기존 노드와 그 하위 모든 노드들을 숨김 처리
        hideNodeAndDescendants(existingNode);

        // 3. 새로운 노드 생성
        StudentAssignment studentAssignment = existingNode.getStudentAssignment();
        Assignment assignment = studentAssignment.getAssignment();
        String title = assignment.getTopic();

        // AI를 통한 요약 생성
        List<String> contentToSummarize = new ArrayList<>();
        contentToSummarize.add(request.getContent());
        request.getEvidences().forEach(evidence -> contentToSummarize.add(evidence.getContent()));

        List<String> summaries = aiClient.getSummaries(contentToSummarize);

        // 새로운 노드 생성
        Node newNode = Node.builder()
                .studentAssignment(studentAssignment)
                .content(request.getContent())
                .summary(summaries.get(0))
                .type(existingNode.getType()) // 기존 타입 유지
                .createdBy(CreatedBy.STUDENT)
                .parent(existingNode.getParent()) // 기존 부모 유지
                .triggeredByEvidence(existingNode.getTriggeredByEvidence()) // 기존 트리거 근거 유지
                .isHidden(false)
                .build();

        Node savedNode = nodeRepository.save(newNode);

        // 근거들 생성
        List<Evidence> evidences = new ArrayList<>();
        for (int i = 0; i < request.getEvidences().size(); i++) {
            EvidenceUpdateRequest evidenceRequest = request.getEvidences().get(i);
            String summary = summaries.get(i + 1);

            Evidence evidence = Evidence.builder()
                    .node(savedNode)
                    .content(evidenceRequest.getContent())
                    .summary(summary)
                    .source(evidenceRequest.getSource())
                    .url(evidenceRequest.getUrl())
                    .createdBy(CreatedBy.STUDENT)
                    .build();
            savedNode.addEvidence(evidence);
            evidences.add(evidenceRepository.save(evidence));
        }

        // 4. 새로운 트리 구조를 AI에게 전송하여 새로운 반박/질문 생성
        requestAiResponseForUpdatedTree(studentAssignment);

        // 응답 생성
        List<EvidenceResponse> evidenceResponses = evidences.stream()
                .map(EvidenceResponse::from)
                .collect(Collectors.toList());

        return NodeResponse.of(savedNode, title, evidenceResponses);
    }

    private void hideNodeAndDescendants(Node node) {
        // 현재 노드 숨김 처리
        node.setHidden(true);
        nodeRepository.save(node);

        // 현재 노드를 부모로 가지는 모든 자식 노드들 재귀적으로 숨김 처리
        List<Node> children = nodeRepository.findByParentAndIsHiddenFalse(node);
        for (Node child : children) {
            hideNodeAndDescendants(child);
        }
    }

    private void requestAiResponseForUpdatedTree(StudentAssignment studentAssignment) {
        // 현재 보이는 노드들만 조회하여 AI에게 새로운 반박/질문 요청
        try {
            // 기본적으로 1개의 리뷰 요청
            aiReviewService.generateAiResponse(studentAssignment.getId(), 1);
        } catch (Exception e) {
            // AI 응답 생성 실패 시 로그만 기록하고 계속 진행
            System.err.println("노드 수정 후 AI 응답 생성 실패: " + e.getMessage());
        }
    }

}