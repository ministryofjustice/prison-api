{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: JAVA_OPTS
    value: "{{ .Values.env.JAVA_OPTS }}"

  - name: DATA_COMPLIANCE_DELETION_ENABLED
    value: "{{ .Values.env.DATA_COMPLIANCE_DELETION_ENABLED }}"

  - name: APPLICATION_INSIGHTS_IKEY
    valueFrom:
      secretKeyRef:
        key: APPLICATION_INSIGHTS_IKEY
        name: {{ template "app.name" . }}

  - name: APPLICATIONINSIGHTS_CONNECTION_STRING
    value: "InstrumentationKey=$(APPLICATION_INSIGHTS_IKEY)"

  - name: DATA_COMPLIANCE_REQUEST_SQS_AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_REQUEST_SQS_AWS_ACCESS_KEY_ID
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_REQUEST_SQS_AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_REQUEST_SQS_AWS_SECRET_ACCESS_KEY
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_REQUEST_SQS_DLQ_AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_REQUEST_SQS_DLQ_AWS_ACCESS_KEY_ID
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_REQUEST_SQS_DLQ_AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_REQUEST_SQS_DLQ_AWS_SECRET_ACCESS_KEY
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_REQUEST_SQS_DLQ_NAME
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_REQUEST_SQS_DLQ_NAME
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_REQUEST_SQS_PROVIDER
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_REQUEST_SQS_PROVIDER
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_REQUEST_SQS_QUEUE_NAME
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_REQUEST_SQS_QUEUE_NAME
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_REQUEST_SQS_REGION
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_REQUEST_SQS_REGION
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_RESPONSE_SQS_AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_RESPONSE_SQS_AWS_ACCESS_KEY_ID
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_RESPONSE_SQS_AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_RESPONSE_SQS_AWS_SECRET_ACCESS_KEY
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_RESPONSE_SQS_DLQ_AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_RESPONSE_SQS_DLQ_AWS_ACCESS_KEY_ID
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_RESPONSE_SQS_DLQ_AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_RESPONSE_SQS_DLQ_AWS_SECRET_ACCESS_KEY
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_RESPONSE_SQS_DLQ_NAME
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_RESPONSE_SQS_DLQ_NAME
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_RESPONSE_SQS_PROVIDER
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_RESPONSE_SQS_PROVIDER
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_RESPONSE_SQS_QUEUE_NAME
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_RESPONSE_SQS_QUEUE_NAME
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_RESPONSE_SQS_QUEUE_URL
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_RESPONSE_SQS_QUEUE_URL
        name: {{ template "app.name" . }}
  - name: DATA_COMPLIANCE_RESPONSE_SQS_REGION
    valueFrom:
      secretKeyRef:
        key: DATA_COMPLIANCE_RESPONSE_SQS_REGION
        name: {{ template "app.name" . }}
  - name: SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE
    value: "{{ .Values.env.SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE }}"

  - name: SPRING_DATASOURCE_PASSWORD
    valueFrom:
      secretKeyRef:
        key: SPRING_DATASOURCE_PASSWORD
        name: {{ template "app.name" . }}
  - name: SPRING_DATASOURCE_URL
    valueFrom:
      secretKeyRef:
        key: SPRING_DATASOURCE_URL
        name: {{ template "app.name" . }}

  - name: SPRING_PROFILES_ACTIVE
    value: nomis

  - name: SPRING_REPLICA_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE
    value: "{{ .Values.env.SPRING_REPLICA_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE }}"

  - name: SPRING_REPLICA_DATASOURCE_PASSWORD
    valueFrom:
      secretKeyRef:
        key: SPRING_REPLICA_DATASOURCE_PASSWORD
        name: {{ template "app.name" . }}
  - name: SPRING_REPLICA_DATASOURCE_URL
    valueFrom:
      secretKeyRef:
        key: SPRING_REPLICA_DATASOURCE_URL
        name: {{ template "app.name" . }}
  - name: SPRING_REPLICA_DATASOURCE_USERNAME
    valueFrom:
      secretKeyRef:
        key: SPRING_REPLICA_DATASOURCE_USERNAME
        name: {{ template "app.name" . }}

  - name: SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI
    value: "{{ .Values.env.SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI }}"

  - name: SMOKE_TEST_AWARE
    value: "{{ .Values.env.SMOKE_TEST_AWARE }}"

{{- end }}
