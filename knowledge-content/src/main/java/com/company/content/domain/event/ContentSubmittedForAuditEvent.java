package com.company.content.domain.event;

/**
 * 内容提交审核事件 — knowledge-content 发布，knowledge-social 监听后推送通知给管理员
 */
public class ContentSubmittedForAuditEvent {

    private final Long contentId;
    private final String title;
    private final Long submitterId;
    private final String submitterName;
    private final String targetType;
    private final Long targetId;

    public ContentSubmittedForAuditEvent(Long contentId, String title, Long submitterId,
                                         String submitterName, String targetType, Long targetId) {
        this.contentId = contentId;
        this.title = title;
        this.submitterId = submitterId;
        this.submitterName = submitterName;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    public Long getContentId() { return contentId; }
    public String getTitle() { return title; }
    public Long getSubmitterId() { return submitterId; }
    public String getSubmitterName() { return submitterName; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
}
