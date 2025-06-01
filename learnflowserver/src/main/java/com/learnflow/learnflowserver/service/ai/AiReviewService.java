package com.learnflow.learnflowserver.service.ai;

import com.learnflow.learnflowserver.domain.Assignment;
import com.learnflow.learnflowserver.domain.Evidence;
import com.learnflow.learnflowserver.domain.Node;
import com.learnflow.learnflowserver.domain.StudentAssignment;
import com.learnflow.learnflowserver.domain.common.enums.CreatedBy;
import com.learnflow.learnflowserver.domain.common.enums.NodeType;
import com.learnflow.learnflowserver.dto.request.NodeCreateRequest;
import com.learnflow.learnflowserver.dto.request.TreeNodeRequest;
import com.learnflow.learnflowserver.dto.request.ai.AiReviewRequest;
import com.learnflow.learnflowserver.dto.response.EvidenceResponse;
import com.learnflow.learnflowserver.dto.response.NodeResponse;
import com.learnflow.learnflowserver.dto.response.TreeNodeResponse;
import com.learnflow.learnflowserver.dto.response.ai.AiReviewResponse;
import com.learnflow.learnflowserver.repository.EvidenceRepository;
import com.learnflow.learnflowserver.repository.NodeRepository;
import com.learnflow.learnflowserver.repository.StudentAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiReviewService {

    private final NodeRepository nodeRepository;
    private final EvidenceRepository evidenceRepository;
    private final StudentAssignmentRepository studentAssignmentRepository;
    private final AiClient aiClient;

    @Transactional
    public List<NodeResponse> generateAiResponse(Long studentAssignmentId, Integer reviewNum) {
        try {
            // 학생-과제 연결 정보 조회
            StudentAssignment studentAssignment = studentAssignmentRepository.findById(studentAssignmentId)
                    .orElseThrow(() -> new IllegalArgumentException("학생-과제 연결 정보를 찾을 수 없습니다."));

            // 현재 트리 구조 조회 (hidden이 false인 노드들만)
            List<Node> visibleNodes = nodeRepository.findByStudentAssignmentAndIsHiddenFalse(studentAssignment);

            // 트리 구조를 AI API 형식으로 변환
            TreeNodeRequest treeRequest = buildTreeRequest(visibleNodes);
            if (treeRequest == null) {
                throw new IllegalArgumentException("변환할 트리 구조가 없습니다.");
            }

            // AI API 호출 - student_id, assignment_id 추가
            AiReviewRequest aiRequest = AiReviewRequest.builder()
                    .tree(treeRequest)
                    .reviewNum(reviewNum)
                    .studentId(studentAssignment.getStudent().getId().toString())
                    .assignmentId(studentAssignment.getAssignment().getId().toString())
                    .build();

            List<AiReviewResponse> aiResponses = aiClient.getReviews(aiRequest);

            // AI 응답을 바탕으로 새로운 노드들 생성
            List<NodeResponse> responses = new ArrayList<>();
            for (AiReviewResponse aiResponse : aiResponses) {
                try {
                    NodeResponse nodeResponse = createAiNode(studentAssignment, aiResponse);
                    responses.add(nodeResponse);
                } catch (Exception e) {
                    // 개별 노드 생성 실패 시 로그 기록하고 계속 진행
                    System.err.println("AI 노드 생성 실패: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            return responses;
        } catch (Exception e) {
            System.err.println("AI 응답 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("AI 응답 생성에 실패했습니다.", e);
        }
    }

    private TreeNodeRequest buildTreeRequest(List<Node> nodes) {
        if (nodes.isEmpty()) {
            return null;
        }

        // 메인 노드 찾기 (parent가 null이고 type이 CLAIM인 노드)
        Node mainNode = nodes.stream()
                .filter(node -> node.getParent() == null && node.getType() == NodeType.CLAIM)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("메인 노드를 찾을 수 없습니다."));

        return convertToTreeRequest(mainNode, nodes, new HashSet<>());
    }

    // 순환 참조 방지를 위해 visited 집합 추가
    private TreeNodeRequest convertToTreeRequest(Node node, List<Node> allNodes, Set<Long> visited) {
        // 순환 참조 방지
        if (visited.contains(node.getId())) {
            return null;
        }
        visited.add(node.getId());

        List<TreeNodeRequest> children = new ArrayList<>();
        List<TreeNodeRequest> siblings = new ArrayList<>();

        try {
            // 1. 현재 노드의 Evidence들을 child로 추가
            for (Evidence evidence : node.getEvidences()) {
                // Evidence에서 파생된 모든 노드들을 찾아서 sibling chain 구성
                List<TreeNodeRequest> evidenceSiblings = buildEvidenceSiblingChain(evidence, allNodes, visited);

                TreeNodeRequest evidenceNode = TreeNodeRequest.builder()
                        .id("evidence_" + evidence.getId())
                        .type("근거")
                        .content(evidence.getContent() != null ? evidence.getContent() : "")
                        .summary(evidence.getSummary() != null ? evidence.getSummary() : "")
                        .createdBy(evidence.getCreatedBy() != null ? evidence.getCreatedBy().name() : "")
                        .createdAt(evidence.getCreatedAt() != null ? evidence.getCreatedAt().toString() : "")
                        .updatedAt(evidence.getUpdatedAt() != null ? evidence.getUpdatedAt().toString() : "")
                        .child(new ArrayList<>()) // Evidence는 child가 없음
                        .sibling(evidenceSiblings) // Evidence에서 파생된 노드 체인
                        .build();
                children.add(evidenceNode);
            }

            // 2. 현재 노드와 같은 레벨의 sibling 노드들 찾기
            if (node.getParent() != null) {
                siblings = allNodes.stream()
                        .filter(n -> n.getParent() != null &&
                                n.getParent().getId().equals(node.getParent().getId()) &&
                                !n.getId().equals(node.getId()))
                        .map(siblingNode -> convertToTreeRequest(siblingNode, allNodes, new HashSet<>(visited)))
                        .filter(Objects::nonNull) // null 제거
                        .collect(Collectors.toList());
            }

            return TreeNodeRequest.builder()
                    .id("node_" + node.getId())
                    .type(convertNodeTypeToString(node.getType()))
                    .content(node.getContent() != null ? node.getContent() : "")
                    .summary(node.getSummary() != null ? node.getSummary() : "")
                    .createdBy(node.getCreatedBy() != null ? node.getCreatedBy().name() : "")
                    .createdAt(node.getCreatedAt() != null ? node.getCreatedAt().toString() : "")
                    .updatedAt(node.getUpdatedAt() != null ? node.getUpdatedAt().toString() : "")
                    .child(children)
                    .sibling(siblings)
                    .build();
        } finally {
            visited.remove(node.getId()); // 백트래킹
        }
    }

    private List<TreeNodeRequest> buildEvidenceSiblingChain(Evidence evidence, List<Node> allNodes, Set<Long> visited) {
        List<TreeNodeRequest> siblingChain = new ArrayList<>();

        try {
            // Evidence를 triggeredByEvidence로 가지는 첫 번째 노드 찾기 (반박 또는 질문)
            Node firstDerivedNode = allNodes.stream()
                    .filter(n -> n.getTriggeredByEvidence() != null &&
                            n.getTriggeredByEvidence().getId().equals(evidence.getId()) &&
                            !visited.contains(n.getId())) // 이미 방문한 노드 제외
                    .findFirst()
                    .orElse(null);

            if (firstDerivedNode != null) {
                // 첫 번째 노드를 변환
                TreeNodeRequest firstNode = convertToTreeRequest(firstDerivedNode, allNodes, new HashSet<>(visited));

                if (firstNode != null) {
                    // 만약 질문 노드라면, 해당 질문에 대한 답변 노드를 sibling으로 연결
                    if (firstDerivedNode.getType() == NodeType.QUESTION) {
                        List<Node> answerNodes = allNodes.stream()
                                .filter(n -> n.getParent() != null &&
                                        n.getParent().getId().equals(firstDerivedNode.getId()) &&
                                        n.getType() == NodeType.ANSWER &&
                                        !visited.contains(n.getId()))
                                .collect(Collectors.toList());

                        List<TreeNodeRequest> answerSiblings = new ArrayList<>();
                        for (Node answerNode : answerNodes) {
                            TreeNodeRequest answerRequest = convertToTreeRequest(answerNode, allNodes, new HashSet<>(visited));
                            if (answerRequest != null) {
                                answerSiblings.add(answerRequest);
                            }
                        }

                        // 질문 노드의 sibling에 답변 노드들 추가
                        firstNode.setSibling(answerSiblings);
                    }

                    siblingChain.add(firstNode);
                }
            }
        } catch (Exception e) {
            System.err.println("Evidence sibling chain 구성 중 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return siblingChain;
    }

    private String convertNodeTypeToString(NodeType nodeType) {
        if (nodeType == null) return "주장";

        switch (nodeType) {
            case CLAIM: return "주장";
            case COUNTER: return "반론";
            case QUESTION: return "질문";
            case ANSWER: return "답변";
            default: return "주장";
        }
    }

    @Transactional
    public NodeResponse createAiNode(StudentAssignment studentAssignment, AiReviewResponse aiResponse) {
        try {
            // AI 응답의 parent에서 Evidence ID 추출 (evidence_ 접두사 제거)
            String parentId = aiResponse.getParent();
            if (parentId == null || !parentId.startsWith("evidence_")) {
                throw new IllegalArgumentException("유효하지 않은 Evidence ID: " + parentId);
            }

            Long evidenceId = Long.parseLong(parentId.substring("evidence_".length()));
            Evidence targetEvidence = evidenceRepository.findById(evidenceId)
                    .orElseThrow(() -> new IllegalArgumentException("대상 근거를 찾을 수 없습니다: " + evidenceId));

            TreeNodeResponse aiTree = aiResponse.getTree();
            if (aiTree == null) {
                throw new IllegalArgumentException("AI 응답 트리가 null입니다.");
            }

            NodeType nodeType = convertStringToNodeType(aiTree.getType());

            // AI 요약 요청 준비
            List<String> contentToSummarize = new ArrayList<>();
            if (aiTree.getContent() != null && !aiTree.getContent().trim().isEmpty()) {
                contentToSummarize.add(aiTree.getContent());
            }

            // 반론의 경우에만 AI가 제공한 근거들도 요약에 포함
            boolean hasChildren = aiTree.getChild() != null && !aiTree.getChild().isEmpty();
            if (nodeType == NodeType.COUNTER && hasChildren) {
                for (TreeNodeResponse child : aiTree.getChild()) {
                    if (child.getContent() != null && !child.getContent().trim().isEmpty()) {
                        contentToSummarize.add(child.getContent());
                    }
                }
            }

//            // 반론의 경우 AI가 제공한 근거들도 요약에 포함
//            if (aiTree.getChild() != null && !aiTree.getChild().isEmpty()) {
//                for (TreeNodeResponse child : aiTree.getChild()) {
//                    if (child.getContent() != null) {
//                        contentToSummarize.add(child.getContent());
//                    }
//                }
//            }

            // AI 요약 생성
            List<String> summaries = new ArrayList<>();
            if (!contentToSummarize.isEmpty()) {
                try {
                    summaries = aiClient.getSummaries(contentToSummarize);
                } catch (Exception e) {
                    System.err.println("AI 요약 생성 실패: " + e.getMessage());
                    // 요약 실패 시 원본 내용 사용
                    summaries = new ArrayList<>(contentToSummarize);
                }
            }

            // AI 노드 생성
            Node aiNode = Node.builder()
                    .studentAssignment(studentAssignment)
                    .content(aiTree.getContent() != null ? aiTree.getContent() : "")
                    .summary(!summaries.isEmpty() ? summaries.get(0) :
                            (aiTree.getSummary() != null ? aiTree.getSummary() : ""))
                    .type(nodeType)
                    .createdBy(CreatedBy.AI)
                    .parent(targetEvidence.getNode()) // Evidence가 속한 Node를 부모로 설정
                    .triggeredByEvidence(targetEvidence) // 트리거한 Evidence 설정
                    .isHidden(false)
                    .build();

            System.out.println("AI 노드 생성 완료: " + aiNode.getType() + ", 내용: " + aiNode.getContent());
            Node savedAiNode = nodeRepository.save(aiNode);
            System.out.println("AI 노드 저장 완료, ID: " + savedAiNode.getId());

            // 반론의 경우에만 AI가 제공한 근거들 생성
            List<Evidence> evidences = new ArrayList<>();
            if (nodeType == NodeType.COUNTER && hasChildren) {
                System.out.println("반론 노드의 근거 생성 시작, 근거 개수: " + aiTree.getChild().size());

                for (int i = 0; i < aiTree.getChild().size(); i++) {
                    TreeNodeResponse childNode = aiTree.getChild().get(i);
                    String summary = (i + 1 < summaries.size()) ?
                            summaries.get(i + 1) :
                            (childNode.getSummary() != null ? childNode.getSummary() : "");

                    Evidence evidence = Evidence.builder()
                            .node(savedAiNode)
                            .content(childNode.getContent() != null ? childNode.getContent() : "")
                            .summary(summary)
                            .createdBy(CreatedBy.AI)
                            .build();

                    System.out.println("근거 생성: " + evidence.getContent());

                    // 안전하게 Evidence 추가
                    savedAiNode.addEvidence(evidence);
                    Evidence savedEvidence = evidenceRepository.save(evidence);
                    evidences.add(savedEvidence);

                    System.out.println("근거 저장 완료, ID: " + savedEvidence.getId());
                }

//                for (int i = 0; i < aiTree.getChild().size(); i++) {
//                    TreeNodeResponse childNode = aiTree.getChild().get(i);
//                    String summary = (i + 1 < summaries.size()) ?
//                            summaries.get(i + 1) :
//                            (childNode.getSummary() != null ? childNode.getSummary() : "");
//
//                    Evidence evidence = Evidence.builder()
//                            .node(savedAiNode)
//                            .content(childNode.getContent() != null ? childNode.getContent() : "")
//                            .summary(summary)
//                            .createdBy(CreatedBy.AI)
//                            .build();
//
//                    savedAiNode.addEvidence(evidence);
//                    evidences.add(evidenceRepository.save(evidence));
//                }
            } else if (nodeType == NodeType.QUESTION) {
                System.out.println("질문 노드이므로 근거 생성 없음");
            }

            // 응답 생성
            List<EvidenceResponse> evidenceResponses = evidences.stream()
                    .map(EvidenceResponse::from)
                    .collect(Collectors.toList());

            Assignment assignment = studentAssignment.getAssignment();
            String title = assignment != null ? assignment.getDescription() : "";

            System.out.println("NodeResponse 생성 완료, 근거 개수: " + evidenceResponses.size());
            return NodeResponse.of(savedAiNode, title, evidenceResponses);
        } catch (Exception e) {
            System.err.println("AI 노드 생성 중 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("AI 노드 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private NodeType convertStringToNodeType(String type) {
        if (type == null) return NodeType.COUNTER;

        switch (type) {
            case "반론": return NodeType.COUNTER;
            case "질문": return NodeType.QUESTION;
            case "답변": return NodeType.ANSWER;
            case "주장": return NodeType.CLAIM;
            default: return NodeType.COUNTER;
        }
    }
}
//public class AiReviewService {
//
//    private final NodeRepository nodeRepository;
//    private final EvidenceRepository evidenceRepository;
//    private final StudentAssignmentRepository studentAssignmentRepository;
//    private final AiClient aiClient;
//
//    @Transactional
//    public List<NodeResponse> generateAiResponse(Long studentAssignmentId, Integer reviewNum) {
//        // 학생-과제 연결 정보 조회
//        StudentAssignment studentAssignment = studentAssignmentRepository.findById(studentAssignmentId)
//                .orElseThrow(() -> new IllegalArgumentException("학생-과제 연결 정보를 찾을 수 없습니다."));
//
//        // 현재 트리 구조 조회 (hidden이 false인 노드들만)
//        List<Node> visibleNodes = nodeRepository.findByStudentAssignmentAndIsHiddenFalse(studentAssignment);
//
//        // 트리 구조를 AI API 형식으로 변환
//        TreeNodeRequest treeRequest = buildTreeRequest(visibleNodes);
//        if (treeRequest == null) {
//            throw new IllegalArgumentException("변환할 트리 구조가 없습니다.");
//        }
//
//        // AI API 호출
//        AiReviewRequest aiRequest = AiReviewRequest.builder()
//                .tree(treeRequest)
//                .reviewNum(reviewNum)
//                .studentId(studentAssignment.getStudent().getId().toString())
//                .assignmentId(studentAssignment.getAssignment().getId().toString())
//                .build();
//
//        List<AiReviewResponse> aiResponses = aiClient.getReviews(aiRequest);
//
//        // AI 응답을 바탕으로 새로운 노드들 생성
//        List<NodeResponse> responses = new ArrayList<>();
//        for (AiReviewResponse aiResponse : aiResponses) {
//            NodeResponse nodeResponse = createAiNode(studentAssignment, aiResponse);
//            responses.add(nodeResponse);
//        }
//
//        return responses;
//    }
//
//    private TreeNodeRequest buildTreeRequest(List<Node> nodes) {
//        if (nodes.isEmpty()) {
//            return null;
//        }
//
//        // 메인 노드 찾기 (parent가 null이고 type이 CLAIM인 노드)
//        Node mainNode = nodes.stream()
//                .filter(node -> node.getParent() == null && node.getType() == NodeType.CLAIM)
//                .findFirst()
//                .orElseThrow(() -> new IllegalArgumentException("메인 노드를 찾을 수 없습니다."));
//
//        return convertToTreeRequest(mainNode, nodes);
//    }
//
//    private TreeNodeRequest convertToTreeRequest(Node node, List<Node> allNodes) {
//        List<TreeNodeRequest> children = new ArrayList<>();
//        List<TreeNodeRequest> siblings = new ArrayList<>();
//
//        // 1. 현재 노드의 Evidence들을 child로 추가
//        for (Evidence evidence : node.getEvidences()) {
//            // Evidence에서 파생된 모든 노드들을 찾아서 sibling chain 구성
//            List<TreeNodeRequest> evidenceSiblings = buildEvidenceSiblingChain(evidence, allNodes);
//
//            TreeNodeRequest evidenceNode = TreeNodeRequest.builder()
//                    .id("evidence_" + evidence.getId())
//                    .type("근거")
//                    .content(evidence.getContent())
//                    .summary(evidence.getSummary())
//                    .createdBy(evidence.getCreatedBy().name())
//                    .createdAt(evidence.getCreatedAt() != null ? evidence.getCreatedAt().toString() : "")
//                    .updatedAt(evidence.getUpdatedAt() != null ? evidence.getUpdatedAt().toString() : "")
//                    .child(new ArrayList<>()) // Evidence는 child가 없음
//                    .sibling(evidenceSiblings) // Evidence에서 파생된 노드 체인
//                    .build();
//            children.add(evidenceNode);
//        }
//
//        // 2. 현재 노드와 같은 레벨의 sibling 노드들 찾기
//        if (node.getParent() != null) {
//            siblings = allNodes.stream()
//                    .filter(n -> n.getParent() != null &&
//                            n.getParent().getId().equals(node.getParent().getId()) &&
//                            !n.getId().equals(node.getId()))
//                    .map(siblingNode -> convertToTreeRequest(siblingNode, allNodes))
//                    .collect(Collectors.toList());
//        }
//
//        return TreeNodeRequest.builder()
//                .id("node_" + node.getId())
//                .type(convertNodeTypeToString(node.getType()))
//                .content(node.getContent())
//                .summary(node.getSummary())
//                .createdBy(node.getCreatedBy().name())
//                .createdAt(node.getCreatedAt() != null ? node.getCreatedAt().toString() : "")
//                .updatedAt(node.getUpdatedAt() != null ? node.getUpdatedAt().toString() : "")
//                .child(children)
//                .sibling(siblings)
//                .build();
//    }
//
//    private List<TreeNodeRequest> buildEvidenceSiblingChain(Evidence evidence, List<Node> allNodes) {
//        List<TreeNodeRequest> siblingChain = new ArrayList<>();
//
//        // Evidence를 triggeredByEvidence로 가지는 첫 번째 노드 찾기 (반박 또는 질문)
//        Node firstDerivedNode = allNodes.stream()
//                .filter(n -> n.getTriggeredByEvidence() != null &&
//                        n.getTriggeredByEvidence().getId().equals(evidence.getId()))
//                .findFirst() // 하나의 Evidence에 대해서는 하나의 AI 응답만 생성된다고 가정
//                .orElse(null);
//
//        if (firstDerivedNode != null) {
//            // 첫 번째 노드를 변환
//            TreeNodeRequest firstNode = convertToTreeRequest(firstDerivedNode, allNodes);
//
//            // 만약 질문 노드라면, 해당 질문에 대한 답변 노드를 sibling으로 연결
//            if (firstDerivedNode.getType() == NodeType.QUESTION) {
//                List<Node> answerNodes = allNodes.stream()
//                        .filter(n -> n.getParent() != null &&
//                                n.getParent().getId().equals(firstDerivedNode.getId()) &&
//                                n.getType() == NodeType.ANSWER)
//                        .collect(Collectors.toList());
//
//                List<TreeNodeRequest> answerSiblings = new ArrayList<>();
//                for (Node answerNode : answerNodes) {
//                    answerSiblings.add(convertToTreeRequest(answerNode, allNodes));
//                }
//
//                // 질문 노드의 sibling에 답변 노드들 추가
//                firstNode.setSibling(answerSiblings);
//            }
//
//            siblingChain.add(firstNode);
//        }
//
//        return siblingChain;
//    }
//
//    private String convertNodeTypeToString(NodeType nodeType) {
//        switch (nodeType) {
//            case CLAIM: return "주장";
//            case COUNTER: return "반론";
//            case QUESTION: return "질문";
//            case ANSWER: return "답변";
//            default: return "주장";
//        }
//    }
//
//    @Transactional
//    public NodeResponse createAiNode(StudentAssignment studentAssignment, AiReviewResponse aiResponse) {
//        // AI 응답의 parent에서 Evidence ID 추출 (evidence_ 접두사 제거)
//        String parentId = aiResponse.getParent();
//        if (!parentId.startsWith("evidence_")) {
//            throw new IllegalArgumentException("유효하지 않은 Evidence ID: " + parentId);
//        }
//
//        Long evidenceId = Long.parseLong(parentId.substring("evidence_".length()));
//        Evidence targetEvidence = evidenceRepository.findById(evidenceId)
//                .orElseThrow(() -> new IllegalArgumentException("대상 근거를 찾을 수 없습니다: " + evidenceId));
//
//        TreeNodeResponse aiTree = aiResponse.getTree();
//        NodeType nodeType = convertStringToNodeType(aiTree.getType());
//
//        // AI 요약 요청 준비
//        List<String> contentToSummarize = new ArrayList<>();
//        contentToSummarize.add(aiTree.getContent());
//
//        // 반론의 경우 AI가 제공한 근거들도 요약에 포함
//        if (aiTree.getChild() != null && !aiTree.getChild().isEmpty()) {
//            for (TreeNodeResponse child : aiTree.getChild()) {
//                contentToSummarize.add(child.getContent());
//            }
//        }
//
//        List<String> summaries = aiClient.getSummaries(contentToSummarize);
//
//        // AI 노드 생성
//        Node aiNode = Node.builder()
//                .studentAssignment(studentAssignment)
//                .content(aiTree.getContent())
//                .summary(!summaries.isEmpty() ? summaries.get(0) : aiTree.getSummary())
//                .type(nodeType)
//                .createdBy(CreatedBy.AI)
//                .parent(targetEvidence.getNode()) // Evidence가 속한 Node를 부모로 설정
//                .triggeredByEvidence(targetEvidence) // 트리거한 Evidence 설정
//                .isHidden(false)
//                .build();
//
//        Node savedAiNode = nodeRepository.save(aiNode);
//
//        // 반론의 경우에만 AI가 제공한 근거들 생성
//        List<Evidence> evidences = new ArrayList<>();
//        if (nodeType == NodeType.COUNTER && aiTree.getChild() != null && !aiTree.getChild().isEmpty()) {
//            for (int i = 0; i < aiTree.getChild().size(); i++) {
//                TreeNodeResponse childNode = aiTree.getChild().get(i);
//                String summary = (i + 1 < summaries.size()) ?
//                        summaries.get(i + 1) :
//                        (childNode.getSummary() != null ? childNode.getSummary() : "");
//
//                Evidence evidence = Evidence.builder()
//                        .node(savedAiNode)
//                        .content(childNode.getContent())
//                        .summary(summary)
//                        .createdBy(CreatedBy.AI)
//                        .build();
//
//                savedAiNode.addEvidence(evidence);
//                evidences.add(evidenceRepository.save(evidence));
//            }
//        }
//
//        // 응답 생성
//        List<EvidenceResponse> evidenceResponses = evidences.stream()
//                .map(EvidenceResponse::from)
//                .collect(Collectors.toList());
//
//        Assignment assignment = studentAssignment.getAssignment();
//        String title = assignment != null ? assignment.getDescription() : "";
//
//        return NodeResponse.of(savedAiNode, title, evidenceResponses);
//    }
//
//    private NodeType convertStringToNodeType(String type) {
//        switch (type) {
//            case "반론": return NodeType.COUNTER;
//            case "질문": return NodeType.QUESTION;
//            case "답변": return NodeType.ANSWER;
//            case "주장": return NodeType.CLAIM;
//            default: return NodeType.COUNTER;
//        }
//    }
//}