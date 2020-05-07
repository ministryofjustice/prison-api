{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: APPLICATION_INSIGHTS_IKEY
    valueFrom:
      secretKeyRef:
        key: APPLICATION_INSIGHTS_IKEY
        name: elite2-api
  - name: DATA_COMPLIANCE_INBOUND_DELETION_SQS_AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_INBOUND_DELETION_SQS_AWS_ACCESS_KEY_ID
        name: elite2-api
  - name: DATA_COMPLIANCE_INBOUND_DELETION_SQS_AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_INBOUND_DELETION_SQS_AWS_SECRET_ACCESS_KEY
        name: elite2-api
  - name: DATA_COMPLIANCE_INBOUND_DELETION_SQS_DLQ_AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_INBOUND_DELETION_SQS_DLQ_AWS_ACCESS_KEY_ID
        name: elite2-api
  - name: DATA_COMPLIANCE_INBOUND_DELETION_SQS_DLQ_AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_INBOUND_DELETION_SQS_DLQ_AWS_SECRET_ACCESS_KEY
        name: elite2-api
  - name: DATA_COMPLIANCE_INBOUND_DELETION_SQS_DLQ_NAME
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_INBOUND_DELETION_SQS_DLQ_NAME
        name: elite2-api
  - name: DATA_COMPLIANCE_INBOUND_DELETION_SQS_PROVIDER
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_INBOUND_DELETION_SQS_PROVIDER
        name: elite2-api
  - name: DATA_COMPLIANCE_INBOUND_DELETION_SQS_QUEUE_NAME
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_INBOUND_DELETION_SQS_QUEUE_NAME
        name: elite2-api
  - name: DATA_COMPLIANCE_INBOUND_DELETION_SQS_REGION
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_INBOUND_DELETION_SQS_REGION
        name: elite2-api
  - name: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_AWS_ACCESS_KEY_ID
        name: elite2-api
  - name: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_AWS_SECRET_ACCESS_KEY
        name: elite2-api
  - name: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_DLQ_AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_DLQ_AWS_ACCESS_KEY_ID
        name: elite2-api
  - name: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_DLQ_AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_DLQ_AWS_SECRET_ACCESS_KEY
        name: elite2-api
  - name: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_DLQ_NAME
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_DLQ_NAME
        name: elite2-api
  - name: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_PROVIDER
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_PROVIDER
        name: elite2-api
  - name: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_QUEUE_NAME
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_QUEUE_NAME
        name: elite2-api
  - name: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_QUEUE_URL
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_QUEUE_URL
        name: elite2-api
  - name: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_REGION
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_OUTBOUND_REFERRAL_SQS_REGION
        name: elite2-api
  - name: JWT_PUBLIC_KEY
    valueFrom:
      secretKeyRef:
        key: JWT_PUBLIC_KEY
        name: elite2-api
  - name: OAUTH_ENDPOINT_URL
    valueFrom:
      secretKeyRef:
        key: OAUTH_ENDPOINT_URL
        name: elite2-api
  - name: SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE
    valueFrom:
      secretKeyRef:
        key: SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE
        name: elite2-api
  - name: SPRING_DATASOURCE_PASSWORD
    valueFrom:
      secretKeyRef:
        key: SPRING_DATASOURCE_PASSWORD
        name: elite2-api
  - name: SPRING_DATASOURCE_URL
    valueFrom:
      secretKeyRef:
        key: SPRING_DATASOURCE_URL
        name: elite2-api
  - name: SPRING_PROFILES_ACTIVE
    valueFrom:
      secretKeyRef:
        key: SPRING_PROFILES_ACTIVE
        name: elite2-api
  - name: SPRING_REPLICA_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE
    valueFrom:
      secretKeyRef:
        key: SPRING_REPLICA_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE
        name: elite2-api
  - name: SPRING_REPLICA_DATASOURCE_PASSWORD
    valueFrom:
      secretKeyRef:
        key: SPRING_REPLICA_DATASOURCE_PASSWORD
        name: elite2-api
  - name: SPRING_REPLICA_DATASOURCE_URL
    valueFrom:
      secretKeyRef:
        key: SPRING_REPLICA_DATASOURCE_URL
        name: elite2-api
  - name: SPRING_REPLICA_DATASOURCE_USERNAME
    valueFrom:
      secretKeyRef:
        key: SPRING_REPLICA_DATASOURCE_USERNAME
        name: elite2-api
{{- end }}
