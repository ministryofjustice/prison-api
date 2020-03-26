{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
{{- range $key, $value := .Values.secrets }}
  - name: {{ $key }}
    valueFrom:
      secretKeyRef:
        key: {{ $key }}
        name: {{ $.Chart.Name }}
{{- end }}
{{- end }}
