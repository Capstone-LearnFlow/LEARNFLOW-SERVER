package com.learnflow.learnflowserver.service;

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
import com.learnflow.learnflowserver.repository.EvidenceRepository;
import com.learnflow.learnflowserver.repository.NodeRepository;
import com.learnflow.learnflowserver.repository.StudentAssignmentRepository;
import com.learnflow.learnflowserver.service.ai.AiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // AI를 통한 요약 생성 (메인 노드의 내용과 근거 목록)
        List<String> contentToSummarize = new ArrayList<>();
        contentToSummarize.add(request.getContent());
        request.getEvidences().forEach(evidence -> contentToSummarize.add(evidence.getContent()));

        List<String> summaries = aiClient.getSummaries(contentToSummarize);

        // 메인 노드 생성 및 저장
        Node node = Node.builder()
                .studentAssignment(studentAssignment)
                .title(request.getTitle())
                .content(request.getContent())
                .summary(summaries.get(0))  // 첫 번째 요약은 메인 노드의 요약
                .type(NodeType.MAIN)
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

            evidences.add(evidenceRepository.save(evidence));
        }

        // 응답 생성
        List<EvidenceResponse> evidenceResponses = evidences.stream()
                .map(EvidenceResponse::from)
                .collect(Collectors.toList());

        return NodeResponse.of(savedNode, evidenceResponses);
    }

    public NodeDetailResponse getNodeDetail(Long nodeId) {
        // 노드 조회
        Node node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 노드를 찾을 수 없습니다."));

        // 노드에 연결된 근거들 찾기
        List<Evidence> evidences = node.getEvidences();

        // 근거 목록 변환
        List<EvidenceResponse> evidenceResponses = evidences.stream()
                .map(EvidenceResponse::from)
                .collect(Collectors.toList());

        // 노드 상세 정보 응답 생성
        return NodeDetailResponse.of(node, evidenceResponses);
    }

}