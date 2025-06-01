package com.learnflow.learnflowserver.service;

import com.learnflow.learnflowserver.domain.Assignment;
import com.learnflow.learnflowserver.domain.Evidence;
import com.learnflow.learnflowserver.domain.Node;
import com.learnflow.learnflowserver.domain.StudentAssignment;
import com.learnflow.learnflowserver.domain.common.enums.CreatedBy;
import com.learnflow.learnflowserver.domain.common.enums.NodeType;
import com.learnflow.learnflowserver.dto.request.EvidenceCreateRequest;
import com.learnflow.learnflowserver.dto.request.NodeCreateRequest;
import com.learnflow.learnflowserver.dto.response.EvidenceResponse;
import com.learnflow.learnflowserver.dto.response.NodeDetailResponse;
import com.learnflow.learnflowserver.dto.response.NodeResponse;
import com.learnflow.learnflowserver.dto.response.NodeTreeResponse;
import com.learnflow.learnflowserver.repository.EvidenceRepository;
import com.learnflow.learnflowserver.repository.NodeRepository;
import com.learnflow.learnflowserver.repository.StudentAssignmentRepository;
import com.learnflow.learnflowserver.service.ai.AiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NodeService {

    private final NodeRepository nodeRepository;
    private final EvidenceRepository evidenceRepository;
    private final StudentAssignmentRepository studentAssignmentRepository;
    private final AiClient aiClient;

    @Transactional
    public NodeResponse createMainNode(Long studentAssignmentId, NodeCreateRequest request) {
        // 학생-과제 연결 정보 조회
        StudentAssignment studentAssignment = studentAssignmentRepository.findById(studentAssignmentId)
                .orElseThrow(() -> new IllegalArgumentException("학생-과제 연결 정보를 찾을 수 없습니다."));

        Assignment assignment = studentAssignment.getAssignment(); // 이 부분이 실제 코드와 다를 수 있음
        if (assignment == null) {
            throw new IllegalArgumentException("과제 정보를 찾을 수 없습니다.");
        }
        String title = assignment.getDescription();

        // AI를 통한 요약 생성 (메인 노드의 내용과 근거 목록)
        List<String> contentToSummarize = new ArrayList<>();
        contentToSummarize.add(request.getContent());
        request.getEvidences().forEach(evidence -> contentToSummarize.add(evidence.getContent()));

        List<String> summaries = aiClient.getSummaries(contentToSummarize);

        // 메인 노드 생성 및 저장
        Node node = Node.builder()
                .studentAssignment(studentAssignment)
                .content(request.getContent())
                .summary(summaries.get(0))  // 첫 번째 요약은 메인 노드의 요약
                .type(NodeType.CLAIM)
                .createdBy(CreatedBy.STUDENT)
                .isHidden(false)
                .build();

        Node savedNode = nodeRepository.save(node);

        // 근거 생성 및 저장
        List<Evidence> evidences = new ArrayList<>();
        for (int i = 0; i < request.getEvidences().size(); i++) {
            EvidenceCreateRequest evidenceRequest = request.getEvidences().get(i);
            String summary = summaries.get(i + 1);  // 첫 번째 요약은 메인 노드의 요약이므로 i+1

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

        return NodeResponse.of(savedNode, title, evidenceResponses);
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
        String title = assignment.getDescription();

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

        // 메인 노드 찾기 (hidden이 false이고 parent가 null인 CLAIM 노드)
        List<Node> allNodes = nodeRepository.findByStudentAssignmentAndIsHiddenFalse(studentAssignment);

        Node mainNode = allNodes.stream()
                .filter(node -> node.getParent() == null && node.getType() == NodeType.CLAIM)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("메인 노드를 찾을 수 없습니다."));

        return buildNodeTree(mainNode, allNodes);
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

    @Transactional
    public NodeResponse createAnswerNode(Long studentAssignmentId, Long questionNodeId, NodeCreateRequest request) {
        // 학생-과제 연결 정보 조회
        StudentAssignment studentAssignment = studentAssignmentRepository.findById(studentAssignmentId)
                .orElseThrow(() -> new IllegalArgumentException("학생-과제 연결 정보를 찾을 수 없습니다."));

        // 질문 노드 조회
        Node questionNode = nodeRepository.findById(questionNodeId)
                .orElseThrow(() -> new IllegalArgumentException("질문 노드를 찾을 수 없습니다."));

        if (questionNode.getType() != NodeType.QUESTION) {
            throw new IllegalArgumentException("질문 노드가 아닙니다.");
        }

        // 답변 노드 생성 (Evidence 없이)
        Node answerNode = Node.builder()
                .studentAssignment(studentAssignment)
                .content(request.getContent())
                .summary("") // 답변 노드는 요약이 필요 없을 수 있음
                .type(NodeType.ANSWER)
                .createdBy(CreatedBy.STUDENT)
                .parent(questionNode) // 질문 노드를 부모로 설정
                .isHidden(false)
                .build();

        Node savedAnswerNode = nodeRepository.save(answerNode);

        // 답변 노드는 Evidence를 가지지 않음
        Assignment assignment = studentAssignment.getAssignment();
        String title = assignment != null ? assignment.getDescription() : "";

        return NodeResponse.of(savedAnswerNode, title, new ArrayList<>());
    }

}