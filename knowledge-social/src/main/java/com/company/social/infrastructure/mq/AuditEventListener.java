package com.company.social.infrastructure.mq;

import com.company.content.domain.event.ContentSubmittedForAuditEvent;
import com.company.userauth.infrastructure.mapper.RoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnBean(RabbitTemplate.class)
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);
    private final EventPublisher eventPublisher;
    private final RoleMapper roleMapper;

    public AuditEventListener(EventPublisher eventPublisher, RoleMapper roleMapper) {
        this.eventPublisher = eventPublisher;
        this.roleMapper = roleMapper;
    }

    @EventListener
    public void onContentSubmittedForAudit(ContentSubmittedForAuditEvent event) {
        List<Long> adminUserIds = roleMapper.findAdminUserIds();
        if (adminUserIds.isEmpty()) {
            log.warn("No admin users found for audit notification");
            return;
        }
        for (Long adminId : adminUserIds) {
            eventPublisher.publishContentSubmittedForAudit(
                    adminId, event.getContentId(), event.getTitle(),
                    event.getSubmitterId(), event.getSubmitterName(), event.getTargetId());
        }
        log.info("Audit notification published to {} admin(s) for content: {}",
                adminUserIds.size(), event.getContentId());
    }
}
